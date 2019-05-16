package tk.bolovsrol.db.orm.sql.containers.consecutive.items;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

/** Простая текстовая константа */
public class ConstantItem implements ConsecutiveItem {
    private final String matter;

    public ConstantItem(String matter) {
        this.matter = matter;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException {
        sb.append(matter);
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws DbException {
        return pos;
    }

    @Override public void committed() {
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
    }

    @Override
    public String toString() {
        return matter;
    }

    public static ConstantItem[] wrap(String... strings) {
        ConstantItem[] cs = new ConstantItem[strings.length];
        for (int i = 0; i < strings.length; i++) {
            cs[i] = new ConstantItem(strings[i]);
        }
        return cs;
    }
}
