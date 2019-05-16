package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.containers.AbstractValueContainer;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.ValueParsingException;
import tk.bolovsrol.utils.time.TwofacedTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

public class DbTwofacedTime extends AbstractValueContainer<TwofacedTime> implements DbTwofacedTimeContainer {

    public DbTwofacedTime() {
    }

    public DbTwofacedTime(TwofacedTime value) {
        this.value = value;
    }

    @Override
    public void putValue(PreparedStatement ps, int pos) throws SQLException {
        if (value == null) {
            ps.setNull(pos, Types.VARCHAR);
        } else {
            ps.setString(pos, value.getHumanReadable());
        }
    }

    @Override
    public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        String tmp = rs.getString(columnIndex);
        if (rs.wasNull()) {
            this.value = null;
        } else {
            try {
                this.value = TwofacedTime.parseHumanReadable(tmp);
            } catch (IllegalArgumentException e) {
                throw new PickFailedException("Cannot parse Twofaced Time", e);
            }
        }
        this.committedValue = this.value;
    }

    @Override
    public String valueToString() {
        return value == null ? null : value.getHumanReadable();
    }

    @Override public String valueToLogString() {
        return ContainerToLogString.forTwofacedTime(committedValue, value);
    }

    @Override public String valueToSqlLogString() {
        return ContainerToSqlLogString.forTwofacedTime(value);
    }

    @Override public void parseValue(String value) throws ValueParsingException {
        if (value == null) {
            dropValue();
        } else {
            try {
                setValue(TwofacedTime.parseHumanReadable(value));
            } catch (IllegalArgumentException e) {
                throw new ValueParsingException(e);
            }
        }
    }

    public static DbTwofacedTimeContainer[] wrap(TwofacedTime... twofacedTimes) {
        if (twofacedTimes == null) {
            return null;
        }
        DbTwofacedTimeContainer[] result = new DbTwofacedTimeContainer[twofacedTimes.length];
        for (int i = 0; i < twofacedTimes.length; i++) {
            result[i] = new DbTwofacedTime(twofacedTimes[i]);
        }
        return result;
    }

    public static DbTwofacedTimeContainer[] wrap(Collection<TwofacedTime> twofacedTimes) {
        if (twofacedTimes == null || twofacedTimes.isEmpty()) {
            return null;
        }
        DbTwofacedTimeContainer[] result = new DbTwofacedTimeContainer[twofacedTimes.size()];
        int i = 0;
        for (TwofacedTime tt : twofacedTimes) {
            result[i] = new DbTwofacedTime(tt);
            i++;
        }
        return result;
    }

    @Override public void putValue(Json json) {
        json.set(valueToString());
    }

    @Override public void parseValue(Json json) throws ValueParsingException {
        parseValue(json == null ? null : json.getString());
    }

}