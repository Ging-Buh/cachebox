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
import CB_Core.Types.*;
import CB_Locator.Coordinate;
import CB_Utils.Interfaces.ICancel;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Log.Log;
import CB_Utils.http.Response;
import CB_Utils.http.Webb;
import CB_Utils.http.WebbException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

// todo rework errorhandling (API 1 differs)
public class GroundspeakAPI {
    public static final int OK = 0;
    public static final int ERROR = -1;
    public static final int CONNECTION_TIMEOUT = -2;
    public static final int API_IS_UNAVAILABLE = -4;
    private static final String log = "GroundspeakAPI";
    private static final String LiteFields = "referenceCode,userData,name,difficulty,terrain,placedDate,geocacheType.id,geocacheSize.id,location,postedCoordinates,status,owner.username,ownerAlias";
    private static final String NotLiteFields = "hints,attributes,longDescription,shortDescription,additionalWaypoints,userWaypoints";
    public static String LastAPIError = "";
    public static int APIError;
    public static int CachesLeft = -1;
    public static boolean CacheStatusValid = false;
    public static int CurrentCacheCount = -1;
    public static int MaxCacheCount = -1;
    public static boolean CacheStatusLiteValid = false;
    private static int CachesLeftLite = -1;
    private static int CurrentCacheCountLite = -1;
    private static int MaxCacheCountLite = -1;
    private static boolean mDownloadLimitExceeded = false;
    private static MemberShipTypes membershipType = MemberShipTypes.Unknown;
    private static UserInfos me;

    /**
     * // Archived, Available and TrackableCount are updated
     * // restriction for nr of caches must be handled by caller
     *
     * @param caches
     * @param icancel
     * @return int as status  on ERROR the String LastAPIError is set
     */
    public static int fetchGeocacheStatus(ArrayList<Cache> caches, final ICancel icancel) {

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e1) {
        }

        if (invalidAccessToken()) return ERROR;

        try {
            JSONArray CacheCodes = new JSONArray();
            for (Cache cache : caches) {
                CacheCodes.put(cache.getGcCode());
            }

            JSONObject json = Webb.create()
                    .post(getUrl("GetGeocacheStatus?format=json"))
                    .body(new JSONObject()
                            .put("AccessToken", GetSettingsAccessToken())
                            .put("CacheCodes", CacheCodes)
                    )
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            JSONObject status = json.getJSONObject("Status");
            if (status.getInt("StatusCode") == 0) {
                JSONArray geocacheStatuses = json.getJSONArray("GeocacheStatuses");
                for (int ii = 0; ii < geocacheStatuses.length(); ii++) {
                    JSONObject jCache = (JSONObject) geocacheStatuses.get(ii);
                    Iterator<Cache> iterator = caches.iterator();
                    do {
                        Cache tmp = iterator.next();
                        if (jCache.getString("CacheCode").equals(tmp.getGcCode())) {
                            tmp.setArchived(jCache.getBoolean("Archived"));
                            tmp.setAvailable(jCache.getBoolean("Available"));
                            tmp.NumTravelbugs = jCache.getInt("TrackableCount");
                            // weitere Infos in diesem Json record
                            // CacheName (getString)
                            // CacheType (getDouble / getLong ?)
                            // Premium   (getBoolean)
                            break;
                        }
                    } while (iterator.hasNext());
                }
                LastAPIError = "";
                return OK;
            } else {
                LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
                LastAPIError += status.getString("StatusMessage") + "\n";
                LastAPIError += status.getString("ExceptionDetails");
                Log.err(log, "GetGeocacheStatus " + LastAPIError);
                return (ERROR);
            }
        } catch (Exception e) {
            LastAPIError = e.getLocalizedMessage();
            Log.err(log, "GetGeocacheStatus", e);
            return ERROR;
        }
    }

    public static int fetchGeocacheLogsByCache(Cache cache, ArrayList<LogEntry> logList, boolean all, cancelRunnable cancelRun) {
        // todo test all=true (but is not used (by CB_Action_LoadLogs, loads all))

        if (cache == null) return ERROR;
        if (invalidAccessToken()) return ERROR;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
        }

        LastAPIError = "";

        LinkedList<String> friendList = new LinkedList<>();
        if (!all) {
            for (String f : CB_Core_Settings.Friends.getValue().replace(" ", "").replace("|", ",").split("\\,")) {
                friendList.add(f.toLowerCase(Locale.US));
            }
        }

        int start = 0;
        int count = 30;

        while (!cancelRun.cancel() && (friendList.size() > 0 || all))
        // Schleife, solange bis entweder keine Logs mehr geladen werden oder bis alle Logs aller Finder geladen sind.
        {
            try {
                JSONObject json = Webb.create()
                        .get(getUrl("GetGeocacheLogsByCacheCode?format=json" + "&AccessToken=" + GetSettingsAccessToken() + "&CacheCode=" + cache.getGcCode() + "&StartIndex=" + start + "&MaxPerPage=" + count))
                        .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                        .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                        .ensureSuccess()
                        .asJsonObject()
                        .getBody();
                JSONObject status = json.getJSONObject("Status");
                int statusCode = status.optInt("StatusCode", ERROR);
                if (statusCode == 0) {
                    JSONArray geocacheLogs = json.getJSONArray("Logs");
                    for (int ii = 0; ii < geocacheLogs.length(); ii++) {
                        JSONObject theLog = geocacheLogs.getJSONObject(ii);
                        String finder = theLog.getJSONObject("Finder").optString("UserName", "");
                        if (!all) {
                            if (!friendList.contains(finder.toLowerCase(Locale.US))) {
                                continue;
                            }
                            friendList.remove(finder.toLowerCase(Locale.US));
                        }
                        LogEntry logEntry = new LogEntry();
                        logEntry.Id = theLog.optInt("ID", -1);
                        logEntry.Finder = finder;
                        logEntry.Type = LogTypes.GC2CB_LogType(theLog.getJSONObject("LogType").optInt("WptLogTypeId", 4)); // 4 = write note as default
                        logEntry.Comment = theLog.optString("LogText", "");
                        logEntry.CacheId = cache.Id;
                        logEntry.Timestamp = new Date();
                        try {
                            String dateCreated = theLog.optString("VisitDate", "");
                            int date1 = dateCreated.indexOf("/Date(");
                            int date2 = dateCreated.indexOf("-");
                            String date = (String) dateCreated.subSequence(date1 + 6, date2);
                            logEntry.Timestamp = new Date(Long.valueOf(date));
                        } catch (Exception exc) {
                            logEntry.Timestamp = new Date();
                            Log.err(log, "SearchForGeocaches_ParseLogDate", exc);
                        }
                        logList.add(logEntry);
                    }

                    if ((geocacheLogs.length() < count) || (friendList.size() == 0)) {
                        return 0; // alle Logs des Caches geladen oder alle gesuchten Finder gefunden
                    }
                } else {
                    LastAPIError = "StatusCode = " + statusCode + "\n";
                    LastAPIError += status.optString("StatusMessage", "") + "\n";
                    LastAPIError += status.optString("ExceptionDetails", "");
                    return (ERROR);
                }
            } catch (Exception e) {
                Log.err(log, "GetGeocacheLogsByCache", e);
                LastAPIError = e.getLocalizedMessage();
                return ERROR;
            }
            // die nächsten Logs laden
            start += count;
        }
        return (ERROR);
    }

    public static int fetchCacheLimits() {
        if (CachesLeft > -1) return OK;

        if (invalidAccessToken()) return ERROR;

        LastAPIError = "";
        // zum Abfragen der CacheLimits einfach nach einem Cache suchen, der nicht existiert: "GCZZZZZ".
        // dadurch wird der Zähler nicht erhöht, die Limits aber zurückgegeben.
        try {
            JSONObject json = Webb.create()
                    .post(getUrl("SearchForGeocaches?format=json"))
                    .body(new JSONObject()
                            .put("AccessToken", GetSettingsAccessToken())
                            .put("IsLight", false)
                            .put("StartIndex", 0)
                            .put("MaxPerPage", 1)
                            .put("GeocacheLogCount", 0)
                            .put("TrackableLogCount", 0)
                            .put("CacheCode", new JSONObject().put("CacheCodes", new JSONArray().put("GCZZZZZ")))
                    )
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            int status = checkCacheStatus(json, false);
            // hier keine Überprüfung des Status,
            // da dieser z.B. 118 (Überschreitung des Limits) sein kann,
            // aber der CacheStatus trotzdem drin ist.
            return status;
        } catch (Exception e) {
            LastAPIError = e.getLocalizedMessage();
            Log.err(log, "GetGeocacheStatus ConnectTimeoutException", e);
            return ERROR;
        }
    }

    static int checkCacheStatus(JSONObject json, boolean isLite) {
        LastAPIError = "";
        try {
            JSONObject cacheLimits = json.getJSONObject("CacheLimits");
            if (isLite) {
                CachesLeftLite = cacheLimits.getInt("CachesLeft");
                CurrentCacheCountLite = cacheLimits.getInt("CurrentCacheCount");
                MaxCacheCountLite = cacheLimits.getInt("MaxCacheCount");
                CacheStatusLiteValid = true;
            } else {
                CachesLeft = cacheLimits.getInt("CachesLeft");
                CurrentCacheCount = cacheLimits.getInt("CurrentCacheCount");
                MaxCacheCount = cacheLimits.getInt("MaxCacheCount");
                CacheStatusValid = true;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            LastAPIError = "API Error: " + e.getMessage();
            return -4;
        }
    }

    public static int GetPocketQueryList(ArrayList<PQ> list) {
        // if (list == null) new NullArgumentException("PQ List"); // wer macht son scheiß
        Log.info(log, "GetPocketQueryList");
        LastAPIError = "";
        if (!IsPremiumMember()) return OK; // leere Liste für Basic members
        try {
            JSONObject json = Webb.create()
                    .get(getUrl("GetPocketQueryList?AccessToken=" + UrlEncode(GetSettingsAccessToken()) + "&format=json"))
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            JSONObject status = json.getJSONObject("Status");
            if (status.getInt("StatusCode") == 0) {
                LastAPIError = "";
                JSONArray jPQs = json.getJSONArray("PocketQueryList");

                for (int ii = 0; ii < jPQs.length(); ii++) {

                    JSONObject jPQ = (JSONObject) jPQs.get(ii);

                    if (jPQ.getBoolean("IsDownloadAvailable")) {
                        PQ pq = new PQ();
                        pq.Name = jPQ.getString("Name");
                        pq.GUID = jPQ.getString("GUID");
                        pq.DateLastGenerated = new Date();
                        try {
                            String dateCreated = jPQ.getString("DateLastGenerated");
                            int date1 = dateCreated.indexOf("/Date(");
                            int date2 = dateCreated.indexOf("-");
                            String date = (String) dateCreated.subSequence(date1 + 6, date2);
                            pq.DateLastGenerated = new Date(Long.valueOf(date));
                        } catch (Exception exc) {
                            Log.err(log, "API", "SearchForGeocaches_ParseDate", exc);
                        }
                        pq.PQCount = jPQ.getInt("PQCount");
                        int Byte = jPQ.getInt("FileSizeInBytes");
                        pq.SizeMB = Byte / 1048576.0;
                        list.add(pq);
                    }
                }
                return OK;
            } else {
                LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
                LastAPIError += status.getString("StatusMessage") + "\n";
                LastAPIError += status.getString("ExceptionDetails");
                Log.err(log, "GetPocketQueryList " + LastAPIError);
                return (ERROR);
            }
        } catch (Exception e) {
            LastAPIError = e.getLocalizedMessage();
            Log.err(log, "GetPocketQueryList" + LastAPIError);
            return ERROR;
        }
    }

    public static int DownloadSinglePocketQuery(PQ pocketQuery, String PqFolder) {
        // Replacement new API: "lists/" + pocketQuery.GUID + "/geocaches"

        try {
            JSONObject json = Webb.create()
                    .get(getUrl("GetPocketQueryZippedFile?format=json&AccessToken=" + UrlEncode(GetSettingsAccessToken()) + "&PocketQueryGuid=" + pocketQuery.GUID))
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            JSONObject status = json.getJSONObject("Status");
            if (status.optInt("StatusCode", 0) == 0) {
                LastAPIError = "";
                String dateString = new SimpleDateFormat("yyyyMMddHHmmss").format(pocketQuery.DateLastGenerated);
                String zipData = json.optString("ZippedFile", "");

                String local = PqFolder + "/" + pocketQuery.Name + "_" + dateString + ".zip";
                FileOutputStream fs;
                fs = new FileOutputStream(local);
                BufferedOutputStream bfs = new BufferedOutputStream(fs);
                try {
                    CB_Utils.Converter.Base64.decodeToStream(zipData, 0, zipData.length(), bfs);
                } catch (Exception ex) {
                    LastAPIError = ex.getLocalizedMessage();
                    Log.err(log, "DownloadSinglePocketQuery CB_Utils.Converter.Base64.decodeToStream " + LastAPIError);
                }
                bfs.flush();
                bfs.close();
                fs.close();
                if (LastAPIError.length() > 0) return ERROR;
                else return OK;
            } else {
                LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
                LastAPIError += status.getString("StatusMessage") + "\n";
                LastAPIError += status.getString("ExceptionDetails");
                Log.err(log, "DownloadSinglePocketQuery " + LastAPIError);
                return ERROR;
            }

        } catch (Exception e) {
            Log.err(log, "DownloadSinglePocketQuery", e);
            return ERROR;
        }
    }

    // End Old API

    // todo add/handle geocacheLimits
    // /users/UserName?fields=...
    public static UserInfos fetchUserInfos(String UserCode) {
        Log.info(log, "fetchUserInfos for " + UserCode);
        LastAPIError = "";
        UserInfos ui = new UserInfos();
        try {
            JSONObject response = Webb.create()
                    .get(getUrl(1, "/users/" + UserCode + "?fields=username,membershipLevelId,findCount"))
                    .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            ui.username = response.optString("username", "");
            ui.memberShipType = MemberShipTypesFromInt(response.optInt("membershipLevelId", -1));
            ui.findCount = response.optInt("findCount", -1);
            Log.info(log, "fetchUserInfos done \n" + response.toString());
        } catch (Exception ex) {
            LastAPIError = ex.getLocalizedMessage();
            if (ex instanceof WebbException) {
                WebbException we = (WebbException) ex;
                Response re = we.getResponse();
                if (re != null) {
                    JSONObject ej;
                    APIError = re.getStatusCode();
                    try {
                        ej = (JSONObject) re.getErrorBody();
                        if (ej != null) {
                            LastAPIError = ej.optString("errorMessage", "" + APIError);
                        } else {
                            LastAPIError = ex.getLocalizedMessage();
                        }
                    } catch (Exception exc) {
                        LastAPIError = ex.getLocalizedMessage();
                    }
                }
            }
            Log.err(log, "fetchUserInfos:" + APIError + ":" + LastAPIError);
            Log.trace(log, ex);
            ui.username = "";
            ui.memberShipType = MemberShipTypes.Unknown;
            ui.findCount = 0;
        }
        return ui;
    }

    // UploadFieldNotes | UploadDrafts | logdrafts | geocachelogs
    public static int UploadDraftOrLog(String cacheCode, int wptLogTypeId, Date dateLogged, String note, boolean directLog) {
        Log.info(log, "UploadDraftOrLog");

        if (invalidAccessToken()) return ERROR;

        try {
            if (directLog) {
                Log.debug(log, "is Log");
                /*
                GEOCACHELOG Fields
                Name	Type	Description	Required for Creation	Can Be Updated (By Log Owner)
                =================================================================================
                referenceCode	string	uniquely identifies the geocache	No	No
                ownerCode	string	identifier of the log owner	            No	No
                imageCount	integer	number of images associated with geocache log	No	No
                loggedDate	datetime	date and time of when user logged the geocache in the timezone of the geocache	Yes	Yes
                text	string	display text of the geocache log	Yes	Yes
                type	string or integer	name or id of the geocache log type (see Geocache Log Types for more info)	Yes	Yes
                updatedCoordinates	Coordinates	latitude and longitude of the geocache (only used with log type 47 - Update Coordinates)	Optional	Yes
                geocacheCode	string	identifier of the associated geocache	Yes	No
                usedFavoritePoint	bool	if a favorite point was awarded from this log	Optional, defaults to false	No
                isEncoded	bool	if log was encrypted using ROT13	No	No
                isArchived	bool	if the log has been deleted	No	No
                 */
                Webb.create()
                        .post(getUrl(1, "geocachelogs"))
                        .body(new JSONObject()
                                .put("geocacheCode", cacheCode)
                                .put("type", wptLogTypeId)
                                .put("loggedDate", getUTCDate(dateLogged))
                                .put("text", prepareNote(note))
                        )
                        .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                        .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                        .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                        .ensureSuccess()
                        .asVoid();
            } else {
                Log.debug(log, "is draft");
                /*
                LOGDRAFT Fields
                Name        	    Type	            Description 	                                                            Required for Creation   Can Be Updated (By Draft Owner)
                guid missing in description of GC                                                                                       Yes                 No
                referenceCode	    string	            uniquely identifies the log draft	                                            No	                No
                geocacheCode	    string	            identifer of the geocache	                                                    Yes	                No
                logType	            string or integer	name or id of the geocache log type (see Geocache Log Types for more info)	    Yes	                No
                note	            string	            display text of the log draft	                                                Optional	        Yes
                dateLoggedUtc	    datetime	        when the user logged the geocache in UTC	                                    Optional, defaults to current datetime	No
                imageCount	        integer	            number of images associated with draft	                                        No	                No
                useFavoritePoint	boolean	whether to award favorite point when	                                                Optional, defaults to false	Yes
                */
                Webb.create()
                        .post(getUrl(1, "logdrafts"))
                        .body(new JSONObject()
                                .put("guid", UUID.randomUUID().toString())
                                .put("geocacheCode", cacheCode)
                                .put("logType", wptLogTypeId)
                                .put("dateLoggedUtc", getUTCDate(dateLogged))
                                .put("note", prepareNote(note))
                        )
                        .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                        .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                        .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                        .ensureSuccess()
                        .asVoid();
            }
            LastAPIError = "";
            Log.info(log, "UploadDraftOrLog done");
            return OK;
        } catch (Exception e) {
            LastAPIError = e.getLocalizedMessage();
            Log.err(log, "UploadDraftOrLog", e);
            return ERROR;
        }
    }

    // geocaches/{referenceCode}/geocachelogs
    // there is no LogId in the new API
    public static int fetchGeocacheLogsOfFriends(Cache cache, ArrayList<LogEntry> logList, cancelRunnable cancelRun) {
        if (cache == null) return ERROR;
        if (invalidAccessToken()) return ERROR;
        Map<String, String> friends = new HashMap<String, String>();
        // todo perhaps entered more friends than allowed by limit (The max amount of usernames allowed is 50)
        try {
            JSONArray userCodes = Webb.create()
                    .get(getUrl(1, "users?" + "usernames=" + CB_Core_Settings.Friends.getValue().replace(" ", "").replace("|", ",") + "&fields=referenceCode,username"))
                    .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonArray()
                    .getBody();
            for (int ii = 0; ii < userCodes.length(); ii++) {
                String friendCode = ((JSONObject) userCodes.get(ii)).optString("referenceCode", "");
                String friendName = ((JSONObject) userCodes.get(ii)).optString("username", "");
                if (friendCode.length() > 0)
                    if (!friends.keySet().contains(friendCode))
                        friends.put(friendCode, friendName);
            }
        } catch (Exception ex) {
            return ERROR;
        }

        int start = 0;
        int count = 30;

        while (!cancelRun.cancel() && (friends.size() > 0))
        // Schleife, solange bis entweder keine Logs mehr geladen werden oder bis Logs aller Freunde geladen sind.
        {
            try {
                JSONArray geocacheLogs = Webb.create()
                        .get(getUrl(1, "geocaches/" + cache.getGcCode() + "/geocachelogs?skip=" + start + "&take=" + count + "&fields=ownerCode,loggedDate,text,type,referenceCode"))
                        .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                        .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                        .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                        .ensureSuccess()
                        .asJsonArray()
                        .getBody();
                for (int ii = 0; ii < geocacheLogs.length(); ii++) {
                    JSONObject jLogs = (JSONObject) geocacheLogs.get(ii);
                    String ownerCode = jLogs.optString("ownerCode", "");
                    if (ownerCode.length() == 0 || !friends.keySet().contains(ownerCode)) {
                        continue;
                    }
                    LogEntry logEntry = new LogEntry();
                    logEntry.CacheId = cache.Id;
                    logEntry.Comment = jLogs.optString("text", "");
                    logEntry.Finder = friends.get(ownerCode);
                    String dateCreated = jLogs.optString("loggedDate", "");
                    try {
                        logEntry.Timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateCreated);
                    } catch (Exception e) {
                        logEntry.Timestamp = new Date();
                    }
                    // todo translate all texts to enum
                    logEntry.Type = LogTypes.parseString(jLogs.optString("type", ""));
                    // todo logEntry.Id = jLogs.getInt("ID"); change database?
                    // referenceCode
                    logList.add(logEntry);

                    friends.remove(ownerCode);
                }

                if ((geocacheLogs.length() < count) || (friends.size() == 0)) {
                    return OK; // alle Logs des Caches geladen oder alle Freunde bearbeitet
                }

            } catch (Exception e) {
                Log.err(log, "fetchGeocacheLogsOfFriends", e);
                return ERROR;
            }
            // die nächsten Logs laden
            start += count;
        }
        return (ERROR);
    }

    public static int downloadImageListForGeocache(String cacheCode, HashMap<String, URI> list) {
        Log.debug(log, "downloadImageListForGeocache for '" + "cacheCode" + "'");
        LastAPIError = "";
        if (cacheCode == null) return ERROR;
        if (invalidAccessToken()) return ERROR;
        if (list == null)
            list = new HashMap<>();
        int skip = 0;
        int take = 50;

        // todo Schleife (es werden nur 50 geholt (was reicht))
        // other fields ,referenceCode,createdDate,guid
        try {
            Log.debug(log, "Webb.create for " + getUrl(1, String.format("geocaches/%s/images?skip=%d&take=%d&fields=url,description", cacheCode, skip, take)));
            JSONArray jImages = Webb.create()
                    .get(getUrl(1, String.format("geocaches/%s/images?skip=%d&take=%d&fields=url,description", cacheCode, skip, take)))
                    .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonArray()
                    .getBody();
            try {
                Log.debug(log, "Anz.: " + jImages.length());
                for (int ii = 0; ii < jImages.length(); ii++) {
                    try {
                        JSONObject jImage = (JSONObject) jImages.get(ii);
                        String name = jImage.getString("description");
                        String uri = jImage.getString("url");
                        Log.trace(log, "downloadImageListForGeocache getImageObject Nr.:" + ii + " '" + name + "' " + uri);
                        // ignore log images (in dieser API kommen keine Logbilder mehr)
                        if (uri.contains("/cache/log")) {
                            Log.trace(log, "downloadImageListForGeocache getImageObject Nr.:" + ii + " ignored.");
                            continue; // LOG-Image
                        }
                        // Check for duplicate name
                        if (list.containsKey(name)) {
                            for (int nr = 1; nr < 10; nr++) {
                                if (list.containsKey(name + "_" + nr)) {
                                    Log.debug(log, "downloadImageListForGeocache getImageObject Nr.:" + ii + " ignored: ");
                                    continue; // Name already exists --> next nr
                                }
                                name += "_" + nr;
                                break;
                            }
                        }
                        list.put(name, new URI(uri));
                    } catch (Exception ex) {
                        Log.err(log, "downloadImageListForGeocache getImageObject Nr.:" + ii, ex);
                    }
                }
            } catch (Exception ex) {
                LastAPIError = ex.getLocalizedMessage();
                Log.err(log, "downloadImageListForGeocache getJSONArray(\"Images\") ", ex);
                return ERROR;
            }

            Log.info(log, "downloadImageListForGeocache done");
            return OK;
        } catch (Exception ex) {
            LastAPIError += ex.getLocalizedMessage();
            LastAPIError += "\n for " + getUrl(1, "geocaches/" + cacheCode + "/images?fields=url,description");
            LastAPIError += "\n APIKey: " + GetSettingsAccessToken();
            Log.err(log, "downloadImageListForGeocache \n" + LastAPIError, ex);
            return ERROR;
        }
    }

    public static TbList downloadUsersTrackables() {
        Log.info(log, "downloadUsersTrackables");
        if (invalidAccessToken()) return null;
        LastAPIError = "";
        try {
            JSONArray jTrackables = Webb.create()
                    .get(getUrl(1, "trackables" + "?fields=referenceCode,trackingNumber,iconUrl,name,goal,description,releasedDate,ownerCode,holderCode,currentGeocacheCode,type,inHolderCollection"))
                    .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonArray()
                    .getBody();
            TbList list = new TbList();
            for (int ii = 0; ii < jTrackables.length(); ii++) {
                JSONObject jTrackable = (JSONObject) jTrackables.get(ii);
                if (!jTrackable.optBoolean("inHolderCollection", false)) {
                    Trackable tb = createTrackable(jTrackable);
                    Log.debug(log, "downloadUsersTrackables: add " + tb.getName());
                    list.add(tb);
                } else {
                    Log.debug(log, "downloadUsersTrackables: not in HolderCollection" + jTrackable.optString("name", ""));
                }
            }

            Log.info(log, "downloadUsersTrackables done \n" + jTrackables.toString());
            return list;
        } catch (Exception ex) {
            LastAPIError = ex.getLocalizedMessage();
            Log.err(log, "downloadUsersTrackables", ex);
            return null;
        }
    }

    public static Trackable fetchTrackable(String TBCode) {
        Log.info(log, "fetchTrackable for " + TBCode);
        LastAPIError = "";
        APIError = 0;
        if (invalidAccessToken()) return null;
        try {
            Trackable tb = createTrackable(Webb.create()
                    .get(getUrl(1, "trackables/" + TBCode + "?fields=referenceCode,trackingNumber,iconUrl,name,goal,description,releasedDate,ownerCode,holderCode,currentGeocacheCode,type"))
                    .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody()
            );
            if (!tb.TBCode.toLowerCase().equals(TBCode.toLowerCase())) {
                // fetched by TrackingCode, the result for trackingcode is always empty, except for owner
                tb.TrackingCode = TBCode;
            }
            return tb;
        } catch (Exception ex) {
            if (ex instanceof WebbException) {
                WebbException we = (WebbException) ex;
                APIError = we.getResponse().getStatusCode();
                JSONObject ej = (JSONObject) we.getResponse().getErrorBody();
                LastAPIError = ej.optString("errorMessage", "" + APIError);
            } else {
                LastAPIError = ex.getLocalizedMessage();
            }
            Log.err(log, "fetchTrackable \n"
                            + LastAPIError
                            + "\n for " + getUrl(1, "trackables/" + TBCode + "?fields=url,description")
                    , ex);
            return null;
        }
    }

    public static boolean uploadTrackableLog(Trackable TB, String cacheCode, int LogTypeId, Date dateLogged, String note) {
        return uploadTrackableLog(TB.getTBCode(), TB.getTrackingCode(), cacheCode, LogTypeId, dateLogged, note);
    }

    // "trackablelogs" CREATE TRACKABLE LOG
    public static boolean uploadTrackableLog(String TBCode, String TrackingNummer, String cacheCode, int LogTypeId, Date dateLogged, String note) {
        Log.info(log, "uploadTrackableLog");
        LastAPIError = "";
        if (cacheCode == null) cacheCode = "";
        if (invalidAccessToken()) return false;
        try {
            JSONObject result = Webb.create()
                    .post(getUrl(1, "trackablelogs"))
                    .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                    .body(new JSONObject()
                            .put("trackingNumber", TrackingNummer) // code only found on the trackable itself (only needed for creating a log)
                            .put("trackableCode", TBCode) // identifier of the related trackable, required for creation
                            .put("geocacheCode", cacheCode)
                            .put("loggedDate", getUTCDate(dateLogged))
                            .put("text", prepareNote(note))
                            .put("typeId", LogTypeId) // see Trackable Log Types https://api.groundspeak.com/documentation#trackable-log-types
                    )
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            LastAPIError += "\n for " + getUrl(1, "trackablelogs");
            LastAPIError += "\n APIKey: " + GetSettingsAccessToken();
            LastAPIError += "\n trackingNumber: " + TrackingNummer;
            LastAPIError += "\n trackableCode: " + TBCode;
            LastAPIError += "\n geocacheCode: " + cacheCode;
            LastAPIError += "\n loggedDate: " + getUTCDate(dateLogged);
            LastAPIError += "\n text: " + prepareNote(note);
            LastAPIError += "\n typeId: " + LogTypeId;
            Log.info(log, "uploadTrackableLog done\n" + LastAPIError + result.toString());
            LastAPIError = "";
            return true;
        } catch (Exception ex) {
            LastAPIError += ex.getLocalizedMessage();
            LastAPIError += "\n for " + getUrl(1, "trackablelogs");
            LastAPIError += "\n APIKey: " + GetSettingsAccessToken();
            LastAPIError += "\n trackingNumber: " + TrackingNummer;
            LastAPIError += "\n trackableCode: " + TBCode;
            LastAPIError += "\n geocacheCode: " + cacheCode;
            LastAPIError += "\n loggedDate: " + getUTCDate(dateLogged);
            LastAPIError += "\n text: " + prepareNote(note);
            LastAPIError += "\n typeId: " + LogTypeId;
            Log.err(log, "CreateTrackableLog \n" + LastAPIError, ex);
            return false;
        }
    }

    // getUTCDate with uppercase G is old Live API
    private static String getUTCDate(Date date) {
        // check "2001-09-28T00:00:00"
        Log.debug(log, "getUTCDate In:" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date));
        long utc = date.getTime();
        TimeZone tzp = TimeZone.getTimeZone("GMT");
        utc = utc - tzp.getOffset(utc);
        date.setTime(utc);
        String ret = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date);
        Log.debug(log, "getUTCDate Out:" + ret);
        return ret;
    }

    public static int uploadCacheNote(String cacheCode, String notes) {
        Log.info(log, "uploadCacheNote for " + cacheCode);
        LastAPIError = "";
        if (cacheCode == null || cacheCode.length() == 0) return ERROR;
        if (!IsPremiumMember()) return ERROR;
        try {
            Webb.create()
                    .put(getUrl(1, "geocaches/" + cacheCode + "/notes"))
                    .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                    .body(new JSONObject().put("note", prepareNote(notes)))
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asVoid();
            Log.info(log, "uploadCacheNote done");
            return OK;
        } catch (Exception ex) {
            LastAPIError = ex.getLocalizedMessage();
            LastAPIError += "\n for " + getUrl(1, "geocaches/" + cacheCode + "/notes");
            LastAPIError += "\n APIKey: " + GetSettingsAccessToken();
            LastAPIError += "\n geocacheCode: " + cacheCode;
            LastAPIError += "\n note: " + prepareNote(notes) + "\n";
            LastAPIError += ((WebbException) ex).getResponse().getErrorBody().toString();
            Log.err(log, "UpdateCacheNote \n" + LastAPIError, ex);
            return ERROR;
        }
    }

    private static String prepareNote(String note) {
        return note.replace("\r", "");
    }

    public static UserInfos fetchMyUserInfos() {
        if (me == null || me.memberShipType == MemberShipTypes.Unknown) {
            me = fetchUserInfos("me");
            if (me.memberShipType == MemberShipTypes.Unknown) {
                // we need a new AccessToken
                // API_ErrorEventHandlerList.handleApiKeyError(API_ErrorEventHandlerList.API_ERROR.INVALID);
                Log.err(log, "fetchMyUserInfos: Need a new Access Token");
            }
        }
        return me;
    }

    public static int GetPocketQueryListAPI1(ArrayList<PQ> list) {
        // if (list == null) new NullArgumentException("PQ List"); // wer macht son scheiß
        Log.info(log, "GetPocketQueryList");
        LastAPIError = "";
        if (!IsPremiumMember()) return OK; // leere Liste für Basic members
        int skip = 0;
        int take = 50;
        /*
        referenceCode	string	uniquely identifies the list	No	No
        createdDateUtc	datetime	when the list was created in UTC	No	No
        lastUpdatedDateUtc	datetime	when the list was last updated in UTC. for pocket queries, this represents the last time the query was generated	No	No
        name	string	display name of the list	Yes	Yes
        count	integer	how many geocaches are in the list	No	No
        findCount	integer	how many of the geocaches in list are found	No	No
        ownerCode	string	identifier of the user who owns the list	No	No
        description	string	text about the list	No	Yes
        typeId	integer	type of the list (see List Types for more info)	Yes	No
        isShared	bool	if the list is accessible through a direct link	Yes	Yes
        isPublic	bool	if the list is accessible to everyone without a direct link	Yes	Yes
        */
        String fields = "name,referenceCode,lastUpdatedDateUtc";
        try {
            JSONArray json;
            do {
                json = Webb.create()
                        .get(getUrl(1, "users/me/lists?types=pq&skip=" + skip + "&take=" + take + "&fields=" + fields))
                        .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                        .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                        .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                        .ensureSuccess()
                        .asJsonArray()
                        .getBody();
                skip = skip + take;
                for (int ii = 0; ii < json.length(); ii++) {
                    JSONObject jPQ = (JSONObject) json.get(ii);
                    PQ pq = new PQ();
                    pq.Name = jPQ.optString("Name", "");
                    pq.GUID = jPQ.optString("referenceCode", "");
                    try {
                        String dateCreated = jPQ.optString("lastUpdatedDateUtc", "");
                        pq.DateLastGenerated = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(dateCreated);
                    } catch (Exception exc) {
                        Log.err(log, "GetPocketQueryList", "DateLastGenerated", exc);
                        pq.DateLastGenerated = new Date();
                    }
                    pq.PQCount = jPQ.getInt("count");
                    pq.SizeMB = -1;
                    pq.downloadAvailable = true;
                    list.add(pq);
                }
            }
            while (json.length() == take);

            Log.info(log, "GetPocketQueryList done \n" + json.toString());
            return OK;
        } catch (Exception ex) {
            LastAPIError = ex.getLocalizedMessage();
            Log.err(log, "GetPocketQueryList", ex);
            return ERROR;
        }
    }

    // "friends?fields=referenceCode" only for test API 1.0
    public static int getFriends() {
        Log.info(log, "getFriends");
        LastAPIError = "";
        if (!IsPremiumMember()) return ERROR;
        try {
            JSONArray response = Webb.create()
                    .get(getUrl(1, "friends?fields=referenceCode"))
                    .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonArray()
                    .getBody();
            Log.info(log, "getFriends done \n" + response.toString());
            return OK;
        } catch (Exception ex) {
            LastAPIError = ex.getLocalizedMessage();
            Log.err(log, "getFriends", ex);
            return ERROR;
        }
    }

    public static boolean invalidAccessToken() {
        return (fetchMyUserInfos().memberShipType == MemberShipTypes.Unknown);
    }

    public static boolean IsPremiumMember() {
        return fetchMyUserInfos().memberShipType == MemberShipTypes.Premium;
    }

    static String GetSettingsAccessToken() {
        /* */
        String act;
        if (CB_Core_Settings.UseTestUrl.getValue()) {
            act = CB_Core_Settings.AccessTokenForTest.getValue();
        } else {
            act = CB_Core_Settings.AccessToken.getValue();
        }

        // for ACB we added an additional A in settings
        if ((act.startsWith("A"))) {
            Log.trace(log, "Access Token = " + act.substring(1, act.length()));
            return act.substring(1);
        } else
            Log.err(log, "no Access Token");
        return "";
        /* */
    }

    static String UrlEncode(String value) {
        return value.replace("/", "%2F")
                .replace("\\", "%5C")
                .replace("+", "%2B")
                .replace("=", "%3D");
    }

    public static boolean isDownloadLimitExceeded() {
        return mDownloadLimitExceeded;
    }

    public static void setDownloadLimitExceeded() {
        mDownloadLimitExceeded = true;
    }

    static String getUrl(String command) {
        return getUrl(0, command);
    }

    static String getUrl(int version, String command) {
        String ApiUrl = "https://api.groundspeak.com/";
        String StagingApiUrl = "https://staging.api.groundspeak.com/";
        String mPath;
        if (version == 0) {
            mPath = "LiveV6/geocaching.svc/";
        } else {
            mPath = "v1.0/";
        }
        String url;
        if (CB_Core_Settings.UseTestUrl.getValue()) {
            url = StagingApiUrl + mPath;
        } else {
            url = ApiUrl + mPath;
        }
        return url + command;
    }

    private static MemberShipTypes MemberShipTypesFromInt(int value) {
        switch (value) {
            case 1:
                return MemberShipTypes.Basic;
            case 2:
                return MemberShipTypes.Charter;
            case 3:
                return MemberShipTypes.Premium;
            default:
                return MemberShipTypes.Unknown;
        }
    }

    private static Trackable createTrackable(JSONObject API1Trackable) {
        // see https://api.groundspeak.com/documentation#trackable
        try {
            Trackable tb = new Trackable();
            Log.debug(log, API1Trackable.toString());
            tb.Archived = false;
            tb.TBCode = API1Trackable.optString("referenceCode", "");
            // trackingNumber	string	unique number used to prove discovery of trackable. only returned if user matches the holderCode
            // will not be stored (Why)
            tb.TrackingCode = API1Trackable.optString("trackingNumber", "");
            tb.CurrentGeocacheCode = API1Trackable.optString("currentGeocacheCode", "");
            if (tb.CurrentGeocacheCode.equals("null")) tb.CurrentGeocacheCode = "";
            tb.CurrentGoal = CB_Utils.StringH.JsoupParse(API1Trackable.optString("goal"));
            String holderCode = API1Trackable.optString("holderCode", "");
            if (holderCode.length() > 0) {
                tb.CurrentOwnerName = fetchUserInfos(holderCode).username;
            } else {
                tb.CurrentOwnerName = "";
            }
            String releasedDate = API1Trackable.optString("releasedDate", "");
            try {
                tb.DateCreated = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(releasedDate);
            } catch (Exception e) {
                tb.DateCreated = new Date();
            }
            tb.Description = CB_Utils.StringH.JsoupParse(API1Trackable.optString("description", ""));
            tb.IconUrl = API1Trackable.optString("iconUrl", "");
            if (tb.IconUrl.startsWith("http:")) {
                tb.IconUrl = "https:" + tb.getIconUrl().substring(5);
            }
            tb.Name = API1Trackable.optString("name", "");
            tb.OwnerName = fetchUserInfos(API1Trackable.optString("ownerCode", "")).username;
            tb.TypeName = API1Trackable.optString("type", "");
            return tb;
        } catch (Exception e) {
            Log.err(log, "createTrackable(JSONObject API1Trackable)", e);
            return null;
        }
    }

    private static Cache createCache(JSONObject API1Cache, String fields) {
        // see https://api.groundspeak.com/documentation#geocache
        // see https://api.groundspeak.com/documentation#lite-geocache
        Cache cache = new Cache(true);
        cache.setAttributesPositive(new DLong(0, 0));
        cache.setAttributesNegative(new DLong(0, 0));
        try {
            for (String field : fields.split(",")) {
                switch (field) {
                    case "referenceCode":
                        cache.setGcCode(API1Cache.optString(field));
                        if (cache.getGcCode().length() == 0) {
                            Log.err(log, "Get no GCCode");
                            return null;
                        }
                        cache.setUrl("https://coord.info/" + cache.getGcCode());
                        cache.Id = Cache.GenerateCacheId(cache.getGcCode());
                        break;
                    case "name":
                        cache.setName(API1Cache.optString(field, ""));
                        break;
                    case "difficulty":
                        cache.setDifficulty((float) API1Cache.optDouble(field, 1));
                        break;
                    case "terrain":
                        cache.setTerrain((float) API1Cache.optDouble(field, 1));
                        break;
                    case "favoritePoints":
                        break;
                    case "trackableCount":
                        cache.NumTravelbugs = API1Cache.optInt(field, 0);
                        break;
                    case "placedDate":
                        cache.setDateHidden(DateFromString(API1Cache.optString("placedDate", "")));
                        break;
                    case "geocacheType.id":
                        cache.Type = CacheTypeFromID(API1Cache.getJSONObject("geocacheType").optInt("id", 0));
                        break;
                    case "geocacheSize.id":
                        cache.Size = CacheSizeFromID(API1Cache.getJSONObject("geocacheSize").optInt("id", 0));
                        break;
                    case "location":
                        JSONObject location = API1Cache.getJSONObject("location");
                        cache.setCountry(location.optString("countryName", ""));
                        cache.setState(location.optString("stateName", ""));
                        break;
                    case "status":
                        String status = API1Cache.optString("status", "");
                        if (status.equals("Archived")) {
                            cache.setArchived(true);
                            cache.setAvailable(false);
                        } else if (status.equals("Disabled")) {
                            cache.setArchived(false);
                            cache.setAvailable(false);
                        } else if (status.equals("Unpublished")) {
                            cache.setArchived(false);
                            cache.setAvailable(false);
                        } else {
                            // Active, Locked
                            cache.setArchived(false);
                            cache.setAvailable(true);
                        }
                        break;
                    case "owner.username":
                        JSONObject ownerData = API1Cache.optJSONObject("owner");
                        if (ownerData != null)
                            cache.setOwner(ownerData.optString("username", ""));
                        break;
                    case "ownerAlias":
                        cache.setPlacedBy(API1Cache.optString("ownerAlias", ""));
                        break;
                    case "userData":
                        JSONObject userData = API1Cache.optJSONObject("userData");
                        if (userData != null) {
                            // foundDate
                            cache.setFound(userData.optString("foundDate", "").length() != 0);
                            // correctedCoordinates
                            JSONObject correctedCoordinates = userData.optJSONObject("correctedCoordinates ");
                            if (correctedCoordinates != null) {
                                cache.Pos = new Coordinate(correctedCoordinates.optDouble("latitude", 0), correctedCoordinates.optDouble("longitude", 0));
                            } else {
                                JSONObject postedCoordinates = API1Cache.optJSONObject("postedCoordinates");
                                if (postedCoordinates != null) {
                                    cache.Pos = new Coordinate(postedCoordinates.optDouble("latitude", 0), postedCoordinates.optDouble("longitude", 0));
                                } else {
                                    cache.Pos = new Coordinate();
                                }
                            }
                            // isFavorited
                            // note (auch bei lite)
                            cache.setTmpNote(userData.optString("note ", ""));
                            // todo split solver from notes
                        } else {
                            cache.setFound(false);
                            JSONObject postedCoordinates = API1Cache.optJSONObject("postedCoordinates");
                            if (postedCoordinates != null) {
                                cache.Pos = new Coordinate(postedCoordinates.optDouble("latitude", 0), postedCoordinates.optDouble("longitude", 0));
                            } else {
                                cache.Pos = new Coordinate();
                            }
                            cache.setTmpNote("");
                        }
                        break;
                    case "hints":
                        cache.setHint(API1Cache.optString("hints", ""));
                        break;
                    case "attributes":
                        JSONArray attributes = API1Cache.optJSONArray("attributes");
                        if (attributes != null) {
                            for (int j = 0; j < attributes.length(); j++) {
                                JSONObject attribute = attributes.optJSONObject(j);
                                if (attribute != null) {
                                    Attributes att = Attributes.getAttributeEnumByGcComId(attribute.optInt("id", 0));
                                    if (attribute.optBoolean("isOn", false)) {
                                        cache.addAttributePositive(att);
                                    } else {
                                        cache.addAttributeNegative(att);
                                    }
                                }
                            }
                        }
                        break;
                    case "longDescription":
                        cache.setLongDescription(API1Cache.optString("longDescription", ""));
                        // containsHtml liefert immer false
                        if (!cache.getLongDescription().contains("<"))
                            cache.setLongDescription(cache.getLongDescription().replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
                        break;
                    case "shortDescription":
                        cache.setShortDescription(API1Cache.optString("shortDescription", ""));
                        // containsHtml liefert immer false
                        if (!cache.getShortDescription().contains("<"))
                            cache.setShortDescription(cache.getShortDescription().replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
                        break;
                    case "additionalWaypoints":
                        addWaypoints(cache, API1Cache.optJSONArray("additionalWaypoints"));
                        break;
                    case "userWaypoints":
                        addUserWaypoints(cache, API1Cache.optJSONArray("userWaypoints"));
                        break;
                    default:
                }
            }
            return cache;
        } catch (Exception e) {
            Log.err(log, "createCache(JSONObject API1Cache)", e);
            return null;
        }
    }

    private static void addWaypoints(Cache cache, JSONArray wpts) {
        if (wpts != null) {
            for (int j = 0; j < wpts.length(); j++) {
                /*
                name (string, optional), coordinates (Coordinates), description (string), typeId (integer), typeName (string), prefix (string)
                 */
                JSONObject wpt = wpts.optJSONObject(j);
                Waypoint waypoint = new Waypoint(true);
                waypoint.CacheId = cache.Id;
                JSONObject coordinates = wpt.optJSONObject("coordinates");
                if (coordinates != null) {
                    waypoint.Pos = new Coordinate(coordinates.optDouble("latitude", 0), coordinates.optDouble("longitude", 0));
                } else {
                    waypoint.Pos = new Coordinate();
                }

                waypoint.setTitle(wpt.optString("name", ""));
                waypoint.setDescription(wpt.optString("description", ""));
                waypoint.Type = CacheTypeFromID(wpt.optInt("typeId", 0));
                waypoint.setGcCode(wpt.optString("prefix", "XX") + cache.getGcCode().substring(2));
                cache.waypoints.add(waypoint);
            }
        }
    }

    private static void addUserWaypoints(Cache cache, JSONArray wpts) {
        if (wpts != null) {
            for (int j = 0; j < wpts.length(); j++) {
                // referenceCode (string), description (string), isCorrectedCoordinates (boolean), coordinates (Coordinates), geocacheCode (string)
                JSONObject wpt = wpts.optJSONObject(j);
                boolean CoordinateOverride = wpt.optString("description", "").equals("Coordinate Override");
                boolean isCorrectedCoordinates = wpt.optBoolean("isCorrectedCoordinates", false);

                if (CoordinateOverride || isCorrectedCoordinates) {
                    Waypoint waypoint = new Waypoint(true);
                    waypoint.CacheId = cache.Id;
                    JSONObject coordinates = wpt.optJSONObject("coordinates");
                    if (coordinates != null) {
                        waypoint.Pos = new Coordinate(coordinates.optDouble("latitude", 0), coordinates.optDouble("longitude", 0));
                    } else {
                        waypoint.Pos = new Coordinate();
                    }

                    waypoint.setTitle("Corrected Coordinates (API)");
                    waypoint.setDescription(wpt.optString("description", ""));
                    waypoint.Type = CacheTypes.Final;
                    waypoint.setGcCode("CO" + cache.getGcCode().substring(2));
                    cache.waypoints.add(waypoint);
                }
            }
        }
    }

    private static CacheTypes CacheTypeFromID(int id) {
        // see https://api.groundspeak.com/documentation#geocache-types
        // see https://api.groundspeak.com/documentation#additional-waypoint-types
        switch (id) {
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
                return CacheTypes.APE;
            case 11:
                return CacheTypes.Camera;
            case 13:
                return CacheTypes.CITO;
            case 137:
                return CacheTypes.Earth;
            case 453:
                return CacheTypes.MegaEvent;
            case 1304:
                return CacheTypes.AdventuresExhibit;
            case 1858:
                return CacheTypes.Wherigo;
            case 3773:
                return CacheTypes.HQ;
            case 7005:
                return CacheTypes.Giga;
            case 217:
                return CacheTypes.ParkingArea;
            case 218:
                return CacheTypes.MultiQuestion;
            case 219:
                return CacheTypes.MultiStage;
            case 220:
                return CacheTypes.Final;
            case 221:
                return CacheTypes.Trailhead;
            case 452:
                return CacheTypes.ReferencePoint;
            default:
                return CacheTypes.Undefined;
        }
    }

    private static CacheSizes CacheSizeFromID(int id) {
        // https://api.groundspeak.com/documentation#geocache-sizes
        switch (id) {
            case 1:
                return CacheSizes.other; // not chosen
            case 2:
                return CacheSizes.micro;
            case 8:
                return CacheSizes.small;
            case 3:
                return CacheSizes.regular; //	Medium
            case 4:
                return CacheSizes.large;
            case 5:
                return CacheSizes.other; //	Virtual
            case 6:
                return CacheSizes.other;
            default:
                return CacheSizes.other;
        }
    }

    private static Date DateFromString(String d) {
        String ps = "yyyy-MM-dd'T'HH:mm:ss";
        if (d.endsWith("Z"))
            ps = ps + "'Z'";
        try {
            return new SimpleDateFormat(ps).parse(d);
        } catch (Exception e) {
            return new Date();
        }
    }

    public enum MemberShipTypes {Unknown, Basic, Charter, Premium}

    public static class PQ implements Serializable {
        private static final long serialVersionUID = 8308386638170255124L;
        public String Name;
        public int PQCount;
        public Date DateLastGenerated;
        public double SizeMB;
        public boolean downloadAvailable = false;
        String GUID;
    }

    public static class UserInfos {
        /*
        the fields:
        referenceCode	string	uniquely identifies the user
        findCount	integer	how many geocache finds the user has
        hideCount	integer	how many geocache hides the user has
        favoritePoints	integer	how many favorite points the user has avaiable
        username	string	the display username
        membershipLevelId	integer	type of the membership (see Membership Types for more info)
        avatarUrl	string	link to image of the user's profile avatar
        bannerUrl	string	link to image of the user's banner image
        profileText	string	text from Profile Information section on user profile page
        homeCoordinates	Coordinates	latitude and longitude of the user's home location
        geocacheLimits	GeocacheLimit	how many geocaches/lite geocaches the user has remaining and time to live until limit is refreshed
        */
        public String username;
        public MemberShipTypes memberShipType;
        public int findCount;
        // geocacheLimits
        public int remaining;
        public int renainingLite;
    }
/*
additionalWaypoints (Array[AdditionalWaypoint], optional),
trackables (Array[Trackable], optional),
geocacheLogs (Array[GeocacheLog], optional),
images (Array[Image], optional),
userWaypoints (Array[UserWaypoint], optional)

User {
membershipLevelId (integer, optional),
findCount (integer, optional),
hideCount (integer, optional),
favoritePoints (integer, optional),
homeCoordinates (Coordinates, optional),
geocacheLimits (GeocacheLimit, optional),
profileText (string, optional),
bannerUrl (string, optional),
url (string, optional),
referenceCode (string, optional),
username (string, optional),
avatarUrl (string, optional)
}
Attribute {
id (integer, optional),
name (string, optional),
isOn (boolean, optional)
}
AdditionalWaypoint {
name (string, optional),
coordinates (Coordinates, optional),
description (string, optional),
typeId (integer, optional),
typeName (string, optional),
prefix (string, optional)
}
Trackable {
referenceCode (string, optional),
iconUrl (string, optional),
name (string, optional),
goal (string, optional),
description (string, optional),
releasedDate (string, optional),
originCountry (string, optional),
allowedToBeCollected (boolean, optional),
ownerCode (string, optional),
holderCode (string, optional),
inHolderCollection (boolean, optional),
currentGeocacheCode (string, optional),
isMissing (boolean, optional),
type (string, optional),
trackableType (TrackableType, optional),
imageCount (integer, optional),
trackingNumber (string, optional),
url (string, optional)
}
GeocacheLog {
referenceCode (string, optional),
ownerCode (string, optional),
owner (User, optional),
imageCount (integer, optional),
isEncoded (boolean, optional),
isArchived (boolean, optional),
images (Array[Image], optional),
url (string, optional),
loggedDate (string),
text (string),
type (string, optional) = ['Found It', 'DNF it', 'Write note', 'Archive', 'Needs archiving', 'Will attend', 'Attended', 'Webcam photo taken', 'Unarchive', 'Temporarily Disable Listing', 'Enable Listing', 'Publish Listing', 'Needs Maintenance', 'Owner Maintenance', 'Update Coordinates', 'Post Reviewer Note', 'Event Announcement'],
geocacheLogType (GeocacheLogType, optional),
updatedCoordinates (Coordinates, optional),
geocacheCode (string),
usedFavoritePoint (boolean, optional)
}
Image {
url (string, optional),
referenceCode (string, optional),
createdDate (string, optional),
description (string): Description of the image ,
guid (string, optional)
}
UserWaypoint {
referenceCode (string, optional),
description (string, optional),
isCorrectedCoordinates (boolean),
coordinates (Coordinates),
geocacheCode (string)
}
GeocacheLimit {
liteCallsRemaining (integer, optional),
liteCallsSecondsToLive (integer, optional),
fullCallsRemaining (integer, optional),
fullCallsSecondsToLive (integer, optional)
}
TrackableType {
id (integer, optional),
name (string, optional),
imageUrl (string, optional)
}
GeocacheLogType {
id (integer, optional),
name (string, optional),
imageUrl (string, optional)
}

 */

}
