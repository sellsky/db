package tk.bolovsrol.db.orm.manualcache;

import tk.bolovsrol.db.orm.versioncache.CacheableDbDataObject;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.spawnmap.ConcurrentHashSpawnMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Диспетчер ручного кэша. Тут новые кэши регистрируются, и сюда нужно сообщать о внесённых в БД изменениях.
 */
public final class ManualCacheManager {

    private static final ConcurrentHashSpawnMap<Class<? extends CacheableDbDataObject<?>>, List<ManualCache>> CLASS_TO_CACHES = new ConcurrentHashSpawnMap<>(cl -> new ArrayList<>());

    private ManualCacheManager() {
    }

    /**
     * Регистрирует переданный кэш в менеджере. Удобнее пользоваться методом {@link ManualCache#register(CacheableDbDataObject)}.
     *
     * @param manualCache
     * @param <D>
     * @param <C>
     * @see ManualCache#register(CacheableDbDataObject)
     * @see ManualCache#register(CacheableDbDataObject, BiConsumer)
     */
    public static <D extends CacheableDbDataObject<C>, C> void register(ManualCache<D, C> manualCache) {
        CLASS_TO_CACHES.getOrSpawn(manualCache.getDbdoClass()).add(manualCache);
    }

    /**
     * Сигнализирует кэшу, что в БД неведомым образом обновлён объект указанного класса.
     * <p>
     * Класс может быть произвольным. Если для этого класса зарегистрированы кэши, то менеджер обновит их целиком. Иначе менеджер ничего не сделает.
     *
     * @param cl класс кэшируемого объекта
     * @return карта ошибок обновлений каждого из кэшей,
     */
    public static Map<ManualCache, Exception> update(Class<?> cl) {
        return update(cl, (Collection<Long>) null);
    }

    /**
     * Сигнализирует кэшу, что в БД обновлена запись указанного класса с указанным ид.
     * <p>
     * Класс может быть произвольным. Если для этого класса зарегистрированы кэши, то менеджер обновит в них запись с указанным ид. Иначе менеджер ничего не сделает.
     *
     * @param cl класс кэшируемого объекта
     * @param id ид записи
     * @return карта ошибок обновлений каждого из кэшей,
     */
    public static Map<ManualCache, Exception> update(Class<?> cl, Long id) {
        return update(cl, Collections.singletonList(id));
    }

    /**
     * Сигнализирует кэшу, что в БД обновлены записи указанного класса с указанными ид.
     * <p>
     * Класс может быть произвольным. Если для этого класса зарегистрированы кэши, то менеджер обновит в них закписи с указанными ид.
     * Если в качестве списка ид передать нул, менеджер обновит кэши целиком. Иначе менеджер ничего не сделает.
     *
     * @param cl
     * @param idsOrNull
     * @return
     */
    @SuppressWarnings("SuspiciousMethodCalls") public static Map<ManualCache, Exception> update(Class<?> cl, Collection<Long> idsOrNull) {
        List<ManualCache> list = CLASS_TO_CACHES.get(cl);
        if (list != null) {
            Map<ManualCache, Exception> ccToE = new LinkedHashMap<>(list.size());
            for (ManualCache cc : list) {
                try {
                    cc.refresh(idsOrNull);
                    ccToE.put(cc, null);
                } catch (Exception e) {
                    ccToE.put(cc, e);
                }
            }
            return ccToE;
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Сигнализирует кэшу, что в БД обновлена переданная запись, и теперь она вот такая.
     * <p>
     * Объект может быть любого класса. Если для этого класса зарегистрированы кэши, то менеджер обновит в них запись с указанным ид. Иначе менеджер ничего не сделает.
     *
     * @param updatedDbdo
     * @param <D>
     * @param <C>
     * @return карту обновлённых кэшей с исключениями, которые кэши выкинули при обновлении (или нулом, если кэш обновился без ошибок)
     */
    @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"}) public static <D extends CacheableDbDataObject<C>, C> Map<ManualCache, Exception> updateLight(Object updatedDbdo) {
        return updateLight(Collections.singletonList(updatedDbdo));
    }

    /**
     * Сигнализирует кэшу, что в БД обновлены переданные записи, и теперь они вот такие.
     * <p>
     * Класс записей в коллекции может быть произвольным, но все записи должны быть одного и того же класса.
     * Если для этого класса зарегистрированы кэши, то менеджер обновит в них закписи с указанными ид.
     * Иначе менеджер ничего не сделает.
     *
     * @param updatedDbdos
     * @param <D>
     * @param <C>
     * @return карту обновлённых кэшей с исключениями, которые кэши выкинули при обновлении (или нулом, если кэш обновился без ошибок)
     */
    @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"}) public static <D extends CacheableDbDataObject<C>, C> Map<ManualCache, Exception> updateLight(List updatedDbdos) {
        if (updatedDbdos == null || updatedDbdos.size() == 0) { return Collections.emptyMap(); }
        List<ManualCache> ccs = CLASS_TO_CACHES.get(updatedDbdos.get(0).getClass());
        if (ccs == null) { return Collections.emptyMap(); }
        // вот тут мы знаем, что в переданном списке у нас CacheableDbDataObject
        Map<ManualCache, Exception> ccToE = new LinkedHashMap<>(ccs.size());
        try {
            Map<Long, C> items = new LinkedHashMap<>(updatedDbdos.size());
            for (Object x : updatedDbdos) {
                CacheableDbDataObject<C> dbdo = (CacheableDbDataObject<C>) x;
                items.put(dbdo.getId(), dbdo.getCacheItem());
            }
            for (ManualCache cc : ccs) {
                try {
                    cc.refreshLight(items);
                    ccToE.put(cc, null);
                } catch (Exception e) {
                    ccToE.put(cc, e);
                }
            }
        } catch (Exception e) {
            // проблемы с формированием кэш-элемента, используем тяжёлый рефреш
            Log.trace("Light cache refresh for " + ((CacheableDbDataObject<C>) updatedDbdos.get(0)).getLogCatalogAndTableName() + " failed: " + Spell.get(e));
            ArrayList<Long> ids = new ArrayList<>(updatedDbdos.size());
            for (Object x : updatedDbdos) {
                CacheableDbDataObject<C> dbdo = (CacheableDbDataObject<C>) x;
                ids.add(dbdo.getId());
            }
            return update(updatedDbdos.get(0).getClass(), ids);
        }
        return ccToE;
    }

    /** @return набор классов, для которых зарегистрированы кэши */
    public static Set<Class<? extends CacheableDbDataObject<?>>> getRegisteredClasses() {
        return CLASS_TO_CACHES.keySet();
    }
}
