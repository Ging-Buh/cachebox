package CB_UI.Api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.CacheDAO;
import CB_Core.DAO.ImageDAO;
import CB_Core.DAO.LogDAO;
import CB_Core.DAO.WaypointDAO;
import CB_Core.DB.Database;
import CB_Core.Enums.Attributes;
import CB_Core.Enums.CacheSizes;
import CB_Core.Enums.CacheTypes;
import CB_Core.Enums.LogTypes;
import CB_Core.Import.DescriptionImageGrabber;
import CB_Core.Map.Descriptor;
import CB_Core.Types.Cache;
import CB_Core.Types.DLong;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Controls.Dialogs.CancelWaitDialog.IReadyListner;
import CB_UI.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI.GL_UI.GL_Listener.GL;
import CB_Utils.DB.CoreCursor;
import CB_Utils.Log.Logger;

public class SearchForGeocaches
{
	private static Boolean LoadBooleanValueFromDB(String sql) // Found-Status aus Datenbank auslesen
	{
		CoreCursor reader = Database.Data.rawQuery(sql, null);
		try
		{
			reader.moveToFirst();
			while (!reader.isAfterLast())
			{
				if (reader.getInt(0) != 0)
				{ // gefunden. Suche abbrechen
					return true;
				}
				reader.moveToNext();
			}
		}
		finally
		{
			reader.close();
		}

		return false;
	}

	public static class Search
	{
		public int number;
		public boolean withoutFinds = false;
		public boolean withoutOwn = false;
		boolean excludeHides = false;
		boolean excludeFounds = false;
		boolean available = true;
	}

	public static class SearchCoordinate extends Search
	{
		public Coordinate pos;
		public float distanceInMeters;
		public int distance;
	}

	public static class SearchGC extends Search
	{
		public String gcCode;
	}

	public static class SearchGCName extends SearchCoordinate
	{
		public String gcName;
	}

	public static class SearchGCOwner extends SearchCoordinate
	{
		public String OwnerName;
	}

	public static String SearchForGeocachesJSON(Search search, ArrayList<Cache> cacheList, ArrayList<LogEntry> logList,
			ArrayList<ImageEntry> imageList, long gpxFilenameId)
	{
		String result = "";

		byte apiStatus = 0;
		boolean isLite = true;
		if (GroundspeakAPI.IsPremiumMember())
		{
			isLite = false;
			apiStatus = 2;
		}
		else
		{
			isLite = true;
			apiStatus = 1;
		}

		HttpPost httppost = new HttpPost("https://api.groundspeak.com/LiveV5/Geocaching.svc/SearchForGeocaches?format=json");

		String requestString = "";

		if (search instanceof SearchGC)
		{
			isLite = false;
			SearchGC searchGC = (SearchGC) search;

			JSONObject request = new JSONObject();
			try
			{
				request.put("AccessToken", GroundspeakAPI.GetAccessToken());
				request.put("IsLight", false);
				request.put("StartIndex", 0);
				request.put("MaxPerPage", 1);
				request.put("GeocacheLogCount", 10);
				request.put("TrackableLogCount", 10);
				JSONObject requestcc = new JSONObject();
				JSONArray requesta = new JSONArray();
				requesta.put(searchGC.gcCode);
				requestcc.put("CacheCodes", requesta);
				request.put("CacheCode", requestcc);
			}
			catch (JSONException e)
			{
				Logger.Error("SearchForGeocaches:JSONException", e.getMessage());
			}
			// ein einzelner Cache wird voll geladen
			apiStatus = 2;

			requestString = request.toString();
		}
		else if (search instanceof SearchGCName)
		{
			SearchGCName searchC = (SearchGCName) search;
			requestString = "{";
			requestString += "\"AccessToken\":\"" + GroundspeakAPI.GetAccessToken() + "\",";
			if (isLite) requestString += "\"IsLite\":true,"; // only lite
			else
				requestString += "\"IsLite\":false,"; // full for Premium
			requestString += "\"StartIndex\":0,";
			requestString += "\"MaxPerPage\":" + String.valueOf(searchC.number) + ",";

			requestString += "\"GeocacheName\":{";
			requestString += "\"GeocacheName\":\"" + searchC.gcName + "\"},";

			requestString += "\"PointRadius\":{";
			requestString += "\"DistanceInMeters\":" + "5000000" + ",";
			requestString += "\"Point\":{";
			requestString += "\"Latitude\":" + String.valueOf(searchC.pos.getLatitude()) + ",";
			requestString += "\"Longitude\":" + String.valueOf(searchC.pos.getLongitude());
			requestString += "}";
			requestString += "},";

			requestString += "}";

		}
		else if (search instanceof SearchGCOwner)
		{
			SearchGCOwner searchC = (SearchGCOwner) search;
			requestString = "{";
			requestString += "\"AccessToken\":\"" + GroundspeakAPI.GetAccessToken() + "\",";

			requestString += "\"HiddenByUsers\":{";
			requestString += "\"UserNames\":[\"" + searchC.OwnerName + "\"]},";

			if (isLite) requestString += "\"IsLite\":true,"; // only lite
			else
				requestString += "\"IsLite\":false,"; // full for Premium
			requestString += "\"StartIndex\":0,";
			requestString += "\"MaxPerPage\":" + String.valueOf(searchC.number) + ",";
			requestString += "\"GeocacheLogCount\":3,";
			requestString += "\"TrackableLogCount\":2,";

			requestString += "\"PointRadius\":{";
			requestString += "\"DistanceInMeters\":" + "5000000" + ",";
			requestString += "\"Point\":{";
			requestString += "\"Latitude\":" + String.valueOf(searchC.pos.getLatitude()) + ",";
			requestString += "\"Longitude\":" + String.valueOf(searchC.pos.getLongitude());
			requestString += "}";
			requestString += "},";
			//

			requestString += "}";

		}
		else if (search instanceof SearchCoordinate)
		{
			SearchCoordinate searchC = (SearchCoordinate) search;
			requestString = "{";
			requestString += "\"AccessToken\":\"" + GroundspeakAPI.GetAccessToken() + "\",";
			if (isLite) requestString += "\"IsLite\":true,"; // only lite
			else
				requestString += "\"IsLite\":false,"; // full for Premium
			requestString += "\"StartIndex\":0,";
			requestString += "\"MaxPerPage\":" + String.valueOf(searchC.number) + ",";
			requestString += "\"PointRadius\":{";
			requestString += "\"DistanceInMeters\":" + String.valueOf(searchC.distanceInMeters) + ",";
			requestString += "\"Point\":{";
			requestString += "\"Latitude\":" + String.valueOf(searchC.pos.getLatitude()) + ",";
			requestString += "\"Longitude\":" + String.valueOf(searchC.pos.getLongitude());
			requestString += "}";
			requestString += "},";
			requestString += "\"GeocacheExclusions\":{";
			requestString += "\"Archived\":false,";
			requestString += "\"Available\":true";
			requestString += "}";
			requestString += "}";

			requestString = "{";
			requestString += "\"AccessToken\":\"" + GroundspeakAPI.GetAccessToken() + "\",";
			if (isLite) requestString += "\"IsLite\":true,"; // only lite
			else
				requestString += "\"IsLite\":false,"; // full for Premium
			requestString += "\"StartIndex\":0,";
			requestString += "\"MaxPerPage\":" + String.valueOf(searchC.number) + ",";
			requestString += "\"PointRadius\":{";
			requestString += "\"DistanceInMeters\":" + String.valueOf((int) searchC.distanceInMeters) + ",";
			requestString += "\"Point\":{";
			requestString += "\"Latitude\":" + String.valueOf(searchC.pos.getLatitude()) + ",";
			requestString += "\"Longitude\":" + String.valueOf(searchC.pos.getLongitude());
			requestString += "}";
			requestString += "},";

			if (searchC.excludeHides)
			{
				requestString += "\"NotHiddenByUsers\":{";
				requestString += "\"UserNames\":[\"" + Config.settings.GcLogin.getValue() + "\"]";
				requestString += "},";
			}

			if (searchC.excludeFounds)
			{
				requestString += "\"NotFoundByUsers\":{";
				requestString += "\"UserNames\":[\"" + Config.settings.GcLogin.getValue() + "\"]";
				requestString += "},";
			}

			requestString += "\"GeocacheExclusions\":{";
			requestString += "\"Archived\":false,";

			if (searchC.available) requestString += "\"Available\":true";

			requestString += "}";
			requestString += "}";

		}

		try
		{
			httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));
		}
		catch (UnsupportedEncodingException e3)
		{
			Logger.Error("SearchForGeocaches:UnsupportedEncodingException", e3.getMessage());
		}
		httppost.setHeader("Accept", "application/json");
		httppost.setHeader("Content-type", "application/json");

		// Execute HTTP Post Request
		try
		{
			result = GroundspeakAPI.Execute(httppost);
			if (result.contains("The service is unavailable"))
			{
				return "The service is unavailable";
			}
		}
		catch (ConnectTimeoutException e)
		{
			Logger.Error("SearchForGeocaches:ConnectTimeoutException", e.getMessage());
			GL.that.Toast(ConnectionError.INSTANCE);

			return "";

		}
		catch (ClientProtocolException e)
		{
			Logger.Error("SearchForGeocaches:ClientProtocolException", e.getMessage());
		}
		catch (IOException e)
		{
			Logger.Error("SearchForGeocaches:IOException", e.getMessage());
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
				Logger.DEBUG("got " + caches.length() + " Caches from gc");
				for (int i = 0; i < caches.length(); i++)
				{
					JSONObject jCache = (JSONObject) caches.get(i);
					String gcCode = jCache.getString("Code");
					Logger.DEBUG("handling " + gcCode);
					String name = jCache.getString("Name");
					result += gcCode + " - " + name + "\n";

					Boolean CacheERROR = false;

					Cache cache = new Cache();
					cache.Archived = jCache.getBoolean("Archived");
					cache.setAttributesPositive(new DLong(0, 0));
					cache.setAttributesNegative(new DLong(0, 0));
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
						}
						else
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
					}
					catch (Exception exc)
					{
						Logger.Error("API", "SearchForGeocaches_ParseDate", exc);
					}
					cache.Difficulty = (float) jCache.getDouble("Difficulty");
					cache.setFavorit(false);

					// Ein evtl. in der Datenbank vorhandenen "Found" nicht überschreiben
					Boolean Found = LoadBooleanValueFromDB("select found from Caches where GcCode = \"" + gcCode + "\"");
					if (!Found)
					{
						cache.Found = jCache.getBoolean("HasbeenFoundbyUser");
					}
					else
					{
						cache.Found = true;
					}

					cache.GcCode = jCache.getString("Code");
					try
					{
						cache.GcId = jCache.getString("ID");
					}
					catch (Exception e)
					{
						// CacheERROR = true; gibt bei jedem Cache ein
						// Fehler ???
					}
					cache.GPXFilename_ID = gpxFilenameId;
					cache.hasUserData = false;
					try
					{
						cache.hint = jCache.getString("EncodedHints");
					}
					catch (Exception e1)
					{
						cache.hint = "";
					}
					cache.Id = Cache.GenerateCacheId(cache.GcCode);
					cache.listingChanged = false;

					try
					{
						cache.longDescription = jCache.getString("LongDescription");
					}
					catch (Exception e1)
					{
						Logger.Error("API", "SearchForGeocaches_LongDescription:" + cache.GcCode, e1);
						cache.longDescription = "";
					}
					if (jCache.getBoolean("LongDescriptionIsHtml") == false)
					{
						cache.longDescription = cache.longDescription.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
					}
					cache.Name = jCache.getString("Name");
					cache.noteCheckSum = 0;
					cache.NumTravelbugs = jCache.getInt("TrackableCount");
					JSONObject jOwner = (JSONObject) jCache.getJSONObject("Owner");
					cache.Owner = jOwner.getString("UserName");
					cache.PlacedBy = cache.Owner;
					try
					{
						cache.Pos = new Coordinate(jCache.getDouble("Latitude"), jCache.getDouble("Longitude"));
					}
					catch (Exception e)
					{

					}
					cache.Rating = 0;
					// cache.Rating =
					try
					{
						cache.shortDescription = jCache.getString("ShortDescription");
					}
					catch (Exception e)
					{
						Logger.Error("API", "SearchForGeocaches_shortDescription:" + cache.GcCode, e);
						cache.shortDescription = "";
					}
					if (jCache.getBoolean("ShortDescriptionIsHtml") == false)
					{
						cache.shortDescription = cache.shortDescription.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
					}
					JSONObject jContainer = jCache.getJSONObject("ContainerType");
					int jSize = jContainer.getInt("ContainerTypeId");
					cache.Size = CacheSizes.parseInt(GroundspeakAPI.getCacheSize(jSize));
					cache.solverCheckSum = 0;
					cache.Terrain = (float) jCache.getDouble("Terrain");
					cache.Type = CacheTypes.Traditional;
					JSONObject jCacheType = jCache.getJSONObject("CacheType");
					cache.Type = GroundspeakAPI.getCacheType(jCacheType.getInt("GeocacheTypeId"));
					cache.Url = jCache.getString("Url");
					cache.ApiStatus = apiStatus;

					// Ein evtl. in der Datenbank vorhandenen "Favorit" nicht überschreiben
					Boolean fav = LoadBooleanValueFromDB("select favorit from Caches where GcCode = \"" + gcCode + "\"");
					cache.setFavorit(fav);

					// Chk if Own or Found
					Boolean exclude = false;
					if (search.withoutFinds && cache.Found) exclude = true;
					if (search.withoutOwn && cache.Owner.equalsIgnoreCase(Config.settings.GcLogin.getValue())) exclude = true;

					if (!CacheERROR && !exclude)
					{
						cacheList.add(cache);
						// insert Logs
						JSONArray logs = jCache.getJSONArray("GeocacheLogs");
						for (int j = 0; j < logs.length(); j++)
						{
							JSONObject jLogs = (JSONObject) logs.get(j);
							JSONObject jFinder = (JSONObject) jLogs.get("Finder");
							JSONObject jLogType = (JSONObject) jLogs.get("LogType");
							LogEntry log = new LogEntry();
							log.CacheId = cache.Id;
							log.Comment = jLogs.getString("LogText");
							log.Finder = jFinder.getString("UserName");
							log.Id = jLogs.getInt("ID");
							log.Timestamp = new Date();
							try
							{
								String dateCreated = jLogs.getString("VisitDate");
								int date1 = dateCreated.indexOf("/Date(");
								int date2 = dateCreated.indexOf("-");
								String date = (String) dateCreated.subSequence(date1 + 6, date2);
								log.Timestamp = new Date(Long.valueOf(date));
							}
							catch (Exception exc)
							{
								Logger.Error("API", "SearchForGeocaches_ParseLogDate", exc);
							}
							log.Type = LogTypes.GC2CB_LogType(jLogType.getInt("WptLogTypeId"));
							logList.add(log);
						}

						// insert Images
						int imageListSizeOrg = imageList.size();
						JSONArray images = jCache.getJSONArray("Images");
						for (int j = 0; j < images.length(); j++)
						{
							JSONObject jImage = (JSONObject) images.get(j);

							ImageEntry image = new ImageEntry();
							image.CacheId = cache.Id;
							image.GcCode = cache.GcCode;
							image.Name = jImage.getString("Name");
							image.Description = jImage.getString("Description");
							image.ImageUrl = jImage.getString("Url").replace("img.geocaching.com/gc/cache", "img.geocaching.com/cache");
							// remove "/gc" to match the url used in the description

							image.IsCacheImage = true;

							imageList.add(image);
						}
						int imageListSizeGC = images.length();

						// insert images from Cache description
						LinkedList<String> allImages = DescriptionImageGrabber.GetAllImages(cache);
						int imageListSizeGrabbed = 0;

						if (allImages != null && allImages.size() > 0)
						{
							imageListSizeGrabbed = allImages.size();
						}

						while (allImages != null && allImages.size() > 0)
						{
							String url;
							url = allImages.poll();

							boolean found = false;
							for (ImageEntry im : imageList)
							{
								if (im.ImageUrl.equalsIgnoreCase(url))
								{
									found = true;
									break;
								}
							}
							if (!found)
							{
								ImageEntry image = new ImageEntry();

								image.CacheId = cache.Id;
								image.GcCode = cache.GcCode;
								image.Name = url.substring(url.lastIndexOf("/") + 1);
								image.Description = "";
								image.ImageUrl = url;
								image.IsCacheImage = true;

								imageList.add(image);
							}

						}
						Logger.DEBUG("Merged imageList has " + imageList.size() + " Entrys (" + imageListSizeOrg + "/" + imageListSizeGC
								+ "/" + imageListSizeGrabbed + ")");

						// insert Waypoints
						JSONArray waypoints = jCache.getJSONArray("AdditionalWaypoints");
						for (int j = 0; j < waypoints.length(); j++)
						{
							JSONObject jWaypoints = (JSONObject) waypoints.get(j);
							Waypoint waypoint = new Waypoint();
							waypoint.CacheId = cache.Id;

							try
							{
								waypoint.Pos = new Coordinate(jWaypoints.getDouble("Latitude"), jWaypoints.getDouble("Longitude"));
							}
							catch (Exception ex)
							{
								// no Coordinates -> Lat/Lon = 0/0
								waypoint.Pos = new Coordinate();
							}

							waypoint.Title = jWaypoints.getString("Description");
							waypoint.Description = jWaypoints.getString("Comment");
							waypoint.Type = GroundspeakAPI.getCacheType(jWaypoints.getInt("WptTypeID"));
							waypoint.GcCode = jWaypoints.getString("Code");
							cache.waypoints.add(waypoint);
						}

						// Spoiler aktualisieren
						if (GlobalCore.getSelectedCache() != null)
						{
							if (GlobalCore.getSelectedCache().GcCode.equals(cache.GcCode))
							{
								GlobalCore.ImportSpoiler().setReadyListner(new IReadyListner()
								{
									@Override
									public void isReady()
									{
										{
											GlobalCore.getSelectedCache().ReloadSpoilerRessources();
										}
									}
								});
							}
						}
					}

				}
				GroundspeakAPI.checkCacheStatus(json, isLite);
			}
			else
			{
				result = "StatusCode = " + status.getInt("StatusCode") + "\n";
				result += status.getString("StatusMessage") + "\n";
				result += status.getString("ExceptionDetails");
			}

		}
		catch (JSONException e)
		{

			e.printStackTrace();
		}

		return result;
	}

	/**
	 * @param aktCache
	 * @param accessToken
	 *            Config.GetAccessToken();
	 * @return
	 */
	public static Cache LoadApiDetails(Cache aktCache)
	{

		Cache newCache = null;
		try
		{
			SearchForGeocaches.SearchGC search = new SearchForGeocaches.SearchGC();
			search.gcCode = aktCache.GcCode;

			ArrayList<Cache> apiCaches = new ArrayList<Cache>();
			ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
			ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
			CB_UI.Api.SearchForGeocaches.SearchForGeocachesJSON(search, apiCaches, apiLogs, apiImages, aktCache.GPXFilename_ID);
			synchronized (Database.Data.Query)
			{
				if (apiCaches.size() == 1)
				{
					Database.Data.beginTransaction();
					newCache = apiCaches.get(0);
					Database.Data.Query.remove(aktCache);
					Database.Data.Query.add(newCache);
					newCache.MapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, newCache.Longitude());
					newCache.MapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, newCache.Latitude());

					new CacheDAO().UpdateDatabase(newCache);

					LogDAO logDAO = new LogDAO();
					for (LogEntry log : apiLogs)
					{
						if (log.CacheId != newCache.Id) continue;
						// Write Log to database

						logDAO.WriteToDatabase(log);
					}

					WaypointDAO waypointDAO = new WaypointDAO();
					for (Waypoint waypoint : newCache.waypoints)
					{

						boolean update = true;

						// dont refresh wp if aktCache.wp is user changed
						for (Waypoint wp : aktCache.waypoints)
						{
							if (wp.GcCode.equalsIgnoreCase(waypoint.GcCode))
							{
								if (wp.IsUserWaypoint) update = false;
								break;
							}
						}

						if (update) waypointDAO.WriteToDatabase(waypoint);
					}

					ImageDAO imageDAO = new ImageDAO();
					for (ImageEntry image : apiImages)
					{
						if (image.CacheId != newCache.Id) continue;
						// Write Image to database

						imageDAO.WriteToDatabase(image, false);
					}

					Database.Data.setTransactionSuccessful();
					Database.Data.endTransaction();

					Database.Data.GPXFilenameUpdateCacheCount();
				}
			}
		}
		catch (Exception ex)
		{
			Logger.Error("DescriptionView", "Load CacheInfo by API", ex);
			return null;
		}

		return newCache;
	}

}
