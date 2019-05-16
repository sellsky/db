package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbTime;
import tk.bolovsrol.db.orm.containers.DbTimeContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.utils.time.Duration;

import java.sql.Time;
import java.time.LocalTime;

/** Поле типа TIME */
public class TimeDbField extends AbstractDbDataField<Time, DbTimeContainer> implements DbTimeContainer {

    public TimeDbField(DbDataObject owner, String name) { super(owner, name, new DbTime()); }

    public TimeDbField(DbDataObject owner, String name, boolean register) { super(owner, name, register, new DbTime()); }

    @Override public DbTimeContainer wrap(Time value) { return new DbTime(value); }

	@Override public void setValue(Long millis) { container.setValue(millis); }

	@Override public Long getValueMillis() { return container.getValueMillis(); }

	@Override public void setValue(LocalTime value) {container.setValue(value);}

	@Override public LocalTime getValueLocalTime() {return container.getValueLocalTime();}

	@Override public void setValue(Duration value) {container.setValue(value);}

	@Override public Duration getValueDuration() {return container.getValueDuration();}
}