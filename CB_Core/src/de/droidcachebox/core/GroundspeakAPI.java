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

import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.database.*;
import de.droidcachebox.ex_import.DescriptionImageGrabber;
import de.droidcachebox.locator.Coordinate;
import de.droidcachebox.locator.map.Descriptor;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.CB_List;
import de.droidcachebox.utils.DLong;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.http.*;
import de.droidcachebox.utils.log.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static de.droidcachebox.database.Cache.IS_FULL;
import static de.droidcachebox.database.Cache.IS_LITE;
import static de.droidcachebox.ex_import.DescriptionImageGrabber.Segmentize;
import static java.lang.Thread.sleep;

public class GroundspeakAPI {
    public static final int OK = 0;
    public static final int ERROR = -1;
    private static final String log = "GroundspeakAPI";
    public static String LastAPIError = "";
    public static int APIError;
    public static String logReferenceCode = "";
    private static UserInfos me;
    private static Webb netz;
    private static long startTs;
    private static long lastTimeLimitFetched;
    private static int nrOfApiCalls;
    private static int retryCount;
    private static boolean active = false;

    public static Webb getNetz() {
        if (netz == null) {
            netz = Webb.create();
            netz.setDefaultHeader(Webb.HDR_AUTHORIZATION, "bearer " + getSettingsAccessToken());
            Webb.setReadTimeout(CB_Core_Settings.socket_timeout.getValue());
            Webb.setConnectTimeout(CB_Core_Settings.connection_timeout.getValue());
            startTs = System.currentTimeMillis();
            nrOfApiCalls = 0;
            retryCount = 0;
            active = false;
        }

        if (System.currentTimeMillis() - startTs > 60000) {
            // reset nrOfApiCalls after one minute
            // perhaps can avoid retry for 429 by checking nrOfApiCalls. ( not implemented yet )
            startTs = System.currentTimeMillis();
            nrOfApiCalls = 0;
            retryCount = 0;
        }

        nrOfApiCalls++;
        APIError = 0;
        return netz;
    }

    // API 1.0 see https://api.groundspeak.com/documentation and https://api.groundspeak.com/api-docs/index

    public static void setAuthorization() {
        getNetz().setDefaultHeader(Webb.HDR_AUTHORIZATION, "bearer " + getSettingsAccessToken());
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
                                sleep(ta);
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
                                .ensureSuccess()
                        ).asJsonArray();

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
            String path = LiveMapQue.getLocalCachePath(descriptor);
            if (FileIO.createDirectory(path)) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));
                writer.write(fetchedCaches.toString());
            }
        } catch (IOException ex) {
            // report
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception ignored) {
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
                //.resultWithImages(30) // todo maybe remove, cause not used from DB
                ;
        if (CB_Core_Settings.numberOfLogs.getValue() > 0) {
            query.resultWithLogs(CB_Core_Settings.numberOfLogs.getValue());
        }
        return updateGeoCaches(query, caches);
    }

    public static ArrayList<GeoCacheRelated> fetchGeoCache(Query query, String GcCode) {
        Cache cache = new Cache(true);
        cache.setGeoCacheCode(GcCode);
        ArrayList<Cache> caches = new ArrayList<>();
        caches.add(cache);
        return updateGeoCaches(query, caches);
    }

    public static ArrayList<GeoCacheRelated> fetchGeoCaches(Query query, String CacheCodes) {
        ArrayList<Cache> caches = new ArrayList<>();
        for (String GcCode : CacheCodes.split(",")) {
            Cache cache = new Cache(true);
            cache.setGeoCacheCode(GcCode);
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
                    if (cache.getGeoCacheCode().toLowerCase().startsWith("gc")) {
                        mapOfCaches.put(cache.getGeoCacheCode(), cache);
                        CacheCodes.append(",").append(cache.getGeoCacheCode());
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
                ArrayList<ImageEntry> images = createImageList(fetchedCache.optJSONArray("images"), cache.getGeoCacheCode(), false);
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
                                pq.name = jPQ.optString("name", "");
                                try {
                                    String dateCreated = jPQ.optString("lastUpdatedDateUtc", "");
                                    pq.lastGenerated = DateFromString(dateCreated);
                                } catch (Exception exc) {
                                    Log.err(log, "fetchPocketQueryList/lastGenerated", exc);
                                    pq.lastGenerated = new Date();
                                }
                                pq.cacheCount = jPQ.getInt("count");
                                pq.sizeMB = -1;
                                pq.doDownload = false;
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

    public static void fetchPocketQuery(PQ pocketQuery, String pqFolder) {
        InputStream inStream = null;
        BufferedOutputStream outStream = null;
        try {
            inStream = getNetz()
                    .get(getUrl(1, "lists/" + pocketQuery.GUID + "/geocaches/zipped"))
                    .ensureSuccess()
                    .asStream()
                    .getBody();
            String dateString = new SimpleDateFormat("yyyyMMddHHmmss").format(pocketQuery.lastGenerated);
            String local = pqFolder + "/" + pocketQuery.GUID + ".zip";
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

    public static int UploadDraftOrLog(String gcCode, int wptLogTypeId, Date dateLogged, String note, boolean directLog) {
        logReferenceCode = "";
        if (isAccessTokenInvalid()) return ERROR; // should be checked in advance

        try {
            if (directLog) {
                if (note.length() == 0) {
                    LastAPIError = Translation.get("emptyLog");
                    return ERROR;
                }
                Log.debug(log, "is Log");
                LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
                params.put("fields", "owner.username,loggedDate,text,type,referenceCode");
                JSONObject geocacheLog = getNetz()
                        .post(getUrl(1, "geocachelogs") + "?" + WebbUtils.queryString(params))
                        .body(new JSONObject()
                                .put("geocacheCode", gcCode)
                                .put("type", wptLogTypeId)
                                .put("loggedDate", getDate(dateLogged))
                                .put("text", prepareNote(note))
                        )
                        .ensureSuccess()
                        .asJsonObject()
                        .getBody();
                long cacheId = Cache.generateCacheId(gcCode);
                // Cache cache = new CacheDAO().getFromDbByCacheId(cacheId);
                LogEntry logEntry = createLog(geocacheLog, cacheId);
                new LogDAO().WriteToDatabase(logEntry);
                // logReferenceCode is return value
                logReferenceCode = geocacheLog.optString("referenceCode", ""); // as return value
            } else {
                Log.debug(log, "is draft"); //  + getUTCDate(dateLogged)
                getNetz()
                        .post(getUrl(1, "logdrafts"))
                        .body(new JSONObject()
                                .put("geocacheCode", gcCode)
                                .put("logType", wptLogTypeId)
                                .put("loggedDate", getDate(dateLogged))
                                .put("note", prepareNote(note))
                        )
                        .ensureSuccess()
                        .asVoid();
            }
            LastAPIError = "";
            Log.info(log, "UploadDraftOrLog done: " + gcCode);
            return OK;
        } catch (Exception e) {
            retry(e);
            Log.err(log, "UploadDraftOrLog geocacheCode: " + gcCode + " logType: " + wptLogTypeId + ".\n" + LastAPIError, e);
            return ERROR;
        }
    }

    public static ArrayList<LogEntry> fetchGeoCacheLogs(Cache cache, boolean all, ICancelRunnable cancelRun) {
        ArrayList<LogEntry> logList = new ArrayList<>();

        LinkedList<String> friendList = new LinkedList<>();
        if (!all) {
            String friends = CB_Core_Settings.friends.getValue().replace(", ", "|").replace(",", "|");
            for (String f : friends.split("\\|")) {
                friendList.add(f.toLowerCase(Locale.US));
            }
        }

        int start = 0;
        int count = 50;

        while (!cancelRun.doCancel())
        // Schleife, solange bis entweder keine Logs mehr geladen werden oder bis Logs aller Freunde geladen sind.
        {
            boolean doRetry;
            do {
                doRetry = false;
                try {
                    JSONArray geocacheLogs = getNetz()
                            .get(getUrl(1, "geocaches/" + cache.getGeoCacheCode() + "/geocachelogs"))
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

                        logList.add(createLog(geocacheLog, cache.generatedId));
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

    public static ArrayList<ImageEntry> downloadImageListForGeocache(String cacheCode, boolean withLogImages) {

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
                        .param("fields", "url,description,referenceCode")
                        .param("skip", skip)
                        .param("take", take)
                        .ensureSuccess()
                        .asJsonArray();

                retryCount = 0;
                // is only, if implemented fetch of more than 50 images (loop)
                imageEntries.addAll(createImageList(r.getBody(), cacheCode, withLogImages));

                return imageEntries;

            } catch (Exception ex) {
                if (!retry(ex)) {
                    return imageEntries;
                }
            }
        } while (true);
    }

    public static TBList downloadUsersTrackables() {
        TBList tbList = new TBList();
        if (isAccessTokenInvalid()) return tbList;
        LastAPIError = "";
        int skip = 0;
        int take = 50;

        try {
            boolean ready;
            do {
                JSONArray jTrackables = getNetz()
                        .get(getUrl(1, "trackables"))
                        .param("fields", "referenceCode,trackingNumber,iconUrl,name,goal,description,releasedDate,owner.username,holder.username,currentGeocacheCode,type,inHolderCollection")
                        .param("skip", skip)
                        .param("take", take)
                        .ensureSuccess().asJsonArray().getBody();

                for (int ii = 0; ii < jTrackables.length(); ii++) {
                    JSONObject jTrackable = (JSONObject) jTrackables.get(ii);
                    if (!jTrackable.optBoolean("inHolderCollection", false)) {
                        Trackable tb = createTrackable(jTrackable);
                        Log.debug(log, "downloadUsersTrackables: add " + tb.getName());
                        tbList.add(tb);
                    } else {
                        Log.debug(log, "downloadUsersTrackables: not in HolderCollection" + jTrackable.optString("name", ""));
                    }
                }

                ready = jTrackables.length() < take;
                skip = skip + take;
            }
            while (!ready);
            Log.info(log, "downloadUsersTrackables done \n");
            return tbList;
        } catch (Exception ex) {
            retry(ex);
            Log.err(log, "downloadUsersTrackables " + LastAPIError, ex);
            return tbList;
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

            if (!tb.getTbCode().toLowerCase().equals(TBCode.toLowerCase())) {
                // fetched by TrackingCode, the result for trackingcode is always empty, except for owner
                tb.setTrackingCode(TBCode);
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
        return uploadTrackableLog(TB.getTbCode(), TB.getTrackingCode(), cacheCode, LogTypeId, dateLogged, note);
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
                            .put("loggedDate", getDate(dateLogged))
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
            LastAPIError += "\n APIKey: " + getSettingsAccessToken();
            LastAPIError += "\n trackingNumber: " + TrackingNummer;
            LastAPIError += "\n trackableCode: " + TBCode;
            LastAPIError += "\n geocacheCode: " + cacheCode;
            LastAPIError += "\n loggedDate: " + getDate(dateLogged);
            LastAPIError += "\n text: " + prepareNote(note);
            LastAPIError += "\n typeId: " + LogTypeId;
            Log.err(log, "uploadTrackableLog \n" + LastAPIError, ex);
            return ERROR;
        }
    }

    public static int AddToWatchList(String gcCode) {
        if (!isAccessTokenInvalid()) {
            try {
                getNetz().post(getUrl(1, "lists/" + fetchWatchListCode() + "/geocaches"))
                        .body(new JSONObject().put("referenceCode", gcCode))
                        .ensureSuccess()
                        .asVoid()
                ;
            } catch (Exception ex) {
                retry(ex);
                return ERROR;
            }
            return OK;
        }
        return ERROR;
    }

    public static int RemoveFromWatchList(String gcCode) {
        if (!isAccessTokenInvalid()) {
            try {
                getNetz().delete(getUrl(1, "lists/" + fetchWatchListCode() + "/geocaches/" + gcCode)).ensureSuccess().asVoid();
            } catch (Exception ex) {
                retry(ex);
                return ERROR;
            }
            return OK;
        }
        return ERROR;
    }

    private static String fetchWatchListCode() {
        JSONArray wl = getNetz()
                .get(getUrl(1, "users/me/lists?types=wl&fields=referenceCode"))
                .ensureSuccess()
                .asJsonArray()
                .getBody();
        return ((JSONObject) wl.get(0)).optString("referenceCode", "");
    }

    public static String fetchFriends() {
        if (!isAccessTokenInvalid()) {
            int skip = 0;
            int take = 50;
            try {
                StringBuilder friends = new StringBuilder();
                boolean ready = false;
                do {
                    JSONArray jFriends = getNetz().get(getUrl(1, "friends"))
                            .param("fields", "username")
                            .param("skip", skip)
                            .param("take", take)
                            .ensureSuccess().asJsonArray().getBody();
                    for (int ii = 0; ii < jFriends.length(); ii++) {
                        friends.append(((JSONObject) jFriends.get(ii)).optString("username", "")).append(",");
                    }
                    skip = skip + take;
                    if (jFriends.length() < take) ready = true;
                }
                while (!ready);
                if (friends.length() > 0)
                    return friends.substring(0, friends.length() - 1);
                else
                    return "";
            } catch (Exception ex) {
                retry(ex);
                return "";
            }
        }
        return "";
    }

    public static void uploadCorrectedCoordinates(String GcCode, Coordinate Pos) {
        boolean doRetry;
        do {
            try {
                getNetz().put(getUrl(1, "geocaches/" + GcCode + "/correctedcoordinates"))
                        .body(new JSONObject().put("latitude", Pos.getLatitude()).put("longitude", Pos.getLongitude()))
                        .ensureSuccess().asVoid();
                retryCount = 0;
                doRetry = false;
            } catch (Exception ex) {
                doRetry = retry(ex);
            }
        }
        while (doRetry);
    }

    public static int uploadCacheNote(String cacheCode, String notes) {
        if (cacheCode == null || cacheCode.length() == 0) return ERROR;
        if (!isPremiumMember()) return ERROR;
        boolean doRetry;
        do {
            try {
                getNetz()
                        .put(getUrl(1, "geocaches/" + cacheCode + "/notes"))
                        .body(new JSONObject().put("note", prepareNote(notes)))
                        .ensureSuccess()
                        .asVoid();
                doRetry = false;
                retryCount = 0;
            } catch (Exception ex) {
                doRetry = retry(ex);
            }
        }
        while (doRetry);
        if (APIError != OK) return ERROR;
        else return OK;
    }

    private static String prepareNote(String note) {
        return note.replace("\r", "");
    }

    public static void uploadLogImage(String logReferenceCode, String image, String description) {
        LastAPIError = "";
        APIError = OK;
        JSONObject url = new JSONObject();
        JSONObject uploading;
        try {
            url = getNetz()
                    .post(getUrl(1, "geocachelogs/" + logReferenceCode + "/images"))
                    .body(uploading = new JSONObject()
                            .put("base64ImageData", image)
                            .put("description", description)
                    )
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            Log.info(log, "uploadLogImage done");
        } catch (Exception ex) {
            APIError = ERROR;
            LastAPIError = ex.toString() + url.toString();
        }
    }

    public static boolean isAccessTokenInvalid() {
        return (fetchMyUserInfos().memberShipType == MemberShipType.Unknown);
    }

    public static boolean isPremiumMember() {
        return fetchMyUserInfos().memberShipType == MemberShipType.Premium;
    }

    public static boolean isDownloadLimitExceeded() {
        // do'nt want to access Web for this info (GL.that.postAsync)
        if (me == null) return false;
        return me.remaining <= 0 && me.remainingLite <= 0;
    }

    public static UserInfos fetchMyUserInfos() {
        if (me == null || me.memberShipType == MemberShipType.Unknown) {
            Log.debug(log, "fetchMyUserInfos called. Must fetch. Active now: " + active);
            do {
                if (active) {
                    // a try to handle quickly following calls (by another thread)
                    int waitedForMillis = 0;
                    do {
                        try {
                            sleep(1000);
                            if (me != null) return me;
                        } catch (InterruptedException ignored) {
                        }
                        waitedForMillis = waitedForMillis + 1;
                    }
                    while (active || waitedForMillis == 60);
                    if (waitedForMillis == 60) {
                        Log.debug(log, "avoid endless loop");
                    }
                }
                active = true;
                me = fetchUserInfos("me");
                if (me.memberShipType == MemberShipType.Unknown) {
                    me.findCount = -1;
                    // we need a new AccessToken
                    // API_ErrorEventHandlerList.handleApiKeyError(API_ErrorEventHandlerList.API_ERROR.INVALID);
                    Log.err(log, "fetchMyUserInfos: Need a new Access Token");
                }
                active = false;
            }
            while (active);
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
                ui.memberShipType = memberShipTypesFromInt(response.optInt("membershipLevelId", -1));
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

    public static String getSettingsAccessToken() {
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

    public static String getUrl(int version, String command) {
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

    private static MemberShipType memberShipTypesFromInt(int value) {
        switch (value) {
            case 1:
                return MemberShipType.Basic;
            case 2:
                return MemberShipType.Charter;
            case 3:
                return MemberShipType.Premium;
            default:
                return MemberShipType.Unknown;
        }
    }

    private static Trackable createTrackable(JSONObject API1Trackable) {
        try {
            Trackable tb = new Trackable();
            Log.debug(log, API1Trackable.toString());
            tb.setArchived(false);
            tb.setTbCode(API1Trackable.optString("referenceCode", ""));
            // trackingNumber	string	unique number used to prove discovery of trackable. only returned if user matches the holderCode
            // will not be stored (Why)
            tb.setTrackingCode(API1Trackable.optString("trackingNumber", ""));
            tb.setCurrentGeoCacheCode(API1Trackable.optString("currentGeocacheCode", ""));
            if (tb.getCurrentGeoCacheCode().contains("null")) tb.setCurrentGeoCacheCode("");
            tb.setCurrentGoal(PlatformUIBase.removeHtmlEntyties(API1Trackable.optString("goal")));
            tb.setCurrentOwnerName(getStringValue(API1Trackable, "holder", "username"));
            String releasedDate = API1Trackable.optString("releasedDate", "");
            try {
                tb.setDateCreated(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(releasedDate));
            } catch (Exception e) {
                tb.setDateCreated(new Date());
            }
            tb.setDescription(PlatformUIBase.removeHtmlEntyties(API1Trackable.optString("description", "")));
            tb.setIconUrl(API1Trackable.optString("iconUrl", ""));
            if (tb.getIconUrl().startsWith("http:")) {
                tb.setIconUrl("https:" + tb.getIconUrl().substring(5));
            }
            tb.setName(API1Trackable.optString("name", ""));
            tb.setOwnerName(getStringValue(API1Trackable, "owner", "username"));
            tb.setTypeName(API1Trackable.optString("type", ""));
            return tb;
        } catch (Exception e) {
            Log.err(log, "createTrackable(JSONObject API1Trackable)", e);
            return null;
        }
    }

    private static Cache createGeoCache(JSONObject API1Cache, ArrayList<String> fields, Cache cache) {
        // see https://api.groundspeak.com/documentation#geocache
        // see https://api.groundspeak.com/documentation#lite-geocache
        if (cache == null) {
            cache = new Cache(true);
            cache.setApiStatus(IS_LITE);
        }
        if (cache.getWayPoints() != null) {
            cache.getWayPoints().clear();
            // no merging of waypoints here
        } else {
            cache.setWayPoints(new CB_List<>());
        }
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
                        cache.setGeoCacheCode(API1Cache.optString(field, ""));
                        if (cache.getGeoCacheCode().length() == 0) {
                            Log.err(log, "Get no GCCode");
                            return null;
                        }
                        cache.setUrl("https://coord.info/" + cache.getGeoCacheCode());
                        cache.generatedId = Cache.generateCacheId(cache.getGeoCacheCode());
                        break;
                    case "name":
                        cache.setGeoCacheName(API1Cache.optString(switchValue, ""));
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
                        cache.numTravelbugs = API1Cache.optInt(switchValue, 0);
                        break;
                    case "placedDate":
                        cache.setDateHidden(DateFromString(API1Cache.optString(switchValue, "")));
                        break;
                    case "geocacheType":
                        // switch subValue
                        cache.setGeoCacheType(CacheTypeFromID(API1Cache.optJSONObject(switchValue).optInt("id", 0)));
                        break;
                    case "geocacheSize":
                        // switch subValue
                        cache.geoCacheSize = CacheSizeFromID(API1Cache.optJSONObject(switchValue).optInt("id", 0));
                        break;
                    case "location":
                        JSONObject location = API1Cache.optJSONObject(switchValue);
                        // switch subValue
                        cache.setCountry(location.optString("country", ""));
                        String state = location.optString("state", "");
                        if (state.toLowerCase().equals("none")) state = "";
                        cache.setState(state);
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
                            String sFound = userData.optString("foundDate", "");
                            boolean didFind = sFound.length() != 0 && !sFound.contains("null");
                            cache.setFound(didFind);
                            // correctedCoordinates
                            JSONObject correctedCoordinate = userData.optJSONObject("correctedCoordinates");
                            if (correctedCoordinate != null) {
                                if (CB_Core_Settings.UseCorrectedFinal.getValue()) {
                                    JSONObject postedCoordinates = API1Cache.optJSONObject("postedCoordinates");
                                    cache.setCoordinate(new Coordinate(postedCoordinates.optDouble("latitude", 0), postedCoordinates.optDouble("longitude", 0)));
                                    cache.getWayPoints().add(new Waypoint(
                                            "!?" + cache.getGeoCacheCode().substring(2),
                                            GeoCacheType.Final,
                                            "",
                                            correctedCoordinate.optDouble("latitude", 0),
                                            correctedCoordinate.optDouble("longitude", 0),
                                            cache.generatedId,
                                            "",
                                            "Final GSAK Corrected"));
                                } else {
                                    cache.setCoordinate(new Coordinate(correctedCoordinate.optDouble("latitude", 0), correctedCoordinate.optDouble("longitude", 0)));
                                    cache.setHasCorrectedCoordinates(true);
                                }
                            } else {
                                JSONObject postedCoordinates = API1Cache.optJSONObject("postedCoordinates");
                                if (postedCoordinates != null) {
                                    cache.setCoordinate(new Coordinate(postedCoordinates.optDouble("latitude", 0), postedCoordinates.optDouble("longitude", 0)));
                                } else {
                                    cache.setCoordinate(new Coordinate());
                                }
                            }
                            cache.setTmpNote(userData.optString("note", ""));
                        } else {
                            cache.setFound(false);
                            JSONObject postedCoordinates = API1Cache.optJSONObject("postedCoordinates");
                            if (postedCoordinates != null) {
                                cache.setCoordinate(new Coordinate(postedCoordinates.optDouble("latitude", 0), postedCoordinates.optDouble("longitude", 0)));
                            } else {
                                cache.setCoordinate(new Coordinate());
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
                            cache.setAttributesPositive(new DLong(0, 0));
                            cache.setAttributesNegative(new DLong(0, 0));
                            for (int j = 0; j < attributes.length(); j++) {
                                JSONObject attribute = attributes.optJSONObject(j);
                                if (attribute != null) {
                                    Attribute att = Attribute.getAttributeEnumByGcComId(attribute.optInt("id", 0));
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
                            if (!tmp.substring(0, Math.min(10, tmp.length())).contains("null")) {
                                // containsHtml liefert immer false
                                if (!tmp.contains("<")) {
                                    tmp = tmp.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
                                }
                                cache.setShortDescription(tmp);
                                cache.setApiStatus(IS_FULL); // got a cache without LongDescription
                            }
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
                        Log.err(log, "createGeoCache: " + switchValue + " not handled");
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
            for (int j = 0; j < wpts.length(); j++) {
                JSONObject wpt = wpts.optJSONObject(j);
                Waypoint waypoint = new Waypoint(true);
                waypoint.geoCacheId = cache.generatedId;
                JSONObject coordinates = wpt.optJSONObject("coordinates");
                if (coordinates != null) {
                    waypoint.setCoordinate(new Coordinate(coordinates.optDouble("latitude", 0), coordinates.optDouble("longitude", 0)));
                } else {
                    waypoint.setCoordinate(new Coordinate());
                }
                waypoint.setTitle(wpt.optString("name", ""));
                waypoint.setDescription(wpt.optString("description", ""));
                waypoint.waypointType = CacheTypeFromID(wpt.optInt("typeId", 0));
                waypoint.setGcCode(wpt.optString("prefix", "XX") + cache.getGeoCacheCode().substring(2));
                cache.getWayPoints().add(waypoint);
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
                    waypoint.geoCacheId = cache.generatedId;
                    JSONObject coordinates = wpt.optJSONObject("coordinates");
                    if (coordinates != null) {
                        waypoint.setCoordinate(new Coordinate(coordinates.optDouble("latitude", 0), coordinates.optDouble("longitude", 0)));
                    } else {
                        waypoint.setCoordinate(new Coordinate());
                    }
                    waypoint.setTitle("Corrected Coordinates (API)");
                    waypoint.setDescription(wpt.optString("description", ""));
                    waypoint.waypointType = GeoCacheType.Final;
                    waypoint.setGcCode("CO" + cache.getGeoCacheCode().substring(2));
                    cache.getWayPoints().add(waypoint);
                }
            }
        }
    }

    private static ArrayList<LogEntry> createLogs(Cache cache, JSONArray geocacheLogs) {
        ArrayList<LogEntry> logList = new ArrayList<>();
        if (geocacheLogs != null) {
            for (int ii = 0; ii < geocacheLogs.length(); ii++) {
                logList.add(createLog((JSONObject) geocacheLogs.get(ii), cache.generatedId));
            }
        }
        return logList;
    }

    private static LogEntry createLog(JSONObject geocacheLog, long cacheId) {
        LogEntry logEntry = new LogEntry();
        logEntry.cacheId = cacheId;
        logEntry.logText = geocacheLog.optString("text", "");
        logEntry.finder = getStringValue(geocacheLog, "owner", "username");
        String dateCreated = geocacheLog.optString("loggedDate", "");
        try {
            logEntry.logDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateCreated);
        } catch (Exception e) {
            logEntry.logDate = new Date();
        }
        logEntry.geoCacheLogType = GeoCacheLogType.parseString(geocacheLog.optString("type", ""));
        String referenceCode = geocacheLog.optString("referenceCode", "");
        logEntry.logId = generateLogId(referenceCode);
        return logEntry;
    }

    public static long generateLogId(String referenceCode) {
        referenceCode = referenceCode.substring(2); // ohne "GL"
        if (referenceCode.length() >= 5 || referenceCode.charAt(0) >= 'G') {
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

    private static ArrayList<ImageEntry> createImageList(JSONArray jImages, String GcCode, boolean withLogImages) {
        ArrayList<ImageEntry> imageEntries = new ArrayList<>();

        if (jImages != null) {
            for (int ii = 0; ii < jImages.length(); ii++) {

                JSONObject jImage = (JSONObject) jImages.get(ii);
                String Description = jImage.optString("description", "");
                String uri = jImage.optString("url", "");
                String referenceCode = jImage.optString("referenceCode", "GC");
                boolean isCacheImage = referenceCode.startsWith("GC");

                if (uri.length() > 0) {
                    if (isCacheImage || withLogImages) {
                        ImageEntry imageEntry = new ImageEntry();
                        imageEntry.setCacheId(Cache.generateCacheId(GcCode));
                        imageEntry.setDescription(Description);
                        imageEntry.setGcCode(GcCode);
                        imageEntry.setImageUrl(uri.replace("img.geocaching.com/gc/cache", "img.geocaching.com/cache"));

                        imageEntry.setCacheImage(false); // todo check is spoiler or what is it used for
                        imageEntry.setLocalPath(""); // create at download / read from DB
                        // imageEntry.LocalPath =  DescriptionImageGrabber.buildDescriptionImageFilename(GcCode, URI.create(uri));
                        imageEntry.setName(""); // does not exist in API 1.0
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
                if (im.getImageUrl().equalsIgnoreCase(url)) {
                    isNotInImageList = false;
                    break;
                }
            }
            if (isNotInImageList) {
                ImageEntry imageEntry = new ImageEntry();
                imageEntry.setCacheId(cache.generatedId);
                imageEntry.setGcCode(cache.getGeoCacheCode());
                imageEntry.setName("");
                imageEntry.setDescription(url.substring(url.lastIndexOf("/") + 1));
                imageEntry.setImageUrl(url);
                imageEntry.setCacheImage(true);
                imageEntry.setLocalPath(""); // create at download / read from DB
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
            cache.setUrl("http://www.geocaching.com/seek/cache_details.aspx?wp=" + cache.getGeoCacheCode());
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
                } catch (Exception ignored) {
                }
            }
        }
        return images;
    }


    private static GeoCacheType CacheTypeFromID(int id) {
        switch (id) {
            case 2:
                return GeoCacheType.Traditional;
            case 3:
                return GeoCacheType.Multi;
            case 4:
                return GeoCacheType.Virtual;
            case 5:
                return GeoCacheType.Letterbox;
            case 6:
                return GeoCacheType.Event;
            case 8:
                return GeoCacheType.Mystery;
            case 9:
                return GeoCacheType.APE;
            case 11:
                return GeoCacheType.Camera;
            case 13:
                return GeoCacheType.CITO;
            case 137:
                return GeoCacheType.Earth;
            case 453:
                return GeoCacheType.MegaEvent;
            case 1304:
                return GeoCacheType.AdventuresExhibit;
            case 1858:
                return GeoCacheType.Wherigo;
            case 3653:
                return GeoCacheType.CelebrationEvent;
            case 3773:
                return GeoCacheType.HQ;
            case 3774:
                return GeoCacheType.HQCelebration;
            case 4738:
                return GeoCacheType.HQBlockParty;
            case 7005:
                return GeoCacheType.Giga;
            case 217:
                return GeoCacheType.ParkingArea;
            case 218:
                return GeoCacheType.MultiQuestion;
            case 219:
                return GeoCacheType.MultiStage;
            case 220:
                return GeoCacheType.Final;
            case 221:
                return GeoCacheType.Trailhead;
            case 452:
                return GeoCacheType.ReferencePoint;
            default:
                return GeoCacheType.Undefined;
        }
    }

    private static GeoCacheSize CacheSizeFromID(int id) {
        switch (id) {
            case 2:
                return GeoCacheSize.micro;
            case 8:
                return GeoCacheSize.small;
            case 3:
                return GeoCacheSize.regular;
            case 4:
                return GeoCacheSize.large;
            default:
                return GeoCacheSize.other;
        }
    }

    private static Date DateFromString(String d) {
        String ps = "yyyy-MM-dd'T'HH:mm:ss";
        if (d.endsWith("Z"))
            ps = ps + "'Z'";
        try {
            return new SimpleDateFormat(ps, Locale.US).parse(d);
        } catch (Exception e) {
            Log.err(log, "DateFromString", e);
            return new Date();
        }
    }

    private static String getDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(date);
    }

    private static String getUTCDate(Date date) {
        // check "2001-09-28T00:00:00"
        Log.debug(log, "getUTCDate In:" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(date));
        long utc = date.getTime();
        // TimeZone tzp = TimeZone.getTimeZone("GMT");
        // utc = utc - tzp.getOffset(utc);
        long newUtc = utc + date.getTimezoneOffset() * 60 * 1000;
        Date newDate = new Date();
        newDate.setTime(newUtc);
        String ret = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(newDate);
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

    private enum MemberShipType {Unknown, Basic, Charter, Premium}

    public static class PQ implements Serializable {
        private static final long serialVersionUID = 8308386638170255124L;
        public String name;
        public int cacheCount;
        public Date lastGenerated;
        public double sizeMB;
        public boolean doDownload = false;
        String GUID;
    }

    public static class UserInfos {
        public String username;
        public MemberShipType memberShipType;
        public int findCount;
        // geocacheLimits
        public int remaining;
        public int remainingLite;
        public int remainingTime;
        public int remainingLiteTime;

        public UserInfos() {
            username = "";
            memberShipType = MemberShipType.Unknown;
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
        private static final String StatusFields = "referenceCode,favoritePoints,status,trackableCount,userData.foundDate";
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
            // addSearchFilter("hcc");
            // addSearchFilter("hn");
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
            addSearchFilter("location:[" + center.latitude + "," + center.longitude + "]");
            addSearchFilter("radius:" + radiusInMeters + "m");
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

        public Query onlyTheseGeoCaches(String commaSeparatedListOfGCCodes) {
            addSearchFilter("code:" + commaSeparatedListOfGCCodes);
            return this;
        }

        public Query notTheseGeoCaches(String commaSeparatedListOfGCCodes) {
            addSearchFilter("code:" + "not(" + commaSeparatedListOfGCCodes + ")");
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

        public Query resultFields(String resultFields) {
            addResultField(resultFields);
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
            if (qs.length() > 0) r.param("q", qs.substring(1));
            String fs = fieldsString.toString();
            if (fs.length() > 0) r.param("fields", fs.substring(1));
            String es = expandString.toString();
            if (es.length() > 0) r.param("expand", es.substring(1));
            return r;
        }
    }
}
