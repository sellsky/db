package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbFlag;
import tk.bolovsrol.db.orm.containers.DbFlagContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.dbcolumns.LikeableDbColumn;
import tk.bolovsrol.utils.Flag;

/** Текстовая строка YES или NO. */
public class FlagDbField extends AbstractDbDataField<Flag, DbFlagContainer> implements DbFlagContainer, LikeableDbColumn<Flag> {

    public FlagDbField(DbDataObject owner, String name) { super(owner, name, new DbFlag());}

    public FlagDbField(DbDataObject owner, String name, boolean register) { super(owner, name, register, new DbFlag()); }

    @Override public DbFlagContainer wrap(Flag value) { return new DbFlag(value); }

    @Override public void setValue(boolean value) { container.setValue(value); }

    @Override public boolean booleanValue() { return container.booleanValue(); }
}