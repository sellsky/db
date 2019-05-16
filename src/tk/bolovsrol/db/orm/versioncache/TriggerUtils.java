package tk.bolovsrol.db.orm.versioncache;

import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.textformatter.compiling.InvalidTemplateException;
import tk.bolovsrol.utils.textformatter.compiling.ProxyingCompiledFormatter;
import tk.bolovsrol.utils.textformatter.compiling.TextFormatCompiler;
import tk.bolovsrol.utils.textformatter.compiling.evaluators.MapEvaluator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Утилитка для проверки наличия триггеров для таблиц-справочников, создания их и генерации SQL-команд для выполнения вручную пользователем.
 * <p>
 * Использует шаблоны, определяемые в {@link tk.bolovsrol.db.DbProperties пропертях} или в конфиге.
 * <p>
 * В командах по удалению триггера можно использовать макросы
 * <ul>
 * <li>triggerCatalogName — каталог триггера и
 * <li>triggerTableName — имя триггера.
 * </ul>
 * <p>
 * В командах по созданию триггеров доступны макросы
 * <ul>
 * <li>vhCatalogName — каталог таблицы version_history,
 * <li>vhTableName — имя таблицы version_history,
 * <li>subjectCatalogName — каталог таблицы исследуемого объекта,
 * <li>subjectTableName — имя таблицы исследуемого объекта и
 * <li>subjectKeyFieldName — имя ключевого поля исследуемого объекта.
 * </ul>
 *
 * @see tk.bolovsrol.db.orm.versioncache.VersionCacheConst#TRIGGER_DELIMITER_START
 * @see tk.bolovsrol.db.orm.versioncache.VersionCacheConst#TRIGGER_DELIMITER_END
 * @see tk.bolovsrol.db.orm.versioncache.VersionCacheConst#TRIGGER_DELIMITER
 * @see tk.bolovsrol.db.orm.versioncache.VersionCacheConst#TRIGGER_DROP
 * @see tk.bolovsrol.db.orm.versioncache.VersionCacheConst#TRIGGER_CREATE_AFTER_INSERT
 * @see tk.bolovsrol.db.orm.versioncache.VersionCacheConst#TRIGGER_CREATE_AFTER_UPDATE
 * @see tk.bolovsrol.db.orm.versioncache.VersionCacheConst#TRIGGER_CREATE_AFTER_DELETE
 */
public class TriggerUtils {

    private static final TriggerUtils INSTANCE;

    static {
        try {
            INSTANCE = new TriggerUtils();
        } catch (InvalidTemplateException e) {
            throw new IllegalArgumentException("Cannot initialize Trigger Checker", e);
        }
    }

    public static TriggerUtils getInstance() {
        return INSTANCE;
    }

    private final ProxyingCompiledFormatter triggerDropFtr;
    private final ProxyingCompiledFormatter triggerCreateAfterInsertFtr;
    private final ProxyingCompiledFormatter triggerCreateAfterUpdateFtr;
    private final ProxyingCompiledFormatter triggerCreateAfterDeleteFtr;

    private final MapEvaluator ke;

    /**
     * Лок нужен, с одной стороны, чтобы обеспечить эксклюзивное использование форматтеров — они не тред-сейфны.
     * И с другой стороны, чтобы в логе не перемешивались триггеры разных таблиц.
     */
    private final Object lock = new Object();

    private TriggerUtils() throws InvalidTemplateException {
        ke = new MapEvaluator();
        TextFormatCompiler tfc = new TextFormatCompiler().setQuoteChars(null);
        triggerDropFtr = tfc.compile(VersionCacheConst.TRIGGER_DROP);
        if (triggerDropFtr != null) triggerDropFtr.setKeywordEvaluator(ke);
        triggerCreateAfterInsertFtr = tfc.compile(VersionCacheConst.TRIGGER_CREATE_AFTER_INSERT);
        if (triggerCreateAfterInsertFtr != null) triggerCreateAfterInsertFtr.setKeywordEvaluator(ke);
        triggerCreateAfterUpdateFtr = tfc.compile(VersionCacheConst.TRIGGER_CREATE_AFTER_UPDATE);
        if (triggerCreateAfterUpdateFtr != null) triggerCreateAfterUpdateFtr.setKeywordEvaluator(ke);
        triggerCreateAfterDeleteFtr = tfc.compile(VersionCacheConst.TRIGGER_CREATE_AFTER_DELETE);
        if (triggerCreateAfterDeleteFtr != null) triggerCreateAfterDeleteFtr.setKeywordEvaluator(ke);

        ke.add("vhCatalogName", VersionCacheConst.VERSION_HISTORY_SQL_CATALOG_NAME);
        ke.add("vhTableName", VersionCacheConst.VERSION_HISTORY_SQL_TABLE_NAME);
    }

    /**
     * Если переданный triggerCheckAction == IGNORE, ничего не делает.
     * Иначе проверяет в БД наличие и содержание триггеров для переданного объекта.
     * <p>
     * Если триггеров нет или они отличаются больше, нежели кавычками (в шаблоне должны быть «"»,
     * а в БД допускаются также «`»), если triggerCheckAction == FIX, пытается исправить триггеры в БД,
     * а если triggerCheckAction == WARN или исправление в режиме FIX окончилось неудачей,
     * пишет в лог варнинг с SQL-командами для исправлению ситуации.
     *
     * @param rdbdo исследуемый объект
     */
    public void checkTriggersAndTakeAction(RefDbDataObject rdbdo, VersionCacheConst.TriggerCheckAction triggerCheckAction) {
        if (triggerCheckAction == VersionCacheConst.TriggerCheckAction.IGNORE) {
            return;
        }
        synchronized (lock) {
            List<String> statements = produceCreateStatements(rdbdo, true);
            if (statements == null) {
                return;
            }

            if (!statements.isEmpty()) {
                // сначала попробуем починить всё тихонько сами
                if (triggerCheckAction == VersionCacheConst.TriggerCheckAction.FIX) {
                    if (runCreateStatements(rdbdo, statements)) {
                        return;
                    }
                    // если не удалось, напишем в логе, что надо делать
                }
                Log.warning("Missing or outdated triggers detected for reference table " + rdbdo.getSqlCatalogName() + '.' + rdbdo.getSqlTableName() + ", consider running following SQL statements by hand:");
                Log.warning(VersionCacheConst.TRIGGER_DELIMITER_START);
                for (String s : statements) {
                    Log.warning(s + VersionCacheConst.TRIGGER_DELIMITER);
                }
                Log.warning(VersionCacheConst.TRIGGER_DELIMITER_END);
            }
        }
    }

    /**
     * Генерирует строку с SQL-командами для создания триггеров для переданного объекта.
     *
     * @param rdbdo исследуемый объект
     * @return SQL-команды
     * @see RefDbDataObject#generateSqlCreateTableTemplate()
     */
    public String generateSqlCreateTriggers(RefDbDataObject rdbdo) {
        synchronized (lock) {
            StringDumpBuilder sdb = new StringDumpBuilder("\n");
            sdb.append(VersionCacheConst.TRIGGER_DELIMITER_START);
            for (String s : produceCreateStatements(rdbdo, false)) {
                sdb.append(s + VersionCacheConst.TRIGGER_DELIMITER);
            }
            sdb.append(VersionCacheConst.TRIGGER_DELIMITER_END);
            return sdb.toString();
        }
    }

    /**
     * Выполняет переданные команды для создания триггеров.
     *
     * @param rdbdo исправляемый объект, только для логов
     * @param statements команды
     * @return true — если команды выполнены, иначе false
     */
    private static boolean runCreateStatements(RefDbDataObject rdbdo, List<String> statements) {
        try {
            Log.info("Fixing missing or outdated triggers detected for reference table " + rdbdo.getSqlCatalogName() + '.' + rdbdo.getSqlTableName());
            try (Connection con = ConnectionManager.getConnection()) {
				try (Statement batch = con.createStatement()) {
					for (String statement : statements) {
						batch.addBatch(statement);
					}
					batch.executeBatch();
				}
			}
            Log.hint("Fixed missing or outdated triggers for reference table " + rdbdo.getSqlCatalogName() + '.' + rdbdo.getSqlTableName());
            return true;
        } catch (Exception e) {
            Log.exception("Error creating triggers for reference table " + rdbdo.getSqlCatalogName() + '.' + rdbdo.getSqlTableName(), e);
            return false;
        }
    }

    //  реализовано только для мускля
    private List<String> produceCreateStatements(RefDbDataObject rdbdo, boolean checkDb) {
        ke.add("subjectCatalogName", rdbdo.getSqlCatalogName());
        ke.add("subjectTableName", rdbdo.getSqlTableName());
        ke.add("subjectKeyFieldName", rdbdo.idField().getName());

        Map<String, String> templateStatements = new HashMap<>(3);
        templateStatements.put("INSERT", triggerCreateAfterInsertFtr.format());
        templateStatements.put("UPDATE", triggerCreateAfterUpdateFtr.format());
        templateStatements.put("DELETE", triggerCreateAfterDeleteFtr.format());

        List<String> msg = new ArrayList<>(6);

        if (checkDb) {
            try {
                try (Connection con = ConnectionManager.getConnection()) {
					try (PreparedStatement ps = con.prepareStatement(
						"SELECT \"TRIGGER_SCHEMA\",\"TRIGGER_NAME\",\"EVENT_MANIPULATION\",\"ACTION_STATEMENT\"" +
							" FROM \"INFORMATION_SCHEMA\".\"TRIGGERS\"" +
							" WHERE \"EVENT_OBJECT_SCHEMA\"=?" +
							" AND \"EVENT_OBJECT_TABLE\"=?" +
							" AND \"ACTION_TIMING\"='AFTER'" +
							" ORDER BY \"EVENT_MANIPULATION\""
					)) {
						ps.setString(1, rdbdo.getSqlCatalogName());
						ps.setString(2, rdbdo.getSqlTableName());
						try (ResultSet rs = ps.executeQuery()) {
							while (rs.next()) {
								String manipulation = rs.getString(3);
								String body = rs.getString(4);
								String templateStatement = templateStatements.remove(manipulation);
								if (!templateStatement.contains(body) && !templateStatement.contains(body.replace('`', '"'))) {
                                    ke.add("triggerCatalogName", rs.getString(1));
                                    ke.add("triggerTableName", rs.getString(2));
                                    msg.add(triggerDropFtr.format());
                                    msg.add(templateStatement);
								}
							}
						}
					}
				}
			} catch (Exception e) {
                Log.exception("Error checking triggers for " + rdbdo.getSqlCatalogName() + '.' + rdbdo.getSqlTableName(), e);
            }
        }

        msg.addAll(templateStatements.values());

//        if (!msg.isEmpty()) {
//            msg.add(0,VersionCacheConst.TRIGGER_DELIMITER_START);
//            msg.add(VersionCacheConst.TRIGGER_DELIMITER_END);
//        }
        return msg;
    }

}
