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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import CB_Core.CB_Core_Settings;

/**
 * Search Definitions
 * 
 * @author Hubert
 */
public class Search {
	public int number;
	public boolean excludeHides = false;
	public boolean excludeFounds = false;
	public boolean available = true;
	int geocacheLogCount = 10;
	int trackableLogCount = 10;
	protected boolean isLite;

	Search(int number) {
		this.number = number;
	}

	protected void getRequest(JSONObject request, boolean isLite) throws JSONException {
		this.isLite = isLite;
		request.put("IsLite", isLite);
		request.put("StartIndex", 0);
		request.put("MaxPerPage", number);
		request.put("GeocacheLogCount", geocacheLogCount);
		request.put("TrackableLogCount", trackableLogCount);
		if (available) {
			JSONObject excl = new JSONObject();
			excl.put("Archived", false);
			excl.put("Available", true);
			request.put("GeocacheExclusions", excl);

		}
		if (excludeHides) {
			JSONObject excl = new JSONObject();
			JSONArray jarr = new JSONArray();
			jarr.put(CB_Core_Settings.GcLogin.getValue());
			excl.put("UserNames", jarr);
			request.put("NotHiddenByUsers", excl);
		}

		if (excludeFounds) {
			JSONObject excl = new JSONObject();
			JSONArray jarr = new JSONArray();
			jarr.put(CB_Core_Settings.GcLogin.getValue());
			excl.put("UserNames", jarr);
			request.put("NotFoundByUsers", excl);
		}
	}

	// isLite kann hier nochmal abgefragt werden da dieser Wert von einzelnen Search-Objecten geändert werden könnte
	protected boolean getIsLite() {
		return isLite;
	}

	public void setIsLite(boolean isLite) {
		this.isLite = isLite;
	}
}