package tk.bolovsrol.db.orm.refgen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Где хранить табличку для отображения интерфейса */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface DbMeta {
    /**
     * SQL-каталог, в котором лежит БД-объект.
	 *
	 * Если не указан, будет использован каталог по умолчанию: tk.bolovsrol.core.Core.CATALOG.
	 *
	 * @return каталог
	 */
	String sqlCatalog() default "";

    /**
     * SQL-таблица, в которой содержатся данные БД-объекта.
     * По умолчанию используется имя класса с приделанной "s" на конце, если там её ещё нет.
     *
     * @return таблица
     */
    String sqlTable() default "";

    /**
     * Название поля с первичным ключом. По умолчанию «id». Иное имя ключевого поля встречается у старых таблиц.
     *
     * @return имя ключевого поля
     */
    String keyField() default "";

    /** @return количество разрядов дробной части полей типа BigDecimal в БД. */
    int bigDecimalScale() default 0;
}
