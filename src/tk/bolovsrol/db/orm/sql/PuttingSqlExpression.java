package tk.bolovsrol.db.orm.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Сущность, которую можно записать в SQL-выражении, не связанная с обработкой резалтсетов,
 * и содержащая значения, которые она может записывать в {@link PreparedStatement}.
 *
 * @see WritingSqlExpression
 */
public interface PuttingSqlExpression extends WritingSqlExpression {

    int putValues(PreparedStatement ps, int pos) throws SQLException, DbException;

    void appendSqlLogValues(List<String> list) throws DbException;
}
