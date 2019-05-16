package tk.bolovsrol.db.orm.refgen;

/**
 * Генерирует шаблон для провайдера из мета-интерфейса.
 */
public class ManualCacheProviderGenerator implements ObjectGenerator {

    public static final String PROVIDER = "Provider";

    @Override public String getSuffix() {
        return PROVIDER;
    }

    @Override public boolean isOverrideExisting() {
        return false;
    }

    @Override public String generate(MetaInterface mi) {
        return mi.getHeader() + '\n' +
            "import tk.bolovsrol.db.orm.RecordNotFoundException;\n" +
            "import tk.bolovsrol.db.orm.manualcache.ManualCache;\n" +
            "import tk.bolovsrol.db.orm.manualcache.ManualCacheClient;\n" +
            '\n' +
            "public final class " + mi.getName() + PROVIDER + " {\n" +
            '\n' +
            "    private static final ManualCache<" + mi.getName() + DbdoGenerator.DBDO + ", " + mi.getName() + "> CACHE = ManualCache.register(new " + mi.getName() + DbdoGenerator.DBDO + "());  \n" +
            '\n' +
            "    private " + mi.getName() + PROVIDER + "() {\n" +
            "    }\n" +
            '\n' +
            "    public static void addClient(ManualCacheClient<" + mi.getName() + "> client) { CACHE.withClient(client); }\n" +
            '\n' +
            "    public static void chainUpdates(ManualCache<?,?> alienCache) { CACHE.chainUpdates(alienCache); }\n" +
            '\n' +
            "    public static " + mi.getName() + " getByIdOrDie(Long id) throws RecordNotFoundException { return CACHE.getByIdOrDie(id); }\n" +
            '\n' +
            "    public static " + mi.getName() + " getById(Long id) { return CACHE.getById(id); }\n" +
            '\n' +
            "}\n";
    }

}
