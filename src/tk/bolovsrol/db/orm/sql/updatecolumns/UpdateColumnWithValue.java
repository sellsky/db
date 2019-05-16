package tk.bolovsrol.db.orm.sql.updatecolumns;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UpdateColumnWithValue<V> implements UpdateColumn<V> {

    private final DbColumn<V> column;
    private final DbValueContainer<V> container;

    public UpdateColumnWithValue(DbColumn<V> column, DbValueContainer<V> container) {
        this.column = column;
        this.container = container;
    }

    public <C extends DbValueContainer<V>> UpdateColumnWithValue(DbDataField<V, C> field, V value) {
        this(field, field.wrap(value));
    }

    @Override
    public void writeSqlExpressionForUpdate(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
        sb.append("=?");
    }

    @Override public int putValuesForUpdate(PreparedStatement ps, int pos) throws SQLException, DbException {
        container.putValue(ps, pos);
        return pos + 1;
    }

    @Override public void appendSqlLogValuesForUpdate(List<String> list) throws DbException {
        list.add(container.valueToSqlLogString());
    }

    @Override public void valueCommittedAtUpdate() {
        container.valueCommitted();
    }

}
