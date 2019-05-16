package tk.bolovsrol.db.orm.sql.statements.insert;

import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;
import tk.bolovsrol.db.orm.sql.statements.AbstractSqlStatement;
import tk.bolovsrol.db.orm.sql.statements.Limit;
import tk.bolovsrol.db.orm.sql.statements.select.GroupByEntity;
import tk.bolovsrol.db.orm.sql.statements.select.Join;
import tk.bolovsrol.db.orm.sql.statements.select.Select;
import tk.bolovsrol.db.orm.sql.statements.select.orderby.OrderByEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * INSERT INTO ... SELECT blah blah blah.
 * <p>
 * Отличительная особенность: колонки надо добавлять по одной в виде пар: колонка для инсерта + колонка для селекта.
 * Остальные методы делегируются селекту.
 */
public class InsertSelect extends AbstractSqlStatement {

    private DbDataObject insertTable;
    private final List<DbDataField<?, ?>> columns = new ArrayList<>();
    private final Select select;

    public InsertSelect(DbDataObject singleTable) {
        this(singleTable, singleTable);
    }

    public InsertSelect(DbDataObject insertTable, DbDataObject selectTable) {
        this.insertTable = insertTable;
        this.select = Select.from(selectTable);
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName) throws DbException, SQLException {
        // insert into TABLE (col1,col2,col3) select ...
        sb.append("INSERT INTO ");
        insertTable.writeSqlExpression(sb, databaseProductName, null);
        sb.append('(');
        for (DbDataField<?, ?> column : columns) {
            column.writeSqlExpression(sb, databaseProductName, null);
            sb.append(',');
        }
        sb.setCharAt(sb.length() - 1, ')');
        sb.append(' ');
        select.writeSqlExpression(sb, databaseProductName);
    }

    public int putValues(PreparedStatement ps, int pos) throws SQLException {
        return select.putValues(ps, pos);
    }

    @Override
    public void appendSqlLogValues(List<String> values) {
        select.appendSqlLogValues(values);
    }

    public int execute(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            generateSqlExpression(con),
            Statement.NO_GENERATED_KEYS
        );
        try {
            putValues(ps, 1);
            return ps.executeUpdate();
        } finally {
            JDBCUtils.close(ps);
        }
    }

    /**
     * Добавляет в оператор пару колонок для инсерта и селекта.
     *
     * @param insertColumn колонка для инсерта
     * @param selectColumn колонка для селекта
     * @return this
     */
    public InsertSelect columnWith(DbDataField<?, ?> insertColumn, DbColumn selectColumn) {
        columns.add(insertColumn);
        select.column(selectColumn);
        return this;
    }

    /**
     * Добавляет в оператор пару колонок для инсерта и селекта.
     *
     * @param insertColumn колонка для инсерта
     * @param value значение для селекта, может быть нулом
     * @return this
     */
    public <V> InsertSelect columnWith(DbDataField<V, ?> insertColumn, V value) {
        columns.add(insertColumn);
        select.column(DbColumn.custom().val(insertColumn.wrap(value)));
        return this;
    }

    /**
     * Добавляет в оператор колонку для записи в неё населекченного значения.
     *
     * @param column
     * @return
     */
    public InsertSelect column(DbDataField<?, ?> column) {
        this.columns.add(column);
        this.select.column(column);
        return this;
    }

    /**
     * Добавляет в оператор набор колонок для записи в них населекченного значения.
     *
     * @param columns
     * @return
     */
    public InsertSelect columns(Collection<DbDataField<?, ?>> columns) {
        this.columns.addAll(columns);
        this.select.columns(columns);
        return this;
    }

    // -------- дальше делегаты селекта
    public InsertSelect setDistinct(boolean distinct) {
        select.distinct(distinct);
        return this;
    }

    public InsertSelect join(Join join) {
        select.join(join);
        return this;
    }

    public InsertSelect innerJoin(DbDataObject table, Condition condition) {
        select.innerJoin(table, condition);
        return this;
    }

    public InsertSelect leftJoin(DbDataObject table, Condition condition) {
        select.leftJoin(table, condition);
        return this;
    }

    public InsertSelect innerJoin(RefDbDataObject table, DbColumn<Long> externalIdColumn) {
        select.innerJoin(table, externalIdColumn);
        return this;
    }

    public InsertSelect leftJoin(RefDbDataObject table, DbColumn<Long> externalIdColumn) {
        select.leftJoin(table, externalIdColumn);
        return this;
    }

    public InsertSelect where(Condition condition) {
        select.where(condition);
        return this;
    }

    public InsertSelect where(Condition... conditions) {
        select.where(conditions);
        return this;
    }

    public InsertSelect where(Collection<? extends Condition> conditions) {
        select.where(conditions);
        return this;
    }

    public InsertSelect orderBy(OrderByEntity... entities) {
        select.orderBy(entities);
        return this;
    }

    public InsertSelect orderBy(OrderByEntity entity) {
        select.orderBy(entity);
        return this;
    }

    public InsertSelect orderBy(Collection<? extends OrderByEntity> entities) {
        select.orderBy(entities);
        return this;
    }

    public InsertSelect having(Condition condition) {
        select.having(condition);
        return this;
    }

    public InsertSelect groupBy(GroupByEntity entity) {
        select.groupBy(entity);
        return this;
    }

    public InsertSelect groupBy(GroupByEntity... entities) {
        select.groupBy(entities);
        return this;
    }

    public InsertSelect groupBy(Collection<? extends GroupByEntity> entities) {
        select.groupBy(entities);
        return this;
    }

    public InsertSelect limit(Limit limit) {
        select.limit(limit);
        return this;
    }
}
