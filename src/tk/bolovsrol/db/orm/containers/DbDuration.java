package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.containers.AbstractValueContainer;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.ValueParsingException;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TimeUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DbDuration extends AbstractValueContainer<Duration> implements DbDurationContainer {

    public DbDuration() {
    }

    public DbDuration(Duration value) {
        this.value = value;
    }

	public DbDuration(Long millis) {
		this(millis == null ? null : new Duration(millis));
	}

    @Override
	public void setValue(Long millis) {
		this.setValue(millis == null ? null : new Duration(millis));
	}

    @Override
	public Long getValueMillis() {
		return value == null ? null : value.getMillis();
	}

    // ---- interface implementations

    @Override
    public void putValue(PreparedStatement ps, int pos) throws SQLException {
        if (value == null) {
            ps.setNull(pos, Types.NULL);
        } else {
            ps.setString(pos, value.getString());
        }
    }

    @Override
    public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        try {
            String str = rs.getString(columnIndex);
            this.value = rs.wasNull() ? null : new Duration(str);
        } catch (Exception e) {
            throw new PickFailedException(e);
        }
        this.committedValue = this.value;
    }

    @Override public String valueToString() { return value == null ? null : value.getString(); }

    @Override public String valueToLogString() { return ContainerToLogString.forDuration(committedValue, value); }

    @Override public String valueToSqlLogString() { return ContainerToSqlLogString.forDuration(value); }

    @Override public void parseValue(String value) throws ValueParsingException {
        if (value == null) {
            dropValue();
        } else {
            try {
                setValue(new Duration(value));
            } catch (TimeUtils.DurationParsingException e) {
                throw new ValueParsingException(e);
            }
        }
    }

// --- service

    /**
     * Оборачивает массив дат в массив контейнеров.
     *
     * @param durations
     * @return массив контейнеров
     */
    public static DbDurationContainer[] wrap(Duration... durations) {
        if (durations == null) {
            return null;
        }
        DbDurationContainer[] result = new DbDurationContainer[durations.length];
        for (int i = 0; i < durations.length; i++) {
            result[i] = new DbDuration(durations[i]);
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