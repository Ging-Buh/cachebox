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

import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Log.Logger;
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
			HttpPost httppost = new HttpPost("https://staging.api.groundspeak.com/Live/V1Beta/geocaching.svc/GetYourUserProfile?format=json");
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
			
			try // Parse JSON Result
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
					
				} else
				{
					result = "StatusCode = " + status.getInt("StatusCode") + "\n";
					result += status.getString("StatusMessage") + "\n";
					result += status.getString("ExceptionDetails");
					
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
			requestString += "\"IsLite\":false,";
			requestString += "\"StartIndex\":0,";
			requestString += "\"MaxPerPage\":" + String.valueOf(number) + ",";
			requestString += "\"PointRadius\":{";
			requestString += "\"DistanceInMeters\":" + String.valueOf(distanceInMeters) + ",";
			requestString += "\"Point\":{";
			requestString += "\"Latitude\":" + String.valueOf(pos.Latitude) + ",";
			requestString += "\"Longitude\":" + String.valueOf(pos.Longitude);
			requestString += "}";
			requestString += "},";
			requestString += "\"GeocacheExclusions\":{";
			requestString += "\"Archived\":false,";
			requestString += "\"Available\":true";
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
						cache.attributesNegative = 0;
						JSONArray jAttributes = jCache.getJSONArray("Attributes");
						for (int j = 0; j < jAttributes.length(); j++)
						{
							JSONObject jAttribute = jAttributes.getJSONObject(j);
							int AttributeTypeId = jAttribute.getInt("AttributeTypeID");
							Boolean isOn = jAttribute.getBoolean("IsOn");
							Attributes att = Attributes.getAttributeEnumByGcComId(AttributeTypeId);
							if (isOn)
							{
								cache.addAttributePositive(att);
							} else
							{
								cache.addAttributeNegative(att);								
							}
						}
						cache.Available = jCache.getBoolean("Available");
						cache.DateHidden = new Date();
						try
						{
							String dateCreated = jCache.getString("DateCreated");
							int date1 = dateCreated.indexOf("/Date(");
							int date2 = dateCreated.indexOf("-");
							String date = (String) dateCreated.subSequence(date1 + 6, date2);
							cache.DateHidden = new Date(Long.valueOf(date));
						} catch (Exception exc)
						{
							Logger.Error("API", "SearchForGeocaches_ParseDate", exc);							
						}
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
						cache.longDescription = jCache.getString("LongDescription");
						cache.Name = jCache.getString("Name");
						cache.noteCheckSum = 0;
						cache.NumTravelbugs = jCache.getInt("TrackableCount");
						JSONObject jOwner = (JSONObject) jCache.getJSONObject("Owner");						
						cache.Owner = jOwner.getString("UserName");
						cache.PlacedBy = cache.Owner;
						cache.Pos = new Coordinate();
						cache.Pos = new Coordinate(pos.Latitude = jCache.getDouble("Latitude"), jCache.getDouble("Longitude"));
						cache.Rating = 0;
//						cache.Rating =
						cache.shortDescription = jCache.getString("ShortDescription");
						JSONObject jContainer = jCache.getJSONObject("ContainerType");
						int jSize = jContainer.getInt("ContainerTypeId");
						cache.Size = CacheSizes.parseInt( getCacheSize(jSize) );
						cache.solverCheckSum = 0;
						cache.Terrain = (float)jCache.getDouble("Terrain");
						cache.Type = CacheTypes.Traditional;
						JSONObject jCacheType = jCache.getJSONObject("CacheType");
						cache.Type = getCacheType(jCacheType.getInt("GeocacheTypeId"));
						cache.Url = jCache.getString("Url");
						
						cacheList.add(cache);
					}
				} else
				{
					result = "StatusCode = " + status.getInt("StatusCode") + "\n";
					result += status.getString("StatusMessage") + "\n";
					result += status.getString("ExceptionDetails");
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
	
	private static int getCacheSize(int containerTypeId) 
	{
		switch (containerTypeId)
		{
		case 1:
			return 0;	// Unknown			
		case 2:
			return 1;	// Micro
		case 3:
			return 3;	// Regular
		case 4:
			return 4;   // Large
		case 5:
			return 5;	// Virtual
		case 6:
			return 0;	// Other
		case 8:
			return 2;
		default: 
			return 0;
			
		}
	}
	
	private static CacheTypes getCacheType(int apiTyp)
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
			return CacheTypes.Cache;   // Project APE Cache???
		case 11:
			return CacheTypes.Camera;
		case 12:
			return CacheTypes.Cache;  // Locationless (Reverse) Cache
		case 13:
			return CacheTypes.Cache;  // Cache In Trash Out Event
		case 137:
			return CacheTypes.Earth;
		case 453:
			return CacheTypes.MegaEvent;
		case 1304: 
			return CacheTypes.Cache;  // GPS Adventures Exhibit
		case 1858:
			return CacheTypes.Wherigo;
		default:
			return CacheTypes.Cache;
			
		}
	}
	
    private static CB_Core.Enums.Attributes getAttributeEnum(int id)
    {
        switch (id)
        {
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
