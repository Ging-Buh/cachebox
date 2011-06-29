package CB_Core.Types;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Types.LogEntry;



public class Cache implements Comparable<Cache> 
{

	 public  int noteCheckSum = 0;   // for Replication
	 public  int solverCheckSum = 0;   // for Replication
	 
    @Override
    public int compareTo(Cache c2) {
    	float dist1 = this.CachedDistance();
    	float dist2 = c2.CachedDistance();
        return (dist1 < dist2 ? -1 : (dist1 == dist2 ? 0 : 1));
    }
    
    // Koordinaten des Caches auf der Karte gelten in diesem Zoom
    public static final int MapZoomLevel = 18;
    /// Koordinaten des Caches auf der Karte
    public double MapX;
    /// Koordinaten des Caches auf der Karte
    public double MapY;
    /// Id des Caches bei geocaching.com. Wird zumm Loggen benötigt und von
    /// geotoad nicht exportiert
    public String GcId;
    /// Id des Caches in der Datenbank von geocaching.com
    public long Id;
    /// Waypoint Code des Caches
    public String GcCode;
    /// Name des Caches
    public String Name;
    public Coordinate Coordinate = new Coordinate(); 
    /// Breitengrad
    public double Latitude() { return Coordinate.Latitude; }
    /// Längengrad
    public double Longitude() { return Coordinate.Longitude; }
    /// Durchschnittliche Bewertung des Caches von GcVote
    public float Rating;
    /// Größe des Caches. Bei Wikipediaeinträgen enthält dieses Feld den Radius in m
    public int Size;
    /// Schwierigkeit des Caches
    public float Difficulty;
    /// Geländebewertung
    public float Terrain;
    /// Wurde der Cache archiviert?
    public boolean Archived;
    /// Ist der Cache derzeit auffindbar?
    public boolean Available;
    /// Ist der Cache einer der Favoriten
    public boolean favorit;
    
    public boolean Favorit()
    {
    	return favorit;
    }
    
    public void Favorit(boolean value)
    {
		favorit = value;
/*		SqlCeCommand command = new SqlCeCommand("update Caches set Favorit=@favorit where Id=@id", Database.Data.Connection);
		command.Parameters.Add("@favorit", DbType.Boolean).Value = value;
		command.Parameters.Add("@id", DbType.Int64).Value = Id;
		command.ExecuteNonQuery();
		command.Dispose();*/
    }
    /// hat der Cache Clues oder Notizen erfasst
    public boolean hasUserData;
/*    public bool HasUserData
    {
        get
        {
            return hasUserData;
        }
        set
        {
            hasUserData = value;
            SqlCeCommand command = new SqlCeCommand("update Caches set HasUserData=@hasUserData where Id=@id", Database.Data.Connection);
            command.Parameters.Add("@hasUserData", DbType.Boolean).Value = value;
            command.Parameters.Add("@id", DbType.Int64).Value = Id;
            command.ExecuteNonQuery();
            command.Dispose();
        }
    }*/
    
    public boolean CorrectedCoordinates;
    
    /// <summary>
    ///  wenn ein Wegpunkt "Final" existiert, ist das mystery-Rätsel gelöst.
    /// </summary>
    public boolean MysterySolved()
    {
        if (this.CorrectedCoordinates)
          return true;

        if (this.Type != CacheTypes.Mystery)
          return false;

        boolean x;
        x = false;

        ArrayList<Waypoint> wps = waypoints;
        for (Waypoint wp : wps)
        {
          if (wp.Type == CacheTypes.Final)
          {
            x = true;
          }
        };
        return x;
    }
    
    ///  true, if a this mystery cache has a final waypoint
    public boolean HasFinalWaypoint() { return GetFinalWaypoint() != null; }

    ///  search the final waypoint for a mystery cache
    public Waypoint GetFinalWaypoint()
    {
        if (this.Type != CacheTypes.Mystery)
            return null;

        for (Waypoint wp : waypoints)
        {
            if (wp.Type == CacheTypes.Final)
            {
                return wp;
            }
        };

        return null;
    }

    public boolean found;

    /// Wurde der Cache bereits gefunden?
    public boolean Found()
    {
    	return found;
    }
  
    public void Found(Boolean value)
    {
    	found = value;
    }

       
        

 
    // Name der Tour, wenn die GPX-Datei aus GCTour importiert wurde
    public String TourName;

    // Name der GPX-Datei aus der importiert wurde
    public int GPXFilename_ID;

    /// <summary>
    /// Art des Caches
    /// </summary>
    public CacheTypes Type;

    /// <summary>
    /// Erschaffer des Caches
    /// </summary>
    public String PlacedBy;

    /// <summary>
    /// Verantwortlicher
    /// </summary>
    public String Owner;

    /// <summary>
    /// Datum, an dem der Cache versteckt wurde
    /// </summary>
    public Date DateHidden;

    /// <summary>
    /// URL des Caches
    /// </summary>
    public String Url;
    
      


    
   
    /// <summary>
    /// Falls keine erneute Distanzberechnung nötig ist nehmen wir diese Distanz
    /// </summary>
    public float cachedDistance = 0;
    public float CachedDistance()
    {
        if (cachedDistance != 0)
            return cachedDistance;
        else
            return 0;//Distance();
    }

    /// <summary>
    /// Anzahl der Travelbugs und Coins, die sich in diesem Cache befinden
    /// </summary>
    public int NumTravelbugs;

   

    public String hint = "";
   

    public ArrayList<Waypoint> waypoints = null;

/*
    public List<Waypoint> Waypoints
    {
        set
        {
            waypoints = value;
        }
        get
        {
            if (waypoints == null)
            {
                waypoints = new List<Waypoint>();

                SqlCeCommand command = new SqlCeCommand("select GcCode, CacheId, Latitude, Longitude, Description, Type, SyncExclude, UserWaypoint, Clue, Title from Waypoint where CacheId=@cacheid", Database.Data.Connection);
                command.Parameters.Add("@cacheid", DbType.Int64).Value = Id;
                SqlCeDataReader reader = command.ExecuteReader();

                while (reader.Read())
                    waypoints.Add(new Waypoint(reader));

                reader.Dispose();
                command.Dispose();
            }

            return waypoints;
        }
    }
*/
    public boolean listingChanged;
 /*
    public bool ListingChanged
    {
        get
        {
            return listingChanged;
        }

        set
        {
            listingChanged = value;
            SqlCeCommand command = new SqlCeCommand("update Caches set ListingChanged=@ListingChanged where Id=@id", Database.Data.Connection);
            command.Parameters.Add("@ListingChanged", DbType.Boolean).Value = value;
            command.Parameters.Add("@id", DbType.Int64).Value = Id;
            command.ExecuteNonQuery();
            command.Dispose();

        }
    }

*/

    

   
    
   
    public long attributesPositive = 0;
    

    public long attributesNegative = 0;
    

/*
    public override int GetHashCode()
    {
        return (int)Id;
    }

    public override bool Equals(object obj)
    {
        if (obj.GetType() != this.GetType())
            return false;

        return ((Cache)obj).Id == this.Id;
    }
*/

    public Cache() 
    {
    	 waypoints= new ArrayList<Waypoint>();
	}

	/*
    public static Cache GetCacheByCacheId(long cacheId)
    {
        foreach (Cache cache in Query)
        {
            if (cache.Id == cacheId)
                return cache;
        }
        return null;
    }
*/
/*
    public Waypoint GetWaypointByGcCode(string gcCode)
    {
        foreach (Waypoint wp in Waypoints)
            if (wp.GcCode == gcCode)
                return wp;

        return null;
    }

    /// <summary>
    /// Zum Sortieren von Caches nach Distanz
    /// </summary>
    /// <param name="obj">Cache, mit dem die Distanz verglichen werden soll</param>
    /// <returns>-1, falls obj näher ist als die Instanz, 1 falls sie weiter entfernt ist und sonst 0.</returns>
    public int CompareTo(object obj)
    {
        System.Diagnostics.Debug.Assert(obj is Cache, "Falscher Typ: " + obj.ToString() + " ist kein Cache!");

        double d1 = (obj as Cache).CachedDistance;

        if (d1 < CachedDistance)
            return 1;

        if (d1 > CachedDistance)
            return -1;

        return 0;
    }
*/
    public ArrayList<String> spoilerRessources = null;
	public String shortDescription;
	public String longDescription;
    public ArrayList<String> SpoilerRessources()
    {
        if (spoilerRessources == null)
        {
           // ReloadSpoilerRessources();
        }

        return spoilerRessources;
    }
    public void SpoilerRessources(ArrayList<String> value)
    {
        spoilerRessources = value;
    }

  
    public boolean SpoilerExists()
    {
    	if(SpoilerRessources()==null)return false;
        return SpoilerRessources().size() > 0;
    }
    
    public int GetMapIconId(String gcLogin)
    {
    	if (this.Owner.toLowerCase().equals(gcLogin) && (gcLogin.length() > 0))
    		return 20;
    	if (this.found)
    		return 19;
    	if ((Type == CacheTypes.Mystery) && this.MysterySolved())
    		return 21;
    	
    	return Type.ordinal();
    }
    
    
    
    /**
	 * Converts the type string into an element of the CacheType enumeration.
	 * @param strText
	 */
	public void parseCacheTypeString(String type)
	{
    	this.Type = CacheTypes.parseString(type);		
	}
	
	public float Distance(Coordinate fromPos)
    {
        
        float[] dist = new float[4];
        Coordinate.distanceBetween(fromPos.Latitude, fromPos.Longitude, Coordinate.Latitude, Coordinate.Longitude, dist);
        return dist[0];
    }
    
	
	
	
	
	
	
	
	
    

    
}
