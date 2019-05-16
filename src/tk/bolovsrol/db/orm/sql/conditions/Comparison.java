package tk.bolovsrol.db.orm.sql.conditions;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConsecutiveItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConstantItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.RegexpItem;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;
import tk.bolovsrol.db.orm.sql.dbcolumns.LikeableDbColumn;
import tk.bolovsrol.db.orm.sql.dbcolumns.ValueDbColumn;

/**
 * Cравнение:
 * колонка &lt;оператор&gt; [значение|колонка]
 * значение &lt;оператор&gt; колонка
 * колонка &lt;оператор&gt;
 * <p>
 * Куча статических методов с интуитивно понятной номенклатурой: <code>оператор[Суффикс](левый аргумент, правый аргумент)</code>.
 * Суффикс:<ul>
 * <li>у методов, оперирующих явным значением, суффикса нет,
 * <li>у методов, оперирующих значением, содержащимся в контейнере, суффикс «<code>Value</code>»,
 * <li>у методов, оперирующих полем и его собственным значением, суффикс «<code>Self</code>»,
 * <li>у методов, оперирующих исключительно колонками, суффикс «<code>Column</code>».
 * </ul>
 */
public class Comparison extends ConsecutiveCondition {

    private static final ConstantItem EQ = new ConstantItem("=");
    private static final ConstantItem NE = new ConstantItem("<>");
    private static final ConstantItem LE = new ConstantItem("<=");
    private static final ConstantItem LT = new ConstantItem("<");
    private static final ConstantItem GE = new ConstantItem(">=");
    private static final ConstantItem GT = new ConstantItem(">");
    private static final ConstantItem IS_NULL = new ConstantItem(" IS NULL");
    private static final ConstantItem IS_NOT_NULL = new ConstantItem(" IS NOT NULL");
    private static final ConstantItem LIKE = new ConstantItem(" LIKE ");
    private static final ConstantItem NOT_LIKE = new ConstantItem(" NOT LIKE ");
    /** Позитивное сравнение, A REGEXP B. */
    public static final RegexpItem REGEXP = new RegexpItem(" REGEXP ", "~");
    /** Негативное сравнение, A NOT REGEXP B. */
    private static final RegexpItem NOT_REGEXP = new RegexpItem(" NOT REGEXP ", "!~");

    private Comparison() {
    }

    protected static <V> Condition unary(DbColumn<V> column, ConsecutiveItem operator) {
        return new ConsecutiveCondition().col(column).item(operator);
    }

    protected static <V> Condition columnVsValue(DbColumn<V> column, ConsecutiveItem operator, DbValueContainer<V> valueContainer) {
        return new ConsecutiveCondition().col(column).item(operator).val(valueContainer);
    }

    protected static <V> Condition valueVsColumn(DbValueContainer<V> valueContainer, ConsecutiveItem operator, DbColumn<V> column) {
        return new ConsecutiveCondition().val(valueContainer).item(operator).col(column);
    }

    protected static <V> Condition columnVsColumn(DbColumn<V> left, ConsecutiveItem operator, DbColumn<V> right) {
        return new ConsecutiveCondition().col(left).item(operator).col(right);
    }

    // нул -------------------------------------------------------------------------------------------
    public static Condition isNull(DbColumn<?> column) {
        return unary(column, IS_NULL);
    }

    public static Condition isNotNull(DbColumn<?> column) {
        return unary(column, IS_NOT_NULL);
    }

    // колонка vs. контейнер -------------------------------------------------------------------------------------------
    public static <V> Condition eqValue(DbColumn<V> column, DbValueContainer<V> container) {
        return columnVsValue(column, EQ, container);
    }

    public static <V> Condition neValue(DbColumn<V> column, DbValueContainer<V> container) {
        return columnVsValue(column, NE, container);
    }

    public static <V> Condition leValue(DbColumn<V> column, DbValueContainer<V> container) {
        return columnVsValue(column, LE, container);
    }

    public static <V> Condition ltValue(DbColumn<V> column, DbValueContainer<V> container) {
        return columnVsValue(column, LT, container);
    }

    public static <V> Condition geValue(DbColumn<V> column, DbValueContainer<V> container) {
        return columnVsValue(column, GE, container);
    }

    public static <V> Condition gtValue(DbColumn<V> column, DbValueContainer<V> container) {
        return columnVsValue(column, GT, container);
    }

    public static Condition likeValue(LikeableDbColumn column, DbValueContainer<String> container) {
        return new ConsecutiveCondition().col(column).item(LIKE).val(container);
    }

    public static Condition notLikeValue(LikeableDbColumn column, DbValueContainer<String> container) {
        return new ConsecutiveCondition().col(column).item(NOT_LIKE).val(container);
    }

    public static Condition regexpValue(LikeableDbColumn column, DbValueContainer<String> container) {
        return new ConsecutiveCondition().col(column).item(REGEXP).val(container);
    }

    public static Condition notRegexpValue(LikeableDbColumn column, DbValueContainer<String> container) {
        return new ConsecutiveCondition().col(column).item(NOT_REGEXP).val(container);
    }

    // поле vs. значение =========================================================
    public static <V> Condition eq(DbDataField<V, ?> field, V value) {
        return columnVsValue(field, EQ, field.wrap(value));
    }

    public static <V> Condition ne(DbDataField<V, ?> field, V value) {
        return columnVsValue(field, NE, field.wrap(value));
    }

    public static <V> Condition le(DbDataField<V, ?> field, V value) {
        return columnVsValue(field, LE, field.wrap(value));
    }

    public static <V> Condition lt(DbDataField<V, ?> field, V value) {
        return columnVsValue(field, LT, field.wrap(value));
    }

    public static <V> Condition ge(DbDataField<V, ?> field, V value) {
        return columnVsValue(field, GE, field.wrap(value));
    }

    public static <V> Condition gt(DbDataField<V, ?> field, V value) {
        return columnVsValue(field, GT, field.wrap(value));
    }

    public static Condition likeValue(LikeableDbColumn column, String pattern) {
        return new ConsecutiveCondition().col(column).item(LIKE).val(pattern);
    }

    public static Condition notLikeValue(LikeableDbColumn column, String pattern) {
        return new ConsecutiveCondition().col(column).item(NOT_LIKE).val(pattern);
    }

    public static Condition regexpValue(LikeableDbColumn column, String pattern) {
        return new ConsecutiveCondition().col(column).item(REGEXP).val(pattern);
    }

    public static Condition notRegexpValue(LikeableDbColumn column, String pattern) {
        return new ConsecutiveCondition().col(column).item(NOT_REGEXP).val(pattern);
    }

    // поле vs. поле ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    public static <V> Condition eqColumn(DbColumn<V> left, DbColumn<V> right) {
        return columnVsColumn(left, EQ, right);
    }

    public static <V> Condition neColumn(DbColumn<V> left, DbColumn<V> right) {
        return columnVsColumn(left, NE, right);
    }

    public static <V> Condition leColumn(DbColumn<V> left, DbColumn<V> right) {
        return columnVsColumn(left, LE, right);
    }

    public static <V> Condition ltColumn(DbColumn<V> left, DbColumn<V> right) {
        return columnVsColumn(left, LT, right);
    }

    public static <V> Condition geColumn(DbColumn<V> left, DbColumn<V> right) {
        return columnVsColumn(left, GE, right);
    }

    public static <V> Condition gtColumn(DbColumn<V> left, DbColumn<V> right) {
        return columnVsColumn(left, GT, right);
    }

    // поле vs. значение поля
    public static <V> Condition eqSelf(ValueDbColumn<V> column) {
        return columnVsValue(column, EQ, column);
    }

    public static <V> Condition neSelf(ValueDbColumn<V> column) {
        return columnVsValue(column, NE, column);
    }

    public static <V> Condition leSelf(ValueDbColumn<V> column) {
        return columnVsValue(column, LE, column);
    }

    public static <V> Condition ltSelf(ValueDbColumn<V> column) {
        return columnVsValue(column, LT, column);
    }

    public static <V> Condition geSelf(ValueDbColumn<V> column) {
        return columnVsValue(column, GE, column);
    }

    public static <V> Condition gtSelf(ValueDbColumn<V> column) {
        return columnVsValue(column, GT, column);
    }

    // контейнер vs. колонка -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    public static <V> Condition like(DbValueContainer<String> container, LikeableDbColumn<?> column) {
        return new ConsecutiveCondition().val(container).item(LIKE).col(column);
    }

    public static <V> Condition notLike(DbValueContainer<String> container, LikeableDbColumn<?> column) {
        return new ConsecutiveCondition().val(container).item(NOT_LIKE).col(column);
    }

    public static <V> Condition regexp(DbValueContainer<String> container, LikeableDbColumn<V> column) {
        return new ConsecutiveCondition().val(container).item(REGEXP).col(column);
    }

    public static <V> Condition notRegexp(DbValueContainer<String> container, LikeableDbColumn<V> column) {
        return new ConsecutiveCondition().val(container).item(NOT_REGEXP).col(column);
    }

    // значение vs. колонка =+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
    public static <V> Condition like(V value, DbDataField<V, ?> field) {
        return valueVsColumn(field.wrap(value), LIKE, field);
    }

    public static <V> Condition notLike(V value, DbDataField<V, ?> field) {
        return valueVsColumn(field.wrap(value), NOT_LIKE, field);
    }

    public static <V> Condition regexp(V value, DbDataField<V, ?> field) {
        return valueVsColumn(field.wrap(value), REGEXP, field);
    }

    public static <V> Condition notRegexp(V value, DbDataField<V, ?> field) {
        return valueVsColumn(field.wrap(value), NOT_REGEXP, field);
    }
}
