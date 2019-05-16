package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.utils.containers.EnumContainer;

public interface DbEnumContainer<E extends Enum<E>> extends EnumContainer<E>, DbValueContainer<E> {
}
