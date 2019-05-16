package tk.bolovsrol.db.orm.sql.statements;

import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.containers.ListingSqlExpressionContainer;

import java.util.Collection;

/**
 * Контейнер-итератор, который хранит перечень условий и собирает их,
 * вставляя определённые префиксы, инфиксы и суффиксы.
 */
public class Where extends ListingSqlExpressionContainer<Condition> {

    public Where() {
        super(" WHERE ", " AND ", null, true);
    }

    public Where(Condition condition) {
        this();
        addEntity(condition);
    }

    public Where(Condition... conditions) {
        this();
        addEntities(conditions);
    }

    public Where(Collection<? extends Condition> conditions) {
        this();
        addEntities(conditions);
    }

    public Where add(Condition condition) {
        addEntity(condition);
        return this;
    }

    public Where add(Condition... conditions) {
        addEntities(conditions);
        return this;
    }

    public Where add(Collection<? extends Condition> conditions) {
        addEntities(conditions);
        return this;
    }

}
