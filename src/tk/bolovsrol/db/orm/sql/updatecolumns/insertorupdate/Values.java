package tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Колонка для update-части оператора INSERT ... ON DUPLICATE KEY UPDATE {@link tk.bolovsrol.db.orm.sql.statements.insert.Insert},
 * она указывает записать в колонку существующей записи значение колонки соответствующей insert-строки.
 * <p/>
 * Колонка, вероятно, будет работать только в MySQL.
 */
public class Values<V> implements InsertOrUpdateColumn<V> {

    private final DbColumn<V> column;

    public Values(DbColumn<V> column) {
        this.column = column;
    }

    @Override
    public void writeSqlExpressionForUpdate(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append("=VALUES(");
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append(')');
    }

    public static Collection<Values<?>> wrap(Collection<? extends DbColumn<?>> columns) {
        ArrayList<Values<?>> result = new ArrayList<>(columns.size());
        for (DbColumn<?> column : columns) {
            result.add(new Values<>(column));
        }
        return result;
    }

    public static Collection<Values<?>> wrap(DbColumn<?>... columns) {
        ArrayList<Values<?>> result = new ArrayList<>(columns.length);
        for (DbColumn<?> column : columns) {
            result.add(new Values<>(column));
        }
        return result;
    }

}
