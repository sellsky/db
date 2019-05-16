package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.db.orm.RecordNotFoundException;
import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.ObjectCopyException;
import tk.bolovsrol.utils.containers.ValueContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Deprecated
public class DbReference<T extends RefDbDataObject> extends DbLong implements DbReferenceContainer<T> {
    /** Ссылка на объект. */
    protected T ref;

    public DbReference() {
    }

    @Override public boolean hasReference() {
        return ref != null;
    }

    @Override public T getReference() {
        return ref;
    }

    @Override public void setReference(T reference) {
        this.ref = reference;
    }

    @Override public T dropReference() {
        T result = this.ref;
        this.ref = null;
        return result;
    }

    @Override
    public void setValue(Long value) {
        if (hasReference()) {
            try {
                try (Connection con = ConnectionManager.getConnection()) {
					if (!ref.selectAllColumns().where(ref.idField().eq(value)).load(con)) {
						throw new RecordNotFoundException("No " + ref.getClass().getSimpleName() + " is found by " + ref.idField().getName() + '=' + Spell.get(value));
					}
				}
            } catch (Exception e) {
                throw new BrokenReferenceException("Cannot load " + ref.getSqlTableName() + " by id " + Spell.get(value), e);
            }
        } else {
            super.setValue(value);
        }
    }

    @Override public Long getValue() {
        if (hasReference()) {
            return ref.getId();
        } else {
            return super.getValue();
        }
    }

    @Override
    public int signum() {
        if (hasReference()) {
            return Long.signum(ref.getId().longValue());
        } else {
            return super.signum();
        }
    }

    @Override
    public void putValue(PreparedStatement ps, int pos) throws SQLException {
        if (hasReference()) {
            if (ref.getId() == null) {
                ps.setNull(pos, Types.NUMERIC);
            } else {
                ps.setLong(pos, ref.getId().longValue());
            }
        } else {
            super.putValue(ps, pos);
        }
    }

    @Override
    public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        if (hasReference()) {
            long tmp = rs.getLong(columnIndex);
            if (rs.wasNull()) {
                if (ref.getId() != null) {
                    dropValue();
                }
                committedValue = null;
            } else {
                Long newValue = tmp;
                if (ref.getId() == null || !ref.getId().equals(newValue)) {
                    setValue(newValue);
                }
                committedValue = newValue;
            }
        } else {
            super.pickValue(rs, columnIndex);
        }
    }

    @Override
    public void dropValue() {
        if (hasReference()) {
            try {
                try (Connection con = ConnectionManager.getConnection()) {
                    ref.selectAllColumns().where(ref.idField().eq(null)).load(con);
                }
			} catch (Exception e) {
                throw new BrokenReferenceException("Cannot drop value to referenceable " + Spell.get(ref), e);
            }
        } else {
            super.dropValue();
        }
    }

    @Override
    public boolean isValueNull() {
        return hasReference() ? ref.getId() == null : super.isValueNull();
    }

    @Override
    public void valueCommitted() {
        if (hasReference()) {
            committedValue = ref.getId();
        } else {
            super.valueCommitted();
        }
    }

    @Override public void rollbackValue() {
        setValue(committedValue);
    }

    @Override
    public boolean isValueChanged() {
        if (hasReference()) {
            return committedValue != ref.getId() && (ref.getId() == null || !ref.getId().equals(committedValue));
        } else {
            return super.isValueChanged();
        }
    }

    @Override
    public String valueToString() {
        if (hasReference()) {
            return ref.getId() == null ? null : ref.getId().toString();
        } else {
            return super.valueToString();
        }
    }

    @Override public String valueToLogString() {
        return hasReference() ? ContainerToLogString.forNumber(committedValue, ref.getId()) : super.valueToLogString();
    }

    @Override public String valueToSqlLogString() {
        return hasReference() ? ContainerToSqlLogString.forNumber(ref.getId()) : super.valueToSqlLogString();
    }

    @Override public void copyValueFrom(ValueContainer<Long> source) throws ClassCastException, ObjectCopyException {
        if (hasReference()) {
            if (source instanceof ReferenceContainer && ((ReferenceContainer<?>) source).hasReference()) {
                try {
                    // скопируем ссылаемый объект целиком
                    ((ReferenceContainer<?>) source).getReference().copyTo(ref);
                } catch (Exception e) {
                    // не получилось, скопируем через загрузку
                    copyViaLoad(source);
                }
            } else {
                // должны загружать
                copyViaLoad(source);
            }
            committedValue = source.getCommittedValue();
        } else {
            super.copyValueFrom(source);
        }
    }

    private void copyViaLoad(ValueContainer<Long> source) throws ObjectCopyException {
        try {
            try (Connection con = ConnectionManager.getConnection()) {
                Long id = source.getValue();
				if (!ref.selectAllColumns().where(ref.idField().eq(id)).load(con)) {
					throw new RecordNotFoundException("No " + ref.getClass().getSimpleName() + " is found by " + ref.idField().getName() + '=' + Spell.get(id));
				}
			}
        } catch (Exception e) {
            throw new ObjectCopyException(
                "Cannot copy value " + Spell.get(source.getValue()) +
                    " to referenceable " + ref.getSqlTableName() + " from source " + Spell.get(source), e
            );
        }
    }

}
