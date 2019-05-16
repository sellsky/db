package tk.bolovsrol.db.orm.sql.statements.select.orderby;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OrderByColumn implements OrderByEntity {

    private final DbColumn column;
    private final Direction direction;

    public OrderByColumn(DbColumn column, Direction direction) {
        this.column = column;
        this.direction = direction;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        direction.writeSqlExpression(sb, databaseProductName, tableAliases);
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        return column.putValuesForSelect(ps, pos);
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
        column.appendSqlLogValuesForSelect(list);
    }
}
