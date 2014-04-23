package CB_Core.Types;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Locale;

import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Settings.CB_Core_Settings;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.DB.CoreCursor;
import CB_Utils.Lists.CB_List;

public class CacheLite implements Comparable<CacheLite>, Serializable
{
	private static final long serialVersionUID = 1015307624242318838L;

	protected static final Charset US_ASCII = Charset.forName("US-ASCII");
	protected static final Charset UTF_8 = Charset.forName("UTF-8");
	protected static final String EMPTY_STRING = "";

	/**
	 * Static holden UserName
	 */
	static String gcLogin = null;

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
	 * Waypoint Code des Caches
	 */
	protected byte[] GcCode;
	/**
	 * Name des Caches
	 */
	protected byte[] Name;
	/**
	 * Die Coordinate, an der der Cache liegt.
	 */
	public Coordinate Pos = new Coordinate();
	/**
	 * Durchschnittliche Bewertung des Caches von GcVote
	 */
	public float Rating;

	/**
	 * Anzahl der Travelbugs und Coins, die sich in diesem Cache befinden
	 */
	public int NumTravelbugs = 0;

	/**
	 * Id des Caches in der Datenbank von geocaching.com
	 */
	public long Id;

	/**
	 * Verantwortlicher
	 */
	protected byte[] Owner;

	/**
	 * Bin ich der Owner? </br>-1 noch nicht getestet </br>1 ja </br>0 nein
	 */
	int myCache = -1;

	/**
	 * Liste der zusätzlichen Wegpunkte des Caches
	 */
	public final CB_List<WaypointLite> waypoints = new CB_List<WaypointLite>();

	/**
	 * Constructor
	 */
	public CacheLite(double Latitude, double Longitude, String Name, CacheTypes type, String GcCode)
	{
		this.Pos.setLatitude(Latitude);
		this.Pos.setLongitude(Longitude);
		this.setName(Name);
		this.Type = type;
		this.setGcCode(GcCode);
		this.Difficulty = 0;
		this.Terrain = 0;
		this.Size = CacheSizes.other;
		this.setAvailable(true);
	}

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
	 * Breitengrad
	 */
	public double Latitude(Boolean useFinal)
	{
		WaypointLite waypoint = null;
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
		return toPos.getLatitude();
	}

	/**
	 * Längengrad
	 */
	public double Longitude(Boolean useFinal)
	{
		WaypointLite waypoint = null;
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
		return toPos.getLongitude();
	}

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

	public void setFavorit(boolean value)
	{
		setFavorite(value);

	}

	public String getHintFromDB()
	{
		String ret = "";

		CoreCursor reader = Database.Data.rawQuery("select Hint from Caches where id = ?", new String[]
			{ String.valueOf(this.Id) });
		reader.moveToFirst();
		while (!reader.isAfterLast())
		{
			ret = reader.getString(0);
			reader.moveToNext();

		}
		reader.close();

		return ret;
	}

	/**
	 * Art des Caches
	 */
	public CacheTypes Type = CacheTypes.Undefined;

	/**
	 * Falls keine erneute Distanzberechnung nötig ist nehmen wir diese Distanz
	 */
	public float cachedDistance = 0;

	/**
	 * -1 = not readed from DB <br>
	 * 0 = now Start WayPoint <br>
	 * 1 = have start WayPoint
	 */
	public byte hasStartWaypoint = -1;

	/**
	 * The start WayPoint of this Cahe, if exist
	 */
	public WaypointLite startWaypoint = null;

	/**
	 * -1 = not readed from DB <br>
	 * 0 = now Start WayPoint <br>
	 * 1 = have start WayPoint
	 */
	public byte hasFinalWaypoint = -1;

	/**
	 * The start WayPoint of this Cahe, if exist
	 */
	public WaypointLite FinalWaypoint = null;

	public CacheLite()
	{
		super();
	}

	/**
	 * @param userName
	 *            Config.settings.GcLogin.getValue()
	 * @return
	 */
	public boolean ImTheOwner()
	{
		String userName = CB_Core_Settings.GcLogin.getValue().toLowerCase(Locale.getDefault());
		if (myCache == 0) return false;
		if (myCache == 1) return true;

		if (gcLogin == null)
		{
			gcLogin = userName;
		}

		boolean ret = false;

		try
		{
			ret = this.getOwner().toLowerCase(Locale.getDefault()).equals(gcLogin);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		myCache = ret ? 1 : 0;
		return ret;
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
	public WaypointLite GetFinalWaypoint()
	{
		if (this.Type != CacheTypes.Mystery && this.Type != CacheTypes.Multi) return null;

		if (this.hasFinalWaypoint > -1) return this.FinalWaypoint;

		WaypointDAO dao = new WaypointDAO();

		CB_List<WaypointLite> waypoints = dao.getWaypointsFromCacheID(this.Id, false);

		for (int i = 0, n = waypoints.size(); i < n; i++)
		{
			WaypointLite wp = waypoints.get(i);
			if (wp.Type == CacheTypes.Final)
			{
				// do not activate final waypoint with invalid coordinates
				if (!wp.Pos.isValid() || wp.Pos.isZero()) continue;
				this.FinalWaypoint = wp;
				this.hasFinalWaypoint = 1;
				return wp;
			}
		}
		;
		this.hasFinalWaypoint = 0;
		return null;
	}

	/**
	 * -- korrigierte Koordinaten (kommt nur aus GSAK? bzw CacheWolf-Import) -- oder Mystery mit gültigem Final
	 */
	public boolean CorrectedCoordiantesOrMysterySolved()
	{
		if (this.hasCorrectedCoordinates()) return true;

		if (this.Type != CacheTypes.Mystery) return false;

		WaypointLite wp = GetFinalWaypoint();
		if (wp == null) return false;
		if (wp.Type == CacheTypes.Final)
		{
			if (!(wp.Pos.getLatitude() == 0 && wp.Pos.getLongitude() == 0)) return true;
		}

		return false;
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
	public WaypointLite GetStartWaypoint()
	{
		if ((this.Type != CacheTypes.Multi) && (this.Type != CacheTypes.Mystery)) return null;

		if (this.hasStartWaypoint > -1) return this.startWaypoint;

		// Read Waypooints from DB

		WaypointDAO dao = new WaypointDAO();

		CB_List<WaypointLite> waypoints = dao.getWaypointsFromCacheID(this.Id, false);

		for (int i = 0, n = waypoints.size(); i < n; i++)
		{
			WaypointLite wp = waypoints.get(i);
			if ((wp.Type == CacheTypes.MultiStage) && (wp.IsStart))
			{
				this.hasStartWaypoint = 1;
				this.startWaypoint = wp;
				return wp;
			}
		}
		this.hasStartWaypoint = 0;
		return null;
	}

	/**
	 * @return Entfernung zur aktUserPos als Float
	 */
	public float CachedDistance(CalculationType type)
	{
		if (cachedDistance != 0)
		{
			return cachedDistance;
		}
		else
		{
			return Distance(type, true);
		}
	}

	/**
	 * Gibt die Entfernung zur übergebenen User Position als Float zurück und Speichert die Aktueller User Position für alle Caches ab.
	 * 
	 * @return Entfernung zur übergebenen User Position als Float
	 */
	public float Distance(CalculationType type, boolean useFinal)
	{
		return Distance(type, useFinal, Locator.getCoordinate());
	}

	public float Distance(CalculationType type, boolean useFinal, Coordinate fromPos)
	{
		WaypointLite waypoint = null;
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
		MathUtils.computeDistanceAndBearing(type, fromPos.getLatitude(), fromPos.getLongitude(), toPos.getLatitude(), toPos.getLongitude(),
				dist);
		cachedDistance = dist[0];
		return cachedDistance;
	}

	public void dispose()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public int compareTo(CacheLite c2)
	{
		float dist1 = this.cachedDistance;
		float dist2 = c2.cachedDistance;
		return (dist1 < dist2 ? -1 : (dist1 == dist2 ? 0 : 1));
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null) return false;
		if (o instanceof Cache)
		{
			Cache c = (Cache) o;

			if (!this.getGcCode().equals(c.getGcCode())) return false;
			if (!this.Pos.equals(c.Pos)) return false;
			return true;
		}
		else if (o instanceof CacheLite)
		{
			CacheLite c = (CacheLite) o;

			if (!this.getGcCode().equals(c.getGcCode())) return false;
			if (!this.Pos.equals(c.Pos)) return false;
			return true;
		}
		return false;
	}

	@Override
	public String toString()
	{
		return "CacheLite:" + getGcCode() + " " + Pos.toString();
	}

	public String getGcCode()
	{
		if (GcCode == null) return EMPTY_STRING;
		return new String(GcCode, US_ASCII);
	}

	public void setGcCode(String gcCode)
	{
		if (gcCode == null)
		{
			GcCode = null;
			return;
		}
		GcCode = gcCode.getBytes(US_ASCII);
	}

	public String getName()
	{
		if (Name == null) return EMPTY_STRING;
		return new String(Name, UTF_8);
	}

	public void setName(String name)
	{
		if (name == null)
		{
			Name = null;
			return;
		}
		Name = name.getBytes(UTF_8);
	}

	public String getOwner()
	{
		if (Owner == null) return EMPTY_STRING;
		return new String(Owner, UTF_8);
	}

	public void setOwner(String owner)
	{
		if (owner == null)
		{
			Owner = null;
			return;
		}
		Owner = owner.getBytes(UTF_8);
	}

	public void setValues(Cache cache)
	{
		this.myCache = cache.myCache;
		this.MapX = cache.MapX;
		this.MapY = cache.MapY;
		this.GcCode = cache.GcCode;
		this.Name = cache.Name;
		this.Pos = cache.Pos;
		this.Rating = cache.Rating;
		this.NumTravelbugs = cache.NumTravelbugs;
		this.Id = cache.Id;
		this.Size = cache.Size;
		this.Difficulty = cache.Difficulty;
		this.Terrain = cache.Terrain;
		this.Type = cache.Type;
		this.cachedDistance = cache.cachedDistance;
		this.Owner = cache.Owner;
		this.hasFinalWaypoint = cache.hasFinalWaypoint;
		this.hasStartWaypoint = cache.hasStartWaypoint;
		this.FinalWaypoint = cache.FinalWaypoint;
		this.startWaypoint = cache.startWaypoint;
		this.BitFlags = cache.BitFlags;
	}

	// ########################################################
	// Boolean Handling
	// one Boolean use up to 4 Bytes
	// Boolean data type represents one bit of information, but its "size" isn't something that's precisely defined. (Oracle Docs)
	//
	// so we use one Short for Store all Boolean and Use a BitMask
	// ########################################################

	// Masks
	protected final static short MASK_HAS_HINT = 1 << 0;
	protected final static short MASK_CORECTED_COORDS = 1 << 1;
	protected final static short MASK_ARCHIVED = 1 << 2;
	protected final static short MASK_AVAILABLE = 1 << 3;
	protected final static short MASK_VAVORITE = 1 << 4;
	protected final static short MASK_FOUND = 1 << 5;

	protected short BitFlags = 0;

	protected boolean getMaskValue(short mask)
	{
		return (BitFlags & mask) == mask;
	}

	protected void setMaskValue(short mask, boolean value)
	{
		if (getMaskValue(mask) == value) return;

		if (value)
		{
			BitFlags |= mask;
		}
		else
		{
			BitFlags &= ~mask;
		}

	}

	// Getter and Setter over Mask

	public boolean hasHint()
	{
		return getMaskValue(MASK_HAS_HINT);
	}

	public void setHasHint(boolean b)
	{
		setMaskValue(MASK_HAS_HINT, b);
	}

	public boolean hasCorrectedCoordinates()
	{
		return this.getMaskValue(MASK_CORECTED_COORDS);
	}

	public void setCorrectedCoordinates(boolean correctedCoordinates)
	{
		this.setMaskValue(MASK_CORECTED_COORDS, correctedCoordinates);
	}

	public boolean isArchived()
	{
		return this.getMaskValue(MASK_ARCHIVED);
	}

	public void setArchived(boolean archived)
	{
		this.setMaskValue(MASK_ARCHIVED, archived);
	}

	public boolean isAvailable()
	{
		return this.getMaskValue(MASK_AVAILABLE);
	}

	public void setAvailable(boolean available)
	{
		this.setMaskValue(MASK_AVAILABLE, available);
	}

	public boolean isFavorite()
	{
		return this.getMaskValue(MASK_VAVORITE);
	}

	public void setFavorite(boolean favorite)
	{
		this.setMaskValue(MASK_VAVORITE, favorite);
	}

	public boolean isFound()
	{
		return this.getMaskValue(MASK_FOUND);
	}

	public void setFound(boolean found)
	{
		this.setMaskValue(MASK_FOUND, found);
	}

}