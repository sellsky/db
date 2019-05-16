package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbJsonContainer;
import tk.bolovsrol.db.orm.sql.dbcolumns.LikeableDbColumn;
import tk.bolovsrol.utils.Json;

/** Поле, содержащее строку. */
public interface DbJsonField extends DbDataField<Json, DbJsonContainer>, DbJsonContainer, LikeableDbColumn<Json> {


}
