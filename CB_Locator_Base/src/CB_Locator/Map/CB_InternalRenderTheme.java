package CB_Locator.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.mapsforge.map.rendertheme.XmlRenderTheme;
import org.mapsforge.map.rendertheme.XmlRenderThemeMenuCallback;

import java.io.InputStream;

public enum CB_InternalRenderTheme implements XmlRenderTheme {

    DEFAULT("osmarender/", "default.xml"), OSMARENDER("osmarender/", "osmarender.xml"), CAR("cartheme/", "cartheme.xml");

    private final String absolutePath;
    private final String fileName;
    private final FileHandle fileHandle;

    CB_InternalRenderTheme(String absolutePath, String file) {
        this.absolutePath = absolutePath;
        this.fileName = file;
        fileHandle = Gdx.files.classpath(this.absolutePath + this.fileName);
    }

    @Override
    public String getRelativePathPrefix() {
        return "/" + this.absolutePath;
        // return this.absolutePath;
    }

    @Override
    public InputStream getRenderThemeAsStream() {
        return fileHandle.read();
    }

    @Override
    public XmlRenderThemeMenuCallback getMenuCallback() {
        return null;
    }
}
