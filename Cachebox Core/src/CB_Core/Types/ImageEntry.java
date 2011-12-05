package CB_Core.Types;

import CB_Core.DB.CoreCursor;

public class ImageEntry
{

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

	public ImageEntry()
	{
	}

	public ImageEntry(CoreCursor reader)
	{
		CacheId = reader.getLong(0);
		GcCode = reader.getString(1).trim();
		Name = reader.getString(2);
		Description = reader.getString(3);
		ImageUrl = reader.getString(4);
		IsCacheImage = reader.getInt(5) == 1 ? true : false;
	}

	public void clear()
	{
		Description = "";
		Name = "";
		ImageUrl = "";
		CacheId = -1;
		GcCode = "";
		IsCacheImage = false;
	}

}
