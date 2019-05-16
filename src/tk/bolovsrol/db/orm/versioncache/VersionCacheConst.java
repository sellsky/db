package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.db.DbProperties;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogLevel;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.TimeUtils;

/** Кешовые константы. */
public final class VersionCacheConst {

    /** Частота проверки изменений кешируемых объектов, по умолчанию 3 секунды. */
    public static final long REFRESH_INTERVAL = Cfg.getLong("versioncache.refresh.ms", 3000L, Log.getInstance());

    /** Название SQL-схемы {@link VersionHistoryDbdo}, по умолчанию «version_cache». */
    public static final String VERSION_HISTORY_SQL_CATALOG_NAME =
        DbProperties.properties().get("versionCache.versionHistory.sqlCatalogName", "version_cache");

    /** Название SQL-таблицы {@link VersionHistoryDbdo}, по умолчанию «version_history». */
    public static final String VERSION_HISTORY_SQL_TABLE_NAME =
        DbProperties.properties().get("versionCache.versionHistory.sqlTableName", "version_history");

    /** Название SQL-схемы {@link LatestAccessDbdo}, по умолчанию «version_cache». */
    public static final String LATEST_ACCESS_SQL_CATALOG_NAME =
        DbProperties.properties().get("versionCache.latestAccess.sqlCatalogName", "version_cache");

    /** Название SQL-таблицы {@link LatestAccessDbdo}, по умолчанию «latest_access». */
    public static final String LATEST_ACCESS_SQL_TABLE_NAME =
        DbProperties.properties().get("versionCache.latestAccess.sqlTableName", "latest_access");

    /**
     * Можно ли логгировать SQL-запросы кэша.
     * <p>
     * Он долбится к базе часто, и запросы изрядно засирают логи,
     * так что включать их имеет смысл только ради отладки собственно кеша.
     * <p>
     * По умолчанию нельзя.
     */
    public static final boolean LOG_SQL = Cfg.getBoolean("log.sql.versioncache", false);

    /**
     * Можно ли логгировать замеченные изменения в таблицах.
     * <p>
     * Пишет уровнем {@link LogLevel#TRACE}
     * информацию о замеченных изменениях в БД.
     * <p>
     * По умолчанию можно.
     */
    public static final boolean LOG_CHANGES = Cfg.getBoolean("log.versioncache", true);

    /**
     * Ограничение размера хранилища кэша неполного типа.
     * Наименее часто используемые записи будут удалены из кэша,
     * чтобы не допустить перегрузки.
     * <p>
     * По умолчанию лимит 10000 записей.
     */
    public static final int PARTIAL_MAX_RECORDS = Cfg.getInteger("versioncache.partial.maxRecords", 10000, Log.getInstance());

    /**
     * Максимальное время спячки записывателя информации
     * о дате последнего доступа к записям.
     * <p>
     * По умолчанию 15 секунд.
     */
    public static final long LATEST_ACCESS_SLEEP = Cfg.getLong("versioncache.latestAccess.sleep.ms", 15000L, Log.getInstance());

    /**
     * Количество несохранённых записей, при достижении которого
     * записыватель информации о дате последнего доступа к записям
     * должен начинать их сохранять, даже если он спал меньше разрешённого срока.
     * <p>
     * По умолчанию 64 штуки.
     */
    public static final int LATEST_ACCESS_THRESHOLD = Cfg.getInteger("versioncache.latestAccess.threshold", 64, Log.getInstance());

    /** Разрешено ли очищать историю версий. */
    public static final boolean CLEANUP_ENABLED = Cfg.getBoolean("versioncache.cleanup.enabled", false);

    /** С каким максимальным запаздыванием очищать историю версий. */
    public static final Duration CLEANUP_LATENCY = Cfg.getDuration("versioncache.cleanup.latency", new Duration(TimeUtils.MS_IN_HOUR), Log.getInstance());

    /** Насколько старые записи удалять из истории версий, очищая её. */
    public static final Duration CLEANUP_AGE = Cfg.getDuration("versioncache.cleanup.age", new Duration(TimeUtils.MS_IN_HOUR), Log.getInstance());


    //--- triggers suite

    /** Режим проверки наличия необходимых триггеров. */
    public enum TriggerCheckAction {
        /** Не проверять ничего. */
        IGNORE,
        /** Писать в лог варнинг. */
        WARN,
        /** Самостоятельно исправить ситуацию. */
        FIX
    }

    /** Проверять ли наличие и наполнение необходимых для версионного кэша триггеров при первой загрузке справочника. */
    public static final TriggerCheckAction TRIGGER_CHECK_ACTION = Cfg.getEnum("versioncache.trigger.checkAction", TriggerCheckAction.FIX, Log.getInstance());

    /**
     * Команда, изменяющая разделитель для последующих инструкций создания триггеров на {@link #TRIGGER_DELIMITER}.
     *
     * @see tk.bolovsrol.db.orm.versioncache.TriggerUtils
     */
    public static final String TRIGGER_DELIMITER_START =
        Cfg.get("versioncache.trigger.delimiterStart", DbProperties.properties().get("versioncache.trigger.delimiterStart"));

    /**
     * Команда, возвращающая разделитель после инструкций создания триггеров.
     *
     * @see tk.bolovsrol.db.orm.versioncache.TriggerUtils
     */
    public static final String TRIGGER_DELIMITER_END =
        Cfg.get("versioncache.trigger.delimiterEnd", DbProperties.properties().get("versioncache.trigger.delimiterEnd"));

    /**
     * Разделитель инструкций для создания триггеров, используемый после {@link #TRIGGER_DELIMITER_START} и до {@link #TRIGGER_DELIMITER_END}
     *
     * @see tk.bolovsrol.db.orm.versioncache.TriggerUtils
     */
    public static final String TRIGGER_DELIMITER =
        Cfg.get("versioncache.trigger.delimiter", DbProperties.properties().get("versioncache.trigger.delimiter"));

    /**
     * Шаблон команды, удаляющей существующий триггер.
     *
     * @see tk.bolovsrol.db.orm.versioncache.TriggerUtils
     */
    public static final String TRIGGER_DROP =
        Cfg.get("versioncache.trigger.drop", DbProperties.properties().get("versioncache.trigger.drop"));

    /**
     * Шаблон команды, создающий триггер AFTER INSERT для версионного кэша.
     *
     * @see tk.bolovsrol.db.orm.versioncache.TriggerUtils
     */
    public static final String TRIGGER_CREATE_AFTER_INSERT =
        Cfg.get("versioncache.trigger.create.afterInsert", DbProperties.properties().get("versioncache.trigger.create.afterInsert"));

    /**
     * Шаблон команды, создающий триггер AFTER UPDATE для версионного кэша.
     *
     * @see tk.bolovsrol.db.orm.versioncache.TriggerUtils
     */
    public static final String TRIGGER_CREATE_AFTER_UPDATE =
        Cfg.get("versioncache.trigger.create.afterUpdate", DbProperties.properties().get("versioncache.trigger.create.afterUpdate"));

    /**
     * Шаблон команды, создающий триггер AFTER DELETE для версионного кэша.
     *
     * @see tk.bolovsrol.db.orm.versioncache.TriggerUtils
     */
    public static final String TRIGGER_CREATE_AFTER_DELETE =
        Cfg.get("versioncache.trigger.create.afterDelete", DbProperties.properties().get("versioncache.trigger.create.afterDelete"));

    private VersionCacheConst() {
    }

}
