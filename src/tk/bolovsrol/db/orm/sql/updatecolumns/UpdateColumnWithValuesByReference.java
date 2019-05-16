package tk.bolovsrol.db.orm.sql.updatecolumns;

import tk.bolovsrol.db.DatabaseProductNames;
import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Обновляет колонку значением, определяемым сопоставлением значения содержимого указанной колонки (например, id)
 * в качестве ключа с передаваемой картой ключей и соответствующих возможных значений.
 * <p/>
 * В MySQL это, например, так:
 * UPDATE foo SET col=CASE id WHEN 1 THEN 'FOO' WHEN 2 THEN 'BAR' ELSE 'ZOOM' END WHERE id IN (1,2);
 * <p/>
 * В Орацле это, вероятно, так:
 * UPDATE foo SET col=DECODE(id, 1, 'FOO', 2, 'BAR', 'ZOOM') WHERE id IN (1,2);
 */
public class UpdateColumnWithValuesByReference<V, R> implements UpdateColumn<V> {

    private final DbColumn<V> updatingColumn;
    private final DbColumn<R> referenceColumn;
    private final Map<? extends DbValueContainer<R>, ? extends DbValueContainer<V>> referenceToValue;
    private final DbValueContainer<V> defaultValue;

    public UpdateColumnWithValuesByReference(DbColumn<V> updatingColumn, DbColumn<R> referenceColumn, Map<? extends DbValueContainer<R>, ? extends DbValueContainer<V>> referenceToValue) {
        this(updatingColumn, referenceColumn, referenceToValue, null);
    }

    public UpdateColumnWithValuesByReference(DbColumn<V> updatingColumn, DbColumn<R> referenceColumn, Map<? extends DbValueContainer<R>, ? extends DbValueContainer<V>> referenceToValue, DbValueContainer<V> defaultValue) {
        this.updatingColumn = updatingColumn;
        this.referenceColumn = referenceColumn;
        this.referenceToValue = referenceToValue;
        this.defaultValue = defaultValue;
    }

    @Override
    public void writeSqlExpressionForUpdate(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        if (DatabaseProductNames.MYSQL.equals(databaseProductName)) {
            updatingColumn.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
            sb.append("=CASE ");
            referenceColumn.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
            for (int i = referenceToValue.size(); i > 0; i--) {
                sb.append(" WHEN ? THEN ?");
            }
            if (defaultValue != null) {
                sb.append(" ELSE ?");
            }
            sb.append("END");
        } else if (DatabaseProductNames.ORACLE.equals(databaseProductName)) {
            updatingColumn.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
            sb.append("=DECODE(");
            referenceColumn.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
            for (int i = referenceToValue.size(); i > 0; i--) {
                sb.append(",?,?");
            }
            if (defaultValue != null) {
                sb.append(",?");
            }
            sb.append(')');
        } else {
            throw new UnsupportedOperationException("Don't know how to encode UpdateColumnWithCaseValue on " + databaseProductName);
        }
    }

    @Override public int putValuesForUpdate(PreparedStatement ps, int pos) throws SQLException, DbException {
        for (Map.Entry<? extends DbValueContainer, ? extends DbValueContainer> entry : referenceToValue.entrySet()) {
            entry.getKey().putValue(ps, pos++);
            entry.getValue().putValue(ps, pos++);
        }
        if (defaultValue != null) {
            defaultValue.putValue(ps, pos++);
        }
        return pos;
    }

    @Override public void appendSqlLogValuesForUpdate(List<String> list) throws DbException {
        for (Map.Entry<? extends DbValueContainer, ? extends DbValueContainer> entry : referenceToValue.entrySet()) {
            list.add(entry.getKey().valueToSqlLogString());
            list.add(entry.getValue().valueToSqlLogString());
        }
        if (defaultValue != null) {
            list.add(defaultValue.valueToSqlLogString());
        }
    }

    public static <V, R> Map<DbValueContainer<R>, DbValueContainer<V>> wrap(Map<R, V> src, Function<R, DbValueContainer<R>> refGen, Function<V, DbValueContainer<V>> valGen) {
        Map<DbValueContainer<R>, DbValueContainer<V>> result = new LinkedHashMap<>();
        for (Map.Entry<R, V> entry : src.entrySet()) {
            result.put(refGen.apply(entry.getKey()), valGen.apply(entry.getValue()));
        }
        return result;
    }

}

