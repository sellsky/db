package tk.bolovsrol.db.orm.object;

import tk.bolovsrol.db.orm.RecordNotFoundException;
import tk.bolovsrol.db.orm.fields.BigDecimalDbField;
import tk.bolovsrol.db.orm.fields.ByteArrayDbField;
import tk.bolovsrol.db.orm.fields.DateDbField;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.fields.DurationDbField;
import tk.bolovsrol.db.orm.fields.EnumDbField;
import tk.bolovsrol.db.orm.fields.FlagDbField;
import tk.bolovsrol.db.orm.fields.IntegerDbField;
import tk.bolovsrol.db.orm.fields.JsonDbField;
import tk.bolovsrol.db.orm.fields.LongDbField;
import tk.bolovsrol.db.orm.fields.StringDbField;
import tk.bolovsrol.db.orm.fields.TimeDbField;
import tk.bolovsrol.db.orm.fields.TwofacedTimeDbField;
import tk.bolovsrol.db.orm.sql.statements.delete.Delete;
import tk.bolovsrol.db.orm.sql.statements.insert.Insert;
import tk.bolovsrol.db.orm.sql.statements.insert.RefInsert;
import tk.bolovsrol.db.orm.sql.statements.insert.RowlessInsertOrUpdate;
import tk.bolovsrol.db.orm.sql.statements.update.Update;
import tk.bolovsrol.utils.RefObject;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.LogDome;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Интерфейс для рефренс-объекта в БД. */
public interface RefDbDataObject extends RefObject, DbDataObject {

	/** @return поле, хранящее ключевое значение. */
	LongDbField idField();

	List<DbDataField<?, ?>> dataFields();

	/**
	 * Попытка облегчить себе жизнь, грязная генерилка шаблона команды создания таблицы для БД-объекта.
	 * <p>
	 * Шаблон он потому, что его надо прочитать и доработать руками: убрать NOT NULL, убрать лишние default,
	 * а недостающие дописать, для строк выбрать между text и varchar, для последнего ещё указать нужную длину,
	 * и, наконец, дописать нужные индексы.
	 * <p>
	 * Кстати об индексах. Имя индекса начинаем с буквы <code>i</code> для обычного или <code>u</code> для уникального индекса,
	 * затем, разделяя подчёркиваниями, пишем названия полей с удалёнными подчёркиваниями, например:
	 * <pre>
	 * UNIQUE KEY "u_serviceid_contentvariantid" ("service_id","content_variant_id"),
	 * KEY "i_operatorid_msisdn" ("operator_id","msisdn")
	 * </pre>
	 * Если имя индекса получается чересчур длинным, его допускается сокращать, выкидывая буковки из названия полей так,
	 * чтобы от взгляда на название индекса, всё же, можно было составить представление о его составе, например,
	 * <pre>
	 * UNIQUE KEY "u_dlvstatus_opid_srcphone_prio_actdate_outgoingsmsid" ("delivery_status","operator_id","source_phone","priority","activation_date","outgoing_sms_id")
	 * </pre>
	 *
	 * @return шаблон SQL-инструкции «CREATE TABLE ...» для MySQL
	 * @see tk.bolovsrol.db.orm.versioncache.TriggerUtils#generateSqlCreateTriggers(RefDbDataObject)
	 */
	@Override
	default String generateSqlCreateTableTemplate() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("-- fixme: NOT NULLs, DEFAULTs, TEXT/CHAR/VARCHAR, indices \n");
		sb.append("CREATE TABLE \"");
		sb.append(this.getSqlCatalogName());
		sb.append("\".\"");
		sb.append(this.getSqlTableName());
		sb.append("\" (\n");
		LongDbField idField = this.idField();
		sb.append("  \"").append(idField.getName()).append("\" int(10) NOT NULL auto_increment,\n");
		for (DbDataField field : this.fields()) {
			if (field == idField) {
				continue;
			}
			sb.append("  \"").append(field.getName()).append("\" ");
			String addDefault = null;
			if (field instanceof BigDecimalDbField) {
				sb.append("decimal (10,").append(((BigDecimalDbField) field).getDbScale()).append(')');
			} else if (field instanceof DateDbField) {
				sb.append("datetime");
			} else if (field instanceof DurationDbField) {
				sb.append("char(16)");
			} else if (field instanceof EnumDbField<?>) {
				sb.append("enum('");
				Enum<?>[] enumConstants = ((EnumDbField<?>) field).getComponentType().getEnumConstants();
				for (Enum<?> anEnum : enumConstants) {
					sb.append(anEnum.toString());
					sb.append("','");
				}
				sb.replace(sb.length() - 2, sb.length(), ")");
				addDefault = '\'' + enumConstants[0].toString() + '\'';
			} else if (field instanceof FlagDbField) {
				sb.append("enum('YES','NO')");
				addDefault = "'YES' 'NO'";
			} else if (field instanceof IntegerDbField || field instanceof LongDbField) {
				sb.append("int(10)");
            } else if (field instanceof StringDbField) {
                sb.append("text varchar(*)");
            } else if (field instanceof JsonDbField) {
                sb.append("text");
            } else if (field instanceof TimeDbField) {
                sb.append("time");
            } else if (field instanceof TwofacedTimeDbField) {
                sb.append("char(29)");
            } else if (field instanceof ByteArrayDbField) {
                sb.append("longblob");
            } else {
                throw new IllegalArgumentException("Unexpected field type " + Spell.get(field.getClass()) + " of field " + Spell.get(field.getName()));
            }
			sb.append(" NOT NULL");
			if (addDefault != null) {
				sb.append(" default ").append(addDefault);
			}
			sb.append(",\n");
		}
		sb.append("  PRIMARY KEY (\"").append(idField.getName()).append("\")\n);\n");
		return sb.toString();
	}

	/**
	 * Создаёт инсерт, вставляющий объект в БД и вычитывающий ключевое поле.
	 *
	 * @return инсерт
	 */
	default RefInsert insert() {
		return Insert.intoAndLoadId(this);
	}

	/**
	 * Вставляет объект в БД. В ключевое поле будет вычитан полученный из СУБД ид.
	 *
	 * @throws SQLException
	 */
	default void insert(Connection con) throws SQLException {
		Insert.intoAndLoadId(this).execute(con);
	}

	/** Вставляет объект в БД, получает сгенерированный Id и пишет соответствующую запись
	 * в журнал с состоянием после записи. */
	default void insert(Connection connection, LogDome log) throws SQLException {
		final String name = this.getClass().getSimpleName();
		final String info = (name != null ? ' ' + name + ": " : ": ") + this.toString();

		try { insert(connection); }
		catch (SQLException ex) { log.warning("Error on insert" + info); throw ex; }
		catch (   Exception ex) { log.warning("Can't insert"    + info); throw ex; }
		log.hint("Created" + info);
	}

	/**
	 * Обновляет объект по его ид.
	 * Проверяет, что ид не нул (иначе {@link IllegalArgumentException} и что он сам не изменён (иначе {@link IllegalStateException}).
	 * Проверяет, что есть изменившиеся колонки и обновляет их и возвращает true, либо, если изменившихся нет, возвращает false.
	 *
	 * @return true, если какие-то изменения обнаружены и сохранены, false иначе
	 * @throws SQLException
	 * @throws SuspiciousUpdatedRowCountException обновлена не 1 запись.
	 */
	default boolean update(Connection con) throws SQLException {
		if (idField().isValueNull()) {
			throw new IllegalArgumentException("Cannot do update, id field is null (" + Spell.get(idField().valueToLogString()) + ").");
		}
		if (idField().isValueChanged()) {
			throw new IllegalStateException("Cannot do update, id field changed (" + Spell.get(idField().valueToLogString()) + ").");
		}
		Update u = this.update().where(idField().eqSelf());
		for (DbDataField<?, ?> field : fields()) {
			if (field.isValueChanged()) { u.column(field); }
		}
		if (u.columns().isEmpty()) {
			return false;
		}
		int rows = u.execute(con);
		if (rows != 1) {
			throw new SuspiciousUpdatedRowCountException(this, this.idField(), rows);
		}
		return true;
	}

	/** Обновляет объект по его Id и пишет соответствующую запись в журнал с состоянием
	 * сохраняемой записи до и после изменения.
	 * @return true, если в объекте были сделаны какие-либо изменения, и, соответственно,
	 *         было выполнено обновление в БД */
	default boolean update(Connection connection, LogDome log) throws SQLException {
		final String name = this.getClass().getSimpleName();
		final String info = (name != null ? ' ' + name + ": " : ": ") + this.toString();

		boolean result;
		try { result = update(connection); }
		catch (SQLException ex) { log.warning("Error on update" + info); throw ex; }
		catch (   Exception ex) { log.warning("Can't update"    + info); throw ex; }

		log.hint((result ? "Updated" : "Not changed") + info);
		return result;
	}

	/**
	 * Сохраняет новую или обновляет существующую запись в зависимости от наполненности id.
	 * <p>
	 * Если id нул, то инсертит новую запись. Иначе апдейтит запись по этому ид.
	 *
	 * @return true, если вставлена новая запись, false, если обновлена существующая.
	 * @throws SQLException
	 * @see #insert(Connection)
	 * @see #update(Connection)
	 */
	default boolean insertOrUpdate(Connection con) throws SQLException {
		if (idField().isValueNull()) {
			insert(con);
			return true;
		} else {
			update(con);
			return false;
		}
	}

	/**
	 * Удаляет объект.
	 *
	 * @return true, если что-нибудь было удалено.
	 * @throws SQLException
	 */
	default boolean delete(Connection con) throws SQLException {
		if (idField().isValueNull()) {
			throw new IllegalArgumentException("Cannot do delete, keyfield " + Spell.get(idField()) + " is null.");
		}
		return new Delete(this).where(idField().eqSelf()).execute(con) > 0;
	}

	/**
	 * Загружает запись по значению первичного ключа.
	 *
	 * @param id ключ
	 * @return true, если загружено, иначе false
	 * @throws SQLException
	 */
	default boolean loadById(Connection con, Long id) throws SQLException {
		return select().where(idField().eq(id)).load(con);
	}

	/**
	 * Загружает запись по значению первичного ключа.
	 * Если загрузить не удалось, выкидывает исключение.
	 *
	 * @param id ключ
	 * @throws SQLException
	 * @throws RecordNotFoundException загрузить не удалось
	 */
	default void loadByIdOrDie(Connection con, Long id) throws SQLException, RecordNotFoundException {
		if (!loadById(con, id)) {
			throw new RecordNotFoundException("No " + getClass().getSimpleName() + " is found by " + idField().getName() + '=' + Spell.get(id));
		}
	}

	/**
	 * Создаёт и возвращает инсерт всех полей объекта, кроме id.
	 *
	 * @return новый инсерт.
	 */
	default RowlessInsertOrUpdate insertDataColumns() {
		return Insert.into(this).columns(dataFields());
	}
}
