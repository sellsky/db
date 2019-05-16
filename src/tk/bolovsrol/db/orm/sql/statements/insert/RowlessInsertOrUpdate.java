package tk.bolovsrol.db.orm.sql.statements.insert;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;

import java.util.Collection;
import java.util.function.Function;

/**
 * Предварительный инсерт-объект, которому нужно добавить ряд значений одним из предлагаемых методов,
 * чтобы получить {@link InsertOrUpdate}.
 */
public interface RowlessInsertOrUpdate {

    /**
     * Добавляет строку значений. Количество элементов массива должно совпадать с количеством заданных колонок.
     *
	 * @param values строка значений.
	 * @return InsertOrUpdate
	 */
    InsertOrUpdate valueRow(Object... values);

    /**
     * Добавляет строку значений. Количество элементов массива должно совпадать с количеством заданных колонок.
     *
	 * @param values строка значений.
	 * @return InsertOrUpdate
	 */
    InsertOrUpdate valueContainerRow(DbValueContainer<?>... values);

	/**
	 * Добавляет строку значений. Количество элементов коллекции должно совпадать с количеством заданных колонок.
	 *
	 * @param values строка значений.
	 * @return InsertOrUpdate
	 */
    InsertOrUpdate valueContainerRow(Collection<? extends DbValueContainer<?>> values);

	/**
	 * Добавляет в качестве строки значений содержимое вставляемых полей-колонок.
	 *
	 * @return InsertOrUpdate
	 */
	InsertOrUpdate valueRowFromColumns();

	/**
	 * Добавляет в качестве строк значений содержимое полей переданных объектов {@link DbDataObject}.
	 * Количество и качество полей объектов должно совпадать с количеством заданных в инсерте колонок.
	 *
	 * @return InsertOrUpdate
	 */
    <D extends DbDataObject> InsertOrUpdate valueRowsFrom(Collection<D> dbdos, Function<D, Collection<? extends DbValueContainer<?>>> mapper);

	/**
	 * Затычка-ассершн, отдающая неработопособный инсерт без рядов значений.
	 * Перед запуском инсерта нужно добавить хотя бы один ряд значений.
	 *
	 * @return неработоспособный InsertOrUpdate
	 */
	InsertOrUpdate valuesAddedLater();
}
