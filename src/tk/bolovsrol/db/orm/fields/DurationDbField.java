package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbDuration;
import tk.bolovsrol.db.orm.containers.DbDurationContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.utils.time.Duration;

/** Хранит продолжительность. */
public class DurationDbField extends AbstractDbDataField<Duration, DbDurationContainer> implements DbDurationContainer {
    public DurationDbField(DbDataObject owner, String name) {
        super(owner, name, new DbDuration());
    }

    public DurationDbField(DbDataObject owner, String name, boolean register) {
        super(owner, name, register, new DbDuration());
    }

    @Override public DbDurationContainer wrap(Duration value) { return new DbDuration(value); }

	@Override public void setValue(Long millis) { container.setValue(millis); }

	@Override public Long getValueMillis() { return container.getValueMillis(); }

}
