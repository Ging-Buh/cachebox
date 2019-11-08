package de.droidcachebox.utils;

/**
 * Created by Longri on 17.02.2016.
 */
public abstract class FileFactory {

    public static final String THUMB = "thumb_";
    public static final String THUMB_OVERVIEW = "overview";
    static FileFactory INSTANCE;

    public FileFactory() {
        if (INSTANCE != null)
            throw new RuntimeException("You need only one FileFactory instance");
        INSTANCE = this;
    }

    public static File createFile(String path) {
        if (INSTANCE == null)
            throw new RuntimeException("no platform specific FileFactory instance!");
        return INSTANCE.createPlatformFile(path);
    }

    public static File createFile(File parent) {
        if (INSTANCE == null)
            throw new RuntimeException("no platform specific FileFactory instance!");
        return INSTANCE.createPlatformFile(parent);
    }

    public static File createFile(File parent, String child) {
        if (INSTANCE == null)
            throw new RuntimeException("no platform specific FileFactory instance!");
        return INSTANCE.createPlatformFile(parent, child);
    }

    public static File createFile(String parent, String child) {
        if (INSTANCE == null)
            throw new RuntimeException("no platform specific FileFactory instance!");
        return INSTANCE.createPlatformFile(parent, child);
    }

    public static String createThumb(String path, int scaledWidth, String thumbPrefix) {
        if (INSTANCE == null)
            throw new RuntimeException("no platform specific FileFactory instance!");
        return INSTANCE.createPlatformThumb(path, scaledWidth, thumbPrefix);
    }

    public static boolean isInitialized() {
        return INSTANCE != null;
    }

    protected abstract File createPlatformFile(String path);

    protected abstract File createPlatformFile(File parent);

    protected abstract File createPlatformFile(File parent, String child);

    protected abstract File createPlatformFile(String parent, String child);

    protected abstract String createPlatformThumb(String Path, int scaledWidth, String thumbPrefix);

}
