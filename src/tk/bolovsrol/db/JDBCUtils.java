package tk.bolovsrol.db;

import tk.bolovsrol.utils.log.Log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Класс содержит статические методы для работы с JDBC объектами
 */
public class JDBCUtils {

    /**
     * Закрывает соеденение к БД
     *
     * @param con Соеденение, которое мы хотим закрыть
     */
    public static void close(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (Exception e) {
                Log.warning(e);
            }
        }
    }

    /**
     * Освобождает <code>Statement</code>
     *
     * @param stmt <code>Statement</code>, который мы хотим освободить
     */
    public static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                Log.warning(e);
            }
        }
    }

    /**
     * Освобождает <code>ResultSet</code>
     *
     * @param rs <code>ResultSet</code>, который мы хотим освободить
     */
    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (Exception e) {
                Log.warning(e);
            }
        }
    }
}
