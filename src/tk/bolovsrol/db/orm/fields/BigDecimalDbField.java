package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbBigDecimal;
import tk.bolovsrol.db.orm.containers.DbBigDecimalContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.dbcolumns.NumericDbColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.NumericUpdateColumn;
import tk.bolovsrol.utils.Spell;

import java.math.BigDecimal;

/** Поле с фиксированной точкой. */
public class BigDecimalDbField extends AbstractDbDataField<BigDecimal, DbBigDecimalContainer> implements NumericDbColumn<BigDecimal>, NumericUpdateColumn<BigDecimal>, DbBigDecimalContainer, DbNumberField<BigDecimal, DbBigDecimalContainer> {

    public BigDecimalDbField(DbDataObject owner, String name, int dbScale) {
        super(owner, name, new DbBigDecimal(dbScale));
    }

    public BigDecimalDbField(DbDataObject owner, String name, boolean register, int dbScale) {
        super(owner, name, register, new DbBigDecimal(dbScale));
    }

    @Override public DbBigDecimalContainer wrap(BigDecimal value) {
        return new DbBigDecimal(getDbScale(), value);
    }

    @Override
    public void setValue(long value, int scale) throws IllegalArgumentException {
        try {
            container.setValue(value, scale);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Cannot set field " + Spell.get(getName()) + " with value " + value + 'e' + (0 - scale), e);
        }
    }

    @Override
    public void setValue(BigDecimal value) throws IllegalArgumentException {
        try {
            container.setValue(value);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Cannot set field " + Spell.get(getName()) + " with value " + Spell.get(value), e);
        }
    }

    @Override public int getDbScale() {
        return container.getDbScale();
    }
}