package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConsecutiveItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.DbColumnItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ValueItem;
import tk.bolovsrol.utils.box.Box;

/**
 * Функция <code>IF(условие, значение-если-истина[, значение-если-ложь])</code>
 *
 * @param <V>
 */
public class If<V> extends ListingDbColumn<V> {
    private If(Condition condition, ConsecutiveItem thenTarget, ConsecutiveItem elseTargetOrNull) {
        super("IF(", ",", ")");
        condition(condition);
        append(thenTarget);
        if (elseTargetOrNull != null) { append(elseTargetOrNull); }
    }

    public If(Condition condition, DbColumn<V> thenColumn) {
        this(condition, new DbColumnItem(thenColumn), null);
    }

    public If(Condition condition, DbColumn<V> thenColumn, DbColumn<V> elseColumnOrNull) {
        this(condition, new DbColumnItem(thenColumn), Box.with(elseColumnOrNull).mapAndGet(DbColumnItem::new));
    }

    public If(Condition condition, DbValueContainer<V> thenValue) {
        this(condition, new ValueItem(thenValue), null);
    }

    public If(Condition condition, DbValueContainer<V> thenValue, DbColumn<V> elseColumnOrNull) {
        this(condition, new ValueItem(thenValue), Box.with(elseColumnOrNull).mapAndGet(DbColumnItem::new));
    }

    public If(Condition condition, DbDataField<V, ?> field, V thenValue) {
        this(condition, new ValueItem(field.wrap(thenValue)), null);
    }

    public If(Condition condition, DbDataField<V, ?> field, V thenValue, V elseValueOrNull) {
        this(condition, new ValueItem(field.wrap(thenValue)), Box.with(elseValueOrNull).map(field::wrap).mapAndGet(ValueItem::new));
    }
}


