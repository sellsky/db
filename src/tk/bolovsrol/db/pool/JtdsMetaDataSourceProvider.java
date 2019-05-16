package tk.bolovsrol.db.pool;

import net.sourceforge.jtds.jdbcx.JtdsDataSource;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.Uri;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;

import java.sql.Connection;
import java.sql.SQLException;

/** Генератор датасорса для Microsoft SQL Server. */
public class JtdsMetaDataSourceProvider implements MetaDataSourceProvider {
    private boolean autoCommit = true;

    @Override
    public MetaConnectionPoolDataSource getMetaConnectionPoolDataSource(ReadOnlyProperties pp, String dbUrl) throws UnexpectedBehaviourException {
        try {
            Uri uri = Uri.parseUri(dbUrl);
            JtdsDataSource ds = new JtdsDataSource();

            if (!uri.hasHostname()) {
                throw new UnexpectedBehaviourException("Hostname is not specified.");
            }
            String serverName = uri.getHostname();
            ds.setServerName(serverName);
            if (uri.hasPort()) {
                ds.setPortNumber(uri.getPortIntValue());
            }
            if (uri.hasPath() && uri.getPath().length() > 1) {
                ds.setDatabaseName(uri.getPath().substring(1));
            }
            if (uri.hasUsername()) {
                ds.setUser(uri.getUsername());
            }
            if (uri.hasPassword()) {
                ds.setPassword(uri.getPassword());
            }
            ReadOnlyProperties rop = uri.queryProperties();
            if (rop.has("user")) {
                ds.setUser(rop.get("user"));
            }
            if (rop.has("password")) {
                ds.setPassword(rop.get("password"));
            }
            if (rop.has("characterEncoding")) {
                ds.setCharset(rop.get("characterEncoding"));
            }
            if (rop.has("autoCommit")) {
				autoCommit = rop.getBoolean("autoCommit", false);
			}

            return new MetaConnectionPoolDataSource(ds, serverName, dbUrl) {
                @Override
                public Connection getConnection() throws SQLException, InterruptedException {
                    Connection connection = super.getConnection();
                    connection.setAutoCommit(autoCommit);
                    return connection;
                }
            };
        } catch (Exception e) {
            throw new UnexpectedBehaviourException(e);
        }
    }
}
