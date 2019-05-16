package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.SimpleDateFormats;
import tk.bolovsrol.utils.containers.AbstractValueContainer;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class DbDate extends AbstractValueContainer<Date> implements DbDateContainer {

    public DbDate() {
    }

    public DbDate(Date value) {
        this();
        this.value = value;
    }

    public DbDate(long millis) {
        this(new Date(millis));
    }

    @Override public void setValue(Long millis) {
        this.setValue(millis == null ? null : new Date(millis));
    }

    @Override public void setValue(Instant value) {
        this.setValue(value == null ? null : value.toEpochMilli());
    }

    @Override public Instant getValueInstant() {
        return value == null ? null : Instant.ofEpochMilli(value.getTime());
    }

    @Override public Long getValueMillis() {
        return value == null ? null : value.getTime();
    }

// ---- interface implementations

    @Override public void putValue(PreparedStatement ps, int pos) throws SQLException {
        if (value == null) {
            ps.setNull(pos, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(pos, new Timestamp(value.getTime()));
        }
    }

    @Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        try {
            this.value = new Date(rs.getTimestamp(columnIndex).getTime());
        } catch (NullPointerException e) {
            this.value = null;
        }
        this.committedValue = this.value;
    }

    @Override public String valueToString(SimpleDateFormat format) {
        return value == null ? null : format.format(value);
    }

    @Override public String valueToString() {
        return valueToString(SimpleDateFormats.DATE_SPACE_TIME_MS.get());
    }

    @Override public String valueToLogString() {
        return ContainerToLogString.forDate(committedValue, value);
    }

    @Override public String valueToSqlLogString() {
        return ContainerToSqlLogString.forDate(value);
    }

    @Override public void parseValue(String value, SimpleDateFormat format) throws ValueParsingException {
        if (value == null) {
            dropValue();
        } else {
            try {
                setValue(format.parse(value));
            } catch (ParseException e) {
                throw new ValueParsingException(e);
            }
        }
    }

    @Override public void parseValue(String value) throws ValueParsingException {
        parseValue(value, SimpleDateFormats.DATE_SPACE_TIME_MS.get());
    }
// --- service

    /**
     * Оборачивает массив дат в массив контейнеров.
     *
     * @param dates
     * @return массив контейнеров
     */
    public static DbDateContainer[] wrap(Date... dates) {
        if (dates == null) {
            return null;
        }
        DbDateContainer[] result = new DbDateContainer[dates.length];
        for (int i = 0; i < dates.length; i++) {
            result[i] = new DbDate(dates[i]);
        }
        return result;
    }

    @Override public void putValue(Json json) {
        json.set(value);
    }

    @Override public void parseValue(Json json) throws ValueParsingException {
        value = json == null ? null : json.getDate();
    }
}
