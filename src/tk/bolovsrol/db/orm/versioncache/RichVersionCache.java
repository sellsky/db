package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.db.orm.RecordNotFoundException;
import tk.bolovsrol.utils.Spell;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Богатый кэш хранит в памяти все кэшируемые элементы и предоставляет своим клиентам информацию о произошедших изменениях,
 * полный дамп кэшируемых элементов, а также элементы, удалённые из кэша при обновлении.
 * <p/>
 * В богатом кэше также можно зарегистрировать и клиента бедного кэша.
 * <p/>
 * Богатый кэш также сам по себе предоставляет несколько простых методов для доступа к хранящимся элементам.
 *
 * @param <O> кэшируемый класс
 * @see VersionCacheManager#rich(Class)
 * @see PoorVersionCache
 * @see LatestAccessRichVersionCacheProxy
 */
public class RichVersionCache<O> extends PoorVersionCache<O> {

    private final ConcurrentMap<Long, O> data = new ConcurrentHashMap<>();

    public RichVersionCache(VersionCacheManager.Agent<O> agent) {
        super(agent);
        data.putAll(agent.retrieveDataMap());
    }

    public void registerClient(RichVersionCacheClient<O> client) {
        super.registerClientInternal(client);
    }

    @SafeVarargs public final void registerClients(RichVersionCacheClient<O>... clients) {
        super.registerClientsInternal(clients);
    }

    @Override protected UserVersionCacheChanges<O> getInitialChanges() {
        return new RichVersionCacheChanges<>(data, data.keySet(), Collections.<Long>emptySet(), Collections.<Long>emptySet(), data, Collections.<Long, O>emptyMap());
    }

    /**
     * Упаковывает информацию об изменениях в таблице в контейнер, попутно обновляя внутреннюю копию кешируемых данных.
     *
     * @param newObjects
     * @param createdIds
     * @param updatedIds
     * @param deletedIds
     * @param hasClients
     * @return
     */
    @Override protected RichVersionCacheChanges<O> applyUpdateAndCollateChanges(Map<Long, O> newObjects, Set<Long> createdIds, Set<Long> updatedIds, Set<Long> deletedIds, boolean hasClients) {
        // Если есть клиенты, то подготовим им изменения, а если нет, то просто обновим карту
        RichVersionCacheChanges<O> result;
        if (hasClients) {
            // вытащим старые объекты, которые были обновлены или удалены, и поместим их в отдельную карту;
            Map<Long, O> obsoleteMap;
            if (!deletedIds.isEmpty() || !updatedIds.isEmpty()) {
                obsoleteMap = new HashMap<>();
                // мы не можем на этом этапе удалять обновлённые элементы из карты, а удалять элементы надо после обновлений, чтобы лишние обновлённые тоже грохнуть
                copyObsoletes(obsoleteMap, updatedIds);
                copyObsoletes(obsoleteMap, deletedIds);
            } else {
                obsoleteMap = Collections.emptyMap();
            }
            result = new RichVersionCacheChanges<>(newObjects, createdIds, updatedIds, deletedIds, data, obsoleteMap);
        } else {
            result = null;
        }

        data.putAll(newObjects);
        data.keySet().removeAll(deletedIds);
        return result;
    }

    private void copyObsoletes(Map<Long, O> obsoleteMap, Set<Long> idSource) {
        for (Long id : idSource) {
            O item = data.get(id);
            if (item != null) {
                obsoleteMap.put(id, item);
            }
        }
    }

    /**
     * Возвращает элемент по его id либо нул, если элемента с таким ид в таблице нет.
     *
     * @param id
     * @return элемент или нул
     */
    public O getById(Long id) {
        return data.get(id);
    }

    /**
     * Возвращает элемент по его id либо выкидывает исключение, если элемента с таким ид в таблице нет.
     *
     * @param id
     * @return элемент
     * @throws RecordNotFoundException элемент с указанным ид не найден
     */
    public O getByIdOrDie(Long id) throws RecordNotFoundException {
        O result = data.get(id);
        if (result == null) {
            throw new RecordNotFoundException("No " + agent.catalogAndTableName + " is found by id " + Spell.get(id));
        }
        return result;
    }

    /**
     * Возвращает живую карту данных кэша.
     * <p/>
     * Следует иметь в виду, что данные в карте могут изменяться с течением времени в соответствии с происходящими
     * в наблюдаемой таблцие изменениями, и наоборот, внесённые изменения будут видны кэшу.
     *
     * @return дамп
     */
    public ConcurrentMap<Long, O> getMap() {
        return data;
    }

    /**
     * Возвращает живой сет идентификаторов (ключей) кэша.
     * <p/>
     * Следует иметь в виду, что данные в сете могут изменяться с течением времени в соответствии с происходящими
     * в наблюдаемой таблцие изменениями, и наоборот, внесённые изменения будут видны кэшу.
     *
     * @return ключи
     */
    public Set<Long> getIds() {
        return data.keySet();
    }

    /**
     * Возвращает живой сет элементов кэша.
     * <p/>
     * Следует иметь в виду, что данные в коллекции могут изменяться с течением времени в соответствии с происходящими
     * в наблюдаемой таблцие изменениями, и наоборот, внесённые изменения будут видны кэшу.
     *
     * @return элементы
     */
    public Collection<O> getItems() {
        return data.values();
    }

}
