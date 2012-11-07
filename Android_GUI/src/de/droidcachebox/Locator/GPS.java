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

package de.droidcachebox.Locator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import CB_Core.Events.GpsStateChangeEventList;
import CB_Core.Locator.GpsStrength;
import CB_Core.Math.UiSizes;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.view.View;
import de.droidcachebox.Global;
import de.droidcachebox.R;

/**
 * Klasse zum verwalten von GPS Status
 * 
 * @author Longri
 */
public class GPS
{

	private static int mSatVisible;
	private static int mSatFixed;

	private static ArrayList<GpsStrength> mSatList;

	public static int getVisibleSats()
	{
		return mSatVisible;
	}

	public static int getFixedSats()
	{
		return mSatFixed;
	}

	public static ArrayList<GpsStrength> getSatList()
	{
		return mSatList;
	}

	public static String getSatAndFix()
	{
		return String.valueOf(mSatVisible) + "/" + String.valueOf(mSatFixed);
	}

	public static class GpsStatusListener implements GpsStatus.Listener
	{

		public GpsStatusListener(LocationManager locationManager)
		{
			mLocationmanager = locationManager;
		}

		private LocationManager mLocationmanager = null;

		@Override
		public void onGpsStatusChanged(int event)
		{
			if (mLocationmanager == null) return;

			if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS)
			{
				GpsStatus status = mLocationmanager.getGpsStatus(null);
				Iterator<GpsSatellite> statusIterator = status.getSatellites().iterator();

				int satellites = 0;
				int fixed = 0;
				ArrayList<GpsStrength> SatList = new ArrayList<GpsStrength>();
				ArrayList<CB_Core.Locator.GpsStrength> coreSatList = new ArrayList<CB_Core.Locator.GpsStrength>();
				while (statusIterator.hasNext())
				{
					GpsSatellite sat = statusIterator.next();
					if (sat.usedInFix() == true)
					{
						fixed++;
					}
					satellites++;

					// satellite signal strength

					if (sat.usedInFix())
					{
						// Log.d("Cachbox satellite signal strength", "Sat #" + satellites + ": " + sat.getSnr() + " FIX");
						SatList.add(new GpsStrength(true, sat.getSnr()));
						coreSatList.add(new GpsStrength(true, sat.getSnr()));
					}
					else
					{
						// Log.d("Cachbox satellite signal strength", "Sat #" + satellites + ": " + sat.getSnr());
						SatList.add(new GpsStrength(false, sat.getSnr()));
						coreSatList.add(new GpsStrength(false, sat.getSnr()));
					}

				}

				mSatFixed = fixed;
				mSatVisible = satellites;
				Collections.sort(SatList);
				Collections.sort(coreSatList);
				mSatList = SatList;

				CB_Core.Locator.GPS.setSatFixes(fixed);
				CB_Core.Locator.GPS.setSatVisible(satellites);
				CB_Core.Locator.GPS.setSatList(coreSatList);
				GpsStateChangeEventList.Call();
			}
		}
	}

	public static void setSatStrength(View strengthView, View[] retBalken)
	{
		View[] balken = retBalken;
		if (balken == null)
		{
			balken = new View[10];
			balken[0] = (View) strengthView.findViewById(R.id.balken_view1);
			balken[1] = (View) strengthView.findViewById(R.id.balken_view2);
			balken[2] = (View) strengthView.findViewById(R.id.balken_view3);
			balken[3] = (View) strengthView.findViewById(R.id.balken_view4);
			balken[4] = (View) strengthView.findViewById(R.id.balken_view5);
			balken[5] = (View) strengthView.findViewById(R.id.balken_view6);
			balken[6] = (View) strengthView.findViewById(R.id.balken_view7);
			balken[7] = (View) strengthView.findViewById(R.id.balken_view8);
			balken[8] = (View) strengthView.findViewById(R.id.balken_view9);
			balken[9] = (View) strengthView.findViewById(R.id.balken_view10);
		}

		int count = 0;
		if (de.droidcachebox.Locator.GPS.getSatList() != null)
		{
			for (GpsStrength tmp : de.droidcachebox.Locator.GPS.getSatList())
			{
				android.view.ViewGroup.LayoutParams params;// = new android.view.LayoutParams(5, (int) (25*tmp.getStrength()));

				// balken höhe festlegen
				params = balken[count].getLayoutParams();
				params.width = 5;
				params.height = (int) (UiSizes.getStrengthHeight() * tmp.getStrength());
				balken[count].setLayoutParams(params);

				// balken farbe festlegen
				if (tmp.getFixed())
				{
					balken[count].setBackgroundColor(Global.getColor(R.attr.ListSeparator));
				}
				else
				{
					balken[count].setBackgroundColor(Global.getColor(R.attr.ListBackground_secend));
					balken[count].setBackgroundColor(Global.getColor(R.attr.TextColor_disable));
				}

				count++;
				if (count >= 9) break;
			}
		}

		// restliche balken ausschalten!
		if (count < 10)
		{
			for (int i = count; i <= 9; i++)
			{
				android.view.ViewGroup.LayoutParams params;// = new android.view.LayoutParams(5, (int) (25*tmp.getStrength()));

				params = balken[i].getLayoutParams();
				params.width = 5;
				params.height = 1;

				balken[i].setLayoutParams(params);
			}
		}
		retBalken = balken;
	}

}
