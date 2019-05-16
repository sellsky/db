package tk.bolovsrol.db.benchmark.threads;

import tk.bolovsrol.db.benchmark.BenchmarkTable;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.LogDome;

import java.sql.Connection;
import java.util.Random;

/**
 * Развлекается, селектя случайные индивидуальные записи, обновляя им поля  и апдейтя их обратно.
 */
public class UpdaterThread extends AbstractWorkerThread {
    private long updatesPerformed;

    public UpdaterThread(String name, LogDome log, BenchmarkTable[] tables, Random random) {
        super(name, log, tables, random);
    }

    @Override protected void work() throws Exception {
        BenchmarkTable table = getRandomTable().cleanCopy();

        try (Connection con = ConnectionManager.getConnection()) {
            if (!table.select().randomOrder().load(con)) {
                log.hint("No record is loaded from " + table.getSqlCatalogName() + '.' + table.getSqlTableName());
            } else {
                log.hint("Loaded " + Spell.get(table));
                table.setRandomValues(random);
				table.update(con);
				log.hint("Updated " + Spell.get(table));
				updatesPerformed++;
			}
        }
    }

    public long getUpdatesPerformed() {
        return updatesPerformed;
    }
}
