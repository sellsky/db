package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.containers.DbNumberContainer;
import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.sql.updatecolumns.ArithmeticUpdateColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.NumericUpdateColumn;

public interface NumericDbColumn<V extends Number> extends ValueDbColumn<V> {

    /**
     * @return болванка для самодельной колонки
     * @see NumericConsecutiveDbColumn
     */
    static <V extends Number> NumericConsecutiveDbColumn<V> custom(DbNumberContainer<V> container) {
        return new NumericConsecutiveDbColumn<>(container);
    }

    default ValueDbColumn<V> sum(DbValueContainer<V> target) {
        return UniFunction.sum(this, target);
    }

    default ValueDbColumn<V> sum() {
        return UniFunction.sum(this);
    }

    default NumericUpdateColumn<V> add(DbValueContainer<V> delta) {
        return ArithmeticUpdateColumn.add(this, delta);
    }

    default NumericUpdateColumn<V> sub(DbValueContainer<V> delta) {
        return ArithmeticUpdateColumn.sub(this, delta);
    }

    default NumericUpdateColumn<V> inc() {
        return ArithmeticUpdateColumn.inc(this);
    }

    default NumericUpdateColumn<V> dec() {
        return ArithmeticUpdateColumn.dec(this);
    }


}
