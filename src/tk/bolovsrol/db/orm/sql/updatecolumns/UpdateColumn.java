package tk.bolovsrol.db.orm.sql.updatecolumns;

import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.InsertOrUpdateColumn;

import java.sql.PreparedStatement;

/**
 * Сущность, управляющая выражением присваивания в UPDATE-операторе,
 * она может записывать себя в SQL-выражении и проставлять своё значение в стейтмент {@link PreparedStatement}.
 *
 * @see ConsecutiveUpdateColumn
 */
public interface UpdateColumn<V> extends InsertOrUpdateColumn<V> {
    /**
     * @return болванка для самодельной колонки
     * @see ConsecutiveUpdateColumn
     */
    static <V> ConsecutiveUpdateColumn<V> custom() {
        return new ConsecutiveUpdateColumn<>();
    }

}
