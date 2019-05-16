package tk.bolovsrol.db.orm.properties;

import tk.bolovsrol.db.orm.sql.statements.select.Browser;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.properties.sources.ReadOnlySource;
import tk.bolovsrol.utils.properties.sources.SourceUnavailableException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Соурс-интерфейс между пропертями и записями в базе данных. */
public class DbDataObjectReadOnlySource implements ReadOnlySource {

    /** Объект, вычитывающий значения из базы данных. */
    public interface Resource {
        /**
         * Возвращает ключ актуальной загруженной записи.
         *
         * @return ключ
         * @throws SQLException
         */
        String getResourceKey() throws SQLException;

        /**
         * Возвращает значение актуальной загруженной записи.
         *
         * @return значение
         * @throws SQLException
         */
        String getResourceValue() throws SQLException;

        /**
         * Загружает запись по ключу.
         *
         * @param con
         * @param key ключ
         * @return true, если запись найдена, false иначе
         * @throws SQLException
         */
        boolean loadByResourceKey(Connection con, String key) throws SQLException;

        /**
         * Перебирает все доступные записи.
         * <p/>
         * Если браузер вычитает несколько записей с одинаковым ключом,
         * то будет использована первая из них.
         *
         * @param con
         * @return браузер, перебирающий все записи
         * @throws SQLException
         */
        Browser browseResourceKeys(Connection con) throws SQLException;
    }

    private final Connection con;
    private final Resource resource;

    public DbDataObjectReadOnlySource(Connection con, Resource resource) {
        this.con = con;
        this.resource = resource;
    }

    @Override public String expand(String localBranchKey) {
        return localBranchKey;
    }

    @Override public String get(String key) throws SourceUnavailableException {
        try {
            if (key == resource.getResourceKey()
                  || (key != null && key.equals(resource.getResourceKey()))
                  || resource.loadByResourceKey(con, key)) {
                return resource.getResourceValue();
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new SourceUnavailableException(e);
        }
    }

    @Override public boolean has(String key) throws SourceUnavailableException {
        try {
            return resource.loadByResourceKey(con, key) && resource.getResourceValue() != null;
        } catch (SQLException e) {
            throw new SourceUnavailableException(e);
        }
    }

    @Override public Map<String, String> dump() throws SourceUnavailableException {
        try {
            Map<String, String> hm = new LinkedHashMap<>();
            try (Browser br = resource.browseResourceKeys(con)) {
                while (br.next()) {
                    String resourceKey = resource.getResourceKey();
                    String resourceValue = resource.getResourceValue();
                    if (resourceValue != null && !hm.containsKey(resourceKey)) {
                        hm.put(resourceKey, resourceValue);
                    }
                }
            }
            return hm;
        } catch (SQLException e) {
            throw new SourceUnavailableException(e);
        }
    }

    @Override public String toString() {
        return "dump=" + Spell.get(dump());
    }

    @Override
    public String getIdentity(String key) throws SourceUnavailableException {
        return null;
    }
}
