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

import CB_Locator.Coordinate;

/**
 * @author Hubert
 */
public class SearchGCOwner extends SearchCoordinate
{
	public String OwnerName;

	public SearchGCOwner(int number, Coordinate pos, float distanceInMeters, String ownerName)
	{
		super(number, pos, distanceInMeters);
		this.OwnerName = ownerName;
	}

	@Override
	protected void getRequest(JSONObject request, boolean isLite) throws JSONException
	{
		super.getRequest(request, isLite);
		JSONObject jhidden = new JSONObject();
		JSONArray jusers = new JSONArray();
		jusers.put(OwnerName);
		jhidden.put("UserNames", jusers);
		request.put("HiddenByUsers", jhidden);
	}
}