package tk.bolovsrol.db.orm.sql.containers.consecutive.items;

import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.utils.containers.NumberContainer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Контейнер, который умеет записывать своё значение. */
public class ValueItem implements ConsecutiveItem {
    private final DbValueContainer container;

    public ValueItem(DbValueContainer container) {
        this.container = container;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException {
        // Если контейнер содержит отрицательную циферку, а предыдущий символ "+",
        // то этот плюс можно будет откусить.
        if (container instanceof NumberContainer
            && !container.isValueNull()
            && ((NumberContainer) container).signum() > 0
            && sb.length() > 1
            && sb.charAt(sb.length() - 1) == '+') {
            sb.setCharAt(sb.length() - 1, '?');
        } else {
            sb.append('?');
        }
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        container.putValue(ps, pos);
        return pos + 1;
    }

    @Override public void committed() {
        container.valueCommitted();
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
        list.add(container.valueToSqlLogString());
    }

    @Override
    public String toString() {
        return container.toString();
    }
}
