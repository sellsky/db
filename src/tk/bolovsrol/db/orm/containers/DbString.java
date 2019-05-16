package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.containers.AbstractValueContainer;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

public class DbString extends AbstractValueContainer<String> implements DbStringContainer {

    public DbString() {
    }

    public DbString(String value) {
        this.value = value;
    }

    @Override public void putValue(PreparedStatement ps, int pos) throws SQLException {
        if (value == null) {
            ps.setNull(pos, Types.VARCHAR);
        } else {
            ps.setString(pos, value);
        }
    }

    @Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        this.value = rs.getString(columnIndex);
        this.committedValue = this.value;
    }

    @Override public String valueToString() {
        return value;
    }

    @Override public String valueToLogString() {
        return ContainerToLogString.forString(committedValue, value);
    }

    @Override public String valueToSqlLogString() {
        return ContainerToSqlLogString.forString(value);
    }

    @Override public void parseValue(String value) {
        setValue(value);
    }

    public static DbStringContainer[] wrap(String... strings) {
        if (strings == null) {
            return null;
        }
        DbStringContainer[] result = new DbStringContainer[strings.length];
        for (int i = 0; i < strings.length; i++) {
            result[i] = new DbString(strings[i]);
        }
        return result;
    }

    public static DbStringContainer[] wrap(Collection<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return null;
        }
        DbStringContainer[] result = new DbStringContainer[strings.size()];
        int i = 0;
        for (String l : strings) {
            result[i] = new DbString(l);
            i++;
        }
        return result;
    }

    @Override public void putValue(Json json) {
        json.set(value);
    }

    @Override public void parseValue(Json json) throws ValueParsingException {
        this.value = json == null ? null : json.getString();
    }
}
