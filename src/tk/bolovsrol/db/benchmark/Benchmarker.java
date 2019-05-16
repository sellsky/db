package tk.bolovsrol.db.benchmark;

import tk.bolovsrol.db.benchmark.threads.InserterThread;
import tk.bolovsrol.db.benchmark.threads.SelecterThread;
import tk.bolovsrol.db.benchmark.threads.UpdaterThread;
import tk.bolovsrol.db.orm.sql.statements.truncate.Truncate;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.conf.AutoConfiguration;
import tk.bolovsrol.utils.conf.InvalidConfigurationException;
import tk.bolovsrol.utils.conf.Param;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.properties.Cfg;
import tk.bolovsrol.utils.threads.Suspendable;
import tk.bolovsrol.utils.threads.Suspendables;
import tk.bolovsrol.utils.time.Duration;
import tk.bolovsrol.utils.time.SleepUtils;
import tk.bolovsrol.utils.time.TimeUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

public final class Benchmarker {

    private Benchmarker() {}

    private static class Conf extends AutoConfiguration {
        @Param(desc = "схема, в которой искать таблицы для бенчмарков") public String sqlCatalogName = "test";
        @Param(desc = "префикс таблиц для бенчмарков, к которому добавляется номер таблицы (benchmark1, benchmark2, ...)") public String sqlTableName = "benchmark";
        @Param(desc = "количество таблиц для бенчмарков") public int tableCount = 8;
        @Param(desc = "количество инт-полей в таблице") public int intFieldsCount = 8;
        @Param(desc = "количество чар-полей (на 64 символа) в таблице") public int charFieldsCount = 8;
        @Param(desc = "количество денежных полей (2 десятичных цифры) в таблице") public int moneyFieldsCount = 4;

        @Param(desc = "сид для рандом-генератора") public long seed = 1302;

        @Param(desc = "количество тредов, которые будут вставлять записи") public int inserterCount = 4;
        @Param(desc = "максимальное количество записей, которые можно вставлять за раз") public int maxInsertBunchSize = 100;
        @Param(desc = "лог для инсертера") public String inserterLog = null;

        @Param(desc = "количество тредов, которые будут читать случайную запись и апдейтить её") public int updaterCount = 10;
        @Param(desc = "лог для апдейтера") public String updaterLog = null;

        @Param(desc = "количество тредов, которые будут читать пачки случайных записей") public int selecterCount = 10;
        @Param(desc = "максимальное количество записей, которое можно читать за раз") public int maxSelectBunchSize = 10240;
        @Param(desc = "лог для селектера") public String selecterLog = null;

        @Param(desc = "длительность теста") public Duration testDuration = new Duration(TimeUtils.MS_IN_MINUTE);
    }

    public static void main(String[] args) throws UnexpectedBehaviourException {
        Cfg.init(args[0]);

        LogDome log = Log.getInstance();

        Conf c = new Conf();
        try {
            c.load(log, Cfg.getInstance());
            log.hint("Using conf " + Spell.get(c));

            BenchmarkTable[] tables = BenchmarkTable.newTables(c.tableCount, c.sqlCatalogName, c.sqlTableName, c.intFieldsCount, c.charFieldsCount, c.moneyFieldsCount);
            log.hint("Opeating with " + Spell.get(tables.length) + " tables");
            log.hint("Truncating...");
            truncate(tables);

            log.hint("Using random seed " + c.seed);
            Random rootRandom = new Random(c.seed);

            Suspendable[] threads = new Suspendable[c.inserterCount + c.updaterCount + c.selecterCount];
            InserterThread[] inserters = new InserterThread[c.inserterCount];
            UpdaterThread[] updaters = new UpdaterThread[c.updaterCount];
            SelecterThread[] selecters = new SelecterThread[c.selecterCount];
            int u = 0;
            {
                LogDome inserterLog = LogDome.coalesce(c.inserterLog, log);
                for (int i = 0; i < c.inserterCount; i++) {
                    inserters[i] = new InserterThread("Inserter" + i, inserterLog, tables, new Random(rootRandom.nextLong()), c.maxInsertBunchSize);
                    threads[u++] = inserters[i];
                }
            }
            {
                LogDome updaterLog = LogDome.coalesce(c.updaterLog, log);
                for (int i = 0; i < c.updaterCount; i++) {
                    updaters[i] = new UpdaterThread("Updater" + i, updaterLog, tables, new Random(rootRandom.nextLong()));
                    threads[u++] = updaters[i];
                }
            }
            {
                LogDome selecterLog = LogDome.coalesce(c.selecterLog, log);
                for (int i = 0; i < c.selecterCount; i++) {
                    selecters[i] = new SelecterThread("Selecter" + i, selecterLog, tables, new Random(rootRandom.nextLong()), c.maxSelectBunchSize);
                    threads[u++] = selecters[i];
                }
            }

            log.hint("Starting, then waiting for " + c.testDuration);
            for (Suspendable thread : threads) {
                thread.start();
            }

            SleepUtils.sleepAtLeast(c.testDuration);

            log.hint("Stop!");
            Suspendables.shutdown(threads);

            long insertsPerformed = 0L, rowsInserted = 0L, selectsPerformed = 0L, rowsSelected = 0L, updatesPerformed = 0L;
            for (InserterThread inserter : inserters) {
                insertsPerformed += inserter.getInsertsPerformed();
                rowsInserted += inserter.getRowsInserted();
            }
            for (UpdaterThread updater : updaters) {
                updatesPerformed += updater.getUpdatesPerformed();
            }
            for (SelecterThread selecter : selecters) {
                selectsPerformed += selecter.getSelectsPerformed();
                rowsSelected += selecter.getRowsSelected();
            }

            long elapsedSeconds = c.testDuration.getMillis() / TimeUtils.MS_IN_SECOND;

            log.hint("So, in " + c.testDuration + ':');
            long rowsInsertedInSec = rowsInserted / elapsedSeconds;
            long insertsPerformedInSec = insertsPerformed / elapsedSeconds;
            log.hint(
                "Inserted " + rowsInserted + " rows in " + insertsPerformed + " statements; "
                    + rowsInsertedInSec + " rows/sec, " + insertsPerformedInSec + " inserts/sec in " + c.inserterCount + " threads; "
                    + rowsInsertedInSec / c.inserterCount + " rows/sec, " + insertsPerformedInSec / c.inserterCount + " inserts/sec for a single thread"
            );
            long updatesPerformedInSec = updatesPerformed / elapsedSeconds;
            log.hint(
                "Selected-and-then-updated " + updatesPerformed + " rows; "
                    + updatesPerformedInSec + " operations/sec in " + c.updaterCount + " threads; "
                    + updatesPerformedInSec / c.updaterCount + " operations/sec for a single thread"
            );
            long rowsSelectedInSec = rowsSelected / elapsedSeconds;
            long selectsPerformedInSec = selectsPerformed / elapsedSeconds;
            log.hint(
                "Selected " + rowsSelected + " rows in " + selectsPerformed + " statements; "
                    + rowsSelectedInSec + " rows/sec, " + selectsPerformedInSec + " selects/sec in " + c.selecterCount + " threads; "
                    + rowsSelectedInSec / c.selecterCount + " rows/sec, " + selectsPerformedInSec / c.selecterCount + " selects/sec for a single thread"
            );

        } catch (InvalidConfigurationException | InterruptedException e) {
            log.warning(e);
        } catch (SQLException e) {
            log.exception(e);
        }
    }

    private static void truncate(BenchmarkTable[] tables) throws SQLException, InterruptedException {
        try (Connection con = ConnectionManager.getConnection()) {
            for (BenchmarkTable table : tables) {
                new Truncate(table).execute(con);
            }
        }
    }
}
