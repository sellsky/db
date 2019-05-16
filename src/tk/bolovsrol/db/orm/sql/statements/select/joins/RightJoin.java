package tk.bolovsrol.db.orm.sql.statements.select.joins;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.conditions.Condition;

/** Правый джойн */
public class RightJoin extends AbstractJoin {
    public RightJoin(DbDataObject joinTable, Condition condition) {
        super("RIGHT JOIN", joinTable, condition);
    }
}
