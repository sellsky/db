package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConsecutiveItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.DbColumnItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ValueItem;

/** Функция <code>IFNULL(первое значение если не null, второе значение если первое null)</code>
 */
public class IfNull<V> extends ListingDbColumn<V> {
	private IfNull(ConsecutiveItem first, ConsecutiveItem second) {
		super("IFNULL(", ",", ")");
		append(first);
		append(second);
	}

	public IfNull(DbDataField<V, ?> field, V value) {
		this(new DbColumnItem(field), new ValueItem(field.wrap(value))); }
}


