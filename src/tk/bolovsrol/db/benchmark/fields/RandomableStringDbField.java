package tk.bolovsrol.db.benchmark.fields;

import tk.bolovsrol.db.orm.containers.DbStringContainer;
import tk.bolovsrol.db.orm.fields.StringDbField;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.utils.RandomizeUtils;

import java.util.Random;

public class RandomableStringDbField extends StringDbField implements RandomableField<String, DbStringContainer> {
    private final int minLen;
    private final int maxLen;

    public RandomableStringDbField(DbDataObject owner, String name, int minLen, int maxLen) {
        super(owner, name);
        this.minLen = minLen;
        this.maxLen = maxLen;
    }

    @Override public void setNextRandomValue(Random random) {
        char[] result = new char[minLen + random.nextInt(maxLen - minLen)];
        for (int i = 0; i < result.length; i++) {
            result[i] = RandomizeUtils.ALPHADIGITS[random.nextInt(RandomizeUtils.ALPHADIGITS.length)];
        }
        setValue(new String(result));
    }
}
