package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbInstant;
import tk.bolovsrol.db.orm.containers.DbInstantContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;

import java.time.Instant;

/** Поле типа DATEInstant/InstantSTAMP. */
public class InstantDbField extends AbstractDbDataField<Instant, DbInstantContainer> implements DbInstantContainer {

	public InstantDbField(DbDataObject owner, String name) { super(owner, name, new DbInstant()); }

	public InstantDbField(DbDataObject owner, String name, boolean register) { super(owner, name, register, new DbInstant()); }

	@Override public DbInstantContainer wrap(Instant value) { return new DbInstant(value); }

	@Override public void setValue(Long millis) { container.setValue(millis); }

	@Override public Long getValueMillis() { return container.getValueMillis(); }
}