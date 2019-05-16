package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.conditions.ColumnInRange;
import tk.bolovsrol.db.orm.sql.conditions.Comparison;
import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.conditions.In;
import tk.bolovsrol.db.orm.sql.conditions.Or;
import tk.bolovsrol.db.orm.sql.statements.select.orderby.Direction;
import tk.bolovsrol.db.orm.sql.statements.select.orderby.OrderByColumn;
import tk.bolovsrol.db.orm.sql.statements.select.orderby.OrderByEntity;
import tk.bolovsrol.db.orm.sql.updatecolumns.UpdateColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.UpdateColumnWithColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.UpdateColumnWithValue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Колонка в БД. Сущность, управляющая записью выражения, возвращающего значение.
 * Она может записывать себя в SQL-выражении, проставлять параметры в стейтмент {@link PreparedStatement}
 * и вычитывать своё значение из резалтсета {@link ResultSet}.
 *
 * @see ConsecutiveDbColumn
 */
public interface DbColumn<V> {

    void writeSqlExpressionForSelect(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException;

    int putValuesForSelect(PreparedStatement ps, int pos) throws SQLException, DbException;

    int pickValuesForSelect(ResultSet rs, int pos) throws SQLException, DbException;

    void appendSqlLogValuesForSelect(List<String> list) throws DbException;

    /**
     * @return болванка для самодельной колонки
     * @see ConsecutiveDbColumn
     */
    static <V> ConsecutiveDbColumn<V> custom() {
        return new ConsecutiveDbColumn<>();
    }

    /**
     * Создаёт и возвращает колонку-вилку: в SQL-выражении она записывает
     * выражение поля, а значение вычитывает в указанный контейнер.
     *
     * @return созданная колонка
     */
    default ForkDbColumn<V> fork(DbValueContainer<V> resultContainer) {
        return new ForkDbColumn<>(this, resultContainer);
    }

    default ValueDbColumn<V> distinct(DbValueContainer<V> resultContainer) {
        return UniFunction.distinct(this, resultContainer);
    }

    default ValueDbColumn<V> length(DbValueContainer<V> resultContainer) {
        return UniFunction.length(this, resultContainer);
    }

    default ValueDbColumn<V> min(DbValueContainer<V> resultContainer) {
        return UniFunction.min(this, resultContainer);
    }

    default ValueDbColumn<V> max(DbValueContainer<V> resultContainer) {
        return UniFunction.max(this, resultContainer);
    }

    default ValueDbColumn<Integer> count() {
        return new Count(this);
    }

    default Condition isNull() {
        return Comparison.isNull(this);
    }

    default Condition isNotNull() {
        return Comparison.isNotNull(this);
    }

    // -- compare to value container
    default Condition eqValue(DbValueContainer<V> container) {
        return Comparison.eqValue(this, container);
    }

    default Condition inValues(Collection<? extends DbValueContainer<V>> containers) {
        return In.in(this, containers);
    }

    default Condition neValue(DbValueContainer<V> container) {
        return Comparison.neValue(this, container);
    }

    default Condition notInValues(Collection<? extends DbValueContainer<V>> containers) {
        return In.notIn(this, containers);
    }

    default Condition leValue(DbValueContainer<V> container) {
        return Comparison.leValue(this, container);
    }

    default Condition ltValue(DbValueContainer<V> container) {
        return Comparison.ltValue(this, container);
    }

    default Condition geValue(DbValueContainer<V> container) {
        return Comparison.geValue(this, container);
    }

    default Condition gtValue(DbValueContainer<V> container) {
        return Comparison.gtValue(this, container);
    }

    default Or isNullOrEqValue(DbValueContainer<V> container) {
        return new Or(Comparison.isNull(this), Comparison.eqValue(this, container));
    }

    default Or isNullOrInValues(Collection<? extends DbValueContainer<V>> containers) {
        return new Or(Comparison.isNull(this), In.in(this, containers));
    }

    default Condition isNullOrNeValue(DbValueContainer<V> container) {
        return new Or(Comparison.isNull(this), Comparison.neValue(this, container));
    }

    default Or isNullOrNotInValues(Collection<? extends DbValueContainer<V>> containers) {
        return new Or(Comparison.isNull(this), In.notIn(this, containers));
    }

    default Condition isNullOrLeValue(DbValueContainer<V> container) {
        return new Or(Comparison.isNull(this), Comparison.leValue(this, container));
    }

    default Condition isNullOrLtValue(DbValueContainer<V> container) {
        return new Or(Comparison.isNull(this), Comparison.ltValue(this, container));
    }

    default Condition isNullOrGeValue(DbValueContainer<V> container) {
        return new Or(Comparison.isNull(this), Comparison.geValue(this, container));
    }

    default Condition isNullOrGtValue(DbValueContainer<V> container) {
        return new Or(Comparison.isNull(this), Comparison.gtValue(this, container));
    }

    // -- compare to db column
    default Condition eqColumn(DbColumn<V> target) {
        return Comparison.eqColumn(this, target);
    }

    default Condition neColumn(DbColumn<V> target) {
        return Comparison.neColumn(this, target);
    }

    default Condition leColumn(DbColumn<V> target) {
        return Comparison.leColumn(this, target);
    }

    default Condition ltColumn(DbColumn<V> target) {
        return Comparison.ltColumn(this, target);
    }

    default Condition geColumn(DbColumn<V> target) {
        return Comparison.geColumn(this, target);
    }

    default Condition gtColumn(DbColumn<V> target) {
        return Comparison.gtColumn(this, target);
    }

    default Condition inRange(DbValueContainer<V> from, DbValueContainer<V> to) {
        return new ColumnInRange(from, this, to);
    }

    // -- order bys
    default OrderByEntity asc() {
        return new OrderByColumn(this, Direction.ASC);
    }

    default OrderByEntity desc() {
        return new OrderByColumn(this, Direction.DESC);
    }

    // --- updates

    /**
     * Создаёт и возвращает апдейт-колонку, обновляющую текущую колонку указанным в контейнере значением.
     *
     * @param container
     * @return апдейт-колонка
     */
    default UpdateColumn<V> withValue(DbValueContainer<V> container) {
        return new UpdateColumnWithValue<>(this, container);
    }

    /**
     * Создаёт и возвращает апдейт-колонку, обновляющую текущую колонку значением указанной колонки БД.
     *
     * @param sourceColumn
     * @return апдейт-колонка
     */
    default UpdateColumn<V> withColumn(DbColumn<V> sourceColumn) {
        return new UpdateColumnWithColumn<>(this, sourceColumn);
    }


}


