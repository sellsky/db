package tk.bolovsrol.db.pool;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;

/** Генератор датасорса для Mysql. */
public class MysqlMetaDataSourceProvider implements MetaDataSourceProvider {

    @Override
    public MetaConnectionPoolDataSource getMetaConnectionPoolDataSource(ReadOnlyProperties pp, String dbUrl) throws UnexpectedBehaviourException {
        try {
            String serverName;
            {
                int po1 = dbUrl.indexOf("://") + 3;
                if (po1 == 2) {
                    throw new UnexpectedBehaviourException("Scheme is not specified.");
                }
                int po2 = dbUrl.indexOf((int) '/', po1);
                if (po2 == -1) {
                    po2 = dbUrl.indexOf((int) '?', po1);
                }
                if (po2 == -1) {
                    serverName = dbUrl.substring(po1);
                } else {
                    serverName = dbUrl.substring(po1, po2);
                }
            }
            MysqlConnectionPoolDataSource ds = new MysqlConnectionPoolDataSource();

            // tune-ups
            ds.setAutoReconnect(true);
            ds.setAutoReconnectForConnectionPools(true);
            ds.setCharacterEncoding("utf8");
            ds.setFailOverReadOnly(false);
            ds.setDumpQueriesOnException(true);
            ds.setDumpMetadataOnColumnNotFound(true);
            ds.setZeroDateTimeBehavior("convertToNull");
            // TRADITIONAL means STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER
            // ANSI means REAL_AS_FLOAT,PIPES_AS_CONCAT,ANSI_QUOTES,IGNORE_SPACE,ANSI
            ds.setSessionVariables("sql_mode='ANSI,TRADITIONAL'");
            ds.setUseUnicode(true);

            ds.setURL(dbUrl);
            return new MetaConnectionPoolDataSource(ds, serverName, dbUrl);
        } catch (Exception e) {
            throw new UnexpectedBehaviourException(e);
        }
    }
}
