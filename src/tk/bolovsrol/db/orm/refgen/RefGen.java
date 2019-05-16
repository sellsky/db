package tk.bolovsrol.db.orm.refgen;

import tk.bolovsrol.utils.Spell;
import tk.bolovsrol.utils.StreamUtils;
import tk.bolovsrol.utils.UnexpectedBehaviourException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * На основании переданного файла-интерфейса создаёт рядом контейнер, БД-объект и провайдер.
 * <p/>
 * Причём, контейнер и БД-объект перезаписывает, если они уже есть.
 */
public class RefGen {
    private static final String _JAVA = ".java";
    private File parentFile;
    private String baseName;
    private MetaInterface mi;

    public void init(File interfaceFile) throws IOException, UnexpectedBehaviourException {
        parentFile = interfaceFile.getParentFile();
        baseName = interfaceFile.getName();
        assert baseName.endsWith(_JAVA);
        baseName = baseName.substring(0, baseName.length() - _JAVA.length());

        System.out.println("Reading interface from " + Spell.get(interfaceFile) + "...");
        mi = new MetaInterface(new String(StreamUtils.readWhileAvailable(new FileInputStream(interfaceFile))));

    }

    public void writeOutManualCacheProvider() throws IOException, UnexpectedBehaviourException {
        writeOut(new ManualCacheProviderGenerator());
    }

    public void writeOutRichProvider() throws IOException, UnexpectedBehaviourException {
        writeOut(new RichProviderGenerator());
    }

    public void writeOutLatestAccessRichProvider() throws IOException, UnexpectedBehaviourException {
        writeOut(new LatestAccessRichProviderGenerator());
    }

    public void writeOutDbdo() throws IOException, UnexpectedBehaviourException {
        writeOut(new DbdoGenerator());
    }

    public void writeOutContainer() throws IOException, UnexpectedBehaviourException {
        writeOut(new ContainerGenerator());
    }

    public String getContainer() {
        return new ContainerGenerator().generate(mi);
    }

    public String getDbdo() throws UnexpectedBehaviourException {
        return new DbdoGenerator().generate(mi);
    }

    public String getManualCacheProvider() {
        return new ManualCacheProviderGenerator().generate(mi);
    }

    public String getRichProvider() {
        return new RichProviderGenerator().generate(mi);
    }

    public String getLatestAccessRichProvider() {
        return new LatestAccessRichProviderGenerator().generate(mi);
    }

    private void writeOut(ObjectGenerator og) throws IOException, UnexpectedBehaviourException {
        byte[] data = og.generate(mi).getBytes();
        File file = new File(parentFile, baseName + og.getSuffix() + _JAVA);
        if (file.exists()) {
            if (!og.isOverrideExisting()) {
                System.out.println("File " + Spell.get(file) + " already exists, not overwriting");
                return;
            }
            System.out.println("Rewriting " + file);
        } else {
            System.out.println("Writing file " + Spell.get(file));
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        }
    }

}
