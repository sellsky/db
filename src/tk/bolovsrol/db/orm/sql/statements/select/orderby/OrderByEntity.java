package tk.bolovsrol.db.orm.sql.statements.select.orderby;

import tk.bolovsrol.db.orm.sql.PuttingSqlExpression;

public interface OrderByEntity extends PuttingSqlExpression {

    /**
     * @return болванка для самодельной директивы сортировки
     * @see ConsecutiveOrderByEntity
     */
    static ConsecutiveOrderByEntity custom() {
        return new ConsecutiveOrderByEntity();
    }

    static ConsecutiveOrderByEntity custom(Direction direction) {
        return new ConsecutiveOrderByEntity(direction);
    }

}
