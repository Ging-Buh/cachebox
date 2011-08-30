package CB_Core.Api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.DebugGraphics;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import CB_Core.GlobalCore;
import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Enums.LogTypes;
import CB_Core.Log.Logger;
import CB_Core.Types.Cache;
import CB_Core.Types.Category;
import CB_Core.Types.Coordinate;
import CB_Core.Types.GpxFilename;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;

public class GroundspeakAPI {
	public static String LastAPIError = "";
	public static boolean CacheStatusValid = false;
	public static int CachesLeft = -1;
	public static int CurrentCacheCount = -1;
	public static int MaxCacheCount = -1;
	public static boolean CacheStatusLiteValid = false;
	public static int CachesLeftLite = -1;
	public static int CurrentCacheCountLite = -1;
	public static int MaxCacheCountLite = -1;
	public static String MemberName = "";	// this will be filled by GetMembershipType

	// 0: Guest??? 1: Basic 2: Charter??? 3: Premium
	private static int membershipType = -1;

	public static boolean IsPremiumMember(String accessToken) {
		if (membershipType < 0)
			membershipType = GetMembershipType(accessToken);
		return membershipType == 3;
	}

	public static String GetUTCDate(Date date) {
		long utc = date.getTime();
		TimeZone tz = TimeZone.getDefault();
		TimeZone tzp = TimeZone.getTimeZone("GMT-8");
		int offset = tz.getOffset(utc);
		utc += offset - tzp.getOffset(utc);
		return "\\/Date(" + utc + ")\\/";
	}

	public static String ConvertNotes(String note) {
		return note.replace("\n", "\\n");
	}

	/**
	 * Upload FieldNotes
	 */
	public static int CreateFieldNoteAndPublish(String accessToken,
			String cacheCode, int wptLogTypeId, Date dateLogged, String note) {
		String result = "";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"https://api.groundspeak.com/LiveV5/Geocaching.svc/CreateFieldNoteAndPublish?format=json");
			String requestString = "";
			requestString = "{";
			requestString += "\"AccessToken\":\"" + accessToken + "\",";
			requestString += "\"CacheCode\":\"" + cacheCode + "\",";
			requestString += "\"WptLogTypeId\":" + 2 + ",";
			requestString += "\"UTCDateLogged\":\"" + GetUTCDate(dateLogged)
					+ "\",";
			requestString += "\"Note\":\"" + ConvertNotes(note) + "\",";
			requestString += "\"PromoteToLog\":false,";
			requestString += "\"FavoriteThisCache\":false";
			requestString += "}";

			httppost.setEntity(new ByteArrayEntity(requestString
					.getBytes("UTF8")));
			httppost.setHeader("Accept", "application/json");
			httppost.setHeader("Content-type", "application/json");

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				result += line + "\n";
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
					result = "StatusCode = " + status.getInt("StatusCode")
							+ "\n";
					result += status.getString("StatusMessage") + "\n";
					result += status.getString("ExceptionDetails");
					LastAPIError = result;
					return -1;
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Logger.Error("UploadFieldNotesAPI", "JSON-Error", e);
				LastAPIError = e.getMessage();
				return -1;
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			Logger.Error("UploadFieldNotesAPI", "Error", ex);
			LastAPIError = ex.getMessage();
			return -1;
		}

		LastAPIError = "";
		return 0;
	}

	/**
	 * Load Number of founds form geocaching.com
	 */
	public static int GetCachesFound(String accessToken) {
		String result = "";

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"https://api.groundspeak.com/LiveV5/Geocaching.svc/GetYourUserProfile?format=json");
			String requestString = "";
			requestString = "{";
			requestString += "\"AccessToken\":\"" + accessToken + "\",";
			requestString += "\"ProfileOptions\":{";
			requestString += "}";
			requestString += "}";

			httppost.setEntity(new ByteArrayEntity(requestString
					.getBytes("UTF8")));
			httppost.setHeader("Accept", "application/json");
			httppost.setHeader("Content-type", "application/json");

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				result += line + "\n";
			}

			try // Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0) {
					result = "";
					JSONObject profile = json.getJSONObject("Profile");
					JSONObject user = (JSONObject) profile
							.getJSONObject("User");
					return user.getInt("FindCount");

				} else {
					result = "StatusCode = " + status.getInt("StatusCode")
							+ "\n";
					result += status.getString("StatusMessage") + "\n";
					result += status.getString("ExceptionDetails");

					return (-1);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return (-1);
		}

		return (-1);
	}

	/**
	 * Loads the Membership type -1: Error 0: Guest??? 1: Basic 2: Charter??? 3:
	 * Premium
	 */
	public static int GetMembershipType(String accessToken) {
		String result = "";

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"https://api.groundspeak.com/LiveV5/Geocaching.svc/GetYourUserProfile?format=json");
			String requestString = "";
			requestString = "{";
			requestString += "\"AccessToken\":\"" + accessToken + "\",";
			requestString += "\"ProfileOptions\":{";
			requestString += "}";
			requestString += "}";

			httppost.setEntity(new ByteArrayEntity(requestString
					.getBytes("UTF8")));
			httppost.setHeader("Accept", "application/json");
			httppost.setHeader("Content-type", "application/json");

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				result += line + "\n";
			}

			try // Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0) {
					result = "";
					JSONObject profile = json.getJSONObject("Profile");
					JSONObject user = (JSONObject) profile
							.getJSONObject("User");
					JSONObject memberType = (JSONObject) user
							.getJSONObject("MemberType");
					int memberTypeId = memberType.getInt("MemberTypeId");
					MemberName = user.getString("UserName");
					// Zurücksetzen, falls ein anderer User gewählt wurde
					return memberTypeId;
				} else {
					result = "StatusCode = " + status.getInt("StatusCode")
							+ "\n";
					result += status.getString("StatusMessage") + "\n";
					result += status.getString("ExceptionDetails");

					return (-1);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return (-1);
		}

		return (-1);
	}

	/**
	 * Gets the Status for the given Caches
	 */
	public static int GetGeocacheStatus(String accessToken,
			ArrayList<String> caches) {
		String result = "";

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"https://api.groundspeak.com/LiveV5/geocaching.svc/GetGeocacheStatus?format=json");
			String requestString = "";
			requestString = "{";
			requestString += "\"AccessToken\":\"" + accessToken + "\",";
			requestString += "\"CacheCodes\":[";

			int i = 0;
			for (String cache : caches) {
				requestString += "\"" + cache + "\"";
				if (i < caches.size() - 1)
					requestString += ",";
				i++;
			}

			requestString += "]";
			requestString += "}";

			httppost.setEntity(new ByteArrayEntity(requestString
					.getBytes("UTF8")));
			httppost.setHeader("Accept", "application/json");
			httppost.setHeader("Content-type", "application/json");

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				result += line + "\n";
			}

			try // Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0) {
					result = "";
					JSONArray geocacheStatuses = json
							.getJSONArray("GeocacheStatuses");
					for (int ii = 0; ii < geocacheStatuses.length(); ii++) {
						JSONObject jCache = (JSONObject) geocacheStatuses
								.get(ii);
					}

					return 0;
				} else {
					result = "StatusCode = " + status.getInt("StatusCode")
							+ "\n";
					result += status.getString("StatusMessage") + "\n";
					result += status.getString("ExceptionDetails");

					return (-1);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return (-1);
		}

		return (-1);
	}


	// returns Status Code (0 -> OK)
	public static int GetCacheLimits(String accessToken) {
		String result = "";
		LastAPIError = "";
		// zum Abfragen der CacheLimits einfach nach einem Cache suchen, der
		// nicht existiert.
		// dadurch wird der Zähler nicht erhöht, die Limits aber zurückgegeben.
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"https://api.groundspeak.com/LiveV5/Geocaching.svc/SearchForGeocaches?format=json");

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

			httppost.setEntity(new ByteArrayEntity(requestString
					.getBytes("UTF8")));
			httppost.setHeader("Accept", "application/json");
			httppost.setHeader("Content-type", "application/json");

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				result += line + "\n";
			}

			// Parse JSON Result
			try {
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				int status = checkCacheStatus(json, false);
				// hier keine Überprüfung des Status, da dieser z.B. 118 (Überschreitung des Limits) sein kann, aber der CacheStatus aber trotzdem drin ist.
				return status;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(e.getMessage());
				LastAPIError = "API Error: " + e.getMessage();
				return -2;
			}

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			LastAPIError = "API Error: " + ex.getMessage();
			return -1;
		}
	}

	// liest den Status aus dem gegebenen json Object aus.
	static int checkStatus(JSONObject json) {
		LastAPIError = "";
		try {
			JSONObject status = json.getJSONObject("Status");
			if (status.getInt("StatusCode") == 0) {
				return 0;
			} else {
				LastAPIError = "StatusCode = " + status.getInt("StatusCode")
						+ "\n";
				LastAPIError += status.getString("StatusMessage") + "\n";
				LastAPIError += status.getString("ExceptionDetails");
				return status.getInt("StatusCode");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			LastAPIError = "API Error: " + e.getMessage();
			return -3;
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
			return CacheTypes.Cache; // Cache In Trash Out Event
		case 137:
			return CacheTypes.Earth;
		case 453:
			return CacheTypes.MegaEvent;
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

	static LogTypes getLogType(int apiTyp) {
		switch (apiTyp) {
		case 2:
			return LogTypes.found;
		case 3:
			return LogTypes.didnt_find;
		case 4:
			return LogTypes.note;
		case 5:
			return LogTypes.archived;
		case 7:
			return LogTypes.needs_archived;
		case 9:
			return LogTypes.will_attend;
		case 10:
			return LogTypes.attended;
		case 11:
			return LogTypes.webcam_photo_taken;
		case 12:
			return LogTypes.enabled;
		case 24:
			return LogTypes.published;
		case 45:
			return LogTypes.needs_maintenance;
		case 46:
			return LogTypes.owner_maintenance;
		case 18:
			return LogTypes.reviewer_note;
		case 22:
			return LogTypes.temporarily_disabled;
		default:
			return LogTypes.note;
		}
	}

	static CB_Core.Enums.Attributes getAttributeEnum(int id) {
		switch (id) {
		case 1:
			return CB_Core.Enums.Attributes.Dogs;
		case 2:
			return CB_Core.Enums.Attributes.Fee;
		case 3:
			return CB_Core.Enums.Attributes.ClimbingGear;
		case 4:
			return CB_Core.Enums.Attributes.Boat;
		case 5:
			return CB_Core.Enums.Attributes.Scuba;
		case 6:
			return CB_Core.Enums.Attributes.Kids;
		case 7:
			return CB_Core.Enums.Attributes.TakesLess;
		case 8:
			return CB_Core.Enums.Attributes.ScenicView;
		case 9:
			return CB_Core.Enums.Attributes.SignificantHike;
		case 10:
			return CB_Core.Enums.Attributes.Climbing;
		case 11:
			return CB_Core.Enums.Attributes.Wading;
		case 12:
			return CB_Core.Enums.Attributes.Swimming;
		case 13:
			return CB_Core.Enums.Attributes.Anytime;
		case 14:
			return CB_Core.Enums.Attributes.Night;
		case 15:
			return CB_Core.Enums.Attributes.Winter;
		case 17:
			return CB_Core.Enums.Attributes.PoisonPlants;
		case 18:
			return CB_Core.Enums.Attributes.Snakes;
		case 19:
			return CB_Core.Enums.Attributes.Ticks;
		case 20:
			return CB_Core.Enums.Attributes.AbandonedMines;
		case 21:
			return CB_Core.Enums.Attributes.Cliff;
		case 22:
			return CB_Core.Enums.Attributes.Hunting;
		case 23:
			return CB_Core.Enums.Attributes.Dangerous;
		case 24:
			return CB_Core.Enums.Attributes.WheelchairAccessible;
		case 25:
			return CB_Core.Enums.Attributes.Parking;
		case 26:
			return CB_Core.Enums.Attributes.PublicTransportation;
		case 27:
			return CB_Core.Enums.Attributes.Drinking;
		case 28:
			return CB_Core.Enums.Attributes.Restrooms;
		case 29:
			return CB_Core.Enums.Attributes.Telephone;
		case 30:
			return CB_Core.Enums.Attributes.Picnic;
		case 31:
			return CB_Core.Enums.Attributes.Camping;
		case 32:
			return CB_Core.Enums.Attributes.Bicycles;
		case 33:
			return CB_Core.Enums.Attributes.Motorcycles;
		case 34:
			return CB_Core.Enums.Attributes.Quads;
		case 35:
			return CB_Core.Enums.Attributes.Offroad;
		case 36:
			return CB_Core.Enums.Attributes.Snowmobiles;
		case 37:
			return CB_Core.Enums.Attributes.Horses;
		case 38:
			return CB_Core.Enums.Attributes.Campfires;
		case 40:
			return CB_Core.Enums.Attributes.Stealth;
		case 41:
			return CB_Core.Enums.Attributes.Stroller;
		case 42:
			return CB_Core.Enums.Attributes.NeedsMaintenance;
		case 43:
			return CB_Core.Enums.Attributes.Livestock;
		case 44:
			return CB_Core.Enums.Attributes.Flashlight;
		case 46:
			return CB_Core.Enums.Attributes.TruckDriver;
		case 47:
			return CB_Core.Enums.Attributes.FieldPuzzle;
		case 48:
			return CB_Core.Enums.Attributes.UVLight;
		case 49:
			return CB_Core.Enums.Attributes.Snowshoes;
		case 50:
			return CB_Core.Enums.Attributes.CrossCountrySkiis;
		case 51:
			return CB_Core.Enums.Attributes.SpecialTool;
		case 52:
			return CB_Core.Enums.Attributes.NightCache;
		case 53:
			return CB_Core.Enums.Attributes.ParkAndGrab;
		case 54:
			return CB_Core.Enums.Attributes.AbandonedStructure;
		case 55:
			return CB_Core.Enums.Attributes.ShortHike;
		case 56:
			return CB_Core.Enums.Attributes.MediumHike;
		case 57:
			return CB_Core.Enums.Attributes.LongHike;
		case 58:
			return CB_Core.Enums.Attributes.FuelNearby;
		case 59:
			return CB_Core.Enums.Attributes.FoodNearby;
		}

		return CB_Core.Enums.Attributes.Default;
	}

}
