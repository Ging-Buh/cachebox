package de.droidcachebox.dataclasses;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.Locator;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;

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
    private byte[] waypointCode;
    /**
     * Titel des Wegpunktes
     */
    private byte[] title;

    public Waypoint(boolean withDetails) {
        geoCacheId = -1;
        setWaypointCode("");
        coordinate = new Coordinate(0, 0);
        setDescription("");
        isStartWaypoint = false;
        if (withDetails) {
            detail = new WaypointDetail();
        }
    }

    /**
     * this waypoint is created as a user created waypoint (isUserWaypoint),
     * what will change the color of the cache, if this waypoint is of GeoCacheType Final
     */
    public Waypoint(String gcCode, GeoCacheType waypointType, String description, double latitude, double longitude, long geoCacheId, String clue, String title) {
        setWaypointCode(gcCode);
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

    public float recalculateAndGetDistance() {
        Coordinate fromPos = Locator.getInstance().getMyPosition();
        float[] dist = new float[4];
        MathUtils.computeDistanceAndBearing(CalculationType.FAST, fromPos.getLatitude(), fromPos.getLongitude(), coordinate.getLatitude(), coordinate.getLongitude(), dist);
        return dist[0];
    }

    public void clear() {
        geoCacheId = -1;
        setWaypointCode("");
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
        return "WP:" + getWaypointCode() + " " + coordinate.toString();
    }

    public String getWaypointCode() {
        if (waypointCode == null)
            return EMPTY_STRING;
        return new String(waypointCode, US_ASCII);
    }

    public void setWaypointCode(String waypointCode) {
        if (waypointCode == null) {
            this.waypointCode = null;
            return;
        }
        this.waypointCode = waypointCode.getBytes(US_ASCII);
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
            if (wp.waypointCode == null)
                return false;
            if (this.waypointCode == null)
                return false;
            return Arrays.equals(wp.waypointCode, this.waypointCode);
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