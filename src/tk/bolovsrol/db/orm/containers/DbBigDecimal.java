package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.containers.AbstractValueContainer;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/** Контейнер для чисел с фиксированной точкой. */
public class DbBigDecimal extends AbstractValueContainer<BigDecimal> implements DbBigDecimalContainer {

    protected final int dbScale;

    public DbBigDecimal(int dbScale) {
        this.dbScale = dbScale;
    }

    public DbBigDecimal(int dbScale, BigDecimal value) throws ArithmeticException {
        this(dbScale);
        this.value = value == null ? null : rescale(value); // throws ArithmeticException
    }

    public DbBigDecimal(int dbScale, long value, int scale) throws ArithmeticException {
        this(dbScale);
        this.value = rescale(BigDecimal.valueOf(value, scale)); // throws ArithmeticException
    }

    private BigDecimal rescale(BigDecimal source) {
        try {
            return source.scale() > dbScale ? source.setScale(dbScale, RoundingMode.UNNECESSARY) : source;
        } catch (ArithmeticException e) {
            throw new ArithmeticException(e.getMessage() + " source=" + Spell.get(source) + " target scale=" + dbScale);
        }
    }

    @Override
    public void setValue(long value, int scale) throws ArithmeticException {
        this.value = rescale(BigDecimal.valueOf(value, scale)); // throws ArithmeticException
    }

    @Override
    public void setValue(BigDecimal value) throws ArithmeticException {
        this.value = value == null ? null : rescale(value); // throws ArithmeticException
    }

    @Override
    public int signum() {
        return value.signum();
    }

    // ---- interface implementations

    @Override
    public void putValue(PreparedStatement ps, int pos) throws SQLException {
        if (value == null) {
            ps.setNull(pos, Types.DECIMAL);
        } else {
            ps.setBigDecimal(pos, value);
        }
    }

    @Override
    public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        this.committedValue = rs.getBigDecimal(columnIndex);
        this.value = this.committedValue;
    }

    @Override
    public boolean isValueChanged() {
        //noinspection NumberEquality
        return committedValue != value && (value == null || !(committedValue != null && (value.compareTo(committedValue) == 0)));
    }

    @Override public String valueToString() { return value == null ? null : value.toPlainString(); }

    @Override public String valueToLogString() { return ContainerToLogString.forBigDecimal(committedValue, value); }

    @Override public String valueToSqlLogString() { return ContainerToSqlLogString.forBigDecimal(value); }

    @Override public void parseValue(String value) throws ValueParsingException {
        if (value == null) {
            dropValue();
        } else {
            try {
                setValue(new BigDecimal(value));
            } catch (ArithmeticException e) {
                throw new ValueParsingException(e);
            }
        }
    }

    @Override public int getDbScale() {
        return dbScale;
    }

    @Override
    public String toString() {
        return (value == null) ? "null" : value.toPlainString();
    }

    @Override public void putValue(Json json) {
        json.set(value);
    }

    @Override public void parseValue(Json json) {
        setValue(json == null ? null : json.getBigDecimal());
    }
}