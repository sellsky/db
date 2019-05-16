package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.db.orm.object.RefDbDataObject;

/**
 * Класс для справочников и прочего кеширования,
 * который умеет генерировать кешируемую сущность.
 */
public interface CacheableDbDataObject<O> extends RefDbDataObject {

    /**
     * Создаёт и возвращает кешируемый объект.
     * Он не привязан к полям БД-объекта.
     * <p/>
     * Если метод выкидывает исключение, то прежний объект , если был,
     * из кэша удаляется, а исключение записывается в лог.
     *
     * @return кешируемый объект.
     * @throws Exception ошибка создания кешируемого объекта.
     */
    O getCacheItem() throws Exception;

}
