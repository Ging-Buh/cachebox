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
package CB_Core;

import CB_Core.Api.LiveMapQue;
import CB_Utils.Config_Core;
import CB_Utils.Settings.*;


public interface CB_Core_Settings {

    // Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
    SettingModus DEVELOPER = SettingModus.DEVELOPER;
    SettingModus NORMAL = SettingModus.Normal;
    SettingModus EXPERT = SettingModus.Expert;
    SettingModus NEVER = SettingModus.Never;

    SettingEncryptedString AccessToken = new SettingEncryptedString("GcAPI", SettingCategory.Login, DEVELOPER, "", SettingStoreType.Global, SettingUsage.ALL);
    SettingString GcLogin = new SettingString("GcLogin", SettingCategory.Login, DEVELOPER, "", SettingStoreType.Global, SettingUsage.ALL);
    SettingBool UseTestUrl = new SettingBool("StagingAPI", SettingCategory.Folder, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ALL);
    SettingEncryptedString AccessTokenForTest = new SettingEncryptedString("GcAPIStaging", SettingCategory.Login, DEVELOPER, "", SettingStoreType.Global, SettingUsage.ALL);
    SettingString Friends = new SettingString("Friends", SettingCategory.Login, NORMAL, "", SettingStoreType.Global, SettingUsage.ALL);
    SettingEncryptedString GcVotePassword = new SettingEncryptedString("GcVotePassword", SettingCategory.Login, NORMAL, "", SettingStoreType.Global, SettingUsage.ALL);

    SettingFolder PocketQueryFolder = new SettingFolder("PocketQueryFolder", SettingCategory.Folder, DEVELOPER, Config_Core.mWorkPath + "/PocketQuery", SettingStoreType.Global, SettingUsage.ALL, true);
    SettingFolder DescriptionImageFolder = new SettingFolder("DescriptionImageFolder", SettingCategory.Folder, NEVER, Config_Core.mWorkPath + "/repository/images", SettingStoreType.Global, SettingUsage.ALL, true);
    SettingFolder SpoilerFolder = new SettingFolder("SpoilerFolder", SettingCategory.Folder, NEVER, Config_Core.mWorkPath + "/repository/spoilers", SettingStoreType.Global, SettingUsage.ALL, true);
    SettingFolder DescriptionImageFolderLocal = new SettingFolder("DescriptionImageFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true);
    SettingFolder SpoilerFolderLocal = new SettingFolder("SpoilerFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true);
    SettingFolder UserImageFolder = new SettingFolder("UserImageFolder", SettingCategory.Folder, NORMAL, Config_Core.mWorkPath + "/User/Media", SettingStoreType.Global, SettingUsage.ALL, true);

    SettingInt connection_timeout = new SettingInt("conection_timeout", SettingCategory.Internal, DEVELOPER, 10000, SettingStoreType.Global, SettingUsage.ALL);
    SettingInt socket_timeout = new SettingInt("socket_timeout", SettingCategory.Internal, DEVELOPER, 60000, SettingStoreType.Global, SettingUsage.ALL);

    SettingDouble ParkingLatitude = new SettingDouble("ParkingLatitude", SettingCategory.Positions, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB);
    SettingDouble ParkingLongitude = new SettingDouble("ParkingLongitude", SettingCategory.Positions, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB);

    SettingBool DirectOnlineLog = new SettingBool("DirectOnlineLog", SettingCategory.Fieldnotes, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool FieldNotesLoadAll = new SettingBool("FieldNotesLoadAll", SettingCategory.Fieldnotes, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingInt FieldNotesLoadLength = new SettingInt("FieldNotesLoadLength", SettingCategory.Fieldnotes, DEVELOPER, 10, SettingStoreType.Global, SettingUsage.ACB);

    SettingEnum<LiveMapQue.Live_Radius> LiveRadius = new SettingEnum<>("LiveRadius", SettingCategory.LiveMap, NORMAL, LiveMapQue.Live_Radius.Zoom_14, SettingStoreType.Global, SettingUsage.ACB,
            LiveMapQue.Live_Radius.Zoom_14);
    SettingBool DisableLiveMap = new SettingBool("DisableLiveMap", SettingCategory.LiveMap, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingInt LiveMaxCount = new SettingInt("LiveMaxCount", SettingCategory.LiveMap, EXPERT, 350, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool LiveExcludeFounds = new SettingBool("LiveExcludeFounds", SettingCategory.LiveMap, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool LiveExcludeOwn = new SettingBool("LiveExcludeOwn", SettingCategory.LiveMap, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingEnum<Live_Cache_Time> LiveCacheTime = new SettingEnum<>("LiveCacheTime", SettingCategory.LiveMap, NORMAL, Live_Cache_Time.h_6, SettingStoreType.Global, SettingUsage.ACB, Live_Cache_Time.h_6);

    SettingBool showSandbox = new SettingBool("showSandbox", SettingCategory.RememberAsk, NORMAL, false, SettingStoreType.Platform, SettingUsage.ACB);

}
