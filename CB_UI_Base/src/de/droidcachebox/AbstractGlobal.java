package de.droidcachebox;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.droidcachebox.gdx.DisplayType;
import de.droidcachebox.utils.Plattform;

public abstract class AbstractGlobal {
    public static final String br = System.getProperty("line.separator");
    public static final String fs = System.getProperty("file.separator");
    public static DisplayType displayType = DisplayType.Normal;
    public static float displayDensity = 1;
    protected static AbstractGlobal Instance;

    protected AbstractGlobal() {
        Instance = this;
    }

    public static boolean isTestVersion() {
        return false;
    }

    public static FileHandle getInternalFileHandle(String path) {
        if (Plattform.used == Plattform.undef)
            throw new IllegalArgumentException("Platform not def");

        if (Plattform.used == Plattform.Android) {
            return Gdx.files.internal(path);
        } else {
            FileHandle ret = Gdx.files.classpath(path);
            //try internal
            if (ret != null && !ret.exists()) ret = Gdx.files.internal(path);
            return ret;
        }
    }

}
