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

package CB_Core;

import junit.framework.TestCase;
import CB_Core.Types.MeasuredCoord;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Utils.MathUtils.CalculationType;

/**
 * Test Klasse zum testen des Typs MeasuredCoord
 * 
 * @author Longri
 */
public class MeasuredCoordTest extends TestCase {

	private MeasuredCoord mMeasuredCoord;

	@Override
	public void setUp() throws Exception {

		super.setUp();
		mMeasuredCoord = new MeasuredCoord(49.428333, 6.203333, 12.0f);
	}

	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
		mMeasuredCoord = null;
	}

	public void testConstructor() {
		assertTrue("Objekt muss konstruierbar sein", mMeasuredCoord != null);
	}

	public void testDistance() {

		Coordinate Referenz = new CoordinateGPS(49.427700, 6.204300);
		assertTrue("Objekt muss konstruierbar sein", Referenz != null);

		MeasuredCoord.Referenz = Referenz;

		float distance = mMeasuredCoord.Distance(CalculationType.ACCURATE);
		assertTrue("Entfernung muss 99.38391m sein", (distance > 99.38390) && (distance < 99.38392));

		distance = mMeasuredCoord.Distance(CalculationType.FAST);
		assertTrue("Entfernung muss 99.38391m sein", (distance > 99) && (distance < 100));

	}
}
