package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.db.orm.sql.statements.select.Browser;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.spawnmap.HashSpawnMap;
import tk.bolovsrol.utils.spawnmap.SpawnMap;
import tk.bolovsrol.utils.time.SleepUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import static tk.bolovsrol.db.orm.versioncache.VersionCacheConst.CLEANUP_AGE;
import static tk.bolovsrol.db.orm.versioncache.VersionCacheConst.CLEANUP_ENABLED;
import static tk.bolovsrol.db.orm.versioncache.VersionCacheConst.CLEANUP_LATENCY;

/** Следит за изменениями версий и рассылает эти изменения внутреннему кэшу. */
class VersionWatcher implements Runnable {

    private final long refreshInterval;
    private final VersionWatcherListener listener;
    private final VersionHistoryDbdo versionHistory = new VersionHistoryDbdo();
    private final SpawnMap<CatalogAndTableName, VersionIdChangesContainer> changes = new HashSpawnMap<>(VersionIdChangesContainer::new);

    private Date nextRefresh;
    private Long maxVersionHistoryId;
    private Date nextCleanup = null;

    VersionWatcher(VersionWatcherListener listener, long refreshInterval) {
        this.listener = listener;
        this.refreshInterval = refreshInterval;
        this.nextRefresh = new Date();
    }

    @Override
    public void run() {
        try {
            init();
            while (true) {
                SleepUtils.sleepUntil(nextRefresh);
                refresh();
                cleanup();
                nextRefresh.setTime(System.currentTimeMillis() + refreshInterval);
            }
        } catch (InterruptedException ignored) {
            Log.info("Cache Version Watcher interrupted");
        } catch (Throwable e) {
            Log.exception(e);
        }
    }

    private void init() throws InterruptedException {
        nextRefresh = new Date(System.currentTimeMillis() + refreshInterval);

        Connection con = null;
        try {
            con = ConnectionManager.getConnection();
            maxVersionHistoryId = versionHistory.retrieveMaxId(con);
        } catch (SQLException e) {
            Log.exception(e);
        } finally {
            JDBCUtils.close(con);
        }
    }

    /**
     * Если настало время, удаляет застарелые записи из истории версий.
     */
    private void cleanup() throws InterruptedException {
        if (nextCleanup == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now >= nextCleanup.getTime()) {
            nextCleanup = null;
            try (Connection con = ConnectionManager.getConnection()) {
                Date threshold = new Date(now - CLEANUP_AGE.getMillis());
                int count = versionHistory.delete().where(versionHistory.eventDate.lt(threshold)).execute(con);
                if (count > 0) {
                    Log.info("Removed " + count + " version history records older than " + Spell.get(threshold));
                }
            } catch (SQLException e) {
                Log.exception(e);
            }
        }
    }

    private void refresh() throws InterruptedException {
        Connection con = null;
        try {
            con = ConnectionManager.getConnection();
            while (versionHistory.hasAfterId(con, maxVersionHistoryId)) {
                if (CLEANUP_ENABLED && nextCleanup == null) {
                    nextCleanup = new Date(System.currentTimeMillis() + CLEANUP_LATENCY.getMillis());
                }
                try (Browser br = versionHistory.browseAfterId(con, maxVersionHistoryId)) {
                    while (br.next()) {
                        if (maxVersionHistoryId == null || maxVersionHistoryId.longValue() < versionHistory.getId().longValue()) {
                            maxVersionHistoryId = versionHistory.getId();
                        }
                        CatalogAndTableName catalogAndTableName = new CatalogAndTableName(versionHistory.getDbName(), versionHistory.getTableName());
                        VersionIdChangesContainer cc = changes.getOrSpawn(catalogAndTableName);
                        cc.maxVersionId = maxVersionHistoryId;
                        switch (versionHistory.getEventType()) {
                        case CREATE:
                            cc.createdIds = appendId(cc.createdIds, versionHistory.getRecordId());
                            break;
                        case DELETE:
                            cc.deletedIds = appendId(cc.deletedIds, versionHistory.getRecordId());
                            break;
                        case UPDATE:
                            cc.updatedIds = appendId(cc.updatedIds, versionHistory.getRecordId());
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Log.exception(e);
        } finally {
            JDBCUtils.close(con);
        }

        listener.versionChanged(changes.values());
        changes.clear();
    }

    private static Set<Long> appendId(Set<Long> ids, Long id) {
        if (ids == null) {
            Set<Long> result = new TreeSet<>();
            result.add(id);
            return result;
        } else {
            ids.add(id);
            return ids;
        }
    }

}
