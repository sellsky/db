package tk.bolovsrol.db.pool;

import tk.bolovsrol.db.DbProperties;
import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;
//import tk.bolovsrol.utils.properties.sources.TextFormatterReadOnlySource;
import tk.bolovsrol.utils.spawnmap.HashSpawnMap;
import tk.bolovsrol.utils.spawnmap.SpawnMap;
import tk.bolovsrol.utils.spawnmap.TreeSpawnMap;
import tk.bolovsrol.utils.textformatter.compiling.evaluators.MapEvaluator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Менеджер разделяемых соединений.
 * <p/>
 * Он один во всей джава-машине.
 * <p/>
 * Настройки менеджер берёт из {@link Cfg основного конфига}.
 * <p/>
 * Параметры конфига:
 * <ul><li><code>db.provider.<i>схема</i></code> &mdash; класс-провайдер соединений для указанной схемы;</li>
 * <li><code>db.url.<i>имя_БД</i></code> &mdash; описание базы данных с указанным именем;</li>
 * <li><code>db.startup.test</code> &mdash; если true, то при создании менеджера будет предпринята попытка соединения
 * с каждой БД, о неудачах будет написано в лог.</li></ul>
 * <p/>
 * Описание базы данных в виде URL:<br/>
 * <code>db.url.dbName = dbUrl</code><br/>, где dbName - название базы данных, а dbUrl - url для соединения с бд.
 * <p/>
 * Также для всех схем с совпадающим классом-провайдером будет создан один общий объект класса.
 */
public class ConnectionManager {

    /** Собственно менеджер. */
    private static final ConnectionManager INSTANCE = createConnectionManager(Cfg.getInstance());

    private static final String JDBC_PREFIX = "jdbc:";
    private static final String DB_PROVIDER_PREFIX = "provider.";
    private static final String DB_URL_PREFIX = "db.url.";
    private static final String DB_STARTUP_TEST = "db.startup.test";
    private static final String DEFAULT_DB_NAME_MASK = "";

    /**
     * Создаёт инстанцию менеджера.
     * <p/>
     * Пользуется стандартным стандартным логом.
     * <p/>
     * Читает и инициализирует схемы.
     * Затем парсит урлы.
     *
     * @return
     */
    private static ConnectionManager createConnectionManager(ReadOnlyProperties cfg) {
// это зачем-то было в пластике, вряд ли использовалось
//        cfg = new ReadOnlyProperties(
//              new TextFormatterReadOnlySource(
//                    cfg.getReadOnlySource(),
//                  new MapEvaluator(cfg.dump())
//              )
//        );

        // I. Cхемы.
        Map<String, MetaDataSourceProvider> schemeToProvider = new LinkedHashMap<>();
        {
            Map<String, MetaDataSourceProvider> classNameToProvider = new TreeMap<>();
            for (Map.Entry<String, String> entry : DbProperties.properties().getBranch(DB_PROVIDER_PREFIX).dump().entrySet()) {
                try {
                    MetaDataSourceProvider provider;
                    if (classNameToProvider.containsKey(entry.getValue())) {
                        provider = classNameToProvider.get(entry.getValue());
                    } else {
                        provider = (MetaDataSourceProvider) Class.forName(entry.getValue()).getConstructor().newInstance();
                        classNameToProvider.put(entry.getValue(), provider);
                    }
                    schemeToProvider.put(
                          entry.getKey(),
                          provider
                    );
                } catch (Exception e) {
                    Log.warning("Error creating an insance of provider class " + Spell.get(entry.getValue()) + " -> ", e);
                }
            }
        }

        // II. базы данных.
        // 1. получаем все урлы и упорядочиваем их
        SpawnMap<String, List<String>> urlToNameBindings = new HashSpawnMap<>(key -> new ArrayList<>());
        {
            for (Map.Entry<String, String> entry : cfg.getBranch(DB_URL_PREFIX).dump().entrySet()) {
                // dbname → dburl
                String dbName = entry.getKey();
                String dbUrl = entry.getValue();
                urlToNameBindings.getOrSpawn(dbUrl).add(dbName);
            }
            String defaultDbUrl = cfg.get("db.url");
            if (defaultDbUrl != null) {
                urlToNameBindings.getOrSpawn(defaultDbUrl).add(DEFAULT_DB_NAME_MASK);
            }
        }

        // группировка датасорсов по именам сервера
        SpawnMap<String, List<MetaConnectionPoolDataSource>> mdsToServerBindings = new TreeSpawnMap<>(key -> new ArrayList<>());
        Map<String, MetaConnectionPoolDataSource> nameToMdsBindings = new TreeMap<>();

        // 2. для каждого урла создаём метадатасорс и прописываем этот метадарасорс как соответствие всем именам
        {
            for (Map.Entry<String, List<String>> urlToNames : urlToNameBindings.entrySet()) {
                String dbUrl = urlToNames.getKey();
                List<String> dbNames = urlToNames.getValue();

                // нужно найти, какой провайдер возмётся за данную базу данных.
                String scheme = StringUtils.substring(dbUrl, ":", ":");
                if (!dbUrl.startsWith(JDBC_PREFIX) || scheme == null) {
                    Log.warning("Invalid db url " + Spell.get(dbUrl) + " scheme, ignored");
					continue;
				}

                MetaDataSourceProvider mdsp = schemeToProvider.get(scheme);
                if (mdsp == null) {
                    Log.warning("Unknown db url scheme " + Spell.get(scheme) + ", db url " + Spell.get(dbUrl) + " ignored");
					continue;
				}

                MetaConnectionPoolDataSource mds;
                try {
                    mds = mdsp.getMetaConnectionPoolDataSource(cfg, dbUrl);
                } catch (UnexpectedBehaviourException e) {
                    Log.warning("Cannot bind db url " + Spell.get(dbUrl) + ": ", e);
					continue;
				}
                if (mds == null) {
                    Log.warning("Provider " + Spell.get(scheme) + " return null data source for db url " + Spell.get(dbUrl) + ", ignored");
					continue;
				}

                // добавляем датасорс в папочку соответствующего сервера
                mdsToServerBindings.getOrSpawn(mds.getServerName()).add(mds);

                // прописываем метадатасорс именам баз
                for (String dbName : dbNames) {
                    nameToMdsBindings.put(dbName, mds);
                }
            }
        }

        // 3. ну и наконец для собранных серверов создаём коннекшн-пулы
        List<ConnectionsPool> connectionsPools = new ArrayList<>();
        {
            for (Map.Entry<String, List<MetaConnectionPoolDataSource>> entry : mdsToServerBindings.entrySet()) {
                String serverName = entry.getKey();
                List<MetaConnectionPoolDataSource> mdses = entry.getValue();

                ConnectionsPool cp = new ConnectionsPool(cfg, serverName);
                connectionsPools.add(cp);

                for (MetaConnectionPoolDataSource mdse : mdses) {
                    mdse.setConnectionsPool(cp);
                }
            }
        }

        ConnectionManager connectionManager = new ConnectionManager(nameToMdsBindings, connectionsPools);

        // 4. Тестируем каждое соединение.
		if (cfg.getBoolean(DB_STARTUP_TEST, false)) {
			for (String dbName : nameToMdsBindings.keySet()) {
				Connection con = null;
                try {
					con = connectionManager.getConnectionInternal(dbName, CommitMode.AUTO);
				} catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                } catch (Exception e) {
                    Log.warning("Database " + Spell.get(dbName) + " seems to be unavailable. " + Spell.get(e.getMessage()));
                } finally {
                    JDBCUtils.close(con);
                }
            }
        }

        return connectionManager;
    }

	/**
	 * Выдаёт соединение с базой данных по умолчанию.
	 * Вернётся соединение с включённым автокоммитом.
	 * <p>
	 * Если база данных по умолчанию не зарегистрирована,
	 * метод выбросит IllegalArgumentException.
	 *
	 * @return соединение
	 * @see #getConnection(String)
	 */
	public static Connection getConnection() throws SQLException, InterruptedException {
		return INSTANCE.getConnectionInternal(null, CommitMode.AUTO);
	}

	/**
	 * Выдаёт соединение с базой данных по умолчанию.
	 * <p/>
	 * Если база данных по умолчанию не зарегистрирована,
     * метод выбросит IllegalArgumentException.
	 *
	 * @param commitMode режим коммитов
	 * @return соединение
     * @see #getConnection(String)
	 */
	public static Connection getConnection(CommitMode commitMode) throws SQLException, InterruptedException, IllegalStateException {
		return INSTANCE.getConnectionInternal(null, commitMode);
	}

    /**
     * Выдаёт соединение с базой данных по умолчанию.
     * <p/>
     * К соединению методами {@link HookConnection#afterCommit(Runnable)} и {@link HookConnection#beforeRollback(Runnable)} можно прцеплять действия,
     * которые будут выполнены после коммита или перед ролбеком.
     * <p/>
     * Если база данных по умолчанию не зарегистрирована,
     * метод выбросит IllegalArgumentException.
     *
     * @param commitMode режим коммитов
     * @return соединение
     * @see #getConnection(String)
     */
    public static HookConnection getHookConnection(CommitMode commitMode) throws SQLException, InterruptedException, IllegalStateException {
        return new HookConnection(getConnection(commitMode));
    }

    /**
     * Выдаёт соединение с базой данных по её имени.
	 * Вернётся соединение с включённым автокоммитом.
	 * <p/>
	 * Если база данных с указанным именем не зарегистрирована,
     * метод выбросит IllegalArgumentException.
     * <p/>
     * В обычных условиях рекомендуется использовать метод {@link #getConnection()},
	 * возвращающий соединение с БД по умолчанию.
	 *
	 * @param dbName Имя базы данных.
     * @return соединение
     * @see #getConnection()
     */
    public static Connection getConnection(String dbName) throws SQLException, InterruptedException, IllegalStateException {
		return INSTANCE.getConnectionInternal(dbName, CommitMode.AUTO);
	}

	/**
	 * Выдаёт соединение с базой данных по её имени.
	 * <p>
	 * Если база данных с указанным именем не зарегистрирована,
	 * метод выбросит IllegalArgumentException.
	 * <p>
	 * В обычных условиях рекомендуется использовать метод {@link #getConnection(CommitMode)},
	 * возвращающий соединение с БД по умолчанию.
	 *
	 * @param dbName Имя базы данных.
	 * @param commitMode режим коммитов
	 * @return соединение
	 * @see #getConnection()
	 */
	public static Connection getConnection(String dbName, CommitMode commitMode) throws SQLException, InterruptedException, IllegalStateException {
		return INSTANCE.getConnectionInternal(dbName, commitMode);
	}

    /**
     * Возвращает информацию об открытых в данное время соединениях.
     *
     * @return структура с информацией
     */
    public static ConnectionInfo[] getConnectionInfos() throws IllegalStateException {
        return INSTANCE.getConnectionInfosInternal();
    }

    //------- dynamic fields below ----
    /** Привязка имени базы данных к метадатасорсу. */
    private final Map<String, MetaConnectionPoolDataSource> nameToMdsBindings;

    /** Ссылки на пулы соединений (для статистики) */
    private final List<ConnectionsPool> connectionsPools;

    public ConnectionManager(Map<String, MetaConnectionPoolDataSource> nameToMdsBindings, List<ConnectionsPool> connectionsPools) {
        this.nameToMdsBindings = nameToMdsBindings;
        this.connectionsPools = connectionsPools;
    }

    /**
     * Выдаёт соединение с заданной базой данных.
     *
     * @param dbName имя базы данных
	 * @param commitMode
	 * @return
	 * @throws SQLException
     * @throws InterruptedException
	 */
	private Connection getConnectionInternal(String dbName, CommitMode commitMode) throws SQLException, InterruptedException {
		MetaConnectionPoolDataSource poolDataSource = nameToMdsBindings.get(dbName == null ? DEFAULT_DB_NAME_MASK : dbName);
        if (poolDataSource == null) {
            throw new IllegalArgumentException((dbName == null ? "Default database " : "Database " + Spell.get(dbName)) + " is not registered.");
		}
		Connection con = poolDataSource.getConnection();
		commitMode.setup(con);
		return con;
	}

    /**
     * Возвращает информацию об открытых в данное время соединениях.
     *
     * @return структура с информацией
     */
    private ConnectionInfo[] getConnectionInfosInternal() {
        if (connectionsPools.size() == 1) {
            return connectionsPools.get(0).getConnectionInfos();
        } else {
            LinkedList<ConnectionInfo> infos = new LinkedList<>();
            for (ConnectionsPool connectionsPool : connectionsPools) {
                ConnectionInfo[] connectionInfos = connectionsPool.getConnectionInfos();
                infos.addAll(Arrays.asList(connectionInfos));
            }
            return infos.toArray(new ConnectionInfo[infos.size()]);
        }
    }
}
