package CB_Core.Api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import CB_Core.Config;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Log.Logger;
import CB_Core.Types.Trackable;

import org.apache.commons.lang.NullArgumentException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/***
 * @author Longri
 */
public class PocketQuery
{
	/***
	 * stellt ein PQ zum Download dar
	 * 
	 * @author Longri
	 */
	public static class PQ implements Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8308386638170255124L;
		public String Name;
		public String GUID;
		public int PQCount;
		public Date DateLastGenerated;
		public double SizeMB;
		public boolean downloadAvible = false;
	}

	/**
	 * Ruft die Liste der PQ´s ab.
	 * 
	 * @param accessToken
	 *            as String
	 * @param list
	 *            as ArrayList<String>
	 * @return
	 */
	public static int GetPocketQueryList(String accessToken, ArrayList<PQ> list)
	{
		HttpGet httpGet = new HttpGet(GroundspeakAPI.GS_LIVE_URL + "GetPocketQueryList?AccessToken=" + accessToken + "&format=json");
		if (list == null) new NullArgumentException("PQ List");
		try
		{
			String result = GroundspeakAPI.Execute(httpGet);

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					GroundspeakAPI.LastAPIError = "";
					JSONArray jPQs = json.getJSONArray("PocketQueryList");

					for (int ii = 0; ii < jPQs.length(); ii++)
					{

						JSONObject jPQ = (JSONObject) jPQs.get(ii);

						if (jPQ.getBoolean("IsDownloadAvailable"))
						{
							PQ pq = new PQ();
							pq.Name = jPQ.getString("Name");
							pq.GUID = jPQ.getString("GUID");
							pq.DateLastGenerated = new Date();
							try
							{
								String dateCreated = jPQ.getString("DateLastGenerated");
								int date1 = dateCreated.indexOf("/Date(");
								int date2 = dateCreated.indexOf("-");
								String date = (String) dateCreated.subSequence(date1 + 6, date2);
								pq.DateLastGenerated = new Date(Long.valueOf(date));
							}
							catch (Exception exc)
							{
								Logger.Error("API", "SearchForGeocaches_ParseDate", exc);
							}
							pq.PQCount = jPQ.getInt("PQCount");
							int Byte = jPQ.getInt("FileSizeInBytes");
							pq.SizeMB = Byte / 1048576.0;
							list.add(pq);
						}
					}
					return 0;
				}
				else
				{
					GroundspeakAPI.LastAPIError = "";
					GroundspeakAPI.LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
					GroundspeakAPI.LastAPIError += status.getString("StatusMessage") + "\n";
					GroundspeakAPI.LastAPIError += status.getString("ExceptionDetails");

					return (-1);
				}

			}
			catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch (ClientProtocolException e)
		{
			System.out.println(e.getMessage());
			return (-1);
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
			return (-1);
		}

		return 0;
	}

	public static int GetPocketQueryUri(String accessToken, String GUID, String Uri)
	{
		HttpGet httpGet = new HttpGet(GroundspeakAPI.GS_LIVE_URL + "GetPocketQueryUrls?AccessToken=" + accessToken + "&PocketQueryGuid="
				+ GUID + "&format=json");
		if (GUID == null || GUID.equals("")) new NullArgumentException("GUID");
		try
		{
			String result = GroundspeakAPI.Execute(httpGet);

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					GroundspeakAPI.LastAPIError = "";
					JSONObject jPQ = json.getJSONObject("PocketQueryUrls");

					Uri = jPQ.getString("Uri");

					return 0;
				}
				else
				{
					GroundspeakAPI.LastAPIError = "";
					GroundspeakAPI.LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
					GroundspeakAPI.LastAPIError += status.getString("StatusMessage") + "\n";
					GroundspeakAPI.LastAPIError += status.getString("ExceptionDetails");

					return (-1);
				}

			}
			catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch (ClientProtocolException e)
		{
			System.out.println(e.getMessage());
			return (-1);
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
			return (-1);
		}

		return 0;

	}

	public static int DownloadSinglePocketQuery(PQ pocketQuery)
	{
		return DownloadSinglePocketQuery(pocketQuery, Config.settings.PocketQueryFolder.getValue() + "\\");
	}

	public static int DownloadSinglePocketQuery(PQ pocketQuery, String savePath)
	{
		String accessToken = Config.GetAccessToken(); // ""
		HttpGet httpGet = new HttpGet(GroundspeakAPI.GS_LIVE_URL + "GetPocketQueryZippedFile?format=json&AccessToken=" + accessToken
				+ "&PocketQueryGuid=" + pocketQuery.GUID);

		try
		{
			String result = GroundspeakAPI.Execute(httpGet);

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					GroundspeakAPI.LastAPIError = "";
					String test = json.getString("ZippedFile");
					byte[] resultByte = CB_Core.Converter.Base64.decode(test);
					SimpleDateFormat postFormater = new SimpleDateFormat("yyyyMMddHHmmss");
					String dateString = postFormater.format(pocketQuery.DateLastGenerated);
					String local = savePath + pocketQuery.Name + "_" + dateString + ".zip";

					FileOutputStream fs;
					fs = new FileOutputStream(local);

					fs.write(resultByte);
					fs.close();

					return 0;
				}
				else
				{
					GroundspeakAPI.LastAPIError = "";
					GroundspeakAPI.LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
					GroundspeakAPI.LastAPIError += status.getString("StatusMessage") + "\n";
					GroundspeakAPI.LastAPIError += status.getString("ExceptionDetails");

					return (-1);
				}

			}
			catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch (ClientProtocolException e)
		{
			System.out.println(e.getMessage());
			return (-1);
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
			return (-1);
		}

		return 0;

	}

}
