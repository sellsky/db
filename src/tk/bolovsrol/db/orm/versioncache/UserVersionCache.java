package tk.bolovsrol.db.orm.versioncache;

import java.util.Map;
import java.util.Set;

/**
 * Пользовательский кэш данных в БД.
 *
 * @param <O> кешируемый класс
 */
interface UserVersionCache<O> {

    /**
     * Информирует кэш об изменении состояния обслуживаемой таблицы.
     * <p/>
     * Гарантируется, что в карте количество новых объектов равно количеству записей в createdIds+updatedIds.
     * <p/>
     * А вот в deletedIds элементы могут повторяться в нескольких последовательных вызовах.
     * <p/>
     * Этот метод вызывается в синхронизированном по контейнеру контексте.
     *
     * @param newObjects
     * @param createdIds
     * @param updatedIds
     * @param deletedIds
     */
    void update(Map<Long, O> newObjects, Set<Long> createdIds, Set<Long> updatedIds, Set<Long> deletedIds);

}
