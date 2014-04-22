package CB_Core.Types;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;

import CB_Core.Enums.CacheTypes;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;

public class WaypointLite implements Serializable
{
	private static final long serialVersionUID = 67610567646416L;
	protected static final Charset US_ASCII = Charset.forName("US-ASCII");
	protected static final Charset UTF_8 = Charset.forName("UTF-8");
	protected static final String EMPTY_STRING = "";
	// / Id des dazugehörigen Caches in der Datenbank von geocaching.com
	public long CacheId;

	// / Waypoint Code
	protected byte[] GcCode;

	public Coordinate Pos;

	// / Art des Wegpunkts
	public CacheTypes Type;

	// / Titel des Wegpunktes
	protected byte[] Title;

	// / true, falls der Wegpunkt vom Benutzer erstellt wurde
	public boolean IsUserWaypoint;

	// / true, falls der Wegpunkt von der Synchronisation ausgeschlossen wird
	public boolean IsSyncExcluded;

	// True wenn dies der Startpunkt für den nächsten Besuch ist.
	// Das CacheIcon wird dann auf diesen Waypoint verschoben und dieser Waypoint wird standardmäßig aktiviert
	// Es muss aber sichergestellt sein dass immer nur 1 Waypoint eines Caches ein Startpunkt ist!
	public boolean IsStart = false;

	public WaypointLite()
	{
		CacheId = -1;
		setGcCode("");
		Pos = new Coordinate();
		IsStart = false;
	}

	public WaypointLite(long cacheId, byte[] gcCode, Coordinate pos, CacheTypes type, boolean isUserWaypoint, boolean isSyncExcluded,
			boolean isStart)
	{
		CacheId = cacheId;
		GcCode = gcCode;
		Pos = pos;
		Type = type;
		IsUserWaypoint = isUserWaypoint;
		IsSyncExcluded = isSyncExcluded;
		IsStart = isStart;
	}

	// / <summary>
	// / Entfernung von der letzten gültigen Position
	// / </summary>
	public float Distance()
	{
		Coordinate fromPos = Locator.getLocation().toCordinate();
		float[] dist = new float[4];

		MathUtils.computeDistanceAndBearing(CalculationType.FAST, fromPos.getLatitude(), fromPos.getLongitude(), Pos.getLatitude(),
				Pos.getLongitude(), dist);
		return dist[0];
	}

	public void setLatitude(double parseDouble)
	{
		Pos.setLatitude(parseDouble);
	}

	public void setLongitude(double parseDouble)
	{
		Pos.setLongitude(parseDouble);
	}

	public void setCoordinate(Coordinate result)
	{
		Pos = result;
	}

	/**
	 * @param strText
	 */
	public void parseTypeString(String strText)
	{
		// Log.d(TAG, "Parsing type string: " + strText);

		/*
		 * Geocaching.com cache types are in the form Geocache|Multi-cache Waypoint|Question to Answer Waypoint|Stages of a Multicache Other
		 * pages / bcaching.com results do not contain the | separator, so make sure that the parsing functionality does work with both
		 * variants
		 */

		String[] arrSplitted = strText.split("\\|");
		if (arrSplitted[0].toLowerCase().equals("geocache"))
		{
			this.Type = CacheTypes.Cache;
		}
		else
		{
			String strCacheType;
			if (arrSplitted.length > 1) strCacheType = arrSplitted[1];
			else
				strCacheType = arrSplitted[0];

			String[] strFirstWord = strCacheType.split(" ");
			this.Type = CacheTypes.parseString(strFirstWord[0]);
		}
	}

	public String getTitle()
	{
		if (Title == null) return EMPTY_STRING;
		return new String(Title, UTF_8);
	}

	public void setTitle(String title)
	{
		if (title == null)
		{
			Title = null;
			return;
		}
		Title = title.getBytes(UTF_8);
	}

	public void clear()
	{
		CacheId = -1;
		setGcCode("");
		Pos = new Coordinate();
		Type = null;
		IsUserWaypoint = false;
		IsSyncExcluded = false;
	}

	@Override
	public String toString()
	{
		return "WP-Full:" + getGcCode() + " " + Pos.toString();
	}

	public void dispose()
	{
		setGcCode(null);
		Pos = null;
		Type = null;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null) return false;
		if (o instanceof WaypointLite)
		{
			WaypointLite w = (WaypointLite) o;
			if (!Arrays.equals(GcCode, w.GcCode)) return false;

			if (!this.Pos.equals(w.Pos)) return false;

			return true;
		}
		else if (o instanceof Waypoint)
		{
			Waypoint w = (Waypoint) o;
			if (!Arrays.equals(GcCode, w.GcCode)) return false;

			if (!this.Pos.equals(w.Pos)) return false;

			return true;
		}

		return false;
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

	/**
	 * Returns the Full Waypoint Object from this waypoint!<br>
	 * Missing informations are read from DB
	 * 
	 * @return
	 */
	public Waypoint makeFull()
	{
		if (this instanceof Waypoint)
		{
			return (Waypoint) this;
		}
		return new Waypoint(this);
	}

}
