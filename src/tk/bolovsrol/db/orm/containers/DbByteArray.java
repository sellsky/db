package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StringUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;
import tk.bolovsrol.utils.containers.AbstractValueContainer;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;

public class DbByteArray extends AbstractValueContainer<byte[]> implements DbByteArrayContainer {

    public DbByteArray() {
    }

    public DbByteArray(byte[] value) {
        this.value = value;
    }

    @Override public boolean isValueChanged() {
        return !Arrays.equals(committedValue, value);
    }

    @Override public String valueToString() {
        return value == null ? null : StringUtils.getHexDump(value);
    }

    @Override public void parseValue(String value) throws ValueParsingException {
        if (value == null) {
            dropValue();
        } else {
            try {
                setValue(StringUtils.getBytesForHexDump(value));
            } catch (UnexpectedBehaviourException e) {
                throw new ValueParsingException("Error parsing string as hex dump: " + Spell.get(value), e);
            }
        }
    }

    @Override public String valueToLogString() {
        return ContainerToLogString.forByteArray(committedValue, value);
    }

    @Override public void putValue(PreparedStatement ps, int pos) throws SQLException {
        if (value == null) {
            ps.setNull(pos, Types.BLOB);
        } else {
            Blob blob = ps.getConnection().createBlob();
            blob.setBytes(1, value);
            ps.setBlob(pos, blob);
        }
    }

    @Override public void pickValue(ResultSet rs, int columnIndex) throws SQLException, PickFailedException {
        Blob blob = rs.getBlob(columnIndex);
        value = blob == null ? null : blob.getBytes(1, (int) blob.length());
        valueCommitted();
    }

    @Override public String valueToSqlLogString() {
        return ContainerToSqlLogString.forByteArray(value);
    }

    public static DbByteArrayContainer[] wrap(byte[]... arrays) {
        if (arrays == null) {
            return null;
        }
        DbByteArrayContainer[] result = new DbByteArrayContainer[arrays.length];
        for (int i = 0; i < arrays.length; i++) {
            result[i] = new DbByteArray(arrays[i]);
        }
        return result;
    }

    public static DbByteArrayContainer[] wrap(Collection<byte[]> arrays) {
        if (arrays == null || arrays.isEmpty()) {
            return null;
        }
        DbByteArrayContainer[] result = new DbByteArrayContainer[arrays.size()];
        int i = 0;
        for (byte[] ba : arrays) {
            result[i] = new DbByteArray(ba);
            i++;
        }
        return result;
    }

    @Override public void putValue(Json json) {
        json.set(value);
    }

    @Override public void parseValue(Json json) throws ValueParsingException {
        value = json == null ? null : json.getBinary();
    }
}
