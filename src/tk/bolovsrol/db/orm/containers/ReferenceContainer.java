package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.utils.containers.LongContainer;

/** Хранит некий RefDbDataObject объект. Или не хранит, тогда он выступает как обычный LongContainer. */
public interface ReferenceContainer<T extends RefDbDataObject> extends LongContainer {

    T getReference();

    void setReference(T reference);

    T dropReference();

    boolean hasReference();

}
