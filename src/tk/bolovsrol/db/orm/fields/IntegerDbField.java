package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbInteger;
import tk.bolovsrol.db.orm.containers.DbIntegerContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.dbcolumns.NumericDbColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.NumericUpdateColumn;

/** Целочисленное поле */
public class IntegerDbField extends AbstractDbDataField<Integer, DbIntegerContainer> implements NumericDbColumn<Integer>, NumericUpdateColumn<Integer>, DbIntegerContainer, DbNumberField<Integer, DbIntegerContainer> {

    public IntegerDbField(DbDataObject owner, String name) { super(owner, name, new DbInteger()); }

    public IntegerDbField(DbDataObject owner, String name, boolean register) { super(owner, name, register, new DbInteger()); }

    @Override public DbIntegerContainer wrap(Integer value) { return new DbInteger(value); }

}
