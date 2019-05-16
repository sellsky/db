package tk.bolovsrol.db.orm.sql.dbcolumns;

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
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.containers.consecutive.ListingSqlExpression;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConditionItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConsecutiveItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConstantItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.DbColumnItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ValueItem;
import tk.bolovsrol.utils.Flag;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TwofacedTime;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Сборная колонка для БД, можно использовать для создания необычных колонок непосредственно в месте употребления,
 * достаточно подобавлять нужные элементы, всё остальное сборная колонка сделает сама.
 * <p>
 * Следует вызывать (в цепочке) такие методы:
 * <ul>
 * <li>{@link #item(ConsecutiveItem)} — добавит уже готовый {@link ConsecutiveItem},
 * <li>{@link #str(String)} — строку-константу, которая будет записана в выражение как есть,
 * <li>{@link #col(DbColumn)} — колонку, в выражении будет записано её имя, и
 * <li>{@link #val(DbValueContainer)}, а также куча перегруженных — значение контейнера или явно переданное значение (через вопросик).
 * </ul>
 *
 * @see DbColumn#custom()
 */
public class ListingDbColumn<V> extends ListingSqlExpression implements DbColumn<V> {


    public ListingDbColumn(ConsecutiveItem prefixItemOrNull, ConsecutiveItem delimiterItem) {
        super(prefixItemOrNull, delimiterItem, null);
    }

    public ListingDbColumn(ConsecutiveItem prefixItemOrNull, ConsecutiveItem delimiterItem, ConsecutiveItem suffixItemOrNull) {
        super(prefixItemOrNull, delimiterItem, suffixItemOrNull);
    }

    public ListingDbColumn(String prefixItemOrNull, String delimiterItem) {
        super(prefixItemOrNull, delimiterItem, null);
    }

    public ListingDbColumn(String prefixItemOrNull, String delimiterItem, String suffixItemOrNull) {
        super(prefixItemOrNull, delimiterItem, suffixItemOrNull);
    }

    public static <V> ListingDbColumn<V> function(String name) {
        return new ListingDbColumn<V>(name + '(', ",", ")");
    }

    @Override public ListingDbColumn<V> item(ConsecutiveItem item) {
        super.append(item);
        return this;
    }

    @Override public ListingDbColumn<V> str(String constant) {
        super.append(new ConstantItem(constant));
        return this;
    }

    @Override public ListingDbColumn<V> col(DbColumn column) {
        super.append(new DbColumnItem(column));
        return this;
    }

    @Override public ListingDbColumn<V> val(DbValueContainer<?> valueContainer) {
        super.append(new ValueItem(valueContainer));
        return this;
    }

    // врапперы для возможных значений
    public ListingDbColumn<V> val(int dbScale, BigDecimal value) {
        super.append(new ValueItem(new DbBigDecimal(dbScale, value)));
        return this;
    }

    public ListingDbColumn<V> val(byte[] value) {
        super.append(new ValueItem(new DbByteArray(value)));
        return this;
    }

    public ListingDbColumn<V> val(Date value) {
        super.append(new ValueItem(new DbDate(value)));
        return this;
    }

    public ListingDbColumn<V> val(Duration value) {
        super.append(new ValueItem(new DbDuration(value)));
        return this;
    }

    public <E extends Enum<E>> ListingDbColumn<V> val(Class<E> cl, E value) {
        super.append(new ValueItem(new DbEnum<>(cl, value)));
        return this;
    }

    public ListingDbColumn<V> val(Flag value) {
        super.append(new ValueItem(new DbFlag(value)));
        return this;
    }

    public ListingDbColumn<V> val(Integer value) {
        super.append(new ValueItem(new DbInteger(value)));
        return this;
    }

    public ListingDbColumn<V> val(Long value) {
        super.append(new ValueItem(new DbLong(value)));
        return this;
    }

    public ListingDbColumn<V> val(String value) {
        super.append(new ValueItem(new DbString(value)));
        return this;
    }

    public ListingDbColumn<V> val(Time value) {
        super.append(new ValueItem(new DbTime(value)));
        return this;
    }

    public ListingDbColumn<V> val(TwofacedTime value) {
        super.append(new ValueItem(new DbTwofacedTime(value)));
        return this;
    }

    public ListingDbColumn<V> condition(Condition condition) {
        super.append(new ConditionItem(condition));
        return this;
    }

    // ДБ-методы
    @Override public void writeSqlExpressionForSelect(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        super.writeListSqlExpression(sb, databaseProductName, tableAliases);
    }

    @Override public int putValuesForSelect(PreparedStatement ps, int pos) throws SQLException, DbException {
        return super.putConsecutiveValues(ps, pos);
    }

    /**
     * @throws SQLException может выбрасываться в наследниках
     */
    @Override public int pickValuesForSelect(ResultSet rs, int pos) throws SQLException, DbException {
        return pos;
    }

    @Override public void appendSqlLogValuesForSelect(List<String> list) throws DbException {
        super.appendConsecutiveSqlLogValues(list);
    }
}
