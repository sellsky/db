package tk.bolovsrol.db.orm.processing;

import tk.bolovsrol.db.orm.fields.EnumDbField;
import tk.bolovsrol.db.orm.object.RefDbDataObject;

/**
 * Агент, предоставляющий читателю ДБДО {@link RefDbdoReaderThread} информацию о том, что надо читать.
 *
 * @param <D> класс объекта данных, который надо читать
 * @param <E> класс, содержащий статусы объекта данных
 */
public interface RefDbdoBrowsingAgent<D extends RefDbDataObject, E extends Enum<E>> {
	/**
	 * Возвращает новый пустой DBDO, в который будет записана информация и который отправится дальше на обработку.
	 *
	 * @return новый пустой объект данных
	 */
	D newDbdo();

	/**
	 * Возвращает поле, содержащее статус записи переданного объекта.
	 *
	 * @param object исследуемый объект
	 * @return поле статуса переданного объекта
	 */
	EnumDbField<E> getStatusDataField(D object);

	/** @return статус записей, готовых к вычитыванию */
	E getReadyStatus();

	/** @return статус записей, которые уже обрабатываются, чтобы их снова не считать */
	E getInprogressStatus();

}
