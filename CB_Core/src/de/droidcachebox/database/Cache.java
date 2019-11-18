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
import de.droidcachebox.locator.Locator;
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
    private static final String EMPTY_STRING = "";
    // ########################################################
    // Boolean Handling
    // one Boolean use up to 4 Bytes
    // Boolean data type represents one bit of information, but its "size" isn't something that's precisely defined. (Oracle Docs)
    //
    // so we use one Short for Store all Boolean and Use a BitMask
    // ########################################################
    final static byte NOT_LIVE = 0;
    public final static byte IS_LITE = 1;
    public final static byte IS_FULL = 2;
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
    /**
     * Detail Information of Waypoint which are not always loaded
     */
    public CacheDetail detail = null;
    /**
     * Id des Caches in der Datenbank von geocaching.com
     */
    public long Id;
    /**
     * Die Coordinate, an der der Cache liegt.
     */
    public Coordinate coordinate = new Coordinate(0, 0);
    /**
     * Durchschnittliche Bewertung des Caches von GcVote
     */
    public float Rating;
    /**
     * Grondspeak favorite points
     */
    public int favPoints = 0;
    /**
     * Groesse des Caches. Bei Wikipediaeintraegen enthaelt dieses Feld den Radius in m
     */
    public CacheSizes Size;

    public void setType(CacheTypes cacheType) {
        this.Type = cacheType;
    }

    public CacheTypes getType() {
        return Type;
    }

    /**
     * Art des Caches
     */
    private CacheTypes Type = CacheTypes.Undefined;
    /**
     * Anzahl der Travelbugs und Coins, die sich in diesem Cache befinden
     */
    public int NumTravelbugs;
    /**
     * Falls keine erneute Distanzberechnung noetig ist nehmen wir diese Distanz
     */
    public float cachedDistance = 0;
    /**
     * Liste der zusaetzlichen Wegpunkte des Caches
     */
    public CB_List<Waypoint> waypoints;
    /**
     * Waypoint Code des Caches
     */
    private byte[] GcCode;
    /**
     * Name des Caches
     */
    private byte[] Name;
    private byte[] GcId;
    /**
     * Bin ich der Owner? </br>
     * -1 noch nicht getestet </br>
     * 1 ja </br>
     * 0 nein
     */
    private int myCache = -1;

    // /**
    // * Das Listing hat sich geaendert!
    // */
    // public boolean listingChanged = false;
    private boolean isDisposed = false;
    /**
     * When Solver1 changes -> this flag must be set. When Solver 2 will be opend and this flag is set -> Solver 2 must reload the content
     * from DB to get the changes from Solver 1
     */
    private boolean solver1Changed = false;
    private short BitFlags = 0;

    /*
     * Constructors
     */
    /**
     * Stored Difficulty and Terrain<br>
     * <br>
     * First four bits for Difficulty<br>
     * Last four bits for Terrain
     */
    private byte DifficultyTerrain = 0;
    /**
     * Verantwortlicher
     */
    private byte[] Owner;

    /**
     * Constructor
     */
    public Cache(boolean withDetails) {
        this.NumTravelbugs = 0;
        this.setDifficulty(0);
        this.setTerrain(0);
        this.Size = CacheSizes.other;
        this.setAvailable(true);
        waypoints = new CB_List<>();
        if (withDetails) {
            detail = new CacheDetail();
        }
    }

    /**
     * Constructor
     */
    public Cache(double Latitude, double Longitude, String Name, CacheTypes cacheType, String GcCode) {
        this.coordinate = new Coordinate(Latitude, Longitude);
        this.setName(Name);
        this.Type = cacheType;
        this.setGcCode(GcCode);
        this.NumTravelbugs = 0;
        this.setDifficulty(0);
        this.setTerrain(0);
        this.Size = CacheSizes.other;
        this.setAvailable(true);
        waypoints = new CB_List<>();

    }

    public static long GenerateCacheId(String GcCode) {
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

    /**
     * Breitengrad
     */
    public double getLatitude() {
        if (coordinate == null) coordinate = new Coordinate(0,0);
        return coordinate.getLatitude();
    }

    /**
     * Längengrad
     */
    public double getLongitude() {
        if (coordinate == null) coordinate = new Coordinate(0,0);
        return coordinate.getLongitude();
    }

    /**
     * Delete Detail Information to save memory
     */
    public void deleteDetail(boolean showAllWaypoints) {
        if (this.detail == null)
            return;
        this.detail.dispose();
        this.detail = null;
        // remove all Detail Information from Waypoints
        // remove all Waypoints != Start and Final
        if ((waypoints != null) && (!showAllWaypoints)) {
            for (int i = 0; i < waypoints.size(); i++) {
                Waypoint wp = waypoints.get(i);
                if (wp.IsStart || wp.Type == CacheTypes.Final) {

                    if (wp.detail != null)
                        wp.detail.dispose();
                    wp.detail = null;
                } else {
                    if (wp.detail != null) {
                        wp.detail.dispose();
                        wp.detail = null;
                    }
                    waypoints.remove(i);
                    i--;
                }
            }
        }
    }

    public boolean mustLoadDetail() {
        return (detail == null);
    }

    /**
     * Load Detail Information from DB
     */
    public void loadDetail() {
        CacheDAO dao = new CacheDAO();
        dao.readDetail(this);
        // load all Waypoints with full Details
        WaypointDAO wdao = new WaypointDAO();
        CB_List<Waypoint> wpts = wdao.getWaypointsFromCacheID(Id, true);
        for (int i = 0; i < wpts.size(); i++) {
            Waypoint wp = wpts.get(i);
            boolean found = false;
            for (int j = 0; j < waypoints.size(); j++) {
                Waypoint wp2 = waypoints.get(j);
                if (wp.getGcCode().equals(wp2.getGcCode())) {
                    found = true;
                    wp2.detail = wp.detail; // copy Detail Info
                    break;
                }
            }
            if (!found) {
                // Waypoint not in List
                // Add Waypoint to List
                waypoints.add(wp);
            }
        }
    }

    public boolean ImTheOwner() {
        String userName = CB_Core_Settings.GcLogin.getValue().toLowerCase(Locale.getDefault());
        if (myCache == 0)
            return false;
        if (myCache == 1)
            return true;

        if (gcLogin == null) {
            gcLogin = userName;
        }

        boolean ret = false;

        try {
            ret = this.getOwner().toLowerCase(Locale.getDefault()).equals(gcLogin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        myCache = ret ? 1 : 0;
        return ret;
    }

    /**
     * -- korrigierte Koordinaten (kommt nur aus GSAK? bzw CacheWolf-Import) -- oder Mystery mit gueltigem Final
     */
    public boolean hasCorrectedCoordiantesOrHasCorrectedFinal() {
        return hasCorrectedCoordinates() || getCorrectedFinal() != null;
    }

    // also checks flag userwaypoint
    public Waypoint getCorrectedFinal() {
        if (waypoints == null || waypoints.size() == 0)
            return null;

        for (int i = 0, n = waypoints.size(); i < n; i++) {
            Waypoint wp = waypoints.get(i);
            if (wp.Type == CacheTypes.Final) {
                if (!wp.Pos.isValid() || wp.Pos.isZero())
                    continue;
                if (wp.IsUserWaypoint) return wp;
            }
        }
        return null;
    }

    /**
     * search the start Waypoint for a multi or mystery
     *
     */
    public Waypoint getStartWaypoint() {
        if ((this.Type != CacheTypes.Multi) && (this.Type != CacheTypes.Mystery))
            return null;

        if (waypoints == null || waypoints.size() == 0)
            return null;

        for (int i = 0, n = waypoints.size(); i < n; i++) {
            Waypoint wp = waypoints.get(i);
            if ((wp.Type == CacheTypes.MultiStage) && (wp.IsStart)) {
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
        if (detail != null) {
            return detail.getSpoilerRessources(this);
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
        if (detail != null) {
            return detail.hasSpoiler(this);
        } else {
            return false;
        }
    }

    public void loadSpoilerRessources() {
        if (detail != null) {
            detail.loadSpoilerRessources(this);
        }
    }

    /**
     * Gibt die Entfernung zur uebergebenen User Position als Float zurueck.
     *
     * @return Entfernung zur uebergebenen User Position als Float
     */
    public float Distance(CalculationType type, boolean useFinal) {
        return Distance(type, useFinal, Locator.getInstance().getMyPosition());
    }

    public float Distance(CalculationType type, boolean useFinal, Coordinate fromPos) {
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
            toPos = new Coordinate(waypoint.Pos.getLatitude(), waypoint.Pos.getLongitude());
            // nur sinnvolles Final, sonst vom Cache
            if (waypoint.Pos.getLatitude() == 0 && waypoint.Pos.getLongitude() == 0)
                toPos = coordinate;
        }
        float[] dist = new float[4];
        MathUtils.computeDistanceAndBearing(type, fromPos.getLatitude(), fromPos.getLongitude(), toPos.getLatitude(), toPos.getLongitude(), dist);
        cachedDistance = dist[0];
        return cachedDistance;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Cache))
            return false;
        Cache other = (Cache) obj;
        return Arrays.equals(this.GcCode, other.GcCode);
    }

    @Override
    public int compareTo(Cache c2) {
        float dist1 = this.cachedDistance;
        float dist2 = c2.cachedDistance;
        return (Float.compare(dist1, dist2));
    }

    private Waypoint findWaypointByGc(String gc) {
        if (isDisposed)
            return null;
        for (int i = 0, n = waypoints.size(); i < n; i++) {
            Waypoint wp = waypoints.get(i);
            if (wp.getGcCode().equals(gc)) {
                return wp;
            }
        }
        return null;
    }

    // copy all Informations from cache into this
    // this is used after actualization of cache with API
    public void copyFrom(Cache cache) {
        // this.MapX = cache.MapX;
        // this.MapY = cache.MapY;
        this.Name = cache.Name;
        this.coordinate = cache.coordinate;
        this.Rating = cache.Rating;
        this.favPoints = cache.favPoints;
        this.Size = cache.Size;
        this.setDifficulty(cache.getDifficulty());
        this.setTerrain(cache.getTerrain());
        this.setArchived(cache.isArchived());
        this.setAvailable(cache.isAvailable());
        // this.favorite = false;
        // this.noteCheckSum = 0;
        // this.solverCheckSum = 0;
        // this.hasUserData = false;
        // this.CorrectedCoordinates = false;
        // only change the found status when it is true in the loaded cache
        // This will prevent ACB from overriding a found cache which is still not found in GC
        if (cache.isFound())
            this.setFound(cache.isFound());
        // this.TourName = "";
        // this.GPXFilename_ID = 0;
        this.Type = cache.Type;
        // this.PlacedBy = cache.PlacedBy;
        this.Owner = cache.Owner;
        // this.listingChanged = true; // so that spoiler download will be done again
        this.NumTravelbugs = cache.NumTravelbugs;
        // this.cachedDistance = 0;
        // do not copy waypoints List directly because actual user defined Waypoints would be deleted
        // this.waypoints = new ArrayList<Waypoint>();

        for (int i = 0, n = cache.waypoints.size(); i < n; i++) {
            Waypoint newWaypoint = cache.waypoints.get(i);

            Waypoint aktWaypoint = this.findWaypointByGc(newWaypoint.getGcCode());
            if (aktWaypoint == null) {
                // this waypoint is new -> Add to list
                this.waypoints.add(newWaypoint);
            } else {
                // this waypoint is already in our list -> Copy Informations
                aktWaypoint.setDescription(newWaypoint.getDescription());
                aktWaypoint.Pos = newWaypoint.Pos;
                aktWaypoint.setTitle(newWaypoint.getTitle());
                aktWaypoint.Type = newWaypoint.Type;
            }
        }
        // this.spoilerRessources = null;
        // copy Detail Information
        this.detail = cache.detail;
        this.myCache = cache.myCache;
        // this.gcLogin = null;

    }

    @Override
    public String toString() {
        return "Cache:" + getGcCode();
    }

    public void dispose() {
        isDisposed = true;

        if (detail != null)
            detail.dispose();
        detail = null;

        GcCode = null;
        Name = null;
        coordinate = null;
        Size = null;
        Type = null;
        Owner = null;

        if (waypoints != null) {
            for (int i = 0, n = waypoints.size(); i < n; i++) {
                Waypoint entry = waypoints.get(i);
                entry.dispose();
            }

            waypoints.clear();
            waypoints = null;
        }
        Owner = null;

    }

    public boolean getSolver1Changed() {
        return solver1Changed;
    }

    public void setSolver1Changed(boolean b) {
        this.solver1Changed = b;
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

    public String getName() {
        if (Name == null)
            return EMPTY_STRING;
        return new String(Name, UTF_8);
    }

    public void setName(String name) {
        if (name == null) {
            Name = null;
            return;
        }
        Name = name.getBytes(UTF_8);
    }

    public String getOwner() {
        if (Owner == null)
            return EMPTY_STRING;
        return new String(Owner, UTF_8);
    }

    public void setOwner(String owner) {
        if (owner == null) {
            Owner = null;
            return;
        }
        Owner = owner.getBytes(UTF_8);
    }

    public String getGcId() {
        if (GcId == null)
            return EMPTY_STRING;
        return new String(GcId, UTF_8);
    }

    public void setGcId(String gcId) {

        if (gcId == null) {
            GcId = null;
            return;
        }
        GcId = gcId.trim().getBytes(UTF_8);
    }

    public String getHint() {
        if (detail != null) {
            return detail.getHint();
        } else {
            return EMPTY_STRING;
        }
    }

    public void setHint(String hint) {
        if (detail != null) {
            detail.setHint(hint);
        }
    }

    public long getGPXFilename_ID() {
        if (detail != null) {
            return detail.GPXFilename_ID;
        }
        return 0;
    }

    public void setGPXFilename_ID(long gpxFilenameId) {
        if (detail != null) {
            detail.GPXFilename_ID = gpxFilenameId;
        }

    }

    // Getter and Setter over Mask

    public boolean hasHint() {
        if (detail != null) {
            return detail.getHint().length() > 0;
        } else {
            return false;
        }
    }

    private boolean getMaskValue(short mask) {
        return (BitFlags & mask) == mask;
    }

    private void setMaskValue(short mask, boolean value) {
        if (getMaskValue(mask) == value)
            return;

        if (value) {
            BitFlags |= mask;
        } else {
            BitFlags &= ~mask;
        }

    }

    public boolean hasCorrectedCoordinates() {
        return this.getMaskValue(MASK_CORECTED_COORDS);
    }

    public void setHasCorrectedCoordinates(boolean correctedCoordinates) {
        this.setMaskValue(MASK_CORECTED_COORDS, correctedCoordinates);
    }

    public boolean isArchived() {
        return this.getMaskValue(MASK_ARCHIVED);
    }

    public void setArchived(boolean archived) {
        this.setMaskValue(MASK_ARCHIVED, archived);
    }

    public boolean isAvailable() {
        return this.getMaskValue(MASK_AVAILABLE);
    }

    public void setAvailable(boolean available) {
        this.setMaskValue(MASK_AVAILABLE, available);
    }

    public boolean isFavorite() {
        return this.getMaskValue(MASK_FAVORITE);
    }

    public void setFavorite(boolean favorite) {
        this.setMaskValue(MASK_FAVORITE, favorite);
    }

    public float getDifficulty() {
        return getFloatX_5FromByte((byte) (DifficultyTerrain & 15));
    }

    public void setDifficulty(float difficulty) {
        DifficultyTerrain = (byte) (DifficultyTerrain & (byte) 240);// clear Bits
        DifficultyTerrain = (byte) (DifficultyTerrain | getDT_HalfByte(difficulty));
    }

    public float getTerrain() {
        return getFloatX_5FromByte((byte) (DifficultyTerrain >>> 4));
    }

    public void setTerrain(float terrain) {
        DifficultyTerrain = (byte) (DifficultyTerrain & (byte) 15);// clear Bits
        DifficultyTerrain = (byte) (DifficultyTerrain | getDT_HalfByte(terrain) << 4);
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
        return this.getMaskValue(MASK_FOUND);
    }

    public void setFound(boolean found) {
        this.setMaskValue(MASK_FOUND, found);
    }

    public boolean isLive() {
        return this.getMaskValue(MASK_IS_LIVE);
    }

    public void setLive(boolean isLive) {
        this.setMaskValue(MASK_IS_LIVE, isLive);
    }

    public boolean isHasUserData() {
        return this.getMaskValue(MASK_HAS_USER_DATA);
    }

    public void setHasUserData(boolean hasUserData) {
        this.setMaskValue(MASK_HAS_USER_DATA, hasUserData);
    }

    public boolean isListingChanged() {
        return this.getMaskValue(MASK_LISTING_CHANGED);
    }

    void setListingChanged(boolean listingChanged) {
        this.setMaskValue(MASK_LISTING_CHANGED, listingChanged);
    }

    public String getPlacedBy() {
        if (detail != null) {
            return detail.PlacedBy;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setPlacedBy(String value) {
        if (detail != null) {
            detail.PlacedBy = value;
        }
    }

    public Date getDateHidden() {
        if (detail != null) {
            return detail.DateHidden;
        } else {
            return null;
        }
    }

    public void setDateHidden(Date date) {
        if (detail != null) {
            detail.DateHidden = date;
        }
    }

    public byte getApiStatus() {
        if (detail != null) {
            return detail.ApiStatus;
        } else {
            return NOT_LIVE;
        }
    }

    public void setApiStatus(byte value) {
        if (detail != null) {
            detail.ApiStatus = value;
        }
    }

    int getNoteChecksum() {
        if (detail != null) {
            return detail.noteCheckSum;
        } else {
            return 0;
        }
    }

    public void setNoteChecksum(int value) {
        if (detail != null) {
            detail.noteCheckSum = value;
        }
    }

    public String getTmpNote() {
        if (detail != null) {
            return detail.tmpNote;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setTmpNote(String value) {
        if (detail != null) {
            detail.tmpNote = value;
        }
    }

    public String getUserNote() {
        if (detail != null) {
            return detail.userNote;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setUserNote(String value) {
        if (detail != null) {
            detail.userNote = value;
        }
    }

    int getSolverChecksum() {
        if (detail != null) {
            return detail.solverCheckSum;
        } else {
            return 0;
        }
    }

    public void setSolverChecksum(int value) {
        if (detail != null) {
            detail.solverCheckSum = value;
        }
    }

    public String getTmpSolver() {
        if (detail != null) {
            return detail.tmpSolver;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setTmpSolver(String value) {
        if (detail != null) {
            detail.tmpSolver = value;
        }
    }

    public String getUrl() {
        if (detail != null) {
            return detail.Url;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setUrl(String value) {
        if (detail != null) {
            detail.Url = value;
        }
    }

    public String getCountry() {
        if (detail != null) {
            return detail.Country;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setCountry(String value) {
        if (detail != null) {
            detail.Country = value;
        }
    }

    public String getState() {
        if (detail != null) {
            return detail.State;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setState(String value) {
        if (detail != null) {
            detail.State = value;
        }
    }

    public ArrayList<Attributes> getAttributes() {
        if (detail != null) {
            return detail.getAttributes(Id);
        } else {
            return null;
        }
    }

    public void addAttributeNegative(Attributes attribute) {
        if (detail != null) {
            detail.addAttributeNegative(attribute);
        }
    }

    public void addAttributePositive(Attributes attribute) {
        if (detail != null) {
            detail.addAttributePositive(attribute);
        }
    }

    DLong getAttributesPositive() {
        if (detail != null) {
            return detail.getAttributesPositive(Id);
        } else {
            return null;
        }
    }

    public void setAttributesPositive(DLong dLong) {
        if (detail != null) {
            detail.setAttributesPositive(dLong);
        }
    }

    DLong getAttributesNegative() {
        if (detail != null) {
            return detail.getAttributesNegative(Id);
        } else {
            return null;
        }
    }

    public void setAttributesNegative(DLong dLong) {
        if (detail != null) {
            detail.setAttributesNegative(dLong);
        }
    }

    public String getLongDescription() {
        if (detail != null) {
            if (detail.getLongDescription() == null || detail.getLongDescription().length() == 0) {
                return Database.getDescription(this);
            }
            return detail.getLongDescription();
        } else {
            return EMPTY_STRING;
        }
    }

    public void setLongDescription(String value) {
        if (detail != null) {
            detail.setLongDescription(value);

        }
    }

    public String getShortDescription() {
        if (detail != null) {
            if (detail.getShortDescription() == null || detail.getShortDescription().length() == 0) {
                return Database.getShortDescription(this);
            }
            return detail.getShortDescription();
        } else {
            return EMPTY_STRING;
        }
    }

    public void setShortDescription(String value) {
        if (detail != null) {
            detail.setShortDescription(value);
        }
    }

    public String getTourName() {
        if (detail != null) {
            return detail.TourName;
        } else {
            return EMPTY_STRING;
        }
    }

    public void setTourName(String value) {
        if (detail != null) {
            detail.TourName = value;
        }
    }

    public boolean isAttributePositiveSet(Attributes attribute) {
        if (detail != null) {
            return detail.isAttributePositiveSet(attribute);
        } else {
            return false;
        }
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    /**
     * Returns true if the Cache a event like Giga, Cito, Event or Mega
     *
     */
    public boolean isEvent() {
        if (this.Type == CacheTypes.Giga)
            return true;
        if (this.Type == CacheTypes.CITO)
            return true;
        if (this.Type == CacheTypes.Event)
            return true;
        return this.Type == CacheTypes.MegaEvent;
    }

}