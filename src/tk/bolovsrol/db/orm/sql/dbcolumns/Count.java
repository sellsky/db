package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;

import java.sql.SQLException;
import java.util.Map;

public class Count extends AbstractCount {

    private final DbColumn<?> column;
    private boolean distinct;

    public Count(DbColumn<?> column) {
        this.column = column;
    }

    @Override
    public void writeSqlExpressionForSelect(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        sb.append("COUNT( ");
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append(')');
    }

}
