package CB_Core.Types;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;

import CB_Core.Config;
import CB_Core.FileIO;
import CB_Core.GlobalCore;
import CB_Core.DAO.ImageDAO;
import CB_Core.DB.CoreCursor;
import CB_Core.DB.Database;
import CB_Core.Enums.Attributes;
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
		return Pos.Latitude;
	}

	/**
	 * Längengrad
	 */
	public double Longitude()
	{
		return Pos.Longitude;
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

	/**
	 * for Replication
	 */
	public int solverCheckSum = 0;

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

	public boolean ImTheOwner()
	{
		if (myCache == 0) return false;
		if (myCache == 1) return true;

		if (gcLogin == null)
		{
			gcLogin = Config.settings.GcLogin.getValue().toLowerCase();
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
		waypoints = new ArrayList<Waypoint>();
	}

	/**
	 * Constructor
	 */
	public Cache(double Latitude, double Longitude, String Name, CacheTypes type, String GcCode)
	{
		this.Pos.Latitude = Latitude;
		this.Pos.Longitude = Longitude;
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
	}

	/*
	 * Getter/Setter
	 */

	/**
	 * wenn ein Wegpunkt "Final" existiert, ist das mystery-Rätsel gelöst.
	 */
	public boolean MysterySolved()
	{
		if (this.CorrectedCoordinates) return true;

		if (this.Type != CacheTypes.Mystery) return false;

		boolean x;
		x = false;

		ArrayList<Waypoint> wps = waypoints;
		for (Waypoint wp : wps)
		{
			if (wp.Type == CacheTypes.Final)
			{
				if (!(wp.Latitude() == 0 && wp.Longitude() == 0)) x = true;
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
				return wp;
			}
		}
		;

		return null;
	}

	/**
	 * @return Entfernung zur aktUserPos als Float
	 */
	public float CachedDistance()
	{
		if (cachedDistance != 0) return cachedDistance;
		else
			return Distance(true);
	}

	/**
	 * Returns a List of Spoiler Ressources
	 * 
	 * @return ArrayList of String
	 */
	public ArrayList<ImageEntry> SpoilerRessources()
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
		if (SpoilerRessources() == null) return false;
		return SpoilerRessources().size() > 0;
	}

	public void ReloadSpoilerRessources()
	{
		spoilerRessources = new ArrayList<ImageEntry>();

		String path = Config.settings.SpoilerFolder.getValue();
		String directory = path + "/" + GcCode.substring(0, 4);

		if (!FileIO.DirectoryExists(directory)) return;

		File dir = new File(directory);
		FilenameFilter filter = new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String filename)
			{

				filename = filename.toLowerCase();
				if (filename.indexOf(GcCode.toLowerCase()) == 0)
				{
					if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".bmp") || filename.endsWith(".png")
							|| filename.endsWith(".gif")) return true;
				}
				return false;
			}
		};
		String[] files = dir.list(filter);

		for (String image : files)
		{
			ImageEntry imageEntry = new ImageEntry();
			imageEntry.LocalPath = directory + "/" + image;
			imageEntry.Name = image;
			spoilerRessources.add(imageEntry);
		}

		// Add own taken photo
		directory = Config.settings.UserImageFolder.getValue();

		if (!FileIO.DirectoryExists(directory)) return;

		dir = new File(directory);
		filter = new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String filename)
			{

				filename = filename.toLowerCase();
				if (filename.indexOf(GcCode.toLowerCase()) >= 0) return true;
				return false;
			}
		};
		files = dir.list(filter);
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

		ImageDAO imageDAO = new ImageDAO();

		ArrayList<ImageEntry> descImages = imageDAO.getDescriptionImagesForCache(GcCode);

		for (ImageEntry image : descImages)
		{
			if (FileIO.FileExists(image.LocalPath))
			{
				spoilerRessources.add(image);
			}
		}

	}

	/**
	 * Returns the MapIconId of this Cache
	 * 
	 * @return interger
	 */
	public int GetMapIconId()
	{
		if (this.ImTheOwner()) return 22;
		if (this.Found) return 19;
		if ((Type == CacheTypes.Mystery) && this.MysterySolved()) return 21;

		return Type.ordinal();
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
		Coordinate fromPos = GlobalCore.LastValidPosition;
		Waypoint waypoint = null;
		if (useFinal) waypoint = this.GetFinalWaypoint();
		// Wenn ein Mystery-Cache einen Final-Waypoint hat, soll die
		// Diszanzberechnung vom Final aus gemacht werden
		// If a mystery has a final waypoint, the distance will be calculated to
		// the final not the the cache coordinates
		Coordinate toPos = Pos;
		if (waypoint != null) toPos = new Coordinate(waypoint.Pos.Latitude, waypoint.Pos.Longitude);
		float[] dist = new float[4];
		Coordinate.distanceBetween(fromPos.Latitude, fromPos.Longitude, toPos.Latitude, toPos.Longitude, dist);
		cachedDistance = dist[0];
		return (float) cachedDistance;
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
		float dist1 = this.CachedDistance();
		float dist2 = c2.CachedDistance();
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

}
