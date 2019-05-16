package tk.bolovsrol.db.orm.manualcache;

import tk.bolovsrol.db.orm.RecordNotFoundException;
import tk.bolovsrol.db.orm.sql.statements.select.Select;
import tk.bolovsrol.db.orm.versioncache.CacheableDbDataObject;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.syncro.Locked;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

/**
 * Ручной кэш. Ему нужно говорить, что и когда обновилось, а он уже подтянется.
 * <p>
 * В конструкторе можно задать специальное условие для выборки из БД, в таком режиме кэш будет вычитывать только те записи, которые подходят под это условие.
 *
 * @param <D>
 * @param <C>
 */
public class ManualCache<D extends CacheableDbDataObject<C>, C> {

    private final HashMap<Long, C> data = new LinkedHashMap<>();
    private final D dbdo;
    private final BiConsumer<D, Select> selectCustomizer;

    private final Set<ManualCacheClient<C>> clients = new CopyOnWriteArraySet<>();
    private final Set<ManualCache<?, ?>> chain = new CopyOnWriteArraySet<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    private final String caption;

    /**
     * Регистрирует в кэше указанный БД-объект.
     *
     * @param dbdo
     * @param <D>
     * @param <C>
     * @return
     */
    public static <D extends CacheableDbDataObject<C>, C> ManualCache<D, C> register(D dbdo) {
        return register(dbdo, null);
    }

    /**
     * Регистрирует в кэше указанный БД-объект с модификатором селектов из БД, который фильтрует записи для кеширования.
     *
     * @param dbdo
     * @param selectCustomizer
     * @param <D>
     * @param <C>
     * @return
     */
    public static <D extends CacheableDbDataObject<C>, C> ManualCache<D, C> register(D dbdo, BiConsumer<D, Select> selectCustomizer) {
        ManualCache<D, C> cache = new ManualCache<D, C>(dbdo, selectCustomizer);
        ManualCacheManager.register(cache);
        return cache;
    }

    protected ManualCache(D dbdo, BiConsumer<D, Select> selectCustomizer) {
        this.dbdo = dbdo;
        this.selectCustomizer = selectCustomizer;
        StringBuilder sb = new StringBuilder(128);
        sb.append("Cache ").append(dbdo.getLogCatalogAndTableName());
        if (selectCustomizer != null) {
            sb.append(" customized via ").append(selectCustomizer.getClass().getSimpleName());
        }
        sb.append(" refreshed with ");
        this.caption = sb.toString();

        try {
            refresh(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize " + dbdo.getLogCatalogAndTableName() + " cache", e);
        }
    }

    protected ManualCache(D dbdo) {
        this(dbdo, null);
    }

    /**
     * Добавляет кэшу клиента, который будет получать карту актуальных данных по мере их обновления.
     * <p>
     * При регистрации клиент тут же получает актуальные данные.
     *
     * @param client
     * @return this
     */
    public ManualCache<D, C> withClient(ManualCacheClient<C> client) {
        clients.add(client);
        Locked.run(readLock, () -> client.cacheUpdated(data));
        return this;
    }

    /**
     * После обновления обновления кэша (кроме инициализации) запускает обновление клиентов указанного кэша.
     * <p>
     * Если клиент кэша использует данные другого кэша, то он может зарегистрировать свой кэш тут, чтобы получать обновления.
     *
     * @param clientCache клиентский кэш
     * @return this
     */
    public ManualCache<D, C> chainUpdates(ManualCache<?, ?> clientCache) {
        clientCache.checkUpdateChainLoop(this);
        chain.add(clientCache);
        return this;
    }

    private void checkUpdateChainLoop(ManualCache<?, ?> matter) {
        for (ManualCache<?, ?> chained : chain) {
            if (chained == matter) {
                throw new IllegalArgumentException("Manual Cache notification chain loop detected on " + Spell.get(matter));
            }
            chained.checkUpdateChainLoop(matter);
        }
    }

    @SuppressWarnings("unchecked") public Class<D> getDbdoClass() {
        return (Class<D>) dbdo.getClass();
    }

    private void refreshSync(Collection<Long> idsOrNull) throws Exception {
        D d = dbdo;
        Select s = d.select();
        if (selectCustomizer != null) {
            // Если установлен кастомайзер, нужно всегда вычитывать полный кэш, так как некоторые записи могут исчезать из вида кэша, перестав удовлетворять условию, и их мы уже не удалим никак.
            idsOrNull = null;
            selectCustomizer.accept(d, s);
        }

        Map<Long, C> itemsToDelete = new HashMap<>(data); // это элементы, которые надо удалить, ниже мы выкинем из этой карты все существующие в БД элементы

        // А: в БД не нул, в data нул;
        // Б: в БД не нул, в data не нул, и они отличаются
        Map<Long, C> createdItems;

        // А: в БД нул, в data не нул;
        // Б: в БД не нул, в data не нул, и они отличаются
        Map<Long, C> removedItems;

        if (idsOrNull != null) {
            s.where(d.idField().in(idsOrNull));
            itemsToDelete.keySet().retainAll(idsOrNull);
            createdItems = new HashMap<>(idsOrNull.size());
            removedItems = new HashMap<>();
        } else {
            createdItems = new HashMap<>();
            removedItems = new HashMap<>();
        }

        try (Connection con = ConnectionManager.getConnection()) {
            s.withFetchSize(Integer.MIN_VALUE).browse(con, () -> {
                Long id = d.getId();
                C newItem = d.getCacheItem();
                C obsoleteItem = itemsToDelete.remove(id);
                data.put(id, newItem);
                if (!Objects.equals(newItem, obsoleteItem)) {
                    // элемент появился либо обновился, засунем его в новые и, если он обновился, то и в старые
                    createdItems.put(id, newItem);
                    if (obsoleteItem != null) {
                        removedItems.put(id, obsoleteItem);
                    }
                }
            });
        }
        if (!itemsToDelete.isEmpty()) {
            for (Map.Entry<Long, C> entry : itemsToDelete.entrySet()) {
                Long id = entry.getKey();
                C obsoleteItem = entry.getValue();
                data.remove(id, obsoleteItem);
                // элемент удалился, засунем его в старые
                removedItems.put(id, obsoleteItem);
            }
        }
        Log.trace(caption + removedItems.size() + " removed and " + createdItems.size() + " created, total " + Spell.get(data.size()) + " item(s) / hard refresh");
        if (!removedItems.isEmpty() || !createdItems.isEmpty()) {
            broadcastUpdate();
        }
    }

    private void broadcastUpdate() {
        for (ManualCacheClient<C> ccc : clients) {
            ccc.cacheUpdated(data);
        }
        for (ManualCache<?, ?> chained : chain) {
            chained.updateClients();
        }
    }

    /**
     * Уведомляет кэш, что записи с указанными ид изменились.
     * <p>
     * Если в качестве коллекции ид передать нул, то кэш будет считать, что изменились все записи.
     *
     * @param idsOrNull
     * @throws Exception
     */
    public final void refresh(Collection<Long> idsOrNull) throws Exception {
        Locked.run(writeLock, () -> refreshSync(idsOrNull));
    }

    private void refreshLightSync(Map<Long, C> createdItems) throws Exception {
        Map<Long, C> removedItems = new LinkedHashMap<>(data);
        removedItems.keySet().retainAll(createdItems.keySet());
        data.putAll(createdItems);
        Log.trace(caption + removedItems.size() + " removed and " + createdItems.size() + " created, total " + Spell.get(data.size()) + " item(s) / light refresh");
        broadcastUpdate();
    }

    /**
     * Уведомляет кэш, что записи с указанными ид изменились, и передаёт эти новые данные. Кэш по возможности не будет обращаться к БД.
     *
     * @param updatedItems
     * @throws Exception
     */
    public void refreshLight(Map<Long, C> updatedItems) throws Exception {
        if (selectCustomizer != null) {
            refresh(null);
        } else {
            // хитрый режим без доступа к БД, обновлённое уже у нас в руках — используем его, если в кэше не установлено кастомных фильтров и, следовательно, все элементы подходят
            Locked.run(writeLock, () -> refreshLightSync(updatedItems));
        }
    }

    /**
     * @param id
     * @return хранимую в кэше запись с указанным ид либо нул, если такой записи не хранится
     */
    public C getById(Long id) {
        return Locked.call(readLock, () -> data.get(id));
    }

    /**
     * @param id
     * @return хранимую в кэше запись с указанным ид
     * @throws RecordNotFoundException запись с указанным ид в кэше не хранится
     */
    public C getByIdOrDie(Long id) throws RecordNotFoundException {
        C c = getById(id);
        if (c == null) {
            throw new RecordNotFoundException("No " + dbdo.getLogCatalogAndTableName() + " id=" + id + " is found");
        }
        return c;
    }

    /** @return копию набора ид хранимых записей */
    public Set<Long> getIds() {
        return Locked.call(readLock, () -> new LinkedHashSet<>(data.keySet()));
    }

    /** @return копию списка хранимых записей */
    public List<C> getObjects() {
        return Locked.call(readLock, () -> new ArrayList<>(data.values()));
    }

    /** @return копию карты ид→запись для хранимых записей */
    public Map<Long, C> getIdToObject() {
        return Locked.call(readLock, () -> new LinkedHashMap<>(data));
    }

    /**
     * Заставляет клиентов кэша получить актуальные данные.
     *
     * @see #chainUpdates(ManualCache).
     */
    public void updateClients() {
        Locked.run(readLock, this::broadcastUpdate);
    }
}
