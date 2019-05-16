package tk.bolovsrol.db.orm.sql.statements.delete;

import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.statements.AbstractSqlStatement;
import tk.bolovsrol.db.orm.sql.statements.Where;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/** Single-table delete. */
public class Delete extends AbstractSqlStatement {

    private DbDataObject table;
    private final Where where = new Where();

    @Deprecated
    public Delete() {
    }

    // ---------- shortcuts ---

    public Delete(DbDataObject primaryTable) {
        this.table = primaryTable;
    }

    //-------- генератор

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName) throws DbException, SQLException {
        sb.append("DELETE FROM ");
        table.writeSqlExpression(sb, databaseProductName, null);
        where.writeSqlExpression(sb, databaseProductName, null);
    }

    public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        pos = where.putValues(ps, pos);
        return pos;
    }

    @Override
    protected void appendSqlLogValues(List<String> values) {
        where.appendSqlLogValues(values);
    }

    /**
     * Загружает первый ряд резалтсета и закрывает его.
     *
     * @return true, если загрузка произошла, false, если загрузка не произошла (резалтсет был пуст)
     * @throws SQLException
     */
    public int execute(Connection con) throws SQLException, DbException {
        PreparedStatement ps = con.prepareStatement(generateSqlExpression(con));
        try {
            putValues(ps, 1);
            return ps.executeUpdate();
        } finally {
            JDBCUtils.close(ps);
        }
    }

    // -------- всякие сеттеры
    // ----------- Tables --------------

    @Deprecated
    public Delete setTable(DbDataObject primaryTable) {
        this.table = primaryTable;
        return this;
    }

    // ----------- Where (and) ---------------

    public Delete where(Condition condition) {
        this.where.add(condition);
        return this;
    }

    public Delete where(Condition... conditions) {
        this.where.add(conditions);
        return this;
    }

    public Delete where(Collection<? extends Condition> conditions) {
        this.where.add(conditions);
        return this;
    }
}
