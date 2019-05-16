package tk.bolovsrol.db.orm.sql.statements.locks;

import tk.bolovsrol.db.DatabaseProductNames;
import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.statements.AbstractSqlStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Lock используется для залочки таблиц.
 */
public class Lock extends AbstractSqlStatement {

	private DbDataObject[] dbdos;
	private boolean[] lockWrites;

	public Lock(DbDataObject[] dbdos, boolean[] lockWrites) {
		this.dbdos = dbdos;
		this.lockWrites = lockWrites;
		if (dbdos.length != lockWrites.length) {
			throw new IllegalArgumentException("Db data objects count does not match lockmodes count");
		}
	}

	public Lock(DbDataObject dbdo, boolean lockWrite) {
		this(new DbDataObject[]{dbdo}, new boolean[]{lockWrite});
	}

	public DbDataObject[] getDbdos() {
		return dbdos;
	}

	/**
	 * Загружает первый ряд резалтсета и закрывает его.
	 *
	 * @throws SQLException
	 */
	public Unlock execute(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(generateSqlExpression(con));
        try {
            ps.execute();
		} finally {
			JDBCUtils.close(ps);
		}
		return new Unlock(con);
	}

	@Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName) throws DbException, SQLException {
        // LOCK TABLES dbdo0,dbdo1 READ|WRITE
        switch (databaseProductName) {
        case DatabaseProductNames.MYSQL:
            sb.append("LOCK TABLES ");
            dbdos[0].writeSqlExpression(sb, databaseProductName, null);
            sb.append(lockWrites[0] ? " WRITE" : " READ");
            for (int i = 1; i < dbdos.length; i++) {
                sb.append(',');
                dbdos[i].writeSqlExpression(sb, databaseProductName, null);
                sb.append(lockWrites[i] ? " WRITE" : " READ");
            }
            break;

        case DatabaseProductNames.POSTGRESQL:
            sb.append("LOCK TABLE ");
            dbdos[0].writeSqlExpression(sb, databaseProductName, null);
            sb.append(" IN ").append(lockWrites[0] ? " ROW EXCLUSIVE" : " ROW SHARE").append(" MODE");
            for (int i = 1; i < dbdos.length; i++) {
                sb.append(',');
                dbdos[i].writeSqlExpression(sb, databaseProductName, null);
                sb.append(" IN ").append(lockWrites[i] ? " ROW EXCLUSIVE" : " ROW SHARE").append(" MODE");
            }
            break;

        default:
            throw new DbException("Unsupported instruction LOCK for DB " + databaseProductName);
        }
    }

	@Override
    protected void appendSqlLogValues(List<String> values) {
    }
}
