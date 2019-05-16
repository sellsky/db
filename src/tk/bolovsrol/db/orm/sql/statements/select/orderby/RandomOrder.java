package tk.bolovsrol.db.orm.sql.statements.select.orderby;


import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class RandomOrder implements OrderByEntity {

    public static final RandomOrder NOW = new RandomOrder("NOW()");

    private String order;

    /** Use intern {@link #NOW} for <code>RAND(NOW())</code>. */
    protected RandomOrder(String seed) {
        this.order = "RAND(" + seed + ')';
    }

    /**
     * Uses seed for random initializer.
     *
     * @param seed
     */
    public RandomOrder(int seed) {
        this(String.valueOf(seed));
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException {
        sb.append(order);
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        return pos;
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
    }
}
