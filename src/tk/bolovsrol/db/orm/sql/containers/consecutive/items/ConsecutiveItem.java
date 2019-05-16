package tk.bolovsrol.db.orm.sql.containers.consecutive.items;

import tk.bolovsrol.db.orm.sql.PuttingSqlExpression;

/**
 * Звено в цепочке для последовательных собирателей.
 */
public interface ConsecutiveItem extends PuttingSqlExpression {

    /**
     * Для изменений, сохраняющих значения снаружи, вызов этого метода означает,
     * что значение итема сохранено.
     */
    void committed();
}
