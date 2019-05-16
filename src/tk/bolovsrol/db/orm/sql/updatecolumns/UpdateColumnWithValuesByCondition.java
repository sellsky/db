package tk.bolovsrol.db.orm.sql.updatecolumns;

import tk.bolovsrol.db.DatabaseProductNames;
import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Обновляет колонку значением, определяемым вычислением указанного условия.
 * <p/>
 * В MySQL это, например, так:
 * UPDATE foo SET col=CASE WHEN id=1 THEN 'FOO' WHEN id in (2,3) THEN 'BAR' ELSE 'ZOOM' END WHERE id IN (1,2,3);
 * <p/>
 * В Орацле это, вероятно, так:
 * UPDATE foo SET col=CASE WHEN (id=1) THEN 'FOO' WHEN (id in (2,3)) THEN 'BAR' ELSE 'ZOOM' END WHERE id IN (1,2,3);
 */
public class UpdateColumnWithValuesByCondition<V> implements UpdateColumn<V> {

    private final DbColumn<V> updatingColumn;
    private final Map<? extends Condition, ? extends DbValueContainer<V>> conditionToValue;
    private final DbValueContainer<V> defaultValue;

    public UpdateColumnWithValuesByCondition(DbColumn<V> updatingColumn, Map<? extends Condition, ? extends DbValueContainer<V>> conditionToValue) {
        this(updatingColumn, conditionToValue, null);
    }

    public UpdateColumnWithValuesByCondition(DbColumn<V> updatingColumn, Map<? extends Condition, ? extends DbValueContainer<V>> conditionToValue, DbValueContainer<V> defaultValue) {
        this.updatingColumn = updatingColumn;
        this.conditionToValue = conditionToValue;
        this.defaultValue = defaultValue;
    }

    @Override
    public void writeSqlExpressionForUpdate(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        if (DatabaseProductNames.MYSQL.equals(databaseProductName)) {
            updatingColumn.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
            sb.append("=CASE");
            for (Condition condition : conditionToValue.keySet()) {
                sb.append(" WHEN ");
                condition.writeSqlExpression(sb, databaseProductName, tableAliases);
                sb.append(" THEN ?");
            }
            if (defaultValue != null) {
                sb.append(" ELSE ?");
            }
            sb.append("END");
        } else if (DatabaseProductNames.ORACLE.equals(databaseProductName)) {
            updatingColumn.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
            sb.append("=CASE");
            for (Condition condition : conditionToValue.keySet()) {
                sb.append(" WHEN(");
                condition.writeSqlExpression(sb, databaseProductName, tableAliases);
                sb.append(")THEN ?");
            }
            if (defaultValue != null) {
                sb.append(" ELSE ?");
            }
            sb.append(" END");
        } else {
            throw new UnsupportedOperationException("Don't know how to encode UpdateColumnWithCaseValue on " + databaseProductName);
        }
    }

    @Override public int putValuesForUpdate(PreparedStatement ps, int pos) throws SQLException, DbException {
        for (Map.Entry<? extends Condition, ? extends DbValueContainer> entry : conditionToValue.entrySet()) {
            pos = entry.getKey().putValues(ps, pos);
            entry.getValue().putValue(ps, pos++);
        }
        if (defaultValue != null) {
            defaultValue.putValue(ps, pos++);
        }
        return pos;
    }

    @Override public void appendSqlLogValuesForUpdate(List<String> list) throws DbException {
        for (Map.Entry<? extends Condition, ? extends DbValueContainer> entry : conditionToValue.entrySet()) {
            entry.getKey().appendSqlLogValues(list);
            list.add(entry.getValue().valueToSqlLogString());
        }
        if (defaultValue != null) {
            list.add(defaultValue.valueToSqlLogString());
        }
    }

}

