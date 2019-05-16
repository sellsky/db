package tk.bolovsrol.db.orm.versioncache;

/**
 * Клиент пользовательского кэша, получающий от кэша информацию о произошедших изменениях
 * той или иной степени насыщенности.
 *
 * @param <O> кэшируемый класс
 * @param <V> класс контейнера с изменениями
 */
interface UserVersionCacheClient<O, V extends UserVersionCacheChanges<O>> {
    /**
     * Уведомляет клиента пользовательского кэша о произошедших изменениях в наблюдаемой таблице.
     *
     * @param changes пакет изменений.
     */
    void cacheChanged(V changes);

}
