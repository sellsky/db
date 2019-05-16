package tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.updatecolumns.UpdateColumn;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Апдейт-колонка, которую можно использовать только в INSERT ... ON DUPLICATE KEY UPDATE.
 * <p>
 * Таких колонок три штуки, {@link AddValues}, {@link Values} и {@link IfNullThenValues}.
 * <p>
 * Для обычных инсерт-колонок нужно наследовать {@link UpdateColumn}.
 */
public interface InsertOrUpdateColumn<V> {
    /**
     * Записывает выражение апдейт-колонки.
     *
     * @param sb куда писать
     * @param databaseProductName для какой СУБД писать
     * @param tableAliases алиасы таблиц или нул
     * @throws DbException
     * @throws SQLException
     * @see tk.bolovsrol.db.DatabaseProductNames
     */
    void writeSqlExpressionForUpdate(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException;

    /**
     * Проставляет значения вопросиков апдейт-колонки.
     *
     * @param ps куда проставлять
     * @param pos с какого номера проставлять
     * @return pos + количество записанных значений
     * @throws SQLException
     * @throws DbException
     */
    default int putValuesForUpdate(PreparedStatement ps, int pos) throws SQLException, DbException {return pos;}

    /**
     * Добавляет значения вопросиков апдейт-колонки в список.
     *
     * @param list куда добавлять
     * @throws DbException
     */
    default void appendSqlLogValuesForUpdate(List<String> list) throws DbException {}

    /** Отмечает, что колонка была сохранена в БД апдейтом. */
    default void valueCommittedAtUpdate() {}

}
