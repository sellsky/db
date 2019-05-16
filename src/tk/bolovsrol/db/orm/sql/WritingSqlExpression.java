package tk.bolovsrol.db.orm.sql;

import tk.bolovsrol.db.orm.object.DbDataObject;

import java.sql.SQLException;
import java.util.Map;

/**
 * Сущность, которую можно записать в SQL-выражении, не связанная с обработкой резалтсетов.
 *
 * @see PuttingSqlExpression
 */
public interface WritingSqlExpression {

    void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException;

}
