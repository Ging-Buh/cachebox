package CB_UI_Base;

import CB_UI_Base.GL_UI.DisplayType;
import CB_Utils.Plattform;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public abstract class AbstractGlobal {
    public static final String br = System.getProperty("line.separator");
    public static final String fs = System.getProperty("file.separator");
    public static boolean useSmallSkin = false;
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

            if (ret != null & !ret.exists()) {
                //try internal
                ret = Gdx.files.internal(path);
            }

            return ret;
        }
    }

}
