package tk.bolovsrol.db.orm.sql.statements;

import tk.bolovsrol.db.orm.sql.DbException;
import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.log.Log;
import tk.bolovsrol.utils.log.LogDome;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Объектное представление SQL-выражения.
 */
public abstract class AbstractSqlStatement implements SqlStatement {

    /** SQL-лог — в установленный файл, либо в лог по умолчанию */
    private static final LogDome SQL_LOG = LogDome.coalesce(LOG_SQL_OUT, Log.getInstance());

    /**
     * Низкоприоритетный выключатель логгирования.
     * SQL-выражение попадёт в лог, если {@link #LOG_SQL} труе && тут тоже труе.
     */
    private boolean allowLogging = LOG_SQL;

    /** @return true, если генерируемые SQL-выражения будут записаны в лог. */
    @Override public boolean isAllowLogging() {
        return allowLogging;
    }

    /**
     * Управляет низкоприоритетным выключателем логгирования.
     * SQL-выражение попадёт в лог, если {@link #LOG_SQL} труе && тут тоже труе.
     */
    @Override public void setAllowLogging(boolean allowLogging) {
        this.allowLogging = LOG_SQL && allowLogging;
    }

    protected AbstractSqlStatement() {
    }

    //-------- генератор

    /**
     * Создаёт и возвращает SQL-выражение в синтаксисе, подходящем для СУБД переданного соединения.
     *
     * @param con соединение, для СУБД которого нужно подготовить SQL-выражение
     * @return SQL-выражение
     * @throws DbException ошибка формирования SQL-выражения для указаного соединения
     * @throws SQLException прочая SQL-ошибка
     */
    @Override public String generateSqlExpression(Connection con) throws SQLException, DbException {
        if (con == null) {
            throw new NullPointerException("No Connection is passed to SQL Statement Generator");
        }
        return generateSqlExpression(con.getMetaData().getDatabaseProductName(), false);
    }


    /**
     * Создаёт и возвращает SQL-выражение в синтаксисе, подходящем для указанной СУБД.
     * Если fillInPlaceholders труе, то вопросики будут заменены на соовтетствующие значения —
     * это для лога и отладки.
     *
     * @param databaseProductName СУБД, для которой нужно подготовить текст
     * @return SQL-выражение
     * @throws DbException ошибка формирования SQL-выражения для указаной СУБД
     * @throws SQLException прочая SQL-ошибка
     */
    @Override public String generateSqlExpression(String databaseProductName, boolean fillInPlaceholders) throws DbException, SQLException {
        StringBuilder sb = new StringBuilder(256);
        writeSqlExpression(sb, databaseProductName);
        String sql = sb.toString();
        if (allowLogging || fillInPlaceholders) {
            String filledSql = fillInPlaceholders(sql);
            if (allowLogging) { SQL_LOG.trace(filledSql); }
            if (fillInPlaceholders) { return filledSql; }
        }
        return sql;
    }

    /**
     * Записывает в предоставленный стринг-билдер SQL-выражение в синтаксисе,
     * подходящем для указанной СУБД.
     *
     * @param sb куда записывать SQL-выражение
     * @param databaseProductName СУБД, для которой нужно подготовить текст
     * @throws DbException ошибка формирования SQL-выражения для указаного соединения
     * @throws SQLException прочая SQL-ошибка
     * @see tk.bolovsrol.db.DatabaseProductNames
     */
    protected abstract void writeSqlExpression(StringBuilder sb, String databaseProductName) throws DbException, SQLException;

    /**
     * Заменяет в переданном SQL-выражении знаки вопроса на значения параметров,
     * стараясь добиться результата, который будет корректным с точки зрения СУБД.
     * <p>
     * Передавать такое выражение на исполнение в JDBC, конечно, нельзя — оно только для логгирования.
     *
     * @param generatedSqlExpression SQL-выражение с вопросиками
     * @return SQL-выражение c параметрами
     */
    protected String fillInPlaceholders(String generatedSqlExpression) {
        int qmPos = generatedSqlExpression.indexOf('?');
        if (qmPos < 0) { return generatedSqlExpression; }

        List<String> values = new ArrayList<>(32);
        appendSqlLogValues(values);
        if (values.isEmpty()) {
            return Spell.get(generatedSqlExpression);
        }

        int valuesIdx = 0;
        int fromPos = 0;
        StringBuilder sb = new StringBuilder(Math.max(256, generatedSqlExpression.length() << 2));
        do {
            sb.append(generatedSqlExpression.substring(fromPos, qmPos));
            sb.append(values.get(valuesIdx++));
            fromPos = qmPos + 1;
            qmPos = generatedSqlExpression.indexOf('?', fromPos);
        } while (qmPos >= 0);
        sb.append(generatedSqlExpression.substring(fromPos));
        return sb.toString();
    }

    /**
     * Добавляет в переданный список строковое представление значений параметров SQL-выражения,
     * тех самых, что обозначаются знаками вопроса.
     * <p>
     * Эти данные используются для отображения SQL-запроса в логе.
     *
     * @param values куда записывать значения
     */
    protected abstract void appendSqlLogValues(List<String> values);

}
