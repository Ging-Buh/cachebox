package CB_Core.Types;

import java.io.Serializable;
import java.net.URI;

import CB_Core.Import.DescriptionImageGrabber;
import de.cb.sqlite.CoreCursor;

public class ImageEntry implements Serializable {

	private static final long serialVersionUID = 4216092006574290607L;

	/**
	 * Ignore Existing by Import
	 */
	public boolean ignoreExisting = false;

	/**
	 * Beschreibung des Bildes
	 */
	public String Description = "";

	/**
	 * Name des Bildes
	 */
	public String Name = "";

	/**
	 * ImageUrl des Bildes
	 */
	public String ImageUrl = "";

	/**
	 * lokaler Pfad des Bildes
	 */
	public String LocalPath = "";

	/**
	 * Id des Caches
	 */
	public long CacheId = -1;

	/**
	 * GcCode des Caches
	 */
	public String GcCode = "";

	/**
	 * Ist das Bild aus der Cachebeschreibung
	 */
	public Boolean IsCacheImage = false;

	public ImageEntry() {
	}

	/**
	 * @param reader
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param DescriptionImageFolderLocal
	 *            Config.settings.DescriptionImageFolderLocal.getValue()
	 */
	public ImageEntry(CoreCursor reader) {
		CacheId = reader.getLong(0);
		GcCode = reader.getString(1).trim();
		Name = reader.getString(2);
		Description = reader.getString(3);
		ImageUrl = reader.getString(4);
		IsCacheImage = reader.getInt(5) == 1 ? true : false;

		LocalPath = DescriptionImageGrabber.BuildImageFilename(GcCode, URI.create(ImageUrl));
	}

	public void clear() {
		Description = "";
		Name = "";
		ImageUrl = "";
		CacheId = -1;
		GcCode = "";
		IsCacheImage = false;
		LocalPath = "";
	}

	public void dispose() {
		Description = null;
		Name = null;
		ImageUrl = null;
		GcCode = null;
		LocalPath = null;
	}

}
