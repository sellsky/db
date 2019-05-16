package tk.bolovsrol.db.orm.sql.statements.locks;

import tk.bolovsrol.db.DatabaseProductNames;
import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.statements.AbstractSqlStatement;
import tk.bolovsrol.utils.Spell;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Lock используется для залочки таблиц.
 */
public class Unlock extends AbstractSqlStatement {

	private final Connection con;
	private static final String SQL = "UNLOCK TABLES";

	protected Unlock(Connection con) {
		this.con = con;
	}

	/**
	 * Загружает первый ряд резалтсета и закрывает его.
	 *
	 * @throws SQLException
	 */
	public void execute() throws SQLException {
		PreparedStatement ps = con.prepareStatement(generateSqlExpression(con));
		try {
			ps.execute();
		} finally {
			JDBCUtils.close(ps);
		}
	}

	@Override
	public void writeSqlExpression(StringBuilder sb, String databaseProductName) throws DbException, SQLException {
		String dbName = con.getMetaData().getDatabaseProductName();
		if (dbName.equals(DatabaseProductNames.MYSQL)) {
			sb.append(SQL);
		} else {
			throw new DbException("Unsupported instruction UNLOCK TABLES for DB: " + Spell.get(dbName));
		}
	}

	@Override
	protected void appendSqlLogValues(List<String> values) {
	}
}
