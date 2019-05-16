package tk.bolovsrol.db.orm.sql.statements.insert;

import tk.bolovsrol.db.orm.fields.BigDecimalDbField;
import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.db.orm.fields.IntegerDbField;
import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.db.orm.sql.updatecolumns.ArithmeticUpdateColumn;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Дельта-апдейт позволяет изменять записи по какому-либо уникальному ключу,
 * не загружая их, при помощи Insert#orUpdate(...).
 * <p>
 * Паразитирует на переданном объекте-хосте, пользуясь его полями в качестве контейнеров.
 * <p>
 * Пользователь наследует этот класс, добавляя удобные публичные дельта-изменятели,
 * которые вызывают методы setColumn, addColumn, incColumn и decColumn, которые записывают
 * нужное значение как в инсерт, так и в апдейт.
 */
public class DeltaUpdate {
    protected final DbDataObject host;
    protected final InsertOrUpdate iu;

    protected DeltaUpdate(DbDataObject host) {
        this.host = host;
		this.iu = Insert.into(host).allColumns().valueRowFromColumns();
	}

    public int execute(Connection con) throws SQLException, DbException {
        return iu.execute(con);
    }

    /**
     * Убирает все апдейт-колонки с дельтами.
     * <p>
     * Содержимое полей хост-объекта не изменяется, о них при необходимости нужно заботиться отдельно.
     */
    public void dropUpdateColumns() {
        iu.dropUpdateColumns();
    }

    @Override public String toString() {
        return iu.toString();
    }


    protected <V> void setColumn(DbDataField<V, ?> column, V newValue) {
        column.setValue(newValue);
        iu.orUpdate(column.with(newValue));
    }

    protected void incColumn(IntegerDbField column) {
        column.setValue(1);
        iu.orUpdate(ArithmeticUpdateColumn.inc(column));
    }

    protected void incColumn(BigDecimalDbField column) {
        column.setValue(BigDecimal.ONE);
        iu.orUpdate(ArithmeticUpdateColumn.inc(column));
    }

    protected void decColumn(IntegerDbField column) {
        column.setValue(-1);
        iu.orUpdate(ArithmeticUpdateColumn.dec(column));
    }

    protected void decColumn(BigDecimalDbField column) {
        column.setValue(BigDecimal.valueOf(-1L));
        iu.orUpdate(ArithmeticUpdateColumn.inc(column));
    }

    protected void addColumn(IntegerDbField column, int delta) {
        if (delta != 0) {
            column.setValue(delta);
            iu.orUpdate(column.add(delta));
        }
    }

    protected void addColumn(BigDecimalDbField column, BigDecimal delta) {
        if (delta.signum() != 0) {
            column.setValue(delta);
            iu.orUpdate(column.add(delta));
        }
    }

}
