package tk.bolovsrol.db.orm.sql.statements.select.orderby;


import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.conditions.Condition;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OrderByCondition implements OrderByEntity {

    private final Condition condition;
    private final Direction direction;

    public OrderByCondition(Condition condition, Direction direction) {
        this.condition = condition;
        this.direction = direction;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        condition.writeSqlExpression(sb, databaseProductName, tableAliases);
        direction.writeSqlExpression(sb, databaseProductName, tableAliases);
    }

    @Override
    public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        return condition.putValues(ps, pos);
    }

    @Override
    public void appendSqlLogValues(List<String> list) throws DbException {
        condition.appendSqlLogValues(list);
    }
}