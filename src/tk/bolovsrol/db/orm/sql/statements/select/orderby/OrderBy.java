package tk.bolovsrol.db.orm.sql.statements.select.orderby;

import tk.bolovsrol.db.orm.sql.containers.ListingSqlExpressionContainer;

import java.util.Collection;

public class OrderBy extends ListingSqlExpressionContainer<OrderByEntity> {

    public OrderBy() {
        super(" ORDER BY ", ",", null, true);
    }

    public void add(OrderByEntity entity) {
        addEntity(entity);
    }

    public void add(OrderByEntity... columns) {
        addEntities(columns);
    }

    public void add(Collection<? extends OrderByEntity> entities) {
        addEntities(entities);
    }
}
