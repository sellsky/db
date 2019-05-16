package tk.bolovsrol.db.orm.versioncache;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * База для богатого и бедного кэшей: регистрирует и разрегистрирует клиентов, принимает уведомления об изменении данных,
 * получает изменения и соответствующим образом их бродкастит. Клиентам лишь остаётся подготовить пакет изменений для своих клиентов.
 * <p/>
 * Дженерики не используются, чтобы обойтись без заумных кастов, принимая богатых и бедных клиентов;
 * контроль верности типа происходит явно в наследниках.
 *
 * @param <O> тип кэшируемых элементов
 */
@SuppressWarnings({"rawtypes", "unchecked"})
abstract class AbstractUserVersionCache<O> implements UserVersionCache<O> {

    protected final VersionCacheManager.Agent<O> agent;

    /**
     * Клиенты. Изменять коллекцию следует
     */
    protected final Queue<UserVersionCacheClient> clients = new ArrayDeque<>();

    protected AbstractUserVersionCache(VersionCacheManager.Agent<O> agent) {
        this.agent = agent;
    }

    /**
     * Регистрирует нового клиента и инициализирует его актуальными данными.
     *
     * @param client
     */
    protected void registerClientInternal(UserVersionCacheClient client) {
        synchronized (agent) {
            UserVersionCacheChanges changes = getInitialChanges();
            if (changes != null) {
                client.cacheChanged(changes);
            }
            this.clients.add(client);
        }
    }

    /**
     * Регистрирует новых клиентов и инициализирует их актуальными данными.
     * <p/>
     * Отдельный метод для регистрации нескольких клиентов позволяет немного оптимизировать процесс,
     * вычисляя данные для них лишь однажды.
     *
     * @param clients
     */
    protected void registerClientsInternal(UserVersionCacheClient... clients) {
        synchronized (agent) {
            UserVersionCacheChanges changes = getInitialChanges();
                for (UserVersionCacheClient client : clients) {
                    if (changes != null) {
                        client.cacheChanged(changes);
                    }
                    this.clients.add(client);
            }
        }
    }

    /**
     * Создаёт пакет изменений с первичными данными — как будто в кэше только что созданы все его записи.
     * <p/>
     * Вызывается в синхронизированном по контейнеру контексте.
     *
     * @return все данные
     */
    protected abstract UserVersionCacheChanges<O> getInitialChanges();

    @Override public void update(Map<Long, O> newObjects, Set<Long> createdIds, Set<Long> updatedIds, Set<Long> deletedIds) {
        // так как этот метод вызывается в синхронизированном по контейнеру контексте, мы тут с клиентами чё хотим делаем
        boolean hasClients = !clients.isEmpty();
        UserVersionCacheChanges changes = applyUpdateAndCollateChanges(newObjects, createdIds, updatedIds, deletedIds, hasClients);
        if (hasClients) {
            for (UserVersionCacheClient client : clients) {
                client.cacheChanged(changes);
            }
        }
    }

    /**
     * Обновляет состояние кэша в соответствии с переданными изменениями,
     * а также опционально приготовляет пакет изменений для клиентов.
     *
     * @param newObjects
     * @param createdIds
     * @param updatedIds
     * @param deletedIds
     * @param hasClients
     * @return
     */
    protected abstract UserVersionCacheChanges<O> applyUpdateAndCollateChanges(Map<Long, O> newObjects, Set<Long> createdIds, Set<Long> updatedIds, Set<Long> deletedIds, boolean hasClients);

    /**
     * Разрегистрирует клиента. Просто — клиент перестаёт получать обновления.
     * <p/>
     * Если клиент не зарегистрирован, ничего не делает.
     *
     * @param client клиент, которого надо разрегистрировать
     * @return был ли клиент разрегистрирован при этом исполнении метода
     */
    public boolean unregisterClient(UserVersionCacheClient client) {
        // мы просто перестаём уведомлять клиента; с контейнером нам делать ничего не следует, так как его могут использовать разными способами
        synchronized (agent) {
            return this.clients.remove(client);
        }
    }

    /** @return класс обслуживаемой таблицы */
    public Class<? extends CacheableDbDataObject<O>> getDbdoClass() {
        return agent.cdbdoClass;
    }

}
