package tk.bolovsrol.db.orm.sql.containers.consecutive.items;

import tk.bolovsrol.db.DatabaseProductNames;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

/**
 * Записывает команду сделать [NOT ]REGEXP в выражении А [NOT ]REGEXP B в соответствии с синтаксисом СУБД.
 * Поддерживаем Мускл и Постгре.
 */
public class RegexpItem implements ConsecutiveItem {

//    /** Позитивное сравнение, A REGEXP B. */
//    public static final RegexpItem REGEXP = new RegexpItem(" REGEXP", " ~ ");
//    /** Негативное сравнение, A NOT REGEXP B. */
//    public static final RegexpItem NOT_REGEXP = new RegexpItem(" NOT REGEXP", " !~ ");

    private final String mysqlKeyword;
    private final String postgreKeyword;

    public RegexpItem(String mysqlKeyword, String postgreKeyword) {
        this.mysqlKeyword = mysqlKeyword;
        this.postgreKeyword = postgreKeyword;
    }

    private String pickKeyword(String databaseProductName) {
        switch (databaseProductName) {
        case DatabaseProductNames.MYSQL:
            return mysqlKeyword;
        case DatabaseProductNames.POSTGRESQL:
            return postgreKeyword;
        default:
            throw new DbException("REGEXP matching is not supported for " + databaseProductName);
        }
    }

    @Override public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException {
        sb.append(pickKeyword(databaseProductName));
    }

    @Override public void committed() {
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws DbException {
        return pos;
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
    }
}
