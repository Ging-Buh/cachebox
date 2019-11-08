/*
 * Copyright (C) 2011-2014 team-cachebox.de
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
package de.droidcachebox.core;

import de.droidcachebox.settings.*;
import de.droidcachebox.utils.Config_Core;

import static de.droidcachebox.settings.SettingCategory.*;
import static de.droidcachebox.settings.SettingModus.*;
import static de.droidcachebox.settings.SettingStoreType.Global;
import static de.droidcachebox.settings.SettingStoreType.Platform;
import static de.droidcachebox.settings.SettingUsage.ACB;
import static de.droidcachebox.settings.SettingUsage.ALL;

public interface CB_Core_Settings {
    Integer[] numberOfLogsArray = new Integer[]{0, 5, 30};

    SettingEncryptedString AccessToken = new SettingEncryptedString("GcAPI", Login, DEVELOPER, "", Global, ALL);
    SettingString GcLogin = new SettingString("GcLogin", Login, DEVELOPER, "", Global, ALL);
    SettingBool UseTestUrl = new SettingBool("StagingAPI", Folder, DEVELOPER, false, Global, ALL);
    SettingEncryptedString AccessTokenForTest = new SettingEncryptedString("GcAPIStaging", Login, DEVELOPER, "", Global, ALL);
    SettingString Friends = new SettingString("Friends", Login, NORMAL, "", Global, ALL);
    SettingEncryptedString GcVotePassword = new SettingEncryptedString("GcVotePassword", Login, NORMAL, "", Global, ALL);

    SettingFolder PocketQueryFolder = new SettingFolder("PocketQueryFolder", Folder, DEVELOPER, Config_Core.mWorkPath + "/PocketQuery", Global, ALL, true);
    SettingFolder DescriptionImageFolder = new SettingFolder("DescriptionImageFolder", Folder, NEVER, Config_Core.mWorkPath + "/repository/images", Global, ALL, true);
    SettingFolder SpoilerFolder = new SettingFolder("SpoilerFolder", Folder, NEVER, Config_Core.mWorkPath + "/repository/spoilers", Global, ALL, true);
    SettingFolder DescriptionImageFolderLocal = new SettingFolder("DescriptionImageFolderLocal", Folder, NEVER, "", SettingStoreType.Local, ALL, true);
    SettingFolder SpoilerFolderLocal = new SettingFolder("SpoilerFolderLocal", Folder, NEVER, "", SettingStoreType.Local, ALL, true);
    SettingFolder UserImageFolder = new SettingFolder("UserImageFolder", Folder, NORMAL, Config_Core.mWorkPath + "/User/Media", Global, ALL, true);

    SettingInt connection_timeout = new SettingInt("conection_timeout", Internal, DEVELOPER, 10000, Global, ALL);
    SettingInt socket_timeout = new SettingInt("socket_timeout", Internal, DEVELOPER, 60000, Global, ALL);

    SettingIntArray numberOfLogs = new SettingIntArray("NumberOfLogs", Misc, NORMAL, 5, Global, ACB, numberOfLogsArray);

    SettingDouble ParkingLatitude = new SettingDouble("ParkingLatitude", Positions, NEVER, 0, Global, ACB);
    SettingDouble ParkingLongitude = new SettingDouble("ParkingLongitude", Positions, NEVER, 0, Global, ACB);

    SettingBool DirectOnlineLog = new SettingBool("DirectOnlineLog", Drafts, NORMAL, true, Global, ACB, false);
    SettingBool DraftsLoadAll = new SettingBool("DraftsLoadAll", Drafts, DEVELOPER, false, Global, ACB, false);
    SettingInt DraftsLoadLength = new SettingInt("DraftsLoadLength", Drafts, DEVELOPER, 10, Global, ACB);

    SettingEnum<LiveMapQue.Live_Radius> LiveRadius = new SettingEnum<>("LiveRadius", LiveMap, NORMAL, LiveMapQue.Live_Radius.Zoom_14, Global, ACB, LiveMapQue.Live_Radius.Zoom_14);
    SettingBool DisableLiveMap = new SettingBool("DisableLiveMap", LiveMap, NORMAL, false, Global, ACB);
    SettingInt LiveMaxCount = new SettingInt("LiveMaxCount", LiveMap, EXPERT, 350, Global, ACB);
    SettingBool LiveExcludeFounds = new SettingBool("LiveExcludeFounds", LiveMap, NORMAL, true, Global, ACB);
    SettingBool LiveExcludeOwn = new SettingBool("LiveExcludeOwn", LiveMap, NORMAL, true, Global, ACB);
    SettingEnum<Live_Cache_Time> LiveCacheTime = new SettingEnum<>("LiveCacheTime", LiveMap, NORMAL, Live_Cache_Time.h_6, Global, ACB, Live_Cache_Time.h_6);

    SettingBool UseCorrectedFinal = new SettingBool("UseCorrectedFinal", Misc, NORMAL, true, Global, ALL);
    SettingBool RunOverLockScreen = new SettingBool("RunOverLockScreen", Misc, NORMAL, true, Global, ACB);

    // base settings, read directly from Platform, before the database can be accessed
    SettingBool AskAgain = new SettingBool("AskAgain", Folder, NORMAL, false, Platform, ALL);
    SettingBool showSandbox = new SettingBool("showSandbox", RememberAsk, NORMAL, false, Platform, ACB);
    SettingFile Sel_LanguagePath = new SettingFile("Sel_LanguagePath", Folder, NEVER, "data/lang/en-GB/strings.ini", Platform, ALL, "lan");
    enum Live_Cache_Time {
        min_10, min_30, min_60, h_6, h_12, h_24;

        public int getMinuten() {
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
