package tk.bolovsrol.db.orm.sql.conditions;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.fields.DbDateField;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConstantItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.DbColumnItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ValueItem;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;
import tk.bolovsrol.utils.time.DateRange;

import java.util.Date;

/**
 * Условие пропускает значения, которые больше или равны нижней границе
 * и меньше верхней границы.
 * <p>
 * Т.е. from <= column < to
 *
 * @see DbColumn#inRange(DbValueContainer, DbValueContainer)
 * @see DbDataField#inRange(Object, Object)
 * @see DbDateField#inRange(DateRange)
 */
public class ColumnInRange extends ConsecutiveCondition {
    private static final ConstantItem OPEN_BRACKET = new ConstantItem("(");
    private static final ConstantItem LE = new ConstantItem("<=");
    private static final ConstantItem AND = new ConstantItem(" AND ");
    private static final ConstantItem LT = new ConstantItem("<");
    private static final ConstantItem CLOSE_BRACKET = new ConstantItem(")");

    public <V> ColumnInRange(DbValueContainer<V> fromInclusive, DbColumn<V> column, DbValueContainer<V> toExclusive) {
        DbColumnItem columnItem = new DbColumnItem(column);
        append(OPEN_BRACKET);
        append(new ValueItem(fromInclusive));
        append(LE);
        append(columnItem);
        append(AND);
        append(columnItem);
        append(LT);
        append(new ValueItem(toExclusive));
        append(CLOSE_BRACKET);
    }

    public <V, C extends DbValueContainer<V>> ColumnInRange(V fromInclusive, DbDataField<V, C> field, V toExclusive) {
        this(field.wrap(fromInclusive), field, field.wrap(toExclusive));
    }

    public ColumnInRange(DbDataField<Date, ? extends DbValueContainer<Date>> dateField, DateRange range) {
        this(dateField.wrap(range.getSince()), dateField, dateField.wrap(range.getUntil()));
    }
}
