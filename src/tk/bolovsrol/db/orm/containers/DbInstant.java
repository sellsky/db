package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.DateTimeFormatters;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.containers.AbstractValueContainer;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.InstantContainer;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.temporal.ChronoField;

public class DbInstant extends AbstractValueContainer<Instant> implements DbInstantContainer {

	public DbInstant() {
	}

	public DbInstant(Instant value) {
		this.value = value;
	}

	@Override public void setValue(Long millis) {
		this.setValue(millis == null ? null : Instant.ofEpochMilli(millis));
	}

	@Override public Long getValueMillis() {
		return value == null ? null : value.getEpochSecond() + value.getLong(ChronoField.MILLI_OF_SECOND);
	}

	@Override public void putValue(PreparedStatement ps, int pos) throws SQLException {
		if (value == null) {
			ps.setNull(pos, Types.TIMESTAMP);
		} else {
			ps.setTimestamp(pos, Timestamp.from(value));
		}
	}

	@Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
		Timestamp tmp = rs.getTimestamp(columnIndex);
		this.value = tmp == null ? null : tmp.toInstant();
		this.committedValue = this.value;
	}

	@Override public String valueToString() {
		return value == null ? null : DateTimeFormatters.DATE_SPACE_TIME_MS.format(value);
	}

	@Override public String valueToLogString() {
		return ContainerToLogString.forInstant(committedValue, value);
	}

	@Override public String valueToSqlLogString() {
		return ContainerToSqlLogString.forInstant(value);
	}

	@Override public void parseValue(String value) throws ValueParsingException {
		if (value == null) {
			dropValue();
		} else {
			try {
				setValue(DateTimeFormatters.DATE_SPACE_TIME_MS.parse(value, Instant::from));
			} catch (Exception e) {
				throw new ValueParsingException(e);
			}
		}
	}

	public static InstantContainer[] wrap(Instant... dates) {
		if (dates == null) {
			return null;
		}
		InstantContainer[] result = new InstantContainer[dates.length];
		for (int i = 0; i < dates.length; i++) {
			result[i] = new DbInstant(dates[i]);
		}
		return result;
	}

    @Override public void putValue(Json json) {
        json.set(value);
    }

    @Override public void parseValue(Json json) throws ValueParsingException {
        value = json == null ? null : json.getInstant();
    }
}
