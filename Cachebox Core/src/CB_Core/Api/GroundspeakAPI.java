package CB_Core.Api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import CB_Core.Enums.CacheTypes;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;

public class GroundspeakAPI {
	
	/**
	 * Load Number of founds form geocaching.com
	 */
	public static int GetCachesFound(String accessToken)
	{ 
		String result = "";

		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("https://staging.api.groundspeak.com/Live/V1Beta/geocaching.svc/GetYourUserProfile");
			String requestString = "";
			requestString = "{";
			requestString += "\"AccessToken\":\"" + accessToken + "\",";
			requestString += "\"ProfileOptions\":{";
			requestString += "}";
			requestString += "}";
			
			httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));		    			 
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
			try 
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					result = "";
					JSONArray caches = json.getJSONArray("Geocaches");
					JSONObject profile = json.getJSONObject("Profile");
					JSONObject user = (JSONObject) profile.getJSONObject("User");						
					return user.getInt("FindCount");
					
				} else
				{
					result = "StatusCode = " + status.getInt("StatusCode");
					return (-1);
				}
			
			
			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (Exception ex)
		{
			System.out.println(ex.getMessage());
			return (-1);
		}
		
		return (-1);
	}

	public static String SearchForGeocachesJSON(String accessToken, Coordinate pos, float distanceInMeters, int number, ArrayList<Cache> cacheList)
	{ 
		String result = "";

		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("https://staging.api.groundspeak.com/Live/V1Beta/geocaching.svc/SearchForGeocachesJSON?format=json");
			String requestString = "";
			requestString = "{";
			requestString += "\"AccessToken\":\"" + accessToken + "\",";
			requestString += "\"IsLite\":true,";
			requestString += "\"StartIndex\":0,";
			requestString += "\"MaxPerPage\":" + String.valueOf(number) + ",";
			requestString += "\"PointRadius\":{";
			requestString += "\"DistanceInMeters\":" + String.valueOf(distanceInMeters) + ",";
			requestString += "\"Point\":{";
			requestString += "\"Latitude\":" + String.valueOf(pos.Latitude) + ",";
			requestString += "\"Longitude\":" + String.valueOf(pos.Longitude);
			requestString += "}";
			requestString += "}";
			requestString += "}";
			
			httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));		    			 
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
			try 
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					result = "";
					JSONArray caches = json.getJSONArray("Geocaches");
					
					for (int i = 0; i < caches.length(); i++)
					{
						JSONObject jCache = (JSONObject) caches.get(i);
						String gcCode = jCache.getString("Code");
						String name = jCache.getString("Name");
						result += gcCode + " - " + name + "\n";
						
						
						Cache cache = new Cache(); 
						cache.Archived = jCache.getBoolean("Archived");
						cache.attributesPositive = 0;
//						cache.attributesNegative =
						cache.attributesPositive = 0;
//						cache.attributesPositive =
						cache.Available = jCache.getBoolean("Available");
						cache.DateHidden = new Date();
//						cache.DateHidden =
						cache.Difficulty = (float)jCache.getDouble("Difficulty");
						cache.Favorit = false;
						cache.Found = jCache.getBoolean("HasbeenFoundbyUser");
						cache.GcCode = jCache.getString("Code");
						cache.GcId = jCache.getString("ID");
						cache.GPXFilename_ID = -1;
						cache.hasUserData = false;
						cache.hint = jCache.getString("EncodedHints");
						cache.Id = Cache.GenerateCacheId(cache.GcCode);
						cache.listingChanged = false;
						cache.longDescription = "";
						cache.Name = jCache.getString("Name");
						cache.noteCheckSum = 0;
						cache.NumTravelbugs = 0;
//						cache.NumTravelbugs = 0;
						JSONObject jOwner = (JSONObject) jCache.getJSONObject("Owner");						
						cache.Owner = jOwner.getString("UserName");
						cache.PlacedBy = cache.Owner;
						cache.Pos = new Coordinate();
						cache.Pos = new Coordinate(pos.Latitude = jCache.getDouble("Latitude"), jCache.getDouble("Longitude"));
						cache.Rating = 2;
//						cache.Rating =
						cache.shortDescription = jCache.getString("ShortDescription");
						cache.Size = 2;
//						cache.Size =
						cache.solverCheckSum = 0;
						cache.Terrain = (float)jCache.getDouble("Terrain");
						cache.Type = CacheTypes.Traditional;
//						cache.Type = CacheTypes.
						cache.Url = jCache.getString("Url");
						
						cacheList.add(cache);
					}
				} else
				{
					result = "StatusCode = " + status.getInt("StatusCode");
				}
			
			
			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (Exception ex)
		{
			System.out.println(ex.getMessage());
			return "API Error: " + ex.getMessage();
		}
		
		return result;
	}
}
