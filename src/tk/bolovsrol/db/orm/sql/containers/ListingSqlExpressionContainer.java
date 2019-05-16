package tk.bolovsrol.db.orm.sql.containers;


import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.PuttingSqlExpression;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Контейнер хранит перечень выражений и умеет перечислять их формируя SQL-выражение,
 * вставляя опционально заданные префикс, инфиксы и суффикс.
 * <p>
 * Нужно унаследовать этот класс, определив методы для добавления сущностей, которые будут вызывать
 * add*-методы этого класса.
 */
public class ListingSqlExpressionContainer<T extends PuttingSqlExpression> implements PuttingSqlExpression {

    protected final List<T> entities = new ArrayList<>();

    private final String prefix;
    private final String infix;
    private final String suffix;
    private final boolean putPrefixAndSuffixForSingleItem;

    public ListingSqlExpressionContainer(String prefix, String infix, String suffix, boolean putPrefixAndSuffixForSingleItem) {
        this.prefix = prefix;
        this.infix = infix;
        this.suffix = suffix;
        this.putPrefixAndSuffixForSingleItem = putPrefixAndSuffixForSingleItem;
    }

    // Вот этим методам можно в наследниках делегировать добавление сущностей, чтобы, например, вернуть this и организовать цепочку.
    protected void addEntity(T entity) {
        entities.add(entity);
    }

    protected void addEntities(T[] entities) {
        this.entities.addAll(Arrays.asList(entities));
    }

    protected void addEntities(Collection<? extends T> entities) {
        this.entities.addAll(entities);
    }

    protected List<T> getEntities() {
        return new ArrayList<>(entities);
    }

    /**
     * Очищает контейнер.
     */
    public void clear() {
        entities.clear();
    }

    public boolean isEmpty() {
        return entities.isEmpty();
    }

    @Override public void writeSqlExpression(StringBuilder sb, String databaseProductName, Map<DbDataObject, String> tableAliases) throws DbException, SQLException {
        if (entities.isEmpty()) { throw new DbException("Cowardly refusing to generate " + getClass().getSimpleName() + " clause with no entities."); }
        int size = entities.size();
        if (size == 1) {
            if (prefix != null && putPrefixAndSuffixForSingleItem) {
                sb.append(prefix);
            }
            entities.get(0).writeSqlExpression(sb, databaseProductName, tableAliases);
            if (suffix != null && putPrefixAndSuffixForSingleItem) {
                sb.append(suffix);
            }
        } else {
            if (prefix != null) {
                sb.append(prefix);
            }
            entities.get(0).writeSqlExpression(sb, databaseProductName, tableAliases);
            for (int i = 1; i < size; i++) {
                if (infix != null) {
                    sb.append(infix);
                }
                entities.get(i).writeSqlExpression(sb, databaseProductName, tableAliases);
            }
            if (suffix != null) {
                sb.append(suffix);
            }
        }
    }

    @Override public int putValues(PreparedStatement ps, int pos) throws SQLException, DbException {
        for (T entity : entities) {
            pos = entity.putValues(ps, pos);
        }
        return pos;
    }

    @Override public void appendSqlLogValues(List<String> list) throws DbException {
        for (T entity : entities) {
            entity.appendSqlLogValues(list);
        }
    }

}
