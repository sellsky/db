package tk.bolovsrol.db.orm.object;

import tk.bolovsrol.db.DbProperties;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.properties.ReadOnlyProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** Объект БД, хранящий свои поля в обычном списке. */
public class AbstractDbDataObject implements DbDataObject, Iterable<DbDataField<?, ?>> {

    /**
     * Статический контейнер для каждой таблицы в БД, хранящий,<br/>
     * во-первых, объявленные в конфиге алиасы каталога и названия таблицы,<br/>
     * а во-вторых, количество полей у объекта.
     * <p>
     * 1. Пользователь может переопределить каталог и название таблицы для любого объекта, указав в конфиге один или оба параметра:
     * <pre>
     * dbdo.&lt;catalogName&gt;.&lt;tableName&gt;.sqlCatalogName=&lt;catalogAlias&gt;
     * dbdo.&lt;catalogName&gt;.&lt;tableName&gt;.sqlTableName=&lt;tableAlias&gt;
     * </pre>
     * При первой инициализации объекта эти параметры будут считаны из конфига и запомнены в контейнере. В SQL-запросах используются
     * эти названия.
     * <p>
     * 2. Поля объекта хранятся в {@link ArrayList}, который, в свою очередь, хранит информацию в обычном массиве, и позволяет указать размер
     * этого массива в своём конструкторе. Мы хотим его указать, чтобы, с одной стороны, не тратить лишнюю память, а с другой,
     * не тратить время на бессмысленное копирование массива по мере регистрации полей. К сожалению, дешёвого способа заранее выяснить,
     * сколько полей будет зарегистрировано, у нас нет.
     * Поэтому я сделал адаптивное вычисление количества полей, этакий костылик. При создании контейнера (перед созданием первого объекта
     * каждой таблицы) в счёчике {@link #fieldCountHint} указано минимальное разумное количество полей {@link #DEFAULT_FIELD_COUNT_HINT}.
     * При создании объекта этот хинт передают конструктору эррэй-листа. По мере регистрации полей этот счётчик увеличивают.
     * Таким образом, первый объект будет создан с недостаточно длинным массивом в эррэй-листе, зато у последующих объектов
     * он будет в точности соответствовать количеству полей.
     */
    protected static class MetaContainer {
        /** Размер создаваемого {@link ArrayList} для хранения полей у нового объекта. */
        public static final int DEFAULT_FIELD_COUNT_HINT = 3;
        /** Название каталога объекта в БД. */
        public final String sqlCatalogName;
        /** Название таблицы объекта в БД. */
        public final String sqlTableName;
        /** Название для использования в селектах, чтобы каждый раз не вычислять: «"catalog"."table"». */
        public final String sqlCatalogAndTableName;
        /** Название для использования в логе, чтобы каждый раз не вычислять: «catalog.table». */
        public final String logCatalogAndTableName;
        /** Адаптивное количество полей объекта данных. */
        private volatile int fieldCountHint = DEFAULT_FIELD_COUNT_HINT;

        public MetaContainer(String sqlCatalogName, String sqlTableName) {
            this.sqlCatalogName = sqlCatalogName;
            this.sqlTableName = sqlTableName;
            if (sqlCatalogName == null) {
                sqlCatalogAndTableName = '\"' + sqlTableName + '\"';
                logCatalogAndTableName = sqlTableName;
            } else {
                sqlCatalogAndTableName = '\"' + sqlCatalogName + "\".\"" + sqlTableName + '\"';
                logCatalogAndTableName = sqlCatalogName + '.' + sqlTableName;
            }
        }
    }

    private static final Map<String, Map<String, MetaContainer>> META_CACHE = new ConcurrentHashMap<>(32, 0.5F, 2);

    private MetaContainer getMetaContainerOrSpawn(String sqlCatalogName, String sqlTableName) {
        String maskedSqlTableName = sqlTableName == null ? getClass().getName() : sqlTableName;
        Map<String, MetaContainer> byCatalog = META_CACHE.get(maskedSqlTableName);
        if (byCatalog == null) {
            byCatalog = new ConcurrentHashMap<>(1, 0.75F, 1); // как правило, имена таблиц у нас уникальны в разрезе БД, так что минимальных настроек тут будет достаточно
            META_CACHE.putIfAbsent(maskedSqlTableName, byCatalog);
        }
        String maskedSqlCatalogName = sqlCatalogName == null ? getClass().getName() : sqlCatalogName;
        MetaContainer meta = byCatalog.get(maskedSqlCatalogName);
        if (meta == null) {
            ReadOnlyProperties cfg = Cfg.getInstance();
            ReadOnlyProperties dbProperties = DbProperties.properties();
            meta = new MetaContainer(
                ReadOnlyProperties.coalesce("dbdo." + sqlCatalogName + '.' + sqlTableName + ".sqlCatalogName", sqlCatalogName, cfg, dbProperties),
                ReadOnlyProperties.coalesce("dbdo." + sqlCatalogName + '.' + sqlTableName + ".sqlTableName", sqlTableName, cfg, dbProperties)
            );
            byCatalog.putIfAbsent(maskedSqlCatalogName, meta);
        }
        return meta;
    }

    //----- Статика позади, теперь поля и методы инстанса

    /** Метаданные объекта. */
    protected MetaContainer meta;

    /**
     * Все поля объекта, отображаемые в базе данных.
     * <p>
     * Наполняется при создании объекта, затем только читается.
     */
	protected final List<DbDataField<?, ?>> dbFields;

    /**
     * Создаёт объект для указанных каталога и таблицы в БД.
     *
     * @param sqlCatalogName название каталога объекта в БД
     * @param sqlTableName название таблицы объекта в БД
     */
    protected AbstractDbDataObject(String sqlCatalogName, String sqlTableName) {
        this.meta = getMetaContainerOrSpawn(sqlCatalogName, sqlTableName);
        this.dbFields = new ArrayList<>(meta.fieldCountHint);
    }

    /**
     * Устанавливает объекту новые названия каталога и таблицы в БД.
     * Так можно динамически менять имя объекта.
     *
     * @param sqlCatalogName название каталога объекта в БД
     * @param sqlTableName название таблицы объекта в БД
     */
    protected void setSqlName(String sqlCatalogName, String sqlTableName) {
        this.meta = getMetaContainerOrSpawn(sqlCatalogName, sqlTableName);
        if (meta.fieldCountHint < dbFields.size()) {
            meta.fieldCountHint = dbFields.size();
        }
    }

    /**
     * Регистрирует поле.
     * <p>
     * <code>DbDataField</code> при инициализации автоматически вызывает этот метод, не нужно вызывать его вручную.
     *
     * @param dataField регистрируемое поле
     */
    @Override public void registerField(DbDataField dataField) {
        dbFields.add(dataField);
        if (meta.fieldCountHint < dbFields.size()) {
            meta.fieldCountHint = dbFields.size();
        }
    }

    /**
     * Возвращает поля объекта в виде списка в порядке их регистрации.
     * <p>
     * Это тот самый список, которым пользуется БД-объект,
     * так что не надо его менять без веских причин.
     *
     * @return список всех полей таблицы в порядке их регистрации
     */
    @Override public List<DbDataField<?, ?>> fields() {
        return dbFields;
    }

    @Override public String getSqlTableName() {
        return meta.sqlTableName;
    }

    @Override public String getSqlCatalogName() {
        return meta.sqlCatalogName;
    }

    @Override public String getLogCatalogAndTableName() {
        return meta.logCatalogAndTableName;
    }

    @Override public String getSqlCatalogAndTableName() {
        return meta.sqlCatalogAndTableName;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException {
        sb.append(meta.sqlCatalogAndTableName);
        if (tableAliases != null) {
            sb.append(" AS ").append(tableAliases.get(this));
        }
    }

    /**
     * Возвращает строковое представление объекта. Указывает его каталог (если задан) и таблицу,
     * затем перечисляет поля со значениями поимённо, отмечает звёздочкой те поля, значение которых изменено
     * и не сохранено в БД.
     *
     * @return [sqlCatalogName.]sqlTableName field1=value1old→value1new field2=value2 ...
     * @see DbDataField#valueToLogString()
     * @see DbDataField#isValueChanged()
     */
    @Override public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append(meta.logCatalogAndTableName);
        for (DbDataField df : dbFields) {
            sb.append(' ');
            sb.append(df.getName());
            sb.append('=');
            sb.append(df.valueToLogString());
        }
        return sb.toString();
    }

    @Override public Iterator<DbDataField<?, ?>> iterator() {
        return dbFields.iterator();
    }

    @Override public void forEach(Consumer<? super DbDataField<?, ?>> action) {
        dbFields.forEach(action);
    }

    @Override public Spliterator<DbDataField<?, ?>> spliterator() {
        return dbFields.spliterator();
    }
}