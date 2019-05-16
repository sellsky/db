package tk.bolovsrol.db.orm.sql.statements;

import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class SimpleDbdoStatement extends AbstractSqlStatement {

    private final String statement;
    private final DbDataObject dbdo;

    public SimpleDbdoStatement(String statement, DbDataObject dbdo) {
        this.statement = statement;
        this.dbdo = dbdo;
    }

    public void execute(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(generateSqlExpression(con));
        try {
            ps.execute();
        } finally {
            JDBCUtils.close(ps);
        }
    }

    @Override protected void writeSqlExpression(StringBuilder sb, String databaseProductName) throws DbException, SQLException {
        sb.append(statement);
        dbdo.writeSqlExpression(sb, databaseProductName, null);
    }

    @Override protected void appendSqlLogValues(List<String> values) {

    }
}
