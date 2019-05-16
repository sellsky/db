package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.EnumUtils;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.containers.AbstractValueContainer;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;

/** DbEnumContainer */
public class DbEnum<E extends Enum<E>> extends AbstractValueContainer<E> implements DbEnumContainer<E> {

    private final Class<E> cl;

    public DbEnum(Class<E> cl) {
        this.cl = cl;
    }

    public DbEnum(Class<E> cl, E value) {
        this(cl);
        this.value = value;
    }

    @Override public void putValue(PreparedStatement ps, int pos) throws SQLException {
        if (value == null) {
            ps.setNull(pos, Types.VARCHAR);
        } else {
            ps.setString(pos, value.toString());
        }
    }

    @Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        try {
            String tmp = rs.getString(columnIndex);
            this.value = rs.wasNull() ? null : EnumUtils.pickByNameOrToString(cl, tmp);
            this.committedValue = this.value;
        } catch (UnexpectedBehaviourException e) {
            throw new PickFailedException(e);
        }
    }

    @Override public boolean isValueChanged() { return committedValue != value; }

    @Override public String valueToString() { return value == null ? null : value.toString(); }

    @Override public String valueToLogString() { return ContainerToLogString.forEnum(committedValue, value); }

    @Override public String valueToSqlLogString() {
        return ContainerToSqlLogString.forEnum(value);
    }

    @Override public void parseValue(String value) throws ValueParsingException {
        if (value == null) {
            dropValue();
        } else {
            try {
                setValue(EnumUtils.pickByNameOrToString(cl, value));
            } catch (UnexpectedBehaviourException e) {
                throw new ValueParsingException(e);
            }
        }
    }

    /** @return класс потенциально содержащихся в контейнере енумов */
    @Override public Class<E> getComponentType() {
        return cl;
    }

    @SuppressWarnings({"unchecked"})
    public static <E extends Enum<E>> DbEnumContainer<E>[] wrap(Class<E> cl, E... enums) {
        if (enums == null) {
            return null;
        }
        DbEnumContainer<E>[] result = (DbEnumContainer<E>[]) new DbEnumContainer<?>[enums.length];
        for (int i = 0; i < enums.length; i++) {
            E e = enums[i];
            result[i] = new DbEnum<>(cl, e);
        }
        return result;
    }

    @SuppressWarnings({"unchecked"})
    public static <E extends Enum<E>> DbEnumContainer<E>[] wrap(Class<E> cl, Collection<E> enums) {
        if (enums == null) {
            return null;
        }
        DbEnumContainer<E>[] result = (DbEnumContainer<E>[]) new DbEnumContainer<?>[enums.size()];
        Iterator<E> it = enums.iterator();
        for (int i = 0; i < enums.size(); i++) {
            result[i] = new DbEnum<>(cl, it.next());
        }
        return result;
    }


    @Override public void putValue(Json json) {
        json.set(value);
    }

    @Override public void parseValue(Json json) throws ValueParsingException {
        value = json == null ? null : json.getEnum(cl);
    }
}
