package tk.bolovsrol.db.pool;

/**
 * Этому интерфейсу враппер PooledConnection`а будет сообщать
 * о событиях, произошедших с его соединением.
 */
public interface PooledConnectionEventListener {

    /**
     * Пользователь закрыл соединение, и его снова можно использовать.
     *
     * @param pcw данные соединения
     */
    void connectionClosed(PooledConnectionWrapper pcw);

    /**
     * В соединении произошла ошибка, оно закрыто и больше не может быть использовано.
     *
     * @param pcw данные соединения
     */
    void connectionBroken(PooledConnectionWrapper pcw);
}
