package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.Log;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/** Записывает, когда какая-либо запись была вынута из кэша по первичному ключу. */
public class LatestAccessWriter implements Runnable {

    private static final Map<Class<? extends RefDbDataObject>, CatalogAndTableName> NAME_CACHE = new ConcurrentHashMap<>();

    private static final LatestAccessWriter INSTANCE;

    static {
        INSTANCE = new LatestAccessWriter();
        final Thread writerThread = new Thread(INSTANCE, "LatestAccessWriter");
        Runtime.getRuntime().addShutdownHook(
              new Thread("LatestAccessWriterAtShutdown") {
                  @Override
                  public void run() {
                      try {
                          writerThread.interrupt();
                          writerThread.join();
                          INSTANCE.writeAtShutdown();
                      } catch (InterruptedException e) {
                          Log.exception(e);
                      }
                  }
              }
        );
        writerThread.setDaemon(true);
        writerThread.start();
    }

    public static LatestAccessWriter instance() {
        return INSTANCE;
    }

    private final Object lock = new Object();

    private Map<LatestAccessCoordinates, Date> data;

    private final LatestAccessDbdo la = new LatestAccessDbdo();


    private LatestAccessWriter() {
    }

    public static void fix(Class<? extends RefDbDataObject> rdbdoClass, Long objectId) {
        INSTANCE.fixInternal(new LatestAccessCoordinates(retrieveCatalogAndTableName(rdbdoClass), objectId), new Date());
    }

    private static CatalogAndTableName retrieveCatalogAndTableName(Class<? extends RefDbDataObject> rdbdoClass) {
        CatalogAndTableName result = NAME_CACHE.get(rdbdoClass);
        if (result == null) {
            try {
                Constructor<? extends RefDbDataObject> constructor;
                try {
                    constructor = rdbdoClass.getConstructor();
                } catch (NoSuchMethodException ignored) {
                    constructor = rdbdoClass.getDeclaredConstructor();
                }
                constructor.setAccessible(true);
                result = CatalogAndTableName.retrieveCatalogAndTableName(constructor.newInstance());
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot create an instance of " + Spell.get(rdbdoClass), e);
            }
            NAME_CACHE.put(rdbdoClass, result);
        }
        return result;
    }

    public static void fix(String catalogName, String tableName, Long objectId) {
        INSTANCE.fixInternal(new LatestAccessCoordinates(new CatalogAndTableName(catalogName, tableName), objectId), new Date());
    }

    /**
     * Добавляет информацию в очередь для фиксирования.
     *
     * @param lac
     * @param when
     */
    private void fixInternal(LatestAccessCoordinates lac, Date when) {
        synchronized (lock) {
            if (data == null) {
                data = new TreeMap<>();
            }
            Date replaced = data.put(lac, when);
            // если мы ничего не заменили, значит размер карты увеличился и мог превысить порог срабатывания.
            if (replaced == null && data.size() > VersionCacheConst.LATEST_ACCESS_THRESHOLD) {
                lock.notifyAll();
            }
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Map<LatestAccessCoordinates, Date> data;
                synchronized (lock) {
                    do {
                        lock.wait(VersionCacheConst.LATEST_ACCESS_SLEEP); // если проснёмся раньше времени, в общем, пофиг
                    } while (this.data == null);
                    data = this.data;
                    this.data = null;
                }
                write(data);
            }
        } catch (InterruptedException e) {
            // ok
        }
    }

    private void writeAtShutdown() {
        synchronized (lock) {
            if (data != null) {
                write(data);
            }
        }
    }

    private void write(Map<LatestAccessCoordinates, Date> lacs) {
        for (Map.Entry<LatestAccessCoordinates, Date> entry : lacs.entrySet()) {
            write(entry.getKey(), entry.getValue());
        }
    }

    protected void write(LatestAccessCoordinates lac, Date date) {
        try (Connection con = ConnectionManager.getConnection()) {
            la.set(lac.getTableName(), lac.getObjectId(), date);
			la.insert(la.dataFields()).valueRowFromColumns().orUpdateEveryColumnWithValues().execute(con);
		} catch (Exception e) {
			Log.warning("Failed to update access info for object " + Spell.get(lac) + ". ", e);
		}
	}
}
