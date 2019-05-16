package tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.sql.SQLException;
import java.util.Map;

public class IfNullThenValues<V> implements InsertOrUpdateColumn<V> {

    private final DbColumn<V> column;

    public IfNullThenValues(DbColumn<V> column) {
        this.column = column;
    }

    @Override public void writeSqlExpressionForUpdate(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append("=IFNULL(");
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append(",VALUES(");
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append("))");
    }

    public static <V> IfNullThenValues<V> wrap(DbColumn<V> column) {
        return new IfNullThenValues<>(column);
    }
}


