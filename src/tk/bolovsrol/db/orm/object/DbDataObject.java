package tk.bolovsrol.db.orm.object;

import tk.bolovsrol.db.orm.fields.BigDecimalDbField;
import tk.bolovsrol.db.orm.fields.ByteArrayDbField;
import tk.bolovsrol.db.orm.fields.DateDbField;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.fields.DurationDbField;
import tk.bolovsrol.db.orm.fields.EnumDbField;
import tk.bolovsrol.db.orm.fields.FlagDbField;
import tk.bolovsrol.db.orm.fields.IntegerDbField;
import tk.bolovsrol.db.orm.fields.LongDbField;
import tk.bolovsrol.db.orm.fields.StringDbField;
import tk.bolovsrol.db.orm.fields.TimeDbField;
import tk.bolovsrol.db.orm.fields.TwofacedTimeDbField;
import tk.bolovsrol.db.orm.sql.WritingSqlExpression;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;
import tk.bolovsrol.db.orm.sql.statements.delete.Delete;
import tk.bolovsrol.db.orm.sql.statements.insert.Insert;
import tk.bolovsrol.db.orm.sql.statements.insert.RowlessInsertOrUpdate;
import tk.bolovsrol.db.orm.sql.statements.select.Select;
import tk.bolovsrol.db.orm.sql.statements.update.Update;
import tk.bolovsrol.db.orm.sql.updatecolumns.UpdateColumn;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.box.CBox;
import tk.bolovsrol.utils.containers.ObjectCopyException;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Объект в базе данных.
 * <p>
 * Удивительным образом сочетает два свойства,
 * представляет собой как таблицу, так и запись в этой таблице.
 */
public interface DbDataObject extends WritingSqlExpression {
    /**
     * Регистрирует поле объекта. Следует использовать при создании объекта.
     *
     * @param dataField регистрируемое поле
     */
    void registerField(DbDataField<?, ?> dataField);

    /**
     * Возвращает БД-поля объекта в виде списка в порядке их регистрации.
     * <p>
     * Тут могут вернуть список, которым пользуется БД-объект,
     * так что не надо его менять без веских причин.
     *
     * @return список всех БД-полей объекта в порядке их регистрации
     */
    List<DbDataField<?, ?>> fields();

    /** @return название таблицы в базе данных */
    String getSqlTableName();

    /** @return название каталога (схемы) в базе данных, в котором находится таблица */
    String getSqlCatalogName();

    /** @return название каталога (схемы) и таблицы в базе данных, в котором находится таблица, в SQL-синтаксисе (в кавычках через точку) */
    String getSqlCatalogAndTableName();

    /** @return название каталога (схемы) и таблицы в базе данных, в котором находится таблица, для лога (просто через точку) */
    String getLogCatalogAndTableName();

    /** @return true, у объекта есть хотя бы одно поле, значение которого изменено с предыдущей загрузки, но не сохранено в БД, иначе false */
    default boolean hasChangedFields() {
        for (DbDataField field : fields()) {
            if (field.isValueChanged()) {
                return true;
            }
        }
        return false;
    }

    /** @return список полей, значение которых изменено, но не закоммичено */
    default List<DbDataField<?, ?>> getChangedFields() {
        return CBox.with(fields()).select(DbDataField::isValueChanged).toList();
    }

    /** @return количество полей, значение которых изменено, но не закоммичено */
    default int countChangedFields() {
        int count = 0;
        for (DbDataField<?, ?> field : fields()) {
            if (field.isValueChanged()) { count++; }
        }
        return count;
    }

    /** Очищает все поля объекта: вызывает каждому полю {@link DbDataField#dropValue()}.. */
    default void clear() {
        for (DbDataField<?, ?> field : fields()) {
            field.dropValue();
        }
    }

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
    default String generateSqlCreateTableTemplate() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("-- fixme: NOT NULLs, DEFAULTs, TEXT/CHAR/VARCHAR, indices \n");
        sb.append("CREATE TABLE \"");
        sb.append(this.getSqlCatalogName());
        sb.append("\".\"");
        sb.append(this.getSqlTableName());
        sb.append("\" (\n");
        for (DbDataField field : this.fields()) {
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
        sb.delete(sb.length() - 2, sb.length());
        sb.append("\n);\n");
        return sb.toString();
    }

    /**
     * Копирует содержимое полей текущего БД-объекта в поля переданного БД-объекта.
     * Возвращает этот переданный объект, что позволяет создавать копии в одну строчку. Например:
     * <pre>
     * Queue&lt;FooDbdo&gt; queue = ...;
     * Connection con = ...;
     * FooDbdo templateFooDbdo = new FooDbdo();
     * try (Browser br = DbdoUtils.browse(con, templateFooDbdo, ...) {
     *     while(br.next) {
     *         queue.put(<b>templateFooDbdo.copy(new FooDbdo())</b>);
     *     }
     * }
     * </pre>
     * Объекты одного класса копируются быстро и целиком.
     * <p>
     * Между объектами различных классов копируются значения одноимённых полей, если поля совместимы.
     * Если поля несовместимы, вываливается ObjectCopyException. Поля, не имеющие пары, остаются нетронутыми.
     *
     * @param target БД-объект, в который нужно скопировать значения.
     * @return target переданный объект
     * @throws ObjectCopyException попытка скопировать несовместимые поля
     */
    @SuppressWarnings("unchecked") default <T extends DbDataObject> T copyTo(T target) throws ObjectCopyException {
        if (this.getClass() == target.getClass()) {
            Iterator<DbDataField<?, ?>> sourceIt = fields().iterator();
            for (DbDataField targetField : target.fields()) {
                try {
                    targetField.copyValueFrom(sourceIt.next());
                } catch (Exception e) {
                    throw new ObjectCopyException(e);
                }
            }
        } else {
            // полей обычно немного, быстрее всего будет искать их перебором
            List<DbDataField<?, ?>> sourceFields = fields();
            DbDataField[] sourceArr = sourceFields.toArray(new DbDataField[sourceFields.size()]);
            for (DbDataField targetField : target.fields()) {
                for (int i = 0; i < sourceArr.length; i++) {
                    DbDataField sourceField = sourceArr[i];
                    if (sourceField != null && sourceField.getName().equals(targetField.getName())) {
                        // Чтобы исключить лишние сравнения, ячейку с уже использованным полем затрём нуллом
                        sourceArr[i] = null;
                        try {
                            targetField.copyValueFrom(sourceField);
                        } catch (Exception e) {
                            throw new ObjectCopyException(e);
                        }
                    }
                }
            }
        }
        return target;
    }

    /**
     * @return представление объекта в формате джсона
     * @see #parse(Json)
     */
    default Json toJson() {
        Json resultJ = new Json();
        for (DbDataField<?, ?> field : fields()) {
            field.putValue(resultJ.newObjectItemFlat(field.getName()));
        }
        return resultJ;
    }

    /**
     * Распознаёт объект, ранее экспортированный методом {@link #toJson()}.
     *
     * @param exportedJ
     * @throws ValueParsingException
     */
    default void parse(Json exportedJ) throws ValueParsingException {
        for (DbDataField<?, ?> field : fields()) {
            try {
                field.parseValue(exportedJ.getObjectItem(field.getName()));
            } catch (ValueParsingException e) {
                throw new ValueParsingException("Error reading value into field " + Spell.get(field), e);
            }
        }
    }

    /**
     * Создаёт и возвращает пустой селект для объекта.
     *
     * @return новый пустой селект.
     */
    default Select select() {
        return Select.from(this);
    }

    /**
     * Создаёт и возвращает селект всех колонок объекта.
     *
     * @return новый селект.
     */
    default Select selectAllColumns() {
        return Select.from(this).allColumns();
    }

    /**
     * Создаёт и возвращает селект указанной колонки объекта.
     *
     * @return новый селект.
     */
    default Select select(DbColumn column) {
        return Select.from(this).columns(column);
    }

    /**
     * Создаёт и возвращает селект указанных колонок объекта.
     *
     * @return новый селект.
     */
    default Select select(DbColumn... columns) {
        return Select.from(this).columns(columns);
    }

    /**
     * Создаёт и возвращает селект указанных колонок объекта.
     *
     * @return новый селект.
     */
    default Select select(Collection<? extends DbColumn> columns) {
        return Select.from(this).columns(columns);
    }

    /**
     * Создаёт и возвращает инсерт всех полей объекта.
     *
     * @return новый инсерт.
     */
    default RowlessInsertOrUpdate insertAllColumns() {
        return Insert.into(this).allColumns();
    }

    /**
     * Создаёт и возвращает инсерт указанного поля объекта.
     *
     * @return новый инсерт.
     */
    default RowlessInsertOrUpdate insert(DbDataField<?, ?> field) {
        return Insert.into(this).column(field);
    }

    /**
     * Создаёт и возвращает инсерт указанных полей объекта.
     *
     * @return новый инсерт.
     */
    default RowlessInsertOrUpdate insert(DbDataField<?, ?>... fields) {
        return Insert.into(this).columns(fields);
    }

    /**
     * Создаёт и возвращает инсерт указанных полей объекта.
     *
     * @return новый инсерт.
     */
    default RowlessInsertOrUpdate insert(Collection<? extends DbDataField<?, ?>> fields) {
        return Insert.into(this).columns(fields);
    }

    /**
     * Создаёт и возвращает пустой апдейт для объекта.
     *
     * @return новый пустой апдейт.
     */
    default Update update() {
        return new Update(this);
    }

    /**
     * Создаёт и возвращает апдейт указанной колонки объекта.
     *
     * @return новый апдейт.
     */
    default Update update(UpdateColumn column) {
        return new Update(this).column(column);
    }

    /**
     * Создаёт и возвращает апдейт указанных колонок объекта.
     *
     * @return новый апдейт.
     */
    default Update update(UpdateColumn... columns) {
        return new Update(this).columns(columns);
    }

    /**
     * Создаёт и возвращает апдейт указанных колонок объекта.
     *
     * @return новый апдейт.
     */
    default Update update(Collection<? extends UpdateColumn<?>> columns) {
        return new Update(this).columns(columns);
    }

    /**
     * Создаёт и возвращает пустой делит для объекта.
     *
     * @return новый пустой делит.
     */
    default Delete delete() {
        return new Delete(this);
    }

}
