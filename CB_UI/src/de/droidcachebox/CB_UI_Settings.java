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

package de.droidcachebox;

import de.droidcachebox.settings.*;
import de.droidcachebox.utils.Config_Core;

import static de.droidcachebox.settings.SettingCategory.*;
import static de.droidcachebox.settings.SettingModus.*;
import static de.droidcachebox.settings.SettingStoreType.Global;
import static de.droidcachebox.settings.SettingStoreType.Platform;
import static de.droidcachebox.settings.SettingUsage.ACB;
import static de.droidcachebox.settings.SettingUsage.ALL;

/**
 * @author Longri
 * @author arbor95
 */

public interface CB_UI_Settings {

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

    Integer[] approachDistanceArray = new Integer[]{0, 2, 10, 25, 50, 100, 200, 500, 1000};
    Integer[] trackDistanceArray = new Integer[]{1, 3, 5, 10, 20};
    String[] navis = new String[]{"Google", "OsmAnd", "OsmAnd2", "Waze", "Orux", "Sygic", "Navigon"};

    SettingString OverrideUrl = new SettingString("OverrideUrl", Login, DEVELOPER, "", Global, ACB);

    SettingBool quickButtonShow = new SettingBool("quickButtonShow", QuickList, NORMAL, true, Global, ACB);
    SettingString quickButtonList = new SettingString("quickButtonList", QuickList, NEVER, "1,15,14,19,12,23,2,13", Global, ACB);
    SettingBool quickButtonLastShow = new SettingBool("quickButtonLastShow", QuickList, NEVER, false, Global, ACB);

    SettingBool liveMapEnabled = new SettingBool("LiveMapEnabeld", LiveMap, NEVER, false, Global, ACB);
    SettingInt routeProfile = new SettingInt("routeProfile", Map, NEVER, 0, Global, ACB); // perhaps change to enum

    SettingInt HardwareCompassLevel = new SettingInt("HardwareCompassLevel", Gps, NORMAL, 5, Global, ACB);
    SettingBool HardwareCompass = new SettingBool("HardwareCompass", Gps, NORMAL, true, Global, ACB);
    SettingInt gpsUpdateTime = new SettingInt("gpsUpdateTime", Gps, NORMAL, 500, Global, ACB);
    SettingBool Ask_Switch_GPS_ON = new SettingBool("Ask_Switch_GPS_ON", Gps, NORMAL, true, Platform, ALL);

    SettingBool CompassShowMap = new SettingBool("CompassShowMap", Compass, NEVER, true, Global, ACB);
    SettingBool CompassShowWP_Name = new SettingBool("CompassShowWP_Name", Compass, NEVER, true, Global, ACB);
    SettingBool CompassShowWP_Icon = new SettingBool("CompassShowWP_Icon", Compass, NEVER, true, Global, ACB);
    SettingBool CompassShowAttributes = new SettingBool("CompassShowAttributes", Compass, NEVER, true, Global, ACB);
    SettingBool CompassShowGcCode = new SettingBool("CompassShowGcCode", Compass, NEVER, true, Global, ACB);
    SettingBool CompassShowCoords = new SettingBool("CompassShowCoords", Compass, NEVER, true, Global, ACB);
    SettingBool CompassShowWpDesc = new SettingBool("CompassShowWpDesc", Compass, NEVER, true, Global, ACB);
    SettingBool CompassShowSatInfos = new SettingBool("CompassShowSatInfos", Compass, NEVER, true, Global, ACB);
    SettingBool CompassShowSunMoon = new SettingBool("CompassShowSunMoon", Compass, NEVER, false, Global, ACB);
    SettingBool CompassShowTargetDirection = new SettingBool("CompassShowTargetDirection", Compass, NEVER, false, Global, ACB);
    SettingBool CompassShowSDT = new SettingBool("CompassShowSDT", Compass, NEVER, true, Global, ACB);
    SettingBool CompassShowLastFound = new SettingBool("CompassShowLastFound", Compass, NEVER, true, Global, ACB);

    SettingInt installedRev = new SettingInt("installRev", Misc, NEVER, 0, Global, ACB);
    SettingInt FoundOffset = new SettingInt("FoundOffset", Misc, NEVER, 0, Global, ACB);
    SettingString LastSelectedCache = new SettingString("LastSelectedCache", Misc, NEVER, "", SettingStoreType.Local, ALL);
    SettingStringArray Navis = new SettingStringArray("Navis", Misc, NORMAL, "Google", Global, ACB, navis);
    SettingBool TrackRecorderStartup = new SettingBool("TrackRecorderStartup", Misc, NORMAL, false, Global, ACB);
    SettingIntArray SoundApproachDistance = new SettingIntArray("SoundApproachDistance", Misc, NORMAL, 50, Global, ACB, approachDistanceArray);
    SettingIntArray TrackDistance = new SettingIntArray("TrackDistance", Misc, NEVER, 3, Global, ACB, trackDistanceArray);
    SettingLongString FilterNew = new SettingLongString("FilterNew", Misc, NEVER, "", SettingStoreType.Local, ALL);
    SettingLongString UserFilters = new SettingLongString("UserFilters", Misc, NEVER, "", Global, ACB);
    SettingBool SuppressPowerSaving = new SettingBool("SuppressPowerSaving", Misc, EXPERT, true, Global, ACB);
    SettingBool StartWithAutoSelect = new SettingBool("StartWithAutoSelect", Misc, EXPERT, false, Global, ACB);
    SettingBool ImperialUnits = new SettingBool("ImperialUnits", Misc, NORMAL, false, Global, ACB);
    SettingBool switchViewApproach = new SettingBool("switchViewApproach", Misc, EXPERT, false, Global, ACB);
    SettingBool vibrateFeedback = new SettingBool("vibrateFeedback", Misc, EXPERT, true, Global, ACB);
    SettingInt VibrateTime = new SettingInt("VibrateTime", Misc, EXPERT, 20, Global, ACB);

    SettingsAudio Approach = new SettingsAudio("Approach", Sounds, EXPERT, new Audio("data/sound/Approach.ogg", false, false, 1.0f), Global, ACB);
    SettingsAudio GPS_lose = new SettingsAudio("GPS_lose", Sounds, EXPERT, new Audio("data/sound/GPS_lose.ogg", false, false, 1.0f), Global, ACB);
    SettingsAudio GPS_fix = new SettingsAudio("GPS_fix", Sounds, EXPERT, new Audio("data/sound/GPS_Fix.ogg", false, false, 1.0f), Global, ACB);
    SettingsAudio AutoResortSound = new SettingsAudio("AutoResortSound", Sounds, EXPERT, new Audio("data/sound/AutoResort.ogg", false, false, 1.0f), Global, ACB);

    SettingBool ImportGpx = new SettingBool("ImportGpx", API, NEVER, false, Global, ACB);
    SettingBool SearchWithoutFounds = new SettingBool("SearchWithoutFounds", API, NEVER, true, Global, ACB);
    SettingBool SearchWithoutOwns = new SettingBool("SearchWithoutOwns", API, NEVER, true, Global, ACB);
    SettingBool SearchOnlyAvailable = new SettingBool("SearchOnlyAvailable", API, NEVER, true, Global, ACB);
    SettingBool ImportRatings = new SettingBool("ImportRatings", API, NEVER, false, Global, ACB);
    SettingBool ImportPQsFromGeocachingCom = new SettingBool("ImportPQsFromGeocachingCom", API, NEVER, false, Global, ACB);
    SettingInt lastSearchRadius = new SettingInt("lastSearchRadius", API, NEVER, 5, Global, ACB);
    SettingInt ImportLimit = new SettingInt("ImportLimit", API, NEVER, 50, Global, ACB);

    SettingFolder TrackFolder = new SettingFolder("TrackFolder", Folder, EXPERT, Config_Core.workPath + "/User/Tracks", Global, ACB, true);
    SettingFile DraftsGarminPath = new SettingFile("DraftsGarminPath", Folder, DEVELOPER, Config_Core.workPath + "/User/geocache_visits.txt", Global, ACB);
    SettingFile gpxExportFileName = new SettingFile("gpxExportFileName", Folder, NEVER, Config_Core.workPath + "/User/export.gpx", Global, ACB, "gpx");

    SettingString FoundTemplate = new SettingLongString("FoundTemplate", Templates, NORMAL, FOUND, Global, ACB);
    SettingString AttendedTemplate = new SettingLongString("AttendedTemplate", Templates, NORMAL, ATTENDED, Global, ACB);
    SettingString WebcamTemplate = new SettingLongString("WebCamTemplate", Templates, NORMAL, WEBCAM, Global, ACB);
    SettingString DNFTemplate = new SettingLongString("DNFTemplate", Templates, NORMAL, DNF, Global, ACB);
    SettingString NeedsMaintenanceTemplate = new SettingLongString("NeedsMaintenanceTemplate", Templates, NORMAL, LOG, Global, ACB);
    SettingString AddNoteTemplate = new SettingLongString("AddNoteTemplate", Templates, NORMAL, LOG, Global, ACB);
    SettingString DiscoverdTemplate = new SettingLongString("DiscoverdTemplate", Templates, NORMAL, DISCOVERD, Global, ACB);
    SettingString VisitedTemplate = new SettingLongString("VisitedTemplate", Templates, NORMAL, VISITED, Global, ACB);
    SettingString DroppedTemplate = new SettingLongString("DroppedTemplate", Templates, NORMAL, DROPPED, Global, ACB);
    SettingString GrabbedTemplate = new SettingLongString("GrabbedTemplate", Templates, NORMAL, GRABED, Global, ACB);
    SettingString PickedTemplate = new SettingLongString("PickedTemplate", Templates, NORMAL, PICKED, Global, ACB);

    SettingBool ShowDraftsAsDefaultView = new SettingBool("ShowDraftsAsDefaultView", Skin, EXPERT, false, Global, ACB, true);
    SettingBool ShowDraftsContextMenuWithFirstShow = new SettingBool("ShowDraftsCMwithFirstShow", Skin, EXPERT, false, Global, ACB, false);
    SettingBool CacheContextMenuShortClickToggle = new SettingBool("CacheContextMenuShortClickToggle", Skin, NEVER, true, Global, ACB, false);

    SettingBool MultiDBAsk = new SettingBool("MultiDBAsk", Internal, NEVER, true, Global, ACB);
    SettingString DatabaseName = new SettingString("DatabaseName", Internal, NEVER, "cachebox.db3", Global, ACB);
    SettingBool CacheMapData = new SettingBool("CacheMapData", Internal, DEVELOPER, false, Global, ACB);
    SettingBool CacheImageData = new SettingBool("CacheImageData", Internal, DEVELOPER, true, Global, ACB);
    SettingBool CacheSpoilerData = new SettingBool("CacheSpoilerData", Internal, DEVELOPER, true, Global, ACB);
    SettingBool newInstall = new SettingBool("newInstall", Internal, NEVER, false, Global, ACB);
    SettingBool DeleteLogs = new SettingBool("DeleteLogs", Internal, DEVELOPER, false, Global, ACB);
    SettingBool CompactDB = new SettingBool("CompactDB", Internal, DEVELOPER, false, Global, ACB);
    SettingBool TB_DirectLog = new SettingBool("TB_DirectLog", Internal, NEVER, true, Global, ALL);
    SettingInt LogMaxMonthAge = new SettingInt("LogMaxMonthAge", Internal, DEVELOPER, 6, Global, ACB);
    SettingInt LogMinCount = new SettingInt("LogMinCount", Internal, DEVELOPER, 99999, Global, ACB);
    SettingInt MultiDBAutoStartTime = new SettingInt("MultiDBAutoStartTime", Internal, NEVER, 0, Global, ACB);
    SettingInt AppRaterlaunchCount = new SettingInt("AppRaterlaunchCount", Internal, NEVER, 0, Global, ACB);
    SettingString AppRaterFirstLunch = new SettingString("AppRaterFirstLunch", Internal, NEVER, "0", Global, ACB);
    SettingString GSAKLastUsedDatabasePath = new SettingString("GSAKLastUsedDatabasePath", Internal, NEVER, "", Global, ACB);
    SettingString GSAKLastUsedDatabaseName = new SettingString("GSAKLastUsedDatabaseName", Internal, NEVER, "", Global, ACB);
    SettingString GSAKLastUsedImageDatabasePath = new SettingString("GSAKLastUsedImageDatabasePath", Internal, NEVER, "", Global, ACB);
    SettingString GSAKLastUsedImageDatabaseName = new SettingString("GSAKLastUsedImageDatabaseName", Internal, NEVER, "", Global, ACB);
    SettingString GSAKLastUsedImagesPath = new SettingString("GSAKLastUsedImagesPath", Internal, NEVER, "", Global, ACB);
    SettingBool withLogImages = new SettingBool("withLogImages", Internal, NEVER, false, Global, ACB);
    SettingString TemplateLastUsedPath = new SettingString("TemplateLastUsedPath", Internal, NEVER, "", Global, ACB);
    SettingString TemplateLastUsedName = new SettingString("TemplateLastUsedName", Internal, NEVER, "", Global, ACB);
    SettingString ImageUploadLastUsedPath = new SettingString("ImageUploadLastUsedPath", Internal, NEVER, "", Global, ACB);

    SettingBool dynamicZoom = new SettingBool("dynamicZoom", CarMode, NORMAL, true, Global, ACB);
    SettingInt dynamicZoomLevelMax = new SettingInt("dynamicZoomLevelMax", CarMode, NORMAL, 17, Global, ACB);
    SettingInt dynamicZoomLevelMin = new SettingInt("dynamicZoomLevelMin", CarMode, NORMAL, 15, Global, ACB);

    SettingBool AppRaterDontShowAgain = new SettingBool("AppRaterDontShowAgain", RememberAsk, NEVER, true, Global, ACB);
    SettingBool RememberAsk_Get_API_Key = new SettingBool("RememberAsk_Get_API_Key", RememberAsk, NORMAL, true, Global, ACB);
    SettingBool RememberAsk_RenderThemePathWritable = new SettingBool("RememberAsk_RenderThemePathWritable", RememberAsk, NORMAL, false, Global, ACB);

}
