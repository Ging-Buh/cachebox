/* 
 * Copyright (C) 2011-2014 team-cachebox.de
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
package CB_Locator;

public class Formatter {
	public static String FormatLatitudeDM(double latitude) {
		String result = FormatDM(latitude);
		if (latitude < 0)
			result += "S";
		else
			result += "N";
		return result;
	}

	public static String FormatLongitudeDM(double longitude) {
		String result = FormatDM(longitude);
		if (longitude < 0)
			result += "W";
		else
			result += "E";
		return result;
	}

	private static String FormatDM(double latOrlon) {
		int deg = (int) latOrlon;
		double frac = latOrlon - deg;
		double min = frac * 60;

		String result = Math.abs(deg) + "\u00B0 " + String.format("%.3f", Math.abs(min));
		return result;
	}

	public static String FormatCoordinate(Coordinate coordinate, String how) {
		String sLat = FormatDM(coordinate.getLatitude()).replace(',', '.');
		String sLon = FormatDM(coordinate.getLongitude()).replace(',', '.');
		String dirLat = coordinate.getLatitude() < 0 ? "S" : "N";
		String dirLon = coordinate.getLongitude() < 0 ? "W" : "E";
		String result = dirLat + " " + sLat + " " + dirLon + " " + sLon;
		/*
		switch (how) {
		default:
		}
		*/
		return result;
	}

	/**
	 * Returns a readable String from given speed value
	 * 
	 * @param kmh
	 *            sped value as float
	 * @param ImperialUnits
	 *            True for using Imperial Units
	 * @return
	 */
	public static String SpeedString(float kmh, boolean ImperialUnits) {
		if (ImperialUnits)
			return SpeedStringImperial(kmh);
		else
			return SpeedStringMetric(kmh);
	}

	private static String SpeedStringMetric(float kmh) {
		return String.format("%.2f km/h", kmh);
	}

	private static String SpeedStringImperial(float kmh) {
		return String.format("%.2f mph", kmh / 1.6093f);
	}

}
