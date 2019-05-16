package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.utils.StringDumpBuilder;

import java.util.Set;

/**
 * Контейнер со списком изменившихся ид в таблице.
 */
class VersionIdChangesContainer {
    public final CatalogAndTableName catalogAndTableName;
    public Set<Long> createdIds;
    public Set<Long> updatedIds;
    public Set<Long> deletedIds;
    public Long maxVersionId;

    VersionIdChangesContainer(CatalogAndTableName catalogAndTableName) {
        this.catalogAndTableName = catalogAndTableName;
    }

    @Override public String toString() {
        return new StringDumpBuilder()
              .append("catalogAndTableName", catalogAndTableName)
              .append("createdIds", createdIds)
              .append("updatedIds", updatedIds)
              .append("deletedIds", deletedIds)
              .append("maxVersionId", maxVersionId)
              .toString();
    }
}
