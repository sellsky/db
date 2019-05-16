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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Сложное выражение, состоящее из некоторого перечня, с заранее заданным префиксом, инфиксами (разделителем) и суффиксом.
 */
public class ListingSqlExpression {

    private final ConsecutiveItem prefixItem;
    private final ConsecutiveItem delimiterItem;
    private final ConsecutiveItem suffixItem;
    protected final Collection<ConsecutiveItem> consecutiveItems = new ArrayList<>(16);

    protected ListingSqlExpression(ConsecutiveItem prefixItemOrNull, ConsecutiveItem delimiterItem, ConsecutiveItem suffixItemOrNull) {
        this.prefixItem = prefixItemOrNull;
        this.delimiterItem = delimiterItem;
        this.suffixItem = suffixItemOrNull;
    }

    protected ListingSqlExpression(String prefixItemOrNull, String delimiterItem, String suffixItemOrNull) {
        this(
            prefixItemOrNull == null ? null : new ConstantItem(prefixItemOrNull),
            new ConstantItem(delimiterItem),
            suffixItemOrNull == null ? null : new ConstantItem(suffixItemOrNull)
        );
    }

    protected void append(ConsecutiveItem c) {
        consecutiveItems.add(c);
    }

    protected void append(ConsecutiveItem... cs) {
        Collections.addAll(consecutiveItems, cs);
    }

    public ListingSqlExpression item(ConsecutiveItem c) {
        append(c);
        return this;
    }

    public ListingSqlExpression items(ConsecutiveItem... c) {
        append(c);
        return this;
    }

    public ListingSqlExpression str(String s) {
        append(new ConstantItem(s));
        return this;
    }

    public ListingSqlExpression col(DbColumn<?> column) {
        append(new DbColumnItem(column));
        return this;
    }

    public ListingSqlExpression val(DbValueContainer<?> container) {
        append(new ValueItem(container));
        return this;
    }

    protected void writeListSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        Iterator<ConsecutiveItem> iterator = consecutiveItems.iterator();
        if (!iterator.hasNext()) {
            throw new DbException("Empty list expression " + this);
        }
        if (prefixItem != null) { prefixItem.writeSqlExpression(sb, databaseProductName, tableAliases); }
        iterator.next().writeSqlExpression(sb, databaseProductName, tableAliases);
        do {
            delimiterItem.writeSqlExpression(sb, databaseProductName, tableAliases);
            iterator.next().writeSqlExpression(sb, databaseProductName, tableAliases);
        } while (iterator.hasNext());
        if (suffixItem != null) { suffixItem.writeSqlExpression(sb, databaseProductName, tableAliases); }
    }

    protected int putConsecutiveValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        // Вообще говоря, у префиксов не должно быть значений, но для стройности пусть будет
        if (prefixItem != null) { pos = prefixItem.putValues(ps, pos); }
        Iterator<ConsecutiveItem> iterator = consecutiveItems.iterator();
        pos = iterator.next().putValues(ps, pos);
        while (iterator.hasNext()) {
            pos = delimiterItem.putValues(ps, pos);
            pos = iterator.next().putValues(ps, pos);
        }
        if (suffixItem != null) { pos = suffixItem.putValues(ps, pos); }
        return pos;
    }

    protected void appendConsecutiveSqlLogValues(List<String> list) throws DbException {
        // Вообще говоря, у префиксов не должно быть значений, но для стройности пусть будет
        if (prefixItem != null) { prefixItem.appendSqlLogValues(list); }
        Iterator<ConsecutiveItem> iterator = consecutiveItems.iterator();
        iterator.next().appendSqlLogValues(list);
        while (iterator.hasNext()) {
            delimiterItem.appendSqlLogValues(list);
            iterator.next().appendSqlLogValues(list);
        }
        if (suffixItem != null) { suffixItem.appendSqlLogValues(list); }
    }

    protected void consecutiveCommitted() {
        if (prefixItem != null) { prefixItem.committed(); }
        delimiterItem.committed();
        for (ConsecutiveItem consecutiveItem : consecutiveItems) {
            consecutiveItem.committed();
        }
        if (suffixItem != null) { suffixItem.committed(); }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        if (prefixItem != null) { sb.append(prefixItem.toString()); }
        Iterator<ConsecutiveItem> iterator = consecutiveItems.iterator();
        if (iterator.hasNext()) {
            sb.append(iterator.next().toString());
            do {
                sb.append(delimiterItem.toString());
                sb.append(iterator.next().toString());
            } while (iterator.hasNext());
        }
        sb.append(suffixItem.toString());
        return sb.toString();
    }

}
