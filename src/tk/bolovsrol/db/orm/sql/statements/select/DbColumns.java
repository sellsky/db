package tk.bolovsrol.db.orm.sql.statements.select;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.PuttingSqlExpression;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;
import tk.bolovsrol.utils.StringDumpBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** Контейнер для колонок. */
class DbColumns implements PuttingSqlExpression {

    private final LinkedHashSet<DbColumn> columns = new LinkedHashSet<>(32);

    public DbColumns() {
    }

    public DbColumns addAll(Collection<? extends DbColumn> columns) {
        this.columns.addAll(columns);
        return this;
    }

    public DbColumns add(DbColumn column) {
        columns.add(column);
        return this;
    }

    public DbColumns addAll(DbColumn[] columns) {
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    public DbColumns addAll(DbDataObject object) {
        this.columns.addAll(object.fields());
        return this;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        Iterator<DbColumn> it = columns.iterator();
        if (it.hasNext()) {
            sb.append(' ');
            it.next().writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
            while (it.hasNext()) {
                sb.append(',');
                it.next().writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
            }
        }
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        for (DbColumn column : columns) {
            pos = column.putValuesForSelect(ps, pos);
        }
        return pos;
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
        for (DbColumn column : columns) {
            column.appendSqlLogValuesForSelect(list);
        }
    }

    public int pickValues(ResultSet rs, int pos) throws SQLException, PickFailedException, DbException {
        for (DbColumn column : columns) {
            pos = column.pickValuesForSelect(rs, pos);
        }
        return pos;
    }

    public int size() {
        return columns.size();
    }

    public boolean isEmpty() {
        return columns.isEmpty();
    }

    public void clear() {
        columns.clear();
    }

    @Override
    public String toString() {
        return new StringDumpBuilder()
            .append("columns", columns)
            .toString();
    }

}
