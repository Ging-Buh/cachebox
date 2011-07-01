package CB_Core.Types;

import java.util.ArrayList;
import java.util.Date;

import CB_Core.Enums.CacheTypes;



public class Cache implements Comparable<Cache> 
{
	/*
	 * Private Member
	 */

	/**
	 * wird in jedem Cache Obj benötigt, um eine Cache-Liste, nach der Entfernung zum User, zu Sortieren.
	 */
	private static Coordinate aktUserPos;
	
	public static long GenerateCacheId(String GcCode)
	{
		long result = 0;
		char[] dummy = GcCode.toCharArray();
		byte[] byteDummy = new byte[8];
		for (int i = 0; i < 8; i++)
		{
			if (i < GcCode.length())
				byteDummy[i] = (byte)dummy[i];
			else
				byteDummy[i] = 0;
		}
		for (int i = 7; i >= 0; i--) 
		{
			result *= 256;
			result += byteDummy[i];
		}
		return result;
	}
	
	
	
	/*
	 * Public Member
	 */
	

	/**
	 * Koordinaten des Caches auf der Karte gelten in diesem Zoom
	 */
    public static final int MapZoomLevel = 18;
    /**
	 * Koordinaten des Caches auf der Karte
     */
    public double MapX;
    /**
	 * Koordinaten des Caches auf der Karte
     */
    public double MapY;
    /**
	 * Id des Caches bei geocaching.com. Wird zumm Loggen benötigt und von
     * geotoad nicht exportiert
     */
    public String GcId;
    /**
	 * Id des Caches in der Datenbank von geocaching.com
     */
    public long Id;
    /**
	 * Waypoint Code des Caches
     */
    public String GcCode;
    /**
	 * Name des Caches
     */
    public String Name;
    
    /**
     * Die Coordinate, an der der Cache liegt.
     */
    public Coordinate Pos = new Coordinate(); 
    /**
	 * Breitengrad
     */
    public double Latitude() { return Pos.Latitude; }
    /**
	 * Längengrad
     */
    public double Longitude() { return Pos.Longitude; }
    /**
	 * Durchschnittliche Bewertung des Caches von GcVote
     */
    public float Rating;
    /**
	 * Größe des Caches. Bei Wikipediaeinträgen enthält dieses Feld den Radius in m
     */
    public int Size;
    /**
	 * Schwierigkeit des Caches
     */
    public float Difficulty;
    /**
	 * Geländebewertung
     */
    public float Terrain;
    /**
	 * Wurde der Cache archiviert?
     */
    public boolean Archived;
    /**
	 * Ist der Cache derzeit auffindbar?
     */
    public boolean Available;
    /**
	 * Ist der Cache einer der Favoriten
     */
    public boolean Favorit;
    
    /**
     * for Replication
     */
    public  int noteCheckSum = 0;
    
    /**
     * for Replication
     */
	public  int solverCheckSum = 0;    
    
	/**
	 * hat der Cache Clues oder Notizen erfasst
	 */
    public boolean hasUserData;

    /**
     * hat der Cache korrigierte Koordinaten
     */
    public boolean CorrectedCoordinates;
    
    /**
     * Wurde der Cache bereits gefunden?
     */
    public boolean Found;

    /**
     * Name der Tour, wenn die GPX-Datei aus GCTour importiert wurde
     */
    public String TourName;

    
    /**
     * Name der GPX-Datei aus der importiert wurde
     */
    public int GPXFilename_ID;

    /**
    * Art des Caches
    */
    public CacheTypes Type;

    /**
    * Erschaffer des Caches
    */
    public String PlacedBy;

    /**
    * Verantwortlicher
    */
    public String Owner;

    /**
    * Datum, an dem der Cache versteckt wurde
    */
    public Date DateHidden;

    /**
    * URL des Caches
    */
    public String Url;
   
    /**
     * Das Listing hat sich geändert!
     */
    public boolean listingChanged;
    
    /**
     * Positive Attribute des Caches
     */
    public long attributesPositive = 0;
    
    /**
     * Negative Attribute des Caches
     */
    public long attributesNegative = 0;
    
    /**
     * Anzahl der Travelbugs und Coins, die sich in diesem Cache befinden
     */
    public int NumTravelbugs;

    
    /**
     * Falls keine erneute Distanzberechnung nötig ist nehmen wir diese Distanz
     */
    public float cachedDistance = 0;
    
    /**
     * Hinweis für diesen Cache
     */
    public String hint = "";
   
    /**
     * Liste der zusätzlichen Wegpunkte des Caches
     */
    public ArrayList<Waypoint> waypoints = null;

    /**
     * Liste der Spoiler Resorcen
     */
    public ArrayList<String> spoilerRessources = null;
	
    /**
     * Kurz Beschreibung des Caches
     */
    public String shortDescription;
	
    /**
     * Ausführliche Beschreibung des Caches
     */
    public String longDescription;
    

    /*
     * Constructors
     */
    
    /**
     * Constructor 
     */
    public Cache() 
    {
    	 waypoints= new ArrayList<Waypoint>();
	}
    
    
    /*
     * Getter/Setter 
     */

    /**
     * Setzt die aktuelle User Position die zum Sortieren einer CacheList benötigt wird.
     * @param pos
     */
    public void setAktUserPos(Coordinate pos)
    {
    	aktUserPos = pos;
    	cachedDistance = Distance(aktUserPos);
    }
    
     
    
    /**
     * wenn ein Wegpunkt "Final" existiert, ist das mystery-Rätsel gelöst.
     */
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
    
    /**
     * true, if a this mystery cache has a final waypoint
     */
    public boolean HasFinalWaypoint() 
    { 
    	return GetFinalWaypoint() != null; 
    }

    /**
     * search the final waypoint for a mystery cache
     */
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

    /**
     *  
     * @return Entfernung  zur aktUserPos als Float
     */
    public float CachedDistance()
    {
        if (cachedDistance != 0)
            return cachedDistance;
        else
            return Distance(aktUserPos);
    }

    /**
     * Returns a List of Spoiler Ressources
     * @return ArrayList of String
     */
   public ArrayList<String> SpoilerRessources()
    {
        if (spoilerRessources == null)
        {
           // ReloadSpoilerRessources();
        }

        return spoilerRessources;
    }
	
   /**
    * Set a List of Spoiler Ressources
    * @param value ArrayList of String
    */
    public void setSpoilerRessources(ArrayList<String> value)
    {
        spoilerRessources = value;
    }

  
    /**
     * Returns true has the Cache Spoilers else returns false
     * @return Boolean
     */
    public boolean SpoilerExists()
    {
    	if(SpoilerRessources()==null)return false;
        return SpoilerRessources().size() > 0;
    }
    
    /**
     * Returns the MapIconId of this Cache
     * @param gcLogin
     * @return interger
     */
    public int GetMapIconId(String gcLogin)
    {
    	if (this.Owner.toLowerCase().equals(gcLogin) && (gcLogin.length() > 0))
    		return 20;
    	if (this.Found)
    		return 19;
    	if ((Type == CacheTypes.Mystery) && this.MysterySolved())
    		return 21;
    	
    	return Type.ordinal();
    }
    
    
    
    /**
	 * Converts the type string into an element of the CacheType enumeration.
	 * @param type
	 */
	public void parseCacheTypeString(String type)
	{
    	this.Type = CacheTypes.parseString(type);		
	}
	
	/**
	 * 
	 * Gibt die Entfernung  zur übergebenen User Position als Float zurück 
	 * und Speichert die Aktueller User Position für alle Caches ab.
	 * 
	 * @param fromPos Aktuelle User Position
	 * @return Entfernung  zur übergebenen User Position als Float
	 */
	public float Distance(Coordinate fromPos)
    {
        if(fromPos==null)return 0;
        float[] dist = new float[4];
        Coordinate.distanceBetween(fromPos.Latitude, fromPos.Longitude, Pos.Latitude, Pos.Longitude, dist);
        cachedDistance=dist[0];
        return dist[0];
    }
    
	
	
	
	
	/*
	 * Overrides
	 */
	
	
	 @Override
	 public int compareTo(Cache c2) 
	 {
	   	float dist1 = this.CachedDistance();
	   	float dist2 = c2.CachedDistance();
	    return (dist1 < dist2 ? -1 : (dist1 == dist2 ? 0 : 1));
	 }
    

    
}
