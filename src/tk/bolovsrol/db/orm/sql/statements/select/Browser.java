package tk.bolovsrol.db.orm.sql.statements.select;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.db.orm.object.AbstractDbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;

import java.sql.SQLException;

/**
 * Браузер управляет изменением состояния ассоциированного с ним {@link AbstractDbDataObject}.
 * <p/>
 * Вызываем метод в браузере, а изменяется объект. Вот так-то.
 * <p/>
 * При помощи этого объекта удобно перебирать подходящие под какое-нибудь условие
 * записи в БД.
 * <p/>
 * После использования браузер нужно закрыть методом {@link #close()}.
 * Оставлять браузер открытым после использования запрещается!
 */
public interface Browser extends AutoCloseable {

    /**
     * Загрузить объект следующим набором значений.
     * <p/>
     * При вызове этого метода будет изменён ассоциированный с браузером {@link AbstractDbDataObject}.
     * <p/>
     * После использования браузер нужно закрыть методом {@link #close()}.
     *
     * @return true, если загружен. false, если все значения кончились, и загружено ничего не было.
     * @throws SQLException
     */
    boolean next() throws SQLException, PickFailedException, DbException;

    /**
     * Закрывает браузер, освобождает использованные ресурсы.
     * Необходимо вызывать в finally-блоке итерации.
     */
    @Override default void close() {
        // noop
    }

}
