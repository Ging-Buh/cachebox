package CB_Core.Types;

import java.io.Serializable;
import java.util.Date;

import CB_Core.Enums.LogTypes;

public class LogEntry implements Serializable
{

	private static final long serialVersionUID = -4269566289864187308L;

	/**
	 * Benutzername des Loggers
	 */
	public String Finder = "";

	/**
	 * Logtyp, z.B. "Found it!"
	 */
	public LogTypes Type;

	// /**
	// * Index des zu verwendenden Bildchens
	// */
	// public int TypeIcon = -1;

	/**
	 * Geschriebener Text
	 */
	public String Comment = "";

	/**
	 * Zeitpunkt
	 */
	public Date Timestamp = new Date(0);

	/**
	 * Id des Caches
	 */
	public long CacheId = -1;

	/**
	 * Id des Logs
	 */
	public long Id = -1;

	public void clear()
	{
		Finder = "";
		Type = null;
		// TypeIcon = -1;
		Comment = "";
		Timestamp = new Date(0);
		CacheId = -1;
		Id = -1;
	}

}
