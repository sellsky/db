package tk.bolovsrol.db.orm.sql.statements.select;

import tk.bolovsrol.db.orm.sql.DbException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Обработчик каждой загруженной браузером строки резалтсета.
 *
 * @see Select#browse(Connection, BrowseConsumer)
 */
@FunctionalInterface public interface BrowseConsumer<E extends Exception> {
    /**
     * Браузер загрузил очередную строку резалтсета, и этот метод вызыван, чтобы обработать загруженное.
     */
    void rowLoaded() throws SQLException, DbException, E;
}
