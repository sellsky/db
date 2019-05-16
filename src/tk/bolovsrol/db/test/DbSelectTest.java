package tk.bolovsrol.db.test;

import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringDumpBuilder;
import tk.bolovsrol.utils.properties.Cfg;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class DbSelectTest {
    private DbSelectTest() {
    }

    public static void main(String[] args) {
        try {
            Cfg.init(args[0]);
            String dbName = Cfg.get("test.dbName", "test");
            String tableName = Cfg.get("test.tableName", "test");
            System.out.println("Looking for table " + Spell.get(tableName) + " in db " + Spell.get(dbName));
            doTestSelect(dbName, tableName);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static void doTestSelect(String dbName, String tableName) throws SQLException, InterruptedException {
        Connection con = ConnectionManager.getConnection(dbName);
        try {
            validateTableName(tableName, con.getMetaData().getExtraNameCharacters());
            PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableName);
            try {
                ResultSet rs = ps.executeQuery();
                try {
                    StringDumpBuilder sdb = new StringDumpBuilder("\t");
                    ResultSetMetaData tableMeta = rs.getMetaData();
                    for (int i = 1; i <= tableMeta.getColumnCount(); i++) {
                        sdb.append(tableMeta.getColumnName(i));
                    }
                    System.out.println(sdb);
                    while (rs.next()) {
                        sdb.clear();
                        for (int i = 1; i <= tableMeta.getColumnCount(); i++) {
                            sdb.append(rs.getString(i));
                        }
                        System.out.println(sdb);
                    }
                } finally {
                    JDBCUtils.close(rs);
                }
            } finally {
                JDBCUtils.close(con);
            }
        } finally {
            JDBCUtils.close(con);
        }
    }

    private static void validateTableName(String tableName, String extraChars) {
        for (char ch : tableName.toCharArray()) {
            if ((ch < '0' || ch > '9') && (ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && ch != '_' && extraChars.indexOf(ch) < 0) {
                throw new IllegalArgumentException("Table name contains invalid character " + Spell.get(ch));
            }
        }
    }
}
