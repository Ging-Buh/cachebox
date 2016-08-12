/* 
 * Copyright (C) 2011 team-cachebox.de
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

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import CB_Core.CB_Core_Settings;

/**
 * Diese Klasse stellt eine verbindung zu Team-Cachebox.de her und gibt dort hinterlegte Informationen zurück. (GCAuth url ; Versionsnummer)
 * 
 * @author Longri
 */
public class CB_Api {

	private static final String CB_API_URL_GET_URLS = "http://team-cachebox.de/CB_API/index.php?get=url_ACB";
	private static final String CB_API_URL_GET_URLS_Staging = "http://team-cachebox.de/CB_API/index.php?get=url_ACB_Staging";

	/**
	 * Gibt die bei Team-Cachebox.de hinterlegte GC Auth url zurück
	 * 
	 * @param staging
	 *            Config.settings.StagingAPI.getValue()
	 * @return String
	 */
	public static String getGcAuthUrl() {
		String result = "";

		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(CB_Core_Settings.StagingAPI.getValue() ? CB_API_URL_GET_URLS_Staging : CB_API_URL_GET_URLS);

			httppost.setHeader("Accept", "application/json");
			httppost.setHeader("Content-type", "application/json");

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				result += line + "\n";
			}

			try
			// Parse JSON Result
			{
				JSONTokener tokener = new JSONTokener(result);
				JSONObject json = (JSONObject) tokener.nextValue();
				if (CB_Core_Settings.StagingAPI.getValue())
					return json.getString("GcAuth_ACB_Staging");
				return json.getString("GcAuth_ACB");

			} catch (JSONException e) {
				e.printStackTrace();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
		return "";
	}
}
