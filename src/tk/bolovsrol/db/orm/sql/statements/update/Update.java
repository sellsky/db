package tk.bolovsrol.db.orm.sql.statements.update;

import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.fields.LongDbField;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;
import tk.bolovsrol.db.orm.sql.dbcolumns.ValueDbColumn;
import tk.bolovsrol.db.orm.sql.statements.AbstractSqlStatement;
import tk.bolovsrol.db.orm.sql.statements.Tables;
import tk.bolovsrol.db.orm.sql.statements.Where;
import tk.bolovsrol.db.orm.sql.statements.select.Join;
import tk.bolovsrol.db.orm.sql.statements.select.joins.InnerJoin;
import tk.bolovsrol.db.orm.sql.updatecolumns.UpdateColumn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Обновление одной или нескольких таблиц. */
public class Update extends AbstractSqlStatement {

    private final Tables tables = new Tables();
    private final UpdateColumns<UpdateColumn<?>> columns = new UpdateColumns<>();
    private final Where where = new Where();

    // ---------- shortcut ---

    /**
     * Создаёт апдейт для таблицы, описываемой переданным {@link RefDbDataObject}.
     *
     * @param primaryTable таблица для апдейта
     */
    public Update(RefDbDataObject primaryTable) {
        tables.setPrimaryTable(primaryTable);
    }

    /**
     * Создаёт апдейт для таблицы, описываемой переданным {@link DbDataObject}.
     *
     * @param primaryTable таблица для апдейта
     */
    public Update(DbDataObject primaryTable) {
        tables.setPrimaryTable(primaryTable);
    }

    //-------- генератор

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName) throws DbException, SQLException {
        Map<DbDataObject, String> tableAliases = tables.getAliases();
        sb.append("UPDATE ");
        tables.writeSqlExpression(sb, databaseProductName, tableAliases);
        sb.append(" SET ");
        columns.writeSqlExpression(sb, databaseProductName, tableAliases);
        where.writeSqlExpression(sb, databaseProductName, tableAliases);
    }

    public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        pos = columns.putValues(ps, pos);
        pos = where.putValues(ps, pos);
        return pos;
    }

    @Override
    protected void appendSqlLogValues(List<String> values) {
        columns.appendSqlLogValues(values);
        where.appendSqlLogValues(values);
    }

    /**
     * Выполняет апдейт.
     * <p>
     * Если ни одной колонки не задано, выкинет DbException.
     * <p>
     * Если не задано ни одного условия, то есть два варианта.
     * Если таблица, для которой исполняется апдейт, {@link RefDbDataObject},
     * и ключевое поле не нул, то добавит в апдейт условие равенства этого поля
	 * своему содержимому {@link ValueDbColumn#eqSelf()}. Иначе выкинет DbException.
	 *
     * @return количество изменённых рядов
     * @throws SQLException
     * @throws DbException
     */
    public int execute(Connection con) throws SQLException, DbException {
        if (columns.isEmpty()) {
            throw new DbException("No Update Column set");
        }
        if (where.isEmpty()) {
			LongDbField keyField = tables.getPrimaryTableKeyField();
			if (keyField.getValue() == null) {
				throw new DbException("No Condition set, KeyField has no value.");
			} else {
				where(keyField.eqSelf());
			}
        }
        PreparedStatement ps = con.prepareStatement(generateSqlExpression(con));
        try {
            putValues(ps, 1);
            int rowCount = ps.executeUpdate();
            columns.committed();
            return rowCount;
        } finally {
            JDBCUtils.close(ps);
        }
    }

    // -------- всякие сеттеры
    // --------- Columns ---------------------

    /** @return обновляемые апдейтом колонки */
	public UpdateColumns columns() {
		return columns;
    }

    /**
     * Добавляет в апдейт обновляемую колонку.
     *
     * @param column
     * @return this
     */
    public Update column(UpdateColumn<?> column) {
        this.columns.add(column);
        return this;
    }

    /**
     * Добавляет в апдейт обновляемые колонки.
     *
     * @param columns
     * @return this
     */
    public Update columns(UpdateColumn<?>... columns) {
        this.columns.add(columns);
        return this;
    }

    /**
     * Добавляет в апдейт обновляемые колонки.
     *
     * @param columns
     * @return this
     */
    public Update columns(Collection<? extends UpdateColumn<?>> columns) {
        this.columns.add(columns);
        return this;
    }

    /**
     * Добавляет в апдейт поле, назначая ему указанное значение.
     * <p>
     * То же, что .update(field.with(value))
     *
     * @param field
     * @param value
     * @param <V>
     * @return
     */
    public <V> Update set(DbDataField<V, ?> field, V value) {
        column(field.with(value));
        return this;
    }

    /** Добавляет в update поле, назначая ему указанную колонку или функцию. */
    public <V> Update set(DbDataField<V, ?> field, DbColumn<V> column) {
        column(field.withColumn(column));
        return this;
    }

    // ----------- Tables --------------

    /** @return используемые в апдейте таблицы. */
    public Tables getTables() {
        return tables;
    }

    /**
     * Добавляет в апдейт указанный джойн.
     *
     * @param join
     * @return this
     */
    public Update join(Join join) {
        tables.addJoin(join);
        return this;
    }

    /**
     * Создаёт и добавляет указанный Inner Join с указанным условием
     *
     * @param table приджойниваемая таблица
     * @param condition условие приджойнивания
     * @return this
     * @see #innerJoin(RefDbDataObject, DbColumn)
     */
    public Update innerJoin(DbDataObject table, Condition condition) {
        tables.addJoin(new InnerJoin(table, condition));
        return this;
    }

    /**
     * добавляет в селект джойн переданной таблицы с условием
     * равенства ключевого поля таблицы с переданной внешней ид-колонкой.
     * <p>
     * «INNER JOIN table ON table.keyField = referenceIdField»
     *
     * @param table
     * @param externalIdColumn
     * @return this
     */
    public Update innerJoin(RefDbDataObject table, DbColumn<Long> externalIdColumn) {
		tables.addJoin(new InnerJoin(table, table.idField().eqColumn(externalIdColumn)));
		return this;
	}


    // ----------- Where (and) ---------------

    /** @return используемые в апдейте условия. */
    public Where getWhere() {
        return where;
    }

    /**
     * Добавляет в апдейт условие обновления.
     *
     * @param condition условие
     * @return this
     */
    public Update where(Condition condition) {
        this.where.add(condition);
        return this;
    }

    /**
     * Добавляет в апдейт условия обновления.
     *
     * @param conditions условие
     * @return this
     */
    @SuppressWarnings({"OverloadedVarargsMethod"})
    public Update where(Condition... conditions) {
        this.where.add(conditions);
        return this;
    }

    /**
     * Добавляет в апдейт условия обновления.
     *
     * @param conditions условие
     * @return this
     */
    public Update where(Collection<? extends Condition> conditions) {
        this.where.add(conditions);
        return this;
    }

    /**
     * Очищает условия обновления апдейта.
     *
     * @return this
     */
    public Update clearWhere() {
        this.where.clear();
        return this;
    }

}
