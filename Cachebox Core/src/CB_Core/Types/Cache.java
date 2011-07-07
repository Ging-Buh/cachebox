package CB_Core.Types;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;



public class Cache implements Comparable<Cache> 
{
	/*
	 * Private Member
	 */

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
    public CacheSizes Size;
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
            return Distance(true);
    }

    /**
     * Returns a List of Spoiler Ressources
     * @return ArrayList of String
     */
   public ArrayList<String> SpoilerRessources()
    {
        if (spoilerRessources == null)
        {
           ReloadSpoilerRessources();
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
    
    public static void ReloadSpoilerRessources(final Cache cache)
    {
    	cache.spoilerRessources = new ArrayList<String>();

    	String path = Config.GetString("SpoilerFolder");
    	String directory = path + "/" + cache.GcCode.substring(0, 4);

    	if (!FileIO.DirectoryExists(directory))
    		return;

	        
    	File dir = new File(directory);
    	FilenameFilter filter = new FilenameFilter() {			
    		@Override
    		public boolean accept(File dir, String filename) {
					
    			filename = filename.toLowerCase();
    			if (filename.indexOf(cache.GcCode.toLowerCase()) == 0)
    			{
    				if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".bmp") || filename.endsWith(".png") || filename.endsWith(".gif"))
    					return true;
    			}
    			return false;
    		}
    	};
    	String[] files = dir.list(filter);

    	for (String image : files)
    	{
    		cache.spoilerRessources.add(directory + "/" + image);
    	}

    	// Add own taken photo
    	directory = Config.GetString("UserImageFolder");

    	if (!FileIO.DirectoryExists(directory))
    		return;

    	dir = new File(directory);
    	filter = new FilenameFilter() {			
    		@Override
    		public boolean accept(File dir, String filename) {
					
    			filename = filename.toLowerCase();
    			if (filename.indexOf(cache.GcCode.toLowerCase()) >= 0)
    				return true;
    			return false;
    		}
    	};
    	files = dir.list(filter);
    	if (!(files == null))
    	{
    		if (files.length>0)
    		{
    			for (String file : files)
    			{
    				String ext = FileIO.GetFileExtension(file);
    				if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("bmp") || ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("gif"))
    				{
    					cache.spoilerRessources.add(directory + "/" + file);
    				}
    			}
    		}
    	}
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
	 * @return Entfernung  zur übergebenen User Position als Float
	 */

    public float Distance(boolean useFinal)
    {
    	Coordinate fromPos = (GlobalCore.Marker.Valid) ? GlobalCore.Marker : GlobalCore.LastValidPosition;
    	Waypoint waypoint = null;
    	if (useFinal)
    		waypoint = this.GetFinalWaypoint();
    	// Wenn ein Mystery-Cache einen Final-Waypoint hat, soll die Diszanzberechnung vom Final aus gemacht werden
    	// If a mystery has a final waypoint, the distance will be calculated to the final not the the cache coordinates
    	Coordinate toPos = Pos;
    	if (waypoint != null)
    		toPos = new Coordinate(waypoint.Pos.Latitude, waypoint.Pos.Longitude);
    	float[] dist = new float[4];
    	Coordinate.distanceBetween(fromPos.Latitude, fromPos.Longitude, toPos.Latitude, toPos.Longitude, dist);
    	cachedDistance = dist[0];
    	return (float)cachedDistance;
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
