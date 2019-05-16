package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.SimpleDateFormats;
import tk.bolovsrol.utils.containers.AbstractValueContainer;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.TimeContainer;
import tk.bolovsrol.utils.containers.ValueParsingException;
import tk.bolovsrol.utils.time.Duration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalTime;

public class DbTime extends AbstractValueContainer<Time> implements DbTimeContainer {

    public DbTime() {
    }

    public DbTime(Time value) {
        this.value = value;
    }

	@Override public void setValue(Long millis) {
		this.setValue(millis == null ? null : new Time(millis));
	}

	@Override public Long getValueMillis() {
		return value.getTime();
	}

	@Override public void setValue(LocalTime value) {
		this.value = value == null ? null : Time.valueOf(value);
	}

	@Override public LocalTime getValueLocalTime() {
		return value == null ? null : this.value.toLocalTime();
	}

	@Override public void setValue(Duration value) {
		this.value = value == null ? null : new Time(value.getMillis());
	}

	@Override public Duration getValueDuration() {
		return value == null ? null : new Duration(this.value.getTime());
	}

	@Override public void putValue(PreparedStatement ps, int pos) throws SQLException {
		if (value == null) {
            ps.setNull(pos, Types.TIME);
        } else {
            ps.setTime(pos, value);
        }
    }

    @Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        Time tmp = rs.getTime(columnIndex);
        this.value = rs.wasNull() ? null : tmp;
        this.committedValue = this.value;
    }

    @Override public String valueToString() {
        return value == null ? null : SimpleDateFormats.TIME_MS.get().format(value);
    }

    @Override public String valueToLogString() {
        return ContainerToLogString.forTime(committedValue, value);
    }

    @Override public String valueToSqlLogString() {
        return ContainerToSqlLogString.forTime(value);
    }

    @Override public void parseValue(String value) throws ValueParsingException {
        if (value == null) {
            dropValue();
        } else {
            try {
                setValue(Time.valueOf(value));
            } catch (Exception e) {
                throw new ValueParsingException(e);
            }
        }
    }

    public static TimeContainer[] wrap(Time... dates) {
        if (dates == null) {
            return null;
        }
        TimeContainer[] result = new TimeContainer[dates.length];
        for (int i = 0; i < dates.length; i++) {
            result[i] = new DbTime(dates[i]);
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
