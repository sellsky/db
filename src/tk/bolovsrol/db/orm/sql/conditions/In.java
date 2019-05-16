package tk.bolovsrol.db.orm.sql.conditions;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Сравнение со списком: <code>column [NOT ]IN(val1,val2,...,valN)</code>.
 */
public class In<V, C extends DbValueContainer<V>> implements Condition {

    private static final String IN = " IN(";
    private static final String NOT_IN = " NOT IN(";
    private static final String EQ = "=";
    private static final String NE = "<>";

    private final String multiComparison;
    private final String singleComparison;
    private final DbColumn<V> column;
    private final Collection<? extends C> containers;

    protected In(String multiComparison, String singleComparison, DbColumn<V> column, Collection<? extends C> containers) {
        if (containers == null || containers.isEmpty()) {
            throw new IllegalArgumentException("IN comparator doesn't permit empty argument list");
        }
        this.multiComparison = multiComparison;
        this.singleComparison = singleComparison;
        this.column = column;
        this.containers = containers;
    }

    protected In(String multiComparison, String singleComparison, DbColumn<V> column, C... containers) {
        if (containers == null || containers.length == 0) {
            throw new IllegalArgumentException("IN comparator doesn't permit empty argument list");
        }
        this.multiComparison = multiComparison;
        this.singleComparison = singleComparison;
        this.column = column;
        this.containers = Arrays.asList(containers);
    }

    @Override public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        int i = containers.size();
        if (i == 1) {
            sb.append(singleComparison).append('?');
        } else {
            sb.append(multiComparison);
            while (i > 0) {
                sb.append("?,");
                i--;
            }
            sb.setCharAt(sb.length() - 1, ')');
        }
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        pos = column.putValuesForSelect(ps, pos);
        for (DbValueContainer<V> container : containers) {
            container.putValue(ps, pos);
            pos++;
        }
        return pos;
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
        column.appendSqlLogValuesForSelect(list);
        for (DbValueContainer<V> container : containers) {
            list.add(container.valueToSqlLogString());
        }
    }

    // колонка vs. контейнер
    @SafeVarargs public static <V, C extends DbValueContainer<V>> In in(DbColumn<V> column, C... containers) {
        return new In<>(IN, EQ, column, containers);
    }

    public static <V, C extends DbValueContainer<V>> In in(DbColumn<V> column, Collection<? extends C> containers) {
        return new In<>(IN, EQ, column, containers);
    }

    @SafeVarargs public static <V, C extends DbValueContainer<V>> In notIn(DbColumn<V> column, C... containers) {
        return new In<>(NOT_IN, NE, column, containers);
    }

    public static <V, C extends DbValueContainer<V>> In notIn(DbColumn<V> column, Collection<? extends C> containers) {
        return new In<>(NOT_IN, NE, column, containers);
    }

    // поле vs. значение
    @SafeVarargs public static <V, C extends DbValueContainer<V>> In in(DbDataField<V, ? extends C> field, V... values) {
        return new In<>(IN, EQ, field, field.wrap(values));
    }

    public static <V, C extends DbValueContainer<V>> In in(DbDataField<V, ? extends C> field, Collection<? extends V> values) {
        return new In<>(IN, EQ, field, field.wrap(values));
    }

    @SafeVarargs public static <V, C extends DbValueContainer<V>> In notIn(DbDataField<V, ? extends C> field, V... values) {
        return new In<>(NOT_IN, NE, field, field.wrap(values));
    }

    public static <V, C extends DbValueContainer<V>> In notIn(DbDataField<V, ? extends C> field, Collection<? extends V> values) {
        return new In<>(NOT_IN, NE, field, field.wrap(values));
    }

}
