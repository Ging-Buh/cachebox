package CB_Core.Types;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import CB_Core.CoreSettingsForward;
import CB_Core.DB.Database;
import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Settings.CB_Core_Settings;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Utils.DB.CoreCursor;
import CB_Utils.Util.FileIO;

public class Cache implements Comparable<Cache>, Serializable
{
	private static final long serialVersionUID = 1015307624242318838L;

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
			if (i < GcCode.length()) byteDummy[i] = (byte) dummy[i];
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
	 * Id des Caches bei geocaching.com. Wird zumm Loggen benötigt und von geotoad nicht exportiert
	 */
	// TODO Warum ist das ein String?
	public String GcId = "";
	/**
	 * Id des Caches in der Datenbank von geocaching.com
	 */
	public long Id;
	/**
	 * Waypoint Code des Caches
	 */
	public String GcCode = "";
	/**
	 * Name des Caches
	 */
	public String Name = "";

	/**
	 * Die Coordinate, an der der Cache liegt.
	 */
	public Coordinate Pos = new Coordinate();

	/**
	 * Breitengrad
	 */
	public double Latitude()
	{
		return Pos.getLatitude();
	}

	/**
	 * Längengrad
	 */
	public double Longitude()
	{
		return Pos.getLongitude();
	}

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
	public float Difficulty = 0;
	/**
	 * Geländebewertung
	 */
	public float Terrain = 0;
	/**
	 * Wurde der Cache archiviert?
	 */
	public boolean Archived;
	/**
	 * Ist der Cache derzeit auffindbar?
	 */
	public boolean Available;
	/**
	 * ApiStatus 0: Cache wurde nicht per Api hinzugefügt 1: Cache wurde per GC Api hinzugefügt und ist noch nicht komplett geladen (IsLite
	 * = true) 2: Cache wurde per GC Api hinzugefügt und ist komplett geladen (IsLite = false)
	 */
	public byte ApiStatus;

	/**
	 * Ist der Cache einer der Favoriten
	 */
	public boolean Favorit()
	{
		return favorite;
	}

	private boolean favorite;

	public void setFavorit(boolean value)
	{
		favorite = value;

	}

	/**
	 * for Replication
	 */
	public int noteCheckSum = 0;
	public String tmpNote = null; // nur für den RPC-Import

	/**
	 * for Replication
	 */
	public int solverCheckSum = 0;
	public String tmpSolver = null; // nur für den RPC-Import

	/**
	 * hat der Cache Clues oder Notizen erfasst
	 */
	public boolean hasUserData;

	/**
	 * hat der Cache korrigierte Koordinaten
	 */
	private boolean CorrectedCoordinates;

	/**
	 * Wurde der Cache bereits gefunden?
	 */
	public boolean Found;

	/**
	 * Name der Tour, wenn die GPX-Datei aus GCTour importiert wurde
	 */
	public String TourName = "";

	/**
	 * Name der GPX-Datei aus der importiert wurde
	 */
	public long GPXFilename_ID = 0;

	/**
	 * Art des Caches
	 */
	public CacheTypes Type = CacheTypes.Undefined;

	/**
	 * Erschaffer des Caches
	 */
	public String PlacedBy = "";

	/**
	 * Verantwortlicher
	 */
	public String Owner = "";

	/**
	 * Datum, an dem der Cache versteckt wurde
	 */
	public Date DateHidden;

	/**
	 * URL des Caches
	 */
	public String Url = "";

	/**
	 * Country des Caches
	 */
	public String Country = "";

	/**
	 * State des Caches
	 */
	public String State = "";

	/**
	 * Das Listing hat sich geändert!
	 */
	public boolean listingChanged = false;

	/**
	 * Positive Attribute des Caches
	 */
	private DLong attributesPositive = new DLong(0, 0);

	/**
	 * Negative Attribute des Caches
	 */
	private DLong attributesNegative = new DLong(0, 0);

	/**
	 * Anzahl der Travelbugs und Coins, die sich in diesem Cache befinden
	 */
	public int NumTravelbugs = 0;

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
	public ArrayList<ImageEntry> spoilerRessources = null;

	/**
	 * Kurz Beschreibung des Caches
	 */
	public String shortDescription;

	/**
	 * Ausführliche Beschreibung des Caches Nur für Import Zwecke. Ist normalerweise leer, da die Description bei aus Speicherplatz Gründen
	 * bei Bedarf aus der DB geladen wird
	 */
	public String longDescription;

	/**
	 * Bin ich der Owner? </br>-1 noch nicht getestet </br>1 ja </br>0 nein
	 */
	private int myCache = -1;

	private static String gcLogin = null;

	/**
	 * @param userName
	 *            Config.settings.GcLogin.getValue()
	 * @return
	 */
	public boolean ImTheOwner()
	{
		String userName = CB_Core_Settings.GcLogin.getValue().toLowerCase();
		if (myCache == 0) return false;
		if (myCache == 1) return true;

		if (gcLogin == null)
		{
			gcLogin = userName;
		}

		boolean ret = this.Owner.toLowerCase().equals(gcLogin);
		myCache = ret ? 1 : 0;
		return ret;
	}

	/*
	 * Constructors
	 */

	/**
	 * Constructor
	 */
	public Cache()
	{
		this.DateHidden = new Date();
		this.NumTravelbugs = 0;
		this.Difficulty = 0;
		this.Terrain = 0;
		this.Size = CacheSizes.other;
		this.Available = true;
		waypoints = new ArrayList<Waypoint>();
	}

	/**
	 * Constructor
	 */
	public Cache(double Latitude, double Longitude, String Name, CacheTypes type, String GcCode)
	{
		this.Pos.setLatitude(Latitude);
		this.Pos.setLongitude(Longitude);
		this.Name = Name;
		this.Type = type;
		this.DateHidden = new Date();
		this.GcCode = GcCode;
		this.NumTravelbugs = 0;
		this.Difficulty = 0;
		this.Terrain = 0;
		this.Size = CacheSizes.other;
		this.Available = true;
		waypoints = new ArrayList<Waypoint>();
		AttributeList = null;
	}

	/*
	 * Getter/Setter
	 */

	/**
	 * -- korrigierte Koordinaten (kommt nur aus GSAK? bzw CacheWolf-Import) -- oder Mystery mit gültigem Final
	 */
	public boolean CorrectedCoordiantesOrMysterySolved()
	{
		if (this.hasCorrectedCoordinates()) return true;

		if (this.Type != CacheTypes.Mystery) return false;

		boolean x;
		x = false;

		ArrayList<Waypoint> wps = waypoints;
		for (Waypoint wp : wps)
		{
			if (wp.Type == CacheTypes.Final)
			{
				if (!(wp.Pos.getLatitude() == 0 && wp.Pos.getLongitude() == 0)) x = true;
			}
		}
		;
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
		if (this.Type != CacheTypes.Mystery) return null;

		for (Waypoint wp : waypoints)
		{
			if (wp.Type == CacheTypes.Final)
			{
				if (wp.Pos.isZero()) continue;
				return wp;
			}
		}
		;

		return null;
	}

	/**
	 * true if this is a mystery of multi with a Stage Waypoint defined as StartPoint
	 * 
	 * @return
	 */
	public boolean HasStartWaypoint()
	{
		return GetStartWaypoint() != null;
	}

	/**
	 * search the start Waypoint for a multi or mystery
	 * 
	 * @return
	 */
	public Waypoint GetStartWaypoint()
	{
		if ((this.Type != CacheTypes.Multi) && (this.Type != CacheTypes.Mystery)) return null;

		for (Waypoint wp : waypoints)
		{
			if ((wp.Type == CacheTypes.MultiStage) && (wp.IsStart))
			{
				return wp;
			}
		}
		return null;
	}

	/**
	 * @return Entfernung zur aktUserPos als Float
	 */
	public float CachedDistance()
	{
		if (cachedDistance != 0)
		{
			return cachedDistance;
		}
		else
		{
			return Distance(true);
		}
	}

	/**
	 * Returns a List of Spoiler Ressources
	 * 
	 * @return ArrayList of String
	 */
	public ArrayList<ImageEntry> getSpoilerRessources()
	{
		if (spoilerRessources == null)
		{
			ReloadSpoilerRessources();
		}

		return spoilerRessources;
	}

	/**
	 * Set a List of Spoiler Ressources
	 * 
	 * @param value
	 *            ArrayList of String
	 */
	public void setSpoilerRessources(ArrayList<ImageEntry> value)
	{
		spoilerRessources = value;
	}

	/**
	 * Returns true has the Cache Spoilers else returns false
	 * 
	 * @return Boolean
	 */
	public boolean SpoilerExists()
	{
		if (spoilerRessources == null) ReloadSpoilerRessources();
		return spoilerRessources.size() > 0;
	}

	/**
	 * @param SpoilerFolderLocal
	 *            Config.settings.SpoilerFolderLocal.getValue()
	 * @param DefaultSpoilerFolder
	 *            Config.settings.SpoilerFolder.getDefaultValue()
	 * @param DescriptionImageFolder
	 *            Config.settings.DescriptionImageFolder.getValue()
	 * @param UserImageFolder
	 *            Config.settings.UserImageFolder.getValue()
	 */
	public void ReloadSpoilerRessources()
	{
		spoilerRessources = new ArrayList<ImageEntry>();

		String directory = "";

		// from own Repository
		String path = CB_Core_Settings.SpoilerFolderLocal.getValue();
		if (path != null && path.length() > 0)
		{
			directory = path + "/" + GcCode.substring(0, 4);
			reloadSpoilerResourcesFromPath(directory, spoilerRessources);
		}

		// from Global Repository
		path = CB_Core_Settings.DescriptionImageFolder.getValue();
		directory = path + "/" + GcCode.substring(0, 4);
		reloadSpoilerResourcesFromPath(directory, spoilerRessources);

		// Spoilers are always loaden from global Repository too
		// from globalUser changed Repository
		path = CB_Core_Settings.SpoilerFolder.getValue();
		directory = path + "/" + GcCode.substring(0, 4);
		reloadSpoilerResourcesFromPath(directory, spoilerRessources);

		// Add own taken photo
		directory = CoreSettingsForward.UserImageFolder;
		if (directory != null)
		{
			reloadSpoilerResourcesFromPath(directory, spoilerRessources);
		}
	}

	private void reloadSpoilerResourcesFromPath(String directory, ArrayList<ImageEntry> spoilerResources)
	{
		if (!FileIO.DirectoryExists(directory)) return;
		// Logger.DEBUG("Loading spoilers from " + directory);
		File dir = new File(directory);
		FilenameFilter filter = new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String filename)
			{
				filename = filename.toLowerCase();
				if (filename.indexOf(GcCode.toLowerCase()) >= 0)
				{
					if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".bmp") || filename.endsWith(".png")
							|| filename.endsWith(".gif")) return true;
				}
				return false;
			}
		};
		String[] files = dir.list(filter);
		if (!(files == null))
		{
			if (files.length > 0)
			{
				for (String file : files)
				{
					String ext = FileIO.GetFileExtension(file);
					if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("bmp")
							|| ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("gif"))
					{
						ImageEntry imageEntry = new ImageEntry();
						imageEntry.LocalPath = directory + "/" + file;
						imageEntry.Name = file;
						spoilerRessources.add(imageEntry);
					}
				}
			}
		}
	}

	/**
	 * Converts the type string into an element of the CacheType enumeration.
	 * 
	 * @param type
	 */
	public void parseCacheTypeString(String type)
	{
		this.Type = CacheTypes.parseString(type);
	}

	/**
	 * Gibt die Entfernung zur übergebenen User Position als Float zurück und Speichert die Aktueller User Position für alle Caches ab.
	 * 
	 * @return Entfernung zur übergebenen User Position als Float
	 */
	public float Distance(boolean useFinal)
	{
		return Distance(useFinal, Locator.getCoordinate());
	}

	public float Distance(boolean useFinal, Coordinate fromPos)
	{
		Waypoint waypoint = null;
		if (useFinal) waypoint = this.GetFinalWaypoint();
		// Wenn ein Mystery-Cache einen Final-Waypoint hat, soll die
		// Diszanzberechnung vom Final aus gemacht werden
		// If a mystery has a final waypoint, the distance will be calculated to
		// the final not the the cache coordinates
		Coordinate toPos = Pos;
		if (waypoint != null)
		{
			toPos = new Coordinate(waypoint.Pos.getLatitude(), waypoint.Pos.getLongitude());
			// nur sinnvolles Final, sonst vom Cache
			if (waypoint.Pos.getLatitude() == 0 && waypoint.Pos.getLongitude() == 0) toPos = Pos;
		}
		float[] dist = new float[4];
		Coordinate.distanceBetween(fromPos.getLatitude(), fromPos.getLongitude(), toPos.getLatitude(), toPos.getLongitude(), dist);
		cachedDistance = dist[0];
		return cachedDistance;
	}

	public boolean isAttributePositiveSet(Attributes attribute)
	{
		return attributesPositive.BitAndBiggerNull(Attributes.GetAttributeDlong(attribute));
		// return (attributesPositive & Attributes.GetAttributeDlong(attribute))
		// > 0;
	}

	public boolean isAttributeNegativeSet(Attributes attribute)
	{
		return attributesNegative.BitAndBiggerNull(Attributes.GetAttributeDlong(attribute));
		// return (attributesNegative & Attributes.GetAttributeDlong(attribute))
		// > 0;
	}

	public void addAttributeNegative(Attributes attribute)
	{
		if (attributesNegative == null) attributesNegative = new DLong(0, 0);
		attributesNegative.BitOr(Attributes.GetAttributeDlong(attribute));
	}

	public void addAttributePositive(Attributes attribute)
	{
		if (attributesPositive == null) attributesPositive = new DLong(0, 0);
		attributesPositive.BitOr(Attributes.GetAttributeDlong(attribute));
	}

	/*
	 * Overrides
	 */

	@Override
	public int compareTo(Cache c2)
	{
		float dist1 = this.cachedDistance;
		float dist2 = c2.cachedDistance;
		return (dist1 < dist2 ? -1 : (dist1 == dist2 ? 0 : 1));
	}

	public void setAttributesPositive(DLong i)
	{
		attributesPositive = i;
	}

	public void setAttributesNegative(DLong i)
	{
		attributesNegative = i;
	}

	public DLong getAttributesNegative()
	{
		if (this.attributesNegative == null)
		{
			CoreCursor c = Database.Data.rawQuery("select AttributesNegative,AttributesNegativeHigh from Caches where Id=?", new String[]
				{ String.valueOf(this.Id) });
			c.moveToFirst();
			while (c.isAfterLast() == false)
			{
				if (!c.isNull(0)) this.attributesNegative = new DLong(c.getLong(1), c.getLong(0));
				else
					this.attributesNegative = new DLong(0, 0);
				break;
			}
			;
			c.close();
		}
		return this.attributesNegative;
	}

	public DLong getAttributesPositive()
	{
		if (this.attributesPositive == null)
		{
			CoreCursor c = Database.Data.rawQuery("select AttributesPositive,AttributesPositiveHigh from Caches where Id=?", new String[]
				{ String.valueOf(this.Id) });
			c.moveToFirst();
			while (c.isAfterLast() == false)
			{
				if (!c.isNull(0)) this.attributesPositive = new DLong(c.getLong(1), c.getLong(0));
				else
					this.attributesPositive = new DLong(0, 0);
				break;
			}
			;
			c.close();
		}
		return this.attributesPositive;
	}

	private ArrayList<Attributes> AttributeList = null;

	public ArrayList<Attributes> getAttributes()
	{
		if (AttributeList == null)
		{
			AttributeList = Attributes.getAttributes(this.getAttributesPositive(), this.getAttributesNegative());
		}

		return AttributeList;
	}

	public void clear()
	{
		MapX = 0;
		MapY = 0;
		GcId = "";
		Id = -1;
		GcCode = "";
		Name = "";
		Pos = new Coordinate();
		Rating = 0;
		Size = null;
		Difficulty = 0;
		Terrain = 0;
		Archived = false;
		Available = false;
		ApiStatus = 0;
		favorite = false;
		noteCheckSum = 0;
		solverCheckSum = 0;
		hasUserData = false;
		CorrectedCoordinates = false;
		Found = false;
		TourName = "";
		GPXFilename_ID = 0;
		Type = CacheTypes.Undefined;
		PlacedBy = "";
		Owner = "";
		DateHidden = null;
		Url = "";
		listingChanged = false;
		attributesPositive = new DLong(0, 0);
		attributesNegative = new DLong(0, 0);
		NumTravelbugs = 0;
		cachedDistance = 0;
		hint = "";
		waypoints = new ArrayList<Waypoint>();
		spoilerRessources = null;
		shortDescription = "";
		longDescription = "";
		myCache = -1;
		gcLogin = null;
		if (AttributeList != null) AttributeList.clear();
		AttributeList = null;
	}

	private boolean isSearchVisible = true;

	public void setSearchVisible(boolean value)
	{
		isSearchVisible = value;
	}

	public boolean isSearchVisible()
	{
		return isSearchVisible;
	}

	public boolean hasCorrectedCoordinates()
	{
		return CorrectedCoordinates;
	}

	public void setCorrectedCoordinates(boolean correctedCoordinates)
	{
		this.CorrectedCoordinates = correctedCoordinates;
	}

	public Waypoint findWaypointByGc(String gc)
	{
		for (Waypoint waypoint : waypoints)
		{
			if (waypoint.GcCode.equals(gc))
			{
				return waypoint;
			}
		}
		return null;
	}

	// copy all Informations from cache into this
	// this is used after actualization of cache with API
	public void copyFrom(Cache cache)
	{
		if (AttributeList != null) AttributeList.clear();
		AttributeList = null;
		this.MapX = cache.MapX;
		this.MapY = cache.MapY;
		this.Name = cache.Name;
		this.Pos = cache.Pos;
		this.Rating = cache.Rating;
		this.Size = cache.Size;
		this.Difficulty = cache.Difficulty;
		this.Terrain = cache.Terrain;
		this.Archived = cache.Archived;
		this.Available = cache.Available;
		this.ApiStatus = cache.ApiStatus;
		// this.favorite = false;
		// this.noteCheckSum = 0;
		// this.solverCheckSum = 0;
		// this.hasUserData = false;
		// this.CorrectedCoordinates = false;
		// only change the found status when it is true in the loaded cache
		// This will prevent ACB from overriding a found cache which is still not found in GC
		if (cache.Found) this.Found = cache.Found;
		// this.TourName = "";
		// this.GPXFilename_ID = 0;
		this.Type = cache.Type;
		this.PlacedBy = cache.PlacedBy;
		this.Owner = cache.Owner;
		this.DateHidden = cache.DateHidden;
		this.Url = cache.Url;
		this.listingChanged = true; // so that spoiler download will be done again
		this.attributesPositive = cache.attributesPositive;
		this.attributesNegative = cache.attributesNegative;
		this.NumTravelbugs = cache.NumTravelbugs;
		// this.cachedDistance = 0;
		this.hint = cache.hint;
		// do not copy waypoints List directly because actual user defined Waypoints would be deleted
		// this.waypoints = new ArrayList<Waypoint>();
		for (Waypoint newWaypoint : cache.waypoints)
		{

			Waypoint aktWaypoint = this.findWaypointByGc(newWaypoint.GcCode);
			if (aktWaypoint == null)
			{
				// this waypoint is new -> Add to list
				this.waypoints.add(newWaypoint);
			}
			else
			{
				// this waypoint is already in our list -> Copy Informations
				aktWaypoint.Description = newWaypoint.Description;
				aktWaypoint.Pos = newWaypoint.Pos;
				aktWaypoint.Title = newWaypoint.Title;
				aktWaypoint.Type = newWaypoint.Type;
			}
		}
		// this.spoilerRessources = null;
		this.shortDescription = cache.shortDescription;
		this.longDescription = cache.longDescription;
		this.myCache = cache.myCache;
		// this.gcLogin = null;

	}

}
