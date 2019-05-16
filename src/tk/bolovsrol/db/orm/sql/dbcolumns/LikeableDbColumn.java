package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.sql.conditions.Comparison;
import tk.bolovsrol.db.orm.sql.conditions.Condition;

/**
 * Маркер: колонка может участвовать в LIKE- и REGEXP-выражениях.
 */
public interface LikeableDbColumn<V> extends DbColumn<V> {

    default Condition regexp(String pattern) {
        return Comparison.regexpValue(this, pattern);
    }

    default Condition notRegexp(String pattern) {
        return Comparison.notRegexpValue(this, pattern);
    }

    default Condition like(String pattern) {
        return Comparison.likeValue(this, pattern);
    }

    default Condition notLike(String pattern) {
        return Comparison.notLikeValue(this, pattern);
    }

}
