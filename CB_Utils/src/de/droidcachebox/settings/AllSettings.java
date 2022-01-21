/*
 * Copyright (C) 2011-2022 team-cachebox.de
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

import static de.droidcachebox.settings.Config_Core.displayDensity;
import static de.droidcachebox.settings.SettingCategory.CarMode;
import static de.droidcachebox.settings.SettingCategory.Drafts;
import static de.droidcachebox.settings.SettingCategory.Gps;
import static de.droidcachebox.settings.SettingCategory.Internal;
import static de.droidcachebox.settings.SettingCategory.LiveMap;
import static de.droidcachebox.settings.SettingCategory.Login;
import static de.droidcachebox.settings.SettingCategory.Map;
import static de.droidcachebox.settings.SettingCategory.Misc;
import static de.droidcachebox.settings.SettingCategory.QuickList;
import static de.droidcachebox.settings.SettingCategory.Skin;
import static de.droidcachebox.settings.SettingCategory.Sounds;
import static de.droidcachebox.settings.SettingCategory.Templates;
import static de.droidcachebox.settings.SettingModus.DEVELOPER;
import static de.droidcachebox.settings.SettingModus.EXPERT;
import static de.droidcachebox.settings.SettingModus.NEVER;
import static de.droidcachebox.settings.SettingModus.NORMAL;
import static de.droidcachebox.settings.SettingStoreType.Global;
import static de.droidcachebox.settings.SettingStoreType.Local;
import static de.droidcachebox.settings.SettingStoreType.Platform;

import de.droidcachebox.gdx.graphics.HSV_Color;
import de.droidcachebox.utils.log.LogLevel;

public interface AllSettings {

    enum Live_Radius {
        Zoom_13, Zoom_14
    }

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

    Integer[] trackDistanceArray = new Integer[]{1, 3, 5, 10, 20};

    Integer[] numberOfLogsArray = new Integer[]{0, 5, 30};
    String[] navis = new String[]{"Google", "OsmAnd", "OsmAnd2", "Waze", "Orux", "Sygic", "Navigon"};
    Integer[] approachDistanceArray = new Integer[]{0, 2, 10, 25, 50, 100, 200, 500, 1000};
    Integer[] Level = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};
    Integer[] CrossLevel = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};

    // Login
    SettingEncryptedString GcVotePassword = new SettingEncryptedString("GcVotePassword", Login, NORMAL, "", Global);
    SettingEncryptedString AccessToken = new SettingEncryptedString("GcAPI", Login, DEVELOPER, "", Global);
    SettingBool RememberAsk_Get_API_Key = new SettingBool("RememberAsk_Get_API_Key", Login, EXPERT, false, Global);
    SettingString GcLogin = new SettingString("GcLogin", Login, DEVELOPER, "", Global);
    SettingBool UseTestUrl = new SettingBool("StagingAPI", Login, DEVELOPER, false, Global);
    SettingEncryptedString AccessTokenForTest = new SettingEncryptedString("GcAPIStaging", Login, DEVELOPER, "", Global);
    SettingString OverrideUrl = new SettingString("OverrideUrl", Login, DEVELOPER, "", Global);

    // Templates
    SettingString FoundTemplate = new SettingLongString("FoundTemplate", Templates, NORMAL, FOUND, Global);
    SettingString AttendedTemplate = new SettingLongString("AttendedTemplate", Templates, NORMAL, ATTENDED, Global);
    SettingString WebcamTemplate = new SettingLongString("WebCamTemplate", Templates, NORMAL, WEBCAM, Global);
    SettingString DNFTemplate = new SettingLongString("DNFTemplate", Templates, NORMAL, DNF, Global);
    SettingString NeedsMaintenanceTemplate = new SettingLongString("NeedsMaintenanceTemplate", Templates, NORMAL, LOG, Global);
    SettingString AddNoteTemplate = new SettingLongString("AddNoteTemplate", Templates, NORMAL, LOG, Global);
    SettingString DiscoverdTemplate = new SettingLongString("DiscoverdTemplate", Templates, NORMAL, DISCOVERD, Global);
    SettingString VisitedTemplate = new SettingLongString("VisitedTemplate", Templates, NORMAL, VISITED, Global);
    SettingString DroppedTemplate = new SettingLongString("DroppedTemplate", Templates, NORMAL, DROPPED, Global);
    SettingString GrabbedTemplate = new SettingLongString("GrabbedTemplate", Templates, NORMAL, GRABED, Global);
    SettingString PickedTemplate = new SettingLongString("PickedTemplate", Templates, NORMAL, PICKED, Global);

    // Map
    SettingFolder MapPackFolder = new SettingFolder("MapPackFolder", Map, NORMAL, Config_Core.workPath + "/repository/maps", Global, false);
    SettingFloat mapViewDPIFaktor = new SettingFloat("MapViewDPIFaktor", Map, NORMAL, displayDensity, Global);
    SettingFloat mapViewTextFaktor = new SettingFloat("MapViewTextFaktor", Map, NORMAL, 2f, Global);
    SettingString UserMap1 = new SettingString("UserMap1", Map, NORMAL, "{JPG}{name:ESRI World_Imagery}http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", Global);
    SettingString UserMap2 = new SettingString("UserMap2", Map, NORMAL, "", Global);
    SettingInt mapsForgeSaveZoomLevel = new SettingInt("mapsForgeSaveZoomLevel", Map, EXPERT, 14, Global);
    SettingFolder RenderThemesFolder = new SettingFolder("RenderThemesFolder", Map, EXPERT, Config_Core.workPath + "/RenderThemes", Global, false);
    SettingBool PositionMarkerTransparent = new SettingBool("PositionMarkerTransparent", Map, EXPERT, true, Global);
    SettingIntArray OsmMinLevel = new SettingIntArray("OsmMinLevel", Map, EXPERT, 7, Global, Level);
    SettingIntArray OsmMaxLevel = new SettingIntArray("OsmMaxLevel", Map, EXPERT, 21, Global, Level);
    SettingIntArray ZoomCross = new SettingIntArray("ZoomCross", Map, EXPERT, 16, Global, CrossLevel);
    SettingIntArray CompassMapMinZoomLevel = new SettingIntArray("CompassMapMinZoomLevel", Map, EXPERT, 13, Global, Level);
    SettingIntArray CompassMapMaxZommLevel = new SettingIntArray("CompassMapMaxZommLevel", Map, EXPERT, 21, Global, Level);

    // LiveMap
    SettingEnum<Live_Radius> liveRadius = new SettingEnum<>("LiveRadius", LiveMap, EXPERT, Live_Radius.Zoom_14, Global, Live_Radius.Zoom_14);
    SettingInt liveMaxCount = new SettingInt("LiveMaxCount", LiveMap, EXPERT, 350, Global);
    SettingBool liveExcludeFounds = new SettingBool("LiveExcludeFounds", LiveMap, EXPERT, true, Global);
    SettingBool liveExcludeOwn = new SettingBool("LiveExcludeOwn", LiveMap, EXPERT, true, Global);
    SettingEnum<LiveCacheTime> liveCacheTime = new SettingEnum<>("LiveCacheTime", LiveMap, EXPERT, LiveCacheTime.h_6, Global, LiveCacheTime.h_6);
    SettingColor liveMapBackgroundColor = new SettingColor("LiveMapBackgroundColor", LiveMap, EXPERT, new HSV_Color(0.8f, 0.8f, 1f, 1f), Global);

    // Gps
    SettingInt HardwareCompassLevel = new SettingInt("HardwareCompassLevel", Gps, EXPERT, 5, Global);
    SettingBool HardwareCompass = new SettingBool("HardwareCompass", Gps, EXPERT, true, Global);
    SettingInt gpsUpdateTime = new SettingInt("gpsUpdateTime", Gps, EXPERT, 500, Global);
    SettingBool Ask_Switch_GPS_ON = new SettingBool("Ask_Switch_GPS_ON", Gps, EXPERT, true, Platform);
    SettingBool allowLocationService = new SettingBool("AllowLocationService", Gps, EXPERT, true, Global);

    // Skin
    SettingBool useDescriptiveCB_Buttons = new SettingBool("useDescriptiveCB_Buttons", Skin, NORMAL, true, Global, true);
    SettingBool rememberLastAction = new SettingBool("rememberLastAction", Skin, NORMAL, true, Global, true);
    SettingBool RunOverLockScreen = new SettingBool("RunOverLockScreen", Skin, NORMAL, true, Global);
    SettingBool useAndroidKeyboard = new SettingBool("useAndroidKeyboard", Skin, NORMAL, false, Global);
    SettingBool ShowDraftsAsDefaultView = new SettingBool("ShowDraftsAsDefaultView", Skin, EXPERT, false, Global, true);
    SettingBool ShowDraftsContextMenuWithFirstShow = new SettingBool("ShowDraftsCMwithFirstShow", Skin, EXPERT, false, Global, false);
    SettingBool gestureOn = new SettingBool("GestureOn", Skin, EXPERT, true, Global, true);
    SettingBool useGrayFader = new SettingBool("useGrayFader", Skin, EXPERT, false, Global, false);
    SettingInt fadeToGrayAfterXSeconds = new SettingInt("fadeToGrayAfterXSeconds", Skin, EXPERT, 10, Global);
    SettingInt FONT_SIZE_COMPASS_DISTANCE = new SettingInt("FONT_SIZE_COMPASS_DISTANCE", Skin, EXPERT, 25, Global);
    SettingInt FONT_SIZE_BIG = new SettingInt("FONT_SIZE_BIG", Skin, EXPERT, 16, Global);
    SettingInt FONT_SIZE_NORMAL = new SettingInt("FONT_SIZE_NORMAL", Skin, EXPERT, 14, Global);
    SettingInt FONT_SIZE_NORMAL_BUBBLE = new SettingInt("FONT_SIZE_NORMAL_BUBBLE", Skin, EXPERT, 13, Global);
    SettingInt FONT_SIZE_SMALL = new SettingInt("FONT_SIZE_SMALL", Skin, EXPERT, 12, Global);
    SettingInt FONT_SIZE_SMALL_BUBBLE = new SettingInt("FONT_SIZE_SMALL_BUBBLE", Skin, EXPERT, 10, Global);
    SettingBool useMipMap = new SettingBool("useMipMap", Skin, DEVELOPER, false, Global);
    SettingColor solvedMysteryColor = new SettingColor("SolvedMysteryColor", Skin, DEVELOPER, new HSV_Color(0.2f, 1f, 0.2f, 1f), Global);

    // QuickList
    SettingBool quickButtonShow = new SettingBool("quickButtonShow", QuickList, NORMAL, true, Global);

    // Drafts
    SettingFile DraftsGarminPath = new SettingFile("DraftsGarminPath", Drafts, DEVELOPER, Config_Core.workPath + "/User/geocache_visits.txt", Global);
    SettingBool DraftsLoadAll = new SettingBool("DraftsLoadAll", Drafts, DEVELOPER, true, Global, false);
    SettingInt DraftsLoadLength = new SettingInt("DraftsLoadLength", Drafts, DEVELOPER, 100, Global);

    // Misc
    SettingStringArray Navis = new SettingStringArray("Navis", Misc, NORMAL, "Google", Global, navis);
    SettingEnum<Enum<LogLevel>> AktLogLevel = new SettingEnum<Enum<LogLevel>>("AktLogLevel", Misc, NORMAL, LogLevel.OFF, Platform, LogLevel.OFF);
    SettingIntArray SoundApproachDistance = new SettingIntArray("SoundApproachDistance", Misc, NORMAL, 50, Global, approachDistanceArray);
    SettingString friends = new SettingString("Friends", Misc, NORMAL, "", Global);
    SettingIntArray numberOfLogs = new SettingIntArray("NumberOfLogs", Misc, EXPERT, 5, Global, numberOfLogsArray);
    SettingBool UseCorrectedFinal = new SettingBool("UseCorrectedFinal", Misc, EXPERT, true, Global);
    // base settings, read directly from Platform, before the database can be accessed
    SettingBool askAgain = new SettingBool("AskAgain", Misc, EXPERT, false, Platform);
    SettingFolder UserImageFolder = new SettingFolder("UserImageFolder", Misc, EXPERT, Config_Core.workPath + "/User/Media", Global, true);
    SettingBool RememberAsk_RenderThemePathWritable = new SettingBool("RememberAsk_RenderThemePathWritable", Misc, EXPERT, false, Global);
    SettingBool TrackRecorderStartup = new SettingBool("TrackRecorderStartup", Misc, EXPERT, false, Global);
    SettingBool SuppressPowerSaving = new SettingBool("SuppressPowerSaving", Misc, EXPERT, true, Global);
    SettingBool StartWithAutoSelect = new SettingBool("StartWithAutoSelect", Misc, EXPERT, false, Global);
    SettingBool ImperialUnits = new SettingBool("ImperialUnits", Misc, EXPERT, false, Global);
    SettingBool switchViewApproach = new SettingBool("switchViewApproach", Misc, EXPERT, false, Global);
    SettingFolder TrackFolder = new SettingFolder("TrackFolder", Misc, EXPERT, Config_Core.workPath + "/User/Tracks", Global, true);
    SettingInt longClickTime = new SettingInt("LongClicktime", Misc, DEVELOPER, 600, Global);
    SettingBool vibrateFeedback = new SettingBool("vibrateFeedback", Misc, DEVELOPER, true, Global);
    SettingInt VibrateTime = new SettingInt("VibrateTime", Misc, DEVELOPER, 20, Global);

    // Sounds
    SettingsAudio globalVolume = new SettingsAudio("GlobalVolume", Sounds, EXPERT, new Audio("data/sound/Approach.ogg", false, false, 1.0f), Global);
    SettingsAudio Approach = new SettingsAudio("Approach", Sounds, EXPERT, new Audio("data/sound/Approach.ogg", false, false, 1.0f), Global);
    SettingsAudio GPS_lose = new SettingsAudio("GPS_lose", Sounds, EXPERT, new Audio("data/sound/GPS_lose.ogg", false, false, 1.0f), Global);
    SettingsAudio GPS_fix = new SettingsAudio("GPS_fix", Sounds, EXPERT, new Audio("data/sound/GPS_Fix.ogg", false, false, 1.0f), Global);
    SettingsAudio AutoResortSound = new SettingsAudio("AutoResortSound", Sounds, EXPERT, new Audio("data/sound/AutoResort.ogg", false, false, 1.0f), Global);

    // CarMode
    SettingBool dynamicZoom = new SettingBool("dynamicZoom", CarMode, EXPERT, true, Global);
    SettingInt dynamicZoomLevelMax = new SettingInt("dynamicZoomLevelMax", CarMode, EXPERT, 17, Global);
    SettingInt dynamicZoomLevelMin = new SettingInt("dynamicZoomLevelMin", CarMode, EXPERT, 15, Global);
    SettingBool MoveMapCenterWithSpeed = new SettingBool("MoveMapCenterWithSpeed", CarMode, EXPERT, false, Global);
    SettingInt MoveMapCenterMaxSpeed = new SettingInt("MoveMapCenterMaxSpeed", CarMode, EXPERT, 60, Global);

    // Internal
    SettingFolder imageCacheFolder = new SettingFolder("ImageCacheFolder", Internal, NEVER, Config_Core.workPath + "/repository/cache", Local, true);
    SettingBool nightMode = new SettingBool("nightMode", Internal, NEVER, false, Global);
    SettingFolder skinFolder = new SettingFolder("SkinFolder", Internal, NEVER, "default", Global, false, true);
    SettingBool isExpert = new SettingBool("SettingsShowExpert", Internal, NEVER, false, Global);
    SettingBool isDeveloper = new SettingBool("SettingsShowAll", Internal, NEVER, false, Global);
    SettingFolder languagePath = new SettingFolder("LanguagePath", Internal, NEVER, "data/lang", Global, true);
    SettingFolder tileCacheFolder = new SettingFolder("TileCacheFolder", Internal, NEVER, Config_Core.workPath + "/repository/cache", Global, true);
    SettingFolder tileCacheFolderLocal = new SettingFolder("TileCacheFolderLocal", Internal, NEVER, "", Local, true);
    SettingDouble mapInitLatitude = new SettingDouble("MapInitLatitude", Internal, NEVER, -1000, Global);
    SettingDouble mapInitLongitude = new SettingDouble("MapInitLongitude", Internal, NEVER, -1000, Global);
    SettingFolder MapPackFolderLocal = new SettingFolder("MapPackFolderLocal", Internal, NEVER, Config_Core.workPath + "/repository/maps", Local, false);
    SettingStringList currentMapLayer = new SettingStringList("CurrentMapLayer", Internal, NEVER, new String[]{"Mapnik"}, Global);
    SettingString CurrentMapOverlayLayerName = new SettingString("CurrentMapOverlayLayer", Internal, NEVER, "", Global);
    SettingInt lastZoomLevel = new SettingInt("lastZoomLevel", Internal, NEVER, 14, Global);
    SettingFile mapsForgeDayTheme = new SettingFile("MapsforgeDayTheme", Internal, NEVER, "", Global, "xml");
    SettingFile mapsForgeNightTheme = new SettingFile("MapsforgeNightTheme", Internal, NEVER, "", Global, "xml");
    SettingFile mapsForgeCarDayTheme = new SettingFile("MapsforgeCarDayTheme", Internal, NEVER, "CAR", Global, "xml");
    SettingFile mapsForgeCarNightTheme = new SettingFile("MapsforgeCarNightTheme", Internal, NEVER, "CAR", Global, "xml");
    SettingString mapsForgeDayStyle = new SettingString("MapsforgeDayStyle", Internal, NEVER, "", Global);
    SettingString mapsForgeNightStyle = new SettingString("MapsforgeNightStyle", Internal, NEVER, "", Global);
    SettingString mapsForgeCarDayStyle = new SettingString("MapsforgeCarDayStyle", Internal, NEVER, "", Global);
    SettingString mapsForgeCarNightStyle = new SettingString("MapsforgeCarNightStyle", Internal, NEVER, "", Global);
    SettingString preferredMapLanguage = new SettingString("MapLanguage", Internal, NEVER, "", Global);
    SettingBool showRating = new SettingBool("MapShowRating", Internal, NEVER, true, Global);
    SettingBool showDifficultyTerrain = new SettingBool("MapShowDT", Internal, NEVER, true, Global);
    SettingBool showTitles = new SettingBool("MapShowTitles", Internal, NEVER, true, Global);
    SettingBool showAllWaypoints = new SettingBool("ShowAllWaypoints", Internal, NEVER, false, Global);
    SettingBool showAccuracyCircle = new SettingBool("ShowAccuracyCircle", Internal, NEVER, true, Global);
    SettingBool showMapCenterCross = new SettingBool("ShowMapCenterCross", Internal, NEVER, true, Global);
    SettingBool showAtOriginalPosition = new SettingBool("ShowAtOriginalPosition", Internal, NEVER, false, Global);
    SettingBool showDistanceCircle = new SettingBool("ShowDistanceCircle", Internal, NEVER, true, Global);
    SettingBool showDistanceToCenter = new SettingBool("ShowDistanceToCenter", Internal, NEVER, false, Global);
    SettingBool showInfo = new SettingBool("", Internal, NEVER, true, Global);
    SettingBool isMapNorthOriented = new SettingBool("MapNorthOriented", Internal, NEVER, true, Global);
    SettingBool showDirectLine = new SettingBool("ShowDirektLine", Internal, NEVER, false, Global);
    SettingBool hideMyFinds = new SettingBool("MapHideMyFinds", Internal, NEVER, false, Global);
    SettingInt lastMapToggleBtnState = new SettingInt("LastMapToggleBtnState", Internal, NEVER, 0, Global);
    SettingBool CompassShowMap = new SettingBool("CompassShowMap", Internal, NEVER, true, Global);
    SettingBool CompassShowWP_Name = new SettingBool("CompassShowWP_Name", Internal, NEVER, true, Global);
    SettingBool CompassShowWP_Icon = new SettingBool("CompassShowWP_Icon", Internal, NEVER, true, Global);
    SettingBool CompassShowAttributes = new SettingBool("CompassShowAttributes", Internal, NEVER, true, Global);
    SettingBool CompassShowGcCode = new SettingBool("CompassShowGcCode", Internal, NEVER, true, Global);
    SettingBool CompassShowCoords = new SettingBool("CompassShowCoords", Internal, NEVER, true, Global);
    SettingBool CompassShowWpDesc = new SettingBool("CompassShowWpDesc", Internal, NEVER, true, Global);
    SettingBool CompassShowSatInfos = new SettingBool("CompassShowSatInfos", Internal, NEVER, true, Global);
    SettingBool CompassShowSunMoon = new SettingBool("CompassShowSunMoon", Internal, NEVER, false, Global);
    SettingBool CompassShowTargetDirection = new SettingBool("CompassShowTargetDirection", Internal, NEVER, false, Global);
    SettingBool CompassShowSDT = new SettingBool("CompassShowSDT", Internal, NEVER, true, Global);
    SettingBool CompassShowLastFound = new SettingBool("CompassShowLastFound", Internal, NEVER, true, Global);
    SettingString rememberedGeoCache = new SettingString("rememberedGeoCache", Internal, NEVER, "", Global);
    SettingFile Sel_LanguagePath = new SettingFile("Sel_LanguagePath", Internal, EXPERT, "data/lang/en-GB/strings.ini", Platform, "lan");
    SettingBool disableLiveMap = new SettingBool("DisableLiveMap", Internal, NEVER, false, Global);
    SettingDouble ParkingLatitude = new SettingDouble("ParkingLatitude", Internal, NEVER, 0, Global);
    SettingDouble ParkingLongitude = new SettingDouble("ParkingLongitude", Internal, NEVER, 0, Global);
    SettingFolder PocketQueryFolder = new SettingFolder("PocketQueryFolder", Internal, NEVER, Config_Core.workPath + "/PocketQuery", Global, true);
    SettingFolder DescriptionImageFolder = new SettingFolder("DescriptionImageFolder", Internal, NEVER, Config_Core.workPath + "/repository/images", Global, true);
    SettingFolder SpoilerFolder = new SettingFolder("SpoilerFolder", Internal, NEVER, Config_Core.workPath + "/repository/spoilers", Global, true);
    SettingFolder DescriptionImageFolderLocal = new SettingFolder("DescriptionImageFolderLocal", Internal, NEVER, "", SettingStoreType.Local, true);
    SettingFolder SpoilerFolderLocal = new SettingFolder("SpoilerFolderLocal", Internal, NEVER, "", SettingStoreType.Local, true);
    SettingInt connection_timeout = new SettingInt("conection_timeout", Internal, NEVER, 10000, Global);
    SettingInt socket_timeout = new SettingInt("socket_timeout", Internal, NEVER, 60000, Global);
    SettingIntArray TrackDistance = new SettingIntArray("TrackDistance", Internal, NEVER, 3, Global, trackDistanceArray);
    SettingLongString lastFilter = new SettingLongString("", Internal, NEVER, "", SettingStoreType.Local);
    SettingLongString UserFilters = new SettingLongString("UserFilters", Internal, NEVER, "", Global);
    SettingInt installedRev = new SettingInt("installRev", Internal, NEVER, 0, Global);
    SettingInt FoundOffset = new SettingInt("FoundOffset", Internal, NEVER, 0, Global);
    SettingString lastSelectedCache = new SettingString("LastSelectedCache", Internal, NEVER, "", SettingStoreType.Local);
    SettingInt routeProfile = new SettingInt("routeProfile", Internal, NEVER, 0, Global); // perhaps change to enum
    SettingBool ImportGpx = new SettingBool("ImportGpx", Internal, NEVER, false, Global);
    SettingBool SearchWithoutFounds = new SettingBool("SearchWithoutFounds", Internal, NEVER, true, Global);
    SettingBool SearchWithoutOwns = new SettingBool("SearchWithoutOwns", Internal, NEVER, true, Global);
    SettingBool SearchOnlyAvailable = new SettingBool("SearchOnlyAvailable", Internal, NEVER, true, Global);
    SettingBool ImportRatings = new SettingBool("ImportRatings", Internal, NEVER, false, Global);
    SettingBool ImportPQsFromGeocachingCom = new SettingBool("ImportPQsFromGeocachingCom", Internal, NEVER, false, Global);
    SettingInt lastSearchRadius = new SettingInt("lastSearchRadius", Internal, NEVER, 5, Global);
    SettingInt ImportLimit = new SettingInt("ImportLimit", Internal, NEVER, 50, Global);
    SettingString quickButtonList = new SettingString("quickButtonList", Internal, NEVER, "1,15,14,19,12,23,2,13", Global);
    SettingBool quickButtonLastShow = new SettingBool("quickButtonLastShow", Internal, NEVER, false, Global);
    SettingBool MultiDBAsk = new SettingBool("MultiDBAsk", Internal, NEVER, true, Global);
    SettingString DatabaseName = new SettingString("DatabaseName", Internal, NEVER, "cachebox.db3", Global);
    SettingBool CacheMapData = new SettingBool("CacheMapData", Internal, NEVER, false, Global);
    SettingBool CacheImageData = new SettingBool("CacheImageData", Internal, NEVER, true, Global);
    SettingBool CacheSpoilerData = new SettingBool("CacheSpoilerData", Internal, NEVER, true, Global);
    SettingBool newInstall = new SettingBool("newInstall", Internal, NEVER, false, Global);
    SettingBool DeleteLogs = new SettingBool("DeleteLogs", Internal, NEVER, false, Global);
    SettingBool CompactDB = new SettingBool("CompactDB", Internal, NEVER, false, Global);
    SettingBool TB_DirectLog = new SettingBool("TB_DirectLog", Internal, NEVER, true, Global);
    SettingInt LogMaxMonthAge = new SettingInt("LogMaxMonthAge", Internal, NEVER, 6, Global);
    SettingInt LogMinCount = new SettingInt("LogMinCount", Internal, NEVER, 99999, Global);
    SettingInt MultiDBAutoStartTime = new SettingInt("MultiDBAutoStartTime", Internal, NEVER, 0, Global);
    SettingInt AppRaterlaunchCount = new SettingInt("AppRaterlaunchCount", Internal, NEVER, 0, Global);
    SettingString AppRaterFirstLunch = new SettingString("AppRaterFirstLunch", Internal, NEVER, "0", Global);
    SettingString GSAKLastUsedDatabasePath = new SettingString("GSAKLastUsedDatabasePath", Internal, NEVER, "", Global);
    SettingString GSAKLastUsedDatabaseName = new SettingString("GSAKLastUsedDatabaseName", Internal, NEVER, "", Global);
    SettingString GSAKLastUsedImageDatabasePath = new SettingString("GSAKLastUsedImageDatabasePath", Internal, NEVER, "", Global);
    SettingString GSAKLastUsedImageDatabaseName = new SettingString("GSAKLastUsedImageDatabaseName", Internal, NEVER, "", Global);
    SettingString GSAKLastUsedImagesPath = new SettingString("GSAKLastUsedImagesPath", Internal, NEVER, "", Global);
    SettingBool withLogImages = new SettingBool("withLogImages", Internal, NEVER, false, Global);
    SettingString TemplateLastUsedPath = new SettingString("TemplateLastUsedPath", Internal, NEVER, "", Global);
    SettingString TemplateLastUsedName = new SettingString("TemplateLastUsedName", Internal, NEVER, "", Global);
    SettingString ImageUploadLastUsedPath = new SettingString("ImageUploadLastUsedPath", Internal, NEVER, "", Global);
    SettingFile gpxExportFileName = new SettingFile("gpxExportFileName", Internal, NEVER, Config_Core.workPath + "/User/export.gpx", Global, "gpx");
    SettingBool liveMapEnabled = new SettingBool("LiveMapEnabeld", Internal, NEVER, false, Global);
    SettingBool CacheContextMenuShortClickToggle = new SettingBool("CacheContextMenuShortClickToggle", Internal, NEVER, true, Global, false);
    SettingBool AppRaterDontShowAgain = new SettingBool("AppRaterDontShowAgain", Internal, NEVER, true, Global);

    enum LiveCacheTime {
        min_10, min_30, min_60, h_6, h_12, h_24;

        public int getLifetime() {
            switch (this) {
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
