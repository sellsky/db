package tk.bolovsrol.db;

/**
 * Список идентификаторов поддерживаемых СУБД.
 * <p>
 * Эти строки потенциально возвращает Connection#getMetaData()#getDatabaseProductName().
 */
public final class DatabaseProductNames {

    public static final String MYSQL = "MySQL";
    public static final String POSTGRESQL = "PostgreSQL";
    public static final String ORACLE = "Oracle";

    private DatabaseProductNames() {
    }
}
