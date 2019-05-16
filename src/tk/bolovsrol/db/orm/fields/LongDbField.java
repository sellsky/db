package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbLong;
import tk.bolovsrol.db.orm.containers.DbLongContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.dbcolumns.NumericDbColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.NumericUpdateColumn;

/** Целочисленное поле длинного типпа */
public class LongDbField extends AbstractDbDataField<Long, DbLongContainer> implements NumericDbColumn<Long>, NumericUpdateColumn<Long>, DbLongContainer, DbNumberField<Long, DbLongContainer> {

    public LongDbField(DbDataObject owner, String name) { super(owner, name, new DbLong()); }

    public LongDbField(DbDataObject owner, String name, boolean register) { super(owner, name, register, new DbLong()); }

    @Override public DbLongContainer wrap(Long value) {
        return new DbLong(value);
    }

    @Override public void setValue(Long value) {
        container.setValue(value);
    }

    @Override public Long getValue() {
        return container.getValue();
    }

}
