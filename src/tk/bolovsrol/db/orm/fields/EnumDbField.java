package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbEnum;
import tk.bolovsrol.db.orm.containers.DbEnumContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.dbcolumns.LikeableDbColumn;

/** Текстовая строка типа ENUM */
public class EnumDbField<E extends Enum<E>> extends AbstractDbDataField<E, DbEnumContainer<E>> implements DbEnumContainer<E>, LikeableDbColumn<E> {

    public EnumDbField(DbDataObject owner, String name, Class<E> cl) {
        super(owner, name, new DbEnum<>(cl));
    }

    public EnumDbField(DbDataObject owner, String name, boolean register, Class<E> cl) {
        super(owner, name, register, new DbEnum<>(cl));
    }

    @Override public DbEnumContainer<E> wrap(E value) { return new DbEnum<>(container.getComponentType(), value); }

}