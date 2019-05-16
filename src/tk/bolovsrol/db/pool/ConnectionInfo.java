package tk.bolovsrol.db.pool;

import tk.bolovsrol.utils.StringDumpBuilder;

/** Справочный класс для динамической статистики по открытым соединениям. */
public class ConnectionInfo {
    public final int hash;
    public final String serverName;
    public final String dbUrl;
    public final long idleAge;
    public final long inUseAge;

    public ConnectionInfo(int hash, String serverName, String dbUrl, long idleAge, long inUseAge) {
        this.hash = hash;
        this.serverName = serverName;
        this.dbUrl = dbUrl;
        this.idleAge = idleAge;
        this.inUseAge = inUseAge;
    }

    public String toString() {
        return new StringDumpBuilder()
                .append("hash", hash)
                .append("serverName", serverName)
                .append("dbUrl", dbUrl)
                .append("idleAge", idleAge)
                .append("inUseAge", inUseAge)
                .toString();
    }
}

