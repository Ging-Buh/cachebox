/*
 * Copyright (C) 2011-2020 team-cachebox.de
 *
 * Licensed under the : GNU General License (GPL);
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

import static de.droidcachebox.settings.SettingCategory.Drafts;
import static de.droidcachebox.settings.SettingCategory.Folder;
import static de.droidcachebox.settings.SettingCategory.Internal;
import static de.droidcachebox.settings.SettingCategory.LiveMap;
import static de.droidcachebox.settings.SettingCategory.Login;
import static de.droidcachebox.settings.SettingCategory.Misc;
import static de.droidcachebox.settings.SettingCategory.Positions;
import static de.droidcachebox.settings.SettingCategory.RememberAsk;
import static de.droidcachebox.settings.SettingModus.DEVELOPER;
import static de.droidcachebox.settings.SettingModus.EXPERT;
import static de.droidcachebox.settings.SettingModus.NEVER;
import static de.droidcachebox.settings.SettingModus.NORMAL;
import static de.droidcachebox.settings.SettingStoreType.Global;
import static de.droidcachebox.settings.SettingStoreType.Platform;
import static de.droidcachebox.settings.SettingUsage.ACB;
import static de.droidcachebox.settings.SettingUsage.ALL;

import de.droidcachebox.core.LiveMapQue;

public interface CB_Core_Settings {
    Integer[] numberOfLogsArray = new Integer[]{0, 5, 30};

    SettingEncryptedString AccessToken = new SettingEncryptedString("GcAPI", Login, DEVELOPER, "", Global, ALL);
    SettingString GcLogin = new SettingString("GcLogin", Login, DEVELOPER, "", Global, ALL);
    SettingBool UseTestUrl = new SettingBool("StagingAPI", Folder, DEVELOPER, false, Global, ALL);
    SettingEncryptedString AccessTokenForTest = new SettingEncryptedString("GcAPIStaging", Login, DEVELOPER, "", Global, ALL);
    SettingString friends = new SettingString("Friends", Login, NORMAL, "", Global, ALL);
    SettingEncryptedString GcVotePassword = new SettingEncryptedString("GcVotePassword", Login, NORMAL, "", Global, ALL);

    SettingFolder PocketQueryFolder = new SettingFolder("PocketQueryFolder", Folder, DEVELOPER, Config_Core.workPath + "/PocketQuery", Global, ALL, true);
    SettingFolder DescriptionImageFolder = new SettingFolder("DescriptionImageFolder", Folder, NEVER, Config_Core.workPath + "/repository/images", Global, ALL, true);
    SettingFolder SpoilerFolder = new SettingFolder("SpoilerFolder", Folder, NEVER, Config_Core.workPath + "/repository/spoilers", Global, ALL, true);
    SettingFolder DescriptionImageFolderLocal = new SettingFolder("DescriptionImageFolderLocal", Folder, NEVER, "", SettingStoreType.Local, ALL, true);
    SettingFolder SpoilerFolderLocal = new SettingFolder("SpoilerFolderLocal", Folder, NEVER, "", SettingStoreType.Local, ALL, true);
    SettingFolder UserImageFolder = new SettingFolder("UserImageFolder", Folder, NORMAL, Config_Core.workPath + "/User/Media", Global, ALL, true);

    SettingInt connection_timeout = new SettingInt("conection_timeout", Internal, DEVELOPER, 10000, Global, ALL);
    SettingInt socket_timeout = new SettingInt("socket_timeout", Internal, DEVELOPER, 60000, Global, ALL);

    SettingIntArray numberOfLogs = new SettingIntArray("NumberOfLogs", Misc, NORMAL, 5, Global, ACB, numberOfLogsArray);

    SettingDouble ParkingLatitude = new SettingDouble("ParkingLatitude", Positions, NEVER, 0, Global, ACB);
    SettingDouble ParkingLongitude = new SettingDouble("ParkingLongitude", Positions, NEVER, 0, Global, ACB);

    SettingBool DraftsLoadAll = new SettingBool("DraftsLoadAll", Drafts, DEVELOPER, false, Global, ACB, false);
    SettingInt DraftsLoadLength = new SettingInt("DraftsLoadLength", Drafts, DEVELOPER, 10, Global, ACB);

    SettingBool disableLiveMap = new SettingBool("DisableLiveMap", LiveMap, NEVER, false, Global, ACB);
    SettingEnum<LiveMapQue.Live_Radius> liveRadius = new SettingEnum<>("LiveRadius", LiveMap, NORMAL, LiveMapQue.Live_Radius.Zoom_14, Global, ACB, LiveMapQue.Live_Radius.Zoom_14);
    SettingInt liveMaxCount = new SettingInt("LiveMaxCount", LiveMap, EXPERT, 350, Global, ACB);
    SettingBool liveExcludeFounds = new SettingBool("LiveExcludeFounds", LiveMap, NORMAL, true, Global, ACB);
    SettingBool liveExcludeOwn = new SettingBool("LiveExcludeOwn", LiveMap, NORMAL, true, Global, ACB);
    SettingEnum<LiveCacheTime> liveCacheTime = new SettingEnum<>("LiveCacheTime", LiveMap, NORMAL, LiveCacheTime.h_6, Global, ACB, LiveCacheTime.h_6);

    SettingBool UseCorrectedFinal = new SettingBool("UseCorrectedFinal", Misc, NORMAL, true, Global, ALL);
    SettingBool RunOverLockScreen = new SettingBool("RunOverLockScreen", Misc, NORMAL, true, Global, ACB);
    SettingString rememberedGeoCache = new SettingString("rememberedGeoCache", Misc, NEVER, "", Global, ACB);

    // base settings, read directly from Platform, before the database can be accessed
    SettingBool AskAgain = new SettingBool("AskAgain", Folder, NORMAL, false, Platform, ALL);
    SettingBool showSandbox = new SettingBool("showSandbox", RememberAsk, NORMAL, false, Platform, ACB);
    SettingFile Sel_LanguagePath = new SettingFile("Sel_LanguagePath", Folder, NEVER, "data/lang/en-GB/strings.ini", Platform, ALL, "lan");

    enum LiveCacheTime {
        min_10, min_30, min_60, h_6, h_12, h_24;

        public int getLifetime() {
            switch (this) {
                case h_24:
                    return 1440;
                case h_12:
                    return 720;
                case h_6:
                    return 360;
                case min_10:
                    return 10;
                case min_30:
                    return 30;
                case min_60:
                    return 60;
                default:
                    return 1440;

            }
        }
    }
}
