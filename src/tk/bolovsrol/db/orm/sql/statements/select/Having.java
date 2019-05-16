package tk.bolovsrol.db.orm.sql.statements.select;

import tk.bolovsrol.db.orm.sql.conditions.Condition;
import tk.bolovsrol.db.orm.sql.containers.ListingSqlExpressionContainer;

import java.util.Collection;

/**
 * Контейнер-итератор, который хранит перечень условий и собирает их,
 * вставляя определённые префиксы, инфиксы и суффиксы.
 */
class Having extends ListingSqlExpressionContainer<Condition> {

    public Having() {
        super(" HAVING ", " AND ", null, true);
    }

    public Having(Condition condition) {
        this();
        super.addEntity(condition);
    }

    public Having(Condition[] conditions) {
        this();
        super.addEntities(conditions);
    }

    public Having(Collection<? extends Condition> conditions) {
        this();
        super.addEntities(conditions);
    }

    public Having add(Condition condition) {
        super.addEntity(condition);
        return this;
    }

    public Having add(Condition[] conditions) {
        super.addEntities(conditions);
        return this;
    }

    public Having add(Collection<? extends Condition> conditions) {
        super.addEntities(conditions);
        return this;
    }

}
