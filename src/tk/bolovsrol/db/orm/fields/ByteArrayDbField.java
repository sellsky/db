package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbByteArray;
import tk.bolovsrol.db.orm.containers.DbByteArrayContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;

/** Двоичные данные типа BLOB. */
public class ByteArrayDbField extends AbstractDbDataField<byte[], DbByteArrayContainer> implements DbByteArrayContainer {

    public ByteArrayDbField(DbDataObject owner, String name) {
        super(owner, name, new DbByteArray());
    }

    public ByteArrayDbField(DbDataObject owner, String name, boolean register) {
        super(owner, name, register, new DbByteArray());
    }

    @Override public DbByteArrayContainer wrap(byte[] value) {
        return new DbByteArray(value);
    }

}
