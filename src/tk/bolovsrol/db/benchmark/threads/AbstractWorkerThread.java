package tk.bolovsrol.db.benchmark.threads;

import tk.bolovsrol.db.benchmark.BenchmarkTable;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.threads.HaltableThread;

import java.util.Random;

public abstract class AbstractWorkerThread extends HaltableThread {

    protected final LogDome log;
    protected final BenchmarkTable[] tables;
    protected final Random random;

    public AbstractWorkerThread(String name, LogDome log, BenchmarkTable[] tables, Random random) {
        super(name);
        this.log = log;
        this.tables = tables;
        this.random = random;
    }

    @Override public void run() {
        try {
            while (!isInterrupted()) {
                work();
            }
        } catch (InterruptedException e) {
            // job is over
        } catch (Exception e) {
            log.exception(e);
        }
    }

    protected BenchmarkTable getRandomTable() {
        return tables[random.nextInt(tables.length)];
    }

    abstract protected void work() throws Exception;
}
