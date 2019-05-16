package tk.bolovsrol.db.orm.sql.statements.select.joins;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.statements.select.Join;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

class AbstractJoin implements Join {

    private final String mode;
    private final DbDataObject joinTable;
    private final Condition condition;

    AbstractJoin(String mode, DbDataObject joinTable, Condition condition) {
        this.mode = mode;
        this.joinTable = joinTable;
        this.condition = condition;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        sb.append(mode).append(' ');
        joinTable.writeSqlExpression(sb, databaseProductName, tableAliases);
        sb.append(" ON ");
        condition.writeSqlExpression(sb, databaseProductName, tableAliases);
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        return condition.putValues(ps, pos);
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
        condition.appendSqlLogValues(list);
    }

    @Override public DbDataObject getTable() {
        return joinTable;
    }
}
