package CB_Core.Types;

import java.util.Date;

public class LogEntry 
{
       
	/**
	 * Benutzername des Loggers
	 */
	public String Finder = "";

    /**
    * Logtyp, z.B. "Found it!"
    */
    public Integer Type = -1;

    /**
    * Index des zu verwendenden Bildchens
    */
    public int TypeIcon = -1;

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

}
