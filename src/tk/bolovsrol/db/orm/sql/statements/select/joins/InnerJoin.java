package tk.bolovsrol.db.orm.sql.statements.select.joins;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.conditions.Condition;

/** Простой внутренний джойн */
public class InnerJoin extends AbstractJoin {
    public InnerJoin(DbDataObject joinTable, Condition condition) {
        super("INNER JOIN", joinTable, condition);
    }
}
