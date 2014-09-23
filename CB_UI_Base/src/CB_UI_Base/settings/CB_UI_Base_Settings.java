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
package CB_UI_Base.settings;

import CB_UI_Base.Global;
import CB_Utils.Config_Core;
import CB_Utils.Settings.Audio;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingColor;
import CB_Utils.Settings.SettingDouble;
import CB_Utils.Settings.SettingFloat;
import CB_Utils.Settings.SettingFolder;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingUsage;
import CB_Utils.Settings.SettingsAudio;
import CB_Utils.Util.HSV_Color;

public interface CB_UI_Base_Settings
{
	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	public static final SettingModus INVISIBLE = SettingModus.Invisible;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;

	public static final SettingBool nightMode = new SettingBool("nightMode", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingFolder SkinFolder = new SettingFolder("SkinFolder", SettingCategory.Folder, INVISIBLE, Config_Core.WorkPath + "/skins/default", SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingInt FONT_SIZE_COMPASS_DISTANCE = new SettingInt("FONT_SIZE_COMPASS_DISTANCE", SettingCategory.Skin, EXPERT, 25, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingInt FONT_SIZE_BIG = new SettingInt("FONT_SIZE_BIG", SettingCategory.Skin, EXPERT, 15, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingInt FONT_SIZE_NORMAL = new SettingInt("FONT_SIZE_NORMAL", SettingCategory.Skin, EXPERT, 13, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingInt FONT_SIZE_NORMAL_BUBBLE = new SettingInt("FONT_SIZE_NORMAL_BUBBLE", SettingCategory.Skin, EXPERT, 12, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingInt FONT_SIZE_SMALL = new SettingInt("FONT_SIZE_SMALL", SettingCategory.Skin, EXPERT, 11, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingInt FONT_SIZE_SMALL_BUBBLE = new SettingInt("FONT_SIZE_SMALL_BUBBLE", SettingCategory.Skin, EXPERT, 10, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingBool useMipMap = new SettingBool("useMipMap", SettingCategory.Skin, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingDouble MapViewFontFaktor = new SettingDouble("MapViewFontFaktor", SettingCategory.Map, NEVER, 1.0, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingInt LongClicktime = new SettingInt("LongClicktime", SettingCategory.Misc, NORMAL, 600, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingBool DebugSpriteBatchCountBuffer = new SettingBool("DebugSpriteBatchCountBuffer", SettingCategory.Debug, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingBool DebugMode = new SettingBool("DebugMode", SettingCategory.Debug, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingBool WriteLoggerDebugMode = new SettingBool("WriteLoggerDebugMode", SettingCategory.Debug, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingsAudio GlobalVolume = new SettingsAudio("GlobalVolume", SettingCategory.Sounds, NORMAL, new Audio("data/sound/Approach.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingFloat MapViewDPIFaktor = new SettingFloat("MapViewDPIFaktor", SettingCategory.Map, EXPERT, (float) Global.displayDensity, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingFolder ImageCacheFolderLocal = new SettingFolder("ImageCacheFolderLocal", SettingCategory.Folder, NEVER, Config_Core.WorkPath + "/repository/cache", SettingStoreType.Local, SettingUsage.ACB);

	public static final SettingBool GestureOn = new SettingBool("GestureOn", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingColor LiveMapBackgroundColor = new SettingColor("LiveMapBackgroundColor", SettingCategory.LiveMap, NORMAL, new HSV_Color(0.8f, 0.8f, 1f, 1f), SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingColor SolvedMysteryColor = new SettingColor("SolvedMysteryColor", SettingCategory.Skin, NORMAL, new HSV_Color(0.2f, 1f, 0.2f, 1f), SettingStoreType.Global, SettingUsage.ACB);

}
