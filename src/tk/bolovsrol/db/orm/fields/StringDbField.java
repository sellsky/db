package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbString;
import tk.bolovsrol.db.orm.containers.DbStringContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;

/** Текстовая строка типа CHAR, VARCHAR, а также CLOB */
public class StringDbField extends AbstractDbDataField<String, DbStringContainer> implements DbStringContainer, DbStringField {

    public StringDbField(DbDataObject owner, String name) { super(owner, name, new DbString()); }

    public StringDbField(DbDataObject owner, String name, boolean register) { super(owner, name, register, new DbString()); }

    @Override public DbStringContainer wrap(String value) { return new DbString(value); }

}
