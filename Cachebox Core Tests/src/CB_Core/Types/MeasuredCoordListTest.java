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

import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Test Klasse zum testen der MeasuredCoordList
 * 
 * @author Longri
 */
public class MeasuredCoordListTest extends TestCase
{

	private MeasuredCoordList mMeasuredCoordList;

	@Override
	public void setUp() throws Exception
	{

		super.setUp();
		mMeasuredCoordList = new MeasuredCoordList();
	}

	@Override
	protected void tearDown() throws Exception
	{

		super.tearDown();
		mMeasuredCoordList = null;
	}

	public void testConstructor()
	{
		assertTrue("Objekt muss konstruierbar sein", mMeasuredCoordList != null);
	}

	public void testList()
	{

		Coordinate Referenz = new Coordinate();
		assertTrue("Objekt muss konstruierbar sein", Referenz != null);
		Referenz.setLatitude(49.427700);
		Referenz.setLongitude(6.204300);

		MeasuredCoord.Referenz = Referenz;

		// add Messwerte zur Liste
		mMeasuredCoordList.add(new MeasuredCoord(49.428333, 6.203333, 12.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.428332, 6.203333, 133.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.428337, 6.203333, 13.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.428333, 6.203373, 44.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.428333, 6.203453, 45.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.428333, 6.203456, 12.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.203456, 6.203333, 35.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.428337, 6.203373, 67.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.428337, 6.203453, 67.5f));
		mMeasuredCoordList.add(new MeasuredCoord(49.428337, 6.203456, 23.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.428337, 6.203373, 14.0f));
		mMeasuredCoordList.add(new MeasuredCoord(49.428337, 6.203633, 43.0f));

		// Die Liste sollte jetzt 12 Werte haben.
		assertTrue("mMeasuredCoordList muss 12 Werte haben", mMeasuredCoordList.size() == 12);

		// Teste Sortierung
		mMeasuredCoordList.sort();

		Iterator<MeasuredCoord> iterator = mMeasuredCoordList.iterator();

		int index = -1;
		do
		{
			MeasuredCoord tmp = iterator.next();
			// Teste ob die Distance der Vorhergehenden Koordinate kleiner ist.
			// Achtung durch die Überschriebene add Methode der
			// MeasuredCoordList
			// wird die, der Sortierung zu Grunde liegende Referenz Koordinate,
			// neu gesetzt!
			if (index > -1)
			{
				if (tmp.Distance() == mMeasuredCoordList.get(index).Distance())
				{
					if (tmp.getAccuracy() != mMeasuredCoordList.get(index).getAccuracy())
					{
						assertTrue("mMeasuredCoordList ist falsch Sortiert", tmp.getAccuracy() > mMeasuredCoordList.get(index)
								.getAccuracy());
					}
				}
				else
				{
					assertTrue("mMeasuredCoordList ist falsch Sortiert", tmp.Distance() > mMeasuredCoordList.get(index).Distance());
				}

			}
			index++;
		}
		while (iterator.hasNext());

		// Teste bei Sortierung entstandene Referenz Koordinate

		boolean test = true;
		if (MeasuredCoord.Referenz.getLatitude() != 49.40959516666667) test = false;
		else if (MeasuredCoord.Referenz.getLongitude() != 6.203408499999999) test = false;
		assertTrue("mMeasuredCoordList hat beim Sortieren eine falsche Referenz erzeugt", test);

		// eleminiere Ausreisser Werte mit einer Referenz Distanz > 50m

		mMeasuredCoordList.clearDiscordantValue();

	}
}
