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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
import org.slf4j.LoggerFactory;

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
import CB_Core.Settings.CB_Core_Settings;
import CB_Core.Types.Cache;
import CB_Core.Types.DLong;
import CB_Core.Types.ImageEntry;
import CB_Core.Types.LogEntry;
import CB_Core.Types.Waypoint;
import CB_Locator.CoordinateGPS;
import CB_Utils.DB.CoreCursor;
import CB_Utils.Interfaces.ICancel;
import CB_Utils.Lists.CB_List;
import CB_Utils.Util.FileIO;
import CB_Utils.http.HttpUtils;

public class SearchForGeocaches_Core
{
	final static org.slf4j.Logger logger = LoggerFactory.getLogger(PocketQuery.class);

	private Boolean LoadBooleanValueFromDB(String sql) // Found-Status aus Datenbank auslesen
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

	public String SearchForGeocachesJSON(Search search, CB_List<Cache> cacheList, ArrayList<LogEntry> logList, ArrayList<ImageEntry> imageList, long gpxFilenameId, ICancel icancel)
	{
		String result = "";
		int startIndex = 0;
		int searchNumber = search.number <= 50 ? search.number : 50;

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

		if (!isLite)
		{
			// Chek if search a lite request
			isLite = search.isLite;
		}

		String URL = CB_Core_Settings.StagingAPI.getValue() ? GroundspeakAPI.STAGING_GS_LIVE_URL : GroundspeakAPI.GS_LIVE_URL;

		HttpPost httppost = new HttpPost(URL + "SearchForGeocaches?format=json");

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
				request.put("StartIndex", startIndex);
				request.put("MaxPerPage", 1);
				request.put("GeocacheLogCount", 10);
				request.put("TrackableLogCount", 10);
				JSONObject requestcc = new JSONObject();
				JSONArray requesta = new JSONArray();

				for (String gcCode : searchGC.gcCodes)
				{
					requesta.put(gcCode);
				}

				requestcc.put("CacheCodes", requesta);
				request.put("CacheCode", requestcc);
			}
			catch (JSONException e)
			{
				logger.error("SearchForGeocaches:JSONException", e);
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
			requestString += "\"StartIndex\":" + startIndex + ",";
			requestString += "\"MaxPerPage\":" + String.valueOf(searchNumber) + ",";

			requestString += "\"GeocacheName\":{";
			requestString += "\"GeocacheName\":\"" + searchC.gcName + "\"},";

			requestString += "\"PointRadius\":{";
			requestString += "\"DistanceInMeters\":" + "5000000" + ",";
			requestString += "\"Point\":{";
			requestString += "\"Latitude\":" + String.valueOf(searchC.pos.getLatitude()) + ",";
			requestString += "\"Longitude\":" + String.valueOf(searchC.pos.getLongitude());
			requestString += "},";

			requestString = writeExclusions(requestString, searchC);

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
			requestString += "\"StartIndex\":" + startIndex + ",";
			requestString += "\"MaxPerPage\":" + String.valueOf(searchNumber) + ",";
			requestString += "\"GeocacheLogCount\":3,";
			requestString += "\"TrackableLogCount\":2,";

			requestString += "\"PointRadius\":{";
			requestString += "\"DistanceInMeters\":" + "5000000" + ",";
			requestString += "\"Point\":{";
			requestString += "\"Latitude\":" + String.valueOf(searchC.pos.getLatitude()) + ",";
			requestString += "\"Longitude\":" + String.valueOf(searchC.pos.getLongitude());
			requestString += "},";

			requestString = writeExclusions(requestString, searchC);

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
			requestString += "\"StartIndex\":" + startIndex + ",";
			requestString += "\"MaxPerPage\":" + String.valueOf(searchNumber) + ",";
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
				requestString += "\"UserNames\":[\"" + CB_Core_Settings.GcLogin.getValue() + "\"]";
				requestString += "},";
			}

			if (searchC.excludeFounds)
			{
				requestString += "\"NotFoundByUsers\":{";
				requestString += "\"UserNames\":[\"" + CB_Core_Settings.GcLogin.getValue() + "\"]";
				requestString += "},";
			}

			requestString = writeExclusions(requestString, searchC);

			requestString += "}";

		}

		try
		{
			httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));
		}
		catch (UnsupportedEncodingException e3)
		{
			logger.error("SearchForGeocaches:UnsupportedEncodingException", e3);
		}
		httppost.setHeader("Accept", "application/json");
		httppost.setHeader("Content-type", "application/json");

		// Execute HTTP Post Request
		try
		{
			result = HttpUtils.Execute(httppost, icancel);
		}
		catch (ConnectTimeoutException e)
		{
			logger.error("SearchForGeocaches:ConnectTimeoutException", e);
			showToastConnectionError();
			return "";
		}
		catch (ClientProtocolException e)
		{
			logger.error("SearchForGeocaches:ClientProtocolException", e);
			showToastConnectionError();
			return "";
		}
		catch (IOException e)
		{
			logger.error("SearchForGeocaches:IOException", e);
			showToastConnectionError();
			return "";
		}

		// chk api result
		if (GroundspeakAPI.getApiStatus(result) != 0) return "";
		if (result.contains("The service is unavailable")) return "The service is unavailable";
		if (result.contains("server error")) return "The service is unavailable";
		if (result.contains("Cache download limit has been exceeded")) return "download limit";
		if (result.length() == 0)
		{
			showToastConnectionError();
			return "The service is unavailable";
		}

		// save result, if this a Live-Request
		if (search instanceof SearchLiveMap)
		{
			SearchLiveMap se = (SearchLiveMap) search;

			Writer writer = null;
			try
			{
				String Path = se.descriptor.getLocalCachePath(LiveMapQue.LIVE_CACHE_NAME) + LiveMapQue.LIVE_CACHE_EXTENTION;

				if (FileIO.createDirectory(Path))
				{
					writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Path), "utf-8"));
					writer.write(result);
				}
			}
			catch (IOException ex)
			{
				// report
			}
			finally
			{
				try
				{
					writer.close();
				}
				catch (Exception ex)
				{
				}
			}
		}

		result = ParseJsonResult(search, cacheList, logList, imageList, gpxFilenameId, result, apiStatus, isLite);
		if (searchNumber > 1 && cacheList.size() == searchNumber)
		{
			startIndex++;
			int lastCacheListSize;
			do
			{
				lastCacheListSize = cacheList.size();
				startIndex += searchNumber;

				requestString = "{";
				requestString += "\"AccessToken\":\"" + GroundspeakAPI.GetAccessToken() + "\",";

				if (isLite) requestString += "\"IsLite\":true,"; // only lite
				else
					requestString += "\"IsLite\":false,"; // full for Premium
				requestString += "\"StartIndex\":" + startIndex + ",";
				requestString += "\"MaxPerPage\":" + String.valueOf(50) + ",";
				requestString += "\"GeocacheLogCount\":3,";
				requestString += "\"TrackableLogCount\":2";
				requestString += "}";

				httppost = new HttpPost(URL + "GetMoreGeocaches?format=json");

				try
				{
					httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));
				}
				catch (UnsupportedEncodingException e3)
				{
					logger.error("SearchForGeocaches:UnsupportedEncodingException", e3);
				}
				httppost.setHeader("Accept", "application/json");
				httppost.setHeader("Content-type", "application/json");

				// Execute HTTP Post Request
				try
				{
					result = HttpUtils.Execute(httppost, icancel);
					if (result.contains("The service is unavailable"))
					{
						logger.error("SearchForGeocaches:The service is unavailable: " + result);
						return "The service is unavailable";
					}
				}
				catch (ConnectTimeoutException e)
				{
					logger.error("SearchForGeocaches:ConnectTimeoutException", e);
					showToastConnectionError();

					return "";

				}
				catch (ClientProtocolException e)
				{
					logger.error("SearchForGeocaches:ClientProtocolException", e);
					showToastConnectionError();

					return "";
				}
				catch (IOException e)
				{
					logger.error("SearchForGeocaches:IOException", e);
					showToastConnectionError();

					return "";
				}

				if (result.length() == 0)
				{
					showToastConnectionError();
					return "The service is unavailable";
				}

				result = ParseJsonResult(search, cacheList, logList, imageList, gpxFilenameId, result, apiStatus, isLite);

			}
			while ((startIndex + searchNumber <= search.number) && (cacheList.size() - lastCacheListSize >= searchNumber) && (lastCacheListSize != cacheList.size() || startIndex + searchNumber > cacheList.size()));
		}
		return result;
	}

	String ParseJsonResult(Search search, CB_List<Cache> cacheList, ArrayList<LogEntry> logList, ArrayList<ImageEntry> imageList, long gpxFilenameId, String result, byte apiStatus, boolean isLite)
	{
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
				// log.debug("got " + caches.length() + " Caches from gc");
				for (int i = 0; i < caches.length(); i++)
				{
					JSONObject jCache = (JSONObject) caches.get(i);
					String gcCode = jCache.getString("Code");
					// log.debug("handling " + gcCode);
					String name = jCache.getString("Name");
					result += gcCode + " - " + name + "\n";

					Boolean CacheERROR = false;

					Cache cache = new Cache(true);
					cache.setArchived(jCache.getBoolean("Archived"));
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
					cache.setAvailable(jCache.getBoolean("Available"));
					cache.setDateHidden(new Date());
					try
					{
						String dateCreated = jCache.getString("DateCreated");
						int date1 = dateCreated.indexOf("/Date(");
						int date2 = dateCreated.lastIndexOf("-");
						String date = (String) dateCreated.subSequence(date1 + 6, date2);
						cache.setDateHidden(new Date(Long.valueOf(date)));
					}
					catch (Exception exc)
					{
						logger.error("SearchForGeocaches_ParseDate", exc);
					}
					cache.setDifficulty((float) jCache.getDouble("Difficulty"));

					// Ein evtl. in der Datenbank vorhandenen "Found" nicht �berschreiben
					Boolean Favorite = LoadBooleanValueFromDB("select Favorit from Caches where GcCode = \"" + gcCode + "\"");
					cache.setFavorit(Favorite);

					// Ein evtl. in der Datenbank vorhandenen "Found" nicht �berschreiben
					Boolean Found = LoadBooleanValueFromDB("select found from Caches where GcCode = \"" + gcCode + "\"");
					if (!Found)
					{
						cache.setFound(jCache.getBoolean("HasbeenFoundbyUser"));
					}
					else
					{
						cache.setFound(true);
					}

					cache.setGcCode(jCache.getString("Code"));
					try
					{
						cache.setGcId(jCache.getString("ID"));
					}
					catch (Exception e)
					{
						// CacheERROR = true; gibt bei jedem Cache ein
						// Fehler ???
					}
					cache.GPXFilename_ID = gpxFilenameId;

					// Ein evtl. in der Datenbank vorhandenen "Found" nicht �berschreiben
					Boolean userData = LoadBooleanValueFromDB("select HasUserData from Caches where GcCode = \"" + gcCode + "\"");

					cache.setHasUserData(userData);
					if (!isLite)
					{
						try
						{
							cache.setHint(jCache.getString("EncodedHints"));
						}
						catch (Exception e1)
						{
							cache.setHint("");
						}
					}

					cache.Id = Cache.GenerateCacheId(cache.getGcCode());
					cache.setListingChanged(false);
					if (!isLite)
					{
						try
						{
							cache.setLongDescription(jCache.getString("LongDescription"));
						}
						catch (Exception e1)
						{
							logger.error("SearchForGeocaches_LongDescription:" + cache.getGcCode(), e1);
							cache.setLongDescription("");
						}
						if (jCache.getBoolean("LongDescriptionIsHtml") == false)
						{
							cache.setLongDescription(cache.getLongDescription().replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
						}
					}

					cache.setName(jCache.getString("Name"));
					cache.setTourName("");
					cache.setNoteChecksum(0);
					cache.NumTravelbugs = jCache.getInt("TrackableCount");
					JSONObject jOwner = (JSONObject) jCache.getJSONObject("Owner");
					cache.setOwner(jOwner.getString("UserName"));
					cache.setPlacedBy(cache.getOwner());
					try
					{
						cache.Pos = new CoordinateGPS(jCache.getDouble("Latitude"), jCache.getDouble("Longitude"));
					}
					catch (Exception e)
					{

					}
					cache.Rating = 0;
					if (!isLite)
					{
						try
						{
							cache.setShortDescription(jCache.getString("ShortDescription"));
						}
						catch (Exception e)
						{
							logger.error("SearchForGeocaches_shortDescription:" + cache.getGcCode(), e);
							cache.setShortDescription("");
						}
						if (jCache.getBoolean("ShortDescriptionIsHtml") == false)
						{
							cache.setShortDescription(cache.getShortDescription().replaceAll("(\r\n|\n\r|\r|\n)", "<br />"));
						}
					}

					JSONObject jContainer = jCache.getJSONObject("ContainerType");
					int jSize = jContainer.getInt("ContainerTypeId");
					cache.Size = CacheSizes.parseInt(GroundspeakAPI.getCacheSize(jSize));
					cache.setSolverChecksum(0);
					cache.setTerrain((float) jCache.getDouble("Terrain"));
					cache.Type = CacheTypes.Traditional;
					try
					{
						JSONObject jCacheType = jCache.getJSONObject("CacheType");
						cache.Type = GroundspeakAPI.getCacheType(jCacheType.getInt("GeocacheTypeId"));
					}
					catch (Exception e)
					{

						if (gcCode.equals("GC4K089"))
						{
							cache.Type = CacheTypes.Giga;
						}
						else
						{
							cache.Type = CacheTypes.Undefined;
						}
					}
					cache.setUrl(jCache.getString("Url"));
					cache.setApiStatus(apiStatus);

					// Ein evtl. in der Datenbank vorhandenen "Favorit" nicht �berschreiben
					Boolean fav = LoadBooleanValueFromDB("select favorit from Caches where GcCode = \"" + gcCode + "\"");
					cache.setFavorit(fav);

					// Chk if Own or Found
					Boolean exclude = false;
					if (search.excludeFounds && cache.isFound()) exclude = true;
					if (search.excludeHides && cache.getOwner().equalsIgnoreCase(CB_Core_Settings.GcLogin.getValue())) exclude = true;
					if (search.available && (cache.isArchived() || !cache.isAvailable())) exclude = true;

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
								logger.error("API", "SearchForGeocaches_ParseLogDate", exc);
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
							image.GcCode = cache.getGcCode();
							image.Name = jImage.getString("Name");
							image.Description = jImage.getString("Description");
							image.ImageUrl = jImage.getString("Url").replace("img.geocaching.com/gc/cache", "img.geocaching.com/cache");
							// remove "/gc" to match the url used in the description

							image.IsCacheImage = true;

							imageList.add(image);
						}
						int imageListSizeGC = images.length();

						// insert images from Cache description
						LinkedList<String> allImages = null;
						if (!search.isLite) allImages = DescriptionImageGrabber.GetAllImages(cache);
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
								image.GcCode = cache.getGcCode();
								image.Name = url.substring(url.lastIndexOf("/") + 1);
								image.Description = "";
								image.ImageUrl = url;
								image.IsCacheImage = true;

								imageList.add(image);
							}

						}
						logger.debug("Merged imageList has " + imageList.size() + " Entrys (" + imageListSizeOrg + "/" + imageListSizeGC + "/" + imageListSizeGrabbed + ")");

						// insert Waypoints
						JSONArray waypoints = jCache.getJSONArray("AdditionalWaypoints");
						for (int j = 0; j < waypoints.length(); j++)
						{
							JSONObject jWaypoints = (JSONObject) waypoints.get(j);
							Waypoint waypoint = new Waypoint(true);
							waypoint.CacheId = cache.Id;

							try
							{
								waypoint.Pos = new CoordinateGPS(jWaypoints.getDouble("Latitude"), jWaypoints.getDouble("Longitude"));
							}
							catch (Exception ex)
							{
								// no Coordinates -> Lat/Lon = 0/0
								waypoint.Pos = new CoordinateGPS(0, 0);
							}

							waypoint.setTitle(jWaypoints.getString("Description"));
							waypoint.setDescription(jWaypoints.getString("Comment"));
							waypoint.Type = GroundspeakAPI.getCacheType(jWaypoints.getInt("WptTypeID"));
							waypoint.setGcCode(jWaypoints.getString("Code"));
							cache.waypoints.add(waypoint);
						}
						// User Waypoints - Corrected Coordinates of the Geocaching.com Website
						JSONArray userWaypoints = jCache.getJSONArray("UserWaypoints");
						for (int j = 0; j < userWaypoints.length(); j++)
						{
							JSONObject jUserWaypoint = (JSONObject) userWaypoints.get(j);
							if (!jUserWaypoint.getString("Description").equals("Coordinate Override"))
							{
								continue; // only corrected Coordinate
							}
							Waypoint waypoint = new Waypoint(true);
							waypoint.CacheId = cache.Id;
							try
							{
								waypoint.Pos = new CoordinateGPS(jUserWaypoint.getDouble("Latitude"), jUserWaypoint.getDouble("Longitude"));
							}
							catch (Exception ex)
							{
								// no Coordinates -> Lat/Lon = 0/0
								waypoint.Pos = new CoordinateGPS(0, 0);
							}
							waypoint.setTitle(jUserWaypoint.getString("Description"));
							waypoint.setDescription(jUserWaypoint.getString("Description"));
							waypoint.Type = CacheTypes.Final;
							waypoint.setGcCode("CO" + cache.getGcCode().substring(2, cache.getGcCode().length()));
							cache.waypoints.add(waypoint);
						}
						// Spoiler aktualisieren
						actualizeSpoilerOfActualCache(cache);
					}

					// Notes
					Object note = jCache.get("GeocacheNote");
					if ((note != null) && (note instanceof String))
					{
						String s = (String) note;
						System.out.println(s);
						cache.setTmpNote(s);
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
			logger.error("SearchForGeocaches:ParserException: " + result, e);
		}
		catch (ClassCastException e)
		{
			logger.error("SearchForGeocaches:ParserException: " + result, e);
		}
		return result;
	}

	protected void showToastConnectionError()
	{
		// hier im Core nichts machen da hier keine UI vorhanden ist
	}

	protected void actualizeSpoilerOfActualCache(Cache cache)
	{
		// hier im Core nichts machen da hier keine UI vorhanden ist
	}

	private String writeExclusions(String requestString, SearchCoordinate searchC)
	{
		if (searchC.available)
		{

			requestString += "\"GeocacheExclusions\":{";
			requestString += "\"Archived\":false,";
			requestString += "\"Available\":true";

			requestString += "},";
		}
		return requestString;
	}

	/**
	 * @param aktCache
	 * @param accessToken
	 *            Config.GetAccessToken();
	 * @return
	 */
	public Cache LoadApiDetails(Cache aktCache, ICancel icancel)
	{

		Cache newCache = null;
		try
		{
			SearchGC search = new SearchGC(aktCache.getGcCode());

			CB_List<Cache> apiCaches = new CB_List<Cache>();
			ArrayList<LogEntry> apiLogs = new ArrayList<LogEntry>();
			ArrayList<ImageEntry> apiImages = new ArrayList<ImageEntry>();
			SearchForGeocachesJSON(search, apiCaches, apiLogs, apiImages, aktCache.GPXFilename_ID, icancel);
			synchronized (Database.Data.Query)
			{
				if (apiCaches.size() == 1)
				{
					Database.Data.beginTransaction();
					newCache = apiCaches.get(0);
					Database.Data.Query.remove(aktCache);
					Database.Data.Query.add(newCache);
					// newCache.MapX = 256.0 * Descriptor.LongitudeToTileX(Cache.MapZoomLevel, newCache.Longitude());
					// newCache.MapY = 256.0 * Descriptor.LatitudeToTileY(Cache.MapZoomLevel, newCache.Latitude());

					new CacheDAO().UpdateDatabase(newCache);

					// Delete LongDescription from this Cache! LongDescription is Loading by showing DescriptionView direct from DB
					newCache.setLongDescription("");

					LogDAO logDAO = new LogDAO();
					for (LogEntry log : apiLogs)
					{
						if (log.CacheId != newCache.Id) continue;
						// Write Log to database

						logDAO.WriteToDatabase(log);
					}

					WaypointDAO waypointDAO = new WaypointDAO();
					for (int i = 0, n = newCache.waypoints.size(); i < n; i++)
					{
						Waypoint waypoint = (Waypoint) newCache.waypoints.get(i);

						boolean update = true;

						// dont refresh wp if aktCache.wp is user changed
						for (int j = 0, m = aktCache.waypoints.size(); j < m; j++)
						{
							Waypoint wp = (Waypoint) aktCache.waypoints.get(j);
							if (wp.getGcCode().equalsIgnoreCase(waypoint.getGcCode()))
							{
								if (wp.IsUserWaypoint) update = false;
								break;
							}
						}

						if (update) waypointDAO.WriteToDatabase(waypoint, false);
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
			logger.error("Load CacheInfo by API", ex);
			return null;
		}

		return newCache;
	}

}
