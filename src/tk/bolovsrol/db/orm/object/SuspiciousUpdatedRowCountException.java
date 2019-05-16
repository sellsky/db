package tk.bolovsrol.db.orm.object;

import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.utils.Spell;

import java.sql.SQLException;

/**
 * {@link AbstractDbDataObject} выкидывает, если апдейт по ключевым полям
 * обновил не одну запись.
 */
public class SuspiciousUpdatedRowCountException extends SQLException {
    private static final long serialVersionUID = 1L;

    private final DbDataObject dbDataObject;
    private final int rows;

	public SuspiciousUpdatedRowCountException(RefDbDataObject dbDataObject, DbDataField keyfield, int rows) {
		super("Updated " + rows + " row(s), keyfield " + Spell.get(keyfield) + ", object " + Spell.get(dbDataObject.getClass().getSimpleName()) + ' ' + Spell.get(dbDataObject.toString()));
		this.rows = rows;
		this.dbDataObject = dbDataObject;
    }

	public int getRows() {
		return rows;
	}

    public DbDataObject getDBDataObject() {
        return dbDataObject;
    }
}
