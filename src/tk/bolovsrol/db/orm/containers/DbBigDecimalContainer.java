package tk.bolovsrol.db.orm.containers;

import tk.bolovsrol.utils.containers.BigDecimalContainer;

import java.math.BigDecimal;

public interface DbBigDecimalContainer extends BigDecimalContainer, DbNumberContainer<BigDecimal> {

    /** @return количество знаков в десятичной части у поля в БД. */
    int getDbScale();

}
