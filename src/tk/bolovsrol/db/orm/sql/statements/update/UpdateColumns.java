package tk.bolovsrol.db.orm.sql.statements.update;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.PuttingSqlExpression;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.InsertOrUpdateColumn;
import tk.bolovsrol.utils.Spell;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Контейнер для колонок. */
public class UpdateColumns<V extends InsertOrUpdateColumn<?>> implements PuttingSqlExpression {

    private final List<V> columns = new ArrayList<>();

    public UpdateColumns() {
    }

    public UpdateColumns add(Collection<? extends V> columns) {
        this.columns.addAll(columns);
        return this;
    }

    public UpdateColumns add(V column) {
        columns.add(column);
        return this;
    }

    public UpdateColumns add(V... columns) {
        this.columns.addAll(Arrays.asList(columns));
        return this;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        if (!columns.isEmpty()) {
            columns.get(0).writeSqlExpressionForUpdate(sb, databaseProductName, tableAliases);
            for (int i = 1; i < columns.size(); i++) {
                sb.append(',');
                columns.get(i).writeSqlExpressionForUpdate(sb, databaseProductName, tableAliases);
            }
        }
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        for (V column : columns) {
            try {
                pos = column.putValuesForUpdate(ps, pos);
            } catch (RuntimeException e) {
                throw new DbException("Error setting value " + Spell.get(column), e);
            }
        }
        return pos;
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
        for (V column : columns) {
            column.appendSqlLogValuesForUpdate(list);
        }
    }

    public void committed() {
        for (V column : columns) {
            column.valueCommittedAtUpdate();
        }
    }

    @Override
    public String toString() {
        return Spell.get(columns);
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
}
