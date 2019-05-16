package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbStringContainer;
import tk.bolovsrol.db.orm.sql.dbcolumns.LikeableDbColumn;

/** Поле, содержащее строку. */
public interface DbStringField extends DbDataField<String, DbStringContainer>, DbStringContainer, LikeableDbColumn<String> {


}
