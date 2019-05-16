package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbDateContainer;
import tk.bolovsrol.db.orm.sql.conditions.ColumnInRange;
import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.dbcolumns.DateDbColumn;
import tk.bolovsrol.utils.time.DateRange;

import java.util.Date;

/** Поле, содержащее дату. */
public interface DbDateField extends DbDataField<Date, DbDateContainer>, DbDateContainer, DateDbColumn {

    default Condition inRange(DateRange range) {
        return new ColumnInRange(this, range);
    }

}
