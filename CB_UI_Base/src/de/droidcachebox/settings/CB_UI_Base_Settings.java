/*
 * Copyright (C) 2011-2020 team-cachebox.de
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
package de.droidcachebox.settings;

import static de.droidcachebox.settings.Config_Core.displayDensity;
import static de.droidcachebox.settings.SettingCategory.Debug;
import static de.droidcachebox.settings.SettingCategory.Folder;
import static de.droidcachebox.settings.SettingCategory.Internal;
import static de.droidcachebox.settings.SettingCategory.LiveMap;
import static de.droidcachebox.settings.SettingCategory.Map;
import static de.droidcachebox.settings.SettingCategory.Misc;
import static de.droidcachebox.settings.SettingCategory.Skin;
import static de.droidcachebox.settings.SettingCategory.Sounds;
import static de.droidcachebox.settings.SettingModus.DEVELOPER;
import static de.droidcachebox.settings.SettingModus.EXPERT;
import static de.droidcachebox.settings.SettingModus.NEVER;
import static de.droidcachebox.settings.SettingModus.NORMAL;
import static de.droidcachebox.settings.SettingStoreType.Global;
import static de.droidcachebox.settings.SettingStoreType.Local;
import static de.droidcachebox.settings.SettingStoreType.Platform;
import static de.droidcachebox.settings.SettingUsage.ACB;
import static de.droidcachebox.settings.SettingUsage.ALL;

import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.utils.log.LogLevel;

public interface CB_UI_Base_Settings {
    SettingEnum<Enum<LogLevel>> AktLogLevel = new SettingEnum<Enum<LogLevel>>("AktLogLevel", Debug, NORMAL, LogLevel.OFF, Platform, ALL, LogLevel.OFF);

    SettingBool nightMode = new SettingBool("nightMode", Internal, NEVER, false, Global, ACB);
    SettingBool useAndroidKeyboard = new SettingBool("useAndroidKeyboard", Skin, NORMAL, false, Global, ACB);

    SettingFolder skinFolder = new SettingFolder("SkinFolder", Folder, DEVELOPER, "default", Global, ACB, false, true);
    SettingFolder imageCacheFolder = new SettingFolder("ImageCacheFolder", Folder, NEVER, Config_Core.workPath + "/repository/cache", Local, ACB, true);

    SettingBool useDescriptiveCB_Buttons = new SettingBool("useDescriptiveCB_Buttons", Skin, EXPERT, true, Global, ACB, true);
    SettingBool rememberLastAction = new SettingBool("rememberLastAction", Skin, EXPERT, true, Global, ACB, true);
    SettingBool gestureOn = new SettingBool("GestureOn", Skin, EXPERT, true, Global, ACB,true);
    SettingBool useMipMap = new SettingBool("useMipMap", Skin, DEVELOPER, false, Global, ACB);
    SettingBool useGrayFader = new SettingBool("useGrayFader", Skin, EXPERT, false, Global, ACB, false);
    SettingInt fadeToGrayAfterXSeconds = new SettingInt("fadeToGrayAfterXSeconds", Skin, EXPERT, 10, Global, ACB);
    SettingColor solvedMysteryColor = new SettingColor("SolvedMysteryColor", Skin, DEVELOPER, new HSV_Color(0.2f, 1f, 0.2f, 1f), Global, ACB);

    SettingInt FONT_SIZE_COMPASS_DISTANCE = new SettingInt("FONT_SIZE_COMPASS_DISTANCE", Skin, EXPERT, 25, Global, ACB);
    SettingInt FONT_SIZE_BIG = new SettingInt("FONT_SIZE_BIG", Skin, EXPERT, 16, Global, ACB);
    SettingInt FONT_SIZE_NORMAL = new SettingInt("FONT_SIZE_NORMAL", Skin, EXPERT, 14, Global, ACB);
    SettingInt FONT_SIZE_NORMAL_BUBBLE = new SettingInt("FONT_SIZE_NORMAL_BUBBLE", Skin, EXPERT, 13, Global, ACB);
    SettingInt FONT_SIZE_SMALL = new SettingInt("FONT_SIZE_SMALL", Skin, EXPERT, 12, Global, ACB);
    SettingInt FONT_SIZE_SMALL_BUBBLE = new SettingInt("FONT_SIZE_SMALL_BUBBLE", Skin, EXPERT, 10, Global, ACB);

    SettingInt longClickTime = new SettingInt("LongClicktime", Misc, EXPERT, 600, Global, ACB);
    SettingsAudio globalVolume = new SettingsAudio("GlobalVolume", Sounds, NORMAL, new Audio("data/sound/Approach.ogg", false, false, 1.0f), Global, ACB);

    SettingFloat mapViewDPIFaktor = new SettingFloat("MapViewDPIFaktor", Map, EXPERT, displayDensity, Global, ACB);
    SettingFloat mapViewTextFaktor = new SettingFloat("MapViewTextFaktor", Map, EXPERT, 2f, Global, ACB);

    SettingColor liveMapBackgroundColor = new SettingColor("LiveMapBackgroundColor", LiveMap, NORMAL, new HSV_Color(0.8f, 0.8f, 1f, 1f), Global, ACB);

    SettingBool isExpert = new SettingBool("SettingsShowExpert", Internal, NEVER, false, Global, ACB);
    SettingBool isDeveloper = new SettingBool("SettingsShowAll", Internal, NEVER, false, Global, ACB);

    SettingFolder languagePath = new SettingFolder("LanguagePath", Folder, NEVER, "data/lang", Global, ALL, true);

}
