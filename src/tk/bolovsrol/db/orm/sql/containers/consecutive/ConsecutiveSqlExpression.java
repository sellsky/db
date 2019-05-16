package tk.bolovsrol.db.orm.sql.containers.consecutive;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConsecutiveItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConstantItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.DbColumnItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ValueItem;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * SQL-выражение, собираемое из последовательности элементов {@link ConsecutiveItem}
 * последовательным вызовом методов add(...).
 */
public class ConsecutiveSqlExpression {
    protected final Collection<ConsecutiveItem> consecutiveItems = new ArrayList<>(16);

    public ConsecutiveSqlExpression() {
    }

    protected void append(ConsecutiveItem c) {
        consecutiveItems.add(c);
    }

    public ConsecutiveSqlExpression item(ConsecutiveItem c) {
        append(c);
        return this;
    }

    public ConsecutiveSqlExpression str(String s) {
        append(new ConstantItem(s));
        return this;
    }

    public ConsecutiveSqlExpression col(DbColumn<?> column) {
        append(new DbColumnItem(column));
        return this;
    }

    public ConsecutiveSqlExpression col(String prefixStr, DbColumn<?> column) {
        append(new ConstantItem(prefixStr));
        append(new DbColumnItem(column));
        return this;
    }

    public ConsecutiveSqlExpression col(String prefixStr, DbColumn<?> column, String suffixStr) {
        append(new ConstantItem(prefixStr));
        append(new DbColumnItem(column));
        append(new ConstantItem(suffixStr));
        return this;
    }

    public ConsecutiveSqlExpression col(DbColumn<?> column, String suffixStr) {
        append(new DbColumnItem(column));
        append(new ConstantItem(suffixStr));
        return this;
    }

    public ConsecutiveSqlExpression val(DbValueContainer<?> container) {
        append(new ValueItem(container));
        return this;
    }

    public ConsecutiveSqlExpression val(String prefixStr, DbValueContainer<?> valueContainer) {
        append(new ConstantItem(prefixStr));
        append(new ValueItem(valueContainer));
        return this;
    }

    public ConsecutiveSqlExpression val(String prefixStr, DbValueContainer<?> valueContainer, String suffixStr) {
        append(new ConstantItem(prefixStr));
        append(new ValueItem(valueContainer));
        append(new ConstantItem(suffixStr));
        return this;
    }

    public ConsecutiveSqlExpression val(DbValueContainer<?> valueContainer, String suffixStr) {
        append(new ValueItem(valueContainer));
        append(new ConstantItem(suffixStr));
        return this;
    }

    protected void writeConsecutiveSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        for (ConsecutiveItem consecutiveItem : consecutiveItems) {
            consecutiveItem.writeSqlExpression(sb, databaseProductName, tableAliases);
        }
    }

    protected int putConsecutiveValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        for (ConsecutiveItem consecutiveItem : consecutiveItems) {
            pos = consecutiveItem.putValues(ps, pos);
        }
        return pos;
    }

    protected void appendConsecutiveSqlLogValues(List<String> list) throws DbException {
        for (ConsecutiveItem consecutiveItem : consecutiveItems) {
            consecutiveItem.appendSqlLogValues(list);
        }
    }

    protected void consecutiveCommitted() {
        for (ConsecutiveItem consecutiveItem : consecutiveItems) {
            consecutiveItem.committed();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        for (ConsecutiveItem consecutiveItem : consecutiveItems) {
            sb.append(consecutiveItem.toString());
        }
        return sb.toString();
    }
}
