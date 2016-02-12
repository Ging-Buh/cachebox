package CB_Locator.Map;

import java.io.InputStream;

import org.mapsforge.map.rendertheme.XmlRenderTheme;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public enum CB_InternalRenderTheme implements XmlRenderTheme {

	/**
	 * A render-theme similar to the OpenStreetMap Osmarender style.
	 * 
	 * @see <a href="http://wiki.openstreetmap.org/wiki/Osmarender">Osmarender</a>
	 */
	OSMARENDER("osmarender/", "osmarender.xml"),

	DAY_CAR_THEME("cartheme/", "cartheme.xml");

	private final String absolutePath;
	private final String file;
	private final FileHandle fileHandle;

	private CB_InternalRenderTheme(String absolutePath, String file) {
		this.absolutePath = absolutePath;
		this.file = file;
		fileHandle = Gdx.files.classpath(this.absolutePath + this.file);
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
	public boolean isFreizeitkarte() {
		return false;
	}
}
