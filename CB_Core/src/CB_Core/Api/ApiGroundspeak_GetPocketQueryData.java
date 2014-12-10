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

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import CB_Core.Api.PocketQuery.PQ;

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
	final static org.slf4j.Logger log = LoggerFactory.getLogger(ApiGroundspeak_GetPocketQueryData.class);
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
			log.error("ApiGS_GetPocketQueryData", e.getMessage());
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
		log.debug("got " + jCaches.length() + " Caches from gc");

		for (int i = 0; i < jCaches.length(); i++)
		{
			String gcCode = (String) jCaches.get(i);

			log.debug("handling " + gcCode);
			caches.add(gcCode);
		}

		result = new ApiGroundspeakResult(0, "OK");
		return result;
	}
}
