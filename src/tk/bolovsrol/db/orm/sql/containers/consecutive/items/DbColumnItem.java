package tk.bolovsrol.db.orm.sql.containers.consecutive.items;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Какая-нибудь колонка, которая умеет записывать своё имя. */
public class DbColumnItem implements ConsecutiveItem {
    private final DbColumn<?> dbColumn;

    public DbColumnItem(DbColumn dbColumn) {
        this.dbColumn = dbColumn;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        dbColumn.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException {
        return dbColumn.putValuesForSelect(ps, pos);
    }

    @Override public void committed() {
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
        dbColumn.appendSqlLogValuesForSelect(list);
    }

    @Override
    public String toString() {
        return dbColumn.toString();
    }
}
