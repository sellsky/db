package tk.bolovsrol.db.orm.object;

import tk.bolovsrol.db.orm.fields.DbDataField;
import tk.bolovsrol.utils.containers.MillisContainer;
import tk.bolovsrol.utils.textformatter.compiling.KeywordEvaluator;
import tk.bolovsrol.utils.textformatter.compiling.TextFormatCompiler;
import tk.bolovsrol.utils.textformatter.compiling.evaluators.DescedantEvaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Решает переданные макросы как имена полей
 * переданного в конструкторе {@link DbDataObject}-а.
 * <p>
 * Синтаксис:<br/>
 * <code>[[каталог.]таблица.]поле</code><br/>
 * — если указанный каталог и таблица не соответствуют
 * переданному объекту, решатель вернёт нул. Таким образом
 * можно {@link DescedantEvaluator сцеплять} несколько ДБДО
 * и получать доступ к каждому из них.
 * <p>
 * Если дополнительные параметры (каталог и таблица) не указаны,
 * будет искать поле в переданном ДБДО. Если поля нет, вернётся нул.
 *
 * @see TextFormatCompiler
 * @see tk.bolovsrol.utils.textformatter.compiling.ProxyingCompiledFormatter
 */
public class DbDataObjectKeywordEvaluator implements KeywordEvaluator {

    public static final char FIELD_DELIMITER = '.';

    private final Map<String, DbDataField<?, ?>> fieldMap;

    public DbDataObjectKeywordEvaluator(Map<String, DbDataField<?, ?>> fieldMap) {
        this.fieldMap = fieldMap;
    }

    public DbDataObjectKeywordEvaluator(DbDataObject dbdo) {
        String tableName = dbdo.getSqlTableName() + FIELD_DELIMITER;
        String catalogTableName = dbdo.getSqlCatalogName() + FIELD_DELIMITER + tableName;
        List<DbDataField<?, ?>> fields = dbdo.fields();
        this.fieldMap = new HashMap<>(fields.size() * 3);
        for (DbDataField field : fields) {
            fieldMap.put(field.getName(), field);
            fieldMap.put(tableName + field.getName(), field);
            fieldMap.put(catalogTableName + field.getName(), field);
        }
    }

    @Override public String evaluate(String keyword) {
        DbDataField field = fieldMap.get(keyword);
        if (field == null) {
            return null;
        }
        if (field instanceof MillisContainer) {
            // без костыля не обошлось, бгг.
            return String.valueOf(((MillisContainer) field).getValueMillis());
        } else {
            return field.valueToString();
        }
    }
}
