package tk.bolovsrol.db.benchmark.threads;

import tk.bolovsrol.db.benchmark.BenchmarkTable;
import tk.bolovsrol.db.orm.sql.statements.insert.Insert;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.LogDome;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Развлекается, инсертя записи в произвольные таблицы.
 * Индивидуально и пачками.
 */
public class InserterThread extends AbstractWorkerThread {
    private final int maxBunchSize;

    private long insertsPerformed;
    private long rowsInserted;

    public InserterThread(String name, LogDome log, BenchmarkTable[] tables, Random random, int maxBunchSize) {
        super(name, log, tables, random);
        this.maxBunchSize = maxBunchSize;
    }

    @Override protected void work() throws Exception {
        BenchmarkTable tableTemplate = getRandomTable();

        int bunchSize = random.nextInt(maxBunchSize) + 1;
        List<BenchmarkTable> bunch = new ArrayList<>(bunchSize);
        for (int i = bunchSize; i > 0; i--) {
            BenchmarkTable copy = tableTemplate.cleanCopy();
            copy.setRandomValues(random);
            bunch.add(copy);
        }
        try (Connection con = ConnectionManager.getConnection()) {
			Insert.bunchAndLoadIds(con, bunch);
		}
		insertsPerformed++;
        rowsInserted += bunchSize;
        for (BenchmarkTable benchmarkTable : bunch) {
            log.hint("Created " + Spell.get(benchmarkTable));
        }
    }

    public long getInsertsPerformed() {
        return insertsPerformed;
    }

    public long getRowsInserted() {
        return rowsInserted;
    }
}
