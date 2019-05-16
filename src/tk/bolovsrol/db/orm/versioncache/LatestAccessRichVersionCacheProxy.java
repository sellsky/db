package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.db.orm.RecordNotFoundException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Прокся для {@link RichVersionCache}, фиксирующая id объектов, выданных методами {#getAndFixById()} и {#getAndFixByIdOrDie()},
 * при помощи {@link LatestAccessWriter}.
 *
 * @param <O>
 * @see #retrieve(RichVersionCache)
 */
public class LatestAccessRichVersionCacheProxy<O> {

    private static final Map<Class<? extends CacheableDbDataObject<?>>, LatestAccessRichVersionCacheProxy<?>> CACHE = new ConcurrentHashMap<>();

    /**
     * При необходимости создаёт и возвращает прокси для переданного кэша. Предполагается использовать как-то так:
     * <pre>
     * private static final LatestAccessRichVersionCacheProxy&lt;Foo&gt; CACHE =
     *     LatestAccessRichVersionCacheProxy.retrieve(VersionCache.rich(MetaFoo.class));
     *
     * public static Foo getByIdOrDie(Long id) throws RecordNotFoundException {
     *     return CACHE.getAndFixByIdOrDie(id);
     * }
     *
     * public static Foo getById(Long id) {
     *     return CACHE.getAndFixById(id);
     * }
     * </pre>
     *
     * @param cache
     * @param <O>
     * @return
     */
    public static <O> LatestAccessRichVersionCacheProxy<O> retrieve(RichVersionCache<O> cache) {
        @SuppressWarnings("unchecked") LatestAccessRichVersionCacheProxy<O> result = (LatestAccessRichVersionCacheProxy<O>) CACHE.get(cache.getDbdoClass());
        if (result == null) {
            result = new LatestAccessRichVersionCacheProxy<>(cache);
            CACHE.put(cache.getDbdoClass(), result);
        }
        return result;
    }


    private final RichVersionCache<O> cache;
    private final Class<? extends CacheableDbDataObject<O>> dbdoClass;

    private LatestAccessRichVersionCacheProxy(RichVersionCache<O> cache) {
        this.cache = cache;
        this.dbdoClass = cache.getDbdoClass();
    }

    /**
     * Возвращает кэшируемый элемент по его ид, и, если элемент с таким ид существует,
     * фиксирует факт использования этого элемента.
     *
     * @see RichVersionCache#getById(Long)
     */
    public O getAndFixById(Long id) {
        O result = cache.getById(id);
        if (result != null) {
            LatestAccessWriter.fix(dbdoClass, id);
        }
        return result;
    }

    /**
     * Возвращает кэшируемый элемент по его ид и, если элемент с таким ид существует,
     * фиксирует факт использования этого элемента.
     *
     * @see RichVersionCache#getByIdOrDie(Long)
     */
    public O getAndFixByIdOrDie(Long id) throws RecordNotFoundException {
        O result = cache.getByIdOrDie(id);
        LatestAccessWriter.fix(dbdoClass, id);
        return result;
    }

    /** @see RichVersionCache#getMap() */
    public ConcurrentMap<Long, O> getMap() {
        return cache.getMap();
    }

    /** @see RichVersionCache#getIds() */
    public Set<Long> getIds() {
        return cache.getIds();
    }

    /** @see RichVersionCache#getItems() */
    public Collection<O> getItems() {
        return cache.getItems();
    }

    /** @return проксируемый кэш */
    public RichVersionCache<O> getCache() {
        return cache;
    }

}
