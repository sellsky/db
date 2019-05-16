package tk.bolovsrol.db.orm.sql.conditions;

import tk.bolovsrol.db.orm.sql.containers.ListingSqlExpressionContainer;

import java.util.Collection;

/**
 * Склеивает условия через OR.
 *
 * @see Condition#or(Condition)
 */
public class Or extends ListingSqlExpressionContainer<Condition> implements Condition {

    public Or() {
        super("(", " OR ", ")", false);
    }

    public Or(Condition condition) {
        this();
        add(condition);
    }

    public Or(Condition condition1, Condition condition2) {
        this();
        add(condition1);
        add(condition2);
    }

    public Or(Condition condition1, Condition condition2, Condition condition3) {
        this();
        add(condition1);
        add(condition2);
        add(condition3);
    }

    public Or(Condition... conditions) {
        this();
        add(conditions);
    }

    public Or(Collection<? extends Condition> conditions) {
        this();
        add(conditions);
    }

    public Or add(Condition condition) {
        addEntity(condition);
        return this;
    }

    public Or add(Condition... conditions) {
        addEntities(conditions);
        return this;
    }

    public Or add(Collection<? extends Condition> conditions) {
        addEntities(conditions);
        return this;
    }

    /**
     * Добавляет условие в текущую OR-группу.
     *
     * @param condition следующее условие
     * @return this
     * @see #or(Condition)
     */
    @Override public Condition or(Condition condition) {
        add(condition);
        return this;
    }

    /**
     * Добавляет условия из переданного Or к своим условиям:
     * <code>A OR (B OR C) → A OR B OR C</code>.
     *
     * @param or
     * @return this
     */
    public Condition or(Or or) {
        add(or.getEntities());
        return this;
    }
}
