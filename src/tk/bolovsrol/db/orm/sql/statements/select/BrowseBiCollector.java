package tk.bolovsrol.db.orm.sql.statements.select;

import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.ValueDbColumn;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Обработчик значений каждой загруженной браузером пары колонок.
 *
 * @see Select#browse(Connection, ValueDbColumn, ValueDbColumn, BrowseBiCollector)
 */
@FunctionalInterface public interface BrowseBiCollector<A, B, E extends Exception> {
    /**
     * Браузер загрузил очередную пару значений, и этот метод вызыван, чтобы обработать загруженное.
     */
    void collect(A value1, B valueB) throws SQLException, DbException, E;
}
