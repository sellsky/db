package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.containers.DbInteger;
import tk.bolovsrol.db.orm.sql.DbException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public abstract class AbstractCount extends DbInteger implements NumericDbColumn<Integer> {

    protected AbstractCount() {
        super(0);
    }

    @Override
    public int putValuesForSelect(PreparedStatement ps, int pos) throws SQLException, DbException {
        return pos;
    }

    @Override
    public void appendSqlLogValuesForSelect(List<String> list) throws DbException {
    }

    @Override
    public int pickValuesForSelect(ResultSet rs, int pos) throws SQLException, DbException {
        value = rs.getInt(pos);
        return pos + 1;
    }

}