package tk.bolovsrol.db.orm;

import tk.bolovsrol.utils.UnexpectedBehaviourException;

/** Попытались загрузить запись, а её не нашлось. */
public class RecordNotFoundException extends UnexpectedBehaviourException {

    public RecordNotFoundException(String message) {
        super(message);
    }

    public RecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
