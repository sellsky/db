package tk.bolovsrol.db.orm.refgen;

import java.io.File;

/**
 * Простенький статический запускатель генератора рефренсов для облегчения создания справочников.
 * <p/>
 * Надо в filename указать полный путь к файлу-интерфейсу,
 * не забыть в самом интерфейсе прописать {@link tk.bolovsrol.db.orm.refgen.DbMeta},
 * да раскомментировать нужные строки.
 */
public final class RefGenRunner {
    private RefGenRunner() { }

    public static void main(String[] args) throws Exception {
        String filename = "/path/to/Interface.java";

        RefGen refGen = new RefGen();
        refGen.init(new File(filename));

        // создаст один файл
        refGen.writeOutContainer();
//        refGen.writeOutDbdo();
//        refGen.writeOutManualCacheProvider();
//        refGen.writeOutRichProvider();
//        refGen.writeOutLatestAccessRichProvider();

        // или вот, не создаст, но напечатает
//        System.out.println(refGen.getContainer());
        System.out.println(refGen.getDbdo());
//        System.out.println(refGen.getManualCacheProvider());
//        System.out.println(refGen.getRichProvider());
//        System.out.println(refGen.getLatestAccessRichProvider());
    }
}
