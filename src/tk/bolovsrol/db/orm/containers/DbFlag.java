package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.EnumUtils;
import tk.bolovsrol.utils.Flag;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.containers.AbstractValueContainer;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Шорткат для DbEnumContainer&lt;Flag&gt;.
 */
public class DbFlag extends AbstractValueContainer<Flag> implements DbFlagContainer {

    public DbFlag() {
    }

    public DbFlag(Flag value) {
        this.value = value;
    }

    public DbFlag(boolean value) {
        this.value = Flag.pickBoolean(value);
    }

    @Override public void setValue(boolean value) {
        this.value = Flag.pickBoolean(value);
    }

    @Override public boolean booleanValue() {
        return this.value.booleanValue();
    }

    @Override public void putValue(PreparedStatement ps, int pos) throws SQLException {
        if (value == null) {
            ps.setNull(pos, Types.VARCHAR);
        } else {
            ps.setString(pos, value.name());
        }
    }

    @Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        String tmp = rs.getString(columnIndex);
        this.value = rs.wasNull() ? null : Flag.valueOf(tmp);
        this.committedValue = this.value;
    }

    @Override public boolean isValueChanged() {
        return committedValue != value;
    }

    @Override public String valueToString() {
        return value == null ? null : value.toString();
    }

    @Override public String valueToLogString() {
        return ContainerToLogString.forEnum(committedValue, value);
    }

    @Override public String valueToSqlLogString() {
        return ContainerToSqlLogString.forEnum(value);
    }

    @Override public void parseValue(String value) throws ValueParsingException {
        if (value == null) {
            dropValue();
        } else {
            try {
                setValue(EnumUtils.pickByNameOrToString(Flag.class, value));
            } catch (UnexpectedBehaviourException e) {
                throw new ValueParsingException(e);
            }
        }
    }

    @Override public void putValue(Json json) {
        json.set(value);
    }

    @Override public void parseValue(Json json) throws ValueParsingException {
        value = json == null ? null : json.getFlag();
    }
}