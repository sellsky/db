package tk.bolovsrol.db.orm.versioncache;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Бедный кэш не хранит данных и предоставляет своим клиентам информацию о произошедших изменениях в обслуживаемой таблице.
 * <p/>
 * При инициализации, тем не менее, бедный кэш передаёт клиенту полный набор данных. Возможно, в будущем это поведение будет изменено так,
 * чтобы кэш передавал клиенту начальную информацию несколькими порциями последовательными вызовами {@link PoorVersionCacheClient#cacheChanged(UserVersionCacheChanges)}.
 *
 * @param <O> кешируемая сущность
 * @see VersionCacheManager#poor(Class)
 * @see RichVersionCache
 */
public class PoorVersionCache<O> extends AbstractUserVersionCache<O> {

    PoorVersionCache(VersionCacheManager.Agent<O> agent) {
        super(agent);
    }

    @Override protected UserVersionCacheChanges<O> getInitialChanges() {
        Map<Long, O> data = agent.retrieveDataMap();
        return new PoorVersionCacheChanges<>(data, data.keySet(), Collections.emptySet(), Collections.emptySet());
    }

    /**
     * Регистрирует бедного клиента.
     *
     * @param client
     */
    public void registerClient(PoorVersionCacheClient<O> client) {
        super.registerClientInternal(client);
    }

    /**
     * Регистрирует бедных клиентов.
     *
     * @param clients
     */
    @SafeVarargs public final void registerClients(PoorVersionCacheClient<O>... clients) {
        super.registerClientsInternal(clients);
    }

    /**
     * Упаковывает переданные данные в контейнер, ничего не добавляя.
     *
     * @param newObjects
     * @param createdIds
     * @param updatedIds
     * @param deletedIds
     * @param hasClients есть ли активные клиенты у кэша; если тут false, то метод может вернуть нул: изменения не будут использоваться
     * @return контейнер с переданными данными
     */
    @Override protected PoorVersionCacheChanges<O> applyUpdateAndCollateChanges(Map<Long, O> newObjects, Set<Long> createdIds, Set<Long> updatedIds, Set<Long> deletedIds, boolean hasClients) {
        return hasClients ? new PoorVersionCacheChanges<>(newObjects, createdIds, updatedIds, deletedIds) : null;
    }
}
