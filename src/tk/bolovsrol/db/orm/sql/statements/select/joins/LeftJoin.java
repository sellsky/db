package tk.bolovsrol.db.orm.sql.statements.select.joins;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.conditions.Condition;

/** Левый джойн */
public class LeftJoin extends AbstractJoin {
    public LeftJoin(DbDataObject joinTable, Condition condition) {
        super("LEFT JOIN", joinTable, condition);
    }
}
