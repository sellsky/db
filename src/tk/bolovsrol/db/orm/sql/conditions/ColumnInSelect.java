package tk.bolovsrol.db.orm.sql.conditions;

import tk.bolovsrol.db.orm.sql.containers.consecutive.items.ConstantItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.DbColumnItem;
import tk.bolovsrol.db.orm.sql.containers.consecutive.items.SubSelectItem;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;
import tk.bolovsrol.db.orm.sql.statements.select.Select;

/**
 * Достаточно примитивная реализация подселекта для ситуации <code>... WHERE column_a IN (SELECT column_b FROM ...)</code>.
 * <p/>
 * Предполагается, что у встраиваемого селекта только одна колонка, которая возвращает
 * интересующее нас значение, и между внешним и добавляемым селектами нет никаких
 * взаимоотношений. (Если они есть, то следует воспользоваться джойнами.)
 */
public class ColumnInSelect extends ConsecutiveCondition {

    private static final ConstantItem IN = new ConstantItem(" IN(");
    private static final ConstantItem BRACKET = new ConstantItem(")");

    public ColumnInSelect(DbColumn<?> column, final Select select) {
        append(new DbColumnItem(column));
        append(IN);
        append(new SubSelectItem(select));
        append(BRACKET);
    }

}
