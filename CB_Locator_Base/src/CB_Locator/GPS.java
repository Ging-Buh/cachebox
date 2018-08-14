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

import CB_Utils.Lists.CB_List;
import CB_Utils.Log.Log;

import java.util.Iterator;

/**
 * Klasse zum verwalten von GPS Status
 *
 * @author Longri
 */
public class GPS {
    private static final String log = "GPS";
    private static int mSatVisible;
    private static int mSatFixed;

    private static CB_List<GpsStrength> mSatList;

    public static int getVisibleSats() {
        return mSatVisible;
    }

    public static int getFixedSats() {
        return mSatFixed;
    }

    public static CB_List<GpsStrength> getSatList() {
        return mSatList;
    }

    public static void setSatList(CB_List<GpsStrength> coreSatList) {
        mSatList = coreSatList;
    }

    public static String getSatAndFix() {
        return String.valueOf(mSatVisible) + "/" + String.valueOf(mSatFixed);
    }

    public static void setStatus(GpsStatus status) {
        if (status == null)
            return;

        Iterator<GpsSatellite> statusIterator = status.getSatellites().iterator();

        int satellites = 0;
        int fixed = 0;
        CB_List<GpsStrength> SatList = new CB_List<GpsStrength>();
        while (statusIterator.hasNext()) {
            GpsSatellite sat = statusIterator.next();
            if (sat.usedInFix()) {
                fixed++;
            }
            satellites++;

            // satellite signal strength

            if (sat.usedInFix()) {
                // Log.d("Cachbox satellite signal strength", "Sat #" + satellites + ": " + sat.getSnr() + " FIX");
                SatList.add(new GpsStrength(true, sat.getSnr()));
            } else {
                // Log.d("Cachbox satellite signal strength", "Sat #" + satellites + ": " + sat.getSnr());
                SatList.add(new GpsStrength(false, sat.getSnr()));
            }

        }

        mSatFixed = fixed;
        mSatVisible = satellites;
        SatList.sort();
        mSatList = SatList;
    }

    public static void setSatFixes(int fixed) {
        Log.trace(log, "set SatFixes to:" + fixed);
        mSatFixed = fixed;
    }

    public static void setSatVisible(int satellites) {
        mSatVisible = satellites;
    }

}
