package tk.bolovsrol.db.orm.sql.statements;

import tk.bolovsrol.db.orm.fields.LongDbField;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.WritingSqlExpression;
import tk.bolovsrol.db.orm.sql.statements.select.Join;
import tk.bolovsrol.utils.DieOnNullMap;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Контейнер для выбираемых табличек */
public class Tables implements WritingSqlExpression {
    private DbDataObject primaryTable;
    private List<String> forceIndexNames;
    private LongDbField primaryTableKeyField;
    private Collection<Join> joins;


    public Tables() {
    }

    public Tables(DbDataObject dbDataObject) {
        this.primaryTable = dbDataObject;
    }

    public Tables(RefDbDataObject refDbDataObject) {
		this.primaryTableKeyField = refDbDataObject.idField();
		this.primaryTable = refDbDataObject;
	}

    public Tables setPrimaryTable(DbDataObject dbDataObject) {
        this.primaryTable = dbDataObject;
        return this;
    }

    public Tables setPrimaryTable(RefDbDataObject refDbDataObject) {
		this.primaryTableKeyField = refDbDataObject.idField();
		this.primaryTable = refDbDataObject;
		return this;
    }

    public Tables addJoin(Join join) {
        if (joins == null) {
            joins = new ArrayList<>();
        }
        joins.add(join);
        return this;
    }

    private static class AliasGenerator {
        private char ch = 'a';
        private int suffix;

        public String nextAlias() {
            String result;
            if (suffix > 0) {
                result = String.valueOf(ch) + suffix;
            } else {
                result = String.valueOf(ch);
            }
            if (ch == 'z') {
                suffix++;
                ch = 'a';
            } else {
                ch++;
            }
            return result;
        }
    }

    public Map<DbDataObject, String> getAliases() {
        if (joins == null) {
            return null;
        } else {
            Map<DbDataObject, String> aliases = new IdentityHashMap<>();
            AliasGenerator ag = new AliasGenerator();
            aliases.put(primaryTable, ag.nextAlias());
            for (Join join : joins) {
                aliases.put(join.getTable(), ag.nextAlias());
            }
            return new DieOnNullMap<>(aliases);
        }
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        if (primaryTable != null) {
            primaryTable.writeSqlExpression(sb, databaseProductName, tableAliases);
        }
        if (forceIndexNames != null) {
            sb.append(" FORCE INDEX(");
            for (String name : forceIndexNames) {
                sb.append("\"").append(StringUtils.mask(name, '\'', StringUtils.QUOTES)).append('\"').append(',');
            }
            sb.setCharAt(sb.length() - 1, ')');
        }
        if (joins != null) {
            for (Join join : joins) {
                sb.append(' ');
                join.writeSqlExpression(sb, databaseProductName, tableAliases);
            }
        }
    }

    public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        if (joins != null) {
            for (Join join : joins) {
                pos = join.putValues(ps, pos);
            }
        }
        return pos;
    }

    public void appendValues(List<String> list) throws DbException {
        if (joins != null) {
            for (Join join : joins) {
                join.appendSqlLogValues(list);
            }
        }
    }

    public boolean hasJoins() {
        return joins != null && !joins.isEmpty();
    }

    public DbDataObject getPrimaryTable() {
        return primaryTable;
    }

	public LongDbField getPrimaryTableKeyField() {
		return primaryTableKeyField;
	}

    public Collection<Join> getJoins() {
        return joins;
    }

    public void addForceIndex(String... indexNames) {
        if (forceIndexNames == null) {
            forceIndexNames = new ArrayList<>();
        }
        this.forceIndexNames.addAll(Arrays.asList(indexNames));
    }

    public void dropForceIndex() {
        this.forceIndexNames = null;
    }

    @Override
    public String toString() {
        return new StringDumpBuilder()
            .append("table", primaryTable)
                .append("joins", joins)
                .toString();
    }
}
