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
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Log.Log;
import CB_Utils.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

// todo rework errorhandling (API 1 differs)
public class GroundspeakAPI {
    public static final int OK = 0;
    public static final int ERROR = -1;
    private static final String log = "GroundspeakAPI";
    public static String LastAPIError = "";
    public static int APIError;

    public static int CurrentCacheCount = -1;
    public static int MaxCacheCount = -1;
    public static UserInfos me;
    private static boolean mDownloadLimitExceeded = false;
    private static Webb netz;
    private static long startTs;
    private static int nrOfApiCalls;
    private static int retryCount;

    private static Webb getNetz() {
        if (netz == null) {
            netz = Webb.create();
            netz.setDefaultHeader(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken());
            Webb.setReadTimeout(CB_Core_Settings.socket_timeout.getValue());
            Webb.setConnectTimeout(CB_Core_Settings.connection_timeout.getValue());
            startTs = System.currentTimeMillis();
            nrOfApiCalls = 0;
            retryCount = 0;
        }

        if (System.currentTimeMillis() - startTs > 60000) {
            // reset nrOfApiCalls after one minute
            // perhaps can avoid retry for 429 by checking nrOfApiCalls. ( not implemented yet )
            startTs = System.currentTimeMillis();
            nrOfApiCalls = 0;
            retryCount = 0;
        }

        nrOfApiCalls++;
        return netz;
    }

    public static void setAuthorization() {
        getNetz().setDefaultHeader(Webb.HDR_AUTHORIZATION, "bearer " + GetSettingsAccessToken());
        me = null;
    }

    private static boolean retry(Exception ex) {
        // Alternate: implement own RetryManager for 429
        if (ex instanceof WebbException) {
            WebbException we = (WebbException) ex;
            Response re = we.getResponse();
            if (re != null) {
                JSONObject ej;
                APIError = re.getStatusCode();
                if (APIError == 429) {
                    // 429 is only for nr of calls per minute
                    if (retryCount == 0) {
                        Log.debug(log, "API-Limit exceeded: " + nrOfApiCalls + " Number of Calls within " + ((System.currentTimeMillis() - startTs) / 1000) + " seconds.");
                        // Difference 61000 is one second more than one minute. (60000 = one minute gives still 429 Exception)
                        try {
                            long ta = 61000 - (System.currentTimeMillis() - startTs);
                            if (ta > 0)
                                Thread.sleep(ta);
                        } catch (InterruptedException ignored) {
                            LastAPIError = "Aborted by user";
                        }
                        startTs = System.currentTimeMillis();
                        nrOfApiCalls = 0;
                        retryCount++; //important hint: on successful execution (no Exception), retryCount must be reset to 0 else there is no retry for the next failure.
                    } else {
                        startTs = System.currentTimeMillis();
                        nrOfApiCalls = 0;
                        retryCount = 0;
                        LastAPIError = "******* Aborting: After retry API-Limit is still exceeded.";
                    }
                } else {
                    // todo Handle 401, 500, ...
                    // 403 = limit exceeded: want to get more caches than remain (lite / full) : get limits for good message
                    try {
                        ej = new JSONObject(new JSONTokener((String) re.getErrorBody()));
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
            else {
                // re == null
                APIError = ERROR;
                LastAPIError = ex.getLocalizedMessage();
            }
        } else {
            // no WebbException
            APIError = ERROR;
            LastAPIError = ex.getLocalizedMessage();
        }
        return retryCount > 0;
    }

    // Live API

    public static boolean isDownloadLimitExceeded() {
        return mDownloadLimitExceeded;
    }

    public static void setDownloadLimitExceeded() {
        mDownloadLimitExceeded = true;
    }

    // API 1.0 see https://api.groundspeak.com/documentation and https://api.groundspeak.com/api-docs/index

    // todo SearchGCName, SearchLiveMap, SearchGC are here only for test. Call that from the UserInterface
    public static ArrayList<Cache> SearchGCName(SearchGCName searchC) {
        GroundspeakAPI.UserInfos me = GroundspeakAPI.fetchMyUserInfos();
        if (me.remaining > 0 && me.renainingLite > 0) {
            Query q = new Query()
                    .setMaxToFetch(searchC.number)
                    .searchForTitle(searchC.gcName)
                    .serchInCircle(searchC.pos, (int) searchC.distanceInMeters)
                    .excludeOwn()
                    .excludeFinds()
                    .onlyActiveGeoCaches()
                    .resultWithFullFields()
                    .resultWithLogs(30)
                    .resultWithImages(30);
            return GroundspeakAPI.fetchGeoCaches(q);
        }
        return new ArrayList<>();
    }

    public static ArrayList<Cache> SearchLiveMap(Coordinate location) {
        Query query = new Query()
                .serchInCircleOf100Miles(location)
                .excludeOwn()
                .excludeFinds()
                .onlyActiveGeoCaches()
                .resultWithLiteFields();
        return fetchGeoCaches(query);
    }

    public static ArrayList<Cache> SearchGC(ArrayList<Cache> caches) {
        /*
        ArrayList<Cache> caches = new ArrayList<>();
        for (String GcCode : cacheCodes) {
            Cache c = new Cache(true);
            c.setGcCode(GcCode);
            caches.add(c);
        }
        */
        Query query = new Query()
                .resultWithFullFields()
                .resultWithLogs(30)
                .resultWithImages(30);
        return updateGeoCaches(query, caches);
    }

    public static ArrayList<Cache> fetchGeoCaches(Query query) {
        // fetch/update geocaches consumes a lite or full cache
        ArrayList<Cache> CacheList = new ArrayList<>();
        try {

            int skip = 0;
            int take = 100;

            ArrayList<String> fields = query.getFields();
            boolean onlyLiteFields = query.containsOnlyLiteFields(fields);
            if (onlyLiteFields) {
                fetchMyCacheLimits();
                if (me.renainingLite < me.remaining) {
                    onlyLiteFields = false;
                }
            }

            do {
                boolean doRetry;
                do {
                    doRetry = false;
                    try {
                        if (query.maxToFetch < skip + take)
                            take = query.maxToFetch - skip;
                        Response<JSONArray> r = query.putQuery(getNetz()
                                .get(getUrl(1, "geocaches/search"))
                                .param("skip", skip)
                                .param("take", take)
                                .param("lite", onlyLiteFields)
                                .ensureSuccess())
                                .asJsonArray();

                        retryCount = 0;

                        JSONArray fetchedCaches = r.getBody();

                        for (int ii = 0; ii < fetchedCaches.length(); ii++) {
                            JSONObject fetchedCache = (JSONObject) fetchedCaches.get(ii);
                            Cache c = createGeoCache(fetchedCache, fields, null);
                            if (c != null) CacheList.add(c);
                        }

                        if (fetchedCaches.length() < take || take < 100) {
                            take = 0; // we got all
                        } else {
                            skip = skip + take;
                        }

                    } catch (Exception ex) {
                        doRetry = retry(ex);
                        if (!doRetry) {
                            return CacheList;
                        }
                    }
                }
                while (doRetry);
            } while (take > 0 && skip < query.maxToFetch);

        } catch (Exception e) {
            APIError = ERROR;
            LastAPIError = e.getLocalizedMessage();
            Log.err(log, "fetchGeoCaches", e);
            return CacheList;
        }
        return CacheList;
    }

    public static ArrayList<Cache> updateStatusOfGeoCaches(ArrayList<Cache> caches) {
        // fetch/update geocaches consumes a lite or full cache
        Query query = new Query().resultForStatusFields();
        return updateGeoCaches(query, caches);
    }

    public static ArrayList<Cache> updateGeoCaches(Query query, ArrayList<Cache> caches) {
        // fetch/update geocaches consumes a lite or full cache
        ArrayList<Cache> CacheList = new ArrayList<>();
        try {

            // just to simplify splitting into blocks of max 50 caches
            Cache[] arrayOfCaches = new Cache[caches.size()];
            caches.toArray(arrayOfCaches);

            int skip = 0;
            int take = 50;


            ArrayList<String> fields = query.getFields();
            boolean onlyLiteFields = query.containsOnlyLiteFields(fields);
            if (onlyLiteFields) {
                fetchMyCacheLimits();
                if (me.renainingLite < me.remaining) {
                    onlyLiteFields = false;
                }
            }
            onlyLiteFields = true;

            do {
                // preparing the next block of max 50 caches to update
                Map<String, Cache> mapOfCaches = new HashMap<>();
                StringBuilder CacheCodes = new StringBuilder();
                int took = 0;
                for (int i = skip; i < Math.min(skip + take, arrayOfCaches.length); i++) {
                    Cache cache = arrayOfCaches[i];
                    if (cache.getGcCode().toLowerCase().startsWith("gc")) {
                        mapOfCaches.put(cache.getGcCode(), cache);
                        CacheCodes.append(",").append(cache.getGcCode());
                        took++;
                    }
                }
                if (took == 0) return CacheList; // no gc in the block
                skip = skip + take;

                boolean doRetry;
                do {
                    doRetry = false;
                    try {
                        Response<JSONArray> r = query.putQuery(getNetz()
                                .get(getUrl(1, "geocaches"))
                                .param("referenceCodes", CacheCodes.substring(1))
                                .param("lite", onlyLiteFields)
                                .ensureSuccess())
                                .asJsonArray();

                        retryCount = 0;

                        JSONArray fetchedCaches = r.getBody();

                        for (int ii = 0; ii < fetchedCaches.length(); ii++) {
                            JSONObject fetchedCache = (JSONObject) fetchedCaches.get(ii);
                            Cache cache = mapOfCaches.get(fetchedCache.optString("referenceCode"));
                            Cache c = createGeoCache(fetchedCache, fields, cache);
                            if (c != null) CacheList.add(c);
                        }
                    } catch (Exception ex) {
                        doRetry = retry(ex);
                        if (!doRetry) {
                            if (APIError == 404) {
                                // one bad GCCode (not starting with GC) causes Error 404: will hopefully be changed in an update after 11.26.2018
                                // a not existing GCCode seems to be ignored, what is ok
                                doRetry = false;
                                Log.err(log, "fetchGeoCaches - skipped block cause: " + LastAPIError);
                            } else {
                                return CacheList;
                            }
                        }
                    }
                }
                while (doRetry);
            } while (skip < arrayOfCaches.length);

        } catch (Exception e) {
            APIError = ERROR;
            LastAPIError = e.getLocalizedMessage();
            Log.err(log, "updateGeoCaches", e);
            return CacheList;
        }
        return CacheList;
    }

    public static ArrayList<PQ> fetchPocketQueryList() {

        ArrayList<PQ> pqList = new ArrayList<>();

        try {

            int skip = 0;
            int take = 50;
            String fields = "referenceCode,name,lastUpdatedDateUtc,count";

            do {
                boolean doRetry;
                do {
                    doRetry = false;
                    try {
                        Response<JSONArray> r = getNetz()
                                .get(getUrl(1, "users/me/lists"))
                                .param("types", "pq")
                                .param("fields", fields)
                                .param("skip", skip)
                                .param("take", take)
                                .ensureSuccess()
                                .asJsonArray();

                        retryCount = 0;
                        skip = skip + take;

                        JSONArray response = r.getBody();

                        for (int ii = 0; ii < response.length(); ii++) {
                            JSONObject jPQ = (JSONObject) response.get(ii);
                            PQ pq = new PQ();
                            pq.GUID = jPQ.optString("referenceCode", "");
                            if (pq.GUID.length() > 0) {
                                pq.Name = jPQ.optString("name", "");
                                try {
                                    String dateCreated = jPQ.optString("lastUpdatedDateUtc", "");
                                    pq.DateLastGenerated = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(dateCreated);
                                } catch (Exception exc) {
                                    Log.err(log, "fetchPocketQueryList", "DateLastGenerated", exc);
                                    pq.DateLastGenerated = new Date();
                                }
                                pq.PQCount = jPQ.getInt("count");
                                pq.SizeMB = -1;
                                pq.downloadAvailable = true;
                                pqList.add(pq);
                            }
                        }

                        if (response.length() < take) {
                            APIError = OK;
                            return pqList;
                        }
                    } catch (Exception ex) {
                        doRetry = retry(ex);
                        if (!doRetry) {
                            // APIError from retry
                            return pqList;
                        }
                    }
                }
                while (doRetry);
            } while (true);

        } catch (Exception e) {
            APIError = ERROR;
            LastAPIError = e.getLocalizedMessage();
            Log.err(log, "fetchPocketQueryList", e);
            return pqList;
        }
    }

    public static void fetchPocketQuery(PQ pocketQuery, String PqFolder) {
        InputStream inStream = null;
        BufferedOutputStream outStream = null;
        try {
            inStream = getNetz()
                    .get(getUrl(1, "lists/" + pocketQuery.GUID + "/geocaches/zipped"))
                    .ensureSuccess()
                    .asStream()
                    .getBody();
            String dateString = new SimpleDateFormat("yyyyMMddHHmmss").format(pocketQuery.DateLastGenerated);
            String local = PqFolder + "/" + pocketQuery.Name + "_" + dateString + ".zip";
            FileOutputStream localFile = new FileOutputStream(local);
            outStream = new BufferedOutputStream(localFile);
            WebbUtils.copyStream(inStream, outStream);
            APIError = OK;
        } catch (Exception e) {
            Log.err(log, "fetchPocketQuery", e);
            APIError = ERROR;
            LastAPIError = e.getLocalizedMessage();
        } finally {
            try {
                if (outStream != null)
                    outStream.close();
                if (inStream != null)
                    inStream.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static int UploadDraftOrLog(String cacheCode, int wptLogTypeId, Date dateLogged, String note, boolean directLog) {
        Log.info(log, "UploadDraftOrLog");

        if (isAccessTokenInvalid()) return ERROR;

        try {
            if (directLog) {
                Log.debug(log, "is Log");
                getNetz()
                        .post(getUrl(1, "geocachelogs"))
                        .body(new JSONObject()
                                .put("geocacheCode", cacheCode)
                                .put("type", wptLogTypeId)
                                .put("loggedDate", getUTCDate(dateLogged))
                                .put("text", prepareNote(note))
                        )
                        .ensureSuccess()
                        .asVoid();
            } else {
                Log.debug(log, "is draft");
                getNetz()
                        .post(getUrl(1, "logdrafts"))
                        .body(new JSONObject()
                                .put("guid", UUID.randomUUID().toString())
                                .put("geocacheCode", cacheCode)
                                .put("logType", wptLogTypeId)
                                .put("dateLoggedUtc", getUTCDate(dateLogged))
                                .put("note", prepareNote(note))
                        )
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

    public static ArrayList<LogEntry> fetchGeocacheLogs(Cache cache, boolean all, ICancelRunnable cancelRun) {
        ArrayList<LogEntry> logList = new ArrayList<>();

        // let the calling thread run to an end
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        LinkedList<String> friendList = new LinkedList<>();
        if (!all) {
            for (String f : CB_Core_Settings.Friends.getValue().split("|")) {
                friendList.add(f.toLowerCase(Locale.US));
            }
        }

        int start = 0;
        int count = 30;

        while (!cancelRun.doCancel())
        // Schleife, solange bis entweder keine Logs mehr geladen werden oder bis Logs aller Freunde geladen sind.
        {
            boolean doRetry;
            do {
                doRetry = false;
                try {
                    JSONArray geocacheLogs = getNetz()
                            .get(getUrl(1, "geocaches/" + cache.getGcCode() + "/geocachelogs"))
                            .param("fields", "owner.username,loggedDate,text,type,referenceCode")
                            .param("skip", start)
                            .param("take", count)
                            .ensureSuccess()
                            .asJsonArray()
                            .getBody();

                    retryCount = 0;

                    for (int ii = 0; ii < geocacheLogs.length(); ii++) {
                        JSONObject geocacheLog = (JSONObject) geocacheLogs.get(ii);
                        if (!all) {
                            String finder = getStringValue(geocacheLog, "owner", "username");
                            if (finder.length() == 0 || !friendList.contains(finder.toLowerCase(Locale.US))) {
                                continue;
                            }
                            friendList.remove(finder.toLowerCase(Locale.US));
                        }

                        logList.add(createLog(geocacheLog, cache));
                    }

                    // all logs loaded or all friends found
                    if ((geocacheLogs.length() < count) || (!all && (friendList.size() == 0))) {
                        APIError = OK;
                        return logList;
                    }

                } catch (Exception e) {
                    doRetry = retry(e);
                    if (!doRetry) {
                        return logList;
                    }
                    Log.err(log, "fetchGeocacheLogs", e);
                }
            }
            while (doRetry);
            // die nächsten Logs laden
            start += count;
        }
        APIError = ERROR;
        LastAPIError = "Loading Logs cancelled";
        return (logList);
    }

    public static HashMap<String, URI> downloadImageListForGeocache(String cacheCode) {

        HashMap<String, URI> list = new HashMap<>();
        LastAPIError = "";

        if (cacheCode == null || isAccessTokenInvalid()) {
            APIError = ERROR;
            return list;
        }

        int skip = 0;
        int take = 50;

        // todo Schleife (es werden nur 50 geholt (was reicht))
        do {
            try {
                Response<JSONArray> r = getNetz()
                        .get(getUrl(1, "geocaches/" + cacheCode + "/images"))
                        .param("fields", "url,description")
                        .param("skip", skip)
                        .param("take", take)
                        .ensureSuccess()
                        .asJsonArray();

                retryCount = 0;

                list.putAll(createImageList(r.getBody()));

            } catch (Exception ex) {
                if (!retry(ex)) {
                    return list;
                }
            }
        } while (true);
    }

    public static TBList downloadUsersTrackables() {
        TBList list = new TBList();
        if (isAccessTokenInvalid()) return list;
        LastAPIError = "";
        try {
            JSONArray jTrackables = getNetz()
                    .get(getUrl(1, "trackables"))
                    .param("fields", "referenceCode,trackingNumber,iconUrl,name,goal,description,releasedDate,owner.username,holder.username,currentGeocacheCode,type,inHolderCollection")
                    .ensureSuccess()
                    .asJsonArray()
                    .getBody();

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
            return list;
        }
    }

    public static Trackable fetchTrackable(String TBCode) {
        Log.info(log, "fetchTrackable for " + TBCode);
        LastAPIError = "";
        APIError = 0;
        if (isAccessTokenInvalid()) return null;
        try {
            Trackable tb = createTrackable(getNetz()
                    .get(getUrl(1, "trackables/" + TBCode))
                    .param("fields", "referenceCode,trackingNumber,iconUrl,name,goal,description,releasedDate,owner.username,holder.username,currentGeocacheCode,type")
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

    public static int uploadTrackableLog(Trackable TB, String cacheCode, int LogTypeId, Date dateLogged, String note) {
        return uploadTrackableLog(TB.getTBCode(), TB.getTrackingCode(), cacheCode, LogTypeId, dateLogged, note);
    }

    public static int uploadTrackableLog(String TBCode, String TrackingNummer, String cacheCode, int LogTypeId, Date dateLogged, String note) {
        Log.info(log, "uploadTrackableLog");
        if (cacheCode == null) cacheCode = "";
        if (isAccessTokenInvalid()) return ERROR;
        try {
            getNetz()
                    .post(getUrl(1, "trackablelogs"))
                    .body(new JSONObject()
                            .put("trackingNumber", TrackingNummer) // code only found on the trackable itself (only needed for creating a log)
                            .put("trackableCode", TBCode) // identifier of the related trackable, required for creation
                            .put("geocacheCode", cacheCode)
                            .put("loggedDate", getUTCDate(dateLogged))
                            .put("text", prepareNote(note))
                            .put("typeId", LogTypeId) // see Trackable Log Types https://api.groundspeak.com/documentation#trackable-log-types
                    )
                    .ensureSuccess()
                    .asVoid();
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
            Log.err(log, "uploadTrackableLog \n" + LastAPIError, ex);
            return ERROR;
        }
    }

    public static int uploadCacheNote(String cacheCode, String notes) {
        Log.info(log, "uploadCacheNote for " + cacheCode);
        LastAPIError = "";
        if (cacheCode == null || cacheCode.length() == 0) return ERROR;
        if (!isPremiumMember()) return ERROR;
        try {
            getNetz()
                    .put(getUrl(1, "geocaches/" + cacheCode + "/notes"))
                    .body(new JSONObject().put("note", prepareNote(notes)))
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

    public static boolean isAccessTokenInvalid() {
        return (fetchMyUserInfos().memberShipType == MemberShipTypes.Unknown);
    }

    public static boolean isPremiumMember() {
        return fetchMyUserInfos().memberShipType == MemberShipTypes.Premium;
    }

    public static UserInfos fetchMyUserInfos() {
        if (me == null || me.memberShipType == MemberShipTypes.Unknown) {
            me = fetchUserInfos("me");
            if (me.memberShipType == MemberShipTypes.Unknown) {
                me.findCount = -1;
                // we need a new AccessToken
                // API_ErrorEventHandlerList.handleApiKeyError(API_ErrorEventHandlerList.API_ERROR.INVALID);
                Log.err(log, "fetchMyUserInfos: Need a new Access Token");
            }
        }
        return me;
    }

    public static void fetchMyCacheLimits() {
        me = fetchUserInfos("me");
    }

    public static UserInfos fetchUserInfos(String UserCode) {
        LastAPIError = "";
        APIError = 0;
        UserInfos ui = new UserInfos();
        do {
            try {
                JSONObject response = getNetz()
                        .get(getUrl(1, "/users/" + UserCode))
                        .param("fields", "username,membershipLevelId,findCount,geocacheLimits")
                        .ensureSuccess()
                        .asJsonObject()
                        .getBody();
                retryCount = 0;
                ui.username = response.optString("username", "");
                ui.memberShipType = MemberShipTypesFromInt(response.optInt("membershipLevelId", -1));
                ui.findCount = response.optInt("findCount", -1);
                JSONObject geocacheLimits = response.optJSONObject("geocacheLimits");
                if (geocacheLimits != null) {
                    ui.remaining = geocacheLimits.optInt("fullCallsRemaining", -1);
                    ui.renainingLite = geocacheLimits.optInt("liteCallsRemaining", -1);
                    ui.remainingTime = geocacheLimits.optInt("fullCallsSecondsToLive", -1);
                    ui.renainingLiteTime = geocacheLimits.optInt("liteCallsSecondsToLive", -1);
                }
                return ui;
            } catch (Exception ex) {
                if (!retry(ex)) {
                    Log.err(log, "fetchUserInfos:" + APIError + ":" + LastAPIError);
                    Log.trace(log, ex);
                    return ui;
                }
            }
        }
        while (true);
    }

    public static String GetSettingsAccessToken() {
        /* */
        String act;
        if (CB_Core_Settings.UseTestUrl.getValue()) {
            act = CB_Core_Settings.AccessTokenForTest.getValue();
        } else {
            act = CB_Core_Settings.AccessToken.getValue();
        }

        // for ACB we added an additional A in settings
        if ((act.startsWith("A"))) {
            // Log.debug(log, "Access Token = " + act.substring(1, act.length()));
            return act.substring(1);
        } else
            Log.err(log, "no Access Token");
        return "";
        /* */
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
            tb.CurrentOwnerName = getStringValue(API1Trackable, "holder", "username");
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
            tb.OwnerName = getStringValue(API1Trackable, "owner", "username");
            tb.TypeName = API1Trackable.optString("type", "");
            return tb;
        } catch (Exception e) {
            Log.err(log, "createTrackable(JSONObject API1Trackable)", e);
            return null;
        }
    }

    private static Cache createGeoCache(JSONObject API1Cache, ArrayList<String> fields, Cache cache) {
        // see https://api.groundspeak.com/documentation#geocache
        // see https://api.groundspeak.com/documentation#lite-geocache
        // todo add handle expand fields
        if (cache == null)
            cache = new Cache(true); // todo ? false if not full
        cache.setAttributesPositive(new DLong(0, 0));
        cache.setAttributesNegative(new DLong(0, 0));
        String tmp;
        try {
            for (String field : fields) {
                int withDot = field.indexOf(".");
                String switchValue;
                String subValue;
                if (withDot > 0) {
                    switchValue = field.substring(0, withDot);
                    subValue = field.substring(withDot + 1);
                } else {
                    switchValue = field;
                }
                switch (switchValue) {
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
                        cache.setName(API1Cache.optString(switchValue, ""));
                        break;
                    case "difficulty":
                        cache.setDifficulty((float) API1Cache.optDouble(switchValue, 1));
                        break;
                    case "terrain":
                        cache.setTerrain((float) API1Cache.optDouble(switchValue, 1));
                        break;
                    case "favoritePoints":
                        cache.favPoints = API1Cache.optInt(switchValue, 0);
                        break;
                    case "trackableCount":
                        cache.NumTravelbugs = API1Cache.optInt(switchValue, 0);
                        break;
                    case "placedDate":
                        cache.setDateHidden(DateFromString(API1Cache.optString(switchValue, "")));
                        break;
                    case "geocacheType":
                        // switch subValue
                        cache.Type = CacheTypeFromID(API1Cache.optJSONObject(switchValue).optInt("id", 0));
                        break;
                    case "geocacheSize":
                        // switch subValue
                        cache.Size = CacheSizeFromID(API1Cache.optJSONObject(switchValue).optInt("id", 0));
                        break;
                    case "location":
                        JSONObject location = API1Cache.optJSONObject(switchValue);
                        // switch subValue
                        cache.setCountry(location.optString("country", ""));
                        cache.setState(location.optString("state", ""));
                        break;
                    case "status":
                        String status = API1Cache.optString(switchValue, "");
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
                    case "owner":
                        cache.setOwner(getStringValue(API1Cache, switchValue, "username"));
                        break;
                    case "ownerAlias":
                        cache.setPlacedBy(API1Cache.optString(switchValue, ""));
                        break;
                    case "userData":
                        JSONObject userData = API1Cache.optJSONObject(switchValue);
                        // switch subValue
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
                        cache.setHint(API1Cache.optString(switchValue, ""));
                        break;
                    case "attributes":
                        JSONArray attributes = API1Cache.optJSONArray(switchValue);
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
                        tmp = API1Cache.optString(switchValue, "");
                        if (tmp.length() > 0) {
                            // containsHtml liefert immer false
                            if (!tmp.contains("<")) {
                                tmp = tmp.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
                            }
                            cache.setLongDescription(tmp);
                        }
                        break;
                    case "shortDescription":
                        tmp = API1Cache.optString(switchValue, "");
                        if (tmp.length() > 0) {
                            // containsHtml liefert immer false
                            if (!tmp.contains("<")) {
                                tmp = tmp.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
                            }
                            cache.setShortDescription(tmp);
                        }
                        break;
                    case "additionalWaypoints":
                        addWayPoints(cache, API1Cache.optJSONArray(switchValue));
                        break;
                    case "userWaypoints":
                        addUserWayPoints(cache, API1Cache.optJSONArray(switchValue));
                        break;
                    default:
                }
            }
            ArrayList<LogEntry> Logs = createLogs(cache, API1Cache.optJSONArray("geocachelogs"));
            HashMap<String, URI> Spoilers = createImageList(API1Cache.optJSONArray("images"));
            // todo trackables
            // todo merge into a result class
            return cache;
        } catch (Exception e) {
            Log.err(log, "createGeoCache(JSONObject API1Cache)", e);
            return null;
        }
    }

    private static void addWayPoints(Cache cache, JSONArray wpts) {
        if (wpts != null) {
            for (int j = 0; j < wpts.length(); j++) {
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

    private static void addUserWayPoints(Cache cache, JSONArray wpts) {
        if (wpts != null) {
            for (int j = 0; j < wpts.length(); j++) {
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

    private static ArrayList<LogEntry> createLogs(Cache cache, JSONArray geocacheLogs) {
        ArrayList<LogEntry> logList = new ArrayList<>();
        if (geocacheLogs != null) {
            for (int ii = 0; ii < geocacheLogs.length(); ii++) {
                logList.add(createLog((JSONObject) geocacheLogs.get(ii), cache));
            }
        }
        return logList;
    }

    private static LogEntry createLog(JSONObject geocacheLog, Cache cache) {
        LogEntry logEntry = new LogEntry();
        logEntry.CacheId = cache.Id;
        logEntry.Comment = geocacheLog.optString("text", "");
        logEntry.Finder = getStringValue(geocacheLog, "owner", "username");
        String dateCreated = geocacheLog.optString("loggedDate", "");
        try {
            logEntry.Timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateCreated);
        } catch (Exception e) {
            logEntry.Timestamp = new Date();
        }
        logEntry.Type = LogTypes.parseString(geocacheLog.optString("type", ""));
        // todo logEntry.Id = geocacheLog.getInt("ID"); change database?
        // temporary solution
        String referenceCode = geocacheLog.optString("referenceCode", "");
        logEntry.Id = generateLogId(referenceCode);
        return logEntry;
    }

    private static long generateLogId(String referenceCode) {
        long result = 0;
        char[] dummy = referenceCode.substring(2).toCharArray(); // ohne "GL"
        byte[] byteDummy = new byte[8];
        for (int i = 0; i < 8; i++) {
            if (i < referenceCode.length() - 2)
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

    private static HashMap<String, URI> createImageList(JSONArray jImages) {
        HashMap<String, URI> list = new HashMap<>();
        if (jImages != null) {
            for (int ii = 0; ii < jImages.length(); ii++) {

                JSONObject jImage = (JSONObject) jImages.get(ii);
                String name = jImage.optString("description", "");
                String uri = jImage.optString("url", "");

                if (uri.length() > 0) {

                    // ignore log images (in API 1.0 logImages are no longer contained)
                    if (uri.contains("/cache/log")) {
                        continue;
                    }

                    // todo change this (the uri should be unique): Check for duplicate name = description must not be unique (e.g. empty)
                    if (list.containsKey(name)) {
                        // todo why only 50 images with equal description?
                        for (int nr = 1; nr < 50; nr++) {
                            if (!list.containsKey(name + "_" + nr)) {
                                name = name + "_" + nr;
                                break;
                            }
                        }
                    }

                    try {
                        URI u = new URI(uri);
                        list.put(name, u);
                    } catch (Exception ignored) {
                    }

                }

            }
        }
        return list;
    }

    private static CacheTypes CacheTypeFromID(int id) {
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

    private static String getStringValue(JSONObject jObject, String from, String KeyName) {
        JSONObject fromObject = jObject.optJSONObject(from);
        if (fromObject != null) {
            return fromObject.optString(KeyName, "");
        } else {
            return "";
        }
    }

    private enum MemberShipTypes {Unknown, Basic, Charter, Premium}

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
        public String username;
        public MemberShipTypes memberShipType;
        public int findCount;
        // geocacheLimits
        public int remaining;
        public int renainingLite;
        public int remainingTime;
        public int renainingLiteTime;

        public UserInfos() {
            username = "";
            memberShipType = MemberShipTypes.Unknown;
            findCount = 0;
            remaining = -1;
            renainingLite = -1;
            remainingTime = -1;
            renainingLiteTime = -1;
        }
    }

    public static class Query {
        private static final String LiteFields = "referenceCode,favoritePoints,userData,name,difficulty,terrain,placedDate,geocacheType.id,geocacheSize.id,location,postedCoordinates,status,owner.username,ownerAlias";
        private static final String NotLiteFields = "hints,attributes,longDescription,shortDescription,additionalWaypoints,userWaypoints";
        private static final String StatusFields = "referenceCode,favoritePoints,status,trackableCount,longDescription";
        private StringBuilder qString;
        private StringBuilder fieldsString;
        private StringBuilder expandString;
        private int maxToFetch;

        public Query() {
            qString = new StringBuilder();
            fieldsString = new StringBuilder();
            expandString = new StringBuilder();
            int maxToFetch = 1;
        }

        public Query setMaxToFetch(int maxToFetch) {
            this.maxToFetch = maxToFetch;
            return this;
        }

        public Query serchInCircleOf100Miles(Coordinate center) {
            addSearchFilter("location:[" + center.latitude + "," + center.longitude + "]"); // == +radius:100mi
            return this;
        }

        public Query serchInCircle(Coordinate center, int radiusInMeters) {
            if (radiusInMeters > 160934) radiusInMeters = 160934; // max 100 miles
            addSearchFilter("location:[" + center.latitude + "," + center.longitude + "]+radius:" + radiusInMeters + "m");
            return this;
        }

        public Query searchForTitle(String containsIgnoreCase) {
            addSearchFilter("name:" + containsIgnoreCase);
            return this;
        }

        public Query searchForOwner(String userName) {
            addSearchFilter("hby:" + userName);
            return this;
        }

        public Query excludeOwn() {
            addSearchFilter("hby:" + "not(" + fetchMyUserInfos().username + ")");
            return this;
        }

        public Query excludeFinds() {
            addSearchFilter("fby:" + "not(" + fetchMyUserInfos().username + ")");
            return this;
        }

        public Query onlyActiveGeoCaches() {
            addSearchFilter("ia:true");
            return this;
        }

        private void addSearchFilter(String filter) {
            qString.append('+').append(filter);
        }

        public Query resultWithLiteFields() {
            addResultField(LiteFields);
            return this;
        }

        public Query resultWithFullFields() {
            addResultField(LiteFields);
            addResultField(NotLiteFields);
            return this;
        }

        public Query resultForStatusFields() {
            addResultField(StatusFields);
            return this;
        }

        private void addResultField(String field) {
            fieldsString.append(",").append(field);
        }

        public Query addExpandField(String field, int count) {
            expandString.append(",").append(field + ":" + count);
            return this;
        }

        public Query resultWithLogs(int count) {
            expandString.append(",").append("geocachelogs:" + count);
            return this;
        }

        public Query resultWithImages(int count) {
            expandString.append(",").append("images:" + count);
            return this;
        }

        public Query resultWithTrackables(int count) {
            expandString.append(",").append("trackables:" + count);
            return this;
        }

        public boolean isSearch() {
            return qString.length() > 0;
        }

        public boolean containsOnlyLiteFields(ArrayList<String> fields) {
            boolean onlyLiteFields = true;
            for (String s : fields) {
                if (NotLiteFields.contains(s)) {
                    onlyLiteFields = false;
                    break;
                }
            }
            return onlyLiteFields;
        }

        public ArrayList<String> getFields() {
            ArrayList<String> result = new ArrayList<>();
            String fs = fieldsString.toString();
            if (fs.length() > 0)
                result.addAll(Arrays.asList(fs.substring(1).split(",")));
            String es = expandString.toString();
            if (es.length() > 0) {
                for (String s : es.substring(1).split(",")) {
                    result.add(s.split(":")[0]);
                }
            }
            return result;
        }

        public Request putQuery(Request r) {
            String qs = qString.toString();
            if (qs.length() > 0) r.param("q", qs.substring(1)); // .replace(" ", "%20"));
            String fs = fieldsString.toString();
            if (fs.length() > 0) r.param("fields", fs.substring(1));
            String es = expandString.toString();
            if (es.length() > 0) r.param("expand", es.substring(1));
            return r;
        }
    }
}
