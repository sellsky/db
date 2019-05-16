package tk.bolovsrol.db.benchmark.threads;

import tk.bolovsrol.db.benchmark.BenchmarkTable;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.sql.statements.select.Browser;
import tk.bolovsrol.db.orm.sql.statements.select.Select;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.LogDome;

import java.sql.Connection;
import java.util.Random;

/**
 * Развлекается, селектя пачки случайных записей.
 */
public class SelecterThread extends AbstractWorkerThread {
    private final int maxLimit;

    private long selectsPerformed = 0;
    private long rowsSelected = 0;

    public SelecterThread(String name, LogDome log, BenchmarkTable[] tables, Random random, int maxLimit) {
        super(name, log, tables, random);
        this.maxLimit = maxLimit;
    }

    @Override protected void work() throws Exception {
        BenchmarkTable table = getRandomTable().cleanCopy();
        Select s = table.select().randomOrder().limit(random.nextInt(maxLimit) + 1);
        try (Connection con = ConnectionManager.getConnection()) {
            try (Browser br = s.browse(con)) {
                selectsPerformed++;
                while (br.next()) {
                    log.hint("Selected " + Spell.get(table));
                    rowsSelected++;
                    table.forEach(DbDataField::dropValue);
                }
            }
        }
    }

    public long getSelectsPerformed() {
        return selectsPerformed;
    }

    public long getRowsSelected() {
        return rowsSelected;
    }
}
