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
import CB_Utils.Settings.*;

/**
 * @author Longri
 * @author arbor95
 */

public interface CB_UI_Settings {
    String Work = Config.mWorkPath;

    // Abkürzende Schreibweisen für die übersichlichkeit bei den add Methoden
    SettingModus DEVELOPER = SettingModus.DEVELOPER;
    SettingModus NORMAL = SettingModus.Normal;
    SettingModus EXPERT = SettingModus.Expert;
    SettingModus NEVER = SettingModus.Never;

    String FOUND = "<br>###finds##, ##time##, Found it with Cachebox!";
    String ATTENDED = "<br>###finds##, ##time##, Have been there!";
    String WEBCAM = "<br>###finds##, ##time##, Photo taken!";
    String DNF = "<br>##time##. Could not find the cache!";
    String LOG = "Logged it with Cachebox!";
    String DISCOVERD = "<br> ##time##, Discovered it with Cachebox!";
    String VISITED = "<br> ##time##, Visited it with Cachebox!";
    String DROPPED = "<br> ##time##, Dropped off with Cachebox!";
    String PICKED = "<br> ##time##, Picked it with Cachebox!";
    String GRABED = "<br> ##time##, Grabed it with Cachebox!";

    Integer[] approach = new Integer[]{0, 2, 10, 25, 50, 100, 200, 500, 1000};
    Integer[] TrackDistanceArray = new Integer[]{1, 3, 5, 10, 20};
    String[] navis = new String[]{"Google", "OsmAnd", "OsmAnd2", "Waze", "Orux", "Sygic", "Navigon"};

    SettingString OverrideUrl = (SettingString) SettingsList.addSetting(new SettingString("OverrideUrl", SettingCategory.Login, DEVELOPER, "", SettingStoreType.Global, SettingUsage.ACB));

    SettingBool quickButtonShow = new SettingBool("quickButtonShow", SettingCategory.QuickList, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingString quickButtonList = (SettingString) SettingsList.addSetting(new SettingString("quickButtonList", SettingCategory.QuickList, NEVER, "1,15,14,19,12,23,2,13", SettingStoreType.Global, SettingUsage.ACB));
    SettingBool quickButtonLastShow = new SettingBool("quickButtonLastShow", SettingCategory.QuickList, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);

    SettingIntArray ZoomCross = new SettingIntArray("ZoomCross", SettingCategory.Map, EXPERT, 16, SettingStoreType.Global, SettingUsage.ACB, LocatorSettings.CrossLevel);
    SettingBool MapShowRating = new SettingBool("MapShowRating", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool MapShowDT = new SettingBool("MapShowDT", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool MapShowTitles = new SettingBool("MapShowTitles", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool ShowAllWaypoints = new SettingBool("ShowAllWaypoints", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool MapShowCompass = new SettingBool("MapShowCompass", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool MapNorthOriented = new SettingBool("MapNorthOriented", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool ShowDirektLine = new SettingBool("ShowDirektLine", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool MapHideMyFinds = new SettingBool("MapHideMyFinds", SettingCategory.Map, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingInt LastMapToggleBtnState = (SettingInt) SettingsList.addSetting(new SettingInt("LastMapToggleBtnState", SettingCategory.Map, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));

    SettingBool LiveMapEnabeld = new SettingBool("LiveMapEnabeld", SettingCategory.LiveMap, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);

    SettingInt HardwareCompassLevel = (SettingInt) SettingsList.addSetting(new SettingInt("HardwareCompassLevel", SettingCategory.Gps, NORMAL, 5, SettingStoreType.Global, SettingUsage.ACB));
    SettingBool HardwareCompass = new SettingBool("HardwareCompass", SettingCategory.Gps, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingInt gpsUpdateTime = (SettingInt) SettingsList.addSetting(new SettingInt("gpsUpdateTime", SettingCategory.Gps, NORMAL, 500, SettingStoreType.Global, SettingUsage.ACB));
    SettingBool Ask_Switch_GPS_ON = new SettingBool("Ask_Switch_GPS_ON", SettingCategory.Gps, NORMAL, true, SettingStoreType.Platform, SettingUsage.ALL);

    SettingBool CompassShowMap = new SettingBool("CompassShowMap", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CompassShowWP_Name = new SettingBool("CompassShowWP_Name", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CompassShowWP_Icon = new SettingBool("CompassShowWP_Icon", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CompassShowAttributes = new SettingBool("CompassShowAttributes", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CompassShowGcCode = new SettingBool("CompassShowGcCode", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CompassShowCoords = new SettingBool("CompassShowCoords", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CompassShowWpDesc = new SettingBool("CompassShowWpDesc", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CompassShowSatInfos = new SettingBool("CompassShowSatInfos", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CompassShowSunMoon = new SettingBool("CompassShowSunMoon", SettingCategory.Compass, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CompassShowTargetDirection = new SettingBool("CompassShowTargetDirection", SettingCategory.Compass, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CompassShowSDT = new SettingBool("CompassShowSDT", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CompassShowLastFound = new SettingBool("CompassShowLastFound", SettingCategory.Compass, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);

    SettingInt installedRev = (SettingInt) SettingsList.addSetting(new SettingInt("installRev", SettingCategory.Misc, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    SettingInt VibrateTime = (SettingInt) SettingsList.addSetting(new SettingInt("VibrateTime", SettingCategory.Misc, EXPERT, 20, SettingStoreType.Global, SettingUsage.ACB));
    SettingInt FoundOffset = (SettingInt) SettingsList.addSetting(new SettingInt("FoundOffset", SettingCategory.Misc, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));
    SettingString LastSelectedCache = (SettingString) SettingsList.addSetting(new SettingString("LastSelectedCache", SettingCategory.Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL));
    SettingStringArray Navis = (SettingStringArray) SettingsList.addSetting(new SettingStringArray("Navis", SettingCategory.Misc, NORMAL, "Google", SettingStoreType.Global, SettingUsage.ACB, navis));
    SettingBool TrackRecorderStartup = new SettingBool("TrackRecorderStartup", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingIntArray SoundApproachDistance = new SettingIntArray("SoundApproachDistance", SettingCategory.Misc, NORMAL, 50, SettingStoreType.Global, SettingUsage.ACB, approach);
    SettingIntArray TrackDistance = new SettingIntArray("TrackDistance", SettingCategory.Misc, NORMAL, 3, SettingStoreType.Global, SettingUsage.ACB, TrackDistanceArray);
    SettingLongString FilterNew = new SettingLongString("FilterNew", SettingCategory.Misc, NEVER, "", SettingStoreType.Local, SettingUsage.ALL);
    SettingLongString UserFilter = new SettingLongString("UserFilter", SettingCategory.Misc, NEVER, "", SettingStoreType.Global, SettingUsage.ACB);
    SettingBool SuppressPowerSaving = new SettingBool("SuppressPowerSaving", SettingCategory.Misc, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool StartWithAutoSelect = new SettingBool("StartWithAutoSelect", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool DescriptionNoAttributes = new SettingBool("DescriptionNoAttributes", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool ImperialUnits = new SettingBool("ImperialUnits", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool switchViewApproach = new SettingBool("switchViewApproach", SettingCategory.Misc, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool vibrateFeedback = new SettingBool("vibrateFeedback", SettingCategory.Misc, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB);

    SettingsAudio Approach = new SettingsAudio("Approach", SettingCategory.Sounds, EXPERT, new Audio("data/sound/Approach.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);
    SettingsAudio GPS_lose = new SettingsAudio("GPS_lose", SettingCategory.Sounds, EXPERT, new Audio("data/sound/GPS_lose.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);
    SettingsAudio GPS_fix = new SettingsAudio("GPS_fix", SettingCategory.Sounds, EXPERT, new Audio("data/sound/GPS_Fix.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);
    SettingsAudio AutoResortSound = new SettingsAudio("AutoResortSound", SettingCategory.Sounds, EXPERT, new Audio("data/sound/AutoResort.ogg", false, false, 1.0f), SettingStoreType.Global, SettingUsage.ACB);

    SettingBool ImportGpx = new SettingBool("ImportGpx", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool SearchWithoutFounds = new SettingBool("SearchWithoutFounds", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool SearchWithoutOwns = new SettingBool("SearchWithoutOwns", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool SearchOnlyAvailable = new SettingBool("SearchOnlyAvailable", SettingCategory.API, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool ImportRatings = new SettingBool("ImportRatings", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool ImportPQsFromGeocachingCom = new SettingBool("ImportPQsFromGeocachingCom", SettingCategory.API, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingInt lastSearchRadius = (SettingInt) SettingsList.addSetting(new SettingInt("lastSearchRadius", SettingCategory.API, NEVER, 5, SettingStoreType.Global, SettingUsage.ACB));
    SettingInt ImportLimit = new SettingInt("ImportLimit", SettingCategory.API, NEVER, 50, SettingStoreType.Global, SettingUsage.ACB);

    SettingFolder TrackFolder = new SettingFolder("TrackFolder", SettingCategory.Folder, EXPERT, Work + "/User/Tracks", SettingStoreType.Global, SettingUsage.ACB, true);
    SettingFile FieldNotesGarminPath = (SettingFile) SettingsList
            .addSetting(new SettingFile("FieldNotesGarminPath", SettingCategory.Folder, DEVELOPER, Work + "/User/geocache_visits.txt", SettingStoreType.Global, SettingUsage.ACB));
    SettingFile gpxExportFileName = new SettingFile("gpxExportFileName", SettingCategory.Folder, NEVER, Work + "/User/export.gpx", SettingStoreType.Global, SettingUsage.ACB, "gpx");

    SettingString FoundTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("FoundTemplate", SettingCategory.Templates, NORMAL, FOUND, SettingStoreType.Global, SettingUsage.ACB));
    SettingString AttendedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("AttendedTemplate", SettingCategory.Templates, NORMAL, ATTENDED, SettingStoreType.Global, SettingUsage.ACB));
    SettingString WebcamTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("WebCamTemplate", SettingCategory.Templates, NORMAL, WEBCAM, SettingStoreType.Global, SettingUsage.ACB));
    SettingString DNFTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("DNFTemplate", SettingCategory.Templates, NORMAL, DNF, SettingStoreType.Global, SettingUsage.ACB));
    SettingString NeedsMaintenanceTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("NeedsMaintenanceTemplate", SettingCategory.Templates, NORMAL, LOG, SettingStoreType.Global, SettingUsage.ACB));
    SettingString AddNoteTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("AddNoteTemplate", SettingCategory.Templates, NORMAL, LOG, SettingStoreType.Global, SettingUsage.ACB));
    SettingString DiscoverdTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("DiscoverdTemplate", SettingCategory.Templates, NORMAL, DISCOVERD, SettingStoreType.Global, SettingUsage.ACB));
    SettingString VisitedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("VisitedTemplate", SettingCategory.Templates, NORMAL, VISITED, SettingStoreType.Global, SettingUsage.ACB));
    SettingString DroppedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("DroppedTemplate", SettingCategory.Templates, NORMAL, DROPPED, SettingStoreType.Global, SettingUsage.ACB));
    SettingString GrabbedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("GrabbedTemplate", SettingCategory.Templates, NORMAL, GRABED, SettingStoreType.Global, SettingUsage.ACB));
    SettingString PickedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("PickedTemplate", SettingCategory.Templates, NORMAL, PICKED, SettingStoreType.Global, SettingUsage.ACB));

    SettingBool ShowFieldnotesCMwithFirstShow = new SettingBool("ShowFieldnotesCMwithFirstShow", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool ShowFieldnotesAsDefaultView = new SettingBool("ShowFieldnotesAsDefaultView", SettingCategory.Fieldnotes, EXPERT, false, SettingStoreType.Global, SettingUsage.ACB);

    SettingBool MultiDBAsk = new SettingBool("MultiDBAsk", SettingCategory.Internal, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingString DatabaseName = new SettingString("DatabaseName", SettingCategory.Internal, NEVER, "cachebox.db3", SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CacheMapData = new SettingBool("CacheMapData", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CacheImageData = new SettingBool("CacheImageData", SettingCategory.Internal, DEVELOPER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CacheSpoilerData = new SettingBool("CacheSpoilerData", SettingCategory.Internal, DEVELOPER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool newInstall = new SettingBool("newInstall", SettingCategory.Internal, NEVER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool DeleteLogs = new SettingBool("DeleteLogs", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool CompactDB = new SettingBool("CompactDB", SettingCategory.Internal, DEVELOPER, false, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool TB_DirectLog = new SettingBool("TB_DirectLog", SettingCategory.Internal, NEVER, true, SettingStoreType.Global, SettingUsage.ALL);
    SettingInt LogMaxMonthAge = (SettingInt) SettingsList.addSetting(new SettingInt("LogMaxMonthAge", SettingCategory.Internal, DEVELOPER, 6, SettingStoreType.Global, SettingUsage.ACB));
    SettingInt LogMinCount = (SettingInt) SettingsList.addSetting(new SettingInt("LogMinCount", SettingCategory.Internal, DEVELOPER, 99999, SettingStoreType.Global, SettingUsage.ACB));
    SettingInt MultiDBAutoStartTime = (SettingInt) SettingsList.addSetting(new SettingInt("MultiDBAutoStartTime", SettingCategory.Internal, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB));

    SettingInt AppRaterlaunchCount = new SettingInt("AppRaterlaunchCount", SettingCategory.Internal, NEVER, 0, SettingStoreType.Global, SettingUsage.ACB);
    SettingString AppRaterFirstLunch = new SettingString("AppRaterFirstLunch", SettingCategory.Internal, NEVER, "0", SettingStoreType.Global, SettingUsage.ACB);
    SettingBool AppRaterDontShowAgain = new SettingBool("AppRaterDontShowAgain", SettingCategory.RememberAsk, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);

    SettingBool dynamicZoom = new SettingBool("dynamicZoom", SettingCategory.CarMode, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingInt dynamicZoomLevelMax = (SettingInt) SettingsList.addSetting(new SettingInt("dynamicZoomLevelMax", SettingCategory.CarMode, NORMAL, 17, SettingStoreType.Global, SettingUsage.ACB));
    SettingInt dynamicZoomLevelMin = (SettingInt) SettingsList.addSetting(new SettingInt("dynamicZoomLevelMin", SettingCategory.CarMode, NORMAL, 15, SettingStoreType.Global, SettingUsage.ACB));

    SettingBool RememberAsk_Get_API_Key = new SettingBool("RememberAsk_Get_API_Key", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);

}
