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

import CB_Core.CB_Core_Settings;
import CB_Core.LogTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.LogEntry;
import CB_Core.Types.TbList;
import CB_Core.Types.Trackable;
import CB_Utils.Interfaces.ICancel;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Log.Log;
import CB_Utils.Util.ByRef;
import CB_Utils.http.Webb;
import CB_Utils.http.WebbException;
import org.json.JSONArray;
import org.json.JSONObject;

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
    public static String LastAPIError = "";

    public static int CachesLeft = -1;

    public static boolean CacheStatusValid = false;
    public static int CurrentCacheCount = -1;
    public static int MaxCacheCount = -1;
    public static boolean CacheStatusLiteValid = false;
    private static int CachesLeftLite = -1;
    private static int CurrentCacheCountLite = -1;
    private static int MaxCacheCountLite = -1;
    private static boolean mDownloadLimitExceeded = false;
    private static boolean MembershipValuesFetched = false;
    private static MemberShipTypes membershipType = MemberShipTypes.Unknown;
    private static String MemberName = "";
    private static int findCount;
    private static String tmpResult = "";

    public static int CreateFieldNoteAndPublish(String cacheCode, int wptLogTypeId, Date dateLogged, String note, boolean directLog, final ICancel icancel) {
        int chk = chkMembership();
        if (chk < 0)
            return ERROR;

        try {
            JSONObject json = Webb.create()
                    .post(getUrl("CreateFieldNoteAndPublish?format=json"))
                    .body(new JSONObject()
                            .put("AccessToken", GetSettingsAccessToken())
                            .put("CacheCode", cacheCode)
                            .put("WptLogTypeId", String.valueOf(wptLogTypeId))
                            .put("UTCDateLogged", GetUTCDate(dateLogged))
                            .put("Note", ConvertNotes(note))
                    )
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();

            JSONObject status = json.getJSONObject("Status");
            if (status.getInt("StatusCode") == 0) {
                LastAPIError = "";
            } else {
                LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
                LastAPIError += status.getString("StatusMessage") + "\n";
                LastAPIError += status.getString("ExceptionDetails");
                return ERROR;
            }

        } catch (Exception e) {
            LastAPIError = e.getLocalizedMessage();
            Log.err(log, "UploadFieldNotesAPI ConnectTimeoutException", e);
            return ERROR;
        }

        LastAPIError = "";
        return OK;
    }
    private static String ConvertNotes(String note) {
        String result = note.replace("\r", "");
        result = result.replace("\"", "\\\"");
        return result.replace("\n", "\\n");
    }
    private static String GetUTCDate(Date date) {
        long utc = date.getTime();
        TimeZone tzp = TimeZone.getTimeZone("GMT");
        utc = utc - tzp.getOffset(utc);
        String ret = "\\/Date(" + utc + ")\\/";
        Log.info(log, "Logdate uploaded: " + ret);
        return ret;
    }

    /**
     * // Archived, Available and TrackableCount are updated
     * // restriction for nr of caches must be handled by caller
     *
     * @param caches
     * @param icancel
     * @return int as status  on ERROR the String LastAPIError is set
     */
    public static int fetchGeocacheStatus(ArrayList<Cache> caches, final ICancel icancel) {

        if (chkMembership(false) < 0) return ERROR;

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e1) {
        }

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
        if (chkMembership(false) < 0) return ERROR;

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

        if (chkMembership(false) < 0) return ERROR;

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

    /* removed in API 1 */
    public static int GetTrackablesByTrackingNumber(String TrackingCode, ByRef<Trackable> TB, ICancel icancel) {
        int chk = chkMembership();
        if (chk < 0)
            return chk;

        try {
            JSONObject json = Webb.create()
                    .get(getUrl("GetTrackablesByTrackingNumber?AccessToken=" + UrlEncode(GetSettingsAccessToken()) + "&trackingNumber=" + TrackingCode + "&format=json"))
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
            JSONObject status = json.getJSONObject("Status");
            if (status.getInt("StatusCode") == 0) {
                LastAPIError = "";
                JSONArray jTrackables = json.getJSONArray("Trackables");

                for (int i = 0; i < jTrackables.length(); ) {
                    JSONObject jTrackable = (JSONObject) jTrackables.get(i);
                    TB.set(new Trackable(jTrackable));
                    TB.get().setTrackingCode(TrackingCode);
                    return OK;
                }
            } else {
                LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
                LastAPIError += status.getString("StatusMessage") + "\n";
                LastAPIError += status.getString("ExceptionDetails");
                TB = null;
                return ERROR;
            }

        } catch (Exception e) {
            LastAPIError = e.getLocalizedMessage();
            Log.err(log, "getTBbyTreckNumber ConnectTimeoutException", e);
            TB = null;
            return ERROR;
        }

        TB = null;
        return ERROR;
    }

    // End Old API

    // geocaches/{referenceCode}/geocachelogs
    // there is no LogId in the new API
    public static int fetchGeocacheLogsOfFriends(Cache cache, ArrayList<LogEntry> logList, cancelRunnable cancelRun) {
        if (cache == null) return ERROR;
        if (chkMembership() < 0) return ERROR;
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

    // fetchImagesForGeocache
    // todo es werden nur 10 geholt
    public static int fetchImagesForGeocache(String cacheCode, HashMap<String, URI> list) {
        Log.info(log, "fetchImagesForGeocache");
        LastAPIError = "";
        if (cacheCode == null) return ERROR;
        if (chkMembership() < 0) return ERROR;
        if (list == null)
            list = new HashMap<>();
        try {
            //
            JSONArray jImages = Webb.create()
                    .get(getUrl(1, "geocaches/" + cacheCode + "/images?fields=url,description"))
                    .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonArray()
                    .getBody();

            try {
                Log.info(log, "fetchImagesForGeocache Anz.: " + jImages.length());
                for (int ii = 0; ii < jImages.length(); ii++) {
                    try {
                        JSONObject jImage = (JSONObject) jImages.get(ii);
                        String name = jImage.getString("description");
                        String uri = jImage.getString("url");
                        Log.debug(log, "fetchImagesForGeocache getImageObject Nr.:" + ii + " '" + name + "' " + uri);
                        // ignore log images (in dieser API kommen keine Logbilder mehr)
                        if (uri.contains("/cache/log")) {
                            Log.debug(log, "fetchImagesForGeocache getImageObject Nr.:" + ii + " ignored.");
                            continue; // LOG-Image
                        }
                        // Check for duplicate name
                        if (list.containsKey(name)) {
                            for (int nr = 1; nr < 10; nr++) {
                                if (list.containsKey(name + "_" + nr)) {
                                    Log.debug(log, "fetchImagesForGeocache getImageObject Nr.:" + ii + " ignored: ");
                                    continue; // Name already exists --> next nr
                                }
                                name += "_" + nr;
                                break;
                            }
                        }
                        list.put(name, new URI(uri));
                    } catch (Exception ex) {
                        Log.err(log, "fetchImagesForGeocache getImageObject Nr.:" + ii, ex);
                    }
                }
            } catch (Exception ex) {
                LastAPIError = ex.getLocalizedMessage();
                Log.err(log, "fetchImagesForGeocache getJSONArray(\"Images\") ", ex);
                return ERROR;
            }

            Log.info(log, "fetchImagesForGeocache done");
            return OK;
        } catch (Exception ex) {
            LastAPIError += ex.getLocalizedMessage();
            LastAPIError += "\n for " + getUrl(1, "geocaches/" + cacheCode + "/images?fields=url,description");
            LastAPIError += "\n APIKey: " + GetSettingsAccessToken();
            Log.err(log, "CreateTrackableLog \n" + LastAPIError, ex);
            return ERROR;
        }
    }

    public static int fetchUsersTrackables(TbList list) {
        Log.info(log, "fetchUsersTrackables");
        if (chkMembership() < 0) return ERROR;
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

            for (int ii = 0; ii < jTrackables.length(); ii++) {
                JSONObject jTrackable = (JSONObject) jTrackables.get(ii);
                if (!jTrackable.optBoolean("inHolderCollection", false)) {
                    Trackable tb = new Trackable(1, jTrackable);
                    Log.debug(log, "fetchUsersTrackables: add " + tb.getName());
                    list.add(tb);
                } else {
                    Log.debug(log, "fetchUsersTrackables: not in HolderCollection" + jTrackable.optString("name", ""));
                }
            }

            Log.info(log, "fetchUsersTrackables done \n" + jTrackables.toString());
            return OK;
        } catch (Exception ex) {
            LastAPIError = ex.getLocalizedMessage();
            Log.err(log, "fetchUsersTrackables", ex);
            return ERROR;
        }
    }

    // "trackables/" + TBCode + "?fields=referenceCode,trackingNumber,iconUrl,name,goal,description,releasedDate,ownerCode,holderCode,currentGeocacheCode,type"
    public static int fetchTrackableByTBCode(String TBCode, ByRef<Trackable> TB) {
        Log.info(log, "fetchTrackableByTBCode for " + TBCode);
        LastAPIError = "";
        if (chkMembership() < 0) return ERROR;
        try {
            /*
            referenceCode	string	uniquely identifies the trackable
            iconUrl	string	link to image for trackable icon
            name	string	display name of the trackable
            imageCount	int	how many owner images on the trackable
            goal	string	the owner's goal for the trackable
            description	string	text about the trackable
            releasedDate	datetime	when the trackable was activated
            originCountry	string	where the trackable originated from
            ownerCode	string	identifier about the owner
            holderCode	string	user identifier about the current holder (null if not currently in someone's inventory)
            inHolderCollection	bool	if the trackable is in the holder's collection
            currentGeocacheCode	string	identifier of the geocache if the trackable is currently in one
            isMissing	bool	flag if trackable is marked as missing
            type	string	category type display name
            trackingNumber	string	unique number used to prove discovery of trackable. only returned if user matches the holderCode
            allowedToBeCollected (boolean, optional),
            */
            JSONObject result = Webb.create()
                    .get(getUrl(1, "trackables/" + TBCode + "?fields=referenceCode,trackingNumber,iconUrl,name,goal,description,releasedDate,ownerCode,holderCode,currentGeocacheCode,type"))
                    .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            Log.debug(log, result.toString());
            TB.set(new Trackable(1, result));
            Log.info(log, "fetchTrackableByTBCode done");
            return OK;
        } catch (Exception ex) {
            LastAPIError += ex.getLocalizedMessage();
            LastAPIError += "\n for " + getUrl(1, "trackables/" + TBCode + "?fields=url,description");
            LastAPIError += "\n APIKey: " + GetSettingsAccessToken();
            Log.err(log, "fetchTrackablesByTBCode \n" + LastAPIError, ex);
            return ERROR;
        }

    }

    public static int createTrackableLog(Trackable TB, String cacheCode, int LogTypeId, Date dateLogged, String note) {
        return createTrackableLog(TB.getTBCode(), TB.getTrackingCode(), cacheCode, LogTypeId, dateLogged, note);
    }

    // "trackablelogs" CREATE TRACKABLE LOG
    public static int createTrackableLog(String TBCode, String TrackingNummer, String cacheCode, int LogTypeId, Date dateLogged, String note) {
        Log.info(log, "createTrackableLog");
        LastAPIError = "";
        if (cacheCode == null) cacheCode = "";
        int chk = chkMembership();
        if (chk < 0) return ERROR;
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
            Log.info(log, "createTrackableLog done\n" + LastAPIError + result.toString());
            LastAPIError = "";
            return OK;
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
            return ERROR;
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

    // todo GC has to correct Implementation
    // see http://forum587.rssing.com/browser.php?indx=16809329&item=44991
    // "geocaches/" + cacheCode + "/notes"
    public static int updateCacheNote(String cacheCode, String notes) {
        Log.info(log, "updateCacheNote");
        LastAPIError = "";
        if (cacheCode == null || cacheCode.length() == 0) return ERROR;
        if (!IsPremiumMember()) return ERROR;
        String wrongJsonBody = "\"" + prepareNote(notes) + "\"";
        try {
            String response = Webb.create()
                    .put(getUrl(1, "geocaches/" + cacheCode + "/notes"))
                    .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                    // todo remove next two lines if GC implementation is corrected
                    .header(Webb.HDR_CONTENT_TYPE, Webb.APP_JSON)
                    .body(wrongJsonBody)
                    // todo use this if Implementation at GC is ok, result should then be JsonObject too :
                    // .body(new JSONObject().put("note", prepareNote(notes)))
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asString()
                    .getBody();
            Log.info(log, "updateCacheNote done \n" + response.toString());
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

    // /users/me?fields=username,membershipLevelId"
    public static int fetchMembership() {
        return fetchMembership("me", "");
    }

    // /users/me?fields=username,membershipLevelId"+"findCount"
    public static int fetchFindCount() {
        fetchMembership("me", ",findCount");
        return findCount;
    }

    // /users/me?fields=username,membershipLevelId"+"geocacheLimits"
    // todo implement geocacheLimits in fetchMembership
    public static int fetchGeocacheLimits() {
        return fetchMembership("me", ",geocacheLimits");
    }

    // /users/UserName?fields=username,membershipLevelId"
    public static String fetchUserName(String UserName) {
        fetchMembership(UserName, "");
        return tmpResult;
    }

    // /users/UserName?fields=username,membershipLevelId"+additionalFields
    private static int fetchMembership(String UserCode, String additionalFields) {
        Log.info(log, "fetchMembership for " + UserCode + additionalFields);
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
        LastAPIError = "";
        try {
            JSONObject response = Webb.create()
                    .get(getUrl(1, "/users/" + UserCode + "?fields=username,membershipLevelId" + additionalFields))
                    .header(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken())
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            if (UserCode.equals("me")) {
                MembershipValuesFetched = true;
                membershipType = MemberShipTypesFromInt(response.getInt("membershipLevelId"));
                MemberName = response.optString("username", "");
                findCount = response.optInt("findCount", -1);
            } else {
                tmpResult = response.optString("username", "");
            }
            Log.info(log, "fetchMembership done \n" + response.toString());
            return OK;
        } catch (Exception ex) {
            LastAPIError = ex.getLocalizedMessage();
            Log.err(log, "fetchMembership", ex);
            if (UserCode.equals("me")) {
                MembershipValuesFetched = false;
                membershipType = MemberShipTypes.Unknown;
                MemberName = "";
                findCount = -1;
            } else {
                tmpResult = "";
            }
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

    public static MemberShipTypes getMembershipType() {
        chkMembership(false);
        return membershipType;
    }

    public static String fetchMemberName() {
        MembershipValuesFetched = false;
        chkMembership(false);
        return MemberName;
    }

    public static int chkMembership() {
        return chkMembership(true);
    }

    public static int chkMembership(boolean getNewAccessTokenIfNeeded) {
        if (MembershipValuesFetched) {
            return OK;
        }

        if (GetSettingsAccessToken().length() > 0) {
            fetchMembership();
        }

        // we need a new AccessToken
        if (!MembershipValuesFetched) {
            if (getNewAccessTokenIfNeeded)
                API_ErrorEventHandlerList.callInvalidApiKey(API_ErrorEventHandlerList.API_ERROR.INVALID);
            return ERROR;
        } else {
            return OK;
        }
    }

    public static boolean isAccessTokenValid() {
        return MembershipValuesFetched;
    }

    public static boolean IsPremiumMember() {
        chkMembership(true);
        return membershipType == MemberShipTypes.Premium;
    }

    static String GetSettingsAccessToken() {
        // return "GMgpNfEDRInXcSWnXxvyXfxH7l0=";
        /* */
        String act;
        if (CB_Core_Settings.UseTestUrl.getValue()) {
            act = CB_Core_Settings.AccessTokenForTest.getValue();
        } else {
            act = CB_Core_Settings.AccessToken.getValue();
        }

        // for ACB we added an additional A in settings
        if ((act.startsWith("A"))) {
            Log.debug(log, "Access Token = " + act.substring(1, act.length()));
            return act.substring(1, act.length());
        }
        else
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

    private static String getUrl(int version, String command) {
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

    public enum MemberShipTypes {Unknown, Basic, Charter, Premium}

}
