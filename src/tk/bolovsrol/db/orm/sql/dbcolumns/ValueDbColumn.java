package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.sql.conditions.Comparison;
import tk.bolovsrol.db.orm.sql.conditions.Condition;

/**
 * Колонка в БД, содержащая некоторое значение: DbColumn и DbValueContainer одновременно.
 *
 * @param <V> тип хранимого значения
 */
public interface ValueDbColumn<V> extends DbColumn<V>, DbValueContainer<V> {

    /**
     * @return болванка для самодельной колонки
     * @see ValueConsecutiveDbColumn
     */
    static <V> ValueConsecutiveDbColumn<V> custom(DbValueContainer<V> container) {
        return new ValueConsecutiveDbColumn<>(container);
    }

    // -- wrapping functions-modifiers
    default ValueDbColumn<V> distinct() {
        return UniFunction.distinct(this);
    }

    default ValueDbColumn<V> length() {
        return UniFunction.length(this);
    }

    default ValueDbColumn<V> min() {
        return UniFunction.min(this);
    }

    default ValueDbColumn<V> max() {
        return UniFunction.max(this);
    }

    // -- compare to own value
	default Condition eqSelf() {
		return Comparison.eqSelf(this);
	}

	default Condition neSelf() {
		return Comparison.neSelf(this);
	}

	default Condition leSelf() {
		return Comparison.leSelf(this);
	}

	default Condition ltSelf() {
		return Comparison.ltSelf(this);
	}

	default Condition geSelf() {
		return Comparison.geSelf(this);
	}

	default Condition gtSelf() {
		return Comparison.gtSelf(this);
	}

}
