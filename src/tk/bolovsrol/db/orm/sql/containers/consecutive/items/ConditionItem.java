package tk.bolovsrol.db.orm.sql.containers.consecutive.items;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.conditions.Condition;

public class ConditionItem implements ConsecutiveItem {
	private final Condition condition;

	public ConditionItem(Condition condition) { this.condition = condition; }

	@Override public void writeSqlExpression( StringBuilder sb, String databaseProductName,
			Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
		condition.writeSqlExpression(sb, databaseProductName, tableAliases); }

	@Override public int putValues(PreparedStatement ps, int pos) throws DbException {
		return pos; }

	@Override public void committed() { }

	@Override public void appendSqlLogValues(List<String> list) throws DbException { }

	@Override public String toString() { return condition.toString(); }
}


