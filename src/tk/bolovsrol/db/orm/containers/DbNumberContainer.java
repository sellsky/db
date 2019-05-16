package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.utils.containers.NumberContainer;

/** Маркер: контейнер содержит циферку. */
public interface DbNumberContainer<V extends Number> extends NumberContainer<V>, DbValueContainer<V> {
}
