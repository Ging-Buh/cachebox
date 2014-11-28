package CB_Core.Api;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import CB_Core.Api.PocketQuery.PQ;
import CB_Utils.Log.Logger;

/**
 * Imports a single PocketQuery from Groundspeak API directly without ZIP Because Groundspeak API only delivers a lite dataset of the caches
 * only the gcCodes are imported here. Cache data must be imported with SearchForGeocaches for this list of caches.
 * 
 * @author Hubert Url:
 *         https://api.groundspeak.com/LiveV5/geocaching.svc/GetPocketQueryData?AccessToken={ACCESSTOKEN}&PocketQueryGuid={POCKETQUERYGUID
 *         }&StartItem={STARTITEM}&MaxItems={MAXITEMS}&GCListOnly={GCLISTONLY}
 *         <p>
 *         HTTP Method: GET
 */
public class ApiGroundspeak_GetPocketQueryData extends ApiGroundspeak
{
	private PQ pocketQuery;
	private ArrayList<String> caches;

	public ApiGroundspeak_GetPocketQueryData()
	{
		super();
		caches = new ArrayList<String>();
	}

	public void setPQ(PQ pocketQuery)
	{
		this.pocketQuery = pocketQuery;
	}

	public ArrayList<String> getCaches()
	{
		return caches;
	}

	@Override
	protected queryType getQueryType()
	{
		return queryType.GET;
	}

	@Override
	protected String getApiFunction()
	{
		return "GetPocketQueryData";
	}

	@Override
	protected boolean getRequest(JSONObject request)
	{
		// create Request String here
		try
		{
			request.put("PocketQueryGuid", pocketQuery.GUID);
			request.put("StartItem", 0);
			request.put("MaxItems", 1000);
			request.put("GCListOnly", true);
		}
		catch (JSONException e)
		{
			Logger.Error("ApiGS_GetPocketQueryData", e.getMessage());
			return false;
		}

		return true;
	}

	@Override
	protected ApiGroundspeakResult parseJson(JSONObject json) throws JSONException
	{
		caches.clear();
		ApiGroundspeakResult result = new ApiGroundspeakResult(-1, "");

		JSONArray jCaches = json.getJSONArray("CacheCodes");
		Logger.DEBUG("got " + jCaches.length() + " Caches from gc");

		for (int i = 0; i < jCaches.length(); i++)
		{
			String gcCode = (String) jCaches.get(i);

			Logger.DEBUG("handling " + gcCode);
			caches.add(gcCode);
		}

		result = new ApiGroundspeakResult(0, "OK");
		return result;
	}
}
