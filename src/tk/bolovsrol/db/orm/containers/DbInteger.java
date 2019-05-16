package tk.bolovsrol.db.orm.containers;


import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.CollectionUtils;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.containers.AbstractValueContainer;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DbInteger extends AbstractValueContainer<Integer> implements DbIntegerContainer {

    public DbInteger() {
    }

    public DbInteger(Integer value) {
        this.value = value;
    }

    // ---- interface implementations
    @Override public void putValue(PreparedStatement ps, int pos) throws SQLException {
        if (value == null) {
            ps.setNull(pos, Types.INTEGER);
        } else {
            ps.setInt(pos, value.intValue());
        }
    }

    @Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        int tmp = rs.getInt(columnIndex);
        this.value = rs.wasNull() ? null : tmp;
        this.committedValue = this.value;
    }

    @Override public String valueToString() {
        return value == null ? null : value.toString();
    }

    @Override public String valueToLogString() {
        return ContainerToLogString.forNumber(committedValue, value);
    }

    @Override public String valueToSqlLogString() {
        return ContainerToSqlLogString.forNumber(value);
    }

    @Override public void parseValue(String value) throws ValueParsingException {
        if (value == null) {
            dropValue();
        } else {
            try {
                setValue(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new ValueParsingException(e);
            }
        }
    }

    @Override public void putValue(Json json) {
        json.set(value);
    }

    @Override public void parseValue(Json json) throws ValueParsingException {
        value = json == null ? null : json.getInteger();
    }

    public static List<DbInteger> wrap(Collection<Integer> integers) {
        return CollectionUtils.map(integers, DbInteger::new, new ArrayList<>(integers.size()));
    }

}
