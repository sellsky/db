package tk.bolovsrol.db.orm.sql.updatecolumns;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Колонка-вилка.
 * <p/>
 * Обновляет одну колонку значением другой.
 */
public class UpdateColumnWithColumn<V> implements UpdateColumn<V> {

    private final DbColumn<V> targetColumn;
    private final DbColumn<V> sourceColumn;

    public UpdateColumnWithColumn(DbColumn<V> targetColumn, DbColumn<V> sourceColumn) {
        this.targetColumn = targetColumn;
        this.sourceColumn = sourceColumn;
    }

    @Override
    public void writeSqlExpressionForUpdate(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        targetColumn.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append('=');
        sourceColumn.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
    }

	@Override public int putValuesForUpdate(PreparedStatement ps, int pos)
			throws SQLException, DbException { return sourceColumn.putValuesForSelect(ps,pos); }

	@Override public void appendSqlLogValuesForUpdate(List<String> list) throws DbException {
		sourceColumn.appendSqlLogValuesForSelect(list); }
}


