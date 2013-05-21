package CB_Core.Api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.CacheTypes;
import CB_Core.Log.Logger;
import CB_Core.Map.Descriptor;
import CB_Core.Types.Cache;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.TbList;
import CB_Core.Types.Trackable;
import CB_Core.Types.Waypoint;

public class GroundspeakAPI
{
	public static final String GS_LIVE_URL = "https://api.groundspeak.com/LiveV6/geocaching.svc/";
	public static final String STAGING_GS_LIVE_URL = "https://staging.api.groundspeak.com/Live/V6Beta/geocaching.svc/";

	public static final int IO = 0;
	public static final int ERROR = -1;
	public static final int CONNECTION_TIMEOUT = -2;
	public static final int API_ERROR = -3;

	public static String LastAPIError = "";
	public static boolean CacheStatusValid = false;
	public static int CachesLeft = -1;
	public static int CurrentCacheCount = -1;
	public static int MaxCacheCount = -1;
	public static boolean CacheStatusLiteValid = false;
	public static int CachesLeftLite = -1;
	public static int CurrentCacheCountLite = -1;
	public static int MaxCacheCountLite = -1;
	public static String MemberName = ""; // this will be filled by
											// GetMembershipType

	/**
	 * 0: Guest??? 1: Basic 2: Charter??? 3: Premium
	 */
	private static int membershipType = -1;

	public static boolean IsPremiumMember(String accessToken)
	{
		if (membershipType < 0) membershipType = GetMembershipType(accessToken);
		return membershipType == 3;
	}

	public static String GetUTCDate(Date date)
	{
		long utc = date.getTime();
		TimeZone tz = TimeZone.getDefault();
		TimeZone tzp = TimeZone.getTimeZone("GMT-8");
		int offset = tz.getOffset(utc);
		utc += offset - tzp.getOffset(utc);
		return "\\/Date(" + utc + ")\\/";
	}

	public static String ConvertNotes(String note)
	{
		String result = note.replace("\r", "");
		result = result.replace("\"", "\\\"");
		return result.replace("\n", "\\n");
	}

	/**
	 * Upload FieldNotes
	 * 
	 * @param accessToken
	 * @param cacheCode
	 * @param wptLogTypeId
	 * @param dateLogged
	 * @param note
	 * @return
	 */
	public static int CreateFieldNoteAndPublish(String accessToken, String cacheCode, int wptLogTypeId, Date dateLogged, String note)
	{
		int chk = chkMemperShip(accessToken);
		if (chk < 0) return chk;

		String URL = Config.settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

		try
		{
			HttpPost httppost = new HttpPost(URL + "CreateFieldNoteAndPublish?format=json");
			String requestString = "";
			requestString = "{";
			requestString += "\"AccessToken\":\"" + accessToken + "\",";
			requestString += "\"CacheCode\":\"" + cacheCode + "\",";
			requestString += "\"WptLogTypeId\":" + String.valueOf(wptLogTypeId) + ",";
			requestString += "\"UTCDateLogged\":\"" + GetUTCDate(dateLogged) + "\",";
			requestString += "\"Note\":\"" + ConvertNotes(note) + "\",";
			requestString += "\"PromoteToLog\":false,";
			requestString += "\"FavoriteThisCache\":false";
			requestString += "}";

			httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

			// Execute HTTP Post Request
			String result = Execute(httppost);

			// Parse JSON Result
			try
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					result = "";
					LastAPIError = "";
				}
				else
				{
					result = "StatusCode = " + status.getInt("StatusCode") + "\n";
					result += status.getString("StatusMessage") + "\n";
					result += status.getString("ExceptionDetails");
					LastAPIError = result;
					return -1;
				}

			}
			catch (JSONException e)
			{
				e.printStackTrace();
				Logger.Error("UploadFieldNotesAPI", "JSON-Error", e);
				LastAPIError = e.getMessage();
				return -1;
			}

		}
		catch (ConnectTimeoutException e)
		{
			Logger.Error("UploadFieldNotesAPI", "ConnectTimeoutException", e);
			return CONNECTION_TIMEOUT;
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.Error("UploadFieldNotesAPI", "UnsupportedEncodingException", e);
			return ERROR;
		}
		catch (ClientProtocolException e)
		{
			Logger.Error("UploadFieldNotesAPI", "ClientProtocolException", e);
			return ERROR;
		}
		catch (IOException e)
		{
			Logger.Error("UploadFieldNotesAPI", "IOException", e);
			return ERROR;
		}

		LastAPIError = "";
		return 0;
	}

	/**
	 * Load Number of founds form geocaching.com
	 * 
	 * @param accessToken
	 * @return
	 */
	public static int GetCachesFound(String accessToken)
	{

		int chk = chkMemperShip(accessToken);
		if (chk < 0) return chk;

		String URL = Config.settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

		try
		{
			HttpPost httppost = new HttpPost(URL + "GetYourUserProfile?format=json");
			String requestString = "";
			requestString = "{";
			requestString += "\"AccessToken\":\"" + accessToken + "\",";
			requestString += "\"ProfileOptions\":{";
			requestString += "}" + ",";
			requestString += getDeviceInfoRequestString();
			requestString += "}";

			httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

			// Execute HTTP Post Request
			String result = Execute(httppost);

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					result = "";
					JSONObject profile = json.getJSONObject("Profile");
					JSONObject user = (JSONObject) profile.getJSONObject("User");
					return user.getInt("FindCount");

				}
				else
				{
					result = "StatusCode = " + status.getInt("StatusCode") + "\n";
					result += status.getString("StatusMessage") + "\n";
					result += status.getString("ExceptionDetails");

					return (-1);
				}

			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}
		catch (ConnectTimeoutException e)
		{
			Logger.Error("GetCachesFound", "ConnectTimeoutException", e);
			return CONNECTION_TIMEOUT;
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.Error("GetCachesFound", "UnsupportedEncodingException", e);
			return ERROR;
		}
		catch (ClientProtocolException e)
		{
			Logger.Error("GetCachesFound", "ClientProtocolException", e);
			return ERROR;
		}
		catch (IOException e)
		{
			Logger.Error("GetCachesFound", "IOException", e);
			return ERROR;
		}

		return (ERROR);
	}

	/**
	 * Loads the Membership type -1: Error 0: Guest??? 1: Basic 2: Charter??? 3: Premium
	 * 
	 * @param accessToken
	 * @return
	 */
	public static int GetMembershipType(String accessToken)
	{
		if (API_isCheked) return membershipType;
		String URL = Config.settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

		try
		{
			HttpPost httppost = new HttpPost(URL + "GetYourUserProfile?format=json");
			String requestString = "";
			requestString = "{";
			requestString += "\"AccessToken\":\"" + accessToken + "\",";
			requestString += "\"ProfileOptions\":{";
			requestString += "}" + ",";
			requestString += getDeviceInfoRequestString();
			requestString += "}";

			httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

			// Execute HTTP Post Request
			String result = Execute(httppost);

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					result = "";
					JSONObject profile = json.getJSONObject("Profile");
					JSONObject user = (JSONObject) profile.getJSONObject("User");
					JSONObject memberType = (JSONObject) user.getJSONObject("MemberType");
					int memberTypeId = memberType.getInt("MemberTypeId");
					MemberName = user.getString("UserName");
					membershipType = memberTypeId;
					API_isCheked = true;
					// Zurücksetzen, falls ein anderer User gewählt wurde
					return memberTypeId;
				}
				else
				{
					result = "StatusCode = " + status.getInt("StatusCode") + "\n";
					result += status.getString("StatusMessage") + "\n";
					result += status.getString("ExceptionDetails");

					Logger.Error("GetMembershipType", "API-Error");
					API_isCheked = false;
					return API_ERROR;
				}

			}
			catch (JSONException e)
			{
				Logger.Error("GetMembershipType", "JSONException", e);
				API_isCheked = false;
				return API_ERROR;
			}

		}
		catch (ConnectTimeoutException e)
		{
			Logger.Error("GetMembershipType", "ConnectTimeoutException", e);
			API_isCheked = false;
			return CONNECTION_TIMEOUT;
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.Error("GetMembershipType", "UnsupportedEncodingException", e);
			API_isCheked = false;
			return ERROR;
		}
		catch (ClientProtocolException e)
		{
			Logger.Error("GetMembershipType", "ClientProtocolException", e);
			API_isCheked = false;
			return ERROR;
		}
		catch (IOException e)
		{
			Logger.Error("GetMembershipType", "IOException", e);
			API_isCheked = false;
			return ERROR;
		}
	}

	/**
	 * Gets the Status for the given Caches
	 * 
	 * @param accessToken
	 * @param caches
	 * @return
	 */
	public static int GetGeocacheStatus(String accessToken, ArrayList<Cache> caches)
	{
		int chk = chkMemperShip(accessToken);
		if (chk < 0) return chk;

		String URL = Config.settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

		try
		{
			HttpPost httppost = new HttpPost(URL + "GetGeocacheStatus?format=json");
			String requestString = "";
			requestString = "{";
			requestString += "\"AccessToken\":\"" + accessToken + "\",";
			requestString += "\"CacheCodes\":[";

			int i = 0;
			for (Cache cache : caches)
			{
				requestString += "\"" + cache.GcCode + "\"";
				if (i < caches.size() - 1) requestString += ",";
				i++;
			}

			requestString += "]";
			requestString += "}";

			httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

			String result = Execute(httppost);

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					result = "";
					JSONArray geocacheStatuses = json.getJSONArray("GeocacheStatuses");
					for (int ii = 0; ii < geocacheStatuses.length(); ii++)
					{
						JSONObject jCache = (JSONObject) geocacheStatuses.get(ii);

						Iterator<Cache> iterator = caches.iterator();

						do
						{
							Cache tmp = iterator.next();

							if (jCache.getString("CacheCode").equals(tmp.GcCode))
							{
								tmp.Archived = jCache.getBoolean("Archived");
								tmp.Available = jCache.getBoolean("Available");
								tmp.NumTravelbugs = jCache.getInt("TrackableCount");
							}
						}
						while (iterator.hasNext());

					}

					return 0;
				}
				else
				{
					result = "StatusCode = " + status.getInt("StatusCode") + "\n";
					result += status.getString("StatusMessage") + "\n";
					result += status.getString("ExceptionDetails");
					LastAPIError = result;
					return (-1);
				}

			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}
		catch (ConnectTimeoutException e)
		{
			Logger.Error("GetGeocacheStatus", "ConnectTimeoutException", e);
			return CONNECTION_TIMEOUT;
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.Error("GetGeocacheStatus", "UnsupportedEncodingException", e);
			return ERROR;
		}
		catch (ClientProtocolException e)
		{
			Logger.Error("GetGeocacheStatus", "ClientProtocolException", e);
			return ERROR;
		}
		catch (IOException e)
		{
			Logger.Error("GetGeocacheStatus", "IOException", e);
			return ERROR;
		}

		return (-1);
	}

	/**
	 * returns Status Code (0 -> OK)
	 * 
	 * @param accessToken
	 * @return
	 */
	public static int GetCacheLimits(String accessToken)
	{
		int chk = chkMemperShip(accessToken);
		if (chk < 0) return chk;

		String URL = Config.settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

		LastAPIError = "";
		// zum Abfragen der CacheLimits einfach nach einem Cache suchen, der
		// nicht existiert.
		// dadurch wird der Zähler nicht erhöht, die Limits aber zurückgegeben.
		try
		{
			HttpPost httppost = new HttpPost(URL + "SearchForGeocaches?format=json");
			try
			{
				JSONObject request = new JSONObject();
				request.put("AccessToken", accessToken);
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

				// Execute HTTP Post Request
				String result = Execute(httppost);

				// Parse JSON Result

				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				int status = checkCacheStatus(json, false);
				// hier keine Überprüfung des Status, da dieser z.B. 118
				// (Überschreitung des Limits) sein kann, aber der CacheStatus
				// aber trotzdem drin ist.
				return status;
			}
			catch (JSONException e)
			{
				e.printStackTrace();
				System.out.println(e.getMessage());
				LastAPIError = "API Error: " + e.getMessage();
				return -2;
			}

		}
		catch (ConnectTimeoutException e)
		{
			Logger.Error("GetGeocacheStatus", "ConnectTimeoutException", e);
			return CONNECTION_TIMEOUT;
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.Error("GetGeocacheStatus", "UnsupportedEncodingException", e);
			return ERROR;
		}
		catch (ClientProtocolException e)
		{
			Logger.Error("GetGeocacheStatus", "ClientProtocolException", e);
			return ERROR;
		}
		catch (IOException e)
		{
			Logger.Error("GetGeocacheStatus", "IOException", e);
			return ERROR;
		}
	}

	// liest den Status aus dem gegebenen json Object aus.
	static int checkStatus(JSONObject json)
	{
		LastAPIError = "";
		try
		{
			JSONObject status = json.getJSONObject("Status");
			if (status.getInt("StatusCode") == 0)
			{
				return 0;
			}
			else
			{
				LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
				LastAPIError += status.getString("StatusMessage") + "\n";
				LastAPIError += status.getString("ExceptionDetails");
				return status.getInt("StatusCode");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
			LastAPIError = "API Error: " + e.getMessage();
			return -3;
		}
	}

	// liest den CacheStatus aus dem gegebenen json Object aus.
	// darin ist gespeichert, wie viele Full Caches schon geladen wurden und wie
	// viele noch frei sind
	static int checkCacheStatus(JSONObject json, boolean isLite)
	{
		LastAPIError = "";
		try
		{
			JSONObject cacheLimits = json.getJSONObject("CacheLimits");
			if (isLite)
			{
				CachesLeftLite = cacheLimits.getInt("CachesLeft");
				CurrentCacheCountLite = cacheLimits.getInt("CurrentCacheCount");
				MaxCacheCountLite = cacheLimits.getInt("MaxCacheCount");
				CacheStatusLiteValid = true;
			}
			else
			{
				CachesLeft = cacheLimits.getInt("CachesLeft");
				CurrentCacheCount = cacheLimits.getInt("CurrentCacheCount");
				MaxCacheCount = cacheLimits.getInt("MaxCacheCount");
				CacheStatusValid = true;
			}
			return 0;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
			LastAPIError = "API Error: " + e.getMessage();
			return -4;
		}
	}

	static int getCacheSize(int containerTypeId)
	{
		switch (containerTypeId)
		{
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

	static CacheTypes getCacheType(int apiTyp)
	{
		switch (apiTyp)
		{
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
			return CacheTypes.Cache; // Cache In Trash Out Event
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
		case 218:
			return CacheTypes.MultiQuestion;

		default:
			return CacheTypes.Cache;

		}
	}

	/**
	 * Ruft die Liste der TB´s ab, die im Besitz des Users sind
	 * 
	 * @param String
	 *            accessToken
	 * @param TbList
	 *            list
	 * @return
	 */
	public static int getMyTbList(String accessToken, TbList list)
	{
		int chk = chkMemperShip(accessToken);
		if (chk < 0) return chk;

		String URL = Config.settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

		try
		{
			HttpPost httppost = new HttpPost(URL + "GetUsersTrackables?format=json");
			try
			{
				JSONObject request = new JSONObject();
				request.put("AccessToken", accessToken);
				request.put("MaxPerPage", 30);

				String requestString = request.toString();

				httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

				// Execute HTTP Post Request
				String result = Execute(httppost);

				// Parse JSON Result
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					LastAPIError = "";
					JSONArray jTrackables = json.getJSONArray("Trackables");

					for (int ii = 0; ii < jTrackables.length(); ii++)
					{
						JSONObject jTrackable = (JSONObject) jTrackables.get(ii);
						list.add(new Trackable(jTrackable));
					}
					return 0;
				}
				else
				{
					LastAPIError = "";
					LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
					LastAPIError += status.getString("StatusMessage") + "\n";
					LastAPIError += status.getString("ExceptionDetails");

					return (-1);
				}

			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}
		catch (ConnectTimeoutException e)
		{
			Logger.Error("GetGeocacheStatus", "ConnectTimeoutException", e);
			return CONNECTION_TIMEOUT;
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.Error("GetGeocacheStatus", "UnsupportedEncodingException", e);
			return ERROR;
		}
		catch (ClientProtocolException e)
		{
			Logger.Error("GetGeocacheStatus", "ClientProtocolException", e);
			return ERROR;
		}
		catch (IOException e)
		{
			Logger.Error("GetGeocacheStatus", "IOException", e);
			return ERROR;
		}

		return (-1);
	}

	public static int getTBbyTreckNumber(String accessToken, String TrackingCode, Trackable TB)
	{
		int chk = chkMemperShip(accessToken);
		if (chk < 0) return chk;

		String URL = Config.settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

		try
		{
			HttpGet httppost = new HttpGet(URL + "GetTrackablesByTrackingNumber?AccessToken=" + accessToken + "&trackingNumber="
					+ TrackingCode + "&format=json");

			String result = Execute(httppost);

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					LastAPIError = "";
					JSONArray jTrackables = json.getJSONArray("Trackables");

					for (int i = 0; i < jTrackables.length();)
					{
						JSONObject jTrackable = (JSONObject) jTrackables.get(i);
						TB = new Trackable(jTrackable);
						TB.setTrackingCode(TrackingCode);
						return IO;
					}
				}
				else
				{
					LastAPIError = "";
					LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
					LastAPIError += status.getString("StatusMessage") + "\n";
					LastAPIError += status.getString("ExceptionDetails");
					TB = null;
					return ERROR;
				}

			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}
		catch (ConnectTimeoutException e)
		{
			Logger.Error("getTBbyTreckNumber", "ConnectTimeoutException", e);
			TB = null;
			return CONNECTION_TIMEOUT;
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.Error("getTBbyTreckNumber", "UnsupportedEncodingException", e);
			TB = null;
			return ERROR;
		}
		catch (ClientProtocolException e)
		{
			Logger.Error("getTBbyTreckNumber", "ClientProtocolException", e);
			TB = null;
			return ERROR;
		}
		catch (IOException e)
		{
			Logger.Error("getTBbyTreckNumber", "IOException", e);
			TB = null;
			return ERROR;
		}

		TB = null;
		return ERROR;
	}

	public static int getTBbyTbCode(String accessToken, String TrackingNumber, Trackable TB)
	{
		int chk = chkMemperShip(accessToken);
		if (chk < 0) return chk;

		String URL = Config.settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

		try
		{
			HttpGet httppost = new HttpGet(URL + "GetTrackablesByTBCode?AccessToken=" + accessToken + "&tbCode=" + TrackingNumber
					+ "&format=json");

			String result = Execute(httppost);

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					LastAPIError = "";
					JSONArray jTrackables = json.getJSONArray("Trackables");

					for (int ii = 0; ii < jTrackables.length();)
					{
						JSONObject jTrackable = (JSONObject) jTrackables.get(ii);
						TB = new Trackable(jTrackable);
						return IO;
					}
				}
				else
				{
					LastAPIError = "";
					LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
					LastAPIError += status.getString("StatusMessage") + "\n";
					LastAPIError += status.getString("ExceptionDetails");

					return ERROR;
				}

			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}
		catch (ConnectTimeoutException e)
		{
			Logger.Error("getTBbyTbCode", "ConnectTimeoutException", e);
			TB = null;
			return CONNECTION_TIMEOUT;
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.Error("getTBbyTbCode", "UnsupportedEncodingException", e);
			TB = null;
			return ERROR;
		}
		catch (ClientProtocolException e)
		{
			Logger.Error("getTBbyTbCode", "ClientProtocolException", e);
			TB = null;
			return ERROR;
		}
		catch (IOException e)
		{
			Logger.Error("getTBbyTbCode", "IOException", e);
			TB = null;
			return ERROR;
		}

		TB = null;
		return ERROR;

	}

	/**
	 * Ruft die Liste der Bilder ab, die in einem Cache sind
	 * 
	 * @param String
	 *            accessToken
	 * @param TbList
	 *            list
	 * @return
	 */
	public static int getImagesForGeocache(String accessToken, String cacheCode, ArrayList<String> images)
	{
		int chk = chkMemperShip(accessToken);
		if (chk < 0) return chk;

		String URL = Config.settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;

		try
		{
			HttpGet httppost = new HttpGet(URL + "GetImagesForGeocache?AccessToken=" + accessToken + "&CacheCode=" + cacheCode
					+ "&format=json");

			String result = Execute(httppost);

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					LastAPIError = "";
					JSONArray jImages = json.getJSONArray("Images");

					for (int ii = 0; ii < jImages.length(); ii++)
					{
						JSONObject jImage = (JSONObject) jImages.get(ii);
						images.add(jImage.getString("Url"));
					}
					return 0;
				}
				else
				{
					LastAPIError = "";
					LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
					LastAPIError += status.getString("StatusMessage") + "\n";
					LastAPIError += status.getString("ExceptionDetails");

					return (-1);
				}

			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}
		catch (ConnectTimeoutException e)
		{
			Logger.Error("getImagesForGeocache", "ConnectTimeoutException", e);
			return CONNECTION_TIMEOUT;
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.Error("getImagesForGeocache", "UnsupportedEncodingException", e);
			return ERROR;
		}
		catch (ClientProtocolException e)
		{
			Logger.Error("getImagesForGeocache", "ClientProtocolException", e);
			return ERROR;
		}
		catch (IOException e)
		{
			Logger.Error("getImagesForGeocache", "IOException", e);
			return ERROR;
		}

		return (-1);
	}

	public static int GetAllImageLinks(String accessToken, String cacheCode, HashMap<String, URI> list)
	{
		int chk = chkMemperShip(accessToken);
		if (chk < 0) return chk;

		String URL = Config.settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
		if (list == null) list = new HashMap<String, URI>();
		try
		{
			HttpGet httppost = new HttpGet(URL + "GetImagesForGeocache?AccessToken=" + accessToken + "&CacheCode=" + cacheCode
					+ "&format=json");

			String result = Execute(httppost);

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					LastAPIError = "";
					JSONArray jImages = json.getJSONArray("Images");

					for (int ii = 0; ii < jImages.length(); ii++)
					{
						JSONObject jImage = (JSONObject) jImages.get(ii);
						list.put(jImage.getString("Name"), new URI(jImage.getString("Url")));
					}
					return IO;
				}
				else
				{
					LastAPIError = "";
					LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
					LastAPIError += status.getString("StatusMessage") + "\n";
					LastAPIError += status.getString("ExceptionDetails");

					list = null;
					return ERROR;
				}

			}
			catch (JSONException e)
			{
				Logger.Error("getTBbyTbCode", "JSONException", e);
			}
			catch (URISyntaxException e)
			{
				Logger.Error("getTBbyTbCode", "URISyntaxException", e);
			}

		}
		catch (ConnectTimeoutException e)
		{
			Logger.Error("getTBbyTbCode", "ConnectTimeoutException", e);
			list = null;
			return CONNECTION_TIMEOUT;
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.Error("getTBbyTbCode", "UnsupportedEncodingException", e);
			list = null;
			return ERROR;
		}
		catch (ClientProtocolException e)
		{
			Logger.Error("getTBbyTbCode", "ClientProtocolException", e);
			list = null;
			return ERROR;
		}
		catch (IOException e)
		{
			Logger.Error("getTBbyTbCode", "IOException", e);
			list = null;
			return ERROR;
		}

		list = null;
		return ERROR;
	}

	/**
	 * Fürt ein Http Request aus und gibt die Antwort als String zurück. Da ein HttpRequestBase übergeben wird kann ein HttpGet oder
	 * HttpPost zum Ausführen übergeben werden.
	 * 
	 * @param httprequest
	 *            HttpGet oder HttpPost
	 * @return Die Antwort als String.
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static String Execute(HttpRequestBase httprequest) throws IOException, ClientProtocolException, ConnectTimeoutException
	{
		httprequest.setHeader("Accept", "application/json");
		httprequest.setHeader("Content-type", "application/json");

		// Execute HTTP Post Request
		String result = "";

		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		// The default value is zero, that means the timeout is not used.

		HttpConnectionParams.setConnectionTimeout(httpParameters, Config.settings.conection_timeout.getValue());
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for data.

		HttpConnectionParams.setSoTimeout(httpParameters, Config.settings.socket_timeout.getValue());

		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

		HttpResponse response = httpClient.execute(httprequest);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while ((line = rd.readLine()) != null)
		{
			result += line + "\n";
		}
		return result;
	}

	public static void WriteCachesLogsImages_toDB(ArrayList<Cache> apiCaches, ArrayList<LogEntry> apiLogs, ArrayList<ImageEntry> apiImages)
			throws InterruptedException
	{
		// Auf eventuellen Thread Abbruch reagieren
		Thread.sleep(2);

		Database.Data.beginTransaction();

		CacheDAO cacheDAO = new CacheDAO();
		LogDAO logDAO = new LogDAO();
		ImageDAO imageDAO = new ImageDAO();
		WaypointDAO waypointDAO = new WaypointDAO();

		for (Cache cache : apiCaches)
		{
			cache.MapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, cache.Longitude());
			cache.MapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, cache.Latitude());
			Cache aktCache = Database.Data.Query.GetCacheById(cache.Id);
			Cache altCache = Database.Data.Query.GetCacheById(cache.Id);
			if (aktCache == null)
			{
				Database.Data.Query.add(cache);
				// cacheDAO.WriteToDatabase(cache);
			}
			else
			{
				// 2012-11-17: do not remove old instance from Query because of problems with cacheList and MapView
				// Database.Data.Query.remove(Database.Data.Query.GetCacheById(cache.Id));
				// Database.Data.Query.add(cache);
				aktCache.copyFrom(cache);
				// cacheDAO.UpdateDatabase(cache);
			}
			// Falls das Update nicht klappt (Cache noch nicht in der DB) Insert machen
			if (!cacheDAO.UpdateDatabase(cache))
			{
				cacheDAO.WriteToDatabase(cache);
			}

			for (LogEntry log : apiLogs)
			{
				if (log.CacheId != cache.Id) continue;
				// Write Log to database

				logDAO.WriteToDatabase(log);
			}

			for (ImageEntry image : apiImages)
			{
				if (image.CacheId != cache.Id) continue;
				// Write Image to database

				imageDAO.WriteToDatabase(image, false);
			}

			for (Waypoint waypoint : cache.waypoints)
			{
				boolean update = true;

				// dont refresh wp if aktCache.wp is user changed
				if (altCache != null)
				{
					for (Waypoint wp : altCache.waypoints)
					{
						if (wp.GcCode.equalsIgnoreCase(waypoint.GcCode))
						{
							if (wp.IsUserWaypoint) update = false;
							break;
						}
					}
				}

				if (update)
				{
					if (!waypointDAO.UpdateDatabase(waypoint))
					{
						waypointDAO.WriteToDatabase(waypoint);
					}
				}

			}

		}
		Database.Data.setTransactionSuccessful();
		Database.Data.endTransaction();

		Database.Data.GPXFilenameUpdateCacheCount();
	}

	private static String getDeviceInfoRequestString()
	{
		String string = "\"DeviceInfo\":{";

		string += "\"ApplicationCurrentMemoryUsage\":\"" + String.valueOf(2147483647) + "\",";
		string += "\"ApplicationPeakMemoryUsage\":\"" + String.valueOf(2147483647) + "\",";
		string += "\"ApplicationSoftwareVersion\":\"" + GlobalCore.getVersionString() + "\",";
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

	private static boolean API_isCheked = false;

	/**
	 * Returns True if the APY-Key INVALID
	 * 
	 * @param accessToken
	 * @return
	 */
	private static int chkMemperShip(String accessToken)
	{
		return chkMemperShip(accessToken, false);
	}

	/**
	 * Returns True if the APY-Key INVALID
	 * 
	 * @param accessToken
	 * @return 0=false 1=true
	 */
	private static int chkMemperShip(String accessToken, boolean withoutMsg)
	{
		boolean isValid = false;
		if (API_isCheked)
		{
			isValid = membershipType > 0;
			return isValid ? 0 : 1;
		}
		int ret = 0;
		if (accessToken.length() > 0)
		{

			if (!isValid)
			{
				ret = GetMembershipType(accessToken);
				if (ret < 0) return ret;
			}
			isValid = membershipType > 0;
		}

		if (!isValid && ret != CONNECTION_TIMEOUT)
		{
			if (!withoutMsg) API_ErrorEventHandlerList.callInvalidApiKey();
		}

		if (ret != CONNECTION_TIMEOUT) API_isCheked = true;
		return isValid ? 0 : 1;
	}

	public static boolean isValidAPI_Key()
	{
		return isValidAPI_Key(false);
	}

	public static boolean isValidAPI_Key(boolean withoutMsg)
	{
		if (API_isCheked) return membershipType > 0;

		chkMemperShip(Config.GetAccessToken(), withoutMsg);

		return membershipType > 0;

	}

	public static int createTrackableLog(String accessToken, Trackable TB, String cacheCode, int LogTypeId, Date dateLogged, String note)
	{
		return createTrackableLog(accessToken, TB.getGcCode(), TB.getTrackingNumber(), cacheCode, LogTypeId, dateLogged, note);
	}

	public static int createTrackableLog(String accessToken, String TbCode, String TrackingNummer, String cacheCode, int LogTypeId,
			Date dateLogged, String note)
	{
		int chk = chkMemperShip(accessToken);
		if (chk < 0) return chk;
		String URL = Config.settings.StagingAPI.getValue() ? STAGING_GS_LIVE_URL : GS_LIVE_URL;
		if (cacheCode == null) cacheCode = "";

		try
		{
			HttpPost httppost = new HttpPost(URL + "CreateTrackableLog?format=json");
			String requestString = "";
			requestString = "{";
			requestString += "\"AccessToken\":\"" + accessToken + "\",";
			requestString += "\"CacheCode\":\"" + cacheCode + "\",";
			requestString += "\"LogType\":" + String.valueOf(LogTypeId) + ",";
			requestString += "\"UTCDateLogged\":\"" + GetUTCDate(dateLogged) + "\",";
			requestString += "\"Note\":\"" + ConvertNotes(note) + "\",";
			requestString += "\"TravelBugCode\":\"" + String.valueOf(TbCode) + "\",";
			requestString += "\"TrackingNumber\":\"" + String.valueOf(TrackingNummer) + "\"";
			requestString += "}";

			httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));

			// Execute HTTP Post Request
			String result = Execute(httppost);

			// Parse JSON Result
			try
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					result = "";
					LastAPIError = "";
				}
				else
				{
					result = "StatusCode = " + status.getInt("StatusCode") + "\n";
					result += status.getString("StatusMessage") + "\n";
					result += status.getString("ExceptionDetails");
					LastAPIError = result;
					return -1;
				}

			}
			catch (JSONException e)
			{
				e.printStackTrace();
				Logger.Error("UploadFieldNotesAPI", "JSON-Error", e);
				LastAPIError = e.getMessage();
				return -1;
			}

		}
		catch (ConnectTimeoutException e)
		{
			Logger.Error("createTrackableLog", "ConnectTimeoutException", e);
			return CONNECTION_TIMEOUT;
		}
		catch (UnsupportedEncodingException e)
		{
			Logger.Error("createTrackableLog", "UnsupportedEncodingException", e);
			return ERROR;
		}
		catch (ClientProtocolException e)
		{
			Logger.Error("createTrackableLog", "ClientProtocolException", e);
			return ERROR;
		}
		catch (IOException e)
		{
			Logger.Error("createTrackableLog", "IOException", e);
			return ERROR;
		}

		LastAPIError = "";
		return 0;
	}

}
