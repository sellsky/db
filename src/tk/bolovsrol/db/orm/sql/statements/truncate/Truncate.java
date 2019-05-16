package tk.bolovsrol.db.orm.sql.statements.truncate;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.statements.SimpleDbdoStatement;

public class Truncate extends SimpleDbdoStatement {

    public Truncate(DbDataObject dbdo) {
        super("TRUNCATE ", dbdo);
    }

}
