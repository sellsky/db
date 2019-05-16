package tk.bolovsrol.db.pool;

import oracle.jdbc.pool.OracleConnectionPoolDataSource;
import tk.bolovsrol.utils.RegexUtils;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;

/**
 * Генератор датасорса для Oracle с thin client.
 * <p/>
 * Строка адреса типа такой:
 * jdbc:oracle:thin:[USER/PASSWORD]@[HOST][:PORT]:SID
 * jdbc:oracle:thin:[USER/PASSWORD]@//[HOST][:PORT]/SERVICE
 * <p/>
 * http://www.orafaq.com/wiki/JDBC
 */
public class OracleThinMetaDataSourceProvider implements MetaDataSourceProvider {

    public static final int DEFAULT_PORT = 1521;

    @Override
    public MetaConnectionPoolDataSource getMetaConnectionPoolDataSource(ReadOnlyProperties pp, String dbUrl) throws UnexpectedBehaviourException {
        try {
            String connectionIdentifier = StringUtils.substring(dbUrl, "@", null);
            if (connectionIdentifier == null) {
                throw new IllegalArgumentException("No connection identifier is found in connection string.");
            }

            OracleConnectionPoolDataSource ds = new OracleConnectionPoolDataSource();
            ds.setURL(dbUrl);

            // несмотря на то, что в доке на сайте сказано, логин и пароль придётся устанавливать явно,
            // иначе коннектор выкинет непонятное NPE
            String[] loginpassword = RegexUtils.parse(":([^:/]+)/([^@]+)@", dbUrl);
            if (loginpassword.length == 2) {
                ds.setUser(loginpassword[0]);
                ds.setPassword(loginpassword[1]);
            }

            return new MetaConnectionPoolDataSource(ds, connectionIdentifier, dbUrl);
        } catch (Exception e) {
            throw new UnexpectedBehaviourException("Error resolving oracle thin source provider metadata for url " + Spell.get(dbUrl), e);
        }
    }
}