package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.db.orm.containers.DbReference;
import tk.bolovsrol.db.orm.containers.DbReferenceContainer;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.containers.ObjectCopyException;
import tk.bolovsrol.utils.containers.ValueContainer;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Поле -- ссылка на запись в другой таблице с автоматической актуализацией.
 * <p>
 * Объект, на который можно ссылаться, должен реализовать интерфейс RefDbDataObject.
 * <p>
 * Поле может оперировать как с объектом, так и без объекта, просто храня его ID.
 * <p>
 * В первом случае getValue() и setValue() делегируются RefDbDataObject-объекту
 * как getId() и loadById() соответственно, а во втором случае поле ведёт себя
 * аналогично LongDbField.
 * <p>
 * Фишка поля в том, что при загрузке объекта, которому поле принадлежит,
 * автоматически будет обновлён (загружен) объект, на который поле ссылается.
 * <p>
 * Установить/переустановить или сбросить ссылку можно в любой момент методами
 * setReference() и dropReference().
 * <p>
 * При установке ссылки прежнее значение поля теряется. При сбрасывании ссылки
 * поле сохряняет ID объекта.
 *
 * @deprecated лучше использовать {@link LongDbField} и подгружать объекты вручную или использовать кэш, выйдет куда нагляднее.
 */
@Deprecated
public class ReferenceDbField<T extends RefDbDataObject> extends LongDbField implements DbReferenceContainer<T> {

    private final DbReference<T> container = new DbReference<>();

    public ReferenceDbField(DbDataObject owner, String name) {
        super(owner, name);
    }

	// -----

    @Override public T getReference() {
        return container.getReference();
    }

    @Override public void setReference(T reference) {
        super.dropValue();
        container.setReference(reference);
    }

    @Override public boolean hasReference() {
        return container.hasReference();
    }

    @Override public T dropReference() {
        super.dropValue();
        return container.dropReference();
    }

    @Override public void setValue(Long value) {
        if (hasReference()) { container.setValue(value); } else { super.setValue(value); }
    }

    @Override public Long getValue() {
        if (hasReference()) { return container.getValue(); } else { return super.getValue(); }
    }

    @Override public int signum() {
        if (hasReference()) { return container.signum(); } else { return super.signum(); }
    }

    @Override public Long getCommittedValue() {
        if (hasReference()) { return container.getCommittedValue(); } else { return super.getCommittedValue(); }
    }

    @Override public void putValue(PreparedStatement ps, int pos) throws SQLException {
        if (hasReference()) { container.putValue(ps, pos); } else { super.putValue(ps, pos); }
    }

    @Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        if (hasReference()) {
            try {
                container.pickValue(rs, columnIndex);
            } catch (SQLException e) {
                throw new PickFailedException("Error reading field " + Spell.get(name), e);
            }
        } else {
            super.pickValue(rs, columnIndex);
        }
    }

    @Override public void dropValue() {
        if (hasReference()) { container.dropValue(); } else { super.dropValue(); }
    }

    @Override public boolean isValueNull() {
        if (hasReference()) { return container.isValueNull(); } else { return super.isValueNull(); }
    }

    @Override public void valueCommitted() {
        if (hasReference()) { container.valueCommitted(); } else { super.valueCommitted(); }
    }

    @Override public void rollbackValue() {
        if (hasReference()) { container.rollbackValue(); } else { super.rollbackValue(); }
    }

    @Override public void valueCommittedAtUpdate() {
        valueCommitted();
    }

    @Override public boolean isValueChanged() {
        if (hasReference()) { return container.isValueChanged(); } else { return super.isValueChanged(); }
    }

    @Override public String valueToString() {
        if (hasReference()) { return container.valueToString(); } else { return super.valueToString(); }
    }

    @Override public String valueToLogString() {
        if (hasReference()) { return container.valueToLogString(); } else { return super.valueToLogString(); }
    }

    @Override public String valueToSqlLogString() {
        if (hasReference()) { return container.valueToSqlLogString(); } else { return super.valueToSqlLogString(); }
    }

    @Override public void parseValue(String value) throws ValueParsingException {
        if (hasReference()) { container.parseValue(value); } else { super.parseValue(value); }
    }

    @Override public void copyValueFrom(ValueContainer<Long> source) throws ClassCastException, ObjectCopyException {
        if (hasReference()) { container.copyValueFrom(source); } else { super.copyValueFrom(source); }
    }

}
