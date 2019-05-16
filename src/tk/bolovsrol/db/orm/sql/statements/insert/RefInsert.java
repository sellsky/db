package tk.bolovsrol.db.orm.sql.statements.insert;

import tk.bolovsrol.db.DatabaseProductNames;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.fields.LongDbField;
import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.statements.AbstractSqlStatement;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.containers.ValueContainer;
import tk.bolovsrol.utils.log.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Инсерт, вставляющий один или несколько объектов одного класса и вычитывающий значение первичного ключа. Без настроек.
 *
 * @see Insert
 */
public class RefInsert extends AbstractSqlStatement {

	private final List<? extends RefDbDataObject> tables;

	RefInsert(RefDbDataObject table) {
		this.tables = Collections.singletonList(table);
	}

	public <T extends RefDbDataObject> RefInsert(List<T> tables) {
		this.tables = tables;
	}

	public <T extends RefDbDataObject> RefInsert(T... tables) {
		this.tables = Arrays.asList(tables);
	}

	@Override
	public void writeSqlExpression(StringBuilder sb, String databaseProductName) throws DbException, SQLException {
		RefDbDataObject table = tables.get(0);
		if (tables.size() > 1 && DatabaseProductNames.ORACLE.equals(databaseProductName)) {
			// insert all into TABLE (col1,col2,col3) values (val1a,val2a,val3a) into TABLE(col1,col2,col3) values (val1b,val2b,val3b) ... SELECT * from dual
			StringBuilder ib = new StringBuilder(256);
			ib.append(" INTO ");
			table.writeSqlExpression(ib, databaseProductName, null);
			ib.append('(');
			for (DbDataField<?, ?> field : table.fields()) {
				field.writeSqlExpression(ib, databaseProductName, null);
				ib.append(',');
			}
			ib.setCharAt(ib.length() - 1, ')');
			ib.append(" VALUES(").append(StringUtils.copies("?", ",", table.fields().size())).append(')');

			sb.append("INSERT ALL");
			StringUtils.appendCopiesTo(sb, ib, tables.size());
			sb.append(" SELECT * FROM DUAL");
		} else {
			// insert into TABLE (col1,col2,col3) values (val1a,val2a,val3a),(val1b,val2b,val3b),...
			sb.append("INSERT INTO ");
			table.writeSqlExpression(sb, databaseProductName, null);
			sb.append('(');
			for (DbDataField<?, ?> field : table.fields()) {
				field.writeSqlExpression(sb, databaseProductName, null);
				sb.append(',');
			}
			sb.setCharAt(sb.length() - 1, ')');
			sb.append(" VALUES");
			StringUtils.appendCopiesTo(sb, '(' + StringUtils.copies("?", ",", table.fields().size()) + ')', ",", tables.size());
		}
	}

	public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
		for (RefDbDataObject table : tables) {
			for (DbDataField<?, ?> field : table.fields()) {
				field.putValue(ps, pos);
				pos++;
			}
		}
		return pos;
	}

	@Override
	protected void appendSqlLogValues(List<String> values) {
		tables.forEach(table -> table.fields().forEach(field -> values.add(field.valueToSqlLogString())));
	}

	private void markValuesCommitted() {
		tables.forEach(table -> table.fields().forEach(ValueContainer::valueCommitted));
	}

	private int insertOracle(Connection con) throws SQLException {
		String databaseProductName = con.getMetaData().getDatabaseProductName();
		if (isAllowLogging()) {
			Log.trace("Insert using Oracle Workaround Mode");
		}
		RefDbDataObject table = tables.get(0);
		LongDbField keyField = table.idField();
		con.setAutoCommit(false);
		try {
			int rowCount;
			// делай раз: лок табле
			{
				StringBuilder sb = new StringBuilder(64);
				sb.append("LOCK TABLE");
				table.writeSqlExpression(sb, databaseProductName, null);
				sb.append(" IN EXCLUSIVE MODE");
				if (isAllowLogging()) {
					Log.trace(sb);
				}
				try (PreparedStatement ps = con.prepareStatement(sb.toString())) {
					ps.execute();
				}
			}

			// делай два: собственно инсерт
			try (PreparedStatement ps = con.prepareStatement(generateSqlExpression(con))) {
				putValues(ps, 1);
				rowCount = ps.executeUpdate();
			}

			// делай три: селект макс. айди
			long lastId;
			{
				StringBuilder sb = new StringBuilder(64);
				sb.append("SELECT MAX(");
				keyField.writeSqlExpression(sb, databaseProductName, null);
				sb.append(") FROM ");
				table.writeSqlExpression(sb, databaseProductName, null);
				if (LOG_SQL) {
					Log.trace(sb);
				}
				try (PreparedStatement ps = con.prepareStatement(sb.toString())) {
					try (ResultSet rs = ps.executeQuery()) {
						if (!rs.next()) {
							throw new SQLException("Could not retrieve max keyfield value");
						}
						lastId = rs.getLong(1);
					}
				}
			}

			// делай четыре: проставляем примерные айди
			ListIterator<? extends RefDbDataObject> iterator = tables.listIterator(tables.size());
			while (iterator.hasPrevious()) {
				iterator.previous().idField().setValue(lastId--);
			}

			// въебали говна!
			if (LOG_SQL) {
				Log.trace("Oracle Workaround Mode: Commit.");
			}
			con.commit();
			return rowCount;
		} catch (DbException | SQLException e) {
			if (LOG_SQL) {
				Log.trace("Oracle Workaround Mode: Rollback.");
			}
			con.rollback();
			throw e;
		}
	}

	private int insertCommon(Connection con) throws SQLException {
		int rowCount;
		try (PreparedStatement ps = con.prepareStatement(generateSqlExpression(con), new String[]{tables.get(0).idField().getName()})) {
			putValues(ps, 1);
			rowCount = ps.executeUpdate();
			Iterator<? extends RefDbDataObject> it = tables.iterator();
			try (ResultSet rs = ps.getGeneratedKeys()) {
				while (rs.next()) {
					it.next().idField().pickValue(rs, 1);
				}
			}
		}
		return rowCount;
	}

	/**
	 * Выполняет подготовленный инсерт.
	 * <ol>
	 * <li>Сбрасывает значение ключевых полей таблиц, участвующих в инсерте.
	 * <li>Вставляет записи.
	 * <li>Вычитывает возвращённые СУБДой ключи в ключевые поля.
	 * </ol>
	 *
	 * @param con соединение с БД
	 * @return количество вставленных записей зачем-то
	 * @throws SQLException
	 * @throws DbException
	 */
	public int execute(Connection con) throws SQLException, DbException {
// К сожалению, некоторые таблицы сдизайнены жопами, и в качестве ключевого поля они используют чужие ид
//		tables.forEach(table -> table.idField().dropValue());
		int rowCount;
		if (DatabaseProductNames.ORACLE.equals(con.getMetaData().getDatabaseProductName())) {
			rowCount = insertOracle(con);
		} else {
			rowCount = insertCommon(con);
		}
		markValuesCommitted();
		return rowCount;
	}
}
