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

package CB_Locator;

/**
 * Structur f�r die Signal St�rken der Satteliten
 *
 * @author Longri
 */
public class GpsStrength implements Comparable<GpsStrength> {
    private boolean hasFix;
    private float mStrength;

    /**
     * Constructor
     *
     * @param fixed
     * @param value
     */
    public GpsStrength(boolean fixed, float value) {
        hasFix = fixed;
        mStrength = value;
    }

    /**
     * Gibt zur�ck ob der Sattelit ein Fix hat
     *
     * @return boolean
     */
    public boolean getFixed() {
        return hasFix;
    }

    /**
     * Gibt die Signal St�rke des Sateliten zur�ck
     *
     * @return float
     */
    public float getStrength() {
        return mStrength;
    }

    @Override
    public int compareTo(GpsStrength c2) {
        if (hasFix == c2.hasFix) {
            return Float.compare(mStrength, c2.mStrength);
        } else {
            if (hasFix) return -1; // less
            else return 1; // greater
        }
    }

}