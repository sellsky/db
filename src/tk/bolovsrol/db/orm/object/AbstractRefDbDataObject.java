package tk.bolovsrol.db.orm.object;

import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.fields.LongDbField;

import java.util.ArrayList;
import java.util.List;

/**
 * Объект с полем «id» типа {@link LongDbField}, содержащим уникальный ключ записи в таблице.
 * Ожидается, что в таблице это поле объявлено как PRIMARY KEY.
 * <p>
 * В конструкторе можно указать название этого поля для древних объектов, у которых это поле в силу исторических причин было названо иначе.
 * У новых объектов ключ должен называться «id».
 */
public class AbstractRefDbDataObject extends AbstractDbDataObject implements RefDbDataObject {

	/** Ключевое поле. */
	public final LongDbField id;

	/** Поля кроме ключевого. */
    private volatile List<DbDataField<?, ?>> dataFields = null; // стоит ли это кешировать вообще?

	/**
	 * Создаёт объект для указанных каталога и таблицы в БД с указанным именем ключевого поля (если оно не «id»)..
	 *
	 * @param sqlCatalogName название каталога объекта в БД
	 * @param sqlTableName название таблицы объекта в БД
	 * @param idFieldName название ключевого поля
	 * @see #AbstractRefDbDataObject(String, String)
	 */
	protected AbstractRefDbDataObject(String sqlCatalogName, String sqlTableName, String idFieldName) {
		super(sqlCatalogName, sqlTableName);
		this.id = new LongDbField(this, idFieldName);
	}

	/**
	 * Создаёт объект для указанных каталога и таблицы в БД.
	 *
	 * @param sqlCatalogName название каталога объекта в БД
	 * @param sqlTableName название таблицы объекта в БД
	 */
	protected AbstractRefDbDataObject(String sqlCatalogName, String sqlTableName) {
        this(sqlCatalogName, sqlTableName, "id");
    }

	@Override public Long getId() {
		return id.getValue();
	}

	@Override public LongDbField idField() {
		return id;
	}

	@Override public List<DbDataField<?, ?>> dataFields() {
		if (dataFields == null) {
            List<DbDataField<?, ?>> dataFields = new ArrayList<>(dbFields.size() - 1);
            for (int i = 1; i < dbFields.size(); i++) { // ид у нас всегда первое поле, его смело пропускаем; а чтоб не городить саблист и не копировать дважды несколько элементов, перетащим их вручную
                dataFields.add(dbFields.get(i));
            }
            this.dataFields = dataFields;
        }
        return dataFields;
    }

}