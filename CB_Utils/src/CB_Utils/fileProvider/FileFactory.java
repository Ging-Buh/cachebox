package CB_Utils.fileProvider;

/**
 * Created by Longri on 17.02.2016.
 */
public abstract class FileFactory {

    static FileFactory INSTANCE;

    public static File createFile(String path) {
        if (INSTANCE == null) throw new RuntimeException("no platform specific FileFactory instance!");
        return INSTANCE.createPlatformFile(path);
    }

    public static File createFile(File parent) {
        if (INSTANCE == null) throw new RuntimeException("no platform specific FileFactory instance!");
        return INSTANCE.createPlatformFile(parent);
    }

    public static File createFile(File parent, String child) {
        if (INSTANCE == null) throw new RuntimeException("no platform specific FileFactory instance!");
        return INSTANCE.createPlatformFile(parent, child);
    }

    public static File createFile(String parent, String child) {
        if (INSTANCE == null) throw new RuntimeException("no platform specific FileFactory instance!");
        return INSTANCE.createPlatformFile(parent, child);
    }


    public FileFactory() {
        if (INSTANCE != null) throw new RuntimeException("You need only one FileFactory instance");
        INSTANCE = this;
    }

    protected abstract File createPlatformFile(String path);

    protected abstract File createPlatformFile(File parent);

    protected abstract File createPlatformFile(File parent, String child);

    protected abstract File createPlatformFile(String parent, String child);


}
