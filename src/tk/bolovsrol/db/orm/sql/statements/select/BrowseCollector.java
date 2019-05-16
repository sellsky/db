package tk.bolovsrol.db.orm.sql.statements.select;

import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.ValueDbColumn;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Обработчик каждого загруженного браузером значения колонки.
 *
 * @see Select#browse(Connection, ValueDbColumn, BrowseCollector)
 */
@FunctionalInterface public interface BrowseCollector<V, E extends Exception> {
    /**
     * Браузер загрузил очередное значение, и этот метод вызыван, чтобы обработать загруженное.
     */
    void collect(V value) throws SQLException, DbException, E;
}
