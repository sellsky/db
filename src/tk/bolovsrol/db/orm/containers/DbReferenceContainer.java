package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.object.RefDbDataObject;

public interface DbReferenceContainer<T extends RefDbDataObject> extends DbLongContainer, ReferenceContainer<T> {
}
