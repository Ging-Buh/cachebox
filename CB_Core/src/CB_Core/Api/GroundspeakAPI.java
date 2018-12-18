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
import CB_Core.Import.DescriptionImageGrabber;
import CB_Core.Types.*;
import CB_Locator.Coordinate;
import CB_Locator.Map.Descriptor;
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import CB_Utils.Util.FileIO;
import CB_Utils.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;

import static CB_Core.Import.DescriptionImageGrabber.Segmentize;
import static CB_Core.Types.Cache.IS_FULL;
import static CB_Core.Types.Cache.IS_LITE;

public class GroundspeakAPI {
    public static final int OK = 0;
    public static final int ERROR = -1;
    private static final String log = "GroundspeakAPI";
    public static String LastAPIError = "";
    public static int APIError;

    private static UserInfos me;
    private static Webb netz;
    private static long startTs;
    private static long lastTimeLimitFetched;
    private static int nrOfApiCalls;
    private static int retryCount;
    private static boolean active = false;

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

    // API 1.0 see https://api.groundspeak.com/documentation and https://api.groundspeak.com/api-docs/index

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
                    // 401 = Not Authorized
                    // 403 = limit exceeded: want to get more caches than remain (lite / full) : get limits for good message
                    // 404 = Not Found
                    try {
                        ej = new JSONObject(new JSONTokener((String) re.getErrorBody()));
                        if (ej != null) {
                            LastAPIError = ej.optString("errorMessage", "" + APIError);
                        } else {
                            LastAPIError = ex.getLocalizedMessage();
                        }
                    } catch (Exception exc) {
                        LastAPIError = ex.getLocalizedMessage();
                        Log.err(log, APIError + ":" + LastAPIError);
                    }
                }
            } else {
                // re == null
                APIError = ERROR;
                LastAPIError = ex.getLocalizedMessage();
                Log.err(log, APIError + ":" + LastAPIError);
            }
        } else {
            // no WebbException
            APIError = ERROR;
            LastAPIError = ex.getLocalizedMessage();
            Log.err(log, APIError + ":" + LastAPIError, ex);
        }
        return retryCount > 0;
    }

    public static ArrayList<GeoCacheRelated> searchGeoCaches(Query query) {
        // fetch/update geocaches consumes a lite or full cache
        ArrayList<GeoCacheRelated> fetchResults = new ArrayList<>();
        Log.debug(log, "searchGeoCaches start " + query.toString());
        try {

            ArrayList<String> fields = query.getFields();
            boolean onlyLiteFields = query.containsOnlyLiteFields(fields);
            int maxCachesPerHttpCall = (onlyLiteFields ? 50 : 5); // API 1.0 says may take 50, but not in what time, and with 10 I got out of memory
            if (query.descriptor == null) {
                if (onlyLiteFields) {
                    fetchMyCacheLimits();
                    if (me.remainingLite < me.remaining) {
                        onlyLiteFields = false;
                    }
                }
            } else {
                // for Live on map
                maxCachesPerHttpCall = 50;
            }
            int skip = 0;
            int take = Math.min(query.maxToFetch, maxCachesPerHttpCall);

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

                        if (query.descriptor != null) {
                            writeSearchResultsToDisc(fetchedCaches, query.descriptor);
                        }
                        fetchResults.addAll(getGeoCacheRelateds(fetchedCaches, fields, null));

                        if (fetchedCaches.length() < take || take < maxCachesPerHttpCall) {
                            take = 0; // we got all
                        } else {
                            skip = skip + take;
                        }

                    } catch (Exception ex) {
                        doRetry = retry(ex);
                        if (!doRetry) {
                            Log.debug(log, "searchGeoCaches with exception: " + LastAPIError);
                            fetchMyCacheLimits();
                            return fetchResults;
                        }
                    }
                }
                while (doRetry);
            } while (take > 0 && skip < query.maxToFetch);

        } catch (Exception e) {
            APIError = ERROR;
            LastAPIError = e.getLocalizedMessage();
            Log.err(log, "searchGeoCaches", e);
            return fetchResults;
        }
        Log.debug(log, "searchGeoCaches ready with " + fetchResults.size() + " Caches.");
        fetchMyCacheLimits();
        return fetchResults;
    }

    private static void writeSearchResultsToDisc(JSONArray fetchedCaches, Descriptor descriptor) {
        Writer writer = null;
        try {
            String Path = descriptor.getLocalCachePath(LiveMapQue.LIVE_CACHE_NAME) + LiveMapQue.LIVE_CACHE_EXTENSION;
            if (FileIO.createDirectory(Path)) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Path), "utf-8"));
                writer.write(fetchedCaches.toString());
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

    public static ArrayList<GeoCacheRelated> updateStatusOfGeoCaches(ArrayList<Cache> caches) {
        // fetch/update geocaches consumes a lite or full cache
        Query query = new Query().resultForStatusFields().setMaxToFetch(caches.size());
        return updateGeoCaches(query, caches);
    }

    public static ArrayList<GeoCacheRelated> updateGeoCache(Cache cache) {
        ArrayList<Cache> caches = new ArrayList<>();
        caches.add(cache);
        // not .onlyActiveGeoCaches() : must be updated to the latest status
        Query query = new Query()
                .resultWithFullFields()
                .resultWithLogs(30)
                //.resultWithImages(30) // todo maybe remove, cause not used from DB
                ;
        return updateGeoCaches(query, caches);
    }

    public static ArrayList<GeoCacheRelated> fetchGeoCache(Query query, String GcCode) {
        Cache cache = new Cache(true);
        cache.setGcCode(GcCode);
        ArrayList<Cache> caches = new ArrayList<>();
        caches.add(cache);
        return updateGeoCaches(query, caches);
    }

    public static ArrayList<GeoCacheRelated> fetchGeoCaches(Query query, String CacheCodes) {
        ArrayList<Cache> caches = new ArrayList<>();
        for (String GcCode : CacheCodes.split(",")) {
            Cache cache = new Cache(true);
            cache.setGcCode(GcCode);
            caches.add(cache);
        }
        return updateGeoCaches(query, caches);
    }

    public static ArrayList<GeoCacheRelated> updateGeoCaches(Query query, ArrayList<Cache> caches) {
        // fetch/update geocaches consumes a lite or full cache
        ArrayList<GeoCacheRelated> fetchResults = new ArrayList<>();
        try {

            ArrayList<String> fields = query.getFields();
            boolean onlyLiteFields = query.containsOnlyLiteFields(fields);
            int maxCachesPerHttpCall = (onlyLiteFields ? 50 : 5); // API 1.0 says may take 50, but not in what time, and with 10 Full I got out of memory
            if (onlyLiteFields) {
                fetchMyCacheLimits();
                if (me.remainingLite < me.remaining) {
                    onlyLiteFields = false;
                }
            }

            // just to simplify splitting into blocks of max 50 caches
            Cache[] arrayOfCaches = new Cache[caches.size()];
            caches.toArray(arrayOfCaches);

            int skip = 0;
            int take = Math.min(query.maxToFetch, maxCachesPerHttpCall);

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
                if (took == 0) return fetchResults; // no gc in the block
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

                        fetchResults.addAll(getGeoCacheRelateds(r.getBody(), fields, mapOfCaches));

                    } catch (Exception ex) {
                        doRetry = retry(ex);
                        if (!doRetry) {
                            if (APIError == 404) {
                                // one bad GCCode (not starting with GC) causes Error 404: will hopefully be changed in an update after 11.26.2018
                                // a not existing GCCode seems to be ignored, what is ok
                                doRetry = false;
                                Log.err(log, "searchGeoCaches - skipped block cause: " + LastAPIError);
                            } else {
                                fetchMyCacheLimits();
                                return fetchResults;
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
            return fetchResults;
        }
        fetchMyCacheLimits();
        return fetchResults;
    }

    public static ArrayList<GeoCacheRelated> getGeoCacheRelateds(JSONArray fetchedCaches, ArrayList<String> fields, Map<String, Cache> mapOfCaches) {
        ArrayList<GeoCacheRelated> fetchResults = new ArrayList<>();
        for (int ii = 0; ii < fetchedCaches.length(); ii++) {
            JSONObject fetchedCache = (JSONObject) fetchedCaches.get(ii);
            Cache originalCache;
            if (mapOfCaches == null) {
                originalCache = null;
            } else {
                originalCache = mapOfCaches.get(fetchedCache.optString("referenceCode"));
            }
            Cache cache = createGeoCache(fetchedCache, fields, originalCache);
            if (cache != null) {
                ArrayList<LogEntry> logs = createLogs(cache, fetchedCache.optJSONArray("geocacheLogs"));
                ArrayList<ImageEntry> images = createImageList(fetchedCache.optJSONArray("images"), cache.getGcCode());
                images = addDescriptionImageList(images, cache);
                fetchResults.add(new GeoCacheRelated(cache, logs, images));
            }
        }
        return fetchResults;
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
                                    pq.DateLastGenerated = DateFromString(dateCreated);
                                } catch (Exception exc) {
                                    Log.err(log, "fetchPocketQueryList/DateLastGenerated", exc);
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

    public static ArrayList<LogEntry> fetchGeoCacheLogs(Cache cache, boolean all, ICancelRunnable cancelRun) {
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
                    Log.err(log, "fetchGeoCacheLogs", e);
                }
            }
            while (doRetry);
            // die nächsten Logs laden
            start += count;
        }
        APIError = ERROR;
        LastAPIError = "Loading Logs canceled";
        return (logList);
    }

    public static ArrayList<ImageEntry> downloadImageListForGeocache(String cacheCode) {

        ArrayList<ImageEntry> imageEntries = new ArrayList<>();
        LastAPIError = "";

        if (cacheCode == null || isAccessTokenInvalid()) {
            APIError = ERROR;
            return imageEntries;
        }

        int skip = 0;
        int take = 50;

        // todo implement loop for more than 50 imagelinks (if it ever will be necessary)
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
                // is only, if implemented fetch of more than 50 images (loop)
                imageEntries.addAll(createImageList(r.getBody(), cacheCode));

                return imageEntries;

            } catch (Exception ex) {
                if (!retry(ex)) {
                    return imageEntries;
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

    public static boolean isDownloadLimitExceeded() {
        // do'nt want to access Web for this info (GL.postAsync)
        if (me == null) return false;
        return me.remaining <= 0 && me.remainingLite <= 0;
    }

    public static UserInfos fetchMyUserInfos() {
        if (me == null || me.memberShipType == MemberShipTypes.Unknown) {
            if (!active) {
                active = true;
                me = fetchUserInfos("me");
                if (me.memberShipType == MemberShipTypes.Unknown) {
                    me.findCount = -1;
                    // we need a new AccessToken
                    // API_ErrorEventHandlerList.handleApiKeyError(API_ErrorEventHandlerList.API_ERROR.INVALID);
                    Log.err(log, "fetchMyUserInfos: Need a new Access Token");
                }
                active = false;
            }
        }
        return me;
    }

    public static void fetchMyCacheLimits() {
        if (System.currentTimeMillis() - lastTimeLimitFetched > 60000) {
            // update one time per minute may be enough
            me = fetchUserInfos("me");
            lastTimeLimitFetched = System.currentTimeMillis();
        }
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
                    ui.remainingLite = geocacheLimits.optInt("liteCallsRemaining", -1);
                    ui.remainingTime = geocacheLimits.optInt("fullCallsSecondsToLive", -1);
                    ui.remainingLiteTime = geocacheLimits.optInt("liteCallsSecondsToLive", -1);
                }
                return ui;
            } catch (Exception ex) {
                if (!retry(ex)) {
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

    static String getUrl(int version, String command) {
        String ApiUrl = "https://api.groundspeak.com/";
        String StagingApiUrl = "https://staging.api.groundspeak.com/";
        String mPath;
        switch (version) {
            case 0:
                mPath = "LiveV6/geocaching.svc/";
                break;
            case 1:
                mPath = "v1.0/";
                break;
            default:
                mPath = "";
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
        if (cache == null)
            cache = new Cache(true);
        cache.setAttributesPositive(new DLong(0, 0));
        cache.setAttributesNegative(new DLong(0, 0));
        cache.setApiStatus(IS_LITE);
        String tmp;
        try {
            for (String field : fields) {
                int withDot = field.indexOf(".");
                String switchValue;
                String subValue; // no need till now
                if (withDot > 0) {
                    switchValue = field.substring(0, withDot);
                    subValue = field.substring(withDot + 1);
                } else {
                    switchValue = field;
                }
                switch (switchValue) {
                    case "referenceCode":
                        cache.setGcCode(API1Cache.optString(field, ""));
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
                    case "postedCoordinates":
                        // handled within userdata
                        break;
                    case "userData":
                        JSONObject userData = API1Cache.optJSONObject(switchValue);
                        // switch subValue
                        if (userData != null) {
                            // foundDate
                            cache.setFound(userData.optString("foundDate", "").length() != 0);
                            // correctedCoordinates
                            JSONObject correctedCoordinates = userData.optJSONObject("correctedCoordinates");
                            if (correctedCoordinates != null) {
                                cache.Pos = new Coordinate(correctedCoordinates.optDouble("latitude", 0), correctedCoordinates.optDouble("longitude", 0));
                                cache.setHasCorrectedCoordinates(true);
                            } else {
                                JSONObject postedCoordinates = API1Cache.optJSONObject("postedCoordinates");
                                if (postedCoordinates != null) {
                                    cache.Pos = new Coordinate(postedCoordinates.optDouble("latitude", 0), postedCoordinates.optDouble("longitude", 0));
                                } else {
                                    cache.Pos = new Coordinate();
                                }
                            }
                            cache.setTmpNote(userData.optString("note", ""));
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
                            cache.setApiStatus(IS_FULL);
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
                            cache.setApiStatus(IS_FULL); // got a cache without LongDescription
                        }
                        break;
                    case "additionalWaypoints":
                        addWayPoints(cache, API1Cache.optJSONArray(switchValue));
                        break;
                    case "userWaypoints":
                        addUserWayPoints(cache, API1Cache.optJSONArray(switchValue));
                        break;
                    default:
                        // Remind the programmer
                        Log.err(log, "createCache: " + switchValue + " not handled");
                }
            }

            return cache;
        } catch (Exception e) {
            Log.err(log, "createGeoCache(JSONObject API1Cache)", e);
            return null;
        }
    }

    private static void addWayPoints(Cache cache, JSONArray wpts) {
        if (wpts != null) {
            if (cache.waypoints != null) {
                cache.waypoints.clear();
                // no merging of waypoints here
            }
            else {
                cache.waypoints = new CB_List<>();
            }
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
        String referenceCode = geocacheLog.optString("referenceCode", "");
        logEntry.Id = generateLogId(referenceCode);
        return logEntry;
    }

    private static long generateLogId(String referenceCode) {
        referenceCode = referenceCode.substring(2); // ohne "GL"
        if (referenceCode.charAt(0) > 'F' || referenceCode.length() > 6) {
            return Base31(referenceCode);
        } else {
            return Base16(referenceCode);
        }
    }

    private static long Base16(String s) {
        String base16chars = "0123456789ABCDEF";
        long r = 0;
        long f = 1;
        for (int i = s.length() - 1; i >= 0; i--) {
            r = r + base16chars.indexOf(s.charAt(i)) * f;
            f = f * 16;
        }
        return r;
    }

    private static long Base31(String s) {
        String base31chars = "0123456789ABCDEFGHJKMNPQRTVWXYZ";
        long r = -411120;
        long f = 1;
        for (int i = s.length() - 1; i >= 0; i--) {
            r = r + base31chars.indexOf(s.charAt(i)) * f;
            f = f * 31;
        }
        return r;
    }

    private static ArrayList<ImageEntry> createImageList(JSONArray jImages, String GcCode) {
        ArrayList<ImageEntry> imageEntries = new ArrayList<>();

        if (jImages != null) {
            for (int ii = 0; ii < jImages.length(); ii++) {

                JSONObject jImage = (JSONObject) jImages.get(ii);
                String Description = jImage.optString("description", "");
                String uri = jImage.optString("url", "");

                if (uri.length() > 0) {
                    // ignore log images (in API 1.0 logImages are no longer contained)
                    if (!uri.contains("/cache/log")) {
                        ImageEntry imageEntry = new ImageEntry();
                        imageEntry.CacheId = Cache.GenerateCacheId(GcCode);
                        imageEntry.Description = Description;
                        imageEntry.GcCode = GcCode;
                        imageEntry.ImageUrl = uri.replace("img.geocaching.com/gc/cache", "img.geocaching.com/cache");

                        imageEntry.IsCacheImage = false; // todo check is spoiler or what is it used for
                        imageEntry.LocalPath = ""; // create at download / read from DB
                        // imageEntry.LocalPath =  DescriptionImageGrabber.BuildDescriptionImageFilename(GcCode, URI.create(uri));
                        imageEntry.Name = ""; // does not exist in API 1.0
                        imageEntries.add(imageEntry);
                    }
                }

            }
        }
        return imageEntries;
    }

    private static ArrayList<ImageEntry> addDescriptionImageList(ArrayList<ImageEntry> imageList, Cache cache) {

        ArrayList<String> DescriptionImages = getDescriptionsImages(cache);
        for (String url : DescriptionImages) {
            // do not take those from spoilers or
            boolean isNotInImageList = true;
            for (ImageEntry im : imageList) {
                if (im.ImageUrl.equalsIgnoreCase(url)) {
                    isNotInImageList = false;
                    break;
                }
            }
            if (isNotInImageList) {
                ImageEntry imageEntry = new ImageEntry();
                imageEntry.CacheId = cache.Id;
                imageEntry.GcCode = cache.getGcCode();
                imageEntry.Name = "";
                imageEntry.Description = url.substring(url.lastIndexOf("/") + 1);
                imageEntry.ImageUrl = url;
                imageEntry.IsCacheImage = true;
                imageEntry.LocalPath = ""; // create at download / read from DB
                imageList.add(imageEntry);
            }
        }
        return imageList;
    }

    private static ArrayList<String> getDescriptionsImages(Cache cache) {

        ArrayList<String> images = new ArrayList<>();

        URI baseUri;
        try {
            baseUri = URI.create(cache.getUrl());
        } catch (Exception exc) {
            baseUri = null;
        }

        if (baseUri == null) {
            cache.setUrl("http://www.geocaching.com/seek/cache_details.aspx?wp=" + cache.getGcCode());
            try {
                baseUri = URI.create(cache.getUrl());
            } catch (Exception exc) {
                return images;
            }
        }

        CB_List<DescriptionImageGrabber.Segment> imgTags = Segmentize(cache.getShortDescription(), "<img", ">");
        imgTags.addAll(Segmentize(cache.getLongDescription(), "<img", ">"));

        for (int i = 0, n = imgTags.size(); i < n; i++) {
            DescriptionImageGrabber.Segment img = imgTags.get(i);
            int srcStart = -1;
            int srcEnd = -1;
            int srcIdx = img.text.toLowerCase().indexOf("src=");
            if (srcIdx != -1)
                srcStart = img.text.indexOf('"', srcIdx + 4);
            if (srcStart != -1)
                srcEnd = img.text.indexOf('"', srcStart + 1);

            if (srcIdx != -1 && srcStart != -1 && srcEnd != -1) {
                String src = img.text.substring(srcStart + 1, srcEnd);
                try {
                    URI imgUri = URI.create(src);

                    images.add(imgUri.toString());

                } catch (Exception exc) {
                }
            }
        }
        return images;
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
            Log.err(log, "DateFromString", e);
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
        public int remainingLite;
        public int remainingTime;
        public int remainingLiteTime;

        public UserInfos() {
            username = "";
            memberShipType = MemberShipTypes.Unknown;
            findCount = 0;
            remaining = -1;
            remainingLite = -1;
            remainingTime = -1;
            remainingLiteTime = -1;
        }
    }

    public static class GeoCacheRelated {
        public Cache cache;
        public ArrayList<LogEntry> logs;
        public ArrayList<ImageEntry> images;
        // trackables

        public GeoCacheRelated(Cache cache, ArrayList<LogEntry> logs, ArrayList<ImageEntry> images) {
            this.cache = cache;
            this.logs = logs;
            this.images = images;
        }
    }

    public static class Query {
        private static final String LiteFields = "referenceCode,favoritePoints,userData,name,difficulty,terrain,placedDate,geocacheType.id,geocacheSize.id,location,postedCoordinates,status,owner.username,ownerAlias";
        private static final String NotLiteFields = "hints,attributes,longDescription,shortDescription,additionalWaypoints,userWaypoints";
        private static final String StatusFields = "referenceCode,favoritePoints,status,trackableCount";
        private StringBuilder qString;
        private StringBuilder fieldsString;
        private StringBuilder expandString;
        private int maxToFetch;
        private Descriptor descriptor;

        public Query() {
            qString = new StringBuilder();
            fieldsString = new StringBuilder();
            expandString = new StringBuilder();
            maxToFetch = 1;
            descriptor = null;
        }

        @Override
        public String toString() {
            return qString.toString();
        }

        public Query setMaxToFetch(int maxToFetch) {
            this.maxToFetch = maxToFetch;
            return this;
        }

        public Query searchInCircleOf100Miles(Coordinate center) {
            addSearchFilter("location:[" + center.latitude + "," + center.longitude + "]"); // == +radius:100mi
            return this;
        }

        public Query searchInCircle(Coordinate center, int radiusInMeters) {
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

        public Query publishedDate(Date date, String when) {
            String before = "";
            String after = "";
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
            if (when == "<=") {
                before = f.format(date);
            } else if (when == ">=") {
                after = f.format(date);
            } else if (when == "=") {
                before = f.format(date);
                after = before;
            }
            addSearchFilter("pd:[" + after + "," + before + "]"); // inclusive
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

        public Descriptor getDescriptor() {
            return descriptor;
        }

        public Query setDescriptor(Descriptor descriptor) {
            this.descriptor = descriptor;
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
