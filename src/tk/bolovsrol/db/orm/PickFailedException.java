package tk.bolovsrol.db.orm;

/** Ошибка вычитывания значения поля из резалтсета. */
public class PickFailedException extends RuntimeException {

    public PickFailedException(String message) {
        super(message);
    }

    public PickFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PickFailedException(Throwable cause) {
        super(cause);
    }
}
