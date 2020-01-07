package de.droidcachebox.database;

import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Waypoint implements Serializable {
    public static final Charset US_ASCII = StandardCharsets.US_ASCII;
    public static final Charset UTF_8 = StandardCharsets.UTF_8;
    public static final String EMPTY_STRING = "";
    private static final long serialVersionUID = 67610567646416L;
    /**
     * Id des dazugehörigen Caches in der Datenbank von geocaching.com
     */
    public long geoCacheId;
    /**
     * Art des Wegpunkts
     */
    public GeoCacheType waypointType;
    /**
     * true, falls der Wegpunkt vom Benutzer erstellt wurde
     */
    public boolean isUserWaypoint;
    /**
     * true, falls der Wegpunkt von der Synchronisation ausgeschlossen wird
     */
    public boolean isSyncExcluded;
    /**
     * True wenn dies der Startpunkt für den nächsten Besuch ist.<br>
     * Das CacheIcon wird dann auf diesen Waypoint verschoben und dieser Waypoint wird standardmäßig aktiviert<br>
     * Es muss aber sichergestellt sein dass immer nur 1 Waypoint eines Caches ein Startpunkt ist!<br>
     */
    public boolean isStartWaypoint;
    // Detail Information of Waypoint which are not always loaded
    public WaypointDetail detail = null;
    private Coordinate coordinate;
    /**
     * Waypoint Code
     */
    private byte[] gcCode;
    /**
     * Titel des Wegpunktes
     */
    private byte[] title;

    public Waypoint(boolean withDetails) {
        geoCacheId = -1;
        setGcCode("");
        coordinate = new Coordinate(0, 0);
        setDescription("");
        isStartWaypoint = false;
        if (withDetails) {
            detail = new WaypointDetail();
        }
    }

    public Waypoint(String gcCode, GeoCacheType waypointType, String description, double latitude, double longitude, long geoCacheId, String clue, String title) {
        setGcCode(gcCode);
        this.geoCacheId = geoCacheId;
        coordinate = new Coordinate(latitude, longitude);
        setDescription(description);
        this.waypointType = waypointType;
        isSyncExcluded = true;
        isUserWaypoint = true;
        setClue(clue);
        setTitle(title);
        isStartWaypoint = false;
        detail = new WaypointDetail();
    }

    // / <summary>
    // / Entfernung von der letzten gültigen Position
    // / </summary>
    public float getDistance() {
        Coordinate fromPos = Locator.getInstance().getLocation().toCordinate();
        float[] dist = new float[4];

        MathUtils.calculateDistanceAndBearing(CalculationType.FAST, fromPos.getLatitude(), fromPos.getLongitude(), coordinate.getLatitude(), coordinate.getLongitude(), dist);
        return dist[0];
    }

    /**
     * @param strText ?
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
            this.waypointType = GeoCacheType.Cache;
        } else {
            String strCacheType;
            if (arrSplitted.length > 1)
                strCacheType = arrSplitted[1];
            else
                strCacheType = arrSplitted[0];

            String[] strFirstWord = strCacheType.split(" ");

            for (String word : strFirstWord) {
                this.waypointType = GeoCacheType.parseString(word);
                if (this.waypointType != GeoCacheType.Undefined)
                    break;
            }

        }
    }

    public void clear() {
        geoCacheId = -1;
        setGcCode("");
        coordinate = new Coordinate(0, 0);
        setTitle("");
        setDescription("");
        waypointType = null;
        isUserWaypoint = false;
        isSyncExcluded = false;
        setClue("");
        setCheckSum(0);
    }

    @Override
    public String toString() {
        return "WP:" + getGcCode() + " " + coordinate.toString();
    }

    public void dispose() {
        setGcCode(null);
        coordinate = null;
        setTitle(null);
        setDescription(null);
        waypointType = null;
        setClue(null);
    }

    public String getGcCode() {
        if (gcCode == null)
            return EMPTY_STRING;
        return new String(gcCode, US_ASCII);
    }

    public void setGcCode(String gcCode) {
        if (gcCode == null) {
            this.gcCode = null;
            return;
        }
        this.gcCode = gcCode.getBytes(US_ASCII);
    }

    public String getTitle() {
        if (title == null)
            return EMPTY_STRING;
        return new String(title, UTF_8);
    }

    public void setTitle(String title) {
        if (title == null) {
            this.title = null;
            return;
        }
        this.title = title.getBytes(UTF_8);
    }

    public String getTitleForGui() {
        if (isCorrectedFinal())
            return Translation.get("coordinatesAreCorrected");
        else
            return getTitle();
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

    public int getCheckSum() {
        if (detail == null) {
            return 0;
        } else {
            return detail.checkSum;
        }
    }

    public void setCheckSum(int i) {
        if (detail != null) {
            detail.setCheckSum(i);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof Waypoint) {

            Waypoint wp = (Waypoint) obj;
            if (wp.gcCode == null)
                return false;
            if (this.gcCode == null)
                return false;
            return Arrays.equals(wp.gcCode, this.gcCode);
        }
        return false;
    }

    public boolean isCorrectedFinal() {
        // return new String(Title, (UTF_8)).equals("Final GSAK Corrected");
        return this.waypointType == GeoCacheType.Final && this.isUserWaypoint && this.coordinate.isValid();
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate value) {
        coordinate = value;
    }

    public double getLatitude() {
        if (coordinate == null) coordinate = new Coordinate(0, 0);
        return coordinate.getLatitude();
    }

    public double getLongitude() {
        if (coordinate == null) coordinate = new Coordinate(0, 0);
        return coordinate.getLongitude();
    }
}