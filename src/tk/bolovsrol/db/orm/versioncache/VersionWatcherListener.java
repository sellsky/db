package tk.bolovsrol.db.orm.versioncache;

import java.util.Collection;

/** Слушатель наблюдателя за версиями. */
interface VersionWatcherListener {
    /**
     * Уведомляет слушателя о произошедших изменениях.
     *
     * @param changes суть изменений
     */
    void versionChanged(Collection<VersionIdChangesContainer> changes);

}
