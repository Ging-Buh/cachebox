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

import CB_Utils.Config_Core;
import CB_Utils.Log.LogLevel;
import CB_Utils.Settings.*;
import CB_Utils.Util.HSV_Color;

import static CB_UI_Base.AbstractGlobal.displayDensity;
import static CB_Utils.Settings.SettingCategory.*;
import static CB_Utils.Settings.SettingModus.*;
import static CB_Utils.Settings.SettingStoreType.*;
import static CB_Utils.Settings.SettingUsage.ACB;
import static CB_Utils.Settings.SettingUsage.ALL;

public interface CB_UI_Base_Settings {
    SettingEnum<Enum<LogLevel>> AktLogLevel = new SettingEnum<Enum<LogLevel>>("AktLogLevel", Debug, NORMAL, LogLevel.OFF, Platform, ALL, LogLevel.OFF);

    SettingBool nightMode = new SettingBool("nightMode", Internal, NEVER, false, Global, ACB);
    SettingBool useAndroidKeyboard = new SettingBool("useAndroidKeyboard", Skin, NORMAL, false, Global, ACB);

    SettingFolder SkinFolder = new SettingFolder("SkinFolder", Folder, DEVELOPER, "default", Global, ACB, false);

    SettingBool useDescriptiveCB_Buttons = new SettingBool("useDescriptiveCB_Buttons", Skin, EXPERT, true, Global, ACB, true);
    SettingBool rememberLastAction = new SettingBool("rememberLastAction", Skin, EXPERT, true, Global, ACB, true);
    SettingBool GestureOn = new SettingBool("GestureOn", Skin, EXPERT, true, Global, ACB,true);
    SettingBool useMipMap = new SettingBool("useMipMap", Skin, DEVELOPER, false, Global, ACB);
    SettingBool useGrayFader = new SettingBool("useGrayFader", Skin, EXPERT, false, Global, ACB, false);
    SettingInt fadeToGrayAfterXSeconds = new SettingInt("fadeToGrayAfterXSeconds", Skin, EXPERT, 10, Global, ACB);
    SettingColor SolvedMysteryColor = new SettingColor("SolvedMysteryColor", Skin, DEVELOPER, new HSV_Color(0.2f, 1f, 0.2f, 1f), Global, ACB);

    SettingInt FONT_SIZE_COMPASS_DISTANCE = new SettingInt("FONT_SIZE_COMPASS_DISTANCE", Skin, EXPERT, 25, Global, ACB);
    SettingInt FONT_SIZE_BIG = new SettingInt("FONT_SIZE_BIG", Skin, EXPERT, 16, Global, ACB);
    SettingInt FONT_SIZE_NORMAL = new SettingInt("FONT_SIZE_NORMAL", Skin, EXPERT, 14, Global, ACB);
    SettingInt FONT_SIZE_NORMAL_BUBBLE = new SettingInt("FONT_SIZE_NORMAL_BUBBLE", Skin, EXPERT, 13, Global, ACB);
    SettingInt FONT_SIZE_SMALL = new SettingInt("FONT_SIZE_SMALL", Skin, EXPERT, 12, Global, ACB);
    SettingInt FONT_SIZE_SMALL_BUBBLE = new SettingInt("FONT_SIZE_SMALL_BUBBLE", Skin, EXPERT, 10, Global, ACB);

    SettingDouble MapViewFontFaktor = new SettingDouble("MapViewFontFaktor", Map, NEVER, 1.0, Global, ACB);

    SettingInt LongClickTime = new SettingInt("LongClicktime", Misc, EXPERT, 600, Global, ACB);
    SettingsAudio GlobalVolume = new SettingsAudio("GlobalVolume", Sounds, NORMAL, new Audio("data/sound/Approach.ogg", false, false, 1.0f), Global, ACB);

    SettingFloat MapViewDPIFaktor = new SettingFloat("MapViewDPIFaktor", Map, EXPERT, displayDensity, Global, ACB);
    SettingFloat MapViewTextFaktor = new SettingFloat("MapViewTextFaktor", Map, EXPERT, 2f, Global, ACB);

    SettingFolder ImageCacheFolder = new SettingFolder("ImageCacheFolder", Folder, NEVER, Config_Core.mWorkPath + "/repository/cache", Local, ACB, true);


    SettingColor LiveMapBackgroundColor = new SettingColor("LiveMapBackgroundColor", LiveMap, NORMAL, new HSV_Color(0.8f, 0.8f, 1f, 1f), Global, ACB);

    SettingBool SettingsShowExpert = new SettingBool("SettingsShowExpert", Internal, NEVER, false, Global, ACB);
    SettingBool SettingsShowAll = new SettingBool("SettingsShowAll", Internal, NEVER, false, Global, ACB);

    SettingFolder LanguagePath = new SettingFolder("LanguagePath", Folder, NEVER, "data/lang", Global, ALL, true);

}
