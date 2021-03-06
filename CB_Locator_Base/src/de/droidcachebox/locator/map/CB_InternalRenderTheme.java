package de.droidcachebox.locator.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import de.droidcachebox.utils.log.Log;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;

import java.io.InputStream;

/**
 * on releasse update: place a copy of the themes from mapsforge-themes\src\main\resources\assets to Android_GUI\assets\internalRenderThemes
 * the old car theme is placed there too
 *
 */
public enum CB_InternalRenderTheme implements XmlRenderTheme {

    DEFAULT("assets/internalRenderThemes/mapsforge/", "default.xml"), OSMARENDER("assets/internalRenderThemes/mapsforge/", "osmarender.xml"), CAR("assets/internalRenderThemes/cartheme/", "cartheme.xml");

    private final String absolutePath;
    private final String fileName;
    private FileHandle fileHandle;

    CB_InternalRenderTheme(String absolutePath, String file) {
        this.absolutePath = absolutePath;
        this.fileName = file;
        try {
            fileHandle = Gdx.files.classpath(this.absolutePath + this.fileName);
            fileHandle.readString(); // to get an Exception
        }
        catch (Exception ex) {
            Log.err("ininin", "irender", ex);
        }
    }

    @Override
    public String getRelativePathPrefix() {
        return "/assets/internalRenderThemes/";
    }

    @Override
    public InputStream getRenderThemeAsStream() {
        return fileHandle.read();
    }

    @Override
    public void setMenuCallback(XmlRenderThemeMenuCallback menuCallback) {
    }

    @Override
    public XmlRenderThemeMenuCallback getMenuCallback() {
        return null;
    }
}
