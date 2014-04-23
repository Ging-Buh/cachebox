/* 
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package CB_Core.Types;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Settings.CB_Core_Settings;
import CB_Locator.CoordinateGPS;
import CB_Utils.DB.CoreCursor;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.FileIO;

/**
 * @author ging-buh
 * @author Longri
 */
public class Cache extends CacheLite
{

	private static final long serialVersionUID = 3442053093499951966L;

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
	 * Id des Caches bei geocaching.com. Wird zumm Loggen benötigt und von geotoad nicht exportiert
	 */
	// TODO Warum ist das ein String?
	private byte[] GcId;

	/**
	 * Erschaffer des Caches
	 */
	public String PlacedBy = "";

	/**
	 * Datum, an dem der Cache versteckt wurde
	 */
	public Date DateHidden;

	/**
	 * ApiStatus 0: Cache wurde nicht per Api hinzugefügt 1: Cache wurde per GC Api hinzugefügt und ist noch nicht komplett geladen (IsLite
	 * = true) 2: Cache wurde per GC Api hinzugefügt und ist komplett geladen (IsLite = false)
	 */
	public byte ApiStatus;

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
	 * Name der Tour, wenn die GPX-Datei aus GCTour importiert wurde
	 */
	public String TourName = "";

	/**
	 * Name der GPX-Datei aus der importiert wurde
	 */
	public long GPXFilename_ID = 0;

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
	 * Hinweis für diesen Cache
	 */
	private String hint = "";

	/**
	 * Liste der Spoiler Resorcen
	 */
	public CB_List<ImageEntry> spoilerRessources = null;

	/**
	 * Kurz Beschreibung des Caches
	 */
	public String shortDescription;

	/**
	 * Ausführliche Beschreibung des Caches Nur für Import Zwecke. Ist normalerweise leer, da die Description bei aus Speicherplatz Gründen
	 * bei Bedarf aus der DB geladen wird
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
		this.DateHidden = new Date();
		this.NumTravelbugs = 0;
		this.Difficulty = 0;
		this.Terrain = 0;
		this.Size = CacheSizes.other;
		this.Available = true;
	}

	/**
	 * Constructor
	 */
	public Cache(double Latitude, double Longitude, String Name, CacheTypes type, String GcCode)
	{
		this.Pos.setLatitude(Latitude);
		this.Pos.setLongitude(Longitude);
		this.setName(Name);
		this.Type = type;
		this.DateHidden = new Date();
		this.setGcCode(GcCode);
		this.NumTravelbugs = 0;
		this.Difficulty = 0;
		this.Terrain = 0;
		this.Size = CacheSizes.other;
		this.Available = true;
		AttributeList = null;
	}

	/*
	 * Getter/Setter
	 */

	public Cache(CacheLite cacheLite)
	{
		if (cacheLite == null) return;

		if (cacheLite.getGcCode().equals("CBPark"))
		{
			this.Pos = cacheLite.Pos;
			this.MapX = cacheLite.MapX;
			this.MapY = cacheLite.MapY;
			this.setGcCode(cacheLite.getGcCode());
			this.setName(cacheLite.getName());
			this.Type = cacheLite.Type;
			return;
		}

		// CacheLiteValues:
		this.myCache = cacheLite.myCache;
		this.MapX = cacheLite.MapX;
		this.MapY = cacheLite.MapY;
		this.GcCode = cacheLite.GcCode;
		this.Name = cacheLite.Name;
		this.Pos = cacheLite.Pos;
		this.Rating = cacheLite.Rating;
		this.NumTravelbugs = cacheLite.NumTravelbugs;
		this.Id = cacheLite.Id;
		this.Size = cacheLite.Size;
		this.Difficulty = cacheLite.Difficulty;
		this.Terrain = cacheLite.Terrain;
		this.Archived = cacheLite.Archived;
		this.Available = cacheLite.Available;
		this.Found = cacheLite.Found;
		this.Type = cacheLite.Type;
		this.cachedDistance = cacheLite.cachedDistance;
		this.Owner = cacheLite.Owner;
		this.hasFinalWaypoint = cacheLite.hasFinalWaypoint;
		this.hasStartWaypoint = cacheLite.hasStartWaypoint;
		this.FinalWaypoint = cacheLite.FinalWaypoint;
		this.startWaypoint = cacheLite.startWaypoint;
		this.hasHint = cacheLite.hasHint;

		// read missing values from DB
		CoreCursor reader = Database.Data
				.rawQuery(
						"select GcId, ApiStatus, CorrectedCoordinates, HasUserData, TourName, GpxFilename_ID, Url, Country, State, ListingChanged, PlacedBy, DateHidden, AttributesPositive, AttributesPositiveHigh, AttributesNegative, AttributesNegativeHigh from Caches where id = ?",
						new String[]
							{ String.valueOf(this.Id) });
		reader.moveToFirst();
		while (!reader.isAfterLast())
		{
			this.setGcId(reader.getString(0));

			if (reader.isNull(1)) this.ApiStatus = 0;
			else
				this.ApiStatus = (byte) reader.getInt(1);

			this.CorrectedCoordinates = (reader.getInt(2) > 0);
			this.hasUserData = (reader.getInt(3) > 0);
			this.TourName = reader.getString(4);
			this.GPXFilename_ID = reader.getLong(5);
			this.Url = reader.getString(6);
			this.Country = reader.getString(7);
			this.State = reader.getString(8);
			this.listingChanged = (reader.getInt(9) > 0);
			this.PlacedBy = reader.getString(10);

			String sDate = reader.getString(11);
			DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try
			{
				this.DateHidden = iso8601Format.parse(sDate);
			}
			catch (ParseException e)
			{
			}

			this.setAttributesPositive(new DLong(reader.getLong(13), reader.getLong(12)));
			this.setAttributesNegative(new DLong(reader.getLong(15), reader.getLong(14)));

			reader.moveToNext();

		}
		reader.close();

		this.waypoints.clear();

		WaypointDAO wDao = new WaypointDAO();
		CB_List<WaypointLite> wayPois = wDao.getWaypointsFromCacheID(this.Id, true);

		for (int i = 0, n = wayPois.size(); i < n; i++)
		{
			this.waypoints.add(wayPois.get(i));
		}

	}

	/**
	 * Returns a List of Spoiler Ressources
	 * 
	 * @return ArrayList of String
	 */
	public CB_List<ImageEntry> getSpoilerRessources()
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
	public void setSpoilerRessources(CB_List<ImageEntry> value)
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
		try
		{
			if (spoilerRessources == null) ReloadSpoilerRessources();
			return spoilerRessources.size() > 0;
		}
		catch (Exception e)
		{
			return false;
		}
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
		spoilerRessources = new CB_List<ImageEntry>();

		String directory = "";

		try
		{
			// from own Repository
			String path = CB_Core_Settings.SpoilerFolderLocal.getValue();
			if (path != null && path.length() > 0)
			{
				directory = path + "/" + getGcCode().substring(0, 4);
				reloadSpoilerResourcesFromPath(directory, spoilerRessources);
			}

			// from Global Repository
			path = CB_Core_Settings.DescriptionImageFolder.getValue();
			directory = path + "/" + getGcCode().substring(0, 4);
			reloadSpoilerResourcesFromPath(directory, spoilerRessources);

			// Spoilers are always loaden from global Repository too
			// from globalUser changed Repository
			path = CB_Core_Settings.SpoilerFolder.getValue();
			directory = path + "/" + getGcCode().substring(0, 4);
			reloadSpoilerResourcesFromPath(directory, spoilerRessources);

			// Add own taken photo
			directory = CB_Core_Settings.UserImageFolder.getValue();
			if (directory != null)
			{
				reloadSpoilerResourcesFromPath(directory, spoilerRessources);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void reloadSpoilerResourcesFromPath(String directory, CB_List<ImageEntry> spoilerRessources2)
	{
		if (!FileIO.DirectoryExists(directory)) return;
		// Logger.DEBUG("Loading spoilers from " + directory);
		File dir = new File(directory);
		FilenameFilter filter = new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String filename)
			{
				filename = filename.toLowerCase(Locale.getDefault());
				if (filename.indexOf(getGcCode().toLowerCase(Locale.getDefault())) >= 0)
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
		setGcId("");
		Id = -1;
		setGcCode("");
		setName("");
		Pos = new CoordinateGPS();
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
		setOwner("");
		DateHidden = null;
		Url = "";
		listingChanged = false;
		attributesPositive = new DLong(0, 0);
		attributesNegative = new DLong(0, 0);
		NumTravelbugs = 0;
		cachedDistance = 0;
		setHint("");
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

	public WaypointLite findWaypointByGc(String gc)
	{
		if (waypoints == null) return null;
		for (int i = 0, n = waypoints.size(); i < n; i++)
		{
			WaypointLite wp = waypoints.get(i);
			if (wp.getGcCode().equals(gc))
			{
				return wp;
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
		this.setName(cache.getName());
		this.Pos = cache.Pos;
		this.Rating = cache.Rating;
		this.Size = cache.Size;
		this.Difficulty = cache.Difficulty;
		this.Terrain = cache.Terrain;
		this.Archived = cache.Archived;
		this.Available = cache.Available;
		this.ApiStatus = cache.ApiStatus;

		// only change the found status when it is true in the loaded cache
		// This will prevent ACB from overriding a found cache which is still not found in GC
		if (cache.Found) this.Found = cache.Found;

		this.Type = cache.Type;
		this.PlacedBy = cache.PlacedBy;
		this.setOwner(cache.getOwner());
		this.DateHidden = cache.DateHidden;
		this.Url = cache.Url;
		this.listingChanged = true; // so that spoiler download will be done again
		this.attributesPositive = cache.attributesPositive;
		this.attributesNegative = cache.attributesNegative;
		this.NumTravelbugs = cache.NumTravelbugs;

		this.setHint(cache.getHint());
		// do not copy waypoints List directly because actual user defined Waypoints would be deleted
		// this.waypoints = new ArrayList<Waypoint>();

		for (int i = 0, n = cache.waypoints.size(); i < n; i++)
		{
			WaypointLite newWaypoint = cache.waypoints.get(i);

			WaypointLite aktWaypoint = this.findWaypointByGc(newWaypoint.getGcCode());
			if (aktWaypoint == null)
			{
				// this waypoint is new -> Add to list
				this.waypoints.add(newWaypoint);
			}
			else
			{
				// this waypoint is already in our list -> Copy Informations
				aktWaypoint.Pos = newWaypoint.Pos;
				aktWaypoint.Type = newWaypoint.Type;
			}
		}

		this.shortDescription = cache.shortDescription;
		this.longDescription = cache.longDescription;
		this.myCache = cache.myCache;

	}

	@Override
	public String toString()
	{
		return "Cache:" + getGcCode() + " " + Pos.toString();
	}

	public void dispose()
	{
		// clear all Lists
		if (AttributeList != null)
		{
			AttributeList.clear();
			AttributeList = null;
		}

		if (spoilerRessources != null)
		{
			for (int i = 0, n = spoilerRessources.size(); i < n; i++)
			{
				ImageEntry entry = spoilerRessources.get(i);
				entry.dispose();
			}
			spoilerRessources.clear();
			spoilerRessources = null;
		}

		if (waypoints != null)
		{
			for (int i = 0, n = waypoints.size(); i < n; i++)
			{
				WaypointLite entry = waypoints.get(i);
				entry.dispose();
			}

			waypoints.clear();
		}

		tmpNote = null;
		tmpSolver = null;
		TourName = null;
		PlacedBy = null;
		setOwner(null);
		DateHidden = null;
		Url = null;
		Country = null;
		State = null;
		setHint(null);
		shortDescription = null;
		longDescription = null;

	}

	/**
	 * When Solver1 changes -> this flag must be set. When Solver 2 will be opend and this flag is set -> Solver 2 must reload the content
	 * from DB to get the changes from Solver 1
	 */
	private boolean solver1Changed = false;

	public void setSolver1Changed(boolean b)
	{
		this.solver1Changed = b;
	}

	public boolean getSolver1Changed()
	{
		return solver1Changed;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null) return false;
		if (o instanceof Cache)
		{
			Cache c = (Cache) o;

			if (!Arrays.equals(this.GcCode, c.GcCode)) return false;
			if (!this.Pos.equals(c.Pos)) return false;
			return true;
		}
		else if (o instanceof CacheLite)
		{
			CacheLite c = (CacheLite) o;

			if (!Arrays.equals(this.GcCode, c.GcCode)) return false;
			if (!this.Pos.equals(c.Pos)) return false;
			return true;
		}
		return false;
	}

	public String getGcId()
	{
		if (GcId == null) return EMPTY_STRING;
		return new String(GcId, US_ASCII);
	}

	public void setGcId(String gcId)
	{
		if (gcId == null)
		{
			GcId = null;
			return;
		}
		GcId = gcId.getBytes(US_ASCII);
	}

	public String getHint()
	{
		return hint;
	}

	public void setHint(String hint)
	{
		this.hint = hint;
	}

}
