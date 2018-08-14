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
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Types.*;
import CB_Utils.Interfaces.ICancel;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;
import CB_Utils.Util.ByRef;
import CB_Utils.http.HttpUtils;
import CB_Utils.http.Webb;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.*;

public class GroundspeakAPI {
    public static final String GS_LIVE_URL = "https://api.groundspeak.com/LiveV6/geocaching.svc/";
    public static final String STAGING_GS_LIVE_URL = "https://staging.api.groundspeak.com/Live/V6Beta/geocaching.svc/";
    public static final int IO = 0;
    public static final int ERROR = -1;
    public static final int CONNECTION_TIMEOUT = -2;
    public static final int API_ERROR = -3;
    public static final int API_IS_UNAVAILABLE = -4;
    private static final String log = "GroundspeakAPI";
    public static String LastAPIError = "";
    public static boolean CacheStatusValid = false;
    public static int CachesLeft = -1;
    public static int CurrentCacheCount = -1;
    public static int MaxCacheCount = -1;
    public static boolean CacheStatusLiteValid = false;
    public static int CachesLeftLite = -1;
    public static int CurrentCacheCountLite = -1;
    public static int MaxCacheCountLite = -1;
    public static String MemberName = "";
    private static int membershipType = -1; // 0: Guest??? 1: Basic 2: Charter??? 3: Premium
    private static boolean mAPIKeyChecked = false;
    private static boolean mDownloadLimitExceeded = false;

    // GetMembershipType

    /**
     * Read the encrypted AccessToken from the config and check whether it is correct for Android CB
     *
     * @return
     */
    static String GetAccessToken() {
        return GetAccessToken(false);
    }

    /**
     * Read the encrypted AccessToken from the config and check whether it is correct for Andorid CB </br>
     * If Url_Codiert==true so the API-Key is URL-Codiert </br>
     * Like replase '/' with '%2F'</br>
     * </br>
     * This is essential for PQ-List
     *
     * @param Url_Codiert
     * @return the Accesstoken as String
     */
    static String GetAccessToken(boolean Url_Codiert) {
        String act;
        if (CB_Core_Settings.StagingAPI.getValue()) {
            act = CB_Core_Settings.GcAPIStaging.getValue();
        } else {
            act = CB_Core_Settings.GcAPI.getValue();
        }

        // Prüfen, ob das AccessToken für ACB ist!!!
        if (!(act.startsWith("A")))
            return "";
        String result = act.substring(1, act.length());

        if (Url_Codiert) {
            result = result.replace("/", "%2F");
            result = result.replace("\\", "%5C");
            result = result.replace("+", "%2B");
            result = result.replace("=", "%3D");
        }

        return result;
    }

    /**
     * @return true, if user is Premium Member
     */
    public static boolean IsPremiumMember() {
        if (membershipType < 0)
            membershipType = GetMembershipType(null);
        return membershipType == 3;
    }

    private static String GetUTCDate(Date date) {
        long utc = date.getTime();
        TimeZone tzp = TimeZone.getTimeZone("GMT");
        utc = utc - tzp.getOffset(utc);
        String ret = "\\/Date(" + utc + ")\\/";
        Log.info(log, "Logdate uploaded: " + ret);
        return ret;
    }

    private static String ConvertNotes(String note) {
        String result = note.replace("\r", "");
        result = result.replace("\"", "\\\"");
        return result.replace("\n", "\\n");
    }


    private static String prepareUTCDate(Date date) {
        long utc = date.getTime();
        TimeZone tzp = TimeZone.getTimeZone("GMT");
        utc = utc - tzp.getOffset(utc);
        String ret = "/Date(" + utc + ")/";
        return ret;
    }

    private static String prepareNote(String note) {
        return note.replace("\r", "");
    }

    /**
     * Upload FieldNotes
     *
     * @param cacheCode
     * @param wptLogTypeId
     * @param dateLogged
     * @param note
     * @param directLog
     * @param icancel
     * @return int: einen der stati IO = 0;ERROR = -1;CONNECTION_TIMEOUT = -2;API_ERROR = -3;API_IS_UNAVAILABLE = -4;
     */
    public static int CreateFieldNoteAndPublish(String cacheCode, int wptLogTypeId, Date dateLogged, String note, boolean directLog, final ICancel icancel) {
        int chk = chkMembership(true);
        if (chk < 0)
            return chk;

        String URL = CB_Core_Settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

        try {
            HttpPost httppost = new HttpPost(URL + "CreateFieldNoteAndPublish?format=json");
            String requestString = "";
            requestString = "{";
            requestString += "\"AccessToken\":\"" + GetAccessToken() + "\",";
            requestString += "\"CacheCode\":\"" + cacheCode + "\",";
            requestString += "\"WptLogTypeId\":" + String.valueOf(wptLogTypeId) + ",";
            requestString += "\"UTCDateLogged\":\"" + GetUTCDate(dateLogged) + "\",";
            requestString += "\"Note\":\"" + ConvertNotes(note) + "\",";
            if (directLog) {
                requestString += "\"PromoteToLog\":true,";
            } else {
                requestString += "\"PromoteToLog\":false,";
            }

            requestString += "\"FavoriteThisCache\":false";
            requestString += "}";

            httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

            // set time outs
            HttpUtils.conectionTimeout = CB_Core_Settings.connection_timeout.getValue();
            HttpUtils.socketTimeout = CB_Core_Settings.socket_timeout.getValue();

            // Execute HTTP Post Request
            String result = HttpUtils.Execute(httppost, icancel);

            if (result.contains("The service is unavailable")) {
                return API_IS_UNAVAILABLE;
            }

            // Parse JSON Result
            try {
                JSONTokener tokener = new JSONTokener(result);
                JSONObject json = (JSONObject) tokener.nextValue();
                JSONObject status = json.getJSONObject("Status");
                if (status.getInt("StatusCode") == 0) {
                    result = "";
                    LastAPIError = "";
                } else {
                    result = "StatusCode = " + status.getInt("StatusCode") + "\n";
                    result += status.getString("StatusMessage") + "\n";
                    result += status.getString("ExceptionDetails");
                    LastAPIError = result;
                    return ERROR;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.err(log, "UploadFieldNotesAPI", e);
                LastAPIError = e.getMessage();
                return ERROR;
            }

        } catch (ConnectTimeoutException e) {
            Log.err(log, "UploadFieldNotesAPI ConnectTimeoutException", e);
            return CONNECTION_TIMEOUT;
        } catch (UnsupportedEncodingException e) {
            Log.err(log, "UploadFieldNotesAPI UnsupportedEncodingException", e);
            return ERROR;
        } catch (ClientProtocolException e) {
            Log.err(log, "UploadFieldNotesAPI ClientProtocolException", e);
            return ERROR;
        } catch (IOException e) {
            Log.err(log, "UploadFieldNotesAPI IOException", e);
            return ERROR;
        }

        LastAPIError = "";
        return IO;
    }

    /**
     * @param icancel
     * @return int CachesFound
     */
    public static int GetCachesFound(final ICancel icancel) {

        int chk = chkMembership(false);
        if (chk < 0)
            return chk;

        String URL = CB_Core_Settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

        try {
            HttpPost httppost = new HttpPost(URL + "GetYourUserProfile?format=json");
            String requestString = "";
            requestString = "{";
            requestString += "\"AccessToken\":\"" + GetAccessToken() + "\",";
            requestString += "\"ProfileOptions\":{";
            requestString += "}" + ",";
            requestString += getDeviceInfoRequestString();
            requestString += "}";

            httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

            // set time outs
            HttpUtils.conectionTimeout = CB_Core_Settings.connection_timeout.getValue();
            HttpUtils.socketTimeout = CB_Core_Settings.socket_timeout.getValue();

            // Execute HTTP Post Request
            String result = HttpUtils.Execute(httppost, icancel);

            if (result.contains("The service is unavailable")) {
                return API_IS_UNAVAILABLE;
            }

            try
            // Parse JSON Result
            {
                JSONTokener tokener = new JSONTokener(result);
                JSONObject json = (JSONObject) tokener.nextValue();
                JSONObject status = json.getJSONObject("Status");
                if (status.getInt("StatusCode") == 0) {
                    result = "";
                    JSONObject profile = json.getJSONObject("Profile");
                    JSONObject user = profile.getJSONObject("User");
                    return user.getInt("FindCount");

                } else {
                    result = "StatusCode = " + status.getInt("StatusCode") + "\n";
                    result += status.getString("StatusMessage") + "\n";
                    result += status.getString("ExceptionDetails");

                    return ERROR;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (ConnectTimeoutException e) {
            Log.err(log, "GetCachesFound ConnectTimeoutException", e);
            return CONNECTION_TIMEOUT;
        } catch (UnsupportedEncodingException e) {
            Log.err(log, "GetCachesFound UnsupportedEncodingException", e);
            return ERROR;
        } catch (ClientProtocolException e) {
            Log.err(log, "GetCachesFound ClientProtocolException", e);
            return ERROR;
        } catch (IOException e) {
            Log.err(log, "GetCachesFound", e);
            return ERROR;
        }

        return (ERROR);
    }

    /**
     * Loads the Membership type
     *
     * @param icancel
     * @return int  -1=Error;0=Guest???;1=Basic;2=Charter???;3=Premium
     */
    public static int GetMembershipType(final ICancel icancel) {
        if (mAPIKeyChecked)
            return membershipType;
        String URL = CB_Core_Settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

        try {
            HttpPost httppost = new HttpPost(URL + "GetYourUserProfile?format=json");
            String requestString = "";
            requestString = "{";
            requestString += "\"AccessToken\":\"" + GetAccessToken() + "\",";
            requestString += "\"ProfileOptions\":{";
            requestString += "}" + ",";
            requestString += getDeviceInfoRequestString();
            requestString += "}";

            httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

            // set time outs
            HttpUtils.conectionTimeout = CB_Core_Settings.connection_timeout.getValue();
            HttpUtils.socketTimeout = CB_Core_Settings.socket_timeout.getValue();

            // Execute HTTP Post Request
            String result = HttpUtils.Execute(httppost, icancel);

            if (result.contains("The service is unavailable")) {
                return API_IS_UNAVAILABLE;
            }

            try
            // Parse JSON Result
            {
                JSONTokener tokener = new JSONTokener(result);
                JSONObject json = (JSONObject) tokener.nextValue();
                int status = getApiStatus(result);
                if (status == 0) {
                    result = "";
                    JSONObject profile = json.getJSONObject("Profile");
                    JSONObject user = profile.getJSONObject("User");
                    JSONObject memberType = user.getJSONObject("MemberType");
                    int memberTypeId = memberType.getInt("MemberTypeId");
                    MemberName = user.getString("UserName");
                    membershipType = memberTypeId;
                    mAPIKeyChecked = true;
                    // Zurücksetzen, falls ein anderer User gewählt wurde
                    return memberTypeId;
                } else if (status == 2 || status == 3 || status == 141) {
                    mAPIKeyChecked = false;
                    return API_ERROR;

                } else {
                    Log.warn(log, "GetMembershipType API-Error: " + result);
                    mAPIKeyChecked = false;
                    return API_ERROR;
                }

            } catch (Exception e) {
                Log.err(log, "GetMembershipType JSONException", e);
                mAPIKeyChecked = false;
                return API_ERROR;
            }

        } catch (ConnectTimeoutException e) {
            Log.err(log, "GetMembershipType ConnectTimeoutException", e);
            mAPIKeyChecked = false;
            return CONNECTION_TIMEOUT;
        } catch (UnsupportedEncodingException e) {
            Log.err(log, "GetMembershipType UnsupportedEncodingException", e);
            mAPIKeyChecked = false;
            return ERROR;
        } catch (ClientProtocolException e) {
            Log.err(log, "GetMembershipType ClientProtocolException", e);
            mAPIKeyChecked = false;
            return ERROR;
        } catch (IOException e) {
            if (e.toString().contains("UnknownHostException")) {
                Log.err(log, "GetMembershipType ConnectTimeoutException", e);
                mAPIKeyChecked = false;
                return CONNECTION_TIMEOUT;
            }
            Log.err(log, "GetMembershipType IOException", e);
            mAPIKeyChecked = false;
            return ERROR;
        }

    }

    private static String getDeviceInfoRequestString() {
        String string = "\"DeviceInfo\":{";

        string += "\"ApplicationCurrentMemoryUsage\":\"" + String.valueOf(2147483647) + "\",";
        string += "\"ApplicationPeakMemoryUsage\":\"" + String.valueOf(2147483647) + "\",";
        string += "\"ApplicationSoftwareVersion\":\"" + CoreSettingsForward.VersionString + "\",";
        string += "\"DeviceManufacturer\":\"" + "?\"" + ",";
        string += "\"DeviceName\":\"" + "?\"" + ",";
        string += "\"DeviceOperatingSystem\":\"ANDROID\"" + ",";
        string += "\"DeviceTotalMemoryInMB\":\"" + String.valueOf(1.26743233E+15) + "\",";
        string += "\"DeviceUniqueId\":\"" + "?\"" + ",";
        string += "\"MobileHardwareVersion\":\"" + "?\"" + ",";
        string += "\"WebBrowserVersion\":\"" + "?\"";

        string += "}";

        return string;
    }

    public static int getApiStatus(String result) {

        try {
            JSONTokener tokener = new JSONTokener(result);
            JSONObject json = (JSONObject) tokener.nextValue();
            JSONObject jsonStatus = json.getJSONObject("Status");
            int status = jsonStatus.getInt("StatusCode");
            String statusMessage = jsonStatus.getString("StatusMessage");
            String exceptionDetails = jsonStatus.getString("ExceptionDetails");

            String logString = "StatusCode = " + status + "\n" + statusMessage + "\n" + exceptionDetails;

            if (status == 0)
                return status;

            if (status == 2) {
                // Not authorized
                API_ErrorEventHandlerList.callInvalidApiKey(API_ErrorEventHandlerList.API_ERROR.INVALID);
                Log.warn(log, "API-Error: " + logString);
                return status;
            }

            if (status == 3) {
                // API Key expired
                API_ErrorEventHandlerList.callInvalidApiKey(API_ErrorEventHandlerList.API_ERROR.EXPIRED);
                Log.warn(log, "API-Error: " + logString);
                return status;
            }

            if (status == 141) {
                // / {"Status":{"StatusCode":141,"StatusMessage":"The AccessToken provided is not valid"
                API_ErrorEventHandlerList.callInvalidApiKey(API_ErrorEventHandlerList.API_ERROR.INVALID);
                Log.warn(log, "API-Error: " + logString);
                return status;
            }

            // unknown
            API_ErrorEventHandlerList.callInvalidApiKey(API_ErrorEventHandlerList.API_ERROR.INVALID);
            Log.warn(log, "API-Error: " + logString);
            return status;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Gets the Status for the given Caches
     * <p>
     * Staging
     * Config.settings.StagingAPI.getValue()
     * accessToken
     * conectionTimeout
     * Config.settings.connection_timeout.getValue()
     * socketTimeout
     * Config.settings.socket_timeout.getValue()
     *
     * @param caches is also for return
     * @return
     */
    public static int GetGeocacheStatus(ArrayList<Cache> caches, final ICancel icancel) {
        int chk = chkMembership(false);
        if (chk < 0)
            return chk;

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        String URL = CB_Core_Settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

        try {
            HttpPost httppost = new HttpPost(URL + "GetGeocacheStatus?format=json");
            String requestString = "";
            requestString = "{";
            requestString += "\"AccessToken\":\"" + GetAccessToken() + "\",";
            requestString += "\"CacheCodes\":[";

            int i = 0;
            for (Cache cache : caches) {
                requestString += "\"" + cache.getGcCode() + "\"";
                if (i < caches.size() - 1)
                    requestString += ",";
                i++;
            }

            requestString += "]";
            requestString += "}";

            httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

            // set time outs
            HttpUtils.conectionTimeout = CB_Core_Settings.connection_timeout.getValue();
            HttpUtils.socketTimeout = CB_Core_Settings.socket_timeout.getValue();

            // Execute HTTP Post Request
            String result = HttpUtils.Execute(httppost, icancel);

            if (result.contains("The service is unavailable")) {
                return API_IS_UNAVAILABLE;
            }
            try
            // Parse JSON Result
            {
                JSONTokener tokener = new JSONTokener(result);
                JSONObject json = (JSONObject) tokener.nextValue();
                JSONObject status = json.getJSONObject("Status");
                if (status.getInt("StatusCode") == 0) {
                    result = "";
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

                    return 0;
                } else {
                    result = "StatusCode = " + status.getInt("StatusCode") + "\n";
                    result += status.getString("StatusMessage") + "\n";
                    result += status.getString("ExceptionDetails");
                    LastAPIError = result;
                    return (-1);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (ConnectTimeoutException e) {
            Log.err(log, "GetGeocacheStatus ConnectTimeoutException", e);
            return CONNECTION_TIMEOUT;
        } catch (UnsupportedEncodingException e) {
            Log.err(log, "GetGeocacheStatus UnsupportedEncodingException", e);
            return ERROR;
        } catch (ClientProtocolException e) {
            Log.err(log, "GetGeocacheStatus ClientProtocolException", e);
            return ERROR;
        } catch (IOException e) {
            Log.err(log, "GetGeocacheStatus IOException", e);
            return ERROR;
        }

        return (-1);
    }

    /**
     * Gets the Logs for the given Cache
     *
     * @return
     */
    public static int GetGeocacheLogsByCache(Cache cache, ArrayList<LogEntry> logList, boolean all, cancelRunnable cancelRun) {
        String finders = CB_Core_Settings.Friends.getValue();
        String[] finder = finders.split("\\|");
        ArrayList<String> finderList = new ArrayList<String>();
        for (String f : finder) {
            finderList.add(f);
        }

        if (cache == null)
            return -3;
        int chk = chkMembership(false);
        if (chk < 0)
            return chk;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        int start = 1;
        int count = 30;

        String URL = CB_Core_Settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
        while (!cancelRun.cancel() && (finderList.size() > 0 || all))
        // Schleife, solange bis entweder keine Logs mehr geladen werden oder bis alle Logs aller Finder geladen sind.
        {
            try {
                String requestString = "";
                requestString += "&AccessToken=" + GetAccessToken();
                requestString += "&CacheCode=" + cache.getGcCode();
                requestString += "&StartIndex=" + start;
                requestString += "&MaxPerPage=" + count;
                HttpGet httppost = new HttpGet(URL + "GetGeocacheLogsByCacheCode?format=json" + requestString);

                // set time outs
                HttpUtils.conectionTimeout = CB_Core_Settings.connection_timeout.getValue();
                HttpUtils.socketTimeout = CB_Core_Settings.socket_timeout.getValue();

                // Execute HTTP Post Request
                String result = HttpUtils.Execute(httppost, cancelRun);

                if (result.contains("The service is unavailable")) {
                    return API_IS_UNAVAILABLE;
                }
                try
                // Parse JSON Result
                {
                    JSONTokener tokener = new JSONTokener(result);
                    JSONObject json = (JSONObject) tokener.nextValue();
                    JSONObject status = json.getJSONObject("Status");
                    if (status.getInt("StatusCode") == 0) {
                        result = "";
                        JSONArray geocacheLogs = json.getJSONArray("Logs");
                        for (int ii = 0; ii < geocacheLogs.length(); ii++) {
                            JSONObject jLogs = (JSONObject) geocacheLogs.get(ii);
                            JSONObject jFinder = (JSONObject) jLogs.get("Finder");
                            JSONObject jLogType = (JSONObject) jLogs.get("LogType");
                            LogEntry logEntry = new LogEntry();
                            logEntry.CacheId = cache.Id;
                            logEntry.Comment = jLogs.getString("LogText");
                            logEntry.Finder = jFinder.getString("UserName");
                            if (!finderList.contains(logEntry.Finder)) {
                                continue;
                            }
                            finderList.remove(logEntry.Finder);
                            logEntry.Id = jLogs.getInt("ID");
                            logEntry.Timestamp = new Date();
                            try {
                                String dateCreated = jLogs.getString("VisitDate");
                                int date1 = dateCreated.indexOf("/Date(");
                                int date2 = dateCreated.indexOf("-");
                                String date = (String) dateCreated.subSequence(date1 + 6, date2);
                                logEntry.Timestamp = new Date(Long.valueOf(date));
                            } catch (Exception exc) {
                                Log.err(log, "SearchForGeocaches_ParseLogDate", exc);
                            }
                            logEntry.Type = LogTypes.GC2CB_LogType(jLogType.getInt("WptLogTypeId"));
                            logList.add(logEntry);

                        }

                        if ((geocacheLogs.length() < count) || (finderList.size() == 0)) {
                            return 0; // alle Logs des Caches geladen oder alle gesuchten Finder gefunden
                        }
                    } else {
                        result = "StatusCode = " + status.getInt("StatusCode") + "\n";
                        result += status.getString("StatusMessage") + "\n";
                        result += status.getString("ExceptionDetails");
                        LastAPIError = result;
                        return (-1);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (ConnectTimeoutException e) {
                Log.err(log, "GetGeocacheLogsByCache ConnectTimeoutException", e);
                return CONNECTION_TIMEOUT;
            } catch (UnsupportedEncodingException e) {
                Log.err(log, "GetGeocacheLogsByCache UnsupportedEncodingException", e);
                return ERROR;
            } catch (ClientProtocolException e) {
                Log.err(log, "GetGeocacheLogsByCache ClientProtocolException", e);
                return ERROR;
            } catch (IOException e) {
                Log.err(log, "GetGeocacheLogsByCache IOException", e);
                return ERROR;
            }
            // die nächsten Logs laden
            start += count;
        }
        return (-1);
    }

    /**
     * returns Status Code (0 -> OK)
     */
    public static int GetCacheLimits(ICancel icancel) {
        if (CachesLeft > -1)
            return 0;

        int chk = chkMembership(false);
        if (chk < 0)
            return chk;

        String URL = CB_Core_Settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

        LastAPIError = "";
        // zum Abfragen der CacheLimits einfach nach einem Cache suchen, der
        // nicht existiert.
        // dadurch wird der Zähler nicht erhöht, die Limits aber zurückgegeben.
        try {
            HttpPost httppost = new HttpPost(URL + "SearchForGeocaches?format=json");
            try {
                JSONObject request = new JSONObject();
                request.put("AccessToken", GetAccessToken());
                request.put("IsLight", false);
                request.put("StartIndex", 0);
                request.put("MaxPerPage", 1);
                request.put("GeocacheLogCount", 0);
                request.put("TrackableLogCount", 0);
                JSONObject requestcc = new JSONObject();
                JSONArray requesta = new JSONArray();
                requesta.put("GCZZZZZ");
                requestcc.put("CacheCodes", requesta);
                request.put("CacheCode", requestcc);

                String requestString = request.toString();

                httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

                // set time outs
                HttpUtils.conectionTimeout = CB_Core_Settings.connection_timeout.getValue();
                HttpUtils.socketTimeout = CB_Core_Settings.socket_timeout.getValue();

                // Execute HTTP Post Request
                String result = HttpUtils.Execute(httppost, icancel);

                if (result.contains("The service is unavailable")) {
                    return API_IS_UNAVAILABLE;
                }
                // Parse JSON Result

                JSONTokener tokener = new JSONTokener(result);
                JSONObject json = (JSONObject) tokener.nextValue();
                int status = checkCacheStatus(json, false);
                // hier keine Überprüfung des Status, da dieser z.B. 118
                // (Überschreitung des Limits) sein kann, aber der CacheStatus
                // aber trotzdem drin ist.
                return status;
            } catch (JSONException e) {
                e.printStackTrace();
                LastAPIError = "API Error: " + e.getMessage();
                return -2;
            }

        } catch (ConnectTimeoutException e) {
            Log.err(log, "GetGeocacheStatus ConnectTimeoutException", e);
            return CONNECTION_TIMEOUT;
        } catch (UnsupportedEncodingException e) {
            Log.err(log, "GetGeocacheStatus UnsupportedEncodingException", e);
            return ERROR;
        } catch (ClientProtocolException e) {
            Log.err(log, "GetGeocacheStatus ClientProtocolException", e);
            return ERROR;
        } catch (IOException e) {
            Log.err(log, "GetGeocacheStatus IOException", e);
            return ERROR;
        }
    }

    // liest den CacheStatus aus dem gegebenen json Object aus.
    // darin ist gespeichert, wie viele Full Caches schon geladen wurden und wie
    // viele noch frei sind
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

    static int getCacheSize(int containerTypeId) {
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

    static CacheTypes getCacheType(int apiTyp) {
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

    /**
     * Ruft die Liste der TBs ab, die im Besitz des Users sind
     *
     * @param list    Liste der TBs
     * @param icancel Rückkehr
     * @return int: einen der stati IO = 0;ERROR = -1;CONNECTION_TIMEOUT = -2;API_ERROR = -3;API_IS_UNAVAILABLE = -4;
     */
    public static int GetUsersTrackables(TbList list, ICancel icancel) {
        int chk = chkMembership(false);
        if (chk < 0)
            return chk;

        try {
            Log.info(log, "GetUsersTrackables: Create request");
            HttpPost httppost;
            try {
                httppost = new HttpPost((CB_Core_Settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL) + "GetUsersTrackables?format=json");
                JSONObject request = new JSONObject();
                request.put("AccessToken", GetAccessToken());
                request.put("MaxPerPage", 30);
                httppost.setEntity(new ByteArrayEntity(request.toString().getBytes("UTF8")));
                HttpUtils.conectionTimeout = CB_Core_Settings.connection_timeout.getValue();
                HttpUtils.socketTimeout = CB_Core_Settings.socket_timeout.getValue();
            } catch (JSONException e) {
                // should never occur
                Log.err(log, "GetUsersTrackables create request error: should never occur!", e);
                return ERROR;
            }

            try {
                Log.info(log, "GetUsersTrackables: Execute request");
                String result = HttpUtils.Execute(httppost, icancel);

                if (result.contains("The service is unavailable")) {
                    Log.err(log, "GetUsersTrackables request: API_IS_UNAVAILABLE ");
                    return API_IS_UNAVAILABLE;
                }

                Log.debug(log, "GetUsersTrackables parse Result\n" + result + "\n");
                // Parse JSON Result
                JSONTokener tokener = new JSONTokener(result);
                JSONObject json = (JSONObject) tokener.nextValue();
                JSONObject status = json.getJSONObject("Status");
                int statusCode = status.getInt("StatusCode");
                Log.info(log, "GetUsersTrackables: " + "StatusCode = " + statusCode);
                LastAPIError = "";
                if (statusCode == 0) {
                    JSONArray jTrackables = json.getJSONArray("Trackables");

                    for (int ii = 0; ii < jTrackables.length(); ii++) {
                        JSONObject jTrackable = (JSONObject) jTrackables.get(ii);
                        boolean InCollection = false;
                        try {
                            InCollection = jTrackable.getBoolean("InCollection");
                        } catch (JSONException e) {
                            Log.err(log, "GetUsersTrackables JSONException for checking TB", e);
                        }
                        if (!InCollection) {
                            Trackable tb = new Trackable(jTrackable);
                            Log.info(log, "GetUsersTrackables: add " + tb.getName());
                            list.add(tb);
                        } else {
                            try {
                                Log.info(log, "GetUsersTrackables: not inCollection " + jTrackable.getString("Name"));
                            } catch (JSONException e) {
                                Log.err(log, "GetUsersTrackables JSONException for checking inCollection get Name", e);
                            }
                        }
                    }
                    Log.info(log, "GetUsersTrackables: return with all OK.");
                    return IO;
                } else {
                    LastAPIError = "StatusCode = " + statusCode + "\n";
                    LastAPIError += status.getString("StatusMessage") + "\n";
                    LastAPIError += status.getString("ExceptionDetails");
                    Log.err(log, "GetUsersTrackables: " + LastAPIError);
                    return (ERROR);
                }

            } catch (JSONException e) {
                Log.err(log, "GetUsersTrackables JSONException " + LastAPIError, e);
                return ERROR;
            }

        } catch (ConnectTimeoutException e) {
            Log.err(log, "GetUsersTrackables ConnectTimeoutException", e);
            return CONNECTION_TIMEOUT;
        } catch (UnsupportedEncodingException e) {
            Log.err(log, "GetUsersTrackables UnsupportedEncodingException", e);
            return ERROR;
        } catch (ClientProtocolException e) {
            Log.err(log, "GetUsersTrackables ClientProtocolException", e);
            return ERROR;
        } catch (IOException e) {
            Log.err(log, "GetUsersTrackables IOException", e);
            return ERROR;
        }
    }

    public static int GetTrackablesByTrackingNumber(String TrackingCode, ByRef<Trackable> TB, ICancel icancel) {
        int chk = chkMembership(false);
        if (chk < 0)
            return chk;

        String URL = CB_Core_Settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

        try {
            HttpGet httppost = new HttpGet(URL + "GetTrackablesByTrackingNumber?AccessToken=" + GetAccessToken(true) + "&trackingNumber=" + TrackingCode + "&format=json");

            // set time outs
            HttpUtils.conectionTimeout = CB_Core_Settings.connection_timeout.getValue();
            HttpUtils.socketTimeout = CB_Core_Settings.socket_timeout.getValue();

            // Execute HTTP Post Request
            String result = HttpUtils.Execute(httppost, icancel);

            if (result.contains("The service is unavailable")) {
                return API_IS_UNAVAILABLE;
            }
            try
            // Parse JSON Result
            {
                JSONTokener tokener = new JSONTokener(result);
                JSONObject json = (JSONObject) tokener.nextValue();
                JSONObject status = json.getJSONObject("Status");
                if (status.getInt("StatusCode") == 0) {
                    LastAPIError = "";
                    JSONArray jTrackables = json.getJSONArray("Trackables");

                    for (int i = 0; i < jTrackables.length(); ) {
                        JSONObject jTrackable = (JSONObject) jTrackables.get(i);
                        TB.set(new Trackable(jTrackable));
                        TB.get().setTrackingCode(TrackingCode);
                        return IO;
                    }
                } else {
                    LastAPIError = "";
                    LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
                    LastAPIError += status.getString("StatusMessage") + "\n";
                    LastAPIError += status.getString("ExceptionDetails");
                    TB = null;
                    return ERROR;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (ConnectTimeoutException e) {
            Log.err(log, "getTBbyTreckNumber ConnectTimeoutException", e);
            TB = null;
            return CONNECTION_TIMEOUT;
        } catch (UnsupportedEncodingException e) {
            Log.err(log, "getTBbyTreckNumber UnsupportedEncodingException", e);
            TB = null;
            return ERROR;
        } catch (ClientProtocolException e) {
            Log.err(log, "getTBbyTreckNumber ClientProtocolException", e);
            TB = null;
            return ERROR;
        } catch (IOException e) {
            Log.err(log, "getTBbyTreckNumber IOException", e);
            TB = null;
            return ERROR;
        }

        TB = null;
        return ERROR;
    }

    public static int getTBbyTbCode(String TrackingNumber, ByRef<Trackable> TB, ICancel icancel) {
        int chk = chkMembership(false);
        if (chk < 0)
            return chk;
        String URL = CB_Core_Settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

        try {
            HttpGet httppost = new HttpGet(URL + "GetTrackablesByTBCode?AccessToken=" + GetAccessToken(true) + "&tbCode=" + TrackingNumber + "&format=json");

            // set time outs
            HttpUtils.conectionTimeout = CB_Core_Settings.connection_timeout.getValue();
            HttpUtils.socketTimeout = CB_Core_Settings.socket_timeout.getValue();

            // Execute HTTP Post Request
            String result = HttpUtils.Execute(httppost, icancel);

            if (result.contains("The service is unavailable")) {
                return API_IS_UNAVAILABLE;
            }
            try
            // Parse JSON Result
            {
                JSONTokener tokener = new JSONTokener(result);
                JSONObject json = (JSONObject) tokener.nextValue();
                JSONObject status = json.getJSONObject("Status");
                if (status.getInt("StatusCode") == 0) {
                    LastAPIError = "";
                    JSONArray jTrackables = json.getJSONArray("Trackables");

                    for (int ii = 0; ii < jTrackables.length(); ) {
                        JSONObject jTrackable = (JSONObject) jTrackables.get(ii);
                        TB.set(new Trackable(jTrackable));
                        return IO;
                    }
                } else {
                    LastAPIError = "";
                    LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
                    LastAPIError += status.getString("StatusMessage") + "\n";
                    LastAPIError += status.getString("ExceptionDetails");

                    return ERROR;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (ConnectTimeoutException e) {
            Log.err(log, "getTBbyTbCode ConnectTimeoutException", e);
            TB = null;
            return CONNECTION_TIMEOUT;
        } catch (UnsupportedEncodingException e) {
            Log.err(log, "getTBbyTbCode UnsupportedEncodingException", e);
            TB = null;
            return ERROR;
        } catch (ClientProtocolException e) {
            Log.err(log, "getTBbyTbCode ClientProtocolException", e);
            TB = null;
            return ERROR;
        } catch (IOException e) {
            Log.err(log, "getTBbyTbCode IOException", e);
            TB = null;
            return ERROR;
        }

        TB = null;
        return ERROR;

    }

    /**
     * Ruft die Liste der Bilder ab, die in einem Cache sind
     */
    public static int getImagesForGeocache(String cacheCode, ArrayList<String> images, ICancel icancel) {
        int chk = chkMembership(false);
        if (chk < 0)
            return chk;

        String URL = CB_Core_Settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

        try {
            HttpGet httppost = new HttpGet(URL + "GetImagesForGeocache?AccessToken=" + GetAccessToken() + "&CacheCode=" + cacheCode + "&format=json");

            // set time outs
            HttpUtils.conectionTimeout = CB_Core_Settings.connection_timeout.getValue();
            HttpUtils.socketTimeout = CB_Core_Settings.socket_timeout.getValue();

            // Execute HTTP Post Request
            String result = HttpUtils.Execute(httppost, icancel);

            if (result.contains("The service is unavailable")) {
                return API_IS_UNAVAILABLE;
            }
            try
            // Parse JSON Result
            {
                JSONTokener tokener = new JSONTokener(result);
                JSONObject json = (JSONObject) tokener.nextValue();
                JSONObject status = json.getJSONObject("Status");
                if (status.getInt("StatusCode") == 0) {
                    LastAPIError = "";
                    JSONArray jImages = json.getJSONArray("Images");

                    for (int ii = 0; ii < jImages.length(); ii++) {
                        JSONObject jImage = (JSONObject) jImages.get(ii);
                        images.add(jImage.getString("Url"));
                    }
                    return 0;
                } else {
                    LastAPIError = "";
                    LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
                    LastAPIError += status.getString("StatusMessage") + "\n";
                    LastAPIError += status.getString("ExceptionDetails");

                    return (-1);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (ConnectTimeoutException e) {
            Log.err(log, "getImagesForGeocache ConnectTimeoutException", e);
            return CONNECTION_TIMEOUT;
        } catch (UnsupportedEncodingException e) {
            Log.err(log, "getImagesForGeocache UnsupportedEncodingException", e);
            return ERROR;
        } catch (ClientProtocolException e) {
            Log.err(log, "getImagesForGeocache ClientProtocolException", e);
            return ERROR;
        } catch (IOException e) {
            Log.err(log, "getImagesForGeocache IOException", e);
            return ERROR;
        }

        return (-1);
    }

    public static int getImagesForGeocache(String cacheCode, HashMap<String, URI> list) {
        Log.info(log, "getImagesForGeocache");
        LastAPIError = "";
        if (cacheCode == null) cacheCode = "";
        int chk = chkMembership(false);
        if (chk < 0) return ERROR;
        if (list == null)
            list = new HashMap<String, URI>();
        try {
            Webb httpClient = Webb.create();
            JSONObject response = httpClient
                    .get(getUrl("GetImagesForGeocache?AccessToken=" + GetAccessToken(true) + "&CacheCode=" + cacheCode + "&format=json"))
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            // todo rework error handling ... possibly "The service is unavailable"
            int ret = getAPICallStatus(response.getJSONObject("Status"));
            if (ret != 0) {
                return ret;
            } else {
                try {
                    JSONArray jImages = response.getJSONArray("Images");
                    Log.info(log, "getImagesForGeocache Anz.: " + jImages.length());
                    for (int ii = 0; ii < jImages.length(); ii++) {
                        try {
                            JSONObject jImage = (JSONObject) jImages.get(ii);
                            String name = jImage.getString("Name");
                            String uri = jImage.getString("Url");
                            Log.debug(log, "getImagesForGeocache getImageObject Nr.:" + ii + " '" + name + "' " + uri);
                            // ignore log images
                            if (uri.contains("/cache/log")) {
                                Log.debug(log, "getImagesForGeocache getImageObject Nr.:" + ii + " ignored.");
                                continue; // LOG-Image
                            }
                            // Check for duplicate name
                            if (list.containsKey(name)) {
                                for (int nr = 1; nr < 10; nr++) {
                                    if (list.containsKey(name + "_" + nr)) {
                                        Log.debug(log, "getImagesForGeocache getImageObject Nr.:" + ii + " ignored: ");
                                        continue; // Name already exists --> next nr
                                    }
                                    name += "_" + nr;
                                    break;
                                }
                            }
                            list.put(name, new URI(uri));
                        } catch (Exception ex) {
                            Log.err(log, "getImagesForGeocache getImageObject Nr.:" + ii, ex);
                        }
                    }
                } catch (Exception ex) {
                    LastAPIError = ex.getLocalizedMessage();
                    Log.err(log, "getImagesForGeocache getJSONArray(\"Images\") ", ex);
                    return ERROR;
                }
            }
        } catch (Exception ex) {
            LastAPIError = ex.getLocalizedMessage();
            Log.err(log, "getImagesForGeocache", ex);
            return ERROR;
        }
        Log.info(log, "getImagesForGeocache done");
        return IO;
    }

    public static void WriteCachesLogsImages_toDB(CB_List<Cache> apiCaches, ArrayList<LogEntry> apiLogs, ArrayList<ImageEntry> apiImages) throws InterruptedException {
        // Auf eventuellen Thread Abbruch reagieren
        Thread.sleep(2);

        Database.Data.beginTransaction();

        CacheDAO cacheDAO = new CacheDAO();
        LogDAO logDAO = new LogDAO();
        ImageDAO imageDAO = new ImageDAO();
        WaypointDAO waypointDAO = new WaypointDAO();

        for (int c = 0; c < apiCaches.size(); c++) {
            Cache cache = apiCaches.get(c);
            Cache aktCache = Database.Data.Query.GetCacheById(cache.Id);

            if (aktCache != null && aktCache.isLive())
                aktCache = null;

            if (aktCache == null) {
                aktCache = cacheDAO.getFromDbByCacheId(cache.Id);
            }
            // Read Detail Info of Cache if not available
            if ((aktCache != null) && (aktCache.detail == null)) {
                aktCache.loadDetail();
            }
            // If Cache into DB, extract saved rating
            if (aktCache != null) {
                cache.Rating = aktCache.Rating;
            }

            // Falls das Update nicht klappt (Cache noch nicht in der DB) Insert machen
            if (!cacheDAO.UpdateDatabase(cache)) {
                cacheDAO.WriteToDatabase(cache);
            }

            // Notes von Groundspeak überprüfen und evtl. in die DB an die vorhandenen Notes anhängen
            if (cache.getTmpNote() != null) {
                String oldNote = Database.GetNote(cache);
                String newNote = "";
                if (oldNote == null) {
                    oldNote = "";
                }
                String begin = "<Import from Geocaching.com>";
                String end = "</Import from Geocaching.com>";
                int iBegin = oldNote.indexOf(begin);
                int iEnd = oldNote.indexOf(end);
                if ((iBegin >= 0) && (iEnd > iBegin)) {
                    // Note from Groundspeak already in Database
                    // -> Replace only this part in whole Note
                    newNote = oldNote.substring(0, iBegin - 1) + System.getProperty("line.separator"); // Copy the old part of Note before
                    // the beginning of the groundspeak
                    // block
                    newNote += begin + System.getProperty("line.separator");
                    newNote += cache.getTmpNote();
                    newNote += System.getProperty("line.separator") + end;
                    newNote += oldNote.substring(iEnd + end.length(), oldNote.length());
                } else {
                    newNote = oldNote + System.getProperty("line.separator");
                    newNote += begin + System.getProperty("line.separator");
                    newNote += cache.getTmpNote();
                    newNote += System.getProperty("line.separator") + end;
                }
                cache.setTmpNote(newNote);
                Database.SetNote(cache, cache.getTmpNote());
            }

            // Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
            cache.setLongDescription("");

            for (LogEntry log : apiLogs) {
                if (log.CacheId != cache.Id)
                    continue;
                // Write Log to database

                logDAO.WriteToDatabase(log);
            }

            for (ImageEntry image : apiImages) {
                if (image.CacheId != cache.Id)
                    continue;
                // Write Image to database

                imageDAO.WriteToDatabase(image, false);
            }

            for (int i = 0, n = cache.waypoints.size(); i < n; i++) {
                // must Cast to Full Waypoint. If Waypoint, is wrong createt!
                Waypoint waypoint = cache.waypoints.get(i);
                boolean update = true;

                // dont refresh wp if aktCache.wp is user changed
                if (aktCache != null) {
                    if (aktCache.waypoints != null) {
                        for (int j = 0, m = aktCache.waypoints.size(); j < m; j++) {
                            Waypoint wp = aktCache.waypoints.get(j);
                            if (wp.getGcCode().equalsIgnoreCase(waypoint.getGcCode())) {
                                if (wp.IsUserWaypoint)
                                    update = false;
                                break;
                            }
                        }
                    }
                }

                if (update) {
                    // do not store replication information when importing caches with GC api
                    if (!waypointDAO.UpdateDatabase(waypoint, false)) {
                        waypointDAO.WriteToDatabase(waypoint, false); // do not store replication information here
                    }
                }

            }

            if (aktCache == null) {
                Database.Data.Query.add(cache);
                // cacheDAO.WriteToDatabase(cache);
            } else {
                // 2012-11-17: do not remove old instance from Query because of problems with cacheList and MapView
                // Database.Data.Query.remove(Database.Data.Query.GetCacheById(cache.Id));
                // Database.Data.Query.add(cache);
                aktCache.copyFrom(cache);
                // cacheDAO.UpdateDatabase(cache);
            }

        }
        Database.Data.setTransactionSuccessful();
        Database.Data.endTransaction();

        Database.Data.GPXFilenameUpdateCacheCount();

    }

    public static int createTrackableLog(Trackable TB, String cacheCode, int LogTypeId, Date dateLogged, String note) {
        return createTrackableLog(TB.getGcCode(), TB.getTrackingNumber(), cacheCode, LogTypeId, dateLogged, note);
    }

    public static int createTrackableLog(String TbCode, String TrackingNummer, String cacheCode, int LogTypeId, Date dateLogged, String note) {
        Log.info(log, "createTrackableLog");
        LastAPIError = "";
        if (cacheCode == null) cacheCode = "";
        int chk = chkMembership(false);
        if (chk < 0) return ERROR;
        try {
            Webb httpClient = Webb.create();
            JSONObject response = httpClient
                    .post(getUrl("CreateTrackableLog?format=json"))
                    .body(new JSONObject()
                            .put("AccessToken", GetAccessToken())
                            .put("CacheCode", cacheCode)
                            .put("LogType", String.valueOf(LogTypeId))
                            .put("UTCDateLogged", prepareUTCDate(dateLogged))
                            .put("Note", prepareNote(note))
                            .put("TravelBugCode", String.valueOf(TbCode))
                            .put("TrackingNumber", String.valueOf(TrackingNummer)))
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            // todo rework error handling ... possibly "The service is unavailable"
            int ret = getAPICallStatus(response.getJSONObject("Status"));
            if (ret != 0) {
                return ret;
            }
        } catch (Exception ex) {
            LastAPIError = ex.getLocalizedMessage();
            Log.err(log, "CreateTrackableLog", ex);
            return ERROR;
        }
        Log.info(log, "createTrackableLog done");
        return IO;
    }

    public static int updateCacheNote(String cacheCode, String notes) {
        Log.info(log, "updateCacheNote");
        LastAPIError = "";
        if (cacheCode == null || cacheCode.length() == 0) return ERROR;
        if (!IsPremiumMember()) return ERROR;
        try {
            Webb httpClient = Webb.create();
            JSONObject response = httpClient
                    .post(getUrl("UpdateCacheNote?format=json"))
                    .body(
                            new JSONObject()
                                    .put("AccessToken", GetAccessToken())
                                    .put("CacheCode", cacheCode)
                                    .put("Note", prepareNote(notes))
                    )
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .ensureSuccess()
                    .asJsonObject()
                    .getBody();
            // todo rework error handling ... possibly "The service is unavailable"
            int ret = getAPICallStatus(response);
            if (ret != 0) {
                return ret;
            }
        } catch (Exception ex) {
            LastAPIError = ex.getLocalizedMessage();
            Log.err(log, "UpdateCacheNote", ex);
            return ERROR;
        }
        Log.info(log, "updateCacheNote done");
        return IO;
    }

    private static String getUrl(String command) {
        String url;
        if (CB_Core_Settings.StagingAPI.getValue()) {
            url = STAGING_GS_LIVE_URL;
        } else {
            url = GS_LIVE_URL;
        }
        return url + command;
    }

    public static int chkMembership(boolean withoutMsg) {
        boolean isValid = false;
        if (mAPIKeyChecked) {
            isValid = membershipType > 0;
            return isValid ? 0 : 1;
        }
        int ret = 0;
        if (GetAccessToken().length() > 0) {

            if (!isValid) {
                ret = GetMembershipType(null);
                isValid = membershipType > 0;
                if (ret < 0)
                    return ret;
            }
            isValid = membershipType > 0;
        }

        if (!isValid && ret != CONNECTION_TIMEOUT) {
            if (!withoutMsg)
                API_ErrorEventHandlerList.callInvalidApiKey(API_ErrorEventHandlerList.API_ERROR.INVALID);
        }

        if (ret != CONNECTION_TIMEOUT)
            mAPIKeyChecked = true;
        else
            return CONNECTION_TIMEOUT;

        return ret;
    }

    public static int isValidAPI_Key(boolean withoutMsg) {
        if (mAPIKeyChecked)
            return membershipType;
        return chkMembership(withoutMsg);
    }

    public static boolean isAPIKeyChecked() {
        return mAPIKeyChecked;
    }

    public static boolean isDownloadLimitExceeded() {
        return mDownloadLimitExceeded;
    }

    public static void setDownloadLimitExceeded() {
        mDownloadLimitExceeded = true;
    }

    private static int getAPICallStatus(JSONObject status) {
        int ret = status.getInt("StatusCode");
        if (ret > 0) {
            if (ret == 140) {
                // API-Limit überschritten -> nach etwas Verzögerung wiederholen!
            } else {
                LastAPIError = status.toString();
                Log.err(log, "APICallStatus\n" + LastAPIError);
                ret = ERROR;
            }
        }
        return ret;
    }
}
