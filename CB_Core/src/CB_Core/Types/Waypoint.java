package CB_Core.Types;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Date;

import CB_Core.Enums.CacheTypes;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;

public class Waypoint implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 67610567646416L;
	private static final Charset US_ASCII = Charset.forName("US-ASCII");
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	private static final String EMPTY_STRING = "";

	// / Id des dazugehörigen Caches in der Datenbank von geocaching.com
	public long CacheId;

	// / Waypoint Code
	private byte[] GcCode;

	public Coordinate Pos;

	// / Titel des Wegpunktes
	private byte[] Title;

	// / Kommentartext
	private byte[] Description;

	// / Art des Wegpunkts
	public CacheTypes Type;

	// / true, falls der Wegpunkt vom Benutzer erstellt wurde
	public boolean IsUserWaypoint;

	// / true, falls der Wegpunkt von der Synchronisation ausgeschlossen wird
	public boolean IsSyncExcluded;

	// / Lösung einer QTA
	private byte[] Clue;

	// True wenn dies der Startpunkt für den nächsten Besuch ist.
	// Das CacheIcon wird dann auf diesen Waypoint verschoben und dieser Waypoint wird standardmäßig aktiviert
	// Es muss aber sichergestellt sein dass immer nur 1 Waypoint eines Caches ein Startpunkt ist!
	public boolean IsStart = false;

	public Waypoint()
	{
		CacheId = -1;
		setGcCode("");
		Pos = new Coordinate();
		setDescription("");
		IsStart = false;
	}

	public int checkSum = 0; // for replication

	public Date time;

	public Waypoint(String gcCode, CacheTypes type, String description, double latitude, double longitude, long cacheId, String clue,
			String title)
	{
		setGcCode(gcCode);
		CacheId = cacheId;
		Pos = new Coordinate(latitude, longitude);
		setDescription(description);
		Type = type;
		IsSyncExcluded = true;
		IsUserWaypoint = true;
		setClue(clue);
		setTitle(title);
		IsStart = false;
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
		// Log.d(TAG, "Waypoint type: " + this.mWaypointType.toString());
	}

	public void clear()
	{
		CacheId = -1;
		setGcCode("");
		Pos = new Coordinate();
		setTitle("");
		setDescription("");
		Type = null;
		IsUserWaypoint = false;
		IsSyncExcluded = false;
		setClue("");
		checkSum = 0;
		time = null;
	}

	public Waypoint copy()
	{
		return new Waypoint(getGcCode(), Type, getDescription(), Pos.getLatitude(), Pos.getLongitude(), CacheId, getClue(), getTitle());
	}

	@Override
	public String toString()
	{
		return "WP:" + getGcCode() + " " + Pos.toString();
	}

	public void dispose()
	{
		setGcCode(null);
		Pos = null;
		setTitle(null);
		setDescription(null);
		Type = null;
		setClue(null);
		time = null;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null) return false;
		if (o instanceof Waypoint)
		{
			Waypoint w = (Waypoint) o;

			if (!this.getGcCode().equals(w.getGcCode())) return false;
			if (!this.Pos.equals(w.Pos)) return false;
			if (!this.getTitle().equals(w.getTitle())) return false;
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

	public String getDescription()
	{
		if (Description == null) return EMPTY_STRING;
		return new String(Description, UTF_8);
	}

	public void setDescription(String description)
	{
		if (description == null)
		{
			Description = null;
			return;
		}
		Description = description.getBytes(UTF_8);
	}

	public String getClue()
	{
		if (Clue == null) return EMPTY_STRING;
		return new String(Clue, UTF_8);
	}

	public void setClue(String clue)
	{
		if (clue == null)
		{
			Clue = null;
			return;
		}
		Clue = clue.getBytes(UTF_8);
	}

}
