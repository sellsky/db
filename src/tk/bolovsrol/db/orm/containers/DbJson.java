package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.db.orm.PickFailedException;
import tk.bolovsrol.utils.Json;
import tk.bolovsrol.utils.JsonParsingException;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.containers.AbstractValueContainer;
import tk.bolovsrol.utils.containers.ContainerToLogString;
import tk.bolovsrol.utils.containers.ContainerToSqlLogString;
import tk.bolovsrol.utils.containers.ValueParsingException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

public class DbJson extends AbstractValueContainer<Json> implements DbJsonContainer {

    public DbJson() {
    }

    public DbJson(Json value) {
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
        String jsonStr = rs.getString(columnIndex);
        try {
            this.value = Json.parse(jsonStr);
        } catch (JsonParsingException e) {
            throw new PickFailedException("Error reading json " + Spell.get(jsonStr), e);
        }
        this.committedValue = this.value;
    }

    @Override public String valueToString() {
        return value.toString();
    }

    @Override public String valueToLogString() {
        return ContainerToLogString.forJson(committedValue, value);
    }

    @Override public String valueToSqlLogString() {
        return ContainerToSqlLogString.forJson(value);
    }

    @Override public void parseValue(String value) throws ValueParsingException {
        try {
            setValue(Json.parse(value));
        } catch (JsonParsingException e) {
            throw new ValueParsingException("Error parsing string as Json " + Spell.get(value), e);
        }
    }

    public static DbJsonContainer[] wrap(Json... jsons) {
        if (jsons == null) {
            return null;
        }
        DbJsonContainer[] result = new DbJsonContainer[jsons.length];
        for (int i = 0; i < jsons.length; i++) {
            result[i] = new DbJson(jsons[i]);
        }
        return result;
    }

    public static DbJsonContainer[] wrap(Collection<Json> jsons) {
        if (jsons == null || jsons.isEmpty()) {
            return null;
        }
        DbJsonContainer[] result = new DbJsonContainer[jsons.size()];
        int i = 0;
        for (Json l : jsons) {
            result[i] = new DbJson(l);
            i++;
        }
        return result;
    }

    @Override public void putValue(Json json) {
        if (this.value == null) { json.drop(); } else { json.copyFrom(this.value); }
    }

    @Override public void parseValue(Json json) throws ValueParsingException {
        this.value = json;
    }
}
