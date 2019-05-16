package tk.bolovsrol.db.orm.sql.statements.select;

import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.dbcolumns.CountAll;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;
import tk.bolovsrol.db.orm.sql.dbcolumns.ValueDbColumn;
import tk.bolovsrol.db.orm.sql.statements.AbstractSqlStatement;
import tk.bolovsrol.db.orm.sql.statements.Limit;
import tk.bolovsrol.db.orm.sql.statements.Tables;
import tk.bolovsrol.db.orm.sql.statements.Where;
import tk.bolovsrol.db.orm.sql.statements.select.joins.InnerJoin;
import tk.bolovsrol.db.orm.sql.statements.select.joins.LeftJoin;
import tk.bolovsrol.db.orm.sql.statements.select.orderby.Direction;
import tk.bolovsrol.db.orm.sql.statements.select.orderby.OrderBy;
import tk.bolovsrol.db.orm.sql.statements.select.orderby.OrderByColumn;
import tk.bolovsrol.db.orm.sql.statements.select.orderby.OrderByCondition;
import tk.bolovsrol.db.orm.sql.statements.select.orderby.OrderByEntity;
import tk.bolovsrol.db.orm.sql.statements.select.orderby.OrderByNumber;
import tk.bolovsrol.db.orm.sql.statements.select.orderby.RandomOrder;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogLevel;
import tk.bolovsrol.utils.properties.Cfg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Объектное представление SQL-ного селекта.
 * С параметрами.
 * <p>
 * Селект состоит из следующих вещей:
 * <ul>
 * <li>префикса режима;</li>
 * <li>списка вычитываемых колонок;</li>
 * <li>списка исследуемых таблиц;</li>
 * <li>списка джойнов;</li>
 * <li>выражения where;</li>
 * <li>выражения group by;</li>
 * <li>выражения having;</li>
 * <li>выражения order by;</li>
 * <li>выражения limit.</li>
 * </ul>
 * Нужно вызывать соответствующие методы, устанавливая нужные условия, как билдеру,
 * а затем выполнить подготовленный селект одним из методов load или получить браузер
 * методом {@link #browse(Connection)}.
 * <p>
 * В простейшем случае как-то так:
 * <pre>
 * DbDataObject o;
 * o.select().where(o.id.eq(123L)).load(con);
 * </pre>
 * Ради наглядности сеттеры этого объекта называются сходно с ключевыми словами SQL, без глаголов.
 *
 * @see #browse(Connection)
 * @see #load(Connection)
 * @see #load(Connection, ValueDbColumn)
 * @see #countAll(Connection)
 */
public class Select extends AbstractSqlStatement {

    private static final long WARN_SELECT_LONGER_MS = Cfg.getLong("log.sql.select.longer.ms", 0L, Log.getInstance());
    private static final int DEFAULT_FETCH_SIZE = Cfg.getInteger("sql.select.defaultFetchSize", 0, Log.getInstance());

    private boolean distinct = false;
    private final DbColumns columns = new DbColumns();
    private final Tables tables = new Tables();
    // ленивая инициализация для необязательных полей
    private Where where = null;
    private Having having = null;
    private OrderBy orderBy = null;
    private GroupBy groupBy = null;
    private Limit limit = null;
    private ReadLocking readLocking = null;

    private int fetchSize = DEFAULT_FETCH_SIZE;

    /**
     * Создаёт селект из указанной таблицы.
     */
    protected Select(DbDataObject primaryTable) {
        this.tables.setPrimaryTable(primaryTable);
    }

    /**
     * Создаёт селект из указанной таблицы.
     * <p>
     * Вероятно, удобней будет вызвать один из методов select() непосредственно у таблицы,
     * в которых можно сразу задать перечень колонок.
     * <p>
     * Если на момент загрузки ни одна колонка явно не задана,
     * будут загружены все колонки всех используемых в селекте таблиц.
     *
     * @param primaryTable основная таблица для селекта
     * @see DbDataObject#select()
     * @see DbDataObject#select(Collection)
     * @see DbDataObject#select(DbColumn)
     * @see DbDataObject#select(DbColumn[])
     */
    public static Select from(DbDataObject primaryTable) {return new Select(primaryTable);}

    //-------- генератор

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName) throws DbException, SQLException {
        Map<DbDataObject, String> tableAliases = tables.getAliases();
        sb.append("SELECT");
        if (distinct) {
            sb.append(" DISTINCT");
        }
        columns.writeSqlExpression(sb, databaseProductName, tableAliases);
        sb.append(" FROM ");
        tables.writeSqlExpression(sb, databaseProductName, tableAliases);
        if (where != null) {
            where.writeSqlExpression(sb, databaseProductName, tableAliases);
        }
        if (groupBy != null) {
            groupBy.writeSqlExpression(sb, databaseProductName, tableAliases);
        }
        if (having != null) {
            having.writeSqlExpression(sb, databaseProductName, tableAliases);
        }
        if (orderBy != null) {
            orderBy.writeSqlExpression(sb, databaseProductName, tableAliases);
        }
        if (limit != null) {
            limit.writeSqlExpression(sb, databaseProductName, tableAliases);
        }
        if (readLocking != null) {
            readLocking.writeSqlExpression(sb, databaseProductName, tableAliases);
        }
    }

    @Override
    public void appendSqlLogValues(List<String> values) {
        columns.appendSqlLogValues(values);
        tables.appendValues(values);
        if (where != null) {
            where.appendSqlLogValues(values);
        }
        if (having != null) {
            having.appendSqlLogValues(values);
        }
        if (orderBy != null) {
            orderBy.appendSqlLogValues(values);
        }
        if (limit != null) {
            limit.appendSqlLogValues(values);
        }
    }

    public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        pos = columns.putValues(ps, pos);
        pos = tables.putValues(ps, pos);
        if (where != null) {
            pos = where.putValues(ps, pos);
        }
        if (having != null) {
            pos = having.putValues(ps, pos);
        }
        if (orderBy != null) {
            pos = orderBy.putValues(ps, pos);
        }
        if (limit != null) {
            pos = limit.putValues(ps, pos);
        }
        return pos;
    }

    public int pickValues(ResultSet rs, int pos) throws SQLException, PickFailedException, DbException {
        try {
            return columns.pickValues(rs, pos);
        } catch (PickFailedException e) {
            throw new DbException("Error while loading values for " + Spell.get(tables.getPrimaryTable().getSqlTableName()), e);
        }
    }

    /**
     * Добавляет в запрос колонку count(*), ставит LIMIT 1, загружает первый ряд резалтсета и закрывает резалтсет.
     *
     * @return вернувшийся результат или 0, если загрузка не произошла
     * @throws SQLException
     * @see #load(Connection, ValueDbColumn)
     */
    public Integer countAll(Connection con) throws SQLException {
        return load(con, new CountAll());
    }

    /**
     * Добавляет в запрос переданную колонку, ставит LIMIT 1, загружает первый ряд резалтсета и закрывает резалтсет.
     *
     * @return загруженный колонкой результат или, если загрузки не произошло, значение, хранимое колонкой при вызове метода
     * @throws SQLException
     */
    public <V> V load(Connection con, ValueDbColumn<V> singleColumn) throws SQLException, PickFailedException, DbException {
        column(singleColumn);
        load(con);
        return singleColumn.getValue();
    }

    /**
     * Добавляет в запрос переданную колонку, ставит LIMIT 1, загружает первый ряд резалтсета и закрывает резалтсет.
     *
     * @return загруженный колонкой результат или defaultValue, если загрузки не произошло
     * @throws SQLException
     */
    public <V> V load(Connection con, ValueDbColumn<V> singleColumn, V defaultValue) throws SQLException, PickFailedException, DbException {
        column(singleColumn);
        return load(con) ? singleColumn.getValue() : defaultValue;
    }

    /**
     * Ставит запросу LIMIT 1, загружает первый ряд резалтсета и закрывает резалтсет.
     * <p>
     * Если в запросе не заданы колонки, добавляет колонки всех привязанных к запросу таблиц (основной и джойнов).
     *
     * @return true, если загрузка произошла, false, если загрузка не произошла (резалтсет пуст)
     * @throws SQLException
     */
    public boolean load(Connection con) throws SQLException, PickFailedException, DbException {
        if (this.columns.isEmpty()) {
            allColumns();
        }
        this.limit = Limit.LIMIT_1;
        try (Browser browser = browse(con)) {
            return browser.next();
        }
    }

    /**
     * Добавляет переданную колонку к колонкам селекта.
     * Браузит резалтсет и добавляет в переданную коллекцию населекченные значения.
     * <p>
     * Возвращает переданную коллекцию, чтобы можно было делать что-то вроде
     * <pre>
     * Set&lt;Long&gt; ids = select()....collect(con, someidField, new HashSet());
     * </pre>
     *
     * @param con соединение с БД
     * @param valueColumn
     * @param targetCollection
     * @param <V> тип значения
     * @param <C> тип коллекции
     * @return переданная коллекция
     * @throws SQLException
     * @throws DbException
     */
    public <V, C extends Collection<? super V>> C collect(Connection con, ValueDbColumn<? extends V> valueColumn, C targetCollection) throws SQLException, DbException {
        this.column(valueColumn);
        try (Browser br = this.browse(con)) {
            while (br.next()) {
                targetCollection.add(valueColumn.getValue());
            }
        }
        return targetCollection;
    }

    /**
     * Добавляет переданные колонки к колонкам селекта.
     * Браузит резалтсет и добавляет в переданную карту населекченные значения.
     * <p>
     * Возвращает переданную коллекцию, чтобы можно было делать что-то вроде
     * <pre>
     * Map&lt;Long, String&gt; idToValue = select()....collect(con, someidField, someStringField, new LinkedHashMap());
     * </pre>
     *
     * @param con соединение с БД
     * @param keyColumn колонка, содержащая ключи
     * @param valueColumn колонка, содержащая значения
     * @param targetMap куда класть населекченное
     * @param <K> тип ключа
     * @param <V> тип значения
     * @param <C> тип карты
     * @return переданная карта
     * @throws SQLException
     * @throws DbException
     */
    public <K, V, C extends Map<? super K, ? super V>> C collect(Connection con, ValueDbColumn<K> keyColumn, ValueDbColumn<V> valueColumn, C targetMap) throws SQLException, DbException {
        this.column(keyColumn);
        this.column(valueColumn);
        try (Browser br = this.browse(con)) {
            while (br.next()) {
                targetMap.put(keyColumn.getValue(), valueColumn.getValue());
            }
        }
        return targetMap;
    }

    /**
     * Для каждого ряда, вычитанного полученным селектом, вызывает {@link BrowseConsumer#rowLoaded()},
     * рассчитывая на обработку загруженного.
     * <p>
     * Если в запросе не заданы колонки, добавляет колонки всех привязанных к запросу таблиц (основной и джойнов).
     *
     * @return true, если загружен хотя бы один ряд, false, если резалтсет оказался пуст
     * @throws SQLException
     */
    public <E extends Exception> boolean browse(Connection con, BrowseConsumer<E> consumer) throws SQLException, DbException, E {
        try (Browser br = this.browse(con)) {
            if (!br.next()) {
                return false;
            } else {
                do {
                    consumer.rowLoaded();
                } while (br.next());
                return true;
            }
        }
    }

    /**
     * Добавляет переданную колонку к колонкам селекта.
     * Браузит резалтсет и вызывает переданный коллектор для каждого загруженного значения. Чтобы делать как-то так:
     * <pre>
     * category.select().browse(con, category.tariffCode, code -> builder.addTariffCategory(code));
     * </pre>
     *
     * @param con
     * @param valueColumn
     * @param collector
     * @param <V>
     * @throws SQLException
     * @throws DbException
     */
    public <V, E extends Exception> void browse(Connection con, ValueDbColumn<? extends V> valueColumn, BrowseCollector<V, E> collector) throws SQLException, DbException, E {
        this.column(valueColumn);
        try (Browser br = this.browse(con)) {
            while (br.next()) {
                collector.collect(valueColumn.getValue());
            }
        }
    }

    /**
     * Добавляет колонки переданного объекта к колонкам селекта.
     * Браузит резалтсет и вызывает переданный коллектор для каждого загруженного состояния объекта. Чтобы делать как-то так:
     * <pre>
     * category.select().browse(con, category, builder::addCategory);
     * </pre>
     * Следует обратить внимание, что каждый раз коллектору передаётся один и тот же, указанный в параметрах метода БД-объект.
     *
     * @param con
     * @param dbDataObject
     * @param collector
     * @param <V>
     * @throws SQLException
     * @throws DbException
     */
    public <V extends DbDataObject, E extends Exception> void browse(Connection con, V dbDataObject, BrowseCollector<V, E> collector) throws SQLException, DbException, E {
        this.columns(dbDataObject);
        try (Browser br = this.browse(con)) {
            while (br.next()) {
                collector.collect(dbDataObject);
            }
        }
    }

    /**
     * Добавляет переданные колонки к колонкам селекта.
     * Браузит резалтсет и вызывает переданный коллектор для каждой загруженной пары значений. Чтобы делать как-то так:
     * <pre>
     * category.select().browse(con, category.id, category.tariffCode, (id, code) -> builder.processCategory(id, code));
     * </pre>
     *
     * @param con
     * @param valueColumnA
     * @param valueColumnB
     * @param collector
     * @param <A>
     * @param <B>
     * @throws SQLException
     * @throws DbException
     */
    public <A, B, E extends Exception> void browse(Connection con, ValueDbColumn<? extends A> valueColumnA, ValueDbColumn<? extends B> valueColumnB, BrowseBiCollector<A, B, E> collector) throws SQLException, DbException, E {
        this.column(valueColumnA);
        this.column(valueColumnB);
        try (Browser br = this.browse(con)) {
            while (br.next()) {
                collector.collect(valueColumnA.getValue(), valueColumnB.getValue());
            }
        }
    }

    /**
     * Возвращает браузер, управляющий обновлением участвующих в селекте колонок.
     * <p>
     * Если в запросе не заданы колонки, добавляет колонки всех привязанных к запросу таблиц (основной и джойнов).
     *
     * @return true, если загрузка произошла, false, если загрузка не произошла (резалтсет пуст)
     * @throws SQLException
     */
    @SuppressWarnings("ReturnOfInnerClass") public Browser browse(Connection con) throws SQLException, DbException {
        if (this.columns.isEmpty()) {
            allColumns();
        }
        return new SelectBrowser(con);
    }

    private final class SelectBrowser implements Browser {
        private final PreparedStatement ps;
        private final ResultSet rs;
        private final String sql;
        private final Connection con;

        @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed") private SelectBrowser(Connection con) throws SQLException, DbException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                this.con = con;
                sql = generateSqlExpression(this.con);
//                ps = this.con.prepareStatement(sql);
                ps = this.con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                ps.setFetchSize(fetchSize);
                Select.this.putValues(ps, 1);
                if (WARN_SELECT_LONGER_MS == 0L) {
                    rs = ps.executeQuery();
                } else {
                    long start = System.currentTimeMillis();
                    rs = ps.executeQuery();
                    long stop = System.currentTimeMillis();
                    long duration = stop - start;
                    if (duration >= WARN_SELECT_LONGER_MS) {
                        Log.write(LogLevel.WARNING, true, new SlowSelectException(duration, fillInPlaceholders(sql)));
                    }
                }
            } catch (SQLException | DbException e) {
                JDBCUtils.close(rs);
                JDBCUtils.close(ps);
                throw e;
            }
            this.ps = ps;
            this.rs = rs;
        }

        @Override public boolean next() throws SQLException, DbException {
            boolean gotNext = rs.next();
            if (gotNext) {
                try {
                    Select.this.pickValues(rs, 1);
                } catch (PickFailedException e) {
                    throw new PickFailedException("Error while reading out data for SQL " + fillInPlaceholders(sql), e);
                } catch (DbException e) {
                    throw new DbException("Error while executing SQL " + fillInPlaceholders(sql), e);
                }
            } else {
                close();
            }
            return gotNext;
        }

        @Override public void close() {
            JDBCUtils.close(rs);
            JDBCUtils.close(ps);
        }
    }

    public static class MaskedSQLException extends RuntimeException {
        private final SQLException e;

        public MaskedSQLException(SQLException e) {
            this.e = e;
        }

        public SQLException getSQLException() {
            return e;
        }
    }

    // -------- всякие сеттеры

    public Select allowLogging(boolean allow) {
        setAllowLogging(allow);
        return this;
    }

    public int getFetchSize() {return fetchSize; }

    public Select withFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
        return this;
    }

    /**
     * Включает или выключает DISTINCT-режим селекта
     *
     * @param distinct включатель-выключатель
     * @return this
     */
    public Select distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * Включает DISTINCT-режим селекта.
     *
     * @return this
     */
    public Select distinct() {
        this.distinct = true;
        return this;
    }

    /**
     * Включает или выключает (если нул) указанный режим синхронизации селекта.
     *
     * @param readLockingOrNull
     * @return this
     * @see #forUpdate()
     * @see #lockInShareMode()
     */
    public Select lock(ReadLocking readLockingOrNull) {
        this.readLocking = readLockingOrNull;
        return this;
    }

    /**
     * Включает режим селекта FOR UPDATE.
     *
     * @return this
     * @see #lock(ReadLocking)
     * @see #lockInShareMode()
     */
    public Select forUpdate() {
        this.readLocking = ReadLocking.FOR_UPDATE;
        return this;
    }

    /**
     * Включает режим селекта LOCK IN SHARE MODE.
     *
     * @return this
     * @see #lock(ReadLocking)
     * @see #forUpdate()
     */
    public Select lockInShareMode() {
        this.readLocking = ReadLocking.LOCK_IN_SHARE_MODE;
        return this;
    }

    // --------- ForceIndex ---------------------

    /**
     * Добавляет первичной таблице название индекса в хинт FORCE INDEX("...").
     *
     * @param indexNames
     * @return this
     */
    public Select forceIndex(String... indexNames) {
        this.tables.addForceIndex(indexNames);
        return this;
    }

    /**
     * Удаляет из первичной таблицы хинт FORCE INDEX("...").
     *
     * @return this
     */
    public Select withoutForceIndex() {
        this.tables.dropForceIndex();
        return this;
    }

    // --------- Columns ---------------------

    /**
     * Добавляет все колонки указанного объекта.
     * <p>
     * Возможно, удобней будет вызвать {@link #allColumns()},
     * который добавит все колонки всех таблиц, участвующих в селекте на момент вызова метода.
     *
     * @param dbDataObject исследуемый объект
     * @return this
     * @see #allColumns()
     */
    public Select columns(DbDataObject dbDataObject) {
        this.columns.addAll(dbDataObject);
        return this;
    }

    /**
     * Добавляет в селект указанную колонку.
     *
     * @param column колонка для добавления
     * @return this
     */
    public Select column(DbColumn<?> column) {
        this.columns.add(column);
        return this;
    }

    /**
     * Добавляет в селект указанные колонки.
     *
     * @param columns колонки для добавления
     * @return this
     */
    @SuppressWarnings({"OverloadedVarargsMethod"}) public Select columns(DbColumn... columns) {
        this.columns.addAll(columns);
        return this;
    }

    /**
     * Добавляет в селект указанные колонки.
     *
     * @param columns колонки для добавления
     * @return this
     */
    public Select columns(Collection<? extends DbColumn> columns) {
        this.columns.addAll(columns);
        return this;
    }

    /**
     * Добавляет в селект все колонки всех таблиц, участвующих в селекте на момент вызова метода.
     * <p>
     * Этот метод автоматически вызывают перед выполнением селекта, если список колонок пуст.
     * Таким образом, по умолчанию селект населектит все возможные колонки, и добавлять что-либо явно
     * (методами <code>column(...)</code>) нужно, чтобы селектить только некоторые колонки.
     */
    public Select allColumns() {
        columns.addAll(tables.getPrimaryTable());
        if (tables.hasJoins()) {
            for (Join join : tables.getJoins()) {
                columns.addAll(join.getTable());
            }
        }
        return this;
    }

    /**
     * Очищает список колонок селекта.
     *
     * @return this
     */
    public Select dropColumns() {
        columns.clear();
        return this;
    }

    // ----------- Tables --------------

    /**
     * Добавляет указанный джойн.
     *
     * @param join
     * @return this
     */
    public Select join(Join join) {
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
    public Select innerJoin(DbDataObject table, Condition condition) {
        tables.addJoin(new InnerJoin(table, condition));
        return this;
    }

    /**
     * Создаёт и добавляет указанный Left Join с указанным условием
     *
     * @param table приджойниваемая таблица
     * @param condition условие приджойнивания
     * @return this
     * @see #innerJoin(RefDbDataObject, DbColumn)
     */
    public Select leftJoin(DbDataObject table, Condition condition) {
        tables.addJoin(new LeftJoin(table, condition));
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
    public Select innerJoin(RefDbDataObject table, DbColumn<Long> externalIdColumn) {
        tables.addJoin(new InnerJoin(table, table.idField().eqColumn(externalIdColumn)));
        return this;
    }

    /**
     * добавляет в селект джойн переданной таблицы с условием
     * равенства ключевого поля таблицы с переданной внешней ид-колонкой.
     * <p>
     * «LEFT JOIN table ON table.keyField = referenceIdField»
     *
     * @param table
     * @param externalIdColumn
     * @return this
     */
    public Select leftJoin(RefDbDataObject table, DbColumn<Long> externalIdColumn) {
        tables.addJoin(new LeftJoin(table, table.idField().eqColumn(externalIdColumn)));
        return this;
    }

    // ----------- Where (and) ---------------

    protected Where getWhere() {
        if (where == null) {
            where = new Where();
        }
        return where;
    }

    /**
     * Добавляет указанное условие в селект.
     * Метод можно вызывать несколько раз, все условия будут связаны через AND.
     *
     * @param condition условие селекта
     * @return this
     * @see Condition#and(Condition)
     * @see Condition#or(Condition)
     */
    public Select where(Condition condition) {
        getWhere().add(condition);
        return this;
    }

    /**
     * Добавляет указанные условия в селект.
     * Метод можно вызывать несколько раз, все условия, уже добавленные и новые будут связаны через AND.
     *
     * @param conditions условия селекта
     * @return this
     * @see Condition#and(Condition)
     * @see Condition#or(Condition)
     */
    @SuppressWarnings({"OverloadedVarargsMethod"})
    public Select where(Condition... conditions) {
        getWhere().add(conditions);
        return this;
    }

    /**
     * Добавляет указанные условия в селект.
     * Метод можно вызывать несколько раз, все условия, уже добавленные и новые будут связаны через AND.
     *
     * @param conditions условия селекта
     * @return this
     * @see Condition#and(Condition)
     * @see Condition#or(Condition)
     */
    public Select where(Collection<? extends Condition> conditions) {
        getWhere().add(conditions);
        return this;
    }

    /**
     * Очищает where-часть запроса. Можно добавлять условия заново, пожалуйста.
     */
    public void dropWhere() {
        this.where = null;
    }

    // -------- Order by ---------------

    protected OrderBy getOrderBy() {
        if (orderBy == null) {
            orderBy = new OrderBy();
        }
        return orderBy;
    }

    /**
     * Сбрасывает
     *
     * @return
     */
    public Select withoutOrder() {
        getOrderBy().clear();
        getOrderBy().add(OrderByEntity.custom().str("NULL"));
        return this;
    }

    /**
     * Добавляет к директивам сортировки возрастающую сортировку по указанной колонке.
     *
     * @param column колонка, по которой сотрировать
     * @return this
     */
    public Select orderBy(DbColumn column) {
        getOrderBy().add(new OrderByColumn(column, Direction.ASC));
        return this;
    }

    /**
     * Добавляет к директивам сортировки возрастающую сортировку по указанной колонке.
     *
     * @param column колонка, по которой сотрировать
     * @param direction
     * @return this
     */
    public Select orderBy(DbColumn column, Direction direction) {
        getOrderBy().add(new OrderByColumn(column, direction));
        return this;
    }

    /**
     * Добавляет к директивам сортировки сортировку по NOW() —
     * это SQL-ный эвфемизм, который значит «не сортировать, а перемешать в более-менее случайном порядке».
     *
     * @return this
     */
    public Select randomOrder() {
        getOrderBy().add(RandomOrder.NOW);
        return this;
    }

    /**
     * Добавляет к директивам сортировки сортировку по NOW() —
     * это SQL-ный эвфемизм, который значит «не сортировать, а перемешать в более-менее случайном порядке».
     *
     * @return this
     */
    public Select randomOrder(int seed) {
        getOrderBy().add(new RandomOrder(seed));
        return this;
    }

    /**
     * Добавляет к директивам сортировки возрастающую сортировку по колонке с указанным номером.
     *
     * @param number номер колонки
     * @return this
     */
    public Select orderByColumnNo(int number) {
        getOrderBy().add(new OrderByNumber(number, Direction.ASC));
        return this;
    }

    /**
     * Добавляет к директивам сортировки сортировку по колонке с указанным номером.
     *
     * @param number номер колонки
     * @param direction направление сортировки
     * @return this
     */
    public Select orderByColumnNo(int number, Direction direction) {
        getOrderBy().add(new OrderByNumber(number, direction));
        return this;
    }

    /**
     * Добавляет к директивам сортировки возрастающую сортировку по указанному условию.
     *
     * @param condition условие сортировки
     * @return this
     */
    public Select orderByCondition(Condition condition) {
        getOrderBy().add(new OrderByCondition(condition, Direction.ASC));
        return this;
    }

    /**
     * Добавляет к директивам сортировки сортировку по указанному условию.
     *
     * @param condition условие сортировки
     * @param direction направление сортировки
     * @return this
     */
    public Select orderByCondition(Condition condition, Direction direction) {
        getOrderBy().add(new OrderByCondition(condition, direction));
        return this;
    }

    /**
     * Добавляет к директивам сортировки указанную директиву.
     *
     * @param entity директива сортировки
     * @return this
     */
    public Select orderBy(OrderByEntity entity) {
        getOrderBy().add(entity);
        return this;
    }


    /**
     * Добавляет к директивам сортировки указанные директивы.
     *
     * @param entities директивы сортировки
     * @return this
     */
    @SuppressWarnings({"OverloadedVarargsMethod"})
    public Select orderBy(OrderByEntity... entities) {
        getOrderBy().add(entities);
        return this;
    }

    /**
     * Добавляет к директивам сортировки указанные директивы.
     *
     * @param entities директивы сортировки
     * @return this
     */
    public Select orderBy(Collection<? extends OrderByEntity> entities) {
        getOrderBy().add(entities);
        return this;
    }

    // ------- Having --------------

    protected Having getHaving() {
        if (having == null) {
            having = new Having();
        }
        return having;
    }

    /**
     * Добавляет условие в хавинг-блок.
     * Так как хавингом никто не пользуется, в селекте один утлый метод,
     * чтобы обозначить такую возможность. При необходимости надо добавить сюда
     * методов аналогично where().
     *
     * @param condition условие
     * @return this
     */
    public Select having(Condition condition) {
        getHaving().add(condition);
        return this;
    }

    // -------- Group by ---------------

    protected GroupBy getGroupBy() {
        if (groupBy == null) {
            groupBy = new GroupBy();
        }
        return groupBy;
    }

    /**
     * Добавляет к директивам группировки указанную директиву.
     *
     * @param entity директива группировки
     * @return this
     */
    public Select groupBy(GroupByEntity entity) {
        getGroupBy().add(entity);
        return this;
    }

    /**
     * Добавляет к директивам группировки указанные директивы.
     *
     * @param entities директивы группировки
     * @return this
     */
    public Select groupBy(GroupByEntity... entities) {
        getGroupBy().add(entities);
        return this;
    }

    /**
     * Добавляет к директивам группировки указанные директивы.
     *
     * @param entities директивы группировки
     * @return this
     */
    public Select groupBy(Collection<? extends GroupByEntity> entities) {
        getGroupBy().add(entities);
        return this;
    }

    // ---------- Limit --------------

    /**
     * Убирает установленный лимит.
     *
     * @return this
     */
    public Select dropLimit() {
        this.limit = null;
        return this;
    }

    /**
     * Устанавливает указанный лимит.
     *
     * @param limit лимит
     * @return this
     */
    public Select limit(Limit limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Устанавливает указанный лимит — до указанного количества записей.
     * <p>
     * Стоит иметь в виду, что перед выполнением однострочных load- и count-селектов LIMIT 1 проставляется автоматически.
     *
     * @param limit лимит
     * @return this
     */
    public Select limit(int limit) {
        this.limit = limit == 1 ? Limit.LIMIT_1 : new Limit(limit);
        return this;
    }

    /**
     * Устанавливает селекту LIMIT 1.
     * <p>
     * Этот метод не имеет практического смысла и существует лишь ради этого комментария:
     * перед выполнением однострочных load- и count-селектов LIMIT 1 проставляется автоматически,
     * а для многострочных browse-селектов LIMIT 1 противоречит сути метода.
     *
     * @return this
     */
    public Select limit1() {
        this.limit = Limit.LIMIT_1;
        return this;
    }

    @Override public String toString() {
        return new StringDumpBuilder()
            .append("distinct", distinct)
            .append("columns", columns)
            .append("tables", tables)
            .append("where", where)
            .append("having", having)
            .append("orderBy", orderBy)
            .append("groupBy", groupBy)
            .append("limit", limit)
            .append("readLocking", readLocking)
            .toString();
    }
}
