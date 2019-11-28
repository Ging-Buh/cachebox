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
package de.droidcachebox.core;

import de.droidcachebox.database.Attributes;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.CacheTypes;
import de.droidcachebox.utils.DLong;
import de.droidcachebox.utils.log.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.Arrays;

public class FilterProperties {
    private static final String log = "FilterProperties";

    private final static String SEPARATOR = ",";
    private final static String GPXSEPARATOR = "^";
    // json.getBoolean("isHistory"); // only remember, that last filter was this
    // the GCCodes are not saved in db but taken from CoreSettingsForward.cacheHistory
    public boolean isHistory;
    // json.getString("types")
    // was a boolean for each geoCachetype in CacheTypes.caches() !!! no waypoints
    // now only the ordinals of a cacheType of  CacheTypes.caches() separated by ,
    public String cacheTypes;
    // json.getString("attributes");
    // an int for each Attribute in Attributes.values()
    public int[] attributes;
    // json.optString("gpxfilenameids", "");
    public ArrayList<Long> gpxFilenameIds;
    // json.optString("categories", "");
    public ArrayList<Long> categories;
    // json.optString("filtername", "");
    public String filterName;
    // json.optString("filterGcCode", "");
    public String filterGcCode;
    // json.optString("filterOwner", "");
    public String filterOwner;
    // json object "caches":
    // the elements are identified by fixed sequence
    // for boolean geoCacheProperties the filterProperty definition is by int:
    // geoCache is shown,
    // if filterProperyvalue == -1 and geoCacheProperty is false
    // or filterProperyvalue ==  0
    // or filterProperyvalue ==  1 and geoCacheProperty is true
    // filterProperyvalue for boolean geoCacheProperties
    // initialized with 0
    //
    // for an area
    // geoCache is shown, if >= minValue and <= maxValue
    // initialized with the corresponding min- and max values
    private int finds; // 0
    private int notAvailable; // 1
    private int archived; // 2
    private int own; // 3
    private int containsTravelbugs; // 4
    private int favorites; // 5
    private int listingChanged; // 6
    private int withManualWaypoint; // 7
    private int hasUserData; // 8
    private double minDifficulty; // 9
    private double maxDifficulty; // 10
    private double minTerrain; // 11
    private double maxTerrain; // 12
    private double minContainerSize; // 13
    private double maxContainerSize; // 14
    private double minRating; // 15
    private double maxRating; // 16
    private int hasCorrectedCoordinates; // 17
    private double minFavPoints; // 18
    private double maxFavPoints; // 19
    // json.optString("UserDefinedSQL");
    private String userDefinedSQL;

    /**
     * creates the FilterProperties with default values.
     * For default nothing is filtered!
     */
    public FilterProperties() {
        initCreation();
    }

    /**
     * creates the FilterProperties from a serialization-String
     * an empty serialization-String filters nothing: means show all
     *
     * @param serialization now a json string
     */
    public FilterProperties(String serialization) {
        initCreation();

        if (serialization.length() == 0) {
            return;
        }
        // Try to parse as JSON
        if (serialization.startsWith("{")) {
            if (serialization.endsWith("}")) {
                JSONTokener tokener = new JSONTokener(serialization);
                try {
                    JSONObject json = (JSONObject) tokener.nextValue();
                    isHistory = json.optBoolean("isHistory", false);
                    userDefinedSQL = json.optString("UserDefinedSQL", "");

                    String caches = json.getString("caches");
                    String[] parts = caches.split(SEPARATOR);
                    for (int cnt = 0; cnt < (parts.length); cnt++) {
                        switch (cnt) {
                            case 0:
                                finds = Integer.parseInt(parts[0]);
                                break;
                            case 1:
                                notAvailable = Integer.parseInt(parts[1]);
                                break;
                            case 2:
                                archived = Integer.parseInt(parts[2]);
                                break;
                            case 3:
                                own = Integer.parseInt(parts[3]);
                                break;
                            case 4:
                                containsTravelbugs = Integer.parseInt(parts[4]);
                                break;
                            case 5:
                                favorites = Integer.parseInt(parts[5]);
                                break;
                            case 6:
                                hasUserData = Integer.parseInt(parts[6]);
                                break;
                            case 7:
                                listingChanged = Integer.parseInt(parts[7]);
                                break;
                            case 8:
                                withManualWaypoint = Integer.parseInt(parts[8]);
                                break;
                            case 9:
                                minDifficulty = Float.parseFloat(parts[9]);
                                break;
                            case 10:
                                maxDifficulty = Float.parseFloat(parts[10]);
                                break;
                            case 11:
                                minTerrain = Float.parseFloat(parts[11]);
                                break;
                            case 12:
                                maxTerrain = Float.parseFloat(parts[12]);
                                break;
                            case 13:
                                minContainerSize = Float.parseFloat(parts[13]);
                                break;
                            case 14:
                                maxContainerSize = Float.parseFloat(parts[14]);
                                break;
                            case 15:
                                minRating = Float.parseFloat(parts[15]);
                                break;
                            case 16:
                                maxRating = Float.parseFloat(parts[16]);
                                break;
                            case 17:
                                hasCorrectedCoordinates = Integer.parseInt(parts[17]);
                                break;
                            case 18:
                                minFavPoints = Double.parseDouble(parts[18]);
                                break;
                            case 19:
                                maxFavPoints = Double.parseDouble(parts[19]);
                                break;
                        }
                    }

                    cacheTypes = json.optString("CacheTypes", "");

                    String jsonString = json.optString("attributes", "");
                    if (jsonString.length() > 0) {
                        parts = jsonString.split(SEPARATOR);
                        attributes = new int[Attributes.values().length];
                        attributes[0] = 0; // gibts nicht
                        int og = parts.length;
                        if (parts.length == attributes.length) {
                            og = parts.length - 1; // falls doch schon mal mit mehr gespeichert
                        }
                        for (int i = 0; i < (og); i++)
                            attributes[i + 1] = Integer.parseInt(parts[i]);
                        // aus älteren Versionen
                        for (int i = og; i < attributes.length - 1; i++)
                            attributes[i + 1] = 0;
                    }

                    gpxFilenameIds = new ArrayList<>();
                    String gpxFileNames = json.optString("gpxfilenameids", "");
                    if (gpxFileNames.length() > 0) {
                        for (String gpxFileName : gpxFileNames.split(SEPARATOR)) {
                            // !!! String, starting with GPXSEPARATOR gives first item empty String)
                            String[] gpxFileNameIds = gpxFileName.split("\\" + GPXSEPARATOR);
                            for (String gpxFileNameId : gpxFileNameIds) {
                                if (gpxFileNameId.length() > 0)
                                    gpxFilenameIds.add(Long.parseLong(gpxFileNameId));
                            }
                        }
                    }

                    filterName = json.optString("filtername", "");
                    filterGcCode = json.optString("filtergc", "");
                    filterOwner = json.optString("filterowner", "");

                    categories = new ArrayList<>();
                    String filtercategories = json.optString("categories", "");
                    if (filtercategories.length() > 0) {
                        String[] partsGPX = filtercategories.split("\\" + GPXSEPARATOR);
                        // !!! String, starting with GPXSEPARATOR gives first item empty String)
                        for (String filterCategory : partsGPX) {
                            if (filterCategory.length() > 0)
                                categories.add(Long.parseLong(filterCategory));
                        }
                    }

                } catch (JSONException ex) {
                    Log.err(log, "Json Version FilterProperties(" + serialization + ")", ex);
                }
            } else {
                Log.err(log, "Json Version FilterProperties(" + serialization + ")");
            }
        } else {
            Log.err(log, "old Version FilterProperties are no longer supported");
        }
    }

    public FilterProperties(boolean b) {
        initCreation();
        if (b)
            userDefinedSQL = "     ";
        else
            userDefinedSQL = "";
    }

    private String join(String separator, ArrayList<String> array) {
        StringBuilder retString = new StringBuilder();
        int count = 0;
        for (String tmp : array) {
            retString.append(tmp);
            count++;
            if (count < array.size())
                retString.append(separator);
        }
        return retString.toString();
    }

    /**
     * initialize default values
     */
    private void initCreation() {
        isHistory = false;
        userDefinedSQL = "";
        finds = 0;
        notAvailable = 0;
        archived = 0;
        own = 0;
        containsTravelbugs = 0;
        favorites = 0;
        listingChanged = 0;
        withManualWaypoint = 0;
        hasUserData = 0;

        minDifficulty = 1;
        maxDifficulty = 5;
        minTerrain = 1;
        maxTerrain = 5;
        minContainerSize = 0;
        maxContainerSize = 4;
        minRating = 0;
        maxRating = 5;
        hasCorrectedCoordinates = 0;
        minFavPoints = -1;
        maxFavPoints = -1;

        cacheTypes = "";

        attributes = new int[Attributes.values().length]; // !!! attention: Attributes 0 not used
        Arrays.fill(attributes, 0);

        gpxFilenameIds = new ArrayList<>();
        filterName = "";
        filterGcCode = "";
        filterOwner = "";

        categories = new ArrayList<>();
    }

    public int getFinds() {
        return finds;
    }

    public void setFinds(int finds) {
        this.finds = finds;
    }

    public int getNotAvailable() {
        return notAvailable;
    }

    public void setNotAvailable(int notAvailable) {
        this.notAvailable = notAvailable;
    }

    public int getArchived() {
        return archived;
    }

    public void setArchived(int archived) {
        this.archived = archived;
    }

    public int getOwn() {
        return own;
    }

    public void setOwn(int own) {
        this.own = own;
    }

    public int getContainsTravelbugs() {
        return containsTravelbugs;
    }

    public void setContainsTravelbugs(int containsTravelbugs) {
        this.containsTravelbugs = containsTravelbugs;
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public int getHasUserData() {
        return hasUserData;
    }

    public void setHasUserData(int hasUserData) {
        this.hasUserData = hasUserData;
    }

    public int getListingChanged() {
        return listingChanged;
    }

    public void setListingChanged(int listingChanged) {
        this.listingChanged = listingChanged;
    }

    public int getWithManualWaypoint() {
        return withManualWaypoint;
    }

    public void setWithManualWaypoint(int withManualWaypoint) {
        this.withManualWaypoint = withManualWaypoint;
    }

    public int getHasCorrectedCoordinates() {
        return hasCorrectedCoordinates;
    }

    public void setHasCorrectedCoordinates(int hasCorrectedCoordinates) {
        this.hasCorrectedCoordinates = hasCorrectedCoordinates;
    }

    public double getMinDifficulty() {
        return minDifficulty;
    }

    public void setMinDifficulty(double minDifficulty) {
        this.minDifficulty = minDifficulty;
    }

    public double getMaxDifficulty() {
        return maxDifficulty;
    }

    public void setMaxDifficulty(double maxDifficulty) {
        this.maxDifficulty = maxDifficulty;
    }

    public double getMinTerrain() {
        return minTerrain;
    }

    public void setMinTerrain(double minTerrain) {
        this.minTerrain = minTerrain;
    }

    public double getMaxTerrain() {
        return maxTerrain;
    }

    public void setMaxTerrain(double maxTerrain) {
        this.maxTerrain = maxTerrain;
    }

    public double getMinContainerSize() {
        return minContainerSize;
    }

    public void setMinContainerSize(double minContainerSize) {
        this.minContainerSize = minContainerSize;
    }

    public double getMaxContainerSize() {
        return maxContainerSize;
    }

    public void setMaxContainerSize(double maxContainerSize) {
        this.maxContainerSize = maxContainerSize;
    }

    public double getMinRating() {
        return minRating;
    }

    public void setMinRating(double minRating) {
        this.minRating = minRating;
    }

    public double getMaxRating() {
        return maxRating;
    }

    public void setMaxRating(double maxRating) {
        this.maxRating = maxRating;
    }

    public double getMinFavPoints() {
        return minFavPoints;
    }

    public void setMinFavPoints(double minFavPoints) {
        this.minFavPoints = minFavPoints;
    }

    public double getMaxFavPoints() {
        return maxFavPoints;
    }

    public void setMaxFavPoints(double maxFavPoints) {
        this.maxFavPoints = maxFavPoints;
    }

    /**
     * a String to save in the database
     */
    @Override
    public String toString() {
        String asJsonString = "";

        try {

            JSONObject json = new JSONObject();
            if (isHistory)
                json.put("isHistory", true);
            if (userDefinedSQL.length() > 0) {
                json.put("UserDefinedSQL", userDefinedSQL);
            }
            // add Cache properties
            json.put("caches", finds + SEPARATOR
                    + notAvailable + SEPARATOR
                    + archived + SEPARATOR
                    + own + SEPARATOR
                    + containsTravelbugs + SEPARATOR
                    + favorites + SEPARATOR
                    + hasUserData + SEPARATOR
                    + listingChanged + SEPARATOR
                    + withManualWaypoint + SEPARATOR
                    + minDifficulty + SEPARATOR
                    + maxDifficulty + SEPARATOR
                    + minTerrain + SEPARATOR
                    + maxTerrain + SEPARATOR
                    + minContainerSize + SEPARATOR
                    + maxContainerSize + SEPARATOR
                    + minRating + SEPARATOR
                    + maxRating + SEPARATOR
                    + hasCorrectedCoordinates + SEPARATOR
                    + minFavPoints + SEPARATOR
                    + maxFavPoints);

            // add Cache Types
            if (cacheTypes.length() > 0 && cacheTypes.split(",").length != CacheTypes.caches().length)
                json.put("CacheTypes", cacheTypes);

            // add Cache Attributes
            boolean notAllAttributes = false;
            StringBuilder tmp = new StringBuilder();
            for (int i = 1; i < attributes.length; i++) {
                if (tmp.length() > 0)
                    tmp.append(SEPARATOR);
                tmp.append(attributes[i]);
                if (attributes[i] != 0) notAllAttributes = true;
            }
            if (notAllAttributes)
                if (tmp.length() > 0)
                    json.put("attributes", tmp.toString());

            // GPX Filenames
            tmp = new StringBuilder();
            for (int i = 0; i <= gpxFilenameIds.size() - 1; i++) {
                tmp.append(GPXSEPARATOR).append(gpxFilenameIds.get(i));
            }
            if (tmp.length() > 0)
                json.put("gpxfilenameids", tmp.toString());

            // title, GCCode, owner
            if (filterName.length() > 0)
                json.put("filtername", filterName);
            if (filterGcCode.length() > 0)
                json.put("filtergc", filterGcCode);
            if (filterOwner.length() > 0)
                json.put("filterowner", filterOwner);

            // Categories
            tmp = new StringBuilder();
            for (long i : categories) {
                tmp.append(GPXSEPARATOR).append(i);
            }
            if (tmp.length() > 0)
                json.put("categories", tmp.toString());

            asJsonString = json.toString();
        } catch (JSONException ex) {
            Log.err(log, "JSON toString", ex);
        }
        return asJsonString;
    }

    /**
     * Gibt den SQL Where String dieses Filters zurück
     *
     * @param userName Config.settings.GcLogin.getValue()
     * @return sql query string
     */
    public String getSqlWhere(String userName) {
        if (isHistory) {
            ArrayList<String> orParts = new ArrayList<>();
            String[] gcCodes = CoreSettingsForward.cacheHistory.split(",");
            for (String gcCode : gcCodes) {
                if (gcCode.length() > 0) {
                    if (!orParts.contains(gcCode))
                        orParts.add("GcCode = '" + gcCode + "'");
                }
            }
            return join(" or ", orParts);
        } else if (userDefinedSQL.length() > 0) {
            return userDefinedSQL;
            // "     left JOIN Logs on Caches.Id = Logs.CacheId where Logs.Type is NULL";
        } else {
            userName = userName.replace("'", "''");

            ArrayList<String> andParts = new ArrayList<>();

            if (finds == 1)
                andParts.add("Found=1");
            if (finds == -1)
                andParts.add("(Found=0 or Found is null)");

            if (notAvailable == 1)
                andParts.add("Available=0");
            if (notAvailable == -1)
                andParts.add("Available=1");

            if (archived == 1)
                andParts.add("Archived=1");
            if (archived == -1)
                andParts.add("Archived=0");

            if (own == 1)
                andParts.add("(Owner='" + userName + "')");
            if (own == -1)
                andParts.add("(not Owner='" + userName + "')");

            if (containsTravelbugs == 1)
                andParts.add("NumTravelbugs > 0");
            if (containsTravelbugs == -1)
                andParts.add("NumTravelbugs = 0");

            if (favorites == 1)
                andParts.add("Favorit=1");
            if (favorites == -1)
                andParts.add("(Favorit=0 or Favorit is null)");

            if (listingChanged == 1)
                andParts.add("ListingChanged=1");
            if (listingChanged == -1)
                andParts.add("(ListingChanged=0 or ListingChanged is null)");

            if (withManualWaypoint == 1)
                andParts.add(" ID in (select CacheId FROM Waypoint WHERE UserWaypoint = 1)");
            if (withManualWaypoint == -1)
                andParts.add(" NOT ID in (select CacheId FROM Waypoint WHERE UserWaypoint = 1)");

            if (hasUserData == 1)
                andParts.add("HasUserData=1");
            if (hasUserData == -1)
                andParts.add("(HasUserData = 0 or HasUserData is null)");

            if (minDifficulty > 1)
                andParts.add("Difficulty >= " + minDifficulty * 2);
            if (maxDifficulty < 5)
                andParts.add("Difficulty <= " + maxDifficulty * 2);
            if (minTerrain > 1)
                andParts.add("Terrain >= " + minTerrain * 2);
            if (maxTerrain < 5)
                andParts.add("Terrain <= " + maxTerrain * 2);
            if (minContainerSize > 0)
                andParts.add("Size >= " + minContainerSize);
            if (maxContainerSize < 4)
                andParts.add("Size <= " + maxContainerSize);
            if (minRating > 0)
                andParts.add("Rating >= " + minRating * 100);
            if (maxRating < 5)
                andParts.add("Rating <= " + maxRating * 100);
            if (minFavPoints > 0)
                andParts.add("FavPoints >= " + minFavPoints);
            if (maxFavPoints > 0) andParts.add("FavPoints <= " + maxFavPoints);
            // FilterInstances.hasCorrectedCoordinates = hasCorrectedCoordinates; // reflects final waypoint
            if (hasCorrectedCoordinates == 1)
                andParts.add("CorrectedCoordinates=1 OR ID in (select CacheId FROM Waypoint Where Type = 18 and Latitude > 0 and Longitude > 0)");
            if (hasCorrectedCoordinates == -1)
                andParts.add("CorrectedCoordinates=0 OR Name like '%hallenge%' OR NOT ID in (select CacheId FROM Waypoint Where Type = 18 and Latitude > 0 and Longitude > 0)");

            if (cacheTypes.length() > 0 && cacheTypes.split(",").length != CacheTypes.caches().length)
                andParts.add("Type in (" + cacheTypes + ")");

            for (int i = 1; i < attributes.length; i++) {
                if (attributes[i] != 0) {
                    if (i < 62) {
                        long shift = DLong.UL1 << (i);
                        if (attributes[i] == 1)
                            andParts.add("(AttributesPositive & " + shift + ") > 0");
                        else
                            andParts.add("(AttributesNegative &  " + shift + ") > 0");
                    } else {
                        long shift = DLong.UL1 << (i - 61);
                        if (attributes[i] == 1)
                            andParts.add("(AttributesPositiveHigh &  " + shift + ") > 0");
                        else
                            andParts.add("(AttributesNegativeHigh & " + shift + ") > 0");
                    }
                }
            }

            if (gpxFilenameIds.size() != 0) {
                StringBuilder s = new StringBuilder();
                for (long id : gpxFilenameIds) {
                    s.append(id).append(",");
                }
                // s += "-1";
                if (s.length() > 0) {
                    andParts.add("GPXFilename_Id not in (" + s.substring(0, s.length() - 1) + ")");
                }
            }

            if (filterName.length() > 0) {
                andParts.add("Name like '%" + filterName + "%'");
            }
            if (filterGcCode.length() > 0) {
                andParts.add("GcCode like '%" + filterGcCode + "%'");
            }
            if (filterOwner.length() > 0) {
                andParts.add("( PlacedBy like '%" + filterOwner + "%' or Owner like '%" + filterOwner + "%' )");
            }

            return join(" and ", andParts);
        }
    }

    /**
     * Filter miteinander vergleichen wobei Category Einstellungen ignoriert werden sollen
     */
    public boolean equals(FilterProperties filter) {
        if (finds != filter.finds)
            return false;
        if (notAvailable != filter.notAvailable)
            return false;
        if (archived != filter.archived)
            return false;
        if (own != filter.own)
            return false;
        if (containsTravelbugs != filter.containsTravelbugs)
            return false;
        if (favorites != filter.favorites)
            return false;
        if (listingChanged != filter.listingChanged)
            return false;
        if (withManualWaypoint != filter.withManualWaypoint)
            return false;
        if (hasUserData != filter.hasUserData)
            return false;

        if (minDifficulty != filter.minDifficulty)
            return false;
        if (maxDifficulty != filter.maxDifficulty)
            return false;
        if (minTerrain != filter.minTerrain)
            return false;
        if (maxTerrain != filter.maxTerrain)
            return false;
        if (minContainerSize != filter.minContainerSize)
            return false;
        if (maxContainerSize != filter.maxContainerSize)
            return false;
        if (minRating != filter.minRating)
            return false;
        if (maxRating != filter.maxRating)
            return false;
        if (minFavPoints != filter.minFavPoints)
            return false;
        if (maxFavPoints != filter.maxFavPoints)
            return false;

        if (hasCorrectedCoordinates != filter.hasCorrectedCoordinates)
            return false;

        String[] thisCacheTypes = cacheTypes.split(",");
        String[] filterCacheTypes = filter.cacheTypes.split(",");
        Arrays.sort(thisCacheTypes);
        Arrays.sort(filterCacheTypes);
        if (!Arrays.equals(thisCacheTypes, filterCacheTypes)) return false;

        if (attributes == null) {
            if (filter.attributes != null) return false;
        } else {
            if (filter.attributes == null) return false;
            if (filter.attributes.length != attributes.length) return false;
            for (int i = 1; i < attributes.length; i++) {
                if (filter.attributes[i] != attributes[i])
                    return false; // nicht gleich!!!
            }
        }

        if (gpxFilenameIds == null) {
            if (filter.gpxFilenameIds != null) return false;
        } else {
            if (filter.gpxFilenameIds == null) return false;
            if (gpxFilenameIds.size() != filter.gpxFilenameIds.size())
                return false;
            for (Long gid : gpxFilenameIds) {
                if (!filter.gpxFilenameIds.contains(gid))
                    return false;
            }
        }

        if (filterOwner == null) {
            if (filter.filterOwner != null) return false;
        } else {
            if (filter.filterOwner == null) return false;
            if (!filterOwner.equals(filter.filterOwner))
                return false;
        }

        if (filterGcCode == null) {
            if (filter.filterGcCode != null) return false;
        } else {
            if (filter.filterGcCode == null) return false;
            if (!filterGcCode.equals(filter.filterGcCode))
                return false;
        }

        if (filterName == null) {
            if (filter.filterName != null) return false;
        } else {
            if (filter.filterName == null) return false;
            if (!filterName.equals(filter.filterName))
                return false;
        }

        if (userDefinedSQL.length() > 0) {
            if (!getSqlWhere("").equals(filter.getSqlWhere(""))) {
                return false;
            }
        }

        return isHistory == filter.isHistory;
    }

    /**
     * @param geoCache apply the filter on a single geoCache
     * @return true if geoCache fulfills all properties
     */
    boolean passed(Cache geoCache) {
        if (chkFilterBoolean(finds, geoCache.isFound()))
            return false;
        if (chkFilterBoolean(own, geoCache.ImTheOwner()))
            return false;
        if (chkFilterBoolean(notAvailable, !geoCache.isAvailable()))
            return false;
        if (chkFilterBoolean(archived, geoCache.isArchived()))
            return false;
        if (chkFilterBoolean(containsTravelbugs, geoCache.NumTravelbugs > 0))
            return false;
        if (chkFilterBoolean(favorites, geoCache.isFavorite()))
            return false;
        if (chkFilterBoolean(listingChanged, geoCache.isListingChanged()))
            return false;
        if (chkFilterBoolean(hasUserData, geoCache.isHasUserData()))
            return false;
        if (chkFilterBoolean(hasCorrectedCoordinates, geoCache.hasCorrectedCoordinates()))
            return false;
        // TODO ? the other restrictions?
        return cacheTypes.contains("" + geoCache.getType().ordinal());
    }

    private boolean chkFilterBoolean(int filterProperty, boolean geoCacheProperty) {
        // -1= Cache.{attribute} == False
        // 0 = Cache.{attribute} == returns false (maybe False|True)
        // 1 = Cache.{attribute} == True

        if (filterProperty != 0) {
            return filterProperty != (geoCacheProperty ? 1 : -1);
        }
        return false;
    }

}