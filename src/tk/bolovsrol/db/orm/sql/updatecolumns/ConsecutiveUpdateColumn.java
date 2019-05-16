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
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.containers.consecutive.ConsecutiveSqlExpression;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConsecutiveItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConstantItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.DbColumnItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ValueItem;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;
import tk.bolovsrol.utils.Flag;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TwofacedTime;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
 * @see UpdateColumn#custom()
 */
public class ConsecutiveUpdateColumn<V> extends ConsecutiveSqlExpression implements UpdateColumn<V> {

    public ConsecutiveUpdateColumn() {
    }

    @Override public ConsecutiveUpdateColumn<V> item(ConsecutiveItem item) {
        super.append(item);
        return this;
    }

    @Override public ConsecutiveUpdateColumn<V> str(String constant) {
        super.append(new ConstantItem(constant));
        return this;
    }

    @Override public ConsecutiveUpdateColumn<V> col(DbColumn<?> column) {
        super.append(new DbColumnItem(column));
        return this;
    }

    @Override public ConsecutiveUpdateColumn col(String prefixStr, DbColumn<?> column) {
        super.append(new ConstantItem(prefixStr));
        super.append(new DbColumnItem(column));
        return this;
    }

    @Override public ConsecutiveUpdateColumn col(String prefixStr, DbColumn<?> column, String suffixStr) {
        super.append(new ConstantItem(prefixStr));
        super.append(new DbColumnItem(column));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public ConsecutiveUpdateColumn col(DbColumn<?> column, String suffixStr) {
        super.append(new DbColumnItem(column));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public ConsecutiveUpdateColumn val(DbValueContainer<?> valueContainer) {
        super.append(new ValueItem(valueContainer));
        return this;
    }

    @Override public ConsecutiveUpdateColumn val(String prefixStr, DbValueContainer<?> valueContainer) {
        super.append(new ConstantItem(prefixStr));
        super.append(new ValueItem(valueContainer));
        return this;
    }

    @Override public ConsecutiveUpdateColumn val(String prefixStr, DbValueContainer<?> valueContainer, String suffixStr) {
        super.append(new ConstantItem(prefixStr));
        super.append(new ValueItem(valueContainer));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public ConsecutiveUpdateColumn val(DbValueContainer<?> valueContainer, String suffixStr) {
        super.append(new ValueItem(valueContainer));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    // врапперы для возможных значений
    public ConsecutiveUpdateColumn<V> val(int dbScale, BigDecimal value) {
        super.append(new ValueItem(new DbBigDecimal(dbScale, value)));
        return this;
    }

    public ConsecutiveUpdateColumn<V> val(byte[] value) {
        super.append(new ValueItem(new DbByteArray(value)));
        return this;
    }

    public ConsecutiveUpdateColumn<V> val(Date value) {
        super.append(new ValueItem(new DbDate(value)));
        return this;
    }

    public ConsecutiveUpdateColumn<V> val(Duration value) {
        super.append(new ValueItem(new DbDuration(value)));
        return this;
    }

    public <E extends Enum<E>> ConsecutiveUpdateColumn<V> val(Class<E> cl, E value) {
        super.append(new ValueItem(new DbEnum<>(cl, value)));
        return this;
    }

    public ConsecutiveUpdateColumn<V> val(Flag value) {
        super.append(new ValueItem(new DbFlag(value)));
        return this;
    }

    public ConsecutiveUpdateColumn<V> val(Integer value) {
        super.append(new ValueItem(new DbInteger(value)));
        return this;
    }

    public ConsecutiveUpdateColumn<V> val(Long value) {
        super.append(new ValueItem(new DbLong(value)));
        return this;
    }

    public ConsecutiveUpdateColumn<V> val(String value) {
        super.append(new ValueItem(new DbString(value)));
        return this;
    }

    public ConsecutiveUpdateColumn<V> val(Time value) {
        super.append(new ValueItem(new DbTime(value)));
        return this;
    }

    public ConsecutiveUpdateColumn<V> val(TwofacedTime value) {
        super.append(new ValueItem(new DbTwofacedTime(value)));
        return this;
    }

    // ДБ-методы
    @Override public void writeSqlExpressionForUpdate(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        super.writeConsecutiveSqlExpression(sb, databaseProductName, tableAliases);
    }

    @Override public int putValuesForUpdate(PreparedStatement ps, int pos) throws SQLException, DbException {
        return super.putConsecutiveValues(ps, pos);
    }

    @Override public void appendSqlLogValuesForUpdate(List<String> list) throws DbException {
        super.appendConsecutiveSqlLogValues(list);
    }

    @Override public void valueCommittedAtUpdate() {
        super.consecutiveCommitted();
    }
}
