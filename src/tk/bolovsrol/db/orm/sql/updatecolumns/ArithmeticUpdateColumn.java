package tk.bolovsrol.db.orm.sql.updatecolumns;

import tk.bolovsrol.db.orm.containers.DbInteger;
import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Колонки с обнавлениями типа <code>column=column арифметический-оператор  value</code>.
 *
 * @param <V>
 */
public class ArithmeticUpdateColumn<V extends Number> implements NumericUpdateColumn<V> {

    private final static char PLUS = '+';
    private final static char MINUS = '-';
    private final static char MUL = '*';
    private final static char DIV = '/';
    private final static char MOD = '%';
    private final static char EQ = '=';

    // обычная единица подойдёт для инкремента/декремента чисел любого типа, поэтому без типа
    private final static DbValueContainer ONE_INT = new DbInteger(1);

    private final DbColumn<V> column;
    private final char operator;
    private final DbValueContainer<V> delta;


    public ArithmeticUpdateColumn(DbColumn<V> column, char operator, DbValueContainer<V> delta) {
        this.column = column;
        this.operator = operator;
        this.delta = delta;
    }

    @Override public void writeSqlExpressionForUpdate(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append('=');
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append(operator);
        sb.append('?');
    }

    @Override public int putValuesForUpdate(PreparedStatement ps, int pos) throws SQLException, DbException {
        delta.putValue(ps, pos);
        return pos + 1;
    }

    @Override public void appendSqlLogValuesForUpdate(List<String> list) throws DbException {
        list.add(delta.valueToSqlLogString());
    }

    public static <V extends Number> ArithmeticUpdateColumn<V> add(DbColumn<V> column, DbValueContainer<V> deltaContainer) {
        return new ArithmeticUpdateColumn<>(column, PLUS, deltaContainer);
    }

    public static <V extends Number> ArithmeticUpdateColumn<V> sub(DbColumn<V> column, DbValueContainer<V> deltaContainer) {
        return new ArithmeticUpdateColumn<>(column, MINUS, deltaContainer);
    }

    public static <V extends Number, C extends DbValueContainer<V>> ArithmeticUpdateColumn<V> add(DbDataField<V, ?> field, V value) {
        return new ArithmeticUpdateColumn<>(field, PLUS, field.wrap(value));
    }

    public static <V extends Number, C extends DbValueContainer<V>> ArithmeticUpdateColumn<V> sub(DbDataField<V, ?> field, V value) {
        return new ArithmeticUpdateColumn<>(field, MINUS, field.wrap(value));
    }

    @SuppressWarnings("unchecked") public static <V extends Number> ArithmeticUpdateColumn<V> inc(DbColumn<V> column) {
        return new ArithmeticUpdateColumn<>(column, PLUS, ONE_INT);
    }

    @SuppressWarnings("unchecked") public static <V extends Number> ArithmeticUpdateColumn<V> dec(DbColumn<V> column) {
        return new ArithmeticUpdateColumn<>(column, MINUS, ONE_INT);
    }

}
