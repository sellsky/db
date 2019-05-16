package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;

/** Ошибка загрузки объекта, назначенного reference-полю. */
public class BrokenReferenceException extends PickFailedException {
    private static final long serialVersionUID = 1L;

    public BrokenReferenceException(String message) {
        super(message);
    }

    public BrokenReferenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public BrokenReferenceException(Throwable cause) {
        super(cause);
    }
}
