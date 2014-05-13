package API;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import __Static.InitTestDBs;
import CB_Core.Api.GroundspeakAPI;
import CB_UI.Config;
import CB_Utils.Util.FileIO;

/**
 * Der Test ist kein Wirklicher Test. Hier werden die GS CacheTypes Herrunter geladen . Damit die Cache Types immer Aktuell gehalten werden
 * können.
 * 
 * @author Longri
 */
public class GenCacheTypes extends TestCase
{

	public static String LastAPIError = "";

	@Test
	public void testGetAllAttributes() throws IOException
	{

		InitTestDBs.InitalConfig();
		String accessToken = Config.GetAccessToken();
		assertFalse("Kein Access Key gefunden, liegt die Config an der richtigen stelle?", accessToken.equals(""));

		// read all GS Attributes
		ArrayList<GsCacheTypes> attList = new ArrayList<GsCacheTypes>();

		try
		{
			HttpGet httppost = new HttpGet(GroundspeakAPI.GS_LIVE_URL + "GetGeocacheTypes?AccessToken=" + accessToken + "&format=json");

			String result = GroundspeakAPI.Execute(httppost);

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				JSONObject status = json.getJSONObject("Status");
				if (status.getInt("StatusCode") == 0)
				{
					LastAPIError = "";
					JSONArray jAttributes = json.getJSONArray("GeocacheTypes");

					for (int ii = 0; ii < jAttributes.length(); ii++)
					{
						JSONObject jAtt = (JSONObject) jAttributes.get(ii);

						GsCacheTypes tmp = new GsCacheTypes();
						try
						{
							tmp.ID = jAtt.getInt("GeocacheTypeId");

							String Name = jAtt.getString("GeocacheTypeName");
							Name = Name.replace(" ", "_").trim();
							int Pos1 = Name.indexOf("(");
							if (Pos1 > 0)
							{
								int Pos2 = Name.indexOf(")");
								String clear = Name.substring(Pos1, Pos2);
								Name = Name.replace(clear, "").trim();
							}
							Name = Name.replace("_/", "").trim();

							tmp.Name = Name;
							tmp.Url = jAtt.getString("ImageURL");

							tmp.Description = jAtt.getString("Description");
						}
						catch (JSONException e)
						{
						}

						attList.add(tmp);
					}

				}
				else
				{
					LastAPIError = "";
					LastAPIError = "StatusCode = " + status.getInt("StatusCode") + "\n";
					LastAPIError += status.getString("StatusMessage") + "\n";
					LastAPIError += status.getString("ExceptionDetails");

					return;
				}

			}
			catch (JSONException e)
			{

				e.printStackTrace();
			}

		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
			return;
		}

		Iterator<GsCacheTypes> iterator = attList.iterator();

		// create folder
		File FolderPath = new File("./testdata/CacheTypes");

		FolderPath.mkdirs();

		// Write to File

		// Download ATTR Images

		iterator = attList.iterator();
		do
		{
			GsCacheTypes tmp = iterator.next();
			String lacalName = "./testdata/CacheTypes/" + tmp.Name + ".png";

			FileIO.Download(tmp.Url, lacalName);

		}
		while (iterator.hasNext());

	}

	class GsCacheTypes
	{
		int ID;
		String Name;

		String Url;
		String Description;

	}

}
