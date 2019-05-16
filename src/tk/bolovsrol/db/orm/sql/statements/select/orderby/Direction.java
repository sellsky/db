package tk.bolovsrol.db.orm.sql.statements.select.orderby;

import tk.bolovsrol.db.orm.sql.WritingSqlExpression;

/** Направление сортировки. */
public interface Direction extends WritingSqlExpression {
    Direction ASC = (sb, databaseProductName, aliases) -> sb.append(" ASC");
    Direction DESC = (sb, databaseProductName, aliases) -> sb.append(" DESC");
}
