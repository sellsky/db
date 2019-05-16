package tk.bolovsrol.db.orm.sql.dbcolumns;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.containers.ObjectCopyException;
import tk.bolovsrol.utils.containers.ValueContainer;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Колонка-вилка.
 * <p>
 * Записывает выражение колонки, вычитывает значение в контейнер.
 *
 * @see DbColumn#fork(DbValueContainer)
 * @see DbDataField#fork(Object)
 * @see DbDataField#fork()
 */
public class ForkDbColumn<V> implements ValueDbColumn<V> {

    protected final DbColumn<V> column;
    protected final DbValueContainer<V> container;

    /**
     * Создаёт колонку-вилку с указанными колонкой и контейнером
     *
     * @param column колонка, которую записывать
     * @param container контейнер, который читать
     */
    public ForkDbColumn(DbColumn<V> column, DbValueContainer<V> container) {
        this.column = column;
        this.container = container;
    }

    /**
     * Создаёт колонку-вилку, которая будет записывать выржение указанного поля,
     * а вычитывать знаения в специальный созданный контейнер.
     *
     * @param field поле для записи значений и генерации контенйера-приёмника
     */
    public ForkDbColumn(DbDataField<V, ?> field) {
        this.column = field;
        this.container = field.wrap((V) null);
    }

    // записываем колонку
    @Override
    public void writeSqlExpressionForSelect(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        column.writeSqlExpressionForSelect(sb, databaseProductName, tableAliases);
    }

    @Override public int putValuesForSelect(PreparedStatement ps, int pos) throws SQLException, DbException {
        return column.putValuesForSelect(ps, pos);
    }

    @Override public void appendSqlLogValuesForSelect(List<String> list) throws DbException {
        column.appendSqlLogValuesForSelect(list);
    }

    // читаем контенйер
    @Override public int pickValuesForSelect(ResultSet rs, int pos) throws SQLException, DbException {
        container.pickValue(rs, pos);
        return pos + 1;
    }

    // все контейнерные методы делегируем контейнеру
    @Override public V getValue() {return container.getValue();}

    @Override public void setValue(V value) {container.setValue(value);}

    @Override public void dropValue() {container.dropValue();}

    @Override public boolean isValueNull() {return container.isValueNull();}

    @Override public V getCommittedValue() {return container.getCommittedValue();}

    @Override public void valueCommitted() {container.valueCommitted();}

    @Override public void rollbackValue() { container.rollbackValue(); }

    @Override public boolean isValueChanged() {return container.isValueChanged();}

    @Override public String valueToString() {return container.valueToString();}

    @Override public void parseValue(String value) throws ValueParsingException {container.parseValue(value);}

    @Override public String valueToLogString() {return container.valueToLogString();}

    @Override public void copyValueFrom(ValueContainer<V> source) throws ClassCastException, ObjectCopyException {container.copyValueFrom(source);}

    @Override public void putValue(PreparedStatement ps, int pos) throws SQLException {container.putValue(ps, pos);}

    @Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {container.pickValue(rs, columnIndex);}

    @Override public String valueToSqlLogString() {return container.valueToSqlLogString();}

    @Override public Class<V> getComponentType() {return container.getComponentType();}

    @Override public void parseValue(Json json) throws ValueParsingException {container.parseValue(json);}

    @Override public void putValue(Json json) {container.putValue(json);}
}
