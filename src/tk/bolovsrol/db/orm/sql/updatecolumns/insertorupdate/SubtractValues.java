package tk.bolovsrol.db.orm.sql.updatecolumns.insertorupdate;

import tk.bolovsrol.db.orm.sql.dbcolumns.DbColumn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Колонка для update-части оператора INSERT ... ON DUPLICATE KEY UPDATE {@link tk.bolovsrol.db.orm.sql.statements.insert.Insert},
 * она указывает арифметически вычесть из значения существующей записи значение соответствующей insert-колонки.
 * <p>
 * Колонка, вероятно, будет работать только в MySQL.
 */
public class SubtractValues<V extends Number> extends ArithmericActionValues<V> {

    public SubtractValues(DbColumn<V> column) {
        super(column, "-");
    }

    /**
     * Оборачивает каждую переданную колонку в AddValues().
     *
     * @param columns колонки
     * @return AddValues для каждой колонки
     */
    @SafeVarargs public static List<SubtractValues<? extends Number>> wrap(
        DbColumn<? extends Number>... columns) {
        ArrayList<SubtractValues<? extends Number>> result = new ArrayList<>(columns.length);
        for (DbColumn<? extends Number> column : columns) {
            result.add(new SubtractValues<>(column));
        }
        return result;
    }

    /**
     * Оборачивает каждую переданную колонку в AddValues().
     *
     * @param columns колонки
     * @return AddValues для каждой колонки
     */
    public static List<SubtractValues<? extends Number>> wrap(Collection<DbColumn<? extends Number>> columns) {
        ArrayList<SubtractValues<? extends Number>> result = new ArrayList<>(columns.size());
        for (DbColumn<? extends Number> column : columns) {
            result.add(new SubtractValues<>(column));
        }
        return result;
    }
}
