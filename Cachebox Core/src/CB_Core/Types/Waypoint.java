package CB_Core.Types;

import java.io.Serializable;
import java.util.Date;

import CB_Core.GlobalCore;
import CB_Core.Enums.CacheTypes;

public class Waypoint implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 67610567646416L;

	/// Id des dazugehörigen Caches in der Datenbank von geocaching.com
    public long CacheId;

    /// Waypoint Code
    public String GcCode= "";

    public Coordinate Pos;
    /// Breitengrad
    public double Latitude() { return Pos.Latitude; } 

    /// Längengrad
    public double Longitude() { return Pos.Longitude; }

    /// Titel des Wegpunktes
    public String Title= "";

    /// Kommentartext
    public String Description= "";

    /// Art des Wegpunkts
    public CacheTypes Type;

    /// true, falls der Wegpunkt vom Benutzer erstellt wurde
    public boolean IsUserWaypoint;

    /// true, falls der Wegpunkt von der Synchronisation ausgeschlossen wird
    public boolean IsSyncExcluded;

    /// Lösung einer QTA
    public String Clue= "";

    
    public Waypoint()
    {
        CacheId = -1;
        GcCode = "";
        Pos = new Coordinate();
        Description = "";
    }
    
    public int checkSum = 0;   // for replication

	public Date time;

   

    

    public Waypoint(String gcCode, CacheTypes type, String description, double latitude, double longitude, long cacheId, String clue, String title)
    {
        GcCode = gcCode;
        CacheId = cacheId;
        Pos = new Coordinate(latitude, longitude);
        Description = description;
        Type = type;
        IsSyncExcluded = true;
        IsUserWaypoint = true;
        Clue = clue;
        Title = title;
    }

   

    /// <summary>
    /// Entfernung von der letzten gültigen Position
    /// </summary>
    public float Distance()
    {
    	Coordinate fromPos = (GlobalCore.Marker.Valid) ? GlobalCore.Marker : GlobalCore.LastValidPosition;
        float[] dist = new float[4];
        Coordinate.distanceBetween(fromPos.Latitude, fromPos.Longitude, Pos.Latitude, Pos.Longitude, dist);
        return dist[0];
    }

	public void setLatitude(double parseDouble) 
	{
		Pos.Latitude = parseDouble;
	}

	public void setLongitude(double parseDouble) 
	{
		Pos.Longitude = parseDouble;
	}

	/**
	 * 
	 * @param strText
	 */
	public void parseTypeString(String strText)
	{
		// Log.d(TAG, "Parsing type string: " + strText);
		
		/* Geocaching.com cache types are in the form
		 * 		Geocache|Multi-cache
		 * 		Waypoint|Question to Answer
		 * 		Waypoint|Stages of a Multicache
		 * Other pages / bcaching.com results do not contain the | separator,
		 * so make sure that the parsing functionality does work with both variants
		 */
		
		String[] arrSplitted = strText.split("\\|");
		if (arrSplitted[0].toLowerCase().equals("geocache"))
		{
			this.Type = CacheTypes.Cache;
		}
		else
		{
			String strCacheType;
			if (arrSplitted.length > 1)
				strCacheType = arrSplitted[1];
			else
				strCacheType = arrSplitted[0];
				
			String[] strFirstWord = strCacheType.split(" ");
			this.Type = CacheTypes.valueOf(strFirstWord[0]);
		}
		// Log.d(TAG, "Waypoint type: " + this.mWaypointType.toString());
	}

}
