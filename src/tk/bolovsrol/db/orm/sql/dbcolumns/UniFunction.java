package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConstantItem;

/**
 * Колонка, которая фунция от какой-либо другой колонки:
 * <p>
 * FUNCTION(column)
 *
 * @param <V>
 */
public class UniFunction<V> extends ValueConsecutiveDbColumn<V> {

    private final static ConstantItem DISTINCT = new ConstantItem("DISTINCT(");
    private final static ConstantItem LENGTH = new ConstantItem("LENGTH(");
    private final static ConstantItem MAX = new ConstantItem("MAX(");
    private final static ConstantItem MIN = new ConstantItem("MIN(");
    private final static ConstantItem SUM = new ConstantItem("SUM(");
    private final static ConstantItem CLOSING_BRACKET = new ConstantItem(")");

    public UniFunction(ConstantItem prefix, DbColumn<V> column, DbValueContainer<V> container) {
        super(container);
        append(prefix);
        col(column);
        append(CLOSING_BRACKET);
    }

    public static <V> UniFunction<V> distinct(DbColumn<V> column, DbValueContainer<V> container) {
        return new UniFunction<>(DISTINCT, column, container);
    }

    public static <V> UniFunction<V> length(DbColumn<V> column, DbValueContainer<V> container) {
        return new UniFunction<>(LENGTH, column, container);
    }

    public static <V> UniFunction<V> max(DbColumn<V> column, DbValueContainer<V> container) {
        return new UniFunction<>(MAX, column, container);
    }

    public static <V> UniFunction<V> min(DbColumn<V> column, DbValueContainer<V> container) {
        return new UniFunction<>(MIN, column, container);
    }

    public static <V extends Number> UniFunction<V> sum(DbColumn<V> column, DbValueContainer<V> container) {
        return new UniFunction<>(SUM, column, container);
    }

    public static <V> UniFunction<V> distinct(ValueDbColumn<V> column) {
        return new UniFunction<>(DISTINCT, column, column);
    }

    public static <V> UniFunction<V> length(ValueDbColumn<V> column) {
        return new UniFunction<>(LENGTH, column, column);
    }

    public static <V> UniFunction<V> max(ValueDbColumn<V> column) {
        return new UniFunction<>(MAX, column, column);
    }

    public static <V> UniFunction<V> min(ValueDbColumn<V> column) {
        return new UniFunction<>(MIN, column, column);
    }

    public static <V extends Number> UniFunction<V> sum(NumericDbColumn<V> column) {
        return new UniFunction<>(SUM, column, column);
    }

}


