package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.utils.Composition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Композиция из двух строк — названий каталога и таблицы.
 *
 * @see #retrieveCatalogAndTableName(DbDataObject)
 */
public class CatalogAndTableName extends Composition {

    private static final Map<DbDataObject, CatalogAndTableName> NAME_CACHE = new ConcurrentHashMap<>();

    public static CatalogAndTableName retrieveCatalogAndTableName(DbDataObject dbdo) {
        CatalogAndTableName result = NAME_CACHE.get(dbdo);
        if (result == null) {
            result = new CatalogAndTableName(dbdo.getSqlCatalogName(), dbdo.getSqlTableName());
            NAME_CACHE.put(dbdo, result);
        }
        return result;
    }

    public CatalogAndTableName(String catalogName, String tableName) {
        super(catalogName, tableName);
    }

    public String getCatalogName() {
        return (String) get(0);
    }

    public String getTableName() {
        return (String) get(1);
    }

    public String toString() {
        return getCatalogName() + '.' + getTableName();
    }
}
