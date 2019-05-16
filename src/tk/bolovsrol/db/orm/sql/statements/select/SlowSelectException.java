package tk.bolovsrol.db.orm.sql.statements.select;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.time.TimeUtils;

public class SlowSelectException extends Exception {
    private final long duration;
    private final String sqlDump;

    public SlowSelectException(long duration, String sqlDump) {
        super("Slow select! Exec time " + Spell.getDuration(duration, TimeUtils.ForceFields.HOURS_MINUTES_SECONDS_MS) + ", SQL " + sqlDump);
        this.duration = duration;
        this.sqlDump = sqlDump;
    }

    public long getDuration() {
        return duration;
    }

    public String getSqlDump() {
        return sqlDump;
    }
}
