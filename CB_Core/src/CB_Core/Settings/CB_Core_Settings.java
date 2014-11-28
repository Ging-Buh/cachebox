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
package CB_Core.Settings;

import CB_Core.Enums.Live_Cache_Time;
import CB_Utils.Config_Core;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingDouble;
import CB_Utils.Settings.SettingEncryptedString;
import CB_Utils.Settings.SettingEnum;
import CB_Utils.Settings.SettingFile;
import CB_Utils.Settings.SettingFolder;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingString;
import CB_Utils.Settings.SettingUsage;
import CB_Utils.Settings.SettingsList;

public interface CB_Core_Settings
{

	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	public static final SettingModus INVISIBLE = SettingModus.Invisible;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;

	public static final SettingFile Sel_LanguagePath = (SettingFile) SettingsList.addSetting(new SettingFile("Sel_LanguagePath", SettingCategory.Folder, NEVER, "data/lang/en-GB/strings.ini", SettingStoreType.Platform, SettingUsage.ALL, "lan"));
	public static final SettingFolder LanguagePath = new SettingFolder("LanguagePath", SettingCategory.Folder, NEVER, "data/lang", SettingStoreType.Global, SettingUsage.ALL);
	public static final SettingString GcLogin = new SettingString("GcLogin", SettingCategory.Login, NORMAL, "", SettingStoreType.Platform, SettingUsage.ALL);
	public static final SettingEncryptedString GcAPI = new SettingEncryptedString("GcAPI", SettingCategory.Login, INVISIBLE, "", SettingStoreType.Platform, SettingUsage.ALL);
	public static final SettingEncryptedString GcAPIStaging = new SettingEncryptedString("GcAPIStaging", SettingCategory.Login, INVISIBLE, "", SettingStoreType.Platform, SettingUsage.ALL);
	public static final SettingBool StagingAPI = new SettingBool("StagingAPI", SettingCategory.Folder, EXPERT, false, SettingStoreType.Global, SettingUsage.ALL);

	// Folder Settings
	public static final SettingFolder DescriptionImageFolder = new SettingFolder("DescriptionImageFolder", SettingCategory.Folder, EXPERT, Config_Core.WorkPath + "/repository/images", SettingStoreType.Global, SettingUsage.ALL);
	public static final SettingFolder DescriptionImageFolderLocal = new SettingFolder("DescriptionImageFolder", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL);
	public static final SettingFolder SpoilerFolder = new SettingFolder("SpoilerFolder", SettingCategory.Folder, EXPERT, Config_Core.WorkPath + "/repository/spoilers", SettingStoreType.Global, SettingUsage.ALL);
	public static final SettingFolder SpoilerFolderLocal = new SettingFolder("SpoilerFolder", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL);
	public static final SettingInt conection_timeout = new SettingInt("conection_timeout", SettingCategory.Internal, INVISIBLE, 10000, SettingStoreType.Global, SettingUsage.ALL);
	public static final SettingInt socket_timeout = new SettingInt("socket_timeout", SettingCategory.Internal, INVISIBLE, 60000, SettingStoreType.Global, SettingUsage.ALL);
	public static final SettingEncryptedString GcVotePassword = new SettingEncryptedString("GcVotePassword", SettingCategory.Login, NORMAL, "", SettingStoreType.Platform, SettingUsage.ALL);
	public static final SettingDouble ParkingLatitude = new SettingDouble("ParkingLatitude", SettingCategory.Positions, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingDouble ParkingLongitude = new SettingDouble("ParkingLongitude", SettingCategory.Positions, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingFolder UserImageFolder = new SettingFolder("UserImageFolder", SettingCategory.Folder, NORMAL, Config_Core.WorkPath + "/User/Media", SettingStoreType.Global, SettingUsage.ALL);
	public static final SettingBool FieldNotesLoadAll = new SettingBool("FieldNotesLoadAll", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingInt FieldNotesLoadLength = new SettingInt("FieldNotesLoadLength", SettingCategory.Fieldnotes, EXPERT, 10, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingString Friends = (SettingString) SettingsList.addSetting(new SettingString("Friends", SettingCategory.Login, EXPERT, "", SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingFolder PocketQueryFolder = new SettingFolder("PocketQueryFolder", SettingCategory.Folder, INVISIBLE, Config_Core.WorkPath + "/PocketQuery", SettingStoreType.Global, SettingUsage.ALL);
	public static final SettingBool ShowAllWaypoints = new SettingBool("ShowAllWaypoints", SettingCategory.Map, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius> LiveRadius = new SettingEnum<CB_Core.Api.LiveMapQue.Live_Radius>("LiveRadius", SettingCategory.LiveMap, NORMAL, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14, SettingStoreType.Global, SettingUsage.ACB, CB_Core.Api.LiveMapQue.Live_Radius.Zoom_14);
	public static final SettingBool DisableLiveMap = new SettingBool("DisableLiveMap", SettingCategory.LiveMap, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingInt LiveMaxCount = new SettingInt("LiveMaxCount", SettingCategory.LiveMap, EXPERT, 350, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool LiveExcludeFounds = new SettingBool("LiveExcludeFounds", SettingCategory.LiveMap, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool LiveExcludeOwn = new SettingBool("LiveExcludeOwn", SettingCategory.LiveMap, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingEnum<Live_Cache_Time> LiveCacheTime = new SettingEnum<Live_Cache_Time>("LiveCacheTime", SettingCategory.LiveMap, NORMAL, Live_Cache_Time.h_6, SettingStoreType.Global, SettingUsage.ACB, Live_Cache_Time.h_6);

}
