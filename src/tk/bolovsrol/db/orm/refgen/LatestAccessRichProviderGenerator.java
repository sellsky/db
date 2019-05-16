package tk.bolovsrol.db.orm.refgen;

/**
 * Генерирует шаблон для провайдера из мета-интерфейса.
 */
public class LatestAccessRichProviderGenerator implements ObjectGenerator {

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
              "import tk.bolovsrol.db.orm.versioncache.LatestAccessRichVersionCacheProxy;\n" +
              "import tk.bolovsrol.db.orm.versioncache.VersionCacheManager;\n" +
              '\n' +
              "public class " + mi.getName() + PROVIDER + " {\n" +
              '\n' +
              "    private static final LatestAccessRichVersionCacheProxy<" + mi.getName() + "> CACHE = LatestAccessRichVersionCacheProxy.retrieve(VersionCacheManager.rich(" + mi.getName() + DbdoGenerator.DBDO + ".class));\n" +
              '\n' +
              "    private " + mi.getName() + PROVIDER + "() {\n" +
              "    }\n" +
              '\n' +
              "    public static " + mi.getName() + " getByIdOrDie(Long id) throws RecordNotFoundException {\n" +
              "        return CACHE.getAndFixByIdOrDie(id);\n" +
              "    }\n" +
              '\n' +
              "    public static " + mi.getName() + " getById(Long id) {\n" +
              "        return CACHE.getAndFixById(id);\n" +
              "    }\n" +
              '\n' +
              "}\n";
    }

}
