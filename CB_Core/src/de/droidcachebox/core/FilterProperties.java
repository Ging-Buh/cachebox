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

    public int Finds;
    public int NotAvailable;
    public int Archived;
    public int Own;
    public int ContainsTravelbugs;
    public int Favorites;
    public int ListingChanged;
    public int WithManualWaypoint;
    public int HasUserData;

    public double MinDifficulty;
    public double MaxDifficulty;
    public double MinTerrain;
    public double MaxTerrain;
    public double MinContainerSize;
    public double MaxContainerSize;
    public double MinRating;
    public double MaxRating;
    public double MinFavPoints;
    public double MaxFavPoints;

    public int hasCorrectedCoordinates;
    public boolean isHistory;

    public boolean[] mCacheTypes;

    public int[] mAttributes;

    public ArrayList<Long> GPXFilenameIds;

    public String filterName;
    public String filterGcCode;
    public String filterOwner;

    public ArrayList<Long> Categories;

    /**
     * creates the FilterProperties with default values.
     * For default nothing is filtered!
     */
    public FilterProperties() {
        initCreation();
    }

    /**
     * creates the FilterProperties from a serialization-String
     * an empty serialization-String filters nothing
     *
     * @param serialization
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
                    try {
                        isHistory = json.getBoolean("isHistory");
                    } catch (Exception e) {
                        isHistory = false;
                    }
                    hasCorrectedCoordinates = 0;
                    this.MinFavPoints = -1;
                    this.MaxFavPoints = -1;
                    String caches = json.getString("caches");
                    String[] parts = caches.split(SEPARATOR);
                    for (int cnt = 0; cnt < (parts.length); cnt++) {
                        switch (cnt) {
                            case 0: Finds = Integer.parseInt(parts[0]); break;
                            case 1: NotAvailable = Integer.parseInt(parts[1]); break;
                            case 2: Archived = Integer.parseInt(parts[2]); break;
                            case 3: Own = Integer.parseInt(parts[3]); break;
                            case 4: ContainsTravelbugs = Integer.parseInt(parts[4]); break;
                            case 5: Favorites = Integer.parseInt(parts[5]); break;
                            case 6: HasUserData = Integer.parseInt(parts[6]); break;
                            case 7: ListingChanged = Integer.parseInt(parts[7]); break;
                            case 8: WithManualWaypoint = Integer.parseInt(parts[8]); break;
                            case 9: MinDifficulty = Float.parseFloat(parts[9]); break;
                            case 10: MaxDifficulty = Float.parseFloat(parts[10]); break;
                            case 11: MinTerrain = Float.parseFloat(parts[11]); break;
                            case 12: MaxTerrain = Float.parseFloat(parts[12]); break;
                            case 13: MinContainerSize = Float.parseFloat(parts[13]); break;
                            case 14: MaxContainerSize = Float.parseFloat(parts[14]); break;
                            case 15: MinRating = Float.parseFloat(parts[15]); break;
                            case 16: MaxRating = Float.parseFloat(parts[16]); break;
                            case 17: hasCorrectedCoordinates = Integer.parseInt(parts[17]); break;
                            case 18: MinFavPoints = Double.parseDouble(parts[18]); break;
                            case 19: MaxFavPoints = Double.parseDouble(parts[19]); break;
                        }
                    }

                    mCacheTypes = parseCacheTypes(json.getString("types"));

                    String attributes = json.getString("attributes");
                    parts = attributes.split(SEPARATOR);
                    mAttributes = new int[Attributes.values().length];
                    mAttributes[0] = 0; // gibts nicht
                    int og = parts.length;
                    if (parts.length == mAttributes.length) {
                        og = parts.length - 1; // falls doch schon mal mit mehr gespeichert
                    }
                    for (int i = 0; i < (og); i++)
                        mAttributes[i + 1] = Integer.parseInt(parts[i]);
                    // aus älteren Versionen
                    for (int i = og; i < mAttributes.length - 1; i++)
                        mAttributes[i + 1] = 0;

                    GPXFilenameIds = new ArrayList<>();
                    String gpxfilenames = json.getString("gpxfilenameids");
                    parts = gpxfilenames.split(SEPARATOR);
                    int cnt = 0;
                    if (parts.length > cnt) {
                        String tempGPX = parts[cnt++];
                        String[] partsGPX = tempGPX.split("\\" + GPXSEPARATOR);
                        for (int i = 1; i < partsGPX.length; i++) {
                            GPXFilenameIds.add(Long.parseLong(partsGPX[i]));
                        }
                    }

                    filterName = json.getString("filtername");
                    filterGcCode = json.getString("filtergc");
                    filterOwner = json.getString("filterowner");

                    Categories = new ArrayList<Long>();
                    String filtercategories = json.getString("categories");
                    if (filtercategories.length() > 0) {
                        String[] partsGPX = filtercategories.split("\\" + GPXSEPARATOR);
                        for (int i = 1; i < partsGPX.length; i++) {
                            // Log.info(log, "parts[" + i + "]=" + partsGPX[i]);
                            Categories.add(Long.parseLong(partsGPX[i]));
                        }
                    }
                } catch (JSONException e) {
                    Log.err(log, "Json Version FilterProperties(" + serialization + ")", "", e);
                }
            } else {
                Log.err(log, "Json Version FilterProperties(" + serialization + ")");
            }
        } else {
            // Filter ist noch in alten Einstellungen gegeben...
            try {
                String[] parts = serialization.split(SEPARATOR);
                int cnt = 0;
                Finds = Integer.parseInt(parts[cnt++]);
                NotAvailable = Integer.parseInt(parts[cnt++]);
                Archived = Integer.parseInt(parts[cnt++]);
                Own = Integer.parseInt(parts[cnt++]);
                ContainsTravelbugs = Integer.parseInt(parts[cnt++]);
                Favorites = Integer.parseInt(parts[cnt++]);
                HasUserData = Integer.parseInt(parts[cnt++]);
                ListingChanged = Integer.parseInt(parts[cnt++]);
                WithManualWaypoint = Integer.parseInt(parts[cnt++]);

                MinDifficulty = Float.parseFloat(parts[cnt++]);
                MaxDifficulty = Float.parseFloat(parts[cnt++]);
                MinTerrain = Float.parseFloat(parts[cnt++]);
                MaxTerrain = Float.parseFloat(parts[cnt++]);
                MinContainerSize = Float.parseFloat(parts[cnt++]);
                MaxContainerSize = Float.parseFloat(parts[cnt++]);
                MinRating = Float.parseFloat(parts[cnt++]);
                MaxRating = Float.parseFloat(parts[cnt++]);

                mCacheTypes = new boolean[CacheTypes.values().length];
                for (int i = 0; i < 11; i++)
                    mCacheTypes[i] = Boolean.parseBoolean(parts[cnt++]);
                for (int i = 11; i < CacheTypes.values().length; i++) {
                    mCacheTypes[i] = true;
                }

                mAttributes = new int[Attributes.values().length];
                mAttributes[0] = 0;
                for (int i = 0; i < 66; i++) {
                    if (parts.length > cnt)
                        mAttributes[i + 1] = Integer.parseInt(parts[cnt++]);
                }
                for (int i = 66; i < mAttributes.length; i++)
                    mAttributes[i + 1] = 0;

                GPXFilenameIds = new ArrayList<Long>();
                GPXFilenameIds.clear();
                if (parts.length > cnt) {
                    String tempGPX = parts[cnt++];
                    String[] partsGPX = new String[]{};
                    partsGPX = tempGPX.split("\\" + GPXSEPARATOR);
                    for (int i = 1; i < partsGPX.length; i++) {
                        GPXFilenameIds.add(Long.parseLong(partsGPX[i]));
                    }
                }
                if (parts.length > cnt)
                    filterName = parts[cnt++];
                else
                    filterName = "";
                if (parts.length > cnt)
                    filterGcCode = parts[cnt++];
                else
                    filterGcCode = "";
                if (parts.length > cnt)
                    filterOwner = parts[cnt++];
                else
                    filterOwner = "";

                if (parts.length > cnt) {
                    String tempGPX = parts[cnt++];
                    String[] partsGPX = new String[]{};
                    partsGPX = tempGPX.split("\\" + GPXSEPARATOR);
                    Categories = new ArrayList<Long>();
                    for (int i = 1; i < partsGPX.length; i++) {
                        Categories.add(Long.parseLong(partsGPX[i]));
                    }
                }
            } catch (Exception exc) {
                Log.err(log, "old Version FilterProperties(" + serialization + ")", "", exc);
            }
        }
    }

    private String join(String separator, ArrayList<String> array) {
        String retString = "";

        int count = 0;
        for (String tmp : array) {
            retString += tmp;
            count++;
            if (count < array.size())
                retString += separator;
        }
        return retString;
    }

    private void initCreation() {
        Finds = 0;
        NotAvailable = 0;
        Archived = 0;
        Own = 0;
        ContainsTravelbugs = 0;
        Favorites = 0;
        ListingChanged = 0;
        WithManualWaypoint = 0;
        HasUserData = 0;

        MinDifficulty = 1;
        MaxDifficulty = 5;
        MinTerrain = 1;
        MaxTerrain = 5;
        MinContainerSize = 0;
        MaxContainerSize = 4;
        MinRating = 0;
        MaxRating = 5;
        MinFavPoints = -1;
        MaxFavPoints = -1;

        this.hasCorrectedCoordinates = 0;
        isHistory = false;
        mCacheTypes = new boolean[CacheTypes.values().length];
        Arrays.fill(mCacheTypes, true);

        mAttributes = new int[Attributes.values().length]; // !!! attention: Attributes 0 not used
        Arrays.fill(mAttributes, 0);

        GPXFilenameIds = new ArrayList<>();
        filterName = "";
        filterGcCode = "";
        filterOwner = "";

        Categories = new ArrayList<>();
    }

    private boolean[] parseCacheTypes(String types) {
        String[] parts = types.split(SEPARATOR);
        final boolean[] result = new boolean[CacheTypes.values().length];
        if (parts.length < CacheTypes.values().length) {
            // old (json) version
            for (int i = 0; i < result.length; i++) {
                if (i < parts.length) {
                    result[i] = Boolean.parseBoolean(parts[i]);
                } else {
                    result[i] = true;
                }
            }
            result[CacheTypes.Munzee.ordinal()] = result[11];
            result[CacheTypes.Giga.ordinal()] = result[12];
            result[11] = true;
            result[12] = true;
        } else {
            for (int i = 0; i < result.length; i++) {
                result[i] = Boolean.parseBoolean(parts[i]);
            }
        }
        return result;
    }

    /**
     * True, wenn FilterProperties eine Filterung nach Name, Gc-Code oder Owner enth&auml;lt!
     *
     * @return if it contains or not
     */
    public boolean isExtendedFilter() {
        if (filterName != null && filterName.length() > 0)
            return true;

        if (filterGcCode != null && filterGcCode.length() > 0)
            return true;

        if (filterOwner != null && filterOwner.length() > 0)
            return true;

        return false;
    }

    /**
     * a String to save in the database
     *
     * @return
     */
    @Override
    public String toString() {
        String result = "";

        try {

            JSONObject json = new JSONObject();
            json.put("isHistory", isHistory);
            // add Cache properties
            json.put("caches",
                    Finds + SEPARATOR + NotAvailable + SEPARATOR
                            + Archived + SEPARATOR + Own + SEPARATOR
                            + ContainsTravelbugs + SEPARATOR + Favorites + SEPARATOR
                            + HasUserData + SEPARATOR + ListingChanged + SEPARATOR
                            + WithManualWaypoint + SEPARATOR + MinDifficulty + SEPARATOR
                            + MaxDifficulty + SEPARATOR + MinTerrain + SEPARATOR
                            + MaxTerrain + SEPARATOR + MinContainerSize + SEPARATOR
                            + MaxContainerSize + SEPARATOR + MinRating + SEPARATOR
                            + MaxRating + SEPARATOR + hasCorrectedCoordinates + SEPARATOR
                            + MinFavPoints + SEPARATOR + MaxFavPoints);

            // add Cache Types
            String tmp = "";
            for (int i = 0; i < mCacheTypes.length; i++) {
                if (i > 0)
                    tmp += SEPARATOR;
                tmp += String.valueOf(mCacheTypes[i]);
            }
            json.put("types", tmp);
            // add Cache Attributes
            tmp = "";
            for (int i = 1; i < mAttributes.length; i++) {
                if (tmp.length() > 0)
                    tmp += SEPARATOR;
                tmp += String.valueOf(mAttributes[i]);
            }
            json.put("attributes", tmp);
            // GPX Filenames
            tmp = "";
            for (int i = 0; i <= GPXFilenameIds.size() - 1; i++) {
                tmp += GPXSEPARATOR + String.valueOf(GPXFilenameIds.get(i));
            }
            json.put("gpxfilenameids", tmp);
            // Filter Name
            json.put("filtername", filterName);
            // Filter GCCode
            json.put("filtergc", filterGcCode);
            // Filter Owner
            json.put("filterowner", filterOwner);
            // Categories
            tmp = "";
            for (long i : Categories) {
                tmp += GPXSEPARATOR + i;
            }
            json.put("categories", tmp);

            result = json.toString();
        } catch (JSONException e) {
            Log.err(log, "JSON toString", "", e);
        }
        return result;
    }

    /**
     * Gibt den SQL Where String dieses Filters zurück
     *
     * @param userName Config.settings.GcLogin.getValue()
     * @return
     */
    public String getSqlWhere(String userName) {
        if (isHistory) {
            ArrayList<String> orParts = new ArrayList<String>();
            String[] gcCodes = CoreSettingsForward.cacheHistory.split(",");
            for (int i = 0; i < gcCodes.length; i++) {
                String gcCode = gcCodes[i];
                if (gcCode.length() > 0) {
                    if (!orParts.contains(gcCode))
                        orParts.add("GcCode = '" + gcCode + "'");
                }
            }
            return join(" or ", orParts);
        } else {
            userName = userName.replace("'", "''");

            ArrayList<String> andParts = new ArrayList<String>();

            if (Finds == 1)
                andParts.add("Found=1");
            if (Finds == -1)
                andParts.add("(Found=0 or Found is null)");

            if (NotAvailable == 1)
                andParts.add("Available=0");
            if (NotAvailable == -1)
                andParts.add("Available=1");

            if (Archived == 1)
                andParts.add("Archived=1");
            if (Archived == -1)
                andParts.add("Archived=0");

            if (Own == 1)
                andParts.add("(Owner='" + userName + "')");
            if (Own == -1)
                andParts.add("(not Owner='" + userName + "')");

            if (ContainsTravelbugs == 1)
                andParts.add("NumTravelbugs > 0");
            if (ContainsTravelbugs == -1)
                andParts.add("NumTravelbugs = 0");

            if (Favorites == 1)
                andParts.add("Favorit=1");
            if (Favorites == -1)
                andParts.add("(Favorit=0 or Favorit is null)");

            if (ListingChanged == 1)
                andParts.add("ListingChanged=1");
            if (ListingChanged == -1)
                andParts.add("(ListingChanged=0 or ListingChanged is null)");

            if (WithManualWaypoint == 1)
                andParts.add(" ID in (select CacheId FROM Waypoint WHERE UserWaypoint = 1)");
            if (WithManualWaypoint == -1)
                andParts.add(" NOT ID in (select CacheId FROM Waypoint WHERE UserWaypoint = 1)");

            if (HasUserData == 1)
                andParts.add("HasUserData=1");
            if (HasUserData == -1)
                andParts.add("(HasUserData = 0 or HasUserData is null)");

            andParts.add("Difficulty >= " + MinDifficulty * 2);
            andParts.add("Difficulty <= " + MaxDifficulty * 2);
            andParts.add("Terrain >= " + MinTerrain * 2);
            andParts.add("Terrain <= " + MaxTerrain * 2);
            andParts.add("Size >= " + MinContainerSize);
            andParts.add("Size <= " + MaxContainerSize);
            andParts.add("Rating >= " + MinRating * 100);
            andParts.add("Rating <= " + MaxRating * 100);

            if (MinFavPoints >= 0) andParts.add("FavPoints >= " + MinFavPoints);
            if (MaxFavPoints >= 0) andParts.add("FavPoints <= " + MaxFavPoints);

            FilterInstances.hasCorrectedCoordinates = hasCorrectedCoordinates;

            String csvTypes = "";
            for (int i = 0; i < mCacheTypes.length; i++) {
                if (mCacheTypes[i])
                    csvTypes += i + ",";
            }
            if (csvTypes.length() > 0) {
                csvTypes = csvTypes.substring(0, csvTypes.length() - 1);
                andParts.add("Type in (" + csvTypes + ")");
            }

            for (int i = 1; i < mAttributes.length; i++) {
                if (mAttributes[i] != 0) {
                    if (i < 62) {
                        long shift = DLong.UL1 << (i);
                        if (mAttributes[i] == 1)
                            andParts.add("(AttributesPositive & " + shift + ") > 0");
                        else
                            andParts.add("(AttributesNegative &  " + shift + ") > 0");
                    } else {
                        long shift = DLong.UL1 << (i - 61);
                        if (mAttributes[i] == 1)
                            andParts.add("(AttributesPositiveHigh &  " + shift + ") > 0");
                        else
                            andParts.add("(AttributesNegativeHigh & " + shift + ") > 0");
                    }
                }
            }

            if (GPXFilenameIds.size() != 0) {
                String s = "";
                for (long id : GPXFilenameIds) {
                    s += String.valueOf(id) + ",";
                }
                // s += "-1";
                if (s.length() > 0) {
                    andParts.add("GPXFilename_Id not in (" + s.substring(0, s.length() - 1) + ")");
                }
            }

            if (filterName != "") {
                andParts.add("Name like '%" + filterName + "%'");
            }
            if (filterGcCode != "") {
                andParts.add("GcCode like '%" + filterGcCode + "%'");
            }
            if (filterOwner != "") {
                andParts.add("( PlacedBy like '%" + filterOwner + "%' or Owner like '%" + filterOwner + "%' )");
            }

            return join(" and ", andParts);
        }
    }

    /**
     * Filter miteinander vergleichen wobei Category Einstellungen ignoriert werden sollen
     *
     * @param filter
     * @return
     */
    public boolean equals(FilterProperties filter) {
        if (Finds != filter.Finds)
            return false;
        if (NotAvailable != filter.NotAvailable)
            return false;
        if (Archived != filter.Archived)
            return false;
        if (Own != filter.Own)
            return false;
        if (ContainsTravelbugs != filter.ContainsTravelbugs)
            return false;
        if (Favorites != filter.Favorites)
            return false;
        if (ListingChanged != filter.ListingChanged)
            return false;
        if (WithManualWaypoint != filter.WithManualWaypoint)
            return false;
        if (HasUserData != filter.HasUserData)
            return false;

        if (MinDifficulty != filter.MinDifficulty)
            return false;
        if (MaxDifficulty != filter.MaxDifficulty)
            return false;
        if (MinTerrain != filter.MinTerrain)
            return false;
        if (MaxTerrain != filter.MaxTerrain)
            return false;
        if (MinContainerSize != filter.MinContainerSize)
            return false;
        if (MaxContainerSize != filter.MaxContainerSize)
            return false;
        if (MinRating != filter.MinRating)
            return false;
        if (MaxRating != filter.MaxRating)
            return false;
        if (MinFavPoints != filter.MinFavPoints)
            return false;
        if (MaxFavPoints != filter.MaxFavPoints)
            return false;

        if (hasCorrectedCoordinates != filter.hasCorrectedCoordinates)
            return false;

        if (mCacheTypes == null) {
            if (filter.mCacheTypes != null) return false;
        } else {
            if (filter.mCacheTypes == null) return false;
            if (filter.mCacheTypes.length != mCacheTypes.length) return false;
            for (int i = 0; i < mCacheTypes.length; i++) {
                if (filter.mCacheTypes[i] != this.mCacheTypes[i])
                    return false;
            }
        }

        if (mAttributes == null) {
            if (filter.mAttributes != null) return false;
        } else {
            if (filter.mAttributes == null) return false;
            if (filter.mAttributes.length != mAttributes.length) return false;
            for (int i = 1; i < mAttributes.length; i++) {
                if (filter.mAttributes[i] != this.mAttributes[i])
                    return false; // nicht gleich!!!
            }
        }

        if (GPXFilenameIds == null) {
            if (filter.GPXFilenameIds != null) return false;
        } else {
            if (filter.GPXFilenameIds == null) return false;
            if (GPXFilenameIds.size() != filter.GPXFilenameIds.size())
                return false;
            for (Long gid : GPXFilenameIds) {
                if (!filter.GPXFilenameIds.contains(gid))
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


        if (isHistory != filter.isHistory)
            return false;

        return true;
    }

    /**
     * @param cache
     * @return
     */
    public boolean passed(Cache cache) {
        if (chkFilterBoolean(this.Finds, cache.isFound()))
            return false;
        if (chkFilterBoolean(this.Own, cache.ImTheOwner()))
            return false;
        if (chkFilterBoolean(this.NotAvailable, !cache.isAvailable()))
            return false;
        if (chkFilterBoolean(this.Archived, cache.isArchived()))
            return false;
        if (chkFilterBoolean(this.ContainsTravelbugs, cache.NumTravelbugs > 0))
            return false;
        if (chkFilterBoolean(this.Favorites, cache.isFavorite()))
            return false;
        if (chkFilterBoolean(this.ListingChanged, cache.isListingChanged()))
            return false;
        if (chkFilterBoolean(this.HasUserData, cache.isHasUserData()))
            return false;
        if (chkFilterBoolean(this.hasCorrectedCoordinates, cache.hasCorrectedCoordinates()))
            return false;
        // TODO implement => if (chkFilterBoolean(this.WithManualWaypoint, cache.)) return false;
        // TODO ? the other restrictions?
        if (!this.mCacheTypes[cache.getType().ordinal()])
            return false;

        return true;
    }

    /**
     * @param propertyValue
     * @param found
     * @return
     */
    private boolean chkFilterBoolean(int propertyValue, boolean found) {
        // -1= Cache.{attribute} == False
        // 0= Cache.{attribute} == False|True
        // 1= Cache.{attribute} == True

        if (propertyValue != 0) {
            if (propertyValue != (found ? 1 : -1))
                return true;
        }
        return false;
    }

}