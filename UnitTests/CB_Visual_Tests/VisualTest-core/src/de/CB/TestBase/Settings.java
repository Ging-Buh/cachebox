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
package de.CB.TestBase;

import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingUsage;
import CB_Utils.Settings.SettingsList;

public interface Settings extends CB_UI_Base.settings.CB_UI_Base_Settings, CB_Locator.LocatorSettings {

	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	public static final SettingModus DEVELOPER = SettingModus.DEVELOPER;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;

	public static final SettingBool test = (SettingBool) SettingsList.addSetting(new SettingBool("test", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ALL));

	public static final SettingBool MapNorthOriented = new SettingBool("MapNorthOriented", SettingCategory.Map, NORMAL, true, SettingStoreType.Global, SettingUsage.ALL);

	public static final SettingBool ImperialUnits = new SettingBool("ImperialUnits", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global, SettingUsage.ALL);

	// Settings Compass
	public static final SettingInt HardwareCompassLevel = (SettingInt) SettingsList.addSetting(new SettingInt("HardwareCompassLevel", SettingCategory.Gps, NORMAL, 5, SettingStoreType.Global, SettingUsage.ALL));

	public static final SettingBool HardwareCompass = new SettingBool("HardwareCompass", SettingCategory.Gps, NORMAL, true, SettingStoreType.Global, SettingUsage.ALL);

	public static final SettingInt gpsUpdateTime = (SettingInt) SettingsList.addSetting(new SettingInt("gpsUpdateTime", SettingCategory.Gps, NORMAL, 500, SettingStoreType.Global, SettingUsage.ALL));

}
