package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.sql.conditions.Comparison;
import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.dbcolumns.LikeableDbColumn;
import tk.bolovsrol.utils.containers.StringContainer;

public interface DbStringContainer extends StringContainer, DbValueContainer<String> {

    default Condition like(LikeableDbColumn<?> column) {
        return Comparison.like(this, column);
    }

    default Condition notLike(LikeableDbColumn<?> column) {
        return Comparison.like(this, column);
    }

    default Condition regexp(LikeableDbColumn<?> column) {
        return Comparison.like(this, column);
    }

    default Condition notRegexp(LikeableDbColumn<?> column) {
        return Comparison.like(this, column);
    }


}
