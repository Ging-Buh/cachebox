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
package de.droidcachebox.database;

import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.DLong;
import de.droidcachebox.utils.MathUtils;
import de.droidcachebox.utils.MathUtils.CalculationType;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class Cache implements Comparable<Cache>, Serializable {
    public final static byte IS_LITE = 1;
    public final static byte IS_FULL = 2;
    // ########################################################
    // Boolean Handling
    // one Boolean use up to 4 Bytes
    // Boolean data type represents one bit of information, but its "size" isn't something that's precisely defined. (Oracle Docs)
    //
    // so we use one Short for Store all Boolean and Use a BitMask
    // ########################################################
    final static byte NOT_LIVE = 0;
    private static final String EMPTY_STRING = "";
    private static final Charset US_ASCII = StandardCharsets.US_ASCII;
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final long serialVersionUID = 1015307624242318838L;
    // Masks
    // protected final static short MASK_HAS_HINT = 1 << 0; // not necessary because hasHint is always called for SelectedCache and
    // SelectedCache will have valid hint field.
    private final static short MASK_CORECTED_COORDS = 1 << 1;
    private final static short MASK_ARCHIVED = 1 << 2;
    private final static short MASK_AVAILABLE = 1 << 3;
    private final static short MASK_FAVORITE = 1 << 4;
    private final static short MASK_FOUND = 1 << 5;
    private final static short MASK_IS_LIVE = 1 << 6;
    // private final static short MASK_SOLVER1CHANGED = 1 << 7;
    private final static short MASK_HAS_USER_DATA = 1 << 8;
    private final static short MASK_LISTING_CHANGED = 1 << 9;
    private static String gcLogin = null;
    public long generatedId;
    public float gcVoteRating;
    public int favPoints = 0;
    /**
     * size of geoCache
     * geoCacheType = Wikipedia: this is the radius in meters
     */
    public GeoCacheSize geoCacheSize;
    public int numTravelbugs;
    /**
     * use this, if no need of recalculation
     */
    public float cachedDistance = 0;
    private CB_List<Waypoint> wayPoints;
    private CacheDetail geoCacheDetail = null;
    private Coordinate coordinate = new Coordinate(0, 0);
    private GeoCacheType geoCacheType = GeoCacheType.Undefined;
    private byte[] geoCacheCode;
    private byte[] geoCacheName;
    private byte[] geoCacheId;
    /**
     * Stored Difficulty and Terrain<br>
     * <br>
     * First four bits for Difficulty<br>
     * Last four bits for Terrain
     */
    private byte difficultyTerrain = 0;
    private byte[] owner;
    /**
     * When Solver1 changes -> this flag must be set. When Solver 2 will be opend and this flag is set -> Solver 2 must reload the content
     * from DB to get the changes from Solver 1
     */
    private boolean solver1Changed = false;
    private short bitFlags = 0;
    /**
     * Bin ich der Owner? </br>
     * -1 noch nicht getestet </br>
     * 1 ja </br>
     * 0 nein
     */
    private int isMyCache = -1;
    private boolean isDisposed = false;

    public Cache(boolean withDetails) {
        numTravelbugs = 0;
        setDifficulty(0);
        setTerrain(0);
        geoCacheSize = GeoCacheSize.other;
        setAvailable(true);
        wayPoints = new CB_List<>();
        if (withDetails) {
            geoCacheDetail = new CacheDetail();
        }
    }

    public Cache(double Latitude, double Longitude, String geoCacheName, GeoCacheType cacheType, String geoCacheCode) {
        coordinate = new Coordinate(Latitude, Longitude);
        setGeoCacheName(geoCacheName);
        geoCacheType = cacheType;
        setGeoCacheCode(geoCacheCode);
        numTravelbugs = 0;
        setDifficulty(0);
        setTerrain(0);
        geoCacheSize = GeoCacheSize.other;
        setAvailable(true);
        wayPoints = new CB_List<>();
    }

    public static long generateCacheId(String GcCode) {
        long result = 0;
        char[] dummy = GcCode.toCharArray();
        byte[] byteDummy = new byte[8];
        for (int i = 0; i < 8; i++) {
            if (i < GcCode.length())
                byteDummy[i] = (byte) dummy[i];
            else
                byteDummy[i] = 0;
        }
        for (int i = 7; i >= 0; i--) {
            result *= 256;
            result += byteDummy[i];
        }
        return result;
    }

    public GeoCacheType getGeoCacheType() {
        return geoCacheType;
    }

    public void setGeoCacheType(GeoCacheType cacheType) {
        geoCacheType = cacheType;
    }

    /**
     * Breitengrad
     */
    public double getLatitude() {
        if (coordinate == null) coordinate = new Coordinate(0, 0);
        return coordinate.getLatitude();
    }

    /**
     * Längengrad
     */
    public double getLongitude() {
        if (coordinate == null) coordinate = new Coordinate(0, 0);
        return coordinate.getLongitude();
    }

    /**
     * Delete Detail Information to save memory
     */
    public void deleteDetail(boolean showAllWaypoints) {
        if (geoCacheDetail == null)
            return;
        geoCacheDetail.dispose();
        geoCacheDetail = null;
        // remove all Detail Information from Waypoints
        // remove all Waypoints != Start and Final
        if ((wayPoints != null) && (!showAllWaypoints)) {
            for (int i = 0; i < wayPoints.size(); i++) {
                Waypoint wp = wayPoints.get(i);
                if (wp.isStartWaypoint || wp.waypointType == GeoCacheType.Final) {
                    if (wp.detail != null)
                        wp.detail.dispose();
                    wp.detail = null;
                } else {
                    if (wp.detail != null) {
                        wp.detail.dispose();
                        wp.detail = null;
                    }
                    wayPoints.remove(i);
                    i--;
                }
            }
        }
    }

    public boolean mustLoadDetail() {
        return (geoCacheDetail == null);
    }

    /**
     * Load Detail Information from DB
     */
    public void loadDetail() {
        CacheDAO dao = new CacheDAO();
        dao.readDetail(this);
        // load all Waypoints with full Details
        WaypointDAO wdao = new WaypointDAO();
        CB_List<Waypoint> readWaypoints = wdao.getWaypointsFromCacheID(generatedId, true);
        if (wayPoints == null) wayPoints = new CB_List<>();
        for (int i = 0; i < readWaypoints.size(); i++) {
            Waypoint readWaypoint = readWaypoints.get(i);
            boolean found = false;
            for (int j = 0; j < wayPoints.size(); j++) {
                Waypoint existingWaypoint = wayPoints.get(j);
                if (readWaypoint.getGcCode().equals(existingWaypoint.getGcCode())) {
                    found = true;
                    existingWaypoint.detail = readWaypoint.detail; // copy Detail Info
                    break;
                }
            }
            if (!found) {
                wayPoints.add(readWaypoint);
            }
        }
    }

    public boolean iAmTheOwner() {
        if (isMyCache == 0)
            return false;
        if (isMyCache == 1)
            return true;
        boolean ret = false;
        try {
            if (gcLogin == null) {
                gcLogin = CB_Core_Settings.GcLogin.getValue().toLowerCase(Locale.getDefault());
            }
            ret = getOwner().toLowerCase(Locale.getDefault()).equals(gcLogin);
        } catch (Exception ignored) {
        }
        isMyCache = ret ? 1 : 0;
        return ret;
    }

    /**
     * -- korrigierte Koordinaten (kommt nur aus GSAK? bzw CacheWolf-Import) -- oder Mystery mit gueltigem Final
     */
    public boolean hasCorrectedCoordinatesOrHasCorrectedFinal() {
        return hasCorrectedCoordinates() || getCorrectedFinal() != null;
    }

    // also checks flag userwaypoint
    public Waypoint getCorrectedFinal() {
        if (wayPoints == null || wayPoints.size() == 0)
            return null;

        for (int i = 0, n = wayPoints.size(); i < n; i++) {
            Waypoint waypoint = wayPoints.get(i);
            if (waypoint.waypointType == GeoCacheType.Final) {
                if (!waypoint.getCoordinate().isValid() || waypoint.getCoordinate().isZero())
                    continue;
                if (waypoint.isUserWaypoint) return waypoint;
            }
        }
        return null;
    }

    /**
     * search the start Waypoint for a multi or mystery
     */
    public Waypoint getStartWaypoint() {
        if ((geoCacheType != GeoCacheType.Multi) && (geoCacheType != GeoCacheType.Mystery))
            return null;

        if (wayPoints == null || wayPoints.size() == 0)
            return null;

        for (int i = 0, n = wayPoints.size(); i < n; i++) {
            Waypoint wp = wayPoints.get(i);
            if ((wp.waypointType == GeoCacheType.MultiStage) && (wp.isStartWaypoint)) {
                return wp;
            }
        }
        return null;
    }

    /**
     * Returns a List of Spoiler Ressources
     *
     * @return ArrayList of String
     */
    public CB_List<ImageEntry> getSpoilerRessources() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.getSpoilerRessources(this);
        } else {
            return null;
        }
    }

    /**
     * Returns true has the Cache Spoilers else returns false
     *
     * @return Boolean
     */
    public boolean hasSpoiler() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.hasSpoiler(this);
        } else {
            return false;
        }
    }

    public void loadSpoilerRessources() {
        if (geoCacheDetail != null) {
            geoCacheDetail.loadSpoilerRessources(this);
        }
    }

    public float recalculateAndGetDistance(CalculationType type, boolean useFinal, Coordinate fromPos) {
        if (isDisposed)
            return 0;
        Waypoint waypoint = null;
        if (useFinal)
            waypoint = getCorrectedFinal();
        // Wenn ein Mystery-Cache einen Final-Waypoint hat, soll die
        // Diszanzberechnung vom Final aus gemacht werden
        // If a mystery has a final waypoint, the distance will be calculated to
        // the final not the the cache coordinates
        Coordinate toPos = coordinate;
        if (waypoint != null) {
            toPos = new Coordinate(waypoint.getLatitude(), waypoint.getLongitude());
            // nur sinnvolles Final, sonst vom Cache
            if (waypoint.getLatitude() == 0 && waypoint.getLongitude() == 0)
                toPos = coordinate;
        }
        float[] dist = new float[4];
        MathUtils.calculateDistanceAndBearing(type, fromPos.getLatitude(), fromPos.getLongitude(), toPos.getLatitude(), toPos.getLongitude(), dist);
        cachedDistance = dist[0];
        return cachedDistance;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Cache))
            return false;
        Cache other = (Cache) obj;
        return Arrays.equals(geoCacheCode, other.geoCacheCode);
    }

    @Override
    public int compareTo(Cache c2) {
        float dist1 = cachedDistance;
        float dist2 = c2.cachedDistance;
        return (Float.compare(dist1, dist2));
    }

    private Waypoint findWayPointByGc(String gc) {
        if (isDisposed)
            return null;
        for (Waypoint wayPoint : wayPoints) {
            if (wayPoint.getGcCode().equals(gc)) {
                return wayPoint;
            }
        }
        return null;
    }

    // copy all Informations from cache into this
    // this is used after actualization of cache with API
    public void copyFrom(Cache cache) {
        // MapX = cache.MapX;
        // MapY = cache.MapY;
        geoCacheName = cache.geoCacheName;
        coordinate = cache.coordinate;
        gcVoteRating = cache.gcVoteRating;
        favPoints = cache.favPoints;
        geoCacheSize = cache.geoCacheSize;
        setDifficulty(cache.getDifficulty());
        setTerrain(cache.getTerrain());
        setArchived(cache.isArchived());
        setAvailable(cache.isAvailable());
        // favorite = false;
        // noteCheckSum = 0;
        // solverCheckSum = 0;
        // hasUserData = false;
        // CorrectedCoordinates = false;
        // only change the found status when it is true in the loaded cache
        // This will prevent ACB from overriding a found cache which is still not found in GC
        if (cache.isFound())
            setFound(cache.isFound());
        // TourName = "";
        // GPXFilename_ID = 0;
        geoCacheType = cache.geoCacheType;
        // PlacedBy = cache.PlacedBy;
        owner = cache.owner;
        // listingChanged = true; // so that spoiler download will be done again
        numTravelbugs = cache.numTravelbugs;
        // cachedDistance = 0;
        // do not copy waypoints List directly because actual user defined Waypoints would be deleted

        for (Waypoint newWaypoint : cache.wayPoints) {
            Waypoint wayPoint = findWayPointByGc(newWaypoint.getGcCode());
            if (wayPoint == null) {
                // this waypoint is new -> Add to list
                wayPoints.add(newWaypoint);
            } else {
                // this waypoint is already in our list -> Copy Informations
                wayPoint.setDescription(newWaypoint.getDescription());
                wayPoint.setCoordinate(newWaypoint.getCoordinate());
                wayPoint.setTitle(newWaypoint.getTitle());
                wayPoint.waypointType = newWaypoint.waypointType;
            }
        }
        // spoilerRessources = null;
        // copy Detail Information
        geoCacheDetail = cache.geoCacheDetail;
        isMyCache = cache.isMyCache;
        // gcLogin = null;

    }

    @Override
    public String toString() {
        return "Cache:" + getGeoCacheCode();
    }

    public void dispose() {
        isDisposed = true;

        if (geoCacheDetail != null)
            geoCacheDetail.dispose();
        geoCacheDetail = null;

        geoCacheCode = null;
        geoCacheName = null;
        coordinate = null;
        geoCacheSize = null;
        geoCacheType = null;
        owner = null;

        if (wayPoints != null) {
            for (int i = 0, n = wayPoints.size(); i < n; i++) {
                Waypoint entry = wayPoints.get(i);
                entry.dispose();
            }

            wayPoints.clear();
            wayPoints = null;
        }
        owner = null;

    }

    public boolean getSolver1Changed() {
        return solver1Changed;
    }

    public void setSolver1Changed(boolean b) {
        solver1Changed = b;
    }

    public String getGeoCacheCode() {
        if (geoCacheCode == null)
            return EMPTY_STRING;
        return new String(geoCacheCode, US_ASCII);
    }

    public void setGeoCacheCode(String geoCacheCode) {
        if (geoCacheCode == null) {
            this.geoCacheCode = null;
            return;
        }
        this.geoCacheCode = geoCacheCode.getBytes(US_ASCII);
    }

    public String getGeoCacheName() {
        if (geoCacheName == null)
            return EMPTY_STRING;
        return new String(geoCacheName, UTF_8);
    }

    public void setGeoCacheName(String geoCacheName) {
        if (geoCacheName == null) {
            this.geoCacheName = null;
            return;
        }
        this.geoCacheName = geoCacheName.getBytes(UTF_8);
    }

    public String getOwner() {
        if (owner == null)
            return EMPTY_STRING;
        return new String(owner, UTF_8);
    }

    public void setOwner(String owner) {
        if (owner == null) {
            this.owner = null;
            return;
        }
        this.owner = owner.getBytes(UTF_8);
    }

    public String getGeoCacheId() {
        if (geoCacheId == null)
            return EMPTY_STRING;
        return new String(geoCacheId, UTF_8);
    }

    public void setGeoCacheId(String geoCacheId) {
        if (geoCacheId == null) {
            this.geoCacheId = null;
            return;
        }
        this.geoCacheId = geoCacheId.trim().getBytes(UTF_8);
    }

    public String getHint() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.getHint();
        } else {
            return EMPTY_STRING;
        }
    }

    public void setHint(String hint) {
        if (geoCacheDetail != null) {
            geoCacheDetail.setHint(hint);
        }
    }

    public long getGPXFilename_ID() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.GPXFilename_ID;
        }
        return 0;
    }

    public void setGPXFilename_ID(long gpxFilenameId) {
        if (geoCacheDetail != null) {
            geoCacheDetail.GPXFilename_ID = gpxFilenameId;
        }

    }

    // Getter and Setter over Mask

    public boolean hasHint() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.getHint().length() > 0;
        } else {
            return false;
        }
    }

    private boolean getMaskValue(short mask) {
        return (bitFlags & mask) == mask;
    }

    private void setMaskValue(short mask, boolean value) {
        if (getMaskValue(mask) == value)
            return;

        if (value) {
            bitFlags |= mask;
        } else {
            bitFlags &= ~mask;
        }

    }

    public boolean hasCorrectedCoordinates() {
        return getMaskValue(MASK_CORECTED_COORDS);
    }

    public void setHasCorrectedCoordinates(boolean correctedCoordinates) {
        setMaskValue(MASK_CORECTED_COORDS, correctedCoordinates);
    }

    public boolean isArchived() {
        return getMaskValue(MASK_ARCHIVED);
    }

    public void setArchived(boolean archived) {
        setMaskValue(MASK_ARCHIVED, archived);
    }

    public boolean isAvailable() {
        return getMaskValue(MASK_AVAILABLE);
    }

    public void setAvailable(boolean available) {
        setMaskValue(MASK_AVAILABLE, available);
    }

    public boolean isFavorite() {
        return getMaskValue(MASK_FAVORITE);
    }

    public void setFavorite(boolean favorite) {
        setMaskValue(MASK_FAVORITE, favorite);
    }

    public float getDifficulty() {
        return getFloatX_5FromByte((byte) (difficultyTerrain & 15));
    }

    public void setDifficulty(float difficulty) {
        difficultyTerrain = (byte) (difficultyTerrain & (byte) 240);// clear Bits
        difficultyTerrain = (byte) (difficultyTerrain | getDT_HalfByte(difficulty));
    }

    public float getTerrain() {
        return getFloatX_5FromByte((byte) (difficultyTerrain >>> 4));
    }

    public void setTerrain(float terrain) {
        difficultyTerrain = (byte) (difficultyTerrain & (byte) 15);// clear Bits
        difficultyTerrain = (byte) (difficultyTerrain | getDT_HalfByte(terrain) << 4);
    }

    private byte getDT_HalfByte(float value) {
        if (value == 1f)
            return (byte) 0;
        if (value == 1.5f)
            return (byte) 1;
        if (value == 2f)
            return (byte) 2;
        if (value == 2.5f)
            return (byte) 3;
        if (value == 3f)
            return (byte) 4;
        if (value == 3.5f)
            return (byte) 5;
        if (value == 4f)
            return (byte) 6;
        if (value == 4.5f)
            return (byte) 7;
        return (byte) 8;
    }

    private float getFloatX_5FromByte(byte value) {
        switch (value) {
            case 0:
                return 1f;
            case 1:
                return 1.5f;
            case 2:
                return 2f;
            case 3:
                return 2.5f;
            case 4:
                return 3f;
            case 5:
                return 3.5f;
            case 6:
                return 4f;
            case 7:
                return 4.5f;
        }
        return 5f;
    }

    public boolean isFound() {
        return getMaskValue(MASK_FOUND);
    }

    public void setFound(boolean found) {
        setMaskValue(MASK_FOUND, found);
    }

    public boolean isLive() {
        return getMaskValue(MASK_IS_LIVE);
    }

    public void setLive(boolean isLive) {
        setMaskValue(MASK_IS_LIVE, isLive);
    }

    public boolean isHasUserData() {
        return getMaskValue(MASK_HAS_USER_DATA);
    }

    public void setHasUserData(boolean hasUserData) {
        setMaskValue(MASK_HAS_USER_DATA, hasUserData);
    }

    public boolean isListingChanged() {
        return getMaskValue(MASK_LISTING_CHANGED);
    }

    void setListingChanged(boolean listingChanged) {
        setMaskValue(MASK_LISTING_CHANGED, listingChanged);
    }

    public String getPlacedBy() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.PlacedBy;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setPlacedBy(String value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.PlacedBy = value;
        }
    }

    public Date getDateHidden() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.DateHidden;
        } else {
            return null;
        }
    }

    public void setDateHidden(Date date) {
        if (geoCacheDetail != null) {
            geoCacheDetail.DateHidden = date;
        }
    }

    public byte getApiStatus() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.ApiStatus;
        } else {
            return NOT_LIVE;
        }
    }

    public void setApiStatus(byte value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.ApiStatus = value;
        }
    }

    int getNoteChecksum() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.noteCheckSum;
        } else {
            return 0;
        }
    }

    public void setNoteChecksum(int value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.noteCheckSum = value;
        }
    }

    public String getTmpNote() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.tmpNote;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setTmpNote(String value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.tmpNote = value;
        }
    }

    public String getUserNote() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.userNote;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setUserNote(String value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.userNote = value;
        }
    }

    int getSolverChecksum() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.solverCheckSum;
        } else {
            return 0;
        }
    }

    public void setSolverChecksum(int value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.solverCheckSum = value;
        }
    }

    public String getTmpSolver() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.tmpSolver;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setTmpSolver(String value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.tmpSolver = value;
        }
    }

    public String getUrl() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.Url;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setUrl(String value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.Url = value;
        }
    }

    public String getCountry() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.Country;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setCountry(String value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.Country = value;
        }
    }

    public String getState() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.State;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setState(String value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.State = value;
        }
    }

    public ArrayList<Attribute> getAttributes() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.getAttributes(generatedId);
        } else {
            return null;
        }
    }

    public void addAttributeNegative(Attribute attribute) {
        if (geoCacheDetail != null) {
            geoCacheDetail.addAttributeNegative(attribute);
        }
    }

    public void addAttributePositive(Attribute attribute) {
        if (geoCacheDetail != null) {
            geoCacheDetail.addAttributePositive(attribute);
        }
    }

    DLong getAttributesPositive() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.getAttributesPositive(generatedId);
        } else {
            return null;
        }
    }

    public void setAttributesPositive(DLong dLong) {
        if (geoCacheDetail != null) {
            geoCacheDetail.setAttributesPositive(dLong);
        }
    }

    DLong getAttributesNegative() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.getAttributesNegative(generatedId);
        } else {
            return null;
        }
    }

    public void setAttributesNegative(DLong dLong) {
        if (geoCacheDetail != null) {
            geoCacheDetail.setAttributesNegative(dLong);
        }
    }

    public String getLongDescription() {
        if (geoCacheDetail != null) {
            if (geoCacheDetail.getLongDescription() == null || geoCacheDetail.getLongDescription().length() == 0) {
                return Database.getDescription(this);
            }
            return geoCacheDetail.getLongDescription();
        } else {
            return EMPTY_STRING;
        }
    }

    public void setLongDescription(String value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.setLongDescription(value);

        }
    }

    public String getShortDescription() {
        if (geoCacheDetail != null) {
            if (geoCacheDetail.getShortDescription() == null || geoCacheDetail.getShortDescription().length() == 0) {
                return Database.getShortDescription(this);
            }
            return geoCacheDetail.getShortDescription();
        } else {
            return EMPTY_STRING;
        }
    }

    public void setShortDescription(String value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.setShortDescription(value);
        }
    }

    public String getTourName() {
        if (geoCacheDetail != null) {
            return geoCacheDetail.TourName;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setTourName(String value) {
        if (geoCacheDetail != null) {
            geoCacheDetail.TourName = value;
        }
    }

    public boolean isAttributePositiveSet(Attribute attribute) {
        if (geoCacheDetail != null) {
            return geoCacheDetail.isAttributePositiveSet(attribute);
        } else {
            return false;
        }
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    /**
     * Returns true if the Cache a event like Giga, Cito, Event or Mega
     */
    public boolean isEvent() {
        switch (geoCacheType) {
            case Giga:
            case CITO:
            case Event:
            case CelebrationEvent:
            case HQCelebration:
            case HQBlockParty:
            case MegaEvent:
                return true;
            default:
                return false;
        }
    }

    /**
     * Die Coordinate, an der der Cache liegt.
     */
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    /**
     * Detail Information of Waypoint which are not always loaded
     */
    public CacheDetail getGeoCacheDetail() {
        return geoCacheDetail;
    }

    public void setGeoCacheDetail(CacheDetail geoCacheDetail) {
        this.geoCacheDetail = geoCacheDetail;
    }

    /**
     * list of wayPoints for the geoCache
     */
    public CB_List<Waypoint> getWayPoints() {
        return wayPoints;
    }

    public void setWayPoints(CB_List<Waypoint> wayPoints) {
        this.wayPoints = wayPoints;
    }
}