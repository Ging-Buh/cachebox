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
package CB_Core.Api;

import CB_Core.*;
import CB_Core.Types.CacheDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Import.DescriptionImageGrabber;
import CB_Core.Types.*;
import CB_Locator.CoordinateGPS;
import CB_Utils.Interfaces.ICancel;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import CB_Utils.http.Webb;
import de.cb.sqlite.CoreCursor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import static CB_Core.Api.GroundspeakAPI.*;

public class SearchForGeocaches_Core {
    private static final String log = "SearchForGeocaches_Core";

    private static int getApiStatus(JSONObject json) {

        try {
            JSONObject jsonStatus = json.getJSONObject("Status");
            int status = jsonStatus.getInt("StatusCode");
            String statusMessage = jsonStatus.getString("StatusMessage");
            String exceptionDetails = jsonStatus.getString("ExceptionDetails");

            String logString = "StatusCode = " + status + "\n" + statusMessage + "\n" + exceptionDetails;

            if (status == 0)
                return status;

            if (status == 2) {
                // Not authorized
                API_ErrorEventHandlerList.handleApiKeyError(API_ErrorEventHandlerList.API_ERROR.INVALID);
                Log.warn(log, "API-Error: " + logString);
                return status;
            }

            if (status == 3) {
                // API Key expired
                API_ErrorEventHandlerList.handleApiKeyError(API_ErrorEventHandlerList.API_ERROR.EXPIRED);
                Log.warn(log, "API-Error: " + logString);
                return status;
            }

            if (status == 141) {
                // / {"Status":{"StatusCode":141,"StatusMessage":"The AccessToken provided is not valid"
                API_ErrorEventHandlerList.handleApiKeyError(API_ErrorEventHandlerList.API_ERROR.INVALID);
                Log.warn(log, "API-Error: " + logString);
                return status;
            }

            // unknown
            API_ErrorEventHandlerList.handleApiKeyError(API_ErrorEventHandlerList.API_ERROR.INVALID);
            Log.warn(log, "API-Error: " + logString);
            return status;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public String SearchForGeocachesJSON(Search search, CB_List<Cache> cacheList, ArrayList<LogEntry> logList, ArrayList<ImageEntry> imageList, long gpxFilenameId, ICancel icancel) {

        int startIndex = 0;
        int searchNumber = search.number <= 50 ? search.number : 50;

        byte apiStatus;
        boolean isLite;
        if (IsPremiumMember()) {
            isLite = false;
            apiStatus = 2;
        } else {
            isLite = true;
            apiStatus = 1;
        }

        if (!isLite) {
            // Check if search a lite request
            isLite = search.isLite;
        }

        JSONObject request = new JSONObject();

        try {
            request.put("AccessToken", GetSettingsAccessToken());
            request.put("StartIndex", startIndex);

            if (search instanceof SearchGC) {
                isLite = false;
                SearchGC searchGC = (SearchGC) search;

                try {
                    request.put("IsLight", false);
                    request.put("MaxPerPage", 1);
                    request.put("GeocacheLogCount", 10);
                    request.put("TrackableLogCount", 10);
                    JSONObject requestcc = new JSONObject();
                    JSONArray requesta = new JSONArray();

                    for (String gcCode : searchGC.gcCodes) {
                        requesta.put(gcCode);
                    }

                    requestcc.put("CacheCodes", requesta);
                    request.put("CacheCode", requestcc);
                } catch (JSONException e) {
                    Log.err(log, "SearchForGeocaches:JSONException", e);
                }
                // ein einzelner Cache wird voll geladen
                apiStatus = 2;

            } else if (search instanceof SearchGCName) {
                SearchGCName searchC = (SearchGCName) search;
                if (isLite)
                    request.put("IsLight", true);
                else
                    request.put("IsLight", false);
                request.put("MaxPerPage", searchNumber);

                JSONObject GeocacheName = new JSONObject()
                        .put("GeocacheName", searchC.gcName);
                request.put("GeocacheName", GeocacheName);

                JSONObject Point = new JSONObject()
                        .put("Latitude", searchC.pos.getLatitude())
                        .put("Longitude", searchC.pos.getLongitude());
                JSONObject PointRadius = new JSONObject()
                        .put("DistanceInMeters", 5000000)
                        .put("Point", Point);
                request.put("PointRadius", PointRadius);

                request = writeExclusions(request, searchC);

            } else if (search instanceof SearchGCOwner) {
                SearchGCOwner searchC = (SearchGCOwner) search;
                if (isLite)
                    request.put("IsLight", true);
                else
                    request.put("IsLight", false);
                request.put("MaxPerPage", searchNumber);

                JSONArray UserNames = new JSONArray().put(searchC.OwnerName);
                JSONObject HiddenByUsers = new JSONObject().put("UserNames", UserNames);
                request.put("HiddenByUsers", HiddenByUsers);

                request.put("GeocacheLogCount", 3);
                request.put("TrackableLogCount", 2);

                JSONObject Point = new JSONObject()
                        .put("Latitude", searchC.pos.getLatitude())
                        .put("Longitude", searchC.pos.getLongitude());
                JSONObject PointRadius = new JSONObject()
                        .put("DistanceInMeters", 5000000)
                        .put("Point", Point);
                request.put("PointRadius", PointRadius);

                request = writeExclusions(request, searchC);

            } else if (search instanceof SearchCoordinate) {
                SearchCoordinate searchC = (SearchCoordinate) search;

                if (isLite)
                    request.put("IsLight", true);
                else
                    request.put("IsLight", false);

                request.put("MaxPerPage", searchNumber);

                JSONObject Point = new JSONObject()
                        .put("Latitude", searchC.pos.getLatitude())
                        .put("Longitude", searchC.pos.getLongitude());
                JSONObject PointRadius = new JSONObject()
                        .put("DistanceInMeters", (int) searchC.distanceInMeters)
                        .put("Point", Point);
                request.put("PointRadius", PointRadius);

                if (searchC.excludeHides) {
                    JSONArray UserNames = new JSONArray().put(CB_Core_Settings.GcLogin.getValue());
                    JSONObject NotHiddenByUsers = new JSONObject().put("UserNames", UserNames);
                    request.put("NotHiddenByUsers", NotHiddenByUsers);
                }

                if (searchC.excludeFounds) {
                    JSONArray UserNames = new JSONArray().put(CB_Core_Settings.GcLogin.getValue());
                    JSONObject NotFoundByUsers = new JSONObject().put("UserNames", UserNames);
                    request.put("NotFoundByUsers", NotFoundByUsers);
                }

                request = writeExclusions(request, searchC);
            }
        }
        catch (Exception e) {
            Log.err(log,"SearchForGeocaches: Can't create request", e);
            return "";
        }

        try {
            JSONObject json = Webb.create()
                    .post(getUrl("SearchForGeocaches?format=json"))
                    .body(request)
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            if (getApiStatus(json) == OK) {
                // save result, if this is a Live-Request
                if (search instanceof SearchLiveMap) {
                    SearchLiveMap mSearchLiveMap = (SearchLiveMap) search;
                    Writer writer = null;
                    try {
                        String Path = mSearchLiveMap.descriptor.getLocalCachePath(LiveMapQue.LIVE_CACHE_NAME) + LiveMapQue.LIVE_CACHE_EXTENSION;
                        if (FileIO.createDirectory(Path)) {
                            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Path), "utf-8"));
                            // todo check if format ok
                            writer.write(json.toString());
                        }
                    } catch (IOException ex) {
                        // report
                    } finally {
                        try {
                            writer.close();
                        } catch (Exception ex) {
                        }
                    }
                }

                String lastError = ParseJsonResult(search, cacheList, logList, imageList, gpxFilenameId, json, apiStatus, isLite);
                if (searchNumber > 1 && cacheList.size() == searchNumber) {
                    // fetch more (than 50) caches
                    // startIndex++;
                    int lastCacheListSize;

                    do {
                        lastCacheListSize = cacheList.size();

                        startIndex += searchNumber;

                        request = new JSONObject().put("AccessToken", GetSettingsAccessToken());

                        if (isLite)
                            request.put("IsLight", true);
                        else
                            request.put("IsLight", false);


                        request.put("StartIndex", startIndex);
                        request.put("MaxPerPage", 50); // == searchNumber

                        request.put("GeocacheLogCount", 3);
                        request.put("TrackableLogCount", 2);

                        try {
                            json = Webb.create()
                                    .post(getUrl("GetMoreGeocaches?format=json"))
                                    .body(request)
                                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                                    .ensureSuccess()
                                    .asJsonObject()
                                    .getBody();
                            lastError = ParseJsonResult(search, cacheList, logList, imageList, gpxFilenameId, json, apiStatus, isLite);
                        } catch (Exception e) {
                            return e.getLocalizedMessage();
                        }

                    }
                    while ((startIndex + searchNumber <= search.number) && (cacheList.size() - lastCacheListSize >= searchNumber) && (lastCacheListSize != cacheList.size() || startIndex + searchNumber > cacheList.size()));
                }
                return lastError;
            } else {
                // Error
                return "";
            }
        } catch (Exception e) {
            Log.err(log, "SearchForGeocaches", e);
            showToastConnectionError();
            return "";
        }
    }

    private boolean LoadBooleanValueFromDB(String sql) // Found-Status aus Datenbank auslesen
    {
        CoreCursor reader = Database.Data.rawQuery(sql, null);
        try {
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                if (reader.getInt(0) != 0) { // gefunden. Suche abbrechen
                    return true;
                }
                reader.moveToNext();
            }
        } finally {
            reader.close();
        }

        return false;
    }

    String ParseJsonResult(Search search, CB_List<Cache> cacheList, ArrayList<LogEntry> logList, ArrayList<ImageEntry> imageList, long gpxFilenameId, JSONObject json, byte apiStatus, boolean isLite) {
        String lastError = "";
        try {
            JSONObject status = json.getJSONObject("Status");
            if (status.getInt("StatusCode") == 0) {
                JSONArray caches = json.getJSONArray("Geocaches");
                // Log.debug(log, "got " + caches.length() + " Caches from gc");
                for (int i = 0; i < caches.length(); i++) {
                    JSONObject jCache = (JSONObject) caches.get(i);
                    String gcCode = jCache.getString("Code");
                    // Log.debug(log, "handling " + gcCode);
                    String name = jCache.getString("Name");
                    lastError += gcCode + " - " + name + "\n";

                    boolean CacheERROR = false;

                    Cache cache = new Cache(true);
                    cache.setArchived(jCache.getBoolean("Archived"));
                    cache.setAttributesPositive(new DLong(0, 0));
                    cache.setAttributesNegative(new DLong(0, 0));
                    JSONArray jAttributes = jCache.getJSONArray("Attributes");
                    for (int j = 0; j < jAttributes.length(); j++) {
                        JSONObject jAttribute = jAttributes.getJSONObject(j);
                        int AttributeTypeId = jAttribute.getInt("AttributeTypeID");
                        boolean isOn = jAttribute.getBoolean("IsOn");
                        Attributes att = Attributes.getAttributeEnumByGcComId(AttributeTypeId);
                        if (isOn) {
                            cache.addAttributePositive(att);
                        } else {
                            cache.addAttributeNegative(att);
                        }
                    }
                    cache.setAvailable(jCache.getBoolean("Available"));
                    cache.setDateHidden(new Date());
                    try {
                        String dateCreated = jCache.getString("DateCreated");
                        int date1 = dateCreated.indexOf("/Date(");
                        int date2 = dateCreated.lastIndexOf("-");
                        String date = (String) dateCreated.subSequence(date1 + 6, date2);
                        cache.setDateHidden(new Date(Long.valueOf(date)));
                    } catch (Exception exc) {
                        Log.err(log, "SearchForGeocaches_ParseDate", exc);
                    }
                    cache.setDifficulty((float) jCache.getDouble("Difficulty"));

                    // Ein evtl. in der Datenbank vorhandenen "Favorite" nicht überschreiben
                    boolean Favorite = LoadBooleanValueFromDB("select Favorit from Caches where GcCode = \"" + gcCode + "\"");
                    cache.setFavorite(Favorite);

                    // Ein evtl. in der Datenbank vorhandenen "Found" nicht überschreiben
                    boolean Found = LoadBooleanValueFromDB("select found from Caches where GcCode = \"" + gcCode + "\"");
                    if (!Found) {
                        cache.setFound(jCache.getBoolean("HasbeenFoundbyUser"));
                    } else {
                        cache.setFound(true);
                    }

                    cache.setGcCode(jCache.getString("Code"));
                    try {
                        cache.setGcId(jCache.getString("ID"));
                    } catch (Exception e) {
                        // CacheERROR = true; gibt bei jedem Cache ein
                        // Fehler ???
                    }
                    cache.setGPXFilename_ID(gpxFilenameId);

                    // Ein evtl. in der Datenbank vorhandenen "Found" nicht überschreiben
                    boolean userData = LoadBooleanValueFromDB("select HasUserData from Caches where GcCode = \"" + gcCode + "\"");
                    cache.setHasUserData(userData);

                    if (!isLite) {
                        try {
                            cache.setHint(jCache.getString("EncodedHints"));
                        } catch (Exception e1) {
                            cache.setHint("");
                        }
                    }

                    cache.Id = Cache.GenerateCacheId(cache.getGcCode());
                    cache.setListingChanged(false);
                    if (!isLite) {
                        try {
                            cache.setLongDescription(jCache.getString("LongDescription"));
                        } catch (Exception e1) {
                            Log.err(log, "SearchForGeocaches_LongDescription:" + cache.getGcCode(), e1);
                            cache.setLongDescription("");
                        }
                        if (!jCache.getBoolean("LongDescriptionIsHtml")) {
                            cache.setLongDescription(cache.getLongDescription().replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
                        }
                    }

                    cache.setName(jCache.getString("Name"));
                    cache.setTourName("");
                    cache.setNoteChecksum(0);
                    cache.NumTravelbugs = jCache.getInt("TrackableCount");
                    JSONObject jOwner = jCache.getJSONObject("Owner");
                    cache.setOwner(jOwner.getString("UserName"));
                    cache.setPlacedBy(cache.getOwner());
                    try {
                        cache.Pos = new CoordinateGPS(jCache.getDouble("Latitude"), jCache.getDouble("Longitude"));
                    } catch (Exception e) {

                    }
                    cache.Rating = 0;
                    if (!isLite) {
                        try {
                            cache.setShortDescription(jCache.getString("ShortDescription"));
                        } catch (Exception e) {
                            Log.err(log, "SearchForGeocaches_shortDescription:" + cache.getGcCode(), e);
                            cache.setShortDescription("");
                        }
                        if (!jCache.getBoolean("ShortDescriptionIsHtml")) {
                            cache.setShortDescription(cache.getShortDescription().replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
                        }
                    }

                    JSONObject jContainer = jCache.getJSONObject("ContainerType");
                    int jSize = jContainer.getInt("ContainerTypeId");
                    cache.Size = CacheSizes.CacheSizesFromInt(getCacheSize(jSize));
                    cache.setSolverChecksum(0);
                    cache.setTerrain((float) jCache.getDouble("Terrain"));
                    cache.Type = CacheTypes.Traditional;
                    try {
                        JSONObject jCacheType = jCache.getJSONObject("CacheType");
                        cache.Type = getCacheType(jCacheType.getInt("GeocacheTypeId"));
                    } catch (Exception e) {
                        if (gcCode.equals("GC4K089")) {
                            cache.Type = CacheTypes.Giga;
                        } else {
                            cache.Type = CacheTypes.Undefined;
                        }
                    }
                    cache.setUrl(jCache.getString("Url"));
                    cache.setApiStatus(apiStatus);

                    // Ein evtl. in der Datenbank vorhandenen "Favorit" nicht überschreiben
                    boolean fav = LoadBooleanValueFromDB("select favorit from Caches where GcCode = \"" + gcCode + "\"");
                    cache.setFavorite(fav);

                    // Chk if Own or Found
                    boolean exclude = false;
                    if (search.excludeFounds && cache.isFound())
                        exclude = true;
                    if (search.excludeHides && cache.getOwner().equalsIgnoreCase(CB_Core_Settings.GcLogin.getValue()))
                        exclude = true;
                    if (search.available && (cache.isArchived() || !cache.isAvailable()))
                        exclude = true;

                    if (!CacheERROR && !exclude) {
                        cacheList.add(cache);
                        // insert Logs
                        JSONArray logs = jCache.getJSONArray("GeocacheLogs");
                        for (int j = 0; j < logs.length(); j++) {
                            JSONObject jLogs = (JSONObject) logs.get(j);
                            JSONObject jFinder = (JSONObject) jLogs.get("Finder");
                            JSONObject jLogType = (JSONObject) jLogs.get("LogType");
                            LogEntry logEntry = new LogEntry();
                            logEntry.CacheId = cache.Id;
                            logEntry.Comment = jLogs.getString("LogText");
                            logEntry.Finder = jFinder.getString("UserName");
                            logEntry.Id = jLogs.getInt("ID");
                            logEntry.Timestamp = new Date();
                            try {
                                String dateCreated = jLogs.getString("VisitDate");
                                int date1 = dateCreated.indexOf("/Date(");
                                int date2 = dateCreated.indexOf("-");
                                String date = (String) dateCreated.subSequence(date1 + 6, date2);
                                logEntry.Timestamp = new Date(Long.valueOf(date));
                            } catch (Exception exc) {
                                Log.err(log, "API", "SearchForGeocaches_ParseLogDate", exc);
                            }
                            logEntry.Type = LogTypes.GC2CB_LogType(jLogType.getInt("WptLogTypeId"));
                            logList.add(logEntry);
                        }

                        // insert Images
                        int imageListSizeOrg = imageList.size();
                        JSONArray images = jCache.getJSONArray("Images");
                        for (int j = 0; j < images.length(); j++) {
                            JSONObject jImage = (JSONObject) images.get(j);

                            ImageEntry image = new ImageEntry();
                            image.CacheId = cache.Id;
                            image.GcCode = cache.getGcCode();
                            image.Name = jImage.getString("Name");
                            image.Description = jImage.getString("Description");
                            image.ImageUrl = jImage.getString("Url").replace("img.geocaching.com/gc/cache", "img.geocaching.com/cache");
                            // remove "/gc" to match the url used in the description

                            image.IsCacheImage = true;

                            imageList.add(image);
                        }
                        int imageListSizeGC = images.length();

                        // insert images from Cache description
                        LinkedList<String> allImages = null;
                        if (!search.isLite)
                            allImages = DescriptionImageGrabber.GetAllImages(cache);
                        int imageListSizeGrabbed = 0;

                        if (allImages != null && allImages.size() > 0) {
                            imageListSizeGrabbed = allImages.size();
                        }

                        while (allImages != null && allImages.size() > 0) {
                            String url;
                            url = allImages.poll();

                            boolean found = false;
                            for (ImageEntry im : imageList) {
                                if (im.ImageUrl.equalsIgnoreCase(url)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                ImageEntry image = new ImageEntry();

                                image.CacheId = cache.Id;
                                image.GcCode = cache.getGcCode();
                                image.Name = url.substring(url.lastIndexOf("/") + 1);
                                image.Description = "";
                                image.ImageUrl = url;
                                image.IsCacheImage = true;

                                imageList.add(image);
                            }

                        }
                        Log.debug(log, "Merged imageList has " + imageList.size() + " Entrys (" + imageListSizeOrg + "/" + imageListSizeGC + "/" + imageListSizeGrabbed + ")");

                        // insert Waypoints
                        JSONArray waypoints = jCache.getJSONArray("AdditionalWaypoints");
                        for (int j = 0; j < waypoints.length(); j++) {
                            JSONObject jWaypoints = (JSONObject) waypoints.get(j);
                            Waypoint waypoint = new Waypoint(true);
                            waypoint.CacheId = cache.Id;

                            try {
                                waypoint.Pos = new CoordinateGPS(jWaypoints.getDouble("Latitude"), jWaypoints.getDouble("Longitude"));
                            } catch (Exception ex) {
                                // no Coordinates -> Lat/Lon = 0/0
                                waypoint.Pos = new CoordinateGPS(0, 0);
                            }

                            waypoint.setTitle(jWaypoints.getString("Description"));
                            waypoint.setDescription(jWaypoints.getString("Comment"));
                            waypoint.Type = getCacheType(jWaypoints.getInt("WptTypeID"));
                            waypoint.setGcCode(jWaypoints.getString("Code"));
                            cache.waypoints.add(waypoint);
                        }
                        // User Waypoints - Corrected Coordinates of the Geocaching.com Website
                        JSONArray userWaypoints = jCache.getJSONArray("UserWaypoints");
                        for (int j = 0; j < userWaypoints.length(); j++) {
                            JSONObject jUserWaypoint = (JSONObject) userWaypoints.get(j);
                            boolean descriptionOverideInfo = false;
                            boolean correctedCoordinateFlag = false;

                             descriptionOverideInfo = jUserWaypoint.optString("Description","").equals("Coordinate Override");

                            try {
                                correctedCoordinateFlag = jUserWaypoint.getBoolean("IsCorrectedCoordinate");
                            } catch (JSONException e) {
                            }

                            if (!(descriptionOverideInfo || correctedCoordinateFlag)) {
                                continue; // only corrected Coordinate
                            }
                            Waypoint waypoint = new Waypoint(true);
                            waypoint.CacheId = cache.Id;
                            try {
                                waypoint.Pos = new CoordinateGPS(jUserWaypoint.getDouble("Latitude"), jUserWaypoint.getDouble("Longitude"));
                            } catch (Exception ex) {
                                // no Coordinates -> Lat/Lon = 0/0
                                waypoint.Pos = new CoordinateGPS(0, 0);
                            }

                            waypoint.setTitle("Corrected Coordinates (API)");
                            waypoint.setDescription("");
                            waypoint.Type = CacheTypes.Final;
                            waypoint.setGcCode("CO" + cache.getGcCode().substring(2, cache.getGcCode().length()));
                            cache.waypoints.add(waypoint);
                        }
                        // Spoiler aktualisieren
                        actualizeSpoilerOfActualCache(cache);
                    }

                    // Notes
                    Object note = jCache.get("GeocacheNote");
                    if ((note != null) && (note instanceof String)) {
                        String s = (String) note;
                        System.out.println(s);
                        cache.setTmpNote(s);
                    }

                }
                GroundspeakAPI.checkCacheStatus(json, isLite);
            } else {
                lastError = "StatusCode = " + status.getInt("StatusCode") + "\n";
                lastError += status.getString("StatusMessage") + "\n";
                lastError += status.getString("ExceptionDetails");
                Log.err(log, lastError);
            }

        } catch (Exception e) {
            Log.err(log, "SearchForGeocaches:ParserException: " + lastError, e);
        }
        return lastError;
    }

    private int getCacheSize(int containerTypeId) {
        switch (containerTypeId) {
            case 1:
                return 0; // Unknown
            case 2:
                return 1; // Micro
            case 3:
                return 3; // Regular
            case 4:
                return 4; // Large
            case 5:
                return 5; // Virtual
            case 6:
                return 0; // Other
            case 8:
                return 2;
            default:
                return 0;

        }
    }

    private CacheTypes getCacheType(int apiTyp) {
        switch (apiTyp) {
            case 2:
                return CacheTypes.Traditional;
            case 3:
                return CacheTypes.Multi;
            case 4:
                return CacheTypes.Virtual;
            case 5:
                return CacheTypes.Letterbox;
            case 6:
                return CacheTypes.Event;
            case 8:
                return CacheTypes.Mystery;
            case 9:
                return CacheTypes.Cache; // Project APE Cache???
            case 11:
                return CacheTypes.Camera;
            case 12:
                return CacheTypes.Cache; // Locationless (Reverse) Cache
            case 13:
                return CacheTypes.CITO; // Cache In Trash Out Event
            case 137:
                return CacheTypes.Earth;
            case 453:
                return CacheTypes.MegaEvent;
            case 452:
                return CacheTypes.ReferencePoint;
            case 1304:
                return CacheTypes.Cache; // GPS Adventures Exhibit
            case 1858:
                return CacheTypes.Wherigo;

            case 217:
                return CacheTypes.ParkingArea;
            case 220:
                return CacheTypes.Final;
            case 219:
                return CacheTypes.MultiStage;
            case 221:
                return CacheTypes.Trailhead;
            case 218:
                return CacheTypes.MultiQuestion;
            case 7005:
                return CacheTypes.Giga;

            default:
                return CacheTypes.Undefined;

        }
    }

    protected void showToastConnectionError() {
        // hier im Core nichts machen da hier keine UI vorhanden ist
    }

    protected void actualizeSpoilerOfActualCache(Cache cache) {
        // hier im Core nichts machen da hier keine UI vorhanden ist
    }

    private JSONObject writeExclusions(JSONObject request, SearchCoordinate searchC) throws JSONException
    {
        if (searchC.available) {
            JSONObject GeocacheExclusions = new JSONObject()
                    .put("Archived", false)
                    .put("Available", true);
            request.put("GeocacheExclusions", GeocacheExclusions);
        }
        JSONObject BookmarksExclude = new JSONObject().put("ExcludeIgnoreList", true);
        request.put("GeocacheExclusions", BookmarksExclude);
        return request;
    }

    public Cache LoadApiDetails(Cache aktCache, ICancel icancel) {

        Cache newCache = null;
        try {
            SearchGC search = new SearchGC(aktCache.getGcCode());

            CB_List<Cache> apiCaches = new CB_List<Cache>();
            ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
            ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
            SearchForGeocachesJSON(search, apiCaches, apiLogs, apiImages, aktCache.getGPXFilename_ID(), icancel);
            synchronized (Database.Data.Query) {
                if (apiCaches.size() == 1) {
                    Database.Data.beginTransaction();
                    newCache = apiCaches.get(0);
                    Database.Data.Query.remove(aktCache);
                    Database.Data.Query.add(newCache);
                    // newCache.MapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, newCache.Longitude());
                    // newCache.MapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, newCache.Latitude());

                    new CacheDAO().UpdateDatabase(newCache);

                    // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
                    newCache.setLongDescription("");

                    LogDAO logDAO = new LogDAO();
                    for (LogEntry apiLog : apiLogs) {
                        if (apiLog.CacheId != newCache.Id)
                            continue;
                        // Write Log to database

                        logDAO.WriteToDatabase(apiLog);
                    }

                    WaypointDAO waypointDAO = new WaypointDAO();
                    for (int i = 0, n = newCache.waypoints.size(); i < n; i++) {
                        Waypoint waypoint = newCache.waypoints.get(i);

                        boolean update = true;

                        // dont refresh wp if aktCache.wp is user changed
                        for (int j = 0, m = aktCache.waypoints.size(); j < m; j++) {
                            Waypoint wp = aktCache.waypoints.get(j);
                            if (wp.getGcCode().equalsIgnoreCase(waypoint.getGcCode())) {
                                if (wp.IsUserWaypoint)
                                    update = false;
                                break;
                            }
                        }

                        if (update)
                            waypointDAO.WriteToDatabase(waypoint, false);
                    }

                    ImageDAO imageDAO = new ImageDAO();
                    for (ImageEntry image : apiImages) {
                        if (image.CacheId != newCache.Id)
                            continue;
                        // Write Image to database

                        imageDAO.WriteToDatabase(image, false);
                    }

                    Database.Data.setTransactionSuccessful();
                    Database.Data.endTransaction();

                    Database.Data.GPXFilenameUpdateCacheCount();
                }
            }
        } catch (Exception ex) {
            Log.err(log, "Load CacheInfo by API", ex);
            return null;
        }

        return newCache;
    }

}
