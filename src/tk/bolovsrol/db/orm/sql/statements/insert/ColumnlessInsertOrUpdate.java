package tk.bolovsrol.db.orm.sql.statements.insert;

import tk.bolovsrol.db.orm.fields.DbDataField;

import java.util.Collection;

/**
 * Предварительный инсерт-объект, которому нужно задать вставляемые колонки одним из предлагаемых методов,
 * чтобы получить {@link RowlessInsertOrUpdate}.
 */
public interface ColumnlessInsertOrUpdate {

    /**
     * Задаёт колонки, которые будут вставлены.
     * <p>
     * Использует прям переданную коллекцию, так что осторожно с ней.
     *
     * @param fields
     * @return Insert
     */
	RowlessInsertOrUpdate columns(Collection<? extends DbDataField<?, ?>> fields);

	/**
	 * Задаёт колонки, которые будут вставлены.
	 *
	 * @param fields
     * @return Insert
     */
	RowlessInsertOrUpdate columns(DbDataField<?, ?>... fields);

	/**
	 * Задаёт единственную колонку, которая будет вставлена.
	 *
	 * @param field
     * @return Insert
     */
	RowlessInsertOrUpdate column(DbDataField<?, ?> field);

	/**
	 * Задаёт в качестве колонок все поля используемой таблицы.
	 *
	 * @return Insert
	 */
	RowlessInsertOrUpdate allColumns();
}
