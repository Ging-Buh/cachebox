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

package CB_Core.Types;

import CB_Core.GlobalCore;

/**
 * Ein Koordinaten Typ, der speziell zum Messen einer Koordinaten Reihe da ist. <br>
 * Dieser Typ implementiert das Comparable Interface um einen Vergleich über die
 * Distanz zur Referenz Koordinate zu ermöglichen.
 * 
 * @author Longri
 */
public class MeasuredCoord implements Comparable<MeasuredCoord>
{
	private double Latitude = 0;
	private double Longitude = 0;
	private float Accuracy = 0.0f;

	/**
	 * Die Referenz Coordinate, auf die sich der Vergleich bezieht. <br>
	 * Je grösser der Abstand zur Referenz Coordinate desto höher der Index in
	 * einer Liste.
	 */
	public static Coordinate Referenz;

	/**
	 * Constructor
	 * 
	 * @param latitude
	 * @param longitude
	 * @param accuracy
	 */
	public MeasuredCoord(double latitude, double longitude, float accuracy)
	{
		Latitude = latitude;
		Longitude = longitude;
		Accuracy = accuracy;
	}

	/**
	 * Gibt die Latitude dieser Koordinate zurück
	 * 
	 * @return double
	 */
	public double getLatitude()
	{
		return Latitude;
	}

	/**
	 * Gibt die Longitude dieser Koordinate zurück
	 * 
	 * @return double
	 */
	public double getLongitude()
	{
		return Longitude;
	}

	/**
	 * Gibt die Genauigkeit dieser gemessenen Koordinate zurück!
	 * 
	 * @return float
	 */
	public float getAccuracy()
	{
		return Accuracy;
	}

	@Override
	public int compareTo(MeasuredCoord o2)
	{
		float dist1 = this.Distance();
		float dist2 = o2.Distance();

		float acc1 = this.Accuracy;
		float acc2 = o2.Accuracy;

		if (dist1 < dist2)
		{
			return -1;
		}
		else if (dist1 == dist2)
		{
			// Wenn die Distanzen gleich sind werden noch die Genauigkeitswerte
			// verglichen!
			return (acc1 < acc2 ? -1 : (acc1 == acc2 ? 0 : 1));
		}
		else
		{
			return 1;
		}

	}

	/**
	 * Gibt die Entfernung zur Referenz Position als Float zurück
	 * 
	 * @return Entfernung zur übergebenen User Position als Float
	 */
	public float Distance()
	{
		float[] dist = new float[4];
		Coordinate.distanceBetween(this.Latitude, this.Longitude,
				Referenz.Latitude, Referenz.Longitude, dist);

		return (float) dist[0];
	}

}
