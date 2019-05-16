package tk.bolovsrol.db.orm.properties;

import tk.bolovsrol.utils.properties.sources.PlainSource;
import tk.bolovsrol.utils.properties.sources.SourceUnavailableException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class DbDataObjectPlainSource extends DbDataObjectReadOnlySource implements PlainSource {

    public interface Resource extends DbDataObjectReadOnlySource.Resource {
        void setResource(Connection con, String key, String value) throws SQLException;

        void dropResource(Connection con, String key) throws SQLException;

        void clear(Connection con) throws SQLException;
    }

    private final Connection con;
    private final Resource resource;

    public DbDataObjectPlainSource(Connection con, Resource resource) {
        super(con, resource);
        this.con = con;
        this.resource = resource;
    }

    @Override public DbDataObjectPlainSource clear() throws SourceUnavailableException {
        try {
            resource.clear(con);
        } catch (SQLException e) {
            throw new SourceUnavailableException(e);
        }
        return this;
    }

    @Override public DbDataObjectPlainSource set(String key, String value) throws SourceUnavailableException {
        if (value == null) {
            drop(key);
        } else {
            try {
                resource.setResource(con, key, value);
            } catch (SQLException e) {
                throw new SourceUnavailableException(e);
            }
        }
        return this;
    }

    @Override public DbDataObjectPlainSource setAll(Map<String, String> matter) throws SourceUnavailableException {
        for (Map.Entry<String, String> entry : matter.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override public DbDataObjectPlainSource drop(String key) throws SourceUnavailableException {
        try {
            resource.dropResource(con, key);
        } catch (SQLException e) {
            throw new SourceUnavailableException(e);
        }
        return this;
    }
}
