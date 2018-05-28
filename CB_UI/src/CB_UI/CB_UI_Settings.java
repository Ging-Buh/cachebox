/* 
 * Copyright (C) 2014 team-cachebox.de
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

package CB_UI;

import CB_Locator.LocatorSettings;
import CB_Utils.Settings.Audio;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingFile;
import CB_Utils.Settings.SettingFolder;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingIntArray;
import CB_Utils.Settings.SettingLongString;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingString;
import CB_Utils.Settings.SettingStringArray;
import CB_Utils.Settings.SettingUsage;
import CB_Utils.Settings.SettingsAudio;
import CB_Utils.Settings.SettingsList;

/**
 * @author Longri
 * @author arbor95
 */

public interface CB_UI_Settings {
	public static final String Work = Config.mWorkPath;

	// Abkürzende Schreibweisen für die übersichlichkeit bei den add Methoden
	public static final SettingModus DEVELOPER = SettingModus.DEVELOPER;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;

	public static final String FOUND = "<br>###finds##, ##time##, Found it with Cachebox!";
	public static final String ATTENDED = "<br>###finds##, ##time##, Have been there!";
	public static final String WEBCAM = "<br>###finds##, ##time##, Photo taken!";
	public static final String DNF = "<br>##time##. Could not find the cache!";
	public static final String LOG = "Logged it with Cachebox!";
	public static final String DISCOVERD = "<br> ##time##, Discovered it with Cachebox!";
	public static final String VISITED = "<br> ##time##, Visited it with Cachebox!";
	public static final String DROPPED = "<br> ##time##, Dropped off with Cachebox!";
	public static final String PICKED = "<br> ##time##, Picked it with Cachebox!";
	public static final String GRABED = "<br> ##time##, Grabed it with Cachebox!";

	public static final Integer[] approach = new Integer[] { 0, 2, 10, 25, 50, 100, 200, 500, 1000 };
	public static final Integer[] TrackDistanceArray = new Integer[] { 1, 3, 5, 10, 20 };
	public static final String[] navis = new String[] { "Google", "OsmAnd", "OsmAnd2", "Waze", "Orux", "Sygic", "Navigon" };

	// Settings Compass
	public static final SettingInt HardwareCompassLevel = (SettingInt) SettingsList.addSetting(new SettingInt("HardwareCompassLevel", SettingCategory.Gps, NORMAL, 5, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingBool HardwareCompass = new SettingBool("HardwareCompass", SettingCategory.Gps, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingInt gpsUpdateTime = (SettingInt) SettingsList.addSetting(new SettingInt("gpsUpdateTime", SettingCategory.Gps, NORMAL, 500, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingBool CompassShowMap = new SettingBool("CompassShowMap", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompassShowWP_Name = new SettingBool("CompassShowWP_Name", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompassShowWP_Icon = new SettingBool("CompassShowWP_Icon", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompassShowAttributes = new SettingBool("CompassShowAttributes", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompassShowGcCode = new SettingBool("CompassShowGcCode", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompassShowCoords = new SettingBool("CompassShowCoords", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompassShowWpDesc = new SettingBool("CompassShowWpDesc", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompassShowSatInfos = new SettingBool("CompassShowSatInfos", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompassShowSunMoon = new SettingBool("CompassShowSunMoon", SettingCategory.Compass, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompassShowTargetDirection = new SettingBool("CompassShowTargetDirection", SettingCategory.Compass, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompassShowSDT = new SettingBool("CompassShowSDT", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompassShowLastFound = new SettingBool("CompassShowLastFound", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingString OverrideUrl = (SettingString) SettingsList.addSetting(new SettingString("OverrideUrl", SettingCategory.Login, DEVELOPER, "", SettingStoreType.Global, SettingUsage.ACB));
	// Folder
	public static final SettingFolder TrackFolder = new SettingFolder("TrackFolder", SettingCategory.Folder, EXPERT, Work + "/User/Tracks", SettingStoreType.Global, SettingUsage.ACB, true);
	// Files
	// public static final SettingFile DatabasePath = (SettingFile) SettingsList.addSetting(new SettingFile("DatabasePath", SettingCategory.Folder, NEVER, Work + "/cachebox.db3", SettingStoreType.Global, SettingUsage.ACB, "db3"));
	public static final SettingString DatabaseName = new SettingString("DatabaseName", SettingCategory.Internal, NEVER, "cachebox.db3", SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingFile FieldNotesGarminPath = (SettingFile) SettingsList
			.addSetting(new SettingFile("FieldNotesGarminPath", SettingCategory.Folder, DEVELOPER, Work + "/User/geocache_visits.txt", SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingFile gpxExportFileName = new SettingFile("gpxExportFileName", SettingCategory.Folder, NEVER, Work + "/User/export.gpx", SettingStoreType.Global, SettingUsage.ACB, "gpx");
	//
	public static final SettingBool MapShowRating = new SettingBool("MapShowRating", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool MapShowDT = new SettingBool("MapShowDT", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool MapShowTitles = new SettingBool("MapShowTitles", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool TrackRecorderStartup = new SettingBool("TrackRecorderStartup", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool MapShowCompass = new SettingBool("MapShowCompass", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompassNorthOriented = new SettingBool("CompassNorthOriented", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool MapNorthOriented = new SettingBool("MapNorthOriented", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool ImportGpx = new SettingBool("ImportGpx", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CacheMapData = new SettingBool("CacheMapData", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CacheImageData = new SettingBool("CacheImageData", SettingCategory.Internal, DEVELOPER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CacheSpoilerData = new SettingBool("CacheSpoilerData", SettingCategory.Internal, DEVELOPER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool SuppressPowerSaving = new SettingBool("SuppressPowerSaving", SettingCategory.Misc, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool GCAdditionalImageDownload = new SettingBool("GCAdditionalImageDownload", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool StartWithAutoSelect = new SettingBool("StartWithAutoSelect", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool FieldnotesUploadAll = new SettingBool("FieldnotesUploadAll", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool MultiDBAsk = new SettingBool("MultiDBAsk", SettingCategory.Internal, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool SearchWithoutFounds = new SettingBool("SearchWithoutFounds", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool SearchWithoutOwns = new SettingBool("SearchWithoutOwns", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool SearchOnlyAvailable = new SettingBool("SearchOnlyAvailable", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool quickButtonShow = new SettingBool("quickButtonShow", SettingCategory.QuickList, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool DescriptionNoAttributes = new SettingBool("DescriptionNoAttributes", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool quickButtonLastShow = new SettingBool("quickButtonLastShow", SettingCategory.QuickList, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool newInstall = new SettingBool("newInstall", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool ImperialUnits = new SettingBool("ImperialUnits", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool ShowDirektLine = new SettingBool("ShowDirektLine", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool ImportRatings = new SettingBool("ImportRatings", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool ImportPQsFromGeocachingCom = new SettingBool("ImportPQsFromGeocachingCom", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool switchViewApproach = new SettingBool("switchViewApproach", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool hasCallPermission = new SettingBool("hasCallPermission", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool vibrateFeedback = new SettingBool("vibrateFeedback", SettingCategory.Misc, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool hasPQ_PlugIn = new SettingBool("hasPQ_PlugIn", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool hasFTF_PlugIn = new SettingBool("hasFTF_PlugIn", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool dynamicZoom = new SettingBool("dynamicZoom", SettingCategory.CarMode, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool dynamicFilterAtSearch = new SettingBool("dynamicFilterAtSearch", SettingCategory.Misc, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool DeleteLogs = new SettingBool("DeleteLogs", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool CompactDB = new SettingBool("CompactDB", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool AskAgain = new SettingBool("AskAgain", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Platform, SettingUsage.ALL);
	public static final SettingBool RememberAsk_Get_API_Key = new SettingBool("RememberAsk_Get_API_Key", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool Ask_Switch_GPS_ON = new SettingBool("Ask_Switch_GPS_ON", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Platform, SettingUsage.ALL);
	public static final SettingBool TB_DirectLog = new SettingBool("TB_DirectLog", SettingCategory.Internal, NEVER, true, SettingStoreType.Platform, SettingUsage.ALL);
	public static final SettingBool MapHideMyFinds = new SettingBool("MapHideMyFinds", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
	// int
	public static final SettingInt LogMaxMonthAge = (SettingInt) SettingsList.addSetting(new SettingInt("LogMaxMonthAge", SettingCategory.Internal, DEVELOPER, 6, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingInt LogMinCount = (SettingInt) SettingsList.addSetting(new SettingInt("LogMinCount", SettingCategory.Internal, DEVELOPER, 99999, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingInt installedRev = (SettingInt) SettingsList.addSetting(new SettingInt("installRev", SettingCategory.Misc, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingInt MapIniWidth = (SettingInt) SettingsList.addSetting(new SettingInt("MapIniWidth", SettingCategory.Map, NEVER, 480, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingInt MapIniHeight = (SettingInt) SettingsList.addSetting(new SettingInt("MapIniHeight", SettingCategory.Map, NEVER, 535, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingInt VibrateTime = (SettingInt) SettingsList.addSetting(new SettingInt("VibrateTime", SettingCategory.Misc, EXPERT, 20, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingInt FoundOffset = (SettingInt) SettingsList.addSetting(new SettingInt("FoundOffset", SettingCategory.Misc, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingInt MultiDBAutoStartTime = (SettingInt) SettingsList.addSetting(new SettingInt("MultiDBAutoStartTime", SettingCategory.Internal, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingInt lastSearchRadius = (SettingInt) SettingsList.addSetting(new SettingInt("lastSearchRadius", SettingCategory.API, NEVER, 5, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingInt LastMapToggleBtnState = (SettingInt) SettingsList.addSetting(new SettingInt("LastMapToggleBtnState", SettingCategory.Map, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingInt dynamicZoomLevelMax = (SettingInt) SettingsList.addSetting(new SettingInt("dynamicZoomLevelMax", SettingCategory.CarMode, NORMAL, 17, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingInt dynamicZoomLevelMin = (SettingInt) SettingsList.addSetting(new SettingInt("dynamicZoomLevelMin", SettingCategory.CarMode, NORMAL, 15, SettingStoreType.Global, SettingUsage.ACB));
	// String
	public static final SettingString LastSelectedCache = (SettingString) SettingsList.addSetting(new SettingString("LastSelectedCache", SettingCategory.Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL));
	public static final SettingString CacheHistory = (SettingString) SettingsList.addSetting(new SettingString("CacheHistory", SettingCategory.Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL));
	public static final SettingString NavigationProvider = (SettingString) SettingsList
			.addSetting(new SettingString("NavigationProvider", SettingCategory.Internal, DEVELOPER, "http://openls.geog.uni-heidelberg.de/testing2015/route?", SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString FoundTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("FoundTemplate", SettingCategory.Templates, NORMAL, FOUND, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString AttendedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("AttendedTemplate", SettingCategory.Templates, NORMAL, ATTENDED, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString WebcamTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("WebCamTemplate", SettingCategory.Templates, NORMAL, WEBCAM, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString DNFTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("DNFTemplate", SettingCategory.Templates, NORMAL, DNF, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString NeedsMaintenanceTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("NeedsMaintenanceTemplate", SettingCategory.Templates, NORMAL, LOG, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString AddNoteTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("AddNoteTemplate", SettingCategory.Templates, NORMAL, LOG, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString DiscoverdTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("DiscoverdTemplate", SettingCategory.Templates, NORMAL, DISCOVERD, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString VisitedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("VisitedTemplate", SettingCategory.Templates, NORMAL, VISITED, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString DroppedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("DroppedTemplate", SettingCategory.Templates, NORMAL, DROPPED, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString GrabbedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("GrabbedTemplate", SettingCategory.Templates, NORMAL, GRABED, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString PickedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("PickedTemplate", SettingCategory.Templates, NORMAL, PICKED, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString SpoilersDescriptionTags = (SettingString) SettingsList.addSetting(new SettingString("SpoilersDescriptionTags", SettingCategory.Internal, DEVELOPER, "", SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString quickButtonList = (SettingString) SettingsList.addSetting(new SettingString("quickButtonList", SettingCategory.QuickList, DEVELOPER, "5,0,1,3,2", SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString GcJoker = new SettingString("GcJoker", SettingCategory.Login, NORMAL, "", SettingStoreType.Platform, SettingUsage.ALL);
	public static final SettingStringArray Navis = (SettingStringArray) SettingsList.addSetting(new SettingStringArray("Navis", SettingCategory.Misc, NORMAL, "Google", SettingStoreType.Global, SettingUsage.ACB, navis));

	// ArrayInt
	public static final SettingIntArray ZoomCross = new SettingIntArray("ZoomCross", SettingCategory.Map, NORMAL, 16, SettingStoreType.Global, SettingUsage.ACB, LocatorSettings.CrossLevel);
	public static final SettingIntArray SoundApproachDistance = new SettingIntArray("SoundApproachDistance", SettingCategory.Misc, NORMAL, 50, SettingStoreType.Global, SettingUsage.ACB, approach);
	public static final SettingIntArray TrackDistance = new SettingIntArray("TrackDistance", SettingCategory.Misc, NORMAL, 3, SettingStoreType.Global, SettingUsage.ACB, TrackDistanceArray);

	// double

	// longString
	//	public static final SettingLongString Filter = new SettingLongString("Filter", SettingCategory.Misc, NEVER, FilterProperties.presets[0].toString(), SettingStoreType.Local, SettingUsage.ALL);
	public static final SettingLongString FilterNew = new SettingLongString("FilterNew", SettingCategory.Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL);
	public static final SettingLongString UserFilter = new SettingLongString("UserFilter", SettingCategory.Misc, NEVER, "", SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingLongString UserFilterNew = new SettingLongString("UserFilterNew", SettingCategory.Misc, NEVER, "", SettingStoreType.Global, SettingUsage.ACB);

	// AudioSettings

	public static final SettingsAudio Approach = new SettingsAudio("Approach", SettingCategory.Sounds, EXPERT, new Audio("data/sound/Approach.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingsAudio GPS_lose = new SettingsAudio("GPS_lose", SettingCategory.Sounds, EXPERT, new Audio("data/sound/GPS_lose.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingsAudio GPS_fix = new SettingsAudio("GPS_fix", SettingCategory.Sounds, EXPERT, new Audio("data/sound/GPS_Fix.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingsAudio AutoResortSound = new SettingsAudio("AutoResortSound", SettingCategory.Sounds, EXPERT, new Audio("data/sound/AutoResort.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool ShowFieldnotesCMwithFirstShow = new SettingBool("ShowFieldnotesCMwithFirstShow", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool ShowFieldnotesAsDefaultView = new SettingBool("ShowFieldnotesAsDefaultView", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool LiveMapEnabeld = new SettingBool("LiveMapEnabeld", SettingCategory.LiveMap, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingBool AppRaterDontShowAgain = new SettingBool("AppRaterDontShowAgain", SettingCategory.RememberAsk, NORMAL, false, SettingStoreType.Platform, SettingUsage.ACB);
	public static final SettingInt AppRaterlaunchCount = new SettingInt("AppRaterlaunchCount", SettingCategory.Internal, NEVER, 0, SettingStoreType.Platform, SettingUsage.ACB);
	public static final SettingString AppRaterFirstLunch = new SettingString("AppRaterFirstLunch", SettingCategory.Internal, NEVER, "0", SettingStoreType.Platform, SettingUsage.ACB);

}
