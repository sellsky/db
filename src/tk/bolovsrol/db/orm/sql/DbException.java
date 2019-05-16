package tk.bolovsrol.db.orm.sql;

/**
 * Ошибка, которая не должна возникать при работе SQL-подсистемы, но которая почему-то возникла.
 * Так как исключение с очень похожим названием SQLException уже существует для нужд JDBC, своё исключение
 * мы назвали вот этак.
 */
public class DbException extends RuntimeException {
    public DbException() {
    }

    public DbException(String message) {
        super(message);
    }

    public DbException(Throwable cause) {
        super(cause);
    }

    public DbException(String message, Throwable cause) {
        super(message, cause);
    }
}
