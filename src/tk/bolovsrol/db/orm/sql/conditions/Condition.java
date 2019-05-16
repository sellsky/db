package tk.bolovsrol.db.orm.sql.conditions;

import tk.bolovsrol.db.orm.sql.PuttingSqlExpression;

/**
 * Условие, которое можно указать, например, в WHERE-выражении.
 *
 * @see ConsecutiveCondition
 */
public interface Condition extends PuttingSqlExpression {

    /**
     * @return болванка для самодельного условия
     * @see ConsecutiveCondition
     */
    static ConsecutiveCondition custom() {
        return new ConsecutiveCondition();
    }

    /**
     * Склеивает нынешнее и следующее условие через AND.
     * <p>
     * Для верхнего уровня это не обязательно, можно просто добавлять условия в where(),
     * они будут склеиваться через AND. Этот метод нужен для склеивания условий
     * внутри or()-групп.
     *
     * @param condition следующее условие
     * @return (текущее условие AND следующее условие)
     * @see And
     * @see #or(Condition)
     * @see Or
     */
    default Condition and(Condition condition) {
        return new And(this, condition);
    }

    /**
     * Склеивает нынешнее и следующее условие через OR.
     *
     * @param condition следующее условие
     * @return (текущее условие OR следующее условие)
     * @see Or
     * @see #and(Condition)
     * @see And
     */
    default Condition or(Condition condition) {
        return new Or(this, condition);
    }

}
