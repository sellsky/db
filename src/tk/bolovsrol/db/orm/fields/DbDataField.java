package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.sql.conditions.ColumnInRange;
import tk.bolovsrol.db.orm.sql.conditions.Comparison;
import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.conditions.In;
import tk.bolovsrol.db.orm.sql.conditions.Or;
import tk.bolovsrol.db.orm.sql.dbcolumns.ForkDbColumn;
import tk.bolovsrol.db.orm.sql.dbcolumns.IfNull;
import tk.bolovsrol.db.orm.sql.dbcolumns.UniFunction;
import tk.bolovsrol.db.orm.sql.dbcolumns.ValueDbColumn;
import tk.bolovsrol.db.orm.sql.statements.select.GroupByEntity;
import tk.bolovsrol.db.orm.sql.updatecolumns.UpdateColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.UpdateColumnWithColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.UpdateColumnWithValue;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.IfHasValueThenValues;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.IfNullThenValues;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.InsertOrUpdateColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.Values;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Поле в таблице в базе данных.
 */
public interface DbDataField<V, C extends DbValueContainer<V>> extends ValueDbColumn<V>, GroupByEntity, UpdateColumn<V>, DbValueContainer<V> {

    /** @return название поля в базе данных */
    String getName();

    /**
     * Оборачивает значение в контейнер, соответствующий типу колонки.
     *
     * @param value значение
     * @return контейнер
     */
    C wrap(V value);

    /**
     * Оборачивает каждое значение в массиве в контейнер, соответствующий типу колонки.
     *
     * @param values массив значений
     * @return список контейнеров
     */
    @SuppressWarnings("unchecked")
    default List<C> wrap(V... values) {
        List<C> result = new ArrayList<>(values.length);
        for (V value : values) {
            result.add(wrap(value));
        }
        return result;
    }

    /**
     * Оборачивает каждое значение в коллекции в контейнер, соответствующий типу колонки.
     *
     * @param values коллекция значений
     * @return список контейнеров
     */
    default List<C> wrap(Collection<? extends V> values) {
        List<C> result = new ArrayList<>(values.size());
        for (V value : values) {
            result.add(wrap(value));
        }
        return result;
    }

    // ===== Любишь сахарок, люби и холодок. =====

    /**
     * Создаёт и возвращает колонку-вилку: в SQL-выражении она записывает
     * выражение поля, а значение вычитывает в специально созданный
     * контейнер (значение поля остаётся неизменным).
     *
     * @param initialValue исходное значение контейнера
     * @return созданная колонка
     */
    default ForkDbColumn<V> fork(V initialValue) {
        return new ForkDbColumn<>(this, this.wrap(initialValue));
    }

    /**
     * Создаёт и возвращает колонку-вилку: в SQL-выражении она записывает
     * выражение поля, а значение вычитывает в специально созданный
     * контейнер (значение поля остаётся неизменным).
     *
     * @return созданная колонка
     */
    default ForkDbColumn<V> fork() {
        return new ForkDbColumn<>(this, this.wrap((V) null));
    }

    // -- compare to explicit value
    default Condition eq(V value) {
        return Comparison.eq(this, value);
    }

    default Condition ne(V value) {
        return Comparison.ne(this, value);
    }

    default Condition le(V value) {
        return Comparison.le(this, value);
    }

    default Condition lt(V value) {
        return Comparison.lt(this, value);
    }

    default Condition ge(V value) {
        return Comparison.ge(this, value);
    }

    default Condition gt(V value) {
        return Comparison.gt(this, value);
    }

    default Or isNullOrEq(V value) {
        return new Or(Comparison.isNull(this), Comparison.eq(this, value));
    }

    default Or isNullOrNe(V value) {
        return new Or(Comparison.isNull(this), Comparison.ne(this, value));
    }

    default Or isNullOrLe(V value) {
        return new Or(Comparison.isNull(this), Comparison.le(this, value));
    }

    default Or isNullOrLt(V value) {
        return new Or(Comparison.isNull(this), Comparison.lt(this, value));
    }

    default Or isNullOrGe(V value) {
        return new Or(Comparison.isNull(this), Comparison.ge(this, value));
    }

    default Or isNullOrGt(V value) {
        return new Or(Comparison.isNull(this), Comparison.gt(this, value));
    }

    // -- compare to any of explicit values
    @SuppressWarnings("unchecked")
    default In in(V... values) {
        return In.in(this, wrap(values));
    }

    default In in(Collection<? extends V> values) {
        return In.in(this, wrap(values));
    }

    @SuppressWarnings("unchecked")
	default In notIn(V... values) {
        return In.notIn(this, wrap(values));
    }

    default In notIn(Collection<? extends V> values) {
        return In.notIn(this, wrap(values));
    }

    @SuppressWarnings("unchecked")
    default Or isNullOrIn(V... values) {
        return new Or(Comparison.isNull(this), In.in(this, wrap(values)));
    }

    default Or isNullOrIn(Collection<? extends V> values) {
        return new Or(Comparison.isNull(this), In.in(this, wrap(values)));
    }

    @SuppressWarnings("unchecked")
    default Or isNullOrNotIn(V... values) {
        return new Or(Comparison.isNull(this), In.notIn(this, wrap(values)));
    }

    default Or isNullOrNotIn(Collection<? extends V> values) {
        return new Or(Comparison.isNull(this), In.notIn(this, wrap(values)));
    }

    default Condition inRange(V from, V to) {
        return new ColumnInRange(wrap(from), this, wrap(to));
    }

    default ValueDbColumn<V> min(V defaultValue) {
        return UniFunction.min(this, wrap(defaultValue));
    }

    default ValueDbColumn<V> max(V defaultValue) {
        return UniFunction.max(this, wrap(defaultValue));
    }

    // -- updates

    /**
     * Создаёт и возвращает апдейт-колонку, обновляющую поле указанным в контейнере значением.
     *
     * @param value требуемое значение
     * @return апдейт-колонка
     */
    default UpdateColumn<V> with(V value) {
        return new UpdateColumnWithValue<>(this, value);
    }

    /**
     * Создаёт и возвращает апдейт-колонку, обновляющую поле нуллом.
     *
     * @return апдейт-колонка
     */
    default UpdateColumn<V> withNull() {
        return new UpdateColumnWithValue<>(this, (V) null);
    }

    /** Update-колонка, обновляет поле заданным в аргументе значением только если в этом поле
     * хранится null. */
    default UpdateColumn<V> withIfNull(V value) {
        return new UpdateColumnWithColumn<>(this, new IfNull<V>(this, value));
    }

    /**
     * Создаёт и возвращает апдейт-колонку для инсерта, обновляющую поле указанным в соответствующем ряду инсерта значением.
     *
     * @return инсерт-ор-апдейт-колонка
     */
    default InsertOrUpdateColumn<V> withValues() {
        return new Values<>(this);
    }

    /**
     * Создаёт и возвращает апдейт-колонку для инсерта, обновляющую поле указанным в соответствующем ряду инсерта значением.
     *
     * @return инсерт-ор-апдейт-колонка
     */
    default InsertOrUpdateColumn<V> ifNullThenWithValues() {
        return new IfNullThenValues<>(this);
    }

	/** Создаёт update-колонку для insert'а: если новое значение не null, то колонка обновится,
	 * иначе в колонке останется значение, какое было. */
	default InsertOrUpdateColumn<V> ifHasValueThenWithValues() {
		return new IfHasValueThenValues<>(this); }
}


