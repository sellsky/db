package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.db.orm.sql.statements.select.Select;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.syncro.LockQueuedSynchronizer;
import tk.bolovsrol.utils.syncro.QueuedKey;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Менеджер предоставляет программам кэши таблиц-справочников из БД и принимает меры к поддержанию этих кэшей в актуальном состоянии.
 * <p>
 * Суть кэширования в следующем. В каждой таблице-справочнике заведены триггеры, которые при модификации данных в таблице
 * создают запись в таблице {@link VersionHistoryDbdo}: схема и название таблицы, ид записи и суть произошедшего (создание, изменение или удаление)..
 * <p>
 * Наблюдатель {@link VersionWatcher} проверяет новые записи в этой таблице каждые несколько секунд и уведомляет менеджера
 * о зафиксированных эволюциях. Менеджер считывает изменившиеся записи из таблиц и уведомляет о призошедшем кэши, один из которых,
 * так называемый бедный {@link PoorVersionCache}, распространяет эти данные среди своих клиентов, а второй,
 * богатый {@link RichVersionCache}, дополняет данные полным отображением справочника и даже удалёнными записями.
 * Кэши распространяют уведомление среди клиентов (прикладных классов), которые, в свою очередь, могут преобразовать уведомление
 * в подходящий для бизнес-логики вид.
 * <p>
 * Таким образом клиенты обеспечены актуальной — с лагом в несколько секунд — информацией из справочников с относительно дешёвым доступом .
 * <p>
 * Название «VersionCache» сложилось исторически, лучшего я придумать не могу. В начале это был простенький кэш,
 * а теперь он выродился в генератор-провайдер кэшей двух уровней и средство для поддержания их данных в актуальном состоянии.
 */
public class VersionCacheManager implements VersionWatcherListener {

    private static final VersionCacheManager INSTANCE;

    static {
        INSTANCE = new VersionCacheManager();
        Thread versionWatcherThread = new Thread(
            new VersionWatcher(INSTANCE, VersionCacheConst.REFRESH_INTERVAL),
            "VersionWatcher"
        );
        versionWatcherThread.setDaemon(true);
        versionWatcherThread.start();
    }

    /**
     * Агент хранит информацию о наблюдаемом объекте и обеспечивает связь кэша с менеджером.
     *
     * @param <O> тип кэшируемого объекта
     */
    static class Agent<O> {
        /** Класс — генератор кешируемых сущностей */
        public final Class<? extends CacheableDbDataObject<O>> cdbdoClass;
        /**
         * Инстанция класса—генератора, болванка для подгрузки изменений и создания из них кешируемых сущностей,
         * а также для всяких служебных нужд.
         */
        public final CacheableDbDataObject<O> cdbdo;
        /**
         * Каталог и таблица кешируемой сущности.
         */
        public final CatalogAndTableName catalogAndTableName;
        /**
         * Бедный кэш, если используется.
         * Его создают при первом вызове {@link tk.bolovsrol.db.orm.versioncache.VersionCacheManager#poor(Class)},
         * и он живёт до смерти джава-машины.
         */
        public PoorVersionCache<O> poor;
        /**
         * Бедный кэш, если используется.
         * Его создают при первом вызове {@link tk.bolovsrol.db.orm.versioncache.VersionCacheManager#rich(Class)},
         * и он живёт до смерти джава-машины.
         */
        public RichVersionCache<O> rich;

        /**
         * Мы тут храним полный набор данных, пока есть память, чтобы отсылать их бедному клиенту при регистрации клиентов.
         * Возможно, для огромных таблиц, которые компилируются в что-то менее огромное, хранить таблицу целиком в памяти —
         * не лучшая стратегия. Но на данное время памяти у нас явно больше, чем объём записей в таблицах,
         * поэтому мы вычитываем данные за один приём и хранить их в одном контейнере.
         */
        public SoftReference<Map<Long, O>> softDataMap = null;

        public Agent(Class<? extends CacheableDbDataObject<O>> cdbdoClass) {
            this.cdbdoClass = cdbdoClass;
            try {
                Constructor<? extends CacheableDbDataObject<O>> constructor;
                try {
                    constructor = cdbdoClass.getConstructor();
                } catch (NoSuchMethodException ignored) {
                    // создадим объект, даже если у него конструктор пекедж-локал! рефлекшън паўэр!
                    constructor = cdbdoClass.getDeclaredConstructor();
                }
                constructor.setAccessible(true);
                this.cdbdo = constructor.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot create an instance of " + Spell.get(cdbdoClass), e);
            }
            this.catalogAndTableName = new CatalogAndTableName(cdbdo.getSqlCatalogName(), cdbdo.getSqlTableName());
        }

        public Map<Long, O> retrieveDataMap() {
            return VersionCacheManager.retrieveAllData(this);
        }
    }

    /**
     * Рассылает изменения по объекту его возможным слушателям.
     *
     * @param <O> кешируемый класс
     */
    private static class Broadcaster<O> implements Runnable {
        private final VersionIdChangesContainer cc;
        private final Agent<O> ct;
        private final QueuedKey key;

        private Broadcaster(VersionIdChangesContainer cc, Agent<O> ct, QueuedKey key) {
            this.cc = cc;
            this.ct = ct;
            this.key = key;
        }

        @Override public void run() {
            try {
                // уберём ид, которые могли задвоиться из-за слишком шустрого пользователя:
                // - если пользователь удалил запись, то её не должно быть в созданных и обновлённых
                if (cc.deletedIds != null) {
                    if (cc.createdIds != null) {
                        cc.createdIds.removeAll(cc.deletedIds);
                    }
                    if (cc.updatedIds != null) {
                        cc.updatedIds.retainAll(cc.deletedIds);
                    }
                }
                // - и если пользователь создал и тут же обновил запись, она должна быть только в созданных
                if (cc.createdIds != null && cc.updatedIds != null) {
                    cc.updatedIds.removeAll(cc.createdIds);
                }

                key.synchronize();
                synchronized (ct) {
                    if (ct.rich == null && ct.poor == null) {
                        return;
                    }

                    // мы не хотим поддерживать это в живом состоянии
                    ct.softDataMap = null;

                    Map<Long, O> newData = retrieveData(ct.cdbdo, cc.createdIds, cc.updatedIds);

                    // с момента, как обыли обнаружены изменения, и то момента, когда мы вычитали данные из таблицы,
                    // некоторые записи пользователь мог и удалить, и мы их не загрузили
                    // значит, кэш уже ждёт следующее обновление с удалением этих записей, но лучше мы это сразу обнаружим
                    Set<Long> actuallyDeletedIds = cc.deletedIds;
                    if (newData != null) {
                        if (actuallyDeletedIds == null) {
                            actuallyDeletedIds = new TreeSet<>();
                        }
                        if (cc.createdIds != null) {
                            actuallyDeletedIds.addAll(cc.createdIds);
                            cc.createdIds.retainAll(newData.keySet());
                            actuallyDeletedIds.removeAll(cc.createdIds);
                        }
                        if (cc.updatedIds != null) {
                            actuallyDeletedIds.addAll(cc.updatedIds);
                            cc.updatedIds.retainAll(newData.keySet());
                            actuallyDeletedIds.removeAll(cc.updatedIds);
                        }
                    }

                    // ну вот, нам надо разослать изменения бедному и/или богатому кэшу
                    {
                        Map<Long, O> createdOrUpdated = newData == null ? Collections.emptyMap() : newData;
                        Set<Long> createdIds = cc.createdIds == null ? Collections.emptySet() : cc.createdIds;
                        Set<Long> updatedIds = cc.updatedIds == null ? Collections.emptySet() : cc.updatedIds;
                        Set<Long> deletedIds = actuallyDeletedIds == null ? Collections.emptySet() : actuallyDeletedIds;

                        if (ct.poor != null) {
                            ct.poor.update(createdOrUpdated, createdIds, updatedIds, deletedIds);
                        }
                        if (ct.rich != null) {
                            ct.rich.update(createdOrUpdated, createdIds, updatedIds, deletedIds);
                        }
                    }
                }
            } catch (InterruptedException ignored) {
                // пора завершать работу, обновления не нужны уж никому
            } finally {
                key.release();
            }
        }
    }

    /**
     * При необходимости создаёт и отдаёт бедного клиента для переданного класса.
     *
     * @param cdbdoClass
     * @param <O> кешируемый класс
     * @return
     */
    public static <O> PoorVersionCache<O> poor(Class<? extends CacheableDbDataObject<O>> cdbdoClass) {
        return INSTANCE.getPoorInternal(cdbdoClass);
    }

    /**
     * При необходимости создаёт и отдаёт богатого клиента для переданного класса..
     *
     * @param cdbdoClass
     * @param <O> кешируемый класс
     * @return
     */
    public static <O> RichVersionCache<O> rich(Class<? extends CacheableDbDataObject<O>> cdbdoClass) {
        return INSTANCE.getRichInternal(cdbdoClass);
    }


    /** Соблюдает порядок рассылки изменений по каждому из объектов, если в какой-то момент времени по объекту нужно разослать несколько пачек изменений. */
    private final LockQueuedSynchronizer<Class<? extends CacheableDbDataObject<?>>> broadcastSync = new LockQueuedSynchronizer<>();

    /** Агенты по именам — для рассылки изменений. */
    private final ConcurrentMap<CatalogAndTableName, Agent<?>> nameToAgent = new ConcurrentSkipListMap<>();

    /** Агенты по классам — для (раз)регистрации слушателей. */
    private final ConcurrentMap<Class<? extends CacheableDbDataObject<?>>, Agent<?>> classToAgent = new ConcurrentHashMap<>();

    private VersionCacheManager() {
    }

    /**
     * Бродкастит изменения по слушателям.
     *
     * @param changes
     */
    @Override public void versionChanged(Collection<VersionIdChangesContainer> changes) {
        for (VersionIdChangesContainer cc : changes) {
            Agent<?> ct = nameToAgent.get(cc.catalogAndTableName);
            if (ct == null) {
                continue;
            }
            if (VersionCacheConst.LOG_CHANGES) {
                StringDumpBuilder sb = new StringDumpBuilder(", ");
                appendReport(sb, cc.createdIds, "created");
                appendReport(sb, cc.updatedIds, "updated");
                appendReport(sb, cc.deletedIds, "deleted");
                Log.info(ct.catalogAndTableName + ": " + sb.toString() + "; current version " + cc.maxVersionId);
            }
            new Thread(
                new Broadcaster<>(cc, ct, broadcastSync.register(ct.cdbdoClass)),
                "VersionCacheUpdate-" + cc.maxVersionId + '-' + ct.catalogAndTableName
            ).start();
        }
    }

    private static void appendReport(StringDumpBuilder sdb, Set<Long> ids, String action) {
        if (ids == null || ids.isEmpty()) {
            return;
        } else if (ids.size() == 1) {
            sdb.append(action + " item id " + ids.iterator().next());
        } else {
            sdb.append(action + ' ' + ids.size() + " item(s) id " + StringUtils.enlistCollection(ids, " "));
        }
    }


    /**
     * Вернёт загруженные записи с указанными id, однако, если оба переданных сета нулы, вернётся нул.
     *
     * @param cdbdo
     * @param createdIdsOrNull
     * @param updatedIdsOrNull
     * @param <O>
     * @return карта актуальных записей или нул
     */
    private static <O> Map<Long, O> retrieveData(CacheableDbDataObject<O> cdbdo, Set<Long> createdIdsOrNull, Set<Long> updatedIdsOrNull) {
        Set<Long> idsToRead;
        if (createdIdsOrNull == null) {
            if (updatedIdsOrNull == null) {
                return null;
            } else {
                idsToRead = updatedIdsOrNull;
            }
        } else {
            if (updatedIdsOrNull == null) {
                idsToRead = createdIdsOrNull;
            } else {
                idsToRead = new TreeSet<>();
                idsToRead.addAll(createdIdsOrNull);
                idsToRead.addAll(updatedIdsOrNull);
            }
        }
        return retrieveData(cdbdo, idsToRead);
    }

    public <O> PoorVersionCache<O> getPoorInternal(Class<? extends CacheableDbDataObject<O>> cdbdoClass) {
        Agent<O> ct = getOrSpawnAgent(cdbdoClass);
        synchronized (ct) {
            if (ct.poor == null) {
                ct.poor = new PoorVersionCache<>(ct);
            }
        }
        return ct.poor;
    }

    public <O> RichVersionCache<O> getRichInternal(Class<? extends CacheableDbDataObject<O>> cdbdoClass) {
        Agent<O> ct = getOrSpawnAgent(cdbdoClass);
        synchronized (ct) {
            if (ct.rich == null) {
                ct.rich = new RichVersionCache<>(ct);
            }
        }
        return ct.rich;
    }

    @SuppressWarnings("unchecked")
    private <O> Agent<O> getOrSpawnAgent(Class<? extends CacheableDbDataObject<O>> cdbdoClass) {
        Agent<O> ct = (Agent<O>) classToAgent.get(cdbdoClass);
        if (ct == null) {
            ct = new Agent<>(cdbdoClass);
            Agent<O> duplicate = (Agent<O>) classToAgent.putIfAbsent(cdbdoClass, ct);
            if (duplicate != null) {
                ct = duplicate;
            } else {
                nameToAgent.put(ct.catalogAndTableName, ct);
                TriggerUtils.getInstance().checkTriggersAndTakeAction(ct.cdbdo, VersionCacheConst.TRIGGER_CHECK_ACTION);
            }
        }
        return ct;
    }


    private static <O> Map<Long, O> retrieveAllData(Agent<O> ct) {
        synchronized (ct) {
            Map<Long, O> data = ct.softDataMap == null ? null : ct.softDataMap.get();
            if (data == null) {
                data = retrieveData(ct.cdbdo, null);
                if (VersionCacheConst.LOG_CHANGES) {
                    Log.info(ct.catalogAndTableName + ": read " + (data.isEmpty() ? "no items" : data.size() == 1 ? "1 item" : data.size() + " item(s)"));
                }
            }
            ct.softDataMap = new SoftReference<>(data);
            return data;
        }
    }

    /**
     * Достаёт актуальные данные из таблицы-справочника.
     *
     * @param cdbdo объект таблицы
     * @param filterIdsOrNull сет интересующих записей или нул, если интересуют все записи
     * @param <O> кешируемый класс
     * @return запрошенные данные, находящиеся в БД
     */
    private static <O> Map<Long, O> retrieveData(final CacheableDbDataObject<O> cdbdo, Set<Long> filterIdsOrNull) {
        if (filterIdsOrNull != null && filterIdsOrNull.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, O> data = new ConcurrentHashMap<>();
        try (Connection con = ConnectionManager.getConnection()) {
            Select s = cdbdo.selectAllColumns();
            if (filterIdsOrNull != null) {
				s.where(cdbdo.idField().in(filterIdsOrNull));
			}
            s.browse(con, () -> {
                try {
                    data.put(cdbdo.getId(), cdbdo.getCacheItem());
                } catch (Exception e) {
                    Log.warning("Failed to retrieve Cache item from " + Spell.get(cdbdo), e);
                }
            });
        } catch (InterruptedException | SQLException e) {
            Log.exception(e);
        }
        return data;
    }

}
