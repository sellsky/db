package tk.bolovsrol.db.orm.sql.conditions;

import tk.bolovsrol.db.orm.sql.containers.ListingSqlExpressionContainer;

import java.util.Collection;

/**
 * Склеивает условия через AND.
 * <p>
 * Для верхнего уровня это не обязательно, можно просто добавлять условия в where(),
 * они будут склеиваться через AND. Этот класс нужен для склеивания условий
 * внутри or()-групп.
 *
 * @see Condition#and(Condition)
 * @see Or
 * @see Condition#or(Condition)
 */
public class And extends ListingSqlExpressionContainer<Condition> implements Condition {

    public And() {
        super("(", " AND ", ")", false);
    }

    public And(Condition condition) {
        this();
        add(condition);
    }

    public And(Condition... conditions) {
        this();
        add(conditions);
    }

    public And(Collection<? extends Condition> conditions) {
        this();
        add(conditions);
    }

    public And add(Condition condition) {
        addEntity(condition);
        return this;
    }

    public And add(Condition condition1, Condition condition2) {
        addEntity(condition1);
        addEntity(condition2);
        return this;
    }

    public And add(Condition condition1, Condition condition2, Condition condition3) {
        addEntity(condition1);
        addEntity(condition2);
        addEntity(condition3);
        return this;
    }

    public And add(Condition... conditions) {
        addEntities(conditions);
        return this;
    }

    public And add(Collection<? extends Condition> conditions) {
        addEntities(conditions);
        return this;
    }

    /**
     * Добавляет условие в текущую AND-группу.
     *
     * @param condition следующее условие
     * @return this
     * @see #or(Condition)
     */
    @Override public Condition and(Condition condition) {
        add(condition);
        return this;
    }

    /**
     * Добавляет условия из переданного And к своим условиям:
     * <code>A AND (B AND C) → A AND B AND C</code>.
     *
     * @param and
     * @return this
     */
    public Condition and(And and) {
        add(and.getEntities());
        return this;
    }
}
