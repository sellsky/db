package tk.bolovsrol.db.orm.versioncache;

/**
 * Клиент бедного кэша получает информацию об изменениях в наблюдаемой таблице.
 *
 * @param <O> кэшируемый класс
 */
public interface PoorVersionCacheClient<O> extends UserVersionCacheClient<O, PoorVersionCacheChanges<O>> {

}
