package tk.bolovsrol.db.orm.versioncache;

/**
 * Клиент богатого кэша получает информацию об изменениях в наблюдаемой таблице,
 * все актуальные данные таблицы, а также удалённые записи.
 *
 * @param <O> кэшируемый класс
 */
public interface RichVersionCacheClient<O> extends UserVersionCacheClient<O, RichVersionCacheChanges<O>> {

}
