package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.db.orm.fields.DateDbField;
import tk.bolovsrol.db.orm.fields.LongDbField;
import tk.bolovsrol.db.orm.fields.StringDbField;
import tk.bolovsrol.db.orm.object.AbstractRefDbDataObject;

import java.util.Date;

/** Фиксируем доступ к объектам */
class LatestAccessDbdo extends AbstractRefDbDataObject {

    public LatestAccessDbdo() {
        super(VersionCacheConst.LATEST_ACCESS_SQL_CATALOG_NAME, VersionCacheConst.LATEST_ACCESS_SQL_TABLE_NAME);
    }

	// id field declaration moved to parent
	protected final StringDbField objectName = new StringDbField(this, "object_name");
	protected final LongDbField objectId = new LongDbField(this, "object_id");
    protected final DateDbField accessDate = new DateDbField(this, "access_date");

    public void set(String objectName, Long objectId, Date accessDate) {
        this.objectName.setValue(objectName);
        this.objectId.setValue(objectId);
        this.accessDate.setValue(accessDate);
    }

}
