package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbDate;
import tk.bolovsrol.db.orm.containers.DbDateContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

/** Поле типа DATE, DATETIME, TIMESTAMP. */
public class DateDbField extends AbstractDbDataField<Date, DbDateContainer> implements DbDateContainer, DbDateField {

    public DateDbField(DbDataObject owner, String name) {
        super(owner, name, new DbDate());
    }

    public DateDbField(DbDataObject owner, String name, boolean register) {
        super(owner, name, register, new DbDate());
    }

    @Override public DbDateContainer wrap(Date value) { return new DbDate(value); }

    // --- делегирование

    @Override public void setValue(Date value) { container.setValue(value); }

    @Override public void setValue(Long millis) { container.setValue(millis); }

    @Override public void setValue(Instant value) { container.setValue(value); }

    @Override public Instant getValueInstant() { return container.getValueInstant(); }

    @Override public Long getValueMillis() { return container.getValueMillis(); }

    @Override public String valueToString(SimpleDateFormat format) {return container.valueToString(format);}

    @Override public void parseValue(String value, SimpleDateFormat format) throws ValueParsingException {container.parseValue(value, format);}

}
