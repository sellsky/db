package tk.bolovsrol.db.orm.sql.statements.insert;

import tk.bolovsrol.db.DatabaseProductNames;
import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;
import tk.bolovsrol.db.orm.sql.statements.AbstractSqlStatement;
import tk.bolovsrol.db.orm.sql.statements.update.UpdateColumns;
import tk.bolovsrol.db.orm.sql.updatecolumns.UpdateColumnWithValue;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.IfHasValueThenValues;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.IfNullThenValues;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.InsertOrUpdateColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.Values;
import tk.bolovsrol.utils.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Инсерт, позволяющий гибко задавать вставляемые колонки (их роль выполняют поля {@link DbDataField}, что позволяет
 * при необходимости использовать и их значения), произвольно задавать вставляемые ряды значений, а также определять действия
 * для обновления существующих записей с дублирующимся уникальным ключом.
 * <p>
 * Этот объект, являясь по сути билдером, возвращается под разными интерфейсами, каждый из которых предполагает
 * инициализацию определённых полей, дабы уменьшить количество рантайм-ошибок.
 *
 * @see Insert
 */
class VersatileInsertOrUpdate extends AbstractSqlStatement implements ColumnlessInsertOrUpdate, InsertOrUpdate, RowlessInsertOrUpdate {
	/** Таблица, в которую инсертим. */
	private final DbDataObject table;

	/** Колонки, в которые пишем значения. */
	private Collection<? extends DbDataField<?, ?>> columns;
	/** Ряды значений, которые пишем в колонки. */
	private final List<Collection<? extends DbValueContainer<?>>> valueRows = new ArrayList<>();
	/** Колонки, которые будут обновлены при срабатывании уникального ключа. Если тут не нул, то мы делаем ON DUPLICATE KEY UPDATE. */
	private UpdateColumns<InsertOrUpdateColumn<?>> updateColumns;

	/**
	 * Создаёт инсерт для указанной таблицы.
	 *
	 * @param table куда инсертим
	 */
	VersatileInsertOrUpdate(DbDataObject table) {
		this.table = table;
		this.columns = table.fields();
	}

	//-------- генератор

	@Override
	public void writeSqlExpression(StringBuilder sb, String databaseProductName) throws DbException, SQLException {
		if (valueRows.size() > 1 && DatabaseProductNames.ORACLE.equals(databaseProductName)) {
			// insert all into TABLE (col1,col2,col3) values (val1a,val2a,val3a) into TABLE(col1,col2,col3) values (val1b,val2b,val3b) ... SELECT * from dual
			String valuesAndQuestions = "VALUES(" + StringUtils.copies("?", ",", columns.size()) + ')';
			sb.append("INSERT ALL ");
			for (int i = valueRows.size(); i > 0; i--) {
				sb.append("INTO ");
				table.writeSqlExpression(sb, databaseProductName, null);
				sb.append('(');
				for (DbDataField<?, ?> column : columns) {
					column.writeSqlExpression(sb, databaseProductName, null);
					sb.append(',');
				}
				sb.setCharAt(sb.length() - 1, ')');
				sb.append(valuesAndQuestions);
			}


			StringBuilder ib = new StringBuilder(256);
			ib.append(" INTO ");
			table.writeSqlExpression(ib, databaseProductName, null);
			ib.append('(');
			for (DbDataField<?, ?> field : columns) {
				field.writeSqlExpression(ib, databaseProductName, null);
				ib.append(',');
			}
			ib.setCharAt(ib.length() - 1, ')');
			ib.append(" VALUES(").append(StringUtils.copies("?", ",", columns.size())).append(')');

			sb.append("INSERT ALL");
			StringUtils.appendCopiesTo(sb, ib, columns.size());
			sb.append(" SELECT * FROM DUAL");

		} else {
			// insert into TABLE (col1,col2,col3) values (val1a,val2a,val3a),(val1b,val2b,val3b),...
			sb.append("INSERT INTO ");
			table.writeSqlExpression(sb, databaseProductName, null);
			sb.append('(');
			for (DbDataField<?, ?> column : columns) {
				column.writeSqlExpression(sb, databaseProductName, null);
				sb.append(',');
			}
			sb.setCharAt(sb.length() - 1, ')');
			sb.append(" VALUES");
			StringUtils.appendCopiesTo(sb, '(' + StringUtils.copies("?", ",", columns.size()) + ')', ",", valueRows.size());
		}

		if (updateColumns != null) {
			if (databaseProductName.equals(DatabaseProductNames.MYSQL)) {
				sb.append(" ON DUPLICATE KEY UPDATE ");
				updateColumns.writeSqlExpression(sb, databaseProductName, null);
			} else {
				throw new DbException("Unsupported instruction ON DUPLICATE KEY UPDATE for " + databaseProductName);
			}
		}
	}

	public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
		for (Collection<? extends DbValueContainer> containers : valueRows) {
			for (DbValueContainer container : containers) {
				container.putValue(ps, pos);
				pos++;
			}
		}
		if (updateColumns != null) { pos = updateColumns.putValues(ps, pos); }
		return pos;
	}

	@Override
	protected void appendSqlLogValues(List<String> values) {
		for (Collection<? extends DbValueContainer> containers : valueRows) {
			for (DbValueContainer container : containers) {
				values.add(container.valueToSqlLogString());
			}
		}
		if (updateColumns != null) { updateColumns.appendSqlLogValues(values); }
	}

	private void markValuesCommitted() {
		for (Collection<? extends DbValueContainer> containers : valueRows) {
			for (DbValueContainer container : containers) {
				container.valueCommitted();
			}
		}
		if (updateColumns != null) { updateColumns.committed(); }
	}

	@Override public int execute(Connection con) throws SQLException, DbException {
		if (valueRows.isEmpty()) {
			throw new DbException("INSERT statement has no value rows");
		}
		int rowCount;
		// если в таблице есть ид-колонка, и её значение у нас нул, вычитаем её
		String[] keys = table instanceof RefDbDataObject && ((RefDbDataObject) table).idField().isValueNull() ? new String[]{((RefDbDataObject) table).idField().getName()} : null;
		PreparedStatement ps = con.prepareStatement(generateSqlExpression(con), keys);
		try {
			putValues(ps, 1);
			rowCount = ps.executeUpdate();
			if (keys != null) { // то есть, если есть ключ
				try (ResultSet rs = ps.getGeneratedKeys()) {
					while (rs.next()) {
						((RefDbDataObject) table).idField().pickValue(rs, 1);
					}
				}
			}

		} finally {
			JDBCUtils.close(ps);
		}
		markValuesCommitted();
		return rowCount;
	}

	// -------- всякие сеттеры

	@Override public InsertOrUpdate columns(Collection<? extends DbDataField<?, ?>> fields) {
		this.columns = fields;
		return this;
	}

	@Override public InsertOrUpdate columns(DbDataField<?, ?>... fields) {
		this.columns = Arrays.asList(fields);
		return this;
	}

	@Override public InsertOrUpdate column(DbDataField<?, ?> field) {
		this.columns = Collections.singletonList(field);
		return this;
	}

	@Override public RowlessInsertOrUpdate allColumns() {
		columns(table.fields());
		return this;
	}

	/**
	 * Проверяем, что количество колонок и размер переданных значений совпадают.
	 *
	 * @param valuesCount
	 */
	private void checkValuesQuantity(int valuesCount) {
		if (columns.size() != valuesCount) {
			throw new IllegalArgumentException("Insert Statement has different column count");
		}
	}

    @Override public InsertOrUpdate valueContainerRow(DbValueContainer<?>... values) {
        checkValuesQuantity(values.length);
        return valueContainerRow(Arrays.asList(values));
    }

    @Override @SuppressWarnings("unchecked") public InsertOrUpdate valueRow(Object... values) { // не варарг, потому что оно пожрёт любой неверный аргумент тогда
        checkValuesQuantity(values.length);
        List<DbValueContainer<?>> valueContainers = new ArrayList<>(values.length + 2);
		int i = 0;
		for (DbDataField column : columns) {
			valueContainers.add(column.wrap(values[i]));
			i++;
		}
        return valueContainerRow(valueContainers);
    }

    @Override public InsertOrUpdate valueContainerRow(Collection<? extends DbValueContainer<?>> values) {
        checkValuesQuantity(values.size());
        valueRows.add(values);
		return this;
	}

	@Override public InsertOrUpdate valueRowFromColumns() {
		valueRows.add(columns);
		return this;
	}

    @Override public <D extends DbDataObject> InsertOrUpdate valueRowsFrom(Collection<D> dbdos, Function<D, Collection<? extends DbValueContainer<?>>> mapper) {
        for (D dbdo : dbdos) {
            valueContainerRow(mapper.apply(dbdo));
        }
        return this;
    }

	@Override public InsertOrUpdate valuesAddedLater() {
		return this;
	}

    @Override public InsertOrUpdate clearValues() {
        valueRows.clear();
        return this;
    }

    @Override public int getValueRowCount() {
        return valueRows.size();
    }

    @Override public boolean hasValueRows() {
        return !valueRows.isEmpty();
    }

    @Override public boolean hasUpdateColumns() {
        return updateColumns != null;
    }

	@Override public InsertOrUpdate dropUpdateColumns() {
		updateColumns = null;
		return this;
	}

	private UpdateColumns<InsertOrUpdateColumn<?>> getOrSpawnUpdateColumns() {
		if (updateColumns == null) {
			updateColumns = new UpdateColumns<>();
		}
		return updateColumns;
	}

	@Override public InsertOrUpdate orUpdate(InsertOrUpdateColumn<?> updateColumn) {
		getOrSpawnUpdateColumns().add(updateColumn);
		return this;
	}

	@Override public <V, C extends DbValueContainer<V>> VersatileInsertOrUpdate orUpdate(DbDataField<V, C> field, V value) {
		getOrSpawnUpdateColumns().add(new UpdateColumnWithValue<>(field, value));
		return this;
	}

	@Override public <V> VersatileInsertOrUpdate orUpdate(DbColumn<V> field, DbValueContainer<V> value) {
		getOrSpawnUpdateColumns().add(new UpdateColumnWithValue<>(field, value));
		return this;
	}

	@Override public InsertOrUpdate orUpdate(InsertOrUpdateColumn... updateColumns) {
		if (updateColumns.length > 0) {
			getOrSpawnUpdateColumns().add(updateColumns);
		}
		return this;
	}

	@Override public InsertOrUpdate orUpdate(Collection<? extends InsertOrUpdateColumn<?>> updateColumns) {
		if (!updateColumns.isEmpty()) {
			getOrSpawnUpdateColumns().add(updateColumns);
		}
		return this;
	}

	@Override public final VersatileInsertOrUpdate orUpdateWithValues(DbColumn<?>... columns) {
		if (columns.length > 0) {
            getOrSpawnUpdateColumns().add(Values.wrap(columns));
        }
		return this;
	}

	@Override public InsertOrUpdate orUpdateEveryColumnWithValues() {
		if (!columns.isEmpty()) {
			UpdateColumns<InsertOrUpdateColumn<?>> updateColumns = getOrSpawnUpdateColumns();
			for (DbDataField<?, ?> column : columns) {
                updateColumns.add(new Values<>(column));
            }
		}
		return this;
	}

	@Override public InsertOrUpdate orUpdateIfNull(DbColumn<?> column) {
        getOrSpawnUpdateColumns().add(IfNullThenValues.wrap(column));
        return this; }

	@Override public InsertOrUpdate orUpdateIfHasValue(DbColumn<?> ... columns) {
		UpdateColumns<InsertOrUpdateColumn<?>> target = getOrSpawnUpdateColumns();
		for (DbColumn<?> column : columns) target.add(IfHasValueThenValues.wrap(column));
		return this; }
}


