package tk.bolovsrol.db.orm.sql.statements;

import tk.bolovsrol.db.DatabaseProductNames;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.PuttingSqlExpression;
import tk.bolovsrol.utils.Spell;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Добавляет Limit X,Y запросу
 * <p>
 * Чтобы не создавать «LIMIT 1», можно воспользоваться статическим {@link #LIMIT_1}.
 * <p>
 * Здесь маркер-интерфейс не используем, потому что лимит у нас в принципе один такой, и других ни разу не надо было.
 * Если вдруг понадобится иная реализация лимита, то этот лимит надо разделить на маркер-интерфейс <code>public interface Limig extends PuttingSqlExpression</code>
 * и реализацию, наследующую этот интерфейс.
 *
 * @see #LIMIT_1
 */
public class Limit implements PuttingSqlExpression {

    public static final Limit LIMIT_1 = new Limit(0, 1);

    private final int offset;
    private final int rowCount;

    public Limit(int offset, int rowCount) {
        this.offset = offset;
        this.rowCount = rowCount;
    }

    public Limit(int rowCount) {
        this(0, rowCount);
    }

    public int getOffset() {
        return offset;
    }

    public int getRowCount() {
        return rowCount;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        switch (databaseProductName) {
        case DatabaseProductNames.MYSQL:
            sb.append(" LIMIT ");
            if (offset != 0) {
                sb.append(offset).append(',');
            }
            sb.append(rowCount);
            break;
        case DatabaseProductNames.POSTGRESQL:
            sb.append(" LIMIT ");
            sb.append(rowCount);
            if (offset != 0) {
                sb.append(" OFFSET ").append(offset);
            }
            break;
        case DatabaseProductNames.ORACLE:
            // https://forums.oracle.com/forums/thread.jspa?threadID=415724
            // http://stackoverflow.com/questions/470542/how-do-i-limit-the-number-of-rows-returned-by-an-oracle-query
            if (offset > 0) {
                sb.insert(0, "SELECT * FROM(SELECT oralimit.*, ROWNUM rnum FROM(");
                sb.append(") oralimit WHERE ROWNUM<=").append(rowCount)
                    .append(") WHERE rnum>=").append(offset);
            } else {
                sb.insert(0, "SELECT * FROM(");
                sb.append(") WHERE ROWNUM<=").append(rowCount);
            }
            break;
        default:
            throw new DbException("Unsupported instruction LIMIT for DB: " + Spell.get(databaseProductName));
        }
    }

    @Override
    public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        return pos;
    }

    @Override
    public void appendSqlLogValues(List<String> list) throws DbException {
    }

}
