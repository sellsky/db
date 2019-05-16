package tk.bolovsrol.db.pool;

import org.mariadb.jdbc.MariaDbDataSource;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;

/** Генератор датасорса для Mysql. */
public class MariaDbMetaDataSourceProvider implements MetaDataSourceProvider {

    @Override
    public MetaConnectionPoolDataSource getMetaConnectionPoolDataSource(ReadOnlyProperties pp, String dbUrl) throws UnexpectedBehaviourException {
        try {
            String serverName;
            {
                int po1 = dbUrl.indexOf("://") + 3;
                if (po1 == 2) { throw new UnexpectedBehaviourException("Scheme is not specified."); }
                int po2 = dbUrl.indexOf((int) '/', po1);
                if (po2 == -1) { po2 = dbUrl.indexOf((int) '?', po1); }
                if (po2 == -1) { serverName = dbUrl.substring(po1); } else { serverName = dbUrl.substring(po1, po2); }
            }

            // db.url=jdbc:mariadb://127.0.0.1:3306/?user=username&password=12345678
            MariaDbDataSource ds = new MariaDbDataSource(dbUrl);

            // https://mariadb.com/kb/en/mariadb/about-mariadb-connector-j/
            ds.setProperties("autoReconnect=true");
            ds.setProperties("dumpQueriesOnException=true");
            ds.setProperties("sessionVariables=sql_mode='ANSI,TRADITIONAL'");

            return new MetaConnectionPoolDataSource(ds, serverName, dbUrl);
        } catch (Exception e) {
            throw new UnexpectedBehaviourException(e);
        }
    }
}
