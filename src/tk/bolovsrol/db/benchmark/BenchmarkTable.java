package tk.bolovsrol.db.benchmark;

import tk.bolovsrol.db.benchmark.fields.RandomableBigDecimalDbField;
import tk.bolovsrol.db.benchmark.fields.RandomableField;
import tk.bolovsrol.db.benchmark.fields.RandomableIntegerDbField;
import tk.bolovsrol.db.benchmark.fields.RandomableStringDbField;
import tk.bolovsrol.db.orm.object.AbstractRefDbDataObject;

import java.util.Random;

public class BenchmarkTable extends AbstractRefDbDataObject {

    private final RandomableField[] dataFields;

	// id field declaration moved to parent
	private final int intsCount;
	private final int stringsCount;
    private final int bigDecimalsCount;

    /**
     * Создаёт объект для указанных каталога и таблицы в БД.
     *
     * @param sqlCatalogName название каталога объекта в БД
     * @param sqlTableName название таблицы объекта в БД
     */
    public BenchmarkTable(String sqlCatalogName, String sqlTableName, int intsCount, int stringsCount, int bigDecimalsCount) {
        super(sqlCatalogName, sqlTableName);
        this.intsCount = intsCount;
        this.stringsCount = stringsCount;
        this.bigDecimalsCount = bigDecimalsCount;
        dataFields = new RandomableField[intsCount + stringsCount + bigDecimalsCount];
        int u = 0;
        for (int i = 1; i <= intsCount; i++) {
            dataFields[u++] = new RandomableIntegerDbField(this, "int" + i);
        }
        for (int i = 1; i <= stringsCount; i++) {
            dataFields[u++] = new RandomableStringDbField(this, "char" + i, 0, 64);
        }
        for (int i = 1; i <= bigDecimalsCount; i++) {
            dataFields[u++] = new RandomableBigDecimalDbField(this, "money" + i, 2);
        }
    }

    public void setRandomValues(Random random) {
        for (RandomableField dataField : dataFields) {
            dataField.setNextRandomValue(random);
        }
    }

    public static BenchmarkTable[] newTables(int quantity, String sqlCatalogName, String sqlTableNamePrefix, int intsCount, int stringsCount, int bigDecimalsCount) {
        BenchmarkTable[] result = new BenchmarkTable[quantity];
        for (int i = 1; i <= quantity; i++) {
            result[i - 1] = new BenchmarkTable(sqlCatalogName, sqlTableNamePrefix + i, intsCount, stringsCount, bigDecimalsCount);
        }
        return result;
    }

    public BenchmarkTable cleanCopy() {
        return new BenchmarkTable(getSqlCatalogName(), getSqlTableName(), intsCount, stringsCount, bigDecimalsCount);
    }
}
