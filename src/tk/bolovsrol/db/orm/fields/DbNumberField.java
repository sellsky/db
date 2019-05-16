package tk.bolovsrol.db.orm.fields;

import tk.bolovsrol.db.orm.containers.DbNumberContainer;
import tk.bolovsrol.db.orm.containers.DbValueContainer;
import tk.bolovsrol.db.orm.sql.updatecolumns.ArithmeticUpdateColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.NumericUpdateColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.AddValues;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.NumericInsertOrUpdateColumn;
import tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate.SubtractValues;

/** Поле, содержащее число. */
public interface DbNumberField<V extends Number, C extends DbValueContainer<V>> extends DbDataField<V, C>, DbNumberContainer<V> {

    default NumericUpdateColumn<V> add(V delta) {
        return ArithmeticUpdateColumn.add(this, delta);
    }

    default NumericUpdateColumn<V> sub(V delta) {
        return ArithmeticUpdateColumn.sub(this, delta);
    }

    /**
     * Колонка для update-части оператора INSERT ... ON DUPLICATE KEY UPDATE {@link tk.bolovsrol.db.orm.sql.statements.insert.Insert},
     * она указывает арифметически прибавить к полю существующей записи значение поля соответствующей insert-колонки.
     * <p>
     * Колонка, вероятно, будет работать только в MySQL.
     */
    default NumericInsertOrUpdateColumn<V> addValues() { return new AddValues<>(this);}

    /**
     * Колонка для update-части оператора INSERT ... ON DUPLICATE KEY UPDATE {@link tk.bolovsrol.db.orm.sql.statements.insert.Insert},
     * она указывает арифметически вычесть из значения существующей записи значение соответствующей insert-колонки.
     * <p>
     * Колонка, вероятно, будет работать только в MySQL.
     */
    default NumericInsertOrUpdateColumn<V> subtractValues() { return new SubtractValues<>(this);}


}
