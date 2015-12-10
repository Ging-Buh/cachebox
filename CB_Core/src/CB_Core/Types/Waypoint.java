package CB_Core.Types;

import java.io.Serializable;
import java.nio.charset.Charset;

import CB_Core.CacheTypes;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;

public class Waypoint implements Serializable {
    private static final long serialVersionUID = 67610567646416L;
    public static final Charset US_ASCII = Charset.forName("US-ASCII");
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final String EMPTY_STRING = "";

    /** Id des dazugehörigen Caches in der Datenbank von geocaching.com */
    public long CacheId;

    /** Waypoint Code */
    private byte[] GcCode;

    public Coordinate Pos;

    /** Titel des Wegpunktes */
    private byte[] Title;

    /** Art des Wegpunkts */
    public CacheTypes Type;

    /** true, falls der Wegpunkt vom Benutzer erstellt wurde */
    public boolean IsUserWaypoint;

    /** true, falls der Wegpunkt von der Synchronisation ausgeschlossen wird */
    public boolean IsSyncExcluded;

    /**
     * True wenn dies der Startpunkt für den nächsten Besuch ist.<br>
     * Das CacheIcon wird dann auf diesen Waypoint verschoben und dieser Waypoint wird standardmäßig aktiviert<br>
     * Es muss aber sichergestellt sein dass immer nur 1 Waypoint eines Caches ein Startpunkt ist!<br>
     */
    public boolean IsStart = false;

    // Detail Information of Waypoint which are not always loaded
    public WaypointDetail detail = null;

    public Waypoint(boolean withDetails) {
	CacheId = -1;
	setGcCode("");
	Pos = new Coordinate(0, 0);
	setDescription("");
	IsStart = false;
	if (withDetails) {
	    detail = new WaypointDetail();
	}
    }

    public Waypoint(String gcCode, CacheTypes type, String description, double latitude, double longitude, long cacheId, String clue, String title) {
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
	detail = new WaypointDetail();
    }

    // / <summary>
    // / Entfernung von der letzten gültigen Position
    // / </summary>
    public float Distance() {
	Coordinate fromPos = Locator.getLocation().toCordinate();
	float[] dist = new float[4];

	MathUtils.computeDistanceAndBearing(CalculationType.FAST, fromPos.getLatitude(), fromPos.getLongitude(), Pos.getLatitude(), Pos.getLongitude(), dist);
	return dist[0];
    }

    public void setCoordinate(Coordinate result) {
	Pos = result;
    }

    /**
     * @param strText
     */
    public void parseTypeString(String strText) {
	// Log.d(TAG, "Parsing type string: " + strText);

	/*
	 * Geocaching.com cache types are in the form Geocache|Multi-cache Waypoint|Question to Answer Waypoint|Stages of a Multicache Other
	 * pages / bcaching.com results do not contain the | separator, so make sure that the parsing functionality does work with both
	 * variants
	 */

	String[] arrSplitted = strText.split("\\|");
	if (arrSplitted[0].toLowerCase().equals("geocache")) {
	    this.Type = CacheTypes.Cache;
	} else {
	    String strCacheType;
	    if (arrSplitted.length > 1)
		strCacheType = arrSplitted[1];
	    else
		strCacheType = arrSplitted[0];

	    String[] strFirstWord = strCacheType.split(" ");

	    for (String word : strFirstWord) {
		this.Type = CacheTypes.parseString(word);
		if (this.Type != CacheTypes.Undefined)
		    break;
	    }

	}
	// Log.d(TAG, "Waypoint type: " + this.mWaypointType.toString());
    }

    public void clear() {
	CacheId = -1;
	setGcCode("");
	Pos = new Coordinate(0, 0);
	setTitle("");
	setDescription("");
	Type = null;
	IsUserWaypoint = false;
	IsSyncExcluded = false;
	setClue("");
	setCheckSum(0);
    }

    @Override
    public String toString() {
	return "WP:" + getGcCode() + " " + Pos.toString();
    }

    public void dispose() {
	setGcCode(null);
	Pos = null;
	setTitle(null);
	setDescription(null);
	Type = null;
	setClue(null);
    }

    public String getGcCode() {
	if (GcCode == null)
	    return EMPTY_STRING;
	return new String(GcCode, US_ASCII);
    }

    public void setGcCode(String gcCode) {
	if (gcCode == null) {
	    GcCode = null;
	    return;
	}
	GcCode = gcCode.getBytes(US_ASCII);
    }

    public String getTitle() {
	if (Title == null)
	    return EMPTY_STRING;
	return new String(Title, UTF_8);
    }

    public void setTitle(String title) {
	if (title == null) {
	    Title = null;
	    return;
	}
	Title = title.getBytes(UTF_8);
    }

    public String getDescription() {
	if (detail == null) {
	    return EMPTY_STRING;
	} else {
	    return detail.getDescription();
	}
    }

    public void setDescription(String description) {
	if (detail != null) {
	    detail.setDescription(description);
	}
    }

    public String getClue() {
	if (detail == null) {
	    return EMPTY_STRING;
	} else {
	    return detail.getClue();
	}
    }

    public void setClue(String clue) {
	if (detail != null) {
	    detail.setClue(clue);
	}
    }

    public void setCheckSum(int i) {
	if (detail != null) {
	    detail.setCheckSum(i);
	}
    }

    public int getCheckSum() {
	if (detail == null) {
	    return 0;
	} else {
	    return detail.checkSum;
	}
    }

}