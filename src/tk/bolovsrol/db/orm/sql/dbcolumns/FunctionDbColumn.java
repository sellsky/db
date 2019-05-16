package tk.bolovsrol.db.orm.sql.dbcolumns;

/**
 * Колонка — функция с параметрами в скобках через запятую
 *
 * @param <V>
 */
public class FunctionDbColumn<V> extends ListingDbColumn<V> {

    public FunctionDbColumn(String name) {
        super(name + '(', ",", ")");
    }
}
