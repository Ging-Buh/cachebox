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

import CB_Locator.Coordinate;

/**
 * @author Hubert
 */
public class SearchCoordinate extends Search {
	public Coordinate pos;
	public float distanceInMeters;

	public SearchCoordinate(int number, Coordinate pos, float distanceInMeters) {
		super(number);
		this.pos = pos;
		this.distanceInMeters = distanceInMeters;
	}

	@Override
	protected void getRequest(JSONObject request, boolean isLite) throws JSONException {
		super.getRequest(request, isLite);
		JSONObject jpr = new JSONObject();
		jpr.put("DistanceInMeters", String.valueOf((int) distanceInMeters));
		JSONObject jpt = new JSONObject();
		jpt.put("Latitude", String.valueOf(pos.getLatitude()));
		jpt.put("Longitude", String.valueOf(pos.getLongitude()));
		jpr.put("Point", jpt);
		request.put("PointRadius", jpr);
	}
}