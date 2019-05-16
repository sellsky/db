package tk.bolovsrol.db.orm.sql.statements.select.orderby;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OrderByNumber implements OrderByEntity {

    private final int number;
    private final Direction direction;

    public OrderByNumber(int number, Direction direction) {
        this.number = number;
        this.direction = direction;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        sb.append('?');
        direction.writeSqlExpression(sb, databaseProductName, tableAliases);
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        ps.setInt(pos, number);
        return pos + 1;
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
        list.add(String.valueOf(number));
    }
}
