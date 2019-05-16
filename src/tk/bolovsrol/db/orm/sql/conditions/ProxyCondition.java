package tk.bolovsrol.db.orm.sql.conditions;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Условие, делегирующее все вызовы заданному делегату. */
public class ProxyCondition implements Condition {
    private Condition delegate;

    public ProxyCondition() {
    }

    public ProxyCondition(Condition delegate) {
        this.delegate = delegate;
    }

    public Condition get() {
        return delegate;
    }

    public void set(Condition delegate) {
        this.delegate = delegate;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        delegate.writeSqlExpression(sb, databaseProductName, tableAliases);
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        return delegate.putValues(ps, pos);
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
        delegate.appendSqlLogValues(list);
    }

    @Override public String toString() {
        return delegate == null ? "(empty proxy condition)" : delegate.toString();
    }
}
