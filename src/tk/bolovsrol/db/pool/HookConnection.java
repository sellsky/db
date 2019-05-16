package tk.bolovsrol.db.pool;

import tk.bolovsrol.utils.Nullable;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * Враппер коннекшна, которому можно навешивать хуки, которые будут исполнены после коммита и перед ролбеком.
 */
public class HookConnection implements Connection {

    public final Connection con;
    private boolean autoCommit;
    private LinkedHashSet<Runnable> commitTasks = null;
    private LinkedHashSet<Runnable> rollbackTasks = null;

    public HookConnection(Connection con) throws SQLException {
        this.con = con;
        this.autoCommit = con.getAutoCommit();
    }

    /**
     * Добавляет действие, которое надо выполнить после коммита.
     * <p>
     * Если соединение работает в режиме автокоммита, то действие будет выполнено немедленно.
     * <p>
     * Иначе добавляет действие в очередь. Если передаваемый таск уже добавлен ранее, то метод ничего не сделает.
     *
     * @param task
     */
    public void afterCommit(Runnable task) {
        if (autoCommit) {
            task.run();
        } else {
            if (commitTasks == null) { commitTasks = new LinkedHashSet<>(); }
            commitTasks.add(task);
        }
    }

    /**
     * Добавляет действие, которое надо выполнить перед ролбеком.
     * <p>
     * Если соединение работает в режиме автокоммита, то действие не будет выполнено никогда.
     * <p>
     * Иначе добавляет действие в очередь. Если передаваемый таск уже добавлен ранее, то метод ничего не сделает.
     *
     * @param task
     */
    public void beforeRollback(Runnable task) {
        if (autoCommit) {
            // no-op
        } else {
            if (rollbackTasks == null) { rollbackTasks = new LinkedHashSet<>(); }
            rollbackTasks.add(task);
        }
    }

    /**
     * Сбрасывает поля, чтобы если в хуках будут использовать это соединение (не рекомендуется, но, в принципе, допустимо),
     * последующий коммит/ролбэк не зациклил хуки.
     * <p>
     * Затем, если tasks не нул, выполняет переданные задачи в порядке итератора.
     *
     * @param tasks
     */
    private void runTasks(@Nullable Set<Runnable> tasks) {
        this.commitTasks = null;
        this.rollbackTasks = null;
        if (tasks != null) { tasks.forEach(Runnable::run); }
    }

    @Override public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (autoCommit != this.autoCommit) {
            this.autoCommit = autoCommit;
            con.setAutoCommit(autoCommit);
            runTasks(commitTasks);
        }
    }

    @Override public void commit() throws SQLException {
        con.commit();
        runTasks(commitTasks);
    }

    @Override public void rollback() throws SQLException {
        runTasks(rollbackTasks);
        con.rollback();
    }

    @Override public void close() throws SQLException {
        runTasks(rollbackTasks);
        con.close();
    }

    public LinkedHashSet<Runnable> getAfterCommitTasks() {
        return commitTasks;
    }

    public LinkedHashSet<Runnable> getBeforeRollbackTasks() {
        return rollbackTasks;
    }

    // Дальше делегации коннекшну

    @Override public Statement createStatement() throws SQLException {return con.createStatement();}

    @Override public PreparedStatement prepareStatement(String sql) throws SQLException {return con.prepareStatement(sql);}

    @Override public CallableStatement prepareCall(String sql) throws SQLException {return con.prepareCall(sql);}

    @Override public String nativeSQL(String sql) throws SQLException {return con.nativeSQL(sql);}

    @Override public boolean getAutoCommit() throws SQLException {return con.getAutoCommit();}

    @Override public boolean isClosed() throws SQLException {return con.isClosed();}

    @Override public DatabaseMetaData getMetaData() throws SQLException {return con.getMetaData();}

    @Override public void setReadOnly(boolean readOnly) throws SQLException {con.setReadOnly(readOnly);}

    @Override public boolean isReadOnly() throws SQLException {return con.isReadOnly();}

    @Override public void setCatalog(String catalog) throws SQLException {con.setCatalog(catalog);}

    @Override public String getCatalog() throws SQLException {return con.getCatalog();}

    @Override public void setTransactionIsolation(int level) throws SQLException {con.setTransactionIsolation(level);}

    @Override public int getTransactionIsolation() throws SQLException {return con.getTransactionIsolation();}

    @Override public SQLWarning getWarnings() throws SQLException {return con.getWarnings();}

    @Override public void clearWarnings() throws SQLException {con.clearWarnings();}

    @Override public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {return con.createStatement(resultSetType, resultSetConcurrency);}

    @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {return con.prepareStatement(sql, resultSetType, resultSetConcurrency);}

    @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {return con.prepareCall(sql, resultSetType, resultSetConcurrency);}

    @Override public Map<String, Class<?>> getTypeMap() throws SQLException {return con.getTypeMap();}

    @Override public void setTypeMap(Map<String, Class<?>> map) throws SQLException {con.setTypeMap(map);}

    @Override public void setHoldability(int holdability) throws SQLException {con.setHoldability(holdability);}

    @Override public int getHoldability() throws SQLException {return con.getHoldability();}

    @Override public Savepoint setSavepoint() throws SQLException {return con.setSavepoint();}

    @Override public Savepoint setSavepoint(String name) throws SQLException {return con.setSavepoint(name);}

    @Override public void rollback(Savepoint savepoint) throws SQLException {con.rollback(savepoint);}

    @Override public void releaseSavepoint(Savepoint savepoint) throws SQLException {con.releaseSavepoint(savepoint);}

    @Override public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {return con.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);}

    @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {return con.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);}

    @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {return con.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);}

    @Override public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {return con.prepareStatement(sql, autoGeneratedKeys);}

    @Override public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {return con.prepareStatement(sql, columnIndexes);}

    @Override public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {return con.prepareStatement(sql, columnNames);}

    @Override public Clob createClob() throws SQLException {return con.createClob();}

    @Override public Blob createBlob() throws SQLException {return con.createBlob();}

    @Override public NClob createNClob() throws SQLException {return con.createNClob();}

    @Override public SQLXML createSQLXML() throws SQLException {return con.createSQLXML();}

    @Override public boolean isValid(int timeout) throws SQLException {return con.isValid(timeout);}

    @Override public void setClientInfo(String name, String value) throws SQLClientInfoException {con.setClientInfo(name, value);}

    @Override public void setClientInfo(Properties properties) throws SQLClientInfoException {con.setClientInfo(properties);}

    @Override public String getClientInfo(String name) throws SQLException {return con.getClientInfo(name);}

    @Override public Properties getClientInfo() throws SQLException {return con.getClientInfo();}

    @Override public Array createArrayOf(String typeName, Object[] elements) throws SQLException {return con.createArrayOf(typeName, elements);}

    @Override public Struct createStruct(String typeName, Object[] attributes) throws SQLException {return con.createStruct(typeName, attributes);}

    @Override public void setSchema(String schema) throws SQLException {con.setSchema(schema);}

    @Override public String getSchema() throws SQLException {return con.getSchema();}

    @Override public void abort(Executor executor) throws SQLException {con.abort(executor);}

    @Override public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {con.setNetworkTimeout(executor, milliseconds);}

    @Override public int getNetworkTimeout() throws SQLException {return con.getNetworkTimeout();}

    @Override public <T> T unwrap(Class<T> iface) throws SQLException {return con.unwrap(iface);}

    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException {return con.isWrapperFor(iface);}
}
