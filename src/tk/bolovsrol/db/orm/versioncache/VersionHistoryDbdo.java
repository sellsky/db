package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.db.orm.fields.DateDbField;
import tk.bolovsrol.db.orm.fields.EnumDbField;
import tk.bolovsrol.db.orm.fields.LongDbField;
import tk.bolovsrol.db.orm.fields.StringDbField;
import tk.bolovsrol.db.orm.object.AbstractRefDbDataObject;
import tk.bolovsrol.db.orm.sql.dbcolumns.FakeColumn;
import tk.bolovsrol.db.orm.sql.statements.Limit;
import tk.bolovsrol.db.orm.sql.statements.select.Browser;
import tk.bolovsrol.db.orm.sql.statements.select.Select;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

/** История версий. */
class VersionHistoryDbdo extends AbstractRefDbDataObject {
    public VersionHistoryDbdo() {
        super(VersionCacheConst.VERSION_HISTORY_SQL_CATALOG_NAME, VersionCacheConst.VERSION_HISTORY_SQL_TABLE_NAME);
    }

	// id field declaration moved to parent
	protected final DateDbField eventDate = new DateDbField(this, "event_date");
	protected final EnumDbField<EventType> eventType = new EnumDbField<>(this, "event_type", EventType.class);
    protected final StringDbField dbName = new StringDbField(this, "db_name");
    protected final StringDbField tableName = new StringDbField(this, "table_name");
    protected final LongDbField recordId = new LongDbField(this, "record_id");

    public enum EventType {
        CREATE, UPDATE, DELETE
    }

    public Long retrieveMaxId(Connection con) throws SQLException {
        Select s = Select.from(this);
        s.setAllowLogging(VersionCacheConst.LOG_SQL);
        return s.load(con, id.max());
    }

    public Browser browseAfterId(Connection con, Long id) throws SQLException {
        Select s = Select.from(this);
        s.setAllowLogging(VersionCacheConst.LOG_SQL);
        if (id != null) {
            s.where(this.id.gt(id));
        }
        return s.browse(con);
    }

    public boolean hasAfterId(Connection con, Long id) throws SQLException {
        Select s = Select.from(this);
        s.setAllowLogging(VersionCacheConst.LOG_SQL);
        s.column(FakeColumn.INSTANCE);
        if (id != null) {
            s.where(this.id.gt(id));
        }
        s.limit(Limit.LIMIT_1);
        return s.load(con);
    }

    public Date getEventDate() {
        return eventDate.getValue();
    }

    public EventType getEventType() {
        return eventType.getValue();
    }

    public String getDbName() {
        return dbName.getValue();
    }

    public String getTableName() {
        return tableName.getValue();
    }

    public Long getRecordId() {
        return recordId.getValue();
    }

}
