package tk.bolovsrol.db.pool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Режим коммитов для коннекшн-менеджера {@link ConnectionManager}: автоматический и ручной. Всё очевидно и наглядно.
 */
public enum CommitMode {
	MANUAL(false),
	AUTO(true);

	private final boolean autoCommit;

	CommitMode(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public void setup(Connection con) throws SQLException {
		con.setAutoCommit(autoCommit);
	}
}
