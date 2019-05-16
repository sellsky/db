package tk.bolovsrol.db.benchmark.fields;

import tk.bolovsrol.db.orm.containers.DbIntegerContainer;
import tk.bolovsrol.db.orm.fields.IntegerDbField;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.utils.RandomizeUtils;

import java.util.Random;

public class RandomableIntegerDbField extends IntegerDbField implements RandomableField<Integer, DbIntegerContainer> {
    public RandomableIntegerDbField(DbDataObject owner, String name) {
        super(owner, name);
    }

    @Override public void setNextRandomValue(Random random) {
        setValue(RandomizeUtils.getInt(Integer.MAX_VALUE));
    }
}
