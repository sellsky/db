package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.containers.ValueContainer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Контейнер хранит какое-то одно значение указанного типа.
 * <p>
 * Умеет проставлять это значение в стейтмент {@link PreparedStatement}
 * и читать его из резалтсета {@link ResultSet} и возвращать в виде строки для SQL-логa.
 */
public interface DbValueContainer<V> extends ValueContainer<V> {

    /**
     * Проставляет хранимое значение в {@link PreparedStatement} в указанную позицию.
     *
     * @param ps
     * @param pos
     * @throws SQLException
     */
    void putValue(PreparedStatement ps, int pos) throws SQLException;

    /**
     * Вычитывает значение из указанной колонки {@link ResultSet}.
     *
     * @param rs
     * @param columnIndex
     * @throws SQLException
     */
    void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException;

    /**
     * Передаёт значение контейнера в виде строки в кавычках для вывода в лог SQL-выражений.
     *
     * @return содержимое поля для SQL-лога.
     */
    String valueToSqlLogString();

}
