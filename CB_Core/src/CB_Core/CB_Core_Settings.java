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

import static CB_Utils.Settings.SettingCategory.*;
import static CB_Utils.Settings.SettingModus.*;
import static CB_Utils.Settings.SettingStoreType.*;
import static CB_Utils.Settings.SettingUsage.*;

public interface CB_Core_Settings {

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

    SettingDouble ParkingLatitude = new SettingDouble("ParkingLatitude", Positions, NEVER, 0, Global, ACB);
    SettingDouble ParkingLongitude = new SettingDouble("ParkingLongitude", Positions, NEVER, 0, Global, ACB);

    SettingBool DirectOnlineLog = new SettingBool("DirectOnlineLog", Fieldnotes, NORMAL, true, Global, ACB);
    SettingBool FieldNotesLoadAll = new SettingBool("FieldNotesLoadAll", Fieldnotes, DEVELOPER, false, Global, ACB);
    SettingInt FieldNotesLoadLength = new SettingInt("FieldNotesLoadLength", Fieldnotes, DEVELOPER, 10, Global, ACB);

    SettingEnum<LiveMapQue.Live_Radius> LiveRadius = new SettingEnum<>("LiveRadius", LiveMap, NORMAL, LiveMapQue.Live_Radius.Zoom_14, Global, ACB, LiveMapQue.Live_Radius.Zoom_14);
    SettingBool DisableLiveMap = new SettingBool("DisableLiveMap", LiveMap, NORMAL, false, Global, ACB);
    SettingInt LiveMaxCount = new SettingInt("LiveMaxCount", LiveMap, EXPERT, 350, Global, ACB);
    SettingBool LiveExcludeFounds = new SettingBool("LiveExcludeFounds", LiveMap, NORMAL, true, Global, ACB);
    SettingBool LiveExcludeOwn = new SettingBool("LiveExcludeOwn", LiveMap, NORMAL, true, Global, ACB);
    SettingEnum<Live_Cache_Time> LiveCacheTime = new SettingEnum<>("LiveCacheTime", LiveMap, NORMAL, Live_Cache_Time.h_6, Global, ACB, Live_Cache_Time.h_6);

    SettingBool showSandbox = new SettingBool("showSandbox", RememberAsk, NORMAL, false, Platform, ACB);

    SettingBool UseCorrectedFinal = new SettingBool("UseCorrectedFinal", Misc, NORMAL, true, Global, ALL);
    SettingBool RunOverLockScreen = new SettingBool("RunOverLockScreen", Misc, NORMAL, true, Global, ACB);

}
