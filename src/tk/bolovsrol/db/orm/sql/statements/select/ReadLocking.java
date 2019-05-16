package tk.bolovsrol.db.orm.sql.statements.select;

import tk.bolovsrol.db.DatabaseProductNames;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.WritingSqlExpression;

import java.sql.SQLException;
import java.util.Map;

public class ReadLocking implements WritingSqlExpression {

    public static final ReadLocking LOCK_IN_SHARE_MODE = new ReadLocking(" LOCK IN SHARE MODE");
    public static final ReadLocking FOR_UPDATE = new ReadLocking(" FOR UPDATE");

    private final String mysqlText;

    protected ReadLocking(String mysqlText) {
        this.mysqlText = mysqlText;
    }

    @Override public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        if (DatabaseProductNames.MYSQL.equals(databaseProductName)) {
            sb.append(mysqlText);
        } else {
            throw new DbException("Read locking for " + databaseProductName + " is not yet supported");
        }
    }


}
