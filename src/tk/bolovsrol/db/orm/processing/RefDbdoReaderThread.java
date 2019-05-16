package tk.bolovsrol.db.orm.processing;

import tk.bolovsrol.db.JDBCUtils;
import tk.bolovsrol.db.orm.object.RefDbDataObject;
import tk.bolovsrol.db.orm.sql.statements.select.Select;
import tk.bolovsrol.db.orm.sql.statements.update.Update;
import tk.bolovsrol.db.pool.CommitMode;
import tk.bolovsrol.db.pool.ConnectionManager;
import tk.bolovsrol.utils.BlockingStack;
import tk.bolovsrol.utils.QuitException;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.LogDome;
import tk.bolovsrol.utils.syncro.VersionParking;
import tk.bolovsrol.utils.threads.HaltableThread;
import tk.bolovsrol.utils.time.Duration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Считывает из базы все новые объекты и помещает их в {@link BlockingStack}.
 *
 * @param <D> класс объекта данных, который надо читать
 * @param <E> класс, содержащий статусы объекта данных
 */
public class RefDbdoReaderThread<D extends RefDbDataObject, E extends Enum<E>> extends HaltableThread {

    protected final LogDome log;
    private final BlockingStack<D> stack;
	private final int haltStackSizeThreshold;
	private final VersionParking insertNotifier;
	private final Duration idleSleepOrNull;
	private final Duration errorSleep;
	private final RefDbdoBrowsingAgent<D, E> agent;

    private final ArrayList<D> objects;
    private final ArrayList<Long> ids;

    private final D object;
    private final Select objectSelectNew;
    private final Update objectUpdateStatus;

	/**
	 * @param name название треда-читателя для тред-дампов
	 * @param log лог
	 * @param stack стек, в который будут отправляться вычитанные объекты
	 * @param haltStackSizeThreshold количество элементов в стеке, по превышении которого нужно приостановить его пополнение
	 * @param agent агент с информацией о конкретных объекте и статусах
	 * @param insertNotifierOrNull флажок, который поднимают при записи в таблицу, или нул, если в текущей джава-машине запись не производится, и надо периодически сканировать таблицу
	 * @param idleSleepOrNull интервал между периодическими сканированиями или нул, если используется insertNotifier
	 * @param errorSleep время спячки после ошибок
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public RefDbdoReaderThread(String name, LogDome log, BlockingStack<D> stack, int haltStackSizeThreshold, RefDbdoBrowsingAgent<D, E> agent, VersionParking insertNotifierOrNull, Duration idleSleepOrNull, Duration errorSleep) throws SQLException, InterruptedException {
		super(name);
		this.log = log;
		this.stack = stack;
		this.haltStackSizeThreshold = haltStackSizeThreshold;
		if (insertNotifierOrNull == null && idleSleepOrNull == null) { throw new IllegalArgumentException("At leas one of insertNotifierOrNull or idleSleepOrNull should be not null"); }
		this.insertNotifier = insertNotifierOrNull == null ? new VersionParking() : insertNotifierOrNull;
		this.idleSleepOrNull = idleSleepOrNull;
		this.errorSleep = errorSleep;
		this.agent = agent;
		this.objects = new ArrayList<>(haltStackSizeThreshold);
		this.ids = new ArrayList<>(haltStackSizeThreshold);

		this.object = agent.newDbdo();
		this.objectSelectNew = object.selectAllColumns()
			.where(agent.getStatusDataField(object).eq(agent.getReadyStatus()))
			.orderBy(object.idField())
			.limit(haltStackSizeThreshold);

		this.objectUpdateStatus = object.update(agent.getStatusDataField(object).with(agent.getInprogressStatus()));

        Connection con = ConnectionManager.getConnection();
		try {
			int count = object
				.update(agent.getStatusDataField(object).with(agent.getReadyStatus()))
				.where(agent.getStatusDataField(object).eq(agent.getInprogressStatus()))
				.execute(con);
			if (count != 0) {
				log.hint(String.valueOf(count) + ' ' + object.getSqlTableName() + " record(s) reset from status "
					+ agent.getInprogressStatus() + " to " + agent.getReadyStatus());
			}
		} finally {
			JDBCUtils.close(con);
		}
	}

	@Override public void run() {
		try {
			while (!isInterrupted()) {
				try {
					stack.waitForTrim(haltStackSizeThreshold);
				} catch (InterruptedException e) {
					throw new QuitException(e);
				}

				try {
					objects.clear();
					ids.clear();

                    int version = insertNotifier.getVersion();
                    Connection con = ConnectionManager.getConnection(CommitMode.MANUAL);
                    try {
                        objectSelectNew.browse(con, () -> {
                            objects.add(object.copyTo(agent.newDbdo()));
                            ids.add(object.getId());
                        });

                        if (!ids.isEmpty()) {
                            objectUpdateStatus
                                .clearWhere()
								.where(object.idField().in(ids))
								.execute(con);
						}
					} finally {
						con.commit();
						JDBCUtils.close(con);
					}

                    if (objects.isEmpty()) {
                        delay(version);
                    } else {
                        stack.addAll(objects);
                    }

                } catch (SQLException e) {
                    log.warning(e);
                    log.info("Sleeping before retry for " + Spell.get(errorSleep));
                    Thread.sleep(errorSleep.getMillis());
                }
            }
        } catch (InterruptedException e) {
			log.warning(e);
		} catch (QuitException e) {
			log.trace(e);
		}
	}

	protected void delay(int version) throws QuitException {
		try {
			if (idleSleepOrNull == null) {
				insertNotifier.park(version);
			} else {
				insertNotifier.parkDuration(version, idleSleepOrNull);
			}
		} catch (InterruptedException e) {
			throw new QuitException(e);
		}
	}
}
