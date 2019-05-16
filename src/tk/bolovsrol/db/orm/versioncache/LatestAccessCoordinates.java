package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.utils.StringDumpBuilder;

/** Контейнер с данными последнего доступа. */
class LatestAccessCoordinates implements Comparable<LatestAccessCoordinates> {
    private final CatalogAndTableName catalogAndTableName;
    private final Long objectId;

    LatestAccessCoordinates(CatalogAndTableName catalogAndTableName, Long objectId) {
        this.catalogAndTableName = catalogAndTableName;
        this.objectId = objectId;
    }

    public String getCatalogName() {
        return catalogAndTableName.getCatalogName();
    }

    public String getTableName() {
        return catalogAndTableName.getTableName();
    }

    public Long getObjectId() {
        return objectId;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof LatestAccessCoordinates && this.equals((LatestAccessCoordinates) o));
    }

    public boolean equals(LatestAccessCoordinates that) {
        return this.catalogAndTableName.equals(that.catalogAndTableName)
              && this.objectId.equals(that.objectId);
    }

    @Override
    public int hashCode() {
        int result;
        result = catalogAndTableName.hashCode();
        result = 31 * result + objectId.hashCode();
        return result;
    }

    @Override public String toString() {
        return new StringDumpBuilder()
              .append("catalogAndTableName", catalogAndTableName)
              .append("objectId", objectId)
              .toString();
    }

    @Override public int compareTo(LatestAccessCoordinates that) {
        int i;
        i = this.catalogAndTableName.compareTo(that.catalogAndTableName);
        if (i != 0) {
            return i;
        }
        return this.objectId.compareTo(that.objectId);
    }
}
