package tk.bolovsrol.db.benchmark.fields;

import tk.bolovsrol.db.orm.containers.DbBigDecimalContainer;
import tk.bolovsrol.db.orm.fields.BigDecimalDbField;
import tk.bolovsrol.db.orm.object.DbDataObject;

import java.math.BigDecimal;
import java.util.Random;

public class RandomableBigDecimalDbField extends BigDecimalDbField implements RandomableField<BigDecimal, DbBigDecimalContainer> {

    public RandomableBigDecimalDbField(DbDataObject owner, String name, int dbScale) {
        super(owner, name, dbScale);
    }

    @Override public void setNextRandomValue(Random random) {
        setValue(BigDecimal.valueOf((long) random.nextInt(), getDbScale()));
    }
}
