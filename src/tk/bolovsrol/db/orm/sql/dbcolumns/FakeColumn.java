package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Колонка-константа для случаев, когда ничего загружать не нужно, а надо узнать,
 * вернётся ли хоть одна запись по заданному условию.
 */
public final class FakeColumn implements DbColumn<Void> {

    public static final FakeColumn INSTANCE = new FakeColumn();

    private FakeColumn() {
    }

    @Override public int putValuesForSelect(PreparedStatement ps, int pos) throws SQLException, DbException {
        return pos;
    }

    @Override public void appendSqlLogValuesForSelect(List<String> list) throws DbException {
    }

    @Override public int pickValuesForSelect(ResultSet rs, int pos) throws SQLException, DbException {
        return pos;
    }

    @Override
    public void writeSqlExpressionForSelect(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException {
        sb.append('0');
    }
}
