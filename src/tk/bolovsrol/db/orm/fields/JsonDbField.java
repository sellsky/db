package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbJson;
import tk.bolovsrol.db.orm.containers.DbJsonContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.utils.Json;

/** Текстовая строка типа CHAR, VARCHAR, а также CLOB */
public class JsonDbField extends AbstractDbDataField<Json, DbJsonContainer> implements DbJsonContainer, DbJsonField {

    public JsonDbField(DbDataObject owner, String name) { super(owner, name, new DbJson()); }

    public JsonDbField(DbDataObject owner, String name, boolean register) { super(owner, name, register, new DbJson()); }

    @Override public DbJsonContainer wrap(Json value) { return new DbJson(value); }

}
