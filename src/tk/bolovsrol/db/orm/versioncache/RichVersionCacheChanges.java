package tk.bolovsrol.db.orm.versioncache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Пакет изменений в таблице для клиента богатого кэша:
 * доступны обновлённые данные, id созданных, обновлённых и удалённых записей,
 * полная обновлённая карта элементов кэша, а также ставшие неактуальными (старые версии обновлённых и удалённые) элементы.
 * <p/>
 * Зарегистрированные клиенты получают пакет с изменениями перед тем,
 * как они станут доступны всей остальной системе.
 */
public class RichVersionCacheChanges<O> extends PoorVersionCacheChanges<O> {
    private final ConcurrentMap<Long, O> data;
    private final Map<Long, O> obsolete;

    public RichVersionCacheChanges(Map<Long, O> createdOrUpdated, Set<Long> createdIds, Set<Long> updatedIds, Set<Long> deletedIds, ConcurrentMap<Long, O> data, Map<Long, O> obsolete) {
        super(createdOrUpdated, createdIds, updatedIds, deletedIds);
        this.data = data;
        this.obsolete = obsolete;
    }

    // ------------ id

    /**
     * Возвращает сет id всех записей в кэше.
     *
     * @return id всех доступных записей
     */
    public Set<Long> getIds() {
        return data.keySet();
    }

    /**
     * Возвращает ключи записей, которые находились в кэше до обновления таблицы, и которые более в таблице не присутствуют.
     * Это как удалённые, так и перезаписанные записи.
     *
     * @return ключи записей, ставших неактуальными на момент обновления
     */
    public Set<Long> getObsoleteIds() {
        return obsolete.keySet();
    }


    // ------- карты

    /**
     * Возвращает карту всех объектов.
     *
     * @return карта id → объект
     */
    public ConcurrentMap<Long, O> getMap() {
        return data;
    }

    /**
     * Возвращает карту записей, которые находились в кэше до обновления таблицы, и которые более в таблице не присутствуют.
     * Здесь как удалённые, так и старые версии обновлённых записей.
     *
     * @return карта записей, ставших неактуальными на момент обновления
     */
    public Map<Long, O> getObsoleteMap() {
        return obsolete;
    }

    // ---- #get~Items()

    /**
     * Возвращает коллекцию всех записей кэша.
     *
     * @return все доступные записи.
     */
    public Collection<O> getItems() {
        return data.values();
    }

    /**
     * Возвращает коллекцию записей, которые находились в кэше до обновления таблицы, и которые более в таблице не присутствуют.
     * Здесь как удалённые, так и старые версии обновлённых записей.
     *
     * @return коллекция записей, ставших неактуальными на момент обновления
     */
    public Collection<O> getObsoleteItems() {
        return obsolete.values();
    }

}