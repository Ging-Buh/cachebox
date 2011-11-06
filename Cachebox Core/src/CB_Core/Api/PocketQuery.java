package CB_Core.Api;

import java.io.IOException;
import java.util.ArrayList;
import CB_Core.Api.GroundspeakAPI;
import CB_Core.Types.Trackable;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class PocketQuery
{
	/**
	 * Ruft die Liste der PQ´s ab.
	 * 
	 * @param accessToken
	 *            as String
	 * @param list
	 *            as ArrayList<String>
	 * @return
	 */
	public static int GetPocketQueryList(String accessToken, ArrayList<String> list)
	{
		HttpGet httpGet = new HttpGet(GroundspeakAPI.GS_LIVE_URL + "GetPocketQueryList?AccessToken=" + accessToken + accessToken
				+ "&format=json");

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
					JSONArray jTrackablesPQs = json.getJSONArray("PocketQueryList");

					for (int ii = 0; ii < jTrackablesPQs.length(); ii++)
					{
						JSONObject jTrackablesPQ = (JSONObject) jTrackablesPQs.get(ii);
						list.add("WERT");
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
}
