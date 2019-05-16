package tk.bolovsrol.db.pool;

import tk.bolovsrol.utils.log.Log;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Враппер вокруг PooledConnection, отслеживающий использование соединения.
 * <p/>
 * Доступ к врапперу, а также обработка событий слушателя
 * должны быть синхронизированы.
 */
public class PooledConnectionWrapper implements ConnectionEventListener {

    /** собственно физическое соединение */
    private final PooledConnection pooledConnection;

    private boolean idle = true;

    /** время, когда соединение было крайний раз в использовании. для вычисления бездельного возраста */
    private long lastAccessed;

    /** родительский пул, которому мы будем сообщать о закрытии соединений */
    private final PooledConnectionEventListener cp;

    /** метаинформация о соединении */
    private final MetaConnectionPoolDataSource ds;

    /**
     * Создаём пулд-коннекшн и регистрируем себя его слушателем.
     *
     * @param cp
     * @param ds
     * @throws SQLException
     */
    public PooledConnectionWrapper(PooledConnectionEventListener cp, MetaConnectionPoolDataSource ds) throws SQLException {
        this.cp = cp;
        this.ds = ds;
        pooledConnection = ds.getConnectionPoolDataSource().getPooledConnection();
        pooledConnection.addConnectionEventListener(this);
    }

    /**
     * Когда приложение закрыло соединение, мы говорим вышестоящему наблюдателю,
     * что соединение можно использовать снова.
     *
     * @param event
     */
    @Override public void connectionClosed(ConnectionEvent event) {
        idle = true;
        lastAccessed = System.currentTimeMillis();
        cp.connectionClosed(this);
    }

    /**
     * Приложение сломало соединение. Эээ... кто кого сломал?
     * Ну, в общем, они все отныне нежизнеспособны.
     *
     * @param event
     */
    @Override public void connectionErrorOccurred(ConnectionEvent event) {
        //Log.warning("Connection error reported! " + Log.getSpell(event.getSQLException()));
        pooledConnection.removeConnectionEventListener(this);
        try {
            pooledConnection.close();
        } catch (SQLException e) {
        }
        cp.connectionBroken(this);
    }

    /**
     * Возвращает соединение, которое может быть использовано приложением.
     *
     * @return
     * @throws SQLException
     */
    public Connection openConnection() throws SQLException {
        if (idle) {
//            Log.trace("Opening connection for " + Log.getSpell(calledBy) + "...");
            idle = false;
            lastAccessed = System.currentTimeMillis();
            return pooledConnection.getConnection();
        } else {
            throw new IllegalStateException("Connection already in use.");
        }
    }

    /**
     * Возвращает время, которое это соединение провело в праздности и бездеятельности.
     *
     * @return время в миллисекундах
     */
    public long getIdleAge() {
        return idle ? System.currentTimeMillis() - lastAccessed : 0L;
    }

    /**
     * Возвращает время, которое это соединение провело в трудах праведных.
     *
     * @return время в миллисекундах
     */
    public long getInUseAge() {
        return idle ? 0L : System.currentTimeMillis() - lastAccessed;
    }

    /** Принудительно закрываем соединение. */
    public void close() {
        if (idle) {
            try {
                pooledConnection.close();
            } catch (SQLException e) {
                Log.exception(e);
            }
        } else {
            throw new IllegalStateException("Connection is in use.");
        }
    }

    public MetaConnectionPoolDataSource getMetaConnectionPoolDataSource() {
        return ds;
    }
}
