package tk.bolovsrol.db.orm.sql.statements.select.orderby;

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
 * Директива сортировки, собираемая из последовательности элементов {@link ConsecutiveItem}.
 * За исключением направления сортировки — оно хранится в специальном полее и прицепляется к собранному выражению справа.
 * <p>
 * Можно использовать для создания необычных ордеров непосредственно в месте употребления,
 * достаточно подобавлять нужные элементы.
 * <p>
 * Следует вызывать (в цепочке) такие методы:
 * <ul>
 * <li>{@link #item(ConsecutiveItem)} — добавит уже готовый {@link ConsecutiveItem},
 * <li>{@link #str(String)} — строку-константу, которая будет записана в выражение как есть,
 * <li>{@link #col(DbColumn)} — колонку, в выражении будет записано её имя, и
 * <li>{@link #val(DbValueContainer)}, а также куча перегруженных — значение контейнера или явно переданное значение (через вопросик).
 * </ul>
 *
 * @see OrderByEntity#custom()
 */
public class ConsecutiveOrderByEntity extends ConsecutiveSqlExpression implements OrderByEntity {

    private Direction direction = Direction.ASC;

    public ConsecutiveOrderByEntity() {
    }

    public ConsecutiveOrderByEntity(Direction direction) {
        this.direction = direction;
    }

    /**
     * Устанавливает направление сортировки по возрастанию.
     * Это по умолчанию, можно не вызывать специально.
     * <p>
     * Это не часть цепочки, а включатель режима, можно вызывать в произвольном месте.
     *
     * @return this
     */
    public ConsecutiveOrderByEntity asc() {
        this.direction = Direction.ASC;
        return this;
    }

    /**
     * Устанавливает направление сортировки по убыванию.
     * <p>
     * Это не часть цепочки, а включатель режима, можно вызывать в произвольном месте.
     *
     * @return this
     */
    public ConsecutiveOrderByEntity desc() {
        this.direction = Direction.ASC;
        return this;
    }

    /**
     * Устанавливает указанное направление сортировки.
     * <p>
     * Это не часть цепочки, а включатель режима, можно вызывать в произвольном месте.
     *
     * @return this
     */
    public ConsecutiveOrderByEntity dir(Direction direction) {
        this.direction = direction;
        return this;
    }

    @Override public ConsecutiveOrderByEntity item(ConsecutiveItem item) {
        super.item(item);
        return this;
    }

    @Override public ConsecutiveOrderByEntity str(String constant) {
        super.append(new ConstantItem(constant));
        return this;
    }

    @Override public ConsecutiveOrderByEntity col(DbColumn<?> column) {
        super.append(new DbColumnItem(column));
        return this;
    }

    @Override public ConsecutiveOrderByEntity col(String prefixStr, DbColumn<?> column) {
        super.append(new ConstantItem(prefixStr));
        super.append(new DbColumnItem(column));
        return this;
    }

    @Override public ConsecutiveOrderByEntity col(String prefixStr, DbColumn<?> column, String suffixStr) {
        super.append(new ConstantItem(prefixStr));
        super.append(new DbColumnItem(column));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public ConsecutiveOrderByEntity col(DbColumn<?> column, String suffixStr) {
        super.append(new DbColumnItem(column));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public ConsecutiveOrderByEntity val(DbValueContainer<?> valueContainer) {
        super.append(new ValueItem(valueContainer));
        return this;
    }

    @Override public ConsecutiveOrderByEntity val(String prefixStr, DbValueContainer<?> valueContainer) {
        super.append(new ConstantItem(prefixStr));
        super.append(new ValueItem(valueContainer));
        return this;
    }

    @Override public ConsecutiveOrderByEntity val(String prefixStr, DbValueContainer<?> valueContainer, String suffixStr) {
        super.append(new ConstantItem(prefixStr));
        super.append(new ValueItem(valueContainer));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    @Override public ConsecutiveOrderByEntity val(DbValueContainer<?> valueContainer, String suffixStr) {
        super.append(new ValueItem(valueContainer));
        super.append(new ConstantItem(suffixStr));
        return this;
    }

    // врапперы для возможных значений
    public ConsecutiveOrderByEntity val(int dbScale, BigDecimal value) {
        super.append(new ValueItem(new DbBigDecimal(dbScale, value)));
        return this;
    }

    public ConsecutiveOrderByEntity val(byte[] value) {
        super.append(new ValueItem(new DbByteArray(value)));
        return this;
    }

    public ConsecutiveOrderByEntity val(Date value) {
        super.append(new ValueItem(new DbDate(value)));
        return this;
    }

    public ConsecutiveOrderByEntity val(Duration value) {
        super.append(new ValueItem(new DbDuration(value)));
        return this;
    }

    public <E extends Enum<E>> ConsecutiveOrderByEntity val(Class<E> cl, E value) {
        super.append(new ValueItem(new DbEnum<>(cl, value)));
        return this;
    }

    public ConsecutiveOrderByEntity val(Flag value) {
        super.append(new ValueItem(new DbFlag(value)));
        return this;
    }

    public ConsecutiveOrderByEntity val(Integer value) {
        super.append(new ValueItem(new DbInteger(value)));
        return this;
    }

    public ConsecutiveOrderByEntity val(Long value) {
        super.append(new ValueItem(new DbLong(value)));
        return this;
    }

    public ConsecutiveOrderByEntity val(String value) {
        super.append(new ValueItem(new DbString(value)));
        return this;
    }

    public ConsecutiveOrderByEntity val(Time value) {
        super.append(new ValueItem(new DbTime(value)));
        return this;
    }

    public ConsecutiveOrderByEntity val(TwofacedTime value) {
        super.append(new ValueItem(new DbTwofacedTime(value)));
        return this;
    }

    // ДБ-методы
    @Override public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        super.writeConsecutiveSqlExpression(sb, databaseProductName, tableAliases);
        direction.writeSqlExpression(sb, databaseProductName, tableAliases);
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        return super.putConsecutiveValues(ps, pos);
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
        super.appendConsecutiveSqlLogValues(list);
    }

}
