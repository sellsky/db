package tk.bolovsrol.db.pool;

import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;

/**
 * Провайдер MetaConnectionPoolDataSource для базы данных.
 *
 * @see MetaConnectionPoolDataSource
 */
public interface MetaDataSourceProvider {

    /**
     * Принимает указанные параметры dbName и dbUrl и, если параметры
     * распознаются провайдером, возвращает структуру метаданных.
     * <p/>
     * Если параметры не относятся к данному провайдеру, он должен возвратить null.
     * <p/>
     * КоннекшнМенеджер при инициализации опрашивает все зарегистрированные
     * провайдеры, пока кто-нить из них не вернёт структуру метаданных.
     *
     * @param pp    проперти
     * @param dbUrl локатор базы данных
     * @return метадатасорс для данной базы данных или null, если базу неовзможно обслужить этим датасорспровайдером.
     */
    MetaConnectionPoolDataSource getMetaConnectionPoolDataSource(ReadOnlyProperties pp, String dbUrl) throws UnexpectedBehaviourException;

}
