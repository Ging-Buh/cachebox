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
import CB_Utils.Settings.*;
import CB_Utils.Util.HSV_Color;

public interface CB_UI_Base_Settings extends CB_Utils_Settings {

    SettingBool nightMode = new SettingBool("nightMode", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);

    SettingFolder SkinFolder = new SettingFolder("SkinFolder", SettingCategory.Folder, DEVELOPER, "default", SettingStoreType.Global, SettingUsage.ACB, false);

    SettingInt FONT_SIZE_COMPASS_DISTANCE = new SettingInt("FONT_SIZE_COMPASS_DISTANCE", SettingCategory.Skin, EXPERT, 25, SettingStoreType.Global, SettingUsage.ACB);
    SettingInt FONT_SIZE_BIG = new SettingInt("FONT_SIZE_BIG", SettingCategory.Skin, EXPERT, 16, SettingStoreType.Global, SettingUsage.ACB);
    SettingInt FONT_SIZE_NORMAL = new SettingInt("FONT_SIZE_NORMAL", SettingCategory.Skin, EXPERT, 14, SettingStoreType.Global, SettingUsage.ACB);
    SettingInt FONT_SIZE_NORMAL_BUBBLE = new SettingInt("FONT_SIZE_NORMAL_BUBBLE", SettingCategory.Skin, EXPERT, 13, SettingStoreType.Global, SettingUsage.ACB);
    SettingInt FONT_SIZE_SMALL = new SettingInt("FONT_SIZE_SMALL", SettingCategory.Skin, EXPERT, 12, SettingStoreType.Global, SettingUsage.ACB);
    SettingInt FONT_SIZE_SMALL_BUBBLE = new SettingInt("FONT_SIZE_SMALL_BUBBLE", SettingCategory.Skin, EXPERT, 10, SettingStoreType.Global, SettingUsage.ACB);

    SettingBool useMipMap = new SettingBool("useMipMap", SettingCategory.Skin, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool dontUseAmbient = new SettingBool("dontUseAmbient", SettingCategory.Skin, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingInt ambientTime = new SettingInt("ambientTime", SettingCategory.Skin, EXPERT, 10, SettingStoreType.Global, SettingUsage.ACB);

    SettingDouble MapViewFontFaktor = new SettingDouble("MapViewFontFaktor", SettingCategory.Map, NEVER, 1.0, SettingStoreType.Global, SettingUsage.ACB);

    SettingInt LongClickTime = new SettingInt("LongClicktime", SettingCategory.Misc, EXPERT, 600, SettingStoreType.Global, SettingUsage.ACB);
    SettingsAudio GlobalVolume = new SettingsAudio("GlobalVolume", SettingCategory.Sounds, NORMAL, new Audio("data/sound/Approach.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);

    SettingFloat MapViewDPIFaktor = new SettingFloat("MapViewDPIFaktor", SettingCategory.Map, EXPERT, (float) Global.displayDensity, SettingStoreType.Global, SettingUsage.ACB);

    SettingFolder ImageCacheFolder = new SettingFolder("ImageCacheFolder", SettingCategory.Folder, NEVER, Config_Core.mWorkPath + "/repository/cache", SettingStoreType.Local, SettingUsage.ACB, true);

    SettingBool GestureOn = new SettingBool("GestureOn", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);

    SettingColor LiveMapBackgroundColor = new SettingColor("LiveMapBackgroundColor", SettingCategory.LiveMap, NORMAL, new HSV_Color(0.8f, 0.8f, 1f, 1f), SettingStoreType.Global, SettingUsage.ACB);

    SettingColor SolvedMysteryColor = new SettingColor("SolvedMysteryColor", SettingCategory.Skin, EXPERT, new HSV_Color(0.2f, 1f, 0.2f, 1f), SettingStoreType.Global, SettingUsage.ACB);
    SettingBool SettingsShowExpert = new SettingBool("SettingsShowExpert", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool SettingsShowAll = new SettingBool("SettingsShowAll", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingFile Sel_LanguagePath = (SettingFile) SettingsList.addSetting(new SettingFile("Sel_LanguagePath", SettingCategory.Folder, NEVER, "data/lang/en-GB/strings.ini", SettingStoreType.Platform, SettingUsage.ALL, "lan"));
    SettingFolder LanguagePath = new SettingFolder("LanguagePath", SettingCategory.Folder, NEVER, "data/lang", SettingStoreType.Global, SettingUsage.ALL, true);

}
