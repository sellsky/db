package tk.bolovsrol.db.orm.sql.containers.consecutive.items;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.statements.select.Select;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Достаточно примитивная реализация подселекта для ситуации <code>... WHERE column_a IN (SELECT column_b FROM ...)</code>.
 * <p>
 * Предполагается, что у встраиваемого селекта только одна колонка, которая возвращает
 * интересующее нас значение, и между внешним и добавляемым селектами нет никаких
 * взаимоотношений. (Если они есть, то следует воспользоваться джойнами.)
 */
public class SubSelectItem implements ConsecutiveItem {
    private final Select select;

    public SubSelectItem(Select select) {this.select = select;}

    @Override public void committed() {
        // nothing to do
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        return select.putValues(ps, pos);
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
        select.appendSqlLogValues(list);
    }

    @Override public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        select.writeSqlExpression(sb, databaseProductName);
    }
}
