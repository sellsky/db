package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.utils.StringDumpBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Пакет изменений в таблице для клиента бедного кэша:
 * доступны обновлённые данные и id созданных, обновлённых и удалённых записей.
 */
public class PoorVersionCacheChanges<O> implements UserVersionCacheChanges<O> {

    private final Map<Long, O> createdOrUpdated;
    private final Set<Long> createdIds;
    private final Set<Long> updatedIds;
    private final Set<Long> deletedIds;

    // Всякие удобства, вычисляемые из исходных данных.
    // Если нас спросят, мы тут закешируем их немножко
    private Set<Long> deletedOrUpdatedIds = null;
    private Map<Long, O> created = null;
    private Map<Long, O> updated = null;

    public PoorVersionCacheChanges(Map<Long, O> createdOrUpdated, Set<Long> createdIds, Set<Long> updatedIds, Set<Long> deletedIds) {
        this.createdOrUpdated = createdOrUpdated;
        this.createdIds = createdIds;
        this.updatedIds = updatedIds;
        this.deletedIds = deletedIds;
    }
// -------------- проверялки

    /** @return true, если есть созданные записи */
    public boolean hasCreated() {
        return !createdIds.isEmpty();
    }

    /** @return true, если есть обновлённые записи */
    public boolean hasUpdated() {
        return !updatedIds.isEmpty();
    }

    /** @return true, если есть созданные или обновлённые записи */
    public boolean hasCreatedOrUpdated() {
        return !createdIds.isEmpty() || !updatedIds.isEmpty();
    }

    /** @return true, если есть удалённые записи */
    public boolean hasDeleted() {
        return !deletedIds.isEmpty();
    }

    public boolean hasDeletedOrUpdated() {
        return !deletedIds.isEmpty() || !updatedIds.isEmpty();
    }

    // ------------ id

    /**
     * Возвращает сет id созданых записей.
     * Если ни одной записи не создано, вернётся пустой сет.
     * <p/>
     * При первичном наполнении слушателя тут будут id всех записей.
     *
     * @return id созданных записей (или пустой сет)
     */
    public Set<Long> getCreatedIds() {
        return createdIds;
    }

    /**
     * Возвращает сет id изменённых записей.
     * Если ни одна запись не изменена, вернётся пустой сет.
     *
     * @return id обновлённых записей (или пустой сет)
     */
    public Set<Long> getUpdatedIds() {
        return updatedIds;
    }

    /**
     * Возвращает сет id созданых и изменённых записей.
     * Если ни одной записи не создано и не изменено, вернётся пустой сет.
     * <p/>
     * При первичном наполнении слушателя тут будут id всех записей.
     *
     * @return id созданных и изменённых записей (или пустой сет)
     */
    public Set<Long> getCreatedOrUpdatedIds() {
        return createdOrUpdated.keySet();
    }

    /**
     * Возвращает сет id удалённых и изменённых записей.
     * Если ни одной записи не удалено и не изменено, вернётся пустой сет.
     *
     * @return id удалённых и изменённых записей (или пустой сет)
     */
    public Set<Long> getDeletedOrUpdatedIds() {
        if (deletedOrUpdatedIds == null) {
            if (deletedIds.isEmpty()) {
                deletedOrUpdatedIds = updatedIds;
            } else if (updatedIds.isEmpty()) {
                deletedOrUpdatedIds = deletedIds;
            } else {
                deletedOrUpdatedIds = new TreeSet<>();
                deletedOrUpdatedIds.addAll(deletedIds);
                deletedOrUpdatedIds.addAll(updatedIds);
            }
        }
        return deletedOrUpdatedIds;
    }

    /**
     * Возвращает сет id удалённых записей.
     * <p/>
     * Если ни одна запись не удалена, вернётся пустой сет.
     *
     * @return id удалённых записей (или пустой сет)
     */
    public Set<Long> getDeletedIds() {
        return deletedIds;
    }

    // ------- карты

    /**
     * Возвращает карту созданых записей.
     * Если ни одной записи не создано, вернётся пустая карта.
     * <p/>
     * При первичном наполнении слушателя тут будут все записи.
     *
     * @return id → объект (или пустая карта)
     */
    public Map<Long, O> getCreatedMap() {
        if (created == null) {
            created = new TreeMap<>();
            created.putAll(createdOrUpdated);
            created.keySet().retainAll(createdIds);
        }
        return created;
    }

    /**
     * Возвращает карту изменённых записей.
     * Если ни одна запись не изменена, вернётся пустая карта.
     *
     * @return id → объект (или пустая карта)
     */
    public Map<Long, O> getUpdatedMap() {
        if (updated == null) {
            updated = new TreeMap<>();
            updated.putAll(createdOrUpdated);
            updated.keySet().retainAll(updatedIds);
        }
        return updated;
    }

    /**
     * Возвращает карту созданых и изменённых записей.
     * Если ни одной записи не создано и не изменено, вернётся пустая карта.
     * <p/>
     * При первичном наполнении слушателя тут будут все записи.
     *
     * @return id → объект (или пустая карта)
     */
    public Map<Long, O> getCreatedOrUpdatedMap() {
        return createdOrUpdated;
    }

    // ---- #get~Items()

    /**
     * Возвращает коллекцию созданых записей.
     * Если ни одной записи не создано, вернётся пустая коллекция.
     * <p/>
     * При первичном наполнении слушателя тут будут все записи.
     *
     * @return объекты (или пустая коллекция)
     */
    public Collection<O> getCreatedItems() {
        return getCreatedMap().values();
    }

    /**
     * Возвращает коллекцию изменённых записей.
     * Если ни одна запись не изменена, вернётся пустая коллекция.
     *
     * @return объекты (или пустая коллекция)
     */
    public Collection<O> getUpdatedItems() {
        return getUpdatedMap().values();
    }

    /**
     * Возвращает коллекцию созданых и изменённых записей.
     * Если ни одной записи не создано и не изменено, вернётся пустая коллекция.
     * <p/>
     * При первичном наполнении слушателя тут будут все записи.
     *
     * @return объекты (или пустая коллекция)
     */
    public Collection<O> getCreatedOrUpdatedItems() {
        return getCreatedOrUpdatedMap().values();
    }

    @Override
    public String toString() {
        return new StringDumpBuilder()
            .append("createdIds", created == null ? Collections.emptySet() : created.keySet())
            .append("updatedIds", updated == null ? Collections.emptySet() : updated.keySet())
              .append("deletedIds", deletedIds)
              .toString();
    }

}