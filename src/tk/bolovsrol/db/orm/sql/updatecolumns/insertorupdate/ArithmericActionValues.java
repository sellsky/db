package tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.sql.SQLException;
import java.util.Map;

/**
 * Колонка для update-части оператора INSERT ... ON DUPLICATE KEY UPDATE {@link tk.bolovsrol.db.orm.sql.statements.insert.Insert},
 * она указывает арифметически прибавить к полю существующей записи значение поля соответствующей insert-колонки.
 * <p>
 * Колонка, вероятно, будет работать только в MySQL.
 */
class ArithmericActionValues<V extends Number> implements NumericInsertOrUpdateColumn<V> {

    private final DbColumn<V> column;
    private String actionString;

    public ArithmericActionValues(DbColumn<V> column, String actionString) {
        this.column = column;
        this.actionString = actionString;
    }

    @Override
    public void writeSqlExpressionForUpdate(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append('=');
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append(actionString);
        sb.append("VALUES(");
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append(')');
    }
}
