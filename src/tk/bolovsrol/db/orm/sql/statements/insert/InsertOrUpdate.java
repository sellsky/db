package tk.bolovsrol.db.orm.sql.statements.insert;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;
import tk.bolovsrol.db.orm.sql.statements.SqlStatement;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.InsertOrUpdateColumn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Инсерт, которому можно задать , который может обновить заданные колонки в случае срабатывания уникального ключа.
 * <p>
 * Этот объект возвращают методы, устанавливающие ряды значений или манипулирующие колонками для обновления.
 *
 * @see Insert
 */
public interface InsertOrUpdate extends SqlStatement, RowlessInsertOrUpdate {

    /**
     * Выполняет инсерт.
     * <p>
     * Если ни одна строка не задана, выкинет DbException.
     *
     * @param con соединение
     * @return количество обновлённых записей
     * @throws SQLException
     * @throws DbException
     */
    int execute(Connection con) throws SQLException, DbException;

    /**
     * Очищает список рядов значений для вставки.
     *
     * @return InsertOrUpdate
     */
    InsertOrUpdate clearValues();

    /** @return количество установленных рядов значений. */
    int getValueRowCount();

    /** @return true, если в инсерте установлен хотя бы один ряд, иначе false */
    boolean hasValueRows();

    /** @return true, если в инсерте установлена обновляемая колонка */
    boolean hasUpdateColumns();

    /**
     * Очищает набор обновляемых колонок.
     * <p>
     * Это чтобы инициализировать инсерт каким-нибудь БД-объектом вне цикла,
     * а затем многократно использовать инсерт, указывая нужные апдейт-колонки для каждой итерации.
     *
	 * @return Insert
	 */
	InsertOrUpdate dropUpdateColumns();


    /**
     * Добавляет обновляемую колонку.
     *
     * @param updateColumn обновляемая колонка
     * @return InsertOrUpdate
     */
    InsertOrUpdate orUpdate(InsertOrUpdateColumn<?> updateColumn);

    /**
     * Добавляет обновляемую колонку: указанное поле будет обновлено указанным значением.
     *
     * @param field обновляемое поле
     * @param value записываемое значение
     * @return InsertOrUpdate
     */
    <V, C extends DbValueContainer<V>> InsertOrUpdate orUpdate(DbDataField<V, C> field, V value);

    /**
     * Добавляет обновляемую колонку: указанное поле будет обновлено указанным значением.
     *
     * @param column обновляемое поле
     * @param value записываемое значение
     * @return InsertOrUpdate
     */
    <V> InsertOrUpdate orUpdate(DbColumn<V> column, DbValueContainer<V> value);

    /**
     * Добавляет указанные обновляемые колонки.
     *
     * @param updateColumns обновляемые колонки
     * @return InsertOrUpdate
     */
    InsertOrUpdate orUpdate(InsertOrUpdateColumn... updateColumns);

    /**
     * Добавляет указанные обновляемые колонки.
     *
     * @param updateColumns обновляемые колонки
     * @return InsertOrUpdate
     */
    InsertOrUpdate orUpdate(Collection<? extends InsertOrUpdateColumn<?>> updateColumns);

    /**
     * Добавляет для указанных полей обновляемую колонку, проставляющую вставляемое значение.
     * Короче говоря, ON DUPLICATE KEY UPDATE со указанными полями column=VALUES(column).
     *
     * @param columns обновляемые поля
     * @return InsertOrUpdate
     */
    InsertOrUpdate orUpdateWithValues(DbColumn<?>... columns);

    /**
     * Добавляет для каждого поля, участвующего в инсерте, обновляемую колонку, проставляющую вставляемое значение.
     * Короче говоря, ON DUPLICATE KEY UPDATE со всеми полями column=VALUES(column).
     *
     * @return InsertOrUpdate
     */
	InsertOrUpdate orUpdateEveryColumnWithValues();

	/** Добавляет обновляемую колонку, если её значение в БД is NULL. */
	InsertOrUpdate orUpdateIfNull(DbColumn<?> column);

	/** Добавляет обновляемую колонку, если новое значение для неё not NULL. */
	InsertOrUpdate orUpdateIfHasValue(DbColumn<?> ... columns);
}


