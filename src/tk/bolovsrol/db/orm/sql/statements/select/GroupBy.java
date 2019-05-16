package tk.bolovsrol.db.orm.sql.statements.select;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.WritingSqlExpression;
import tk.bolovsrol.utils.StringDumpBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Контейнер для колонок. */
class GroupBy implements WritingSqlExpression {

    private final List<GroupByEntity> columns = new ArrayList<>();

    public GroupBy() {
    }

    public GroupBy add(Collection<? extends GroupByEntity> columns) {
        this.columns.addAll(columns);
        return this;
    }

    public GroupBy add(GroupByEntity entity) {
        columns.add(entity);
        return this;
    }

    public GroupBy add(GroupByEntity[] columns) {
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        Iterator<GroupByEntity> it = columns.iterator();
        if (it.hasNext()) {
            sb.append(" GROUP BY ");
            it.next().writeSqlExpression(sb, databaseProductName, tableAliases);
            while (it.hasNext()) {
                sb.append(',');
                it.next().writeSqlExpression(sb, databaseProductName, tableAliases);
            }
        }
    }

    @Override
    public String toString() {
        return new StringDumpBuilder()
                .append("columns", columns)
                .toString();
    }

}
