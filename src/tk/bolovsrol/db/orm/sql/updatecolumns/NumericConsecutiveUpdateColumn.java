package tk.bolovsrol.db.orm.sql.updatecolumns;

import tk.bolovsrol.db.orm.containers.DbBigDecimal;
import tk.bolovsrol.db.orm.containers.DbByteArray;
import tk.bolovsrol.db.orm.containers.DbDate;
import tk.bolovsrol.db.orm.containers.DbDuration;
import tk.bolovsrol.db.orm.containers.DbEnum;
import tk.bolovsrol.db.orm.containers.DbFlag;
import tk.bolovsrol.db.orm.containers.DbInteger;
import tk.bolovsrol.db.orm.containers.DbLong;
import tk.bolovsrol.db.orm.containers.DbString;
import tk.bolovsrol.db.orm.containers.DbTime;
import tk.bolovsrol.db.orm.containers.DbTwofacedTime;
import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConsecutiveItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConstantItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.DbColumnItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ValueItem;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;
import tk.bolovsrol.utils.Flag;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TwofacedTime;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Date;

/**
 * Сборная апдейт-колонка для БД, можно использовать для создания необычных колонок непосредственно в месте употребления,
 * достаточно подобавлять нужные элементы;
 * <p>
 * Следует вызывать (в цепочке) такие методы:
 * <ul>
 * <li>{@link #item(ConsecutiveItem)} — добавит уже готовый {@link ConsecutiveItem},
 * <li>{@link #str(String)} — строку-константу, которая будет записана в выражение как есть,
 * <li>{@link #col(DbColumn)} — колонку, в выражении будет записано её имя, и
 * <li>{@link #val(DbValueContainer)}, а также куча перегруженных — значение контейнера или явно переданное значение (через вопросик).
 * </ul>
 *
 * @see NumericUpdateColumn#custom()
 */
public class NumericConsecutiveUpdateColumn<V extends Number> extends ConsecutiveUpdateColumn<V> implements NumericUpdateColumn<V> {

    @Override public NumericConsecutiveUpdateColumn<V> item(ConsecutiveItem item) {
        super.append(item);
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn<V> str(String constant) {
        super.append(new ConstantItem(constant));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn<V> col(DbColumn<?> column) {
        super.append(new DbColumnItem(column));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn col(String prefixStr, DbColumn<?> column) {
        super.append(new ConstantItem(prefixStr));
        super.append(new DbColumnItem(column));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn col(String prefixStr, DbColumn<?> column, String suffixStr) {
        super.append(new ConstantItem(prefixStr));
        super.append(new DbColumnItem(column));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn col(DbColumn<?> column, String suffixStr) {
        super.append(new DbColumnItem(column));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn val(DbValueContainer<?> valueContainer) {
        super.append(new ValueItem(valueContainer));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn val(String prefixStr, DbValueContainer<?> valueContainer) {
        super.append(new ConstantItem(prefixStr));
        super.append(new ValueItem(valueContainer));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn val(String prefixStr, DbValueContainer<?> valueContainer, String suffixStr) {
        super.append(new ConstantItem(prefixStr));
        super.append(new ValueItem(valueContainer));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn val(DbValueContainer<?> valueContainer, String suffixStr) {
        super.append(new ValueItem(valueContainer));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    // врапперы для возможных значений
    @Override public NumericConsecutiveUpdateColumn<V> val(int dbScale, BigDecimal value) {
        super.append(new ValueItem(new DbBigDecimal(dbScale, value)));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn<V> val(byte[] value) {
        super.append(new ValueItem(new DbByteArray(value)));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn<V> val(Date value) {
        super.append(new ValueItem(new DbDate(value)));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn<V> val(Duration value) {
        super.append(new ValueItem(new DbDuration(value)));
        return this;
    }

    @Override public <E extends Enum<E>> NumericConsecutiveUpdateColumn<V> val(Class<E> cl, E value) {
        super.append(new ValueItem(new DbEnum<>(cl, value)));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn<V> val(Flag value) {
        super.append(new ValueItem(new DbFlag(value)));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn<V> val(Integer value) {
        super.append(new ValueItem(new DbInteger(value)));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn<V> val(Long value) {
        super.append(new ValueItem(new DbLong(value)));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn<V> val(String value) {
        super.append(new ValueItem(new DbString(value)));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn<V> val(Time value) {
        super.append(new ValueItem(new DbTime(value)));
        return this;
    }

    @Override public NumericConsecutiveUpdateColumn<V> val(TwofacedTime value) {
        super.append(new ValueItem(new DbTwofacedTime(value)));
        return this;
    }

}
