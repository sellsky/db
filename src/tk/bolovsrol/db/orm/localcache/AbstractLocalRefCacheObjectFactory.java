package tk.bolovsrol.db.orm.localcache;

import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.db.orm.sql.statements.select.Browser;
import tk.bolovsrol.db.orm.sql.statements.select.Select;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.localcache.LocalCacheObjectFactory;
import tk.bolovsrol.utils.localcache.ObjectCreationFailedException;

import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractLocalRefCacheObjectFactory<O extends RefDbDataObject> implements LocalCacheObjectFactory<Long, O> {

    private final String dbName;

    protected AbstractLocalRefCacheObjectFactory() {
        this(null);
    }

    protected AbstractLocalRefCacheObjectFactory(String dbName) {
        this.dbName = dbName;
    }

    protected abstract O newDbdo();

    @Override public O newObject(Long id) throws ObjectCreationFailedException {
        O o = newDbdo();
        try {
            try (Connection con = ConnectionManager.getConnection(dbName)) {
                try {
					if (o.selectAllColumns().where(o.idField().eq(id)).load(con)) {
						return o;
					} else {
                        return null;
                    }
                } finally {
                    JDBCUtils.close(con);
                }
            }
        } catch (Exception e) {
            throw new ObjectCreationFailedException("Cannot load " + Spell.get(o.getSqlTableName()) + " by id " + Spell.get(id), e);
        }
    }

    @Override public Map<Long, O> newObjects(Collection<Long> ids) throws ObjectCreationFailedException {
        Map<Long, O> result = new LinkedHashMap<>(ids.size());
        O template = newDbdo();
		Select s = template.select().where(template.idField().in(ids));
		try {
			try (Connection con = ConnectionManager.getConnection(dbName)) {
                try {
                    try (Browser br = s.browse(con)) {
                        while (br.next()) {
                            result.put(template.getId(), template.copyTo(newDbdo()));
                        }
                    }
                } finally {
                    JDBCUtils.close(con);
                }
            }
        } catch (Exception e) {
            throw new ObjectCreationFailedException("Cannot load " + Spell.get(template.getSqlTableName()) + " for ids " + Spell.get(ids), e);
        }
        return result;
    }
}
