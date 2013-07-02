package CB_Core.Api;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.NullArgumentException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import CB_Core.CoreSettingsForward;
import CB_Core.Log.Logger;

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
	 * @param Staging
	 *            Config.settings.StagingAPI.getValue()
	 * @param accessToken
	 *            as String
	 * @param list
	 *            as ArrayList<String>
	 * @param conectionTimeout
	 *            Config.settings.conection_timeout.getValue()
	 * @param socketTimeout
	 *            Config.settings.socket_timeout.getValue()
	 * @return
	 */
	public static int GetPocketQueryList(ArrayList<PQ> list)
	{
		HttpGet httpGet = new HttpGet(GroundspeakAPI.GS_LIVE_URL + "GetPocketQueryList?AccessToken="
				+ CoreSettingsForward.accessTokenUrlCodiert + "&format=json");
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

	/**
	 * @param accessToken
	 * @param GUID
	 * @param Uri
	 * @param conectionTimeout
	 *            Config.settings.conection_timeout.getValue()
	 * @param socketTimeout
	 *            Config.settings.socket_timeout.getValue()t
	 * @return
	 */
	public static int GetPocketQueryUri(String GUID, String Uri)
	{
		HttpGet httpGet = new HttpGet(GroundspeakAPI.GS_LIVE_URL + "GetPocketQueryUrls?AccessToken=" + CoreSettingsForward.accessToken
				+ "&PocketQueryGuid=" + GUID + "&format=json");
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

	/**
	 * @param AccessToken
	 *            Config.GetAccessToken(true)
	 * @param pocketQueryConfig
	 *            Config.settings.PocketQueryFolder.getValue()
	 * @param PqFolder
	 * @return
	 */
	public static int DownloadSinglePocketQuery(PQ pocketQuery, String PqFolder)
	{
		HttpGet httpGet = new HttpGet(GroundspeakAPI.GS_LIVE_URL + "GetPocketQueryZippedFile?format=json&AccessToken="
				+ CoreSettingsForward.accessTokenUrlCodiert + "&PocketQueryGuid=" + pocketQuery.GUID);

		try
		{
			// String result = GroundspeakAPI.Execute(httpGet);
			httpGet.setHeader("Accept", "application/json");
			httpGet.setHeader("Content-type", "application/json");

			// Execute HTTP Post Request
			String result = "";
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = httpclient.execute(httpGet);

			int buffLen = 32 * 1024;
			byte[] buff = new byte[buffLen];
			InputStream inputStream = response.getEntity().getContent();
			int buffCount = inputStream.read(buff, 0, buffLen);
			int buffPos = 0;
			result = ""; // now read from the response until the ZIP Informations are beginning or to the end of stream
			for (int i = 0; i < buffCount; i++)
			{
				byte c = buff[i];
				result += (char) c;

				if (result.contains("\"ZippedFile\":\""))
				{ // The stream position represents the beginning of the ZIP block // to have a correct JSON Array we must add a "}} to the
					// result
					result += "\"}}";
					buffPos = i; // Position im Buffer, an der die ZIP-Infos beginnen
					break;
				}
			}

			//
			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					GroundspeakAPI.LastAPIError = "";
					SimpleDateFormat postFormater = new SimpleDateFormat("yyyyMMddHHmmss");
					String dateString = postFormater.format(pocketQuery.DateLastGenerated);
					String local = PqFolder + pocketQuery.Name + "_" + dateString + ".zip";

					// String test = json.getString("ZippedFile");

					FileOutputStream fs;
					fs = new FileOutputStream(local);
					BufferedOutputStream bfs = new BufferedOutputStream(fs);

					try
					{
						// int firstZipPos = result.indexOf("\"ZippedFile\":\"") + 14;
						// int lastZipPos = result.indexOf("\"", firstZipPos + 1) - 1;
						CB_Core.Converter.Base64.decodeStreamToStream(inputStream, buff, buffLen, buffCount, buffPos, bfs);
					}
					catch (Exception ex)
					{
					}

					// fs.write(resultByte);
					bfs.flush();
					bfs.close();
					fs.close();

					result = null;
					System.gc();

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
