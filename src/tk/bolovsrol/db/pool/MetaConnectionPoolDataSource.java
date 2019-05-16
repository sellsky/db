package tk.bolovsrol.db.pool;

import javax.sql.ConnectionPoolDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/** Контейнер для информации о коннекшн-пул-датасорсе. */
public class MetaConnectionPoolDataSource {
    private final ConnectionPoolDataSource connectionPoolDataSource;
    private final String serverName;
    private final String dbUrl;
    private ConnectionsPool connectionsPool = null;

    public MetaConnectionPoolDataSource(ConnectionPoolDataSource connectionPoolDataSource, String serverName, String dbUrl) {
        this.connectionPoolDataSource = connectionPoolDataSource;
        this.serverName = serverName;
        this.dbUrl = dbUrl;
    }

    public Connection getConnection() throws SQLException, InterruptedException {
        return connectionsPool.getConnection(this);
    }

    public ConnectionPoolDataSource getConnectionPoolDataSource() {
        return connectionPoolDataSource;
    }

    public String getServerName() {
        return serverName;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public ConnectionsPool getConnectionsPool() {
        return connectionsPool;
    }

    public void setConnectionsPool(ConnectionsPool connectionsPool) {
        this.connectionsPool = connectionsPool;
    }
}
