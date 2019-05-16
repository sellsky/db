package tk.bolovsrol.db.benchmark.fields;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.fields.DbDataField;

import java.util.Random;

public interface RandomableField<V, C extends DbValueContainer<V>> extends DbDataField<V, C> {

    void setNextRandomValue(Random random);
}
