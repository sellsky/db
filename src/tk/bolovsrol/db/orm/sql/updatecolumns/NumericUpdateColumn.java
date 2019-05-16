package tk.bolovsrol.db.orm.sql.updatecolumns;

import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.NumericInsertOrUpdateColumn;

public interface NumericUpdateColumn<V extends Number> extends UpdateColumn<V>, NumericInsertOrUpdateColumn<V> {

    /**
     * @return болванка для самодельной колонки
     * @see NumericConsecutiveUpdateColumn
     */
    static NumericConsecutiveUpdateColumn custom() {
        return new NumericConsecutiveUpdateColumn();
    }

}
