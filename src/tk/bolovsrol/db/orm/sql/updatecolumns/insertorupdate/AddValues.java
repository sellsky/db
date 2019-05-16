package tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate;

import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Колонка для update-части оператора INSERT ... ON DUPLICATE KEY UPDATE {@link tk.bolovsrol.db.orm.sql.statements.insert.Insert},
 * она указывает арифметически прибавить к полю существующей записи значение поля соответствующей insert-колонки.
 * <p>
 * Колонка, вероятно, будет работать только в MySQL.
 */
public class AddValues<V extends Number> extends ArithmericActionValues<V> {

    public AddValues(DbColumn<V> column) {
        super(column, "+");
    }

    /**
     * Оборачивает каждую переданную колонку в AddValues().
     *
     * @param columns колонки
     * @return AddValues для каждой колонки
     */
    @SafeVarargs public static List<AddValues<? extends Number>> wrap(
            DbColumn<? extends Number> ... columns) {
        ArrayList<AddValues<? extends Number>> result = new ArrayList<>(columns.length);
        for (DbColumn<? extends Number> column : columns) {
            result.add(new AddValues<>(column));
        }
        return result;
    }

    /**
     * Оборачивает каждую переданную колонку в AddValues().
     *
     * @param columns колонки
     * @return AddValues для каждой колонки
     */
    public static List<AddValues<? extends Number>> wrap(Collection<DbColumn<? extends Number>> columns) {
        ArrayList<AddValues<? extends Number>> result = new ArrayList<>(columns.size());
        for (DbColumn<? extends Number> column : columns) {
            result.add(new AddValues<>(column));
        }
        return result;
    }
}
