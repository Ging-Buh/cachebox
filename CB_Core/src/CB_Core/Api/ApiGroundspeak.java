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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Base Class for all API-Classes of Groundspeak API access
 * 
 * @author Hubert
 * @author Longri
 */
public class ApiGroundspeak extends ApiBase
{
	protected enum queryType
	{
		GET, POST
	}

	protected ApiGroundspeakResult result;
	protected byte apiStatus = 0;
	protected boolean isLite = true;

	public ApiGroundspeak()
	{
		result = new ApiGroundspeakResult(-1, "not initialized");
	}

	// public ApiGroundspeakResult execute()
	// {
	// String URL = CB_Core_Settings.StagingAPI.getValue() ? GroundspeakAPI.STAGING_GS_LIVE_URL : GroundspeakAPI.GS_LIVE_URL;
	// // check API staus
	// int res = checkApiState();
	// if (res != 0)
	// {
	// // error while checking API state
	// result.setResult(-1);
	// result.setMessage("Error while checking API State");
	// return result;
	// }
	//
	// // generate request string in child class
	// JSONObject request = new JSONObject();
	// // add ApiKey to request
	// try
	// {
	// request.put("AccessToken", GroundspeakAPI.GetAccessToken());
	// }
	// catch (JSONException e)
	// {
	// // error while checking API state
	// result.setResult(-1);
	// result.setMessage("Error while adding AccessToken to Request");
	// return result;
	// }
	//
	// // Generate Request in child class
	// if (!getRequest(request))
	// {
	// // error while getting RequestString
	// result.setResult(-1);
	// result.setMessage("ApiGroundspeak(" + getApiFunction() + "): Error while getting RequestString");
	// return result;
	// }
	//
	// // generating HTTP-Post object
	// HttpRequestBase httpRequest = null;
	// switch (getQueryType())
	// {
	// case GET:
	// {
	// String requestString = "";
	// String[] names = JSONObject.getNames(request);
	// for (String name : names)
	// {
	// requestString += "&";
	// requestString += name + "=";
	// try
	// {
	// requestString += request.getString(name);
	// }
	// catch (JSONException e)
	// {
	// }
	// }
	// HttpGet httpGet = new HttpGet(URL + getApiFunction() + "?format=json" + requestString);
	// httpRequest = httpGet;
	// break;
	// }
	// case POST:
	// {
	// String requestString = request.toString();
	// HttpPost httppost = new HttpPost(URL + getApiFunction() + "?format=json");
	// try
	// {
	// httppost.setEntity(new ByteArrayEntity(requestString.getBytes("UTF8")));
	// }
	// catch (UnsupportedEncodingException e3)
	// {
	// Logger.Error("ApiGroundspeak (" + getApiFunction() + "):UnsupportedEncodingException", e3.getMessage());
	// result.setResult(-1);
	// result.setMessage("ApiGroundspeak (" + getApiFunction() + "):UnsupportedEncodingException: " + e3.getMessage());
	// return result;
	// }
	// httppost.setHeader("Accept", "application/json");
	// httppost.setHeader("Content-type", "application/json");
	// httpRequest = httppost;
	// break;
	// }
	// }
	// // Execute HTTP Get/Post Request
	// String httpResult = "";
	// try
	// {
	// httpResult = GroundspeakAPI.Execute(httpRequest);
	// if (httpResult.contains("The service is unavailable"))
	// {
	// result.setResult(-1);
	// result.setMessage("ApiGroundspeak (" + getApiFunction() + "): The service is unavailable");
	// return result;
	// }
	// }
	// catch (ConnectTimeoutException e)
	// {
	// Logger.Error("SearchForGeocaches:ConnectTimeoutException", e.getMessage());
	// showToastConnectionError();
	//
	// result.setResult(-1);
	// result.setMessage("ApiGroundspeak (" + getApiFunction() + "): Connection Timeout");
	// return result;
	//
	// }
	// catch (ClientProtocolException e)
	// {
	// Logger.Error("SearchForGeocaches:ClientProtocolException", e.getMessage());
	// result.setResult(-1);
	// result.setMessage("ApiGroundspeak (" + getApiFunction() + "): ClientProtocolException: " + e.getMessage());
	// return result;
	// }
	// catch (IOException e)
	// {
	// Logger.Error("ApiGroundspeak (" + getApiFunction() + "):IOException", e.getMessage());
	// result.setResult(-1);
	// result.setMessage("ApiGroundspeak (" + getApiFunction() + "): IOException: " + e.getMessage());
	// return result;
	// }
	// // Parse JSON Result
	// try
	// {
	// JSONTokener tokener = new JSONTokener(httpResult);
	// JSONObject json = (JSONObject) tokener.nextValue();
	// JSONObject status = json.getJSONObject("Status");
	// if (status.getInt("StatusCode") == 0)
	// {
	// result = parseJson(json);
	// }
	// else
	// {
	// String res1 = "StatusCode = " + status.getInt("StatusCode") + "\n";
	// res1 += status.getString("StatusMessage") + "\n";
	// res1 += status.getString("ExceptionDetails");
	// result.setResult(-1);
	// result.setMessage("ApiGroundspeak (" + getApiFunction() + "): GetStatus: " + res1);
	// return result;
	//
	// }
	// }
	// catch (Exception ex)
	// {
	// result.setResult(-1);
	// result.setMessage("ApiGroundspeak (" + getApiFunction() + "): Error Parsing Json Result: " + ex.getMessage());
	// return result;
	//
	// }
	// return result;
	// }

	protected ApiGroundspeakResult parseJson(JSONObject json) throws JSONException
	{
		return new ApiGroundspeakResult(-1, "not initialized");
	}

	/**
	 * a special class with access to a UI can show error message here
	 */
	protected void showToastConnectionError()
	{
	}

	protected boolean getRequest(JSONObject request)
	{
		return false;
	}

	protected String getApiFunction()
	{
		return "";
	}

	protected queryType getQueryType()
	{
		return queryType.GET;
	}

	private int checkApiState()
	{
		// check API staus
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
		return 0;
	}
}
