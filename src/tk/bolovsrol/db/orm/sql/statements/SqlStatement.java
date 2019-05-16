package tk.bolovsrol.db.orm.sql.statements;

import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.utils.properties.Cfg;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlStatement {
    /** Путь к "чистому" sql-логу */
	String LOG_SQL_OUT = Cfg.get("log.sql.out", (String) null);
	/** Глобальный включатель логгирования sql-выражений в логе. */
	boolean LOG_SQL = Cfg.getBoolean("log.sql", LOG_SQL_OUT != null);

	boolean isAllowLogging();

    void setAllowLogging(boolean allowLogging);

    String generateSqlExpression(Connection con) throws SQLException, DbException;

    String generateSqlExpression(String databaseProductName, boolean fillInPlaceholders) throws DbException, SQLException;
}
