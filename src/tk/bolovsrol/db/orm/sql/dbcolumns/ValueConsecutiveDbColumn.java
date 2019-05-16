package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.PickFailedException;
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
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConsecutiveItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConstantItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.DbColumnItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ValueItem;
import tk.bolovsrol.utils.Flag;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.containers.ObjectCopyException;
import tk.bolovsrol.utils.containers.ValueContainer;
import tk.bolovsrol.utils.containers.ValueParsingException;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TwofacedTime;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;

/**
 * Сборная колонка для БД, умеющая отдавать значение, можно использовать для создания необычных колонок непосредственно
 * в месте употребления, достаточно подобавлять нужные элементы, всё остальное сборная колонка сделает сама.
 * <p>
 * Следует вызывать (в цепочке) такие методы:
 * <ul>
 * <li>{@link #item(ConsecutiveItem)} — добавит уже готовый {@link ConsecutiveItem},
 * <li>{@link #str(String)} — строку-константу, которая будет записана в выражение как есть,
 * <li>{@link #col(DbColumn)} — колонку, в выражении будет записано её имя, и
 * <li>{@link #val(DbValueContainer)}, а также куча перегруженных — значение контейнера или явно переданное значение (через вопросик).
 * </ul>
 *
 * @see ValueDbColumn#custom()
 */
public class ValueConsecutiveDbColumn<V> extends ConsecutiveDbColumn<V> implements ValueDbColumn<V> {

    protected final DbValueContainer<V> container;

    public ValueConsecutiveDbColumn(DbValueContainer<V> container) {
        this.container = container;
    }

    public DbValueContainer<V> getContainer() {
        return container;
    }

    @Override public ValueConsecutiveDbColumn<V> item(ConsecutiveItem item) {
        super.append(item);
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> str(String constant) {
        super.append(new ConstantItem(constant));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> col(DbColumn column) {
        super.append(new DbColumnItem(column));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> col(String prefixStr, DbColumn<?> column) {
        super.append(new ConstantItem(prefixStr));
        super.append(new DbColumnItem(column));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> col(String prefixStr, DbColumn<?> column, String suffixStr) {
        super.append(new ConstantItem(prefixStr));
        super.append(new DbColumnItem(column));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> col(DbColumn<?> column, String suffixStr) {
        super.append(new DbColumnItem(column));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(DbValueContainer<?> valueContainer) {
        super.append(new ValueItem(valueContainer));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(String prefixStr, DbValueContainer<?> valueContainer) {
        super.append(new ConstantItem(prefixStr));
        super.append(new ValueItem(valueContainer));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(String prefixStr, DbValueContainer<?> valueContainer, String suffixStr) {
        super.append(new ConstantItem(prefixStr));
        super.append(new ValueItem(valueContainer));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(DbValueContainer<?> valueContainer, String suffixStr) {
        super.append(new ValueItem(valueContainer));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    // врапперы для возможных значений
    @Override public ValueConsecutiveDbColumn<V> val(int dbScale, BigDecimal value) {
        super.append(new ValueItem(new DbBigDecimal(dbScale, value)));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(byte[] value) {
        super.append(new ValueItem(new DbByteArray(value)));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(Date value) {
        super.append(new ValueItem(new DbDate(value)));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(Duration value) {
        super.append(new ValueItem(new DbDuration(value)));
        return this;
    }

    @Override public <E extends Enum<E>> ValueConsecutiveDbColumn<V> val(Class<E> cl, E value) {
        super.append(new ValueItem(new DbEnum<>(cl, value)));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(Flag value) {
        super.append(new ValueItem(new DbFlag(value)));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(Integer value) {
        super.append(new ValueItem(new DbInteger(value)));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(Long value) {
        super.append(new ValueItem(new DbLong(value)));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(String value) {
        super.append(new ValueItem(new DbString(value)));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(Time value) {
        super.append(new ValueItem(new DbTime(value)));
        return this;
    }

    @Override public ValueConsecutiveDbColumn<V> val(TwofacedTime value) {
        super.append(new ValueItem(new DbTwofacedTime(value)));
        return this;
    }

    // ДБ-методы
    @Override public int pickValuesForSelect(ResultSet rs, int pos) throws SQLException, DbException {
        container.pickValue(rs, pos);
        return pos + 1;
    }

    @Override public V getValue() { return container.getValue(); }

    @Override public void setValue(V value) {container.setValue(value);}

    @Override public void dropValue() {container.dropValue();}

    @Override public boolean isValueNull() {return container.isValueNull();}

    @Override public V getCommittedValue() {return container.getCommittedValue();}

    @Override public void valueCommitted() {container.valueCommitted();}

    @Override public void rollbackValue() { container.rollbackValue(); }

    @Override public boolean isValueChanged() {return container.isValueChanged();}

    @Override public String valueToString() {return container.valueToString();}

    @Override public void parseValue(String value) throws ValueParsingException {container.parseValue(value);}

    @Override public String valueToLogString() {return container.valueToLogString();}

    @Override public void copyValueFrom(ValueContainer<V> source) throws ClassCastException, ObjectCopyException {container.copyValueFrom(source);}

    @Override public void putValue(PreparedStatement ps, int pos) throws SQLException {container.putValue(ps, pos);}

    @Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {container.pickValue(rs, columnIndex);}

    @Override public String valueToSqlLogString() {return container.valueToSqlLogString();}

    @Override public Class<V> getComponentType() {return container.getComponentType();}

    @Override public void parseValue(Json json) throws ValueParsingException {container.parseValue(json);}

    @Override public void putValue(Json json) {container.putValue(json);}
}
