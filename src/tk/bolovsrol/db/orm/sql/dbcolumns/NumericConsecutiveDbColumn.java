package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.containers.DbBigDecimal;
import tk.bolovsrol.db.orm.containers.DbByteArray;
import tk.bolovsrol.db.orm.containers.DbDate;
import tk.bolovsrol.db.orm.containers.DbDuration;
import tk.bolovsrol.db.orm.containers.DbEnum;
import tk.bolovsrol.db.orm.containers.DbFlag;
import tk.bolovsrol.db.orm.containers.DbInteger;
import tk.bolovsrol.db.orm.containers.DbLong;
import tk.bolovsrol.db.orm.containers.DbNumberContainer;
import tk.bolovsrol.db.orm.containers.DbString;
import tk.bolovsrol.db.orm.containers.DbTime;
import tk.bolovsrol.db.orm.containers.DbTwofacedTime;
import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConsecutiveItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConstantItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.DbColumnItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ValueItem;
import tk.bolovsrol.utils.Flag;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TwofacedTime;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

/**
 * Сборная колонка для БД, хранящая цифровое значение и умеющая его отдавать; можно использовать для создания необычных колонок
 * непосредственно в месте употребления, достаточно подобавлять нужные элементы, всё остальное сборная колонка сделает сама.
 * <p>
 * Следует вызывать (в цепочке) такие методы:
 * <ul>
 * <li>{@link #item(ConsecutiveItem)} — добавит уже готовый {@link ConsecutiveItem},
 * <li>{@link #str(String)} — строку-константу, которая будет записана в выражение как есть,
 * <li>{@link #col(DbColumn)} — колонку, в выражении будет записано её имя, и
 * <li>{@link #val(DbValueContainer)}, а также куча перегруженных — значение контейнера или явно переданное значение (через вопросик).
 * </ul>
 *
 * @see NumericDbColumn#custom()
 */
public class NumericConsecutiveDbColumn<V extends Number> extends ValueConsecutiveDbColumn<V> implements NumericDbColumn<V> {

    public NumericConsecutiveDbColumn(DbNumberContainer<V> container) {
        super(container);
    }

    @Override public NumericConsecutiveDbColumn<V> item(ConsecutiveItem item) {
        super.append(item);
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> str(String constant) {
        super.append(new ConstantItem(constant));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> col(DbColumn column) {
        super.append(new DbColumnItem(column));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> col(String prefixStr, DbColumn<?> column) {
        super.append(new ConstantItem(prefixStr));
        super.append(new DbColumnItem(column));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> col(String prefixStr, DbColumn<?> column, String suffixStr) {
        super.append(new ConstantItem(prefixStr));
        super.append(new DbColumnItem(column));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> col(DbColumn<?> column, String suffixStr) {
        super.append(new DbColumnItem(column));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(DbValueContainer<?> valueContainer) {
        super.append(new ValueItem(valueContainer));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(String prefixStr, DbValueContainer<?> valueContainer) {
        super.append(new ConstantItem(prefixStr));
        super.append(new ValueItem(valueContainer));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(String prefixStr, DbValueContainer<?> valueContainer, String suffixStr) {
        super.append(new ConstantItem(prefixStr));
        super.append(new ValueItem(valueContainer));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(DbValueContainer<?> valueContainer, String suffixStr) {
        super.append(new ValueItem(valueContainer));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    // врапперы для возможных значений
    @Override public NumericConsecutiveDbColumn<V> val(int dbScale, BigDecimal value) {
        super.append(new ValueItem(new DbBigDecimal(dbScale, value)));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(byte[] value) {
        super.append(new ValueItem(new DbByteArray(value)));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(Date value) {
        super.append(new ValueItem(new DbDate(value)));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(Duration value) {
        super.append(new ValueItem(new DbDuration(value)));
        return this;
    }

    @Override public <E extends Enum<E>> NumericConsecutiveDbColumn<V> val(Class<E> cl, E value) {
        super.append(new ValueItem(new DbEnum<>(cl, value)));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(Flag value) {
        super.append(new ValueItem(new DbFlag(value)));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(Integer value) {
        super.append(new ValueItem(new DbInteger(value)));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(Long value) {
        super.append(new ValueItem(new DbLong(value)));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(String value) {
        super.append(new ValueItem(new DbString(value)));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(Time value) {
        super.append(new ValueItem(new DbTime(value)));
        return this;
    }

    @Override public NumericConsecutiveDbColumn<V> val(TwofacedTime value) {
        super.append(new ValueItem(new DbTwofacedTime(value)));
        return this;
    }

}
