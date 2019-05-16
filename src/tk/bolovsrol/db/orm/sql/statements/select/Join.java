package tk.bolovsrol.db.orm.sql.statements.select;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.PuttingSqlExpression;

public interface Join extends PuttingSqlExpression {

    DbDataObject getTable();

}
