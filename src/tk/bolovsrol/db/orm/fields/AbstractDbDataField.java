package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.containers.ObjectCopyException;
import tk.bolovsrol.utils.containers.ValueContainer;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Поле, отражаемое в таблице базы данных. */
public abstract class AbstractDbDataField<V, C extends DbValueContainer<V>> implements DbDataField<V, C> {

    /** Владелец поля */
    protected final DbDataObject owner;

    /** Имя поля */
    protected final String name;

    /** Значение поля. */
    protected final C container;

    /**
     * Создаёт поле.
     * <p>
     * Поле можно спрятать, передав третьим параметром true. Это значит, что поле будет доступно при непосредственном обращении,
     * но в массовых операциях вроде вычитвания всех полей БД-объекта из базы это поле участвовать не будет.
     * Так мы достаточно дёшево можем избавиться от необходимости держать в БД всякие специальные поля в тех инсталляциях, где этих полей не надо.
     *
     * @param owner таблица, которой пренадлежит поле
     * @param name название поля
     * @param register (true) регистрировать поле в БД-объекте, чтобы оно присутствовало в селектах, или (false) спрятать его от БД-объекта
     * @param container
     */
    protected AbstractDbDataField(DbDataObject owner, String name, boolean register, C container) {
        this.owner = owner;
        this.name = name;
        this.container = container;
        if (register) {
            owner.registerField(this);
        }
    }

    /**
     * Создаёт поле.
     *
     * @param owner таблица, которой пренадлежит поле
     * @param name название поля
     * @param container
     */
    protected AbstractDbDataField(DbDataObject owner, String name, C container) {
        this(owner, name, true, container);
    }

    @Override public String getName() {
        return name;
    }

    @Override
    public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException {
        if (tableAliases != null) {
            sb.append(tableAliases.get(owner));
            sb.append('.');
        }
		sb.append('\"').append(name).append('\"');
	}

    @Override public void writeSqlExpressionForSelect(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException {
        writeSqlExpression(sb, databaseProductName, tableAliases);
    }

    @Override public int putValuesForSelect(PreparedStatement ps, int pos) throws DbException {
        return pos;
    }

    @Override public void appendSqlLogValuesForSelect(List<String> list) throws DbException {
    }

    @Override public int pickValuesForSelect(ResultSet rs, int pos) throws SQLException, DbException {
        pickValue(rs, pos);
        return pos + 1;
    }

    @Override
    public void writeSqlExpressionForUpdate(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException {
        writeSqlExpression(sb, databaseProductName, tableAliases);
        sb.append("=?");
    }

    @Override public int putValuesForUpdate(PreparedStatement ps, int pos) throws SQLException, DbException {
        putValue(ps, pos);
        return pos + 1;
    }

    @Override public void appendSqlLogValuesForUpdate(List<String> list) throws DbException { list.add(valueToSqlLogString()); }

    @Override public void valueCommittedAtUpdate() {container.valueCommitted();}

    @Override
    public String toString() {
        return name;
    }

    /**
     * @throws SQLException может выбрасываться наследниками */
    @Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        try {
            container.pickValue(rs, columnIndex);
        } catch (SQLException e) {
            throw new PickFailedException("Error reading field " + Spell.get(owner.getSqlCatalogName() + '.' + owner.getSqlTableName() + '.' + name), e);
        }
    }

    // --- массивные делегации
    @Override public void putValue(PreparedStatement ps, int pos) throws SQLException {container.putValue(ps, pos);}

    @Override public String valueToSqlLogString() {return container.valueToSqlLogString();}

    @Override public V getValue() {return container.getValue();}

    @Override public void setValue(V value) {container.setValue(value);}

    @Override public void dropValue() {container.dropValue();}

    @Override public boolean isValueNull() {return container.isValueNull();}

    @Override public V getCommittedValue() {return container.getCommittedValue();}

    @Override public void valueCommitted() {container.valueCommitted();}

    @Override public void rollbackValue() {container.rollbackValue();}

    @Override public boolean isValueChanged() {return container.isValueChanged();}

    @Override public String valueToString() {return container.valueToString();}

    @Override public void parseValue(String value) throws ValueParsingException {container.parseValue(value);}

    @Override public String valueToLogString() {return container.valueToLogString();}

    @Override public void copyValueFrom(ValueContainer<V> source) throws ClassCastException, ObjectCopyException {container.copyValueFrom(source);}

    @Override public Class<V> getComponentType() { return container.getComponentType(); }

    @Override public void putValue(Json json) {container.putValue(json);}

    @Override public void parseValue(Json json) throws ValueParsingException {container.parseValue(json);}
}
