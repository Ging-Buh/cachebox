package de.droidcachebox.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

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

    public static FileHandle getInternalFileHandle(String path) {
        return Gdx.files.internal(path);
    }

    public static AbstractFile createFile(String path) {
        if (INSTANCE == null)
            throw new RuntimeException("no platform specific FileFactory instance!");
        return INSTANCE.createPlatformFile(path);
    }

    public static AbstractFile createFile(AbstractFile parent) {
        if (INSTANCE == null)
            throw new RuntimeException("no platform specific FileFactory instance!");
        return INSTANCE.createPlatformFile(parent);
    }

    public static AbstractFile createFile(AbstractFile parent, String child) {
        if (INSTANCE == null)
            throw new RuntimeException("no platform specific FileFactory instance!");
        return INSTANCE.createPlatformFile(parent, child);
    }

    public static AbstractFile createFile(String parent, String child) {
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

    protected abstract AbstractFile createPlatformFile(String path);

    protected abstract AbstractFile createPlatformFile(AbstractFile parent);

    protected abstract AbstractFile createPlatformFile(AbstractFile parent, String child);

    protected abstract AbstractFile createPlatformFile(String parent, String child);

    protected abstract String createPlatformThumb(String Path, int scaledWidth, String thumbPrefix);

}
