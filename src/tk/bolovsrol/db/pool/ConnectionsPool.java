package tk.bolovsrol.db.pool;

import tk.bolovsrol.utils.QuitException;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Пул соединений с сервером баз данных.
 * <p/>
 * Здесь хранятся бездельничающие соединения
 * раздельно по базам данных (для повторного использования)
 * и большой кучей (для воспроизведения).
 * <p/>
 * Пул обслуживает запросы на выдачу соединения максимально экффективным образом.
 * Он же соблюдает ограничение на общее количество открытых соединений.
 * <p/>
 * Он скажет в лог варнинг, когда будет достингут лимит соединений.
 */
public class ConnectionsPool implements PooledConnectionEventListener {

    /**
     * Максимальное количество открываемых соединений к каждой из баз данных.
     * По умолчанию {@link #UNLIMITED_CONNECTIONS}.
     */
    public static final String DB_CONNECTIONS_MAX = "db.connections.max";

    /** Можно открывать бесконченое количество соединений. Не рекомендуется. */
    public static final int UNLIMITED_CONNECTIONS = 0;


    /**
     * Параметр: время спячки убивателя тунеядцев, мс.
     * По умолчанию {@link #DB_JUNKWATCHER_SLEEP_DEF}.
     */
    public static final String DB_JUNKWATCHER_SLEEP = "db.junkwatcher.sleep.ms";
    /** Время спячки убивателя тунеядцев по умолчанию 10 секунд. */
    public static final long DB_JUNKWATCHER_SLEEP_DEF = 10000L;


    /**
     * Параметр: время, которое соединение должно пробездельничать,
     * чтобы его можно было убивать как тунеядца, мс.
     * По умолчанию {@link #DB_JUNKWATCHER_AGETOKILL_DEF}.
     */
    public static final String DB_JUNKWATCHER_AGETOKILL = "db.junkwatcher.agetokill.ms";
    /** Время признания бездельника тунеядцем, по умолчанию 5 минут. */
    public static final long DB_JUNKWATCHER_AGETOKILL_DEF = 600000L;

    /** Ограничение количества соединений. */
    private final int maxConnections;

    /** Бездельничающие соединения (PooledConnectionWrapper), разложенные по именам баз данных. */
    private final Map<MetaConnectionPoolDataSource, LinkedList<PooledConnectionWrapper>> dbBindConnections
		= new HashMap<>();

    /**
     * Бездельничающие соединения, разложенные в хронологическом порядке: сверху наиболее
     * давно бездельничающие.
     */
	private final LinkedList<PooledConnectionWrapper> idleConnections = new LinkedList<>();

    /** Все созданные (открытые) соединения. Для статистики или типа того. */
	private final Collection<PooledConnectionWrapper> createdConnections = new LinkedList<>();

    /** Синхронизатор доступа. */
    private final Object lock = new Object();

    /** Время спячки чистильщика тунеядцев™. */
    private final long junkWatcherSleepTime;

    /** Время, через которое бездельничающее соединение признаётся тунеядцем™ и подлежит уничтожению. */
    private final long cwsIdleTimeToDie;

    private volatile boolean connectionAvailable;

    /**
     * Создаём инстанцию пула для сервера баз данных.
     *
     * @param pp
     * @param serverName
     */
    public ConnectionsPool(ReadOnlyProperties pp, String serverName) {
		maxConnections = pp.getInteger(DB_CONNECTIONS_MAX, UNLIMITED_CONNECTIONS, Log.getInstance());
		junkWatcherSleepTime = pp.getLong(DB_JUNKWATCHER_SLEEP, DB_JUNKWATCHER_SLEEP_DEF, Log.getInstance());
		cwsIdleTimeToDie = pp.getLong(DB_JUNKWATCHER_AGETOKILL, DB_JUNKWATCHER_AGETOKILL_DEF, Log.getInstance());

        if (junkWatcherSleepTime > 0L) {
            new JunkWatcher(serverName).start();
        }

    }

    /**
     * Выдаёт соединение к базе данных.
     *
     * @return соединение
     */
    public Connection getConnection(MetaConnectionPoolDataSource mcpds) throws SQLException, InterruptedException {
        //Log.trace("Retrieving connection to " + Log.getSpell(dbName) + " for class " + calledBy.getName() + "...");
        PooledConnectionWrapper connectionWrapper;
        synchronized (lock) {
            connectionWrapper = getConnectionWrapper(mcpds);
        }
        return connectionWrapper.openConnection();
    }

    /**
     * Получает свободный ConnectionWrapper, из которого можно вытянуть
     * Connection для использования приложением.
     * <p/>
     * Метод нужно вызывать в синхронизированном контексте!
     *
     * @param mcpds метадатасорс базы данных, к которой нужно прицепиться
     * @return
     * @throws SQLException
     * @throws InterruptedException
     */
    private PooledConnectionWrapper getConnectionWrapper(MetaConnectionPoolDataSource mcpds) throws SQLException, InterruptedException {
        while (true) {
            // сначала достаём существующее соединение.
            {
                PooledConnectionWrapper pcw = pickExistingConnectionWrapper(mcpds);
                if (pcw != null) {
                    return pcw;
                }
            }

            // а свободных открытых соединений нет. создадим новое?
            if (maxConnections == UNLIMITED_CONNECTIONS || createdConnections.size() < maxConnections) {
                // создавать новое соединение можно
                return createConnectionWrapper(mcpds);
            }

            // больше соединений создавать нельзя. позаимствуем бездельнчиающее соединение для другой базы
            if (!idleConnections.isEmpty()) {
                closeMostIdleConnectionWrapper();
                return createConnectionWrapper(mcpds);
            }

            // а бездельничающих соединений нет -- ждём их появления и ругаемся об этом,
            // так как это хоть и не ошибка, но ситуация, безусловно, нехорошая
            Log.warning("Connections pool size has reached max connections limit (" + maxConnections + "). Waiting for a connection to free.");
            connectionAvailable = false;
            while (!connectionAvailable) {
                lock.wait();
            }
            // теперь должно появиться хоть одно соединение либо возможность создать новое.
            // повторяем процесс получения соединения...
        }
    }

    /**
     * Создаёт новое соединение.
     * <p/>
     * Метод нужно вызывать в синхронизированном контексте!
     *
     * @param mcpds метадатасорс, для которого нужно получить соединение
     * @return
     * @throws SQLException
     */
    private PooledConnectionWrapper createConnectionWrapper(MetaConnectionPoolDataSource mcpds) throws SQLException {
        PooledConnectionWrapper pcw = new PooledConnectionWrapper(this, mcpds);
        createdConnections.add(pcw);
        return pcw;
    }

    /**
     * Закрывает самое старое соединение
     * <p/>
     * Метод нужно вызывать в синхронизированном контексте!
     */
    private void closeMostIdleConnectionWrapper() {
        // закроем соединение к самой старой базе
        PooledConnectionWrapper pcw = idleConnections.removeFirst();
        dbBindConnections.get(pcw.getMetaConnectionPoolDataSource()).remove(pcw);
        pcw.close();
        createdConnections.remove(pcw);
    }

    /**
     * Кладёт соединение в пул для повторного использования.
     * <p/>
     * Метод нужно вызывать в синхронизированном контексте!
     *
     * @param pcw
     */
    private void putConnectionWrapper(PooledConnectionWrapper pcw) {
        MetaConnectionPoolDataSource mcpds = pcw.getMetaConnectionPoolDataSource();
        LinkedList<PooledConnectionWrapper> cws = dbBindConnections.get(mcpds);
        if (cws == null) {
			cws = new LinkedList<>();
			dbBindConnections.put(mcpds, cws);
        }
        cws.addLast(pcw);
        idleConnections.addLast(pcw);
    }

    /**
     * Возвращает доступное открытое бездельничающее соединение для повторного использования.
     * Если таких нету, то возвращает null.
     * <p/>
     * Метод нужно вызывать в синхронизированном контексте!
     *
     * @param mcpds
     * @return соединение
     */
    private PooledConnectionWrapper pickExistingConnectionWrapper(MetaConnectionPoolDataSource mcpds) {
        LinkedList<PooledConnectionWrapper> cws = dbBindConnections.get(mcpds);
        if (cws == null || cws.isEmpty()) {
            return null;
        } else {
            PooledConnectionWrapper pcw = cws.removeLast();
            idleConnections.remove(pcw);
            return pcw;
        }
    }

    /**
     * КоннекшнВраппер сообщает, что его соединение закрыли, и он снова свободен.
     *
     * @param pcw
     */
    @Override public void connectionClosed(PooledConnectionWrapper pcw) {
        synchronized (lock) {
            putConnectionWrapper(pcw);
            connectionAvailable = true;
            lock.notifyAll();
        }
    }

    /**
     * КоннекшнВраппер сообщает, что его соединение сдохло, и на него можно больше не расчитывать.
     *
     * @param pcw
     */
    @Override public void connectionBroken(PooledConnectionWrapper pcw) {
        synchronized (lock) {
            createdConnections.remove(pcw);
            connectionAvailable = true;
            lock.notifyAll();
        }
    }

    /** Вычищатель тунеядцев™. */
    private class JunkWatcher extends Thread {

        private JunkWatcher(String serverName) {
            super();
            setDaemon(true);
            setName("JW-" + serverName);
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    // некоторое время спим...
                    try {
                        sleep(junkWatcherSleepTime);
                    } catch (InterruptedException e) {
                        throw new QuitException(e);
                    }

                    // затем вычищаем... ага, этих самых... тунеядцев™.
                    synchronized (lock) {
                        // так как соединения кладутся в idle-список в хронологическом порядке,
                        // первое встреченное живое соединение будет значить, что за ним смотреть нечего.
                        try {
                            PooledConnectionWrapper pcw = idleConnections.getFirst();
                            while (pcw.getIdleAge() >= cwsIdleTimeToDie) {
                                closeMostIdleConnectionWrapper();
                                connectionAvailable = true;
                                lock.notifyAll();
                                pcw = idleConnections.getFirst();
                            }
                        } catch (NoSuchElementException ignored) {
                        }
                    }
                }
            } catch (QuitException ignored) {
            } catch (Throwable e) {
                Log.exception(e);
            }
        }
    }

    public int getOpenConnectionsCount() {
        synchronized (lock) { // необходимо ли здесь? ну, пусть будет, некритично.
            return createdConnections.size();
        }
    }

    public int getIdleConnectionsCount() {
        synchronized (lock) {
            return idleConnections.size();
        }
    }

    public ConnectionInfo[] getConnectionInfos() {
		LinkedList<ConnectionInfo> infos = new LinkedList<>();
		synchronized (lock) {
            for (PooledConnectionWrapper pcw : createdConnections) {
                MetaConnectionPoolDataSource ds = pcw.getMetaConnectionPoolDataSource();
                infos.add(new ConnectionInfo(pcw.hashCode(),
                        ds.getServerName(), ds.getDbUrl(),
                        pcw.getIdleAge(), pcw.getInUseAge()));
            }
        }
        return infos.toArray(new ConnectionInfo[infos.size()]);
    }
}
