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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DbLong extends AbstractValueContainer<Long> implements DbLongContainer {

    public DbLong() {
    }

    public DbLong(Long value) {
        this.value = value;
    }

    @Override public void putValue(PreparedStatement ps, int pos) throws SQLException {
        if (value == null) {
            ps.setNull(pos, Types.INTEGER);
        } else {
            ps.setLong(pos, value.longValue());
        }
    }

    @Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        long value = rs.getLong(columnIndex);
        this.value = rs.wasNull() ? null : value;
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
                setValue(Long.parseLong(value));
            } catch (NumberFormatException e) {
                throw new ValueParsingException(e);
            }
        }
    }

//    //-- несколько арифметических действий над полем
//    public Long inc() {
//        return (value = MathUtils.inc(value));
//    }
//
//    public Long dec() {
//        return (value = MathUtils.dec(value));
//    }
//
//    public Long add(Number item) {
//        return (value = MathUtils.add(value, item));
//    }
//
//    public Long sub(Number subtrahend) {
//        return (value = MathUtils.sub(value, subtrahend));
//    }
//
//    public Long mul(Number multiplier) {
//        return (value = MathUtils.mul(value, multiplier));
//    }
//
//    public Long div(Number divisor) {
//        return (value = MathUtils.div(value, divisor));
//    }
//
//    public Long mod(Number divisor) {
//        return (value = MathUtils.mod(value, divisor));
//    }
//
//    public Long add(long item) {
//        return (value = MathUtils.add(value, item));
//    }
//
//    public Long sub(long subtrahend) {
//        return (value = MathUtils.sub(value, subtrahend));
//    }
//
//    public Long mul(long multiplier) {
//        return (value = MathUtils.mul(value, multiplier));
//    }
//
//    public Long div(long divisor) {
//        return (value = MathUtils.div(value, divisor));
//    }
//
//    public Long mod(long divisor) {
//        return (value = MathUtils.mod(value, divisor));
//    }

    public static DbLongContainer[] wrap(Long... longs) {
        if (longs == null) {
            return null;
        }
        DbLongContainer[] result = new DbLongContainer[longs.length];
        for (int i = 0; i < longs.length; i++) {
            result[i] = new DbLong(longs[i]);
        }
        return result;
    }

    public static DbLongContainer[] wrap(long... longs) {
        if (longs == null) {
            return null;
        }
        DbLongContainer[] result = new DbLongContainer[longs.length];
        for (int i = 0; i < longs.length; i++) {
            result[i] = new DbLong(longs[i]);
        }
        return result;
    }

    public static List<DbLong> wrap(Collection<Long> longs) {
        if (longs == null) { return null; }
        if (longs.isEmpty()) { return Collections.emptyList(); }
        List<DbLong> result = new ArrayList<>(longs.size());
        for (Long l : longs) {
            result.add(new DbLong(l));
        }
        return result;
    }

    @Override public void putValue(Json json) {
        json.set(value);
    }

    @Override public void parseValue(Json json) throws ValueParsingException {
        value = json == null ? null : json.getLong();
    }
}
