package tk.bolovsrol.db.orm.sql.statements.insert;

import tk.bolovsrol.db.orm.object.DbDataObject;
import tk.bolovsrol.db.orm.object.RefDbDataObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Обёртка, через которую можно создавать один из двух инсертов, используемых в системе.
 * <p>
 * Инсерт {@link RefInsert} вставляет один или несколько объектов {@link RefDbDataObject} целиком и вычитывает сгенерированные первичные ключи.
 * <p>
 * Инсерт {@link InsertOrUpdate} позволяет указать произвольное количество колонок, задать им произвольные значения, а также обновить
 * поля существующих записей в случае дублирующихся индексов (ON DUPLICATE KEY UPDATE). Для уменьшения количества ошибок
 * этот инсерт создаётся в три этапа: на первом нужно определить вставляемую таблицу, на втором — набор вставляемых колонок,
 * на третьем этапе нужно определить хотя бы один ряд значений либо указать, что значения будут добавлены позже. После создания
 * опицонально можно добавить дополнительные ряды значений и/или правила обновления дубликатов. (Создаётся единственный инсерт-объект,
 * который на разных этапах возвращается под личиной различных интерфейсов.)
 */
public final class Insert {

	private Insert() {}

	/**
	 * Создаёт инсерт указанного объекта {@link RefDbDataObject}, который остаётся только выполнить.
	 *
	 * @param rdbdo таблица для вставки
	 * @return предварительный инсерт
	 */
	public static RefInsert intoAndLoadId(RefDbDataObject rdbdo) {
		return new RefInsert(rdbdo);
	}

	/**
	 * Создаёт инсерт, который вставляет пачку {@link RefDbDataObject} одного класса одним инсертом.
	 * <p>
	 * Инсерт вычитывает первичные ключи в ключевую колонку объектов.
	 *
	 * @param rdbdos объекты для вставки
	 * @return предварительный инсерт
	 */
    @SafeVarargs public static <T extends RefDbDataObject> RefInsert bunchAndLoadIds(T... rdbdos) {
        return new RefInsert(rdbdos);
	}

	/**
	 * Вставляет пачку {@link RefDbDataObject} одного класса одним инсертом.
	 * <p>
	 * Инсерт вычитывает первичные ключи в ключевую колонку объектов.
	 * <p>
	 * Если переданная коллекция пуста или нул, вернёт 0.
	 *
	 * @param con подключение к БД
	 * @param dbdos объекты для вставки
	 * @param <T> класс объектов
	 * @return количество вставленных записей
	 * @throws SQLException
	 */
	public static <T extends RefDbDataObject> int bunchAndLoadIds(Connection con, List<T> dbdos) throws SQLException {
		if (dbdos == null || dbdos.isEmpty()) {
			return 0;
		}
		return new RefInsert(dbdos).execute(con);
	}

	/**
     * Вставляет пачку {@link RefDbDataObject} одного класса одним инсертом.
     * <p>
     * Инсерт вычитывает первичные ключи в ключевую колонку объектов.
     * <p>
     * Если переданная коллекция пуста или нул, вернёт 0.
     *
     * @param con подключение к БД
     * @param dbdos объекты для вставки
     * @param <T> класс объектов
     * @return количество вставленных записей
     * @throws SQLException
     */
    @SafeVarargs public static <T extends RefDbDataObject> int bunchAndLoadIds(Connection con, T... dbdos) throws SQLException {
        if (dbdos == null || dbdos.length == 0) {
            return 0;
        }
        return new RefInsert(dbdos).execute(con);
    }

    /**
     * Создаёт болванку для инсерта в указанную таблицу.
     * Следующим шагом нужно задать колонки, а затем хотя бы один ряд значений, после чего элемент можно выполнять.
	 *
	 * @param dbdo таблица для вставки
	 * @return предварительный инсерт
	 */
	public static ColumnlessInsertOrUpdate into(DbDataObject dbdo) {
		return new VersatileInsertOrUpdate(dbdo);
	}

	/**
	 * Вставляет или обновляет список {@link DbDataObject} одного класса одним инсертом.
	 * <p>
	 * При срабатывании первичного ключа обновляет все колонки существующей в БД записи значениями,
	 * которые попытались вставить. Короче говоря, ON DUPLICATE KEY UPDATE со всеми полями column=VALUES(column).
	 * <p>
	 * Следует иметь в виду, что поле id также будет добавлено в список values — возможно, это не совсем то, чего нужно достичь..
	 * <p>
	 * Если переданная коллекция пуста или нул, вернёт 0.
	 *
	 * @param con подключение к БД
	 * @param dbdos объекты для вставки или обновления
	 * @param <T> класс объектов
	 * @return количество вставленных записей
	 * @throws SQLException
	 */
	public static <T extends DbDataObject> int bunchOrUpdateAllColumnsWithValues(Connection con, List<T> dbdos) throws SQLException {
		if (dbdos == null || dbdos.isEmpty()) {
			return 0;
		}
        return dbdos.get(0).insertAllColumns().valueRowsFrom(dbdos, DbDataObject::fields).orUpdateEveryColumnWithValues().execute(con);
    }

    /**
     * Создаёт инсерт, который вставляет или обновляет список {@link DbDataObject} одного класса одним инсертом.
	 * <p>
	 * Если переданная коллекция пуста или нул, выкинет {@link IllegalStateException}.
	 *
	 * @param dbdos объекты для вставки или обновления
	 * @param <T> класс объектов
	 * @return инсерт
	 * @throws IllegalStateException
	 */
	public static <T extends DbDataObject> InsertOrUpdate bunch(List<T> dbdos) throws IllegalStateException {
		if (dbdos == null || dbdos.isEmpty()) {
			throw new IllegalStateException("Empty bunch insert");
		}
        return new VersatileInsertOrUpdate(dbdos.get(0)).allColumns().valueRowsFrom(dbdos, DbDataObject::fields);
    }

    /**
     * Создаёт и выполняет инсерт, который вставляет или обновляет список {@link DbDataObject} одного класса одним инсертом.
     * <p>
     * Если переданная коллекция пуста или нул, выкинет {@link IllegalStateException}.
     *
     * @param dbdos объекты для вставки или обновления
     * @param <T> класс объектов
     * @return количество обновлённых строк
     * @throws IllegalStateException
     */
    public static <T extends DbDataObject> int bunch(Connection con, List<T> dbdos) throws IllegalStateException, SQLException {
        return bunch(dbdos).execute(con);
    }

}
