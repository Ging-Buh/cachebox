package CB_UI.Settings;

import CB_Core.FilterProperties;
import CB_UI.Config;
import CB_Utils.Settings.Audio;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingDouble;
import CB_Utils.Settings.SettingEncryptedString;
import CB_Utils.Settings.SettingFile;
import CB_Utils.Settings.SettingFolder;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingIntArray;
import CB_Utils.Settings.SettingLongString;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingString;
import CB_Utils.Settings.SettingTime;
import CB_Utils.Settings.SettingsAudio;
import CB_Utils.Settings.SettingsList;

public interface CB_UI_Settings
{
	public static final String Work = Config.WorkPath;

	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	public static final SettingModus INVISIBLE = SettingModus.Invisible;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;

	public static final String FOUND = "<br>###finds##, ##time##, Found it with Cachebox!";
	public static final String DNF = "<br>##time##. Logged it with Cachebox!";
	public static final String LOG = "Logged it with Cachebox!";
	public static final String DISCOVERD = "<br> ##time##, Discovered it with Cachebox!";
	public static final String VISITED = "<br> ##time##, Visited it with Cachebox!";
	public static final String DROPPED = "<br> ##time##, Dropped off with Cachebox!";
	public static final String PICKED = "<br> ##time##, Picked it with Cachebox!";
	public static final String GRABED = "<br> ##time##, Grabed it with Cachebox!";

	public static final Integer[] approach = new Integer[]
		{ 0, 2, 10, 25, 50, 100, 200, 500, 1000 };
	public static final Integer[] TrackDistanceArray = new Integer[]
		{ 1, 3, 5, 10, 20 };

	public Integer Level[] = new Integer[]
		{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21 };
	public Integer CrossLevel[] = new Integer[]
		{ 0, 1, 2, 3, 4, 5, 6, 7 };

	// Settings Compass
	public static final SettingInt HardwareCompassLevel = (SettingInt) SettingsList.addSetting(new SettingInt("HardwareCompassLevel",
			SettingCategory.Gps, NORMAL, 5, SettingStoreType.Global));

	public static final SettingBool HardwareCompass = (SettingBool) SettingsList.addSetting(new SettingBool("HardwareCompass",
			SettingCategory.Gps, NORMAL, true, SettingStoreType.Global));

	public static final SettingInt gpsUpdateTime = (SettingInt) SettingsList.addSetting(new SettingInt("gpsUpdateTime",
			SettingCategory.Gps, NORMAL, 500, SettingStoreType.Global));

	public static final SettingBool CompassShowMap = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowMap",
			SettingCategory.Compass, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool CompassShowWP_Name = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowWP_Name",
			SettingCategory.Compass, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool CompassShowWP_Icon = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowWP_Icon",
			SettingCategory.Compass, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool CompassShowAttributes = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowAttributes",
			SettingCategory.Compass, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool CompassShowGcCode = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowGcCode",
			SettingCategory.Compass, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool CompassShowCoords = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowCoords",
			SettingCategory.Compass, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool CompassShowWpDesc = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowWpDesc",
			SettingCategory.Compass, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool CompassShowSatInfos = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowSatInfos",
			SettingCategory.Compass, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool CompassShowSunMoon = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowSunMoon",
			SettingCategory.Compass, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool CompassShowTargetDirection = (SettingBool) SettingsList.addSetting(new SettingBool(
			"CompassShowTargetDirection", SettingCategory.Compass, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool CompassShowSDT = (SettingBool) SettingsList.addSetting(new SettingBool("CompassSDT",
			SettingCategory.Compass, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool CompassShowLastFound = (SettingBool) SettingsList.addSetting(new SettingBool("CompassShowLastFound",
			SettingCategory.Compass, NORMAL, true, SettingStoreType.Global));

	public static final SettingString OverrideUrl = (SettingString) SettingsList.addSetting(new SettingString("OverrideUrl",
			SettingCategory.Debug, EXPERT, "", SettingStoreType.Global));

	// Folder
	public static final SettingFolder UserImageFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("UserImageFolder",
			SettingCategory.Folder, NORMAL, Work + "/User/Media", SettingStoreType.Global));
	public static final SettingFolder LanguagePath = (SettingFolder) SettingsList.addSetting(new SettingFolder("LanguagePath",
			SettingCategory.Folder, NEVER, "data/lang", SettingStoreType.Global));

	public static final SettingFolder TileCacheFolderLocal = (SettingFolder) SettingsList.addSetting(new SettingFolder(
			"TileCacheFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local));
	public static final SettingFolder PocketQueryFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("PocketQueryFolder",
			SettingCategory.Folder, INVISIBLE, Work + "/PocketQuery", SettingStoreType.Global));

	public static final SettingFolder MapPackFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("MapPackFolder",
			SettingCategory.Folder, NORMAL, Work + "/repository/maps", SettingStoreType.Global));
	public static final SettingFolder MapPackFolderLocal = (SettingFolder) SettingsList.addSetting(new SettingFolder("MapPackFolderLocal",
			SettingCategory.Folder, NEVER, "", SettingStoreType.Local));

	public static final SettingFolder TrackFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("TrackFolder",
			SettingCategory.Folder, NORMAL, Work + "/User/Tracks", SettingStoreType.Global));

	// Files
	public static final SettingFile Sel_LanguagePath = (SettingFile) SettingsList.addSetting(new SettingFile("Sel_LanguagePath",
			SettingCategory.Folder, NEVER, "data/lang/en-GB/strings.ini", SettingStoreType.Platform, "lan"));
	public static final SettingFile DatabasePath = (SettingFile) SettingsList.addSetting(new SettingFile("DatabasePath",
			SettingCategory.Folder, NEVER, Work + "/cachebox.db3", SettingStoreType.Global, "db3"));
	public static final SettingFile FieldNotesGarminPath = (SettingFile) SettingsList.addSetting(new SettingFile("FieldNotesGarminPath",
			SettingCategory.Folder, INVISIBLE, Work + "/User/geocache_visits.txt", SettingStoreType.Global));
	public static final SettingFile MapsforgeDayTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeDayTheme",
			SettingCategory.Skin, NORMAL, "", SettingStoreType.Global, "xml"));
	public static final SettingFile MapsforgeNightTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeNightTheme",
			SettingCategory.Skin, NORMAL, "", SettingStoreType.Global, "xml"));

	public static final SettingBool MapShowRating = (SettingBool) SettingsList.addSetting(new SettingBool("MapShowRating",
			SettingCategory.Map, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool MapShowDT = (SettingBool) SettingsList.addSetting(new SettingBool("MapShowDT", SettingCategory.Map,
			NORMAL, true, SettingStoreType.Global));
	public static final SettingBool MapShowTitles = (SettingBool) SettingsList.addSetting(new SettingBool("MapShowTitles",
			SettingCategory.Map, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool ImportLayerOsm = (SettingBool) SettingsList.addSetting(new SettingBool("ImportLayerOsm",
			SettingCategory.Internal, NEVER, true, SettingStoreType.Global));
	public static final SettingBool TrackRecorderStartup = (SettingBool) SettingsList.addSetting(new SettingBool("TrackRecorderStartup",
			SettingCategory.Misc, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool MapShowCompass = (SettingBool) SettingsList.addSetting(new SettingBool("MapShowCompass",
			SettingCategory.Map, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool CompassNorthOriented = (SettingBool) SettingsList.addSetting(new SettingBool("CompassNorthOriented",
			SettingCategory.Map, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool MapNorthOriented = (SettingBool) SettingsList.addSetting(new SettingBool("MapNorthOriented",
			SettingCategory.Map, NORMAL, true, SettingStoreType.Global));

	public static final SettingBool ImportGpx = (SettingBool) SettingsList.addSetting(new SettingBool("ImportGpx", SettingCategory.API,
			NORMAL, false, SettingStoreType.Global));
	public static final SettingBool CacheMapData = (SettingBool) SettingsList.addSetting(new SettingBool("CacheMapData",
			SettingCategory.Internal, INVISIBLE, false, SettingStoreType.Global));
	public static final SettingBool CacheImageData = (SettingBool) SettingsList.addSetting(new SettingBool("CacheImageData",
			SettingCategory.Internal, INVISIBLE, true, SettingStoreType.Global));
	public static final SettingBool CacheSpoilerData = (SettingBool) SettingsList.addSetting(new SettingBool("CacheSpoilerData",
			SettingCategory.Internal, INVISIBLE, true, SettingStoreType.Global));
	public static final SettingBool SuppressPowerSaving = (SettingBool) SettingsList.addSetting(new SettingBool("SuppressPowerSaving",
			SettingCategory.Misc, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool GCAutoSyncCachesFound = (SettingBool) SettingsList.addSetting(new SettingBool("GCAutoSyncCachesFound",
			SettingCategory.Internal, NEVER, true, SettingStoreType.Global));
	public static final SettingBool GCAdditionalImageDownload = (SettingBool) SettingsList.addSetting(new SettingBool(
			"GCAdditionalImageDownload", SettingCategory.API, EXPERT, false, SettingStoreType.Global));
	public static final SettingBool StartWithAutoSelect = (SettingBool) SettingsList.addSetting(new SettingBool("StartWithAutoSelect",
			SettingCategory.Misc, EXPERT, false, SettingStoreType.Global));
	public static final SettingBool FieldnotesUploadAll = (SettingBool) SettingsList.addSetting(new SettingBool("FieldnotesUploadAll",
			SettingCategory.API, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool MultiDBAsk = (SettingBool) SettingsList.addSetting(new SettingBool("MultiDBAsk",
			SettingCategory.Internal, NEVER, true, SettingStoreType.Global));
	public static final SettingBool MoveMapCenterWithSpeed = (SettingBool) SettingsList.addSetting(new SettingBool(
			"MoveMapCenterWithSpeed", SettingCategory.CarMode, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool SearchWithoutFounds = (SettingBool) SettingsList.addSetting(new SettingBool("SearchWithoutFounds",
			SettingCategory.API, INVISIBLE, true, SettingStoreType.Global));
	public static final SettingBool SearchWithoutOwns = (SettingBool) SettingsList.addSetting(new SettingBool("SearchWithoutOwns",
			SettingCategory.API, INVISIBLE, true, SettingStoreType.Global));
	public static final SettingBool SearchOnlyAvible = (SettingBool) SettingsList.addSetting(new SettingBool("SearchOnlyAvible",
			SettingCategory.API, INVISIBLE, true, SettingStoreType.Global));
	public static final SettingBool quickButtonShow = (SettingBool) SettingsList.addSetting(new SettingBool("quickButtonShow",
			SettingCategory.QuickList, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool DebugShowPanel = (SettingBool) SettingsList.addSetting(new SettingBool("DebugShowPanel",
			SettingCategory.Debug, EXPERT, false, SettingStoreType.Global));
	public static final SettingBool DebugMemory = (SettingBool) SettingsList.addSetting(new SettingBool("DebugMemory",
			SettingCategory.Debug, EXPERT, false, SettingStoreType.Global));
	public static final SettingBool DebugShowMsg = (SettingBool) SettingsList.addSetting(new SettingBool("DebugShowMsg",
			SettingCategory.Debug, EXPERT, false, SettingStoreType.Global));

	public static final SettingBool DebugShowLog = (SettingBool) SettingsList.addSetting(new SettingBool("DebugShowLog",
			SettingCategory.Debug, EXPERT, false, SettingStoreType.Global));
	public static final SettingBool DescriptionNoAttributes = (SettingBool) SettingsList.addSetting(new SettingBool(
			"DescriptionNoAttributes", SettingCategory.Misc, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool quickButtonLastShow = (SettingBool) SettingsList.addSetting(new SettingBool("quickButtonLastShow",
			SettingCategory.QuickList, INVISIBLE, false, SettingStoreType.Global));
	public static final SettingBool newInstall = (SettingBool) SettingsList.addSetting(new SettingBool("newInstall",
			SettingCategory.Internal, NEVER, false, SettingStoreType.Global));
	public static final SettingBool ImperialUnits = (SettingBool) SettingsList.addSetting(new SettingBool("ImperialUnits",
			SettingCategory.Misc, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool ShowDirektLine = (SettingBool) SettingsList.addSetting(new SettingBool("ShowDirektLine",
			SettingCategory.Map, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool ShowAccuracyCircle = (SettingBool) SettingsList.addSetting(new SettingBool("ShowAccuracyCircle",
			SettingCategory.Map, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool ShowMapCenterCross = (SettingBool) SettingsList.addSetting(new SettingBool("ShowMapCenterCross",
			SettingCategory.Map, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool PositionMarkerTransparent = (SettingBool) SettingsList.addSetting(new SettingBool(
			"PositionMarkerTransparent", SettingCategory.Map, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool DebugShowMarker = (SettingBool) SettingsList.addSetting(new SettingBool("DebugShowMarker",
			SettingCategory.Debug, EXPERT, false, SettingStoreType.Global));
	public static final SettingBool ImportRatings = (SettingBool) SettingsList.addSetting(new SettingBool("ImportRatings",
			SettingCategory.API, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool ImportPQsFromGeocachingCom = (SettingBool) SettingsList.addSetting(new SettingBool(
			"ImportPQsFromGeocachingCom", SettingCategory.API, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool SettingsShowExpert = (SettingBool) SettingsList.addSetting(new SettingBool("SettingsShowExpert",
			SettingCategory.Internal, NEVER, false, SettingStoreType.Global));
	public static final SettingBool SettingsShowAll = (SettingBool) SettingsList.addSetting(new SettingBool("SettingsShowAll",
			SettingCategory.Internal, NEVER, false, SettingStoreType.Global));
	public static final SettingBool switchViewApproach = (SettingBool) SettingsList.addSetting(new SettingBool("switchViewApproach",
			SettingCategory.Misc, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool hasCallPermission = (SettingBool) SettingsList.addSetting(new SettingBool("hasCallPermission",
			SettingCategory.Internal, NEVER, false, SettingStoreType.Global));
	public static final SettingBool vibrateFeedback = (SettingBool) SettingsList.addSetting(new SettingBool("vibrateFeedback",
			SettingCategory.Misc, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool hasPQ_PlugIn = (SettingBool) SettingsList.addSetting(new SettingBool("hasPQ_PlugIn",
			SettingCategory.Internal, NEVER, false, SettingStoreType.Global));
	public static final SettingBool hasFTF_PlugIn = (SettingBool) SettingsList.addSetting(new SettingBool("hasFTF_PlugIn",
			SettingCategory.Internal, NEVER, false, SettingStoreType.Global));
	public static final SettingBool dynamicZoom = (SettingBool) SettingsList.addSetting(new SettingBool("dynamicZoom",
			SettingCategory.CarMode, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool dynamicFilterAtSearch = (SettingBool) SettingsList.addSetting(new SettingBool("dynamicFilterAtSearch",
			SettingCategory.Misc, NEVER, true, SettingStoreType.Global));
	public static final SettingBool DeleteLogs = (SettingBool) SettingsList.addSetting(new SettingBool("DeleteLogs",
			SettingCategory.Internal, INVISIBLE, false, SettingStoreType.Global));
	public static final SettingBool CompactDB = (SettingBool) SettingsList.addSetting(new SettingBool("CompactDB",
			SettingCategory.Internal, INVISIBLE, false, SettingStoreType.Global));
	public static final SettingBool RememberAsk_API_Coast = (SettingBool) SettingsList.addSetting(new SettingBool("RememberAsk_API_Coast",
			SettingCategory.RememberAsk, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool AskAgain = (SettingBool) SettingsList.addSetting(new SettingBool("AskAgain",
			SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Platform));
	public static final SettingBool RememberAsk_Get_API_Key = (SettingBool) SettingsList.addSetting(new SettingBool(
			"RememberAsk_Get_API_Key", SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Global));
	public static final SettingBool Ask_Switch_GPS_ON = (SettingBool) SettingsList.addSetting(new SettingBool("Ask_Switch_GPS_ON",
			SettingCategory.RememberAsk, NORMAL, true, SettingStoreType.Platform));
	public static final SettingBool FireMapQueueProcessorExceptions = (SettingBool) SettingsList.addSetting(new SettingBool(
			"FireMapQueueProcessorExceptions", SettingCategory.Internal, INVISIBLE, false, SettingStoreType.Global));

	public static final SettingBool TB_DirectLog = (SettingBool) SettingsList.addSetting(new SettingBool("TB_DirectLog",
			SettingCategory.Internal, NEVER, true, SettingStoreType.Platform));

	public static final SettingBool MapHideMyFinds = (SettingBool) SettingsList.addSetting(new SettingBool("MapHideMyFinds",
			SettingCategory.Map, NORMAL, false, SettingStoreType.Global));
	public static final SettingBool ShowAllWaypoints = (SettingBool) SettingsList.addSetting(new SettingBool("ShowAllWaypoints",
			SettingCategory.Map, NORMAL, false, SettingStoreType.Global));

	// int
	public static final SettingInt LogMaxMonthAge = (SettingInt) SettingsList.addSetting(new SettingInt("LogMaxMonthAge",
			SettingCategory.Internal, INVISIBLE, 6, SettingStoreType.Global));
	public static final SettingInt LogMinCount = (SettingInt) SettingsList.addSetting(new SettingInt("LogMinCount",
			SettingCategory.Internal, INVISIBLE, 99999, SettingStoreType.Global));
	public static final SettingInt installRev = (SettingInt) SettingsList.addSetting(new SettingInt("installRev", SettingCategory.Internal,
			NEVER, 0, SettingStoreType.Global));
	public static final SettingInt MapIniWidth = (SettingInt) SettingsList.addSetting(new SettingInt("MapIniWidth", SettingCategory.Map,
			INVISIBLE, 480, SettingStoreType.Global));
	public static final SettingInt MapIniHeight = (SettingInt) SettingsList.addSetting(new SettingInt("MapIniHeight", SettingCategory.Map,
			INVISIBLE, 535, SettingStoreType.Global));

	public static final SettingInt VibrateTime = (SettingInt) SettingsList.addSetting(new SettingInt("VibrateTime", SettingCategory.Misc,
			NORMAL, 20, SettingStoreType.Global));

	public static final SettingInt FoundOffset = (SettingInt) SettingsList.addSetting(new SettingInt("FoundOffset", SettingCategory.Misc,
			NEVER, 0, SettingStoreType.Global));
	public static final SettingInt MapMaxCachesLabel = (SettingInt) SettingsList.addSetting(new SettingInt("MapMaxCachesLabel",
			SettingCategory.Map, INVISIBLE, 12, SettingStoreType.Global));
	public static final SettingInt MapMaxCachesDisplay_config = (SettingInt) SettingsList.addSetting(new SettingInt(
			"MapMaxCachesDisplay_config", SettingCategory.Map, INVISIBLE, 10000, SettingStoreType.Global));
	public static final SettingInt mapMaxCachesDisplayLarge_config = (SettingInt) SettingsList.addSetting(new SettingInt(
			"mapMaxCachesDisplayLarge_config", SettingCategory.Map, INVISIBLE, 75, SettingStoreType.Global));
	public static final SettingInt MultiDBAutoStartTime = (SettingInt) SettingsList.addSetting(new SettingInt("MultiDBAutoStartTime",
			SettingCategory.Internal, NEVER, 0, SettingStoreType.Global));
	public static final SettingInt MoveMapCenterMaxSpeed = (SettingInt) SettingsList.addSetting(new SettingInt("MoveMapCenterMaxSpeed",
			SettingCategory.CarMode, NORMAL, 60, SettingStoreType.Global));
	public static final SettingInt lastZoomLevel = (SettingInt) SettingsList.addSetting(new SettingInt("lastZoomLevel",
			SettingCategory.Map, INVISIBLE, 14, SettingStoreType.Global));
	public static final SettingInt lastSearchRadius = (SettingInt) SettingsList.addSetting(new SettingInt("lastSearchRadius",
			SettingCategory.API, INVISIBLE, 5, SettingStoreType.Global));
	public static final SettingInt LastMapToggleBtnState = (SettingInt) SettingsList.addSetting(new SettingInt("LastMapToggleBtnState",
			SettingCategory.Map, INVISIBLE, 0, SettingStoreType.Global));
	public static final SettingInt dynamicZoomLevelMax = (SettingInt) SettingsList.addSetting(new SettingInt("dynamicZoomLevelMax",
			SettingCategory.CarMode, NORMAL, 17, SettingStoreType.Global));
	public static final SettingInt dynamicZoomLevelMin = (SettingInt) SettingsList.addSetting(new SettingInt("dynamicZoomLevelMin",
			SettingCategory.CarMode, NORMAL, 15, SettingStoreType.Global));

	public static final SettingInt conection_timeout = (SettingInt) SettingsList.addSetting(new SettingInt("conection_timeout",
			SettingCategory.Internal, INVISIBLE, 10000, SettingStoreType.Global));
	public static final SettingInt socket_timeout = (SettingInt) SettingsList.addSetting(new SettingInt("socket_timeout",
			SettingCategory.Internal, INVISIBLE, 60000, SettingStoreType.Global));

	// String
	public static final SettingString CurrentMapLayer = (SettingString) SettingsList.addSetting(new SettingString("CurrentMapLayer",
			SettingCategory.Map, EXPERT, "Mapnik", SettingStoreType.Global));
	public static final SettingString CurrentMapOverlayLayer = (SettingString) SettingsList.addSetting(new SettingString(
			"CurrentMapOverlayLayer", SettingCategory.Map, EXPERT, "", SettingStoreType.Global));
	public static final SettingString LastSelectedCache = (SettingString) SettingsList.addSetting(new SettingString("LastSelectedCache",
			SettingCategory.Misc, NEVER, "", SettingStoreType.Local));
	public static final SettingString NavigationProvider = (SettingString) SettingsList.addSetting(new SettingString("NavigationProvider",
			SettingCategory.Internal, INVISIBLE, "http://openrouteservice.org/php/OpenLSRS_DetermineRoute.php", SettingStoreType.Global));
	public static final SettingString FoundTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("FoundTemplate",
			SettingCategory.Templates, NORMAL, FOUND, SettingStoreType.Global));
	public static final SettingString DNFTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("DNFTemplate",
			SettingCategory.Templates, NORMAL, DNF, SettingStoreType.Global));
	public static final SettingString NeedsMaintenanceTemplate = (SettingString) SettingsList.addSetting(new SettingLongString(
			"NeedsMaintenanceTemplate", SettingCategory.Templates, NORMAL, LOG, SettingStoreType.Global));
	public static final SettingString AddNoteTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("AddNoteTemplate",
			SettingCategory.Templates, NORMAL, LOG, SettingStoreType.Global));
	public static final SettingString DiscoverdTemplate = (SettingString) SettingsList.addSetting(new SettingLongString(
			"DiscoverdTemplate", SettingCategory.Templates, NORMAL, DISCOVERD, SettingStoreType.Global));
	public static final SettingString VisitedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("VisitedTemplate",
			SettingCategory.Templates, NORMAL, VISITED, SettingStoreType.Global));
	public static final SettingString DroppedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("DroppedTemplate",
			SettingCategory.Templates, NORMAL, DROPPED, SettingStoreType.Global));
	public static final SettingString GrabbedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("GrabbedTemplate",
			SettingCategory.Templates, NORMAL, GRABED, SettingStoreType.Global));
	public static final SettingString PickedTemplate = (SettingString) SettingsList.addSetting(new SettingLongString("PickedTemplate",
			SettingCategory.Templates, NORMAL, PICKED, SettingStoreType.Global));
	public static final SettingString SpoilersDescriptionTags = (SettingString) SettingsList.addSetting(new SettingString(
			"SpoilersDescriptionTags", SettingCategory.Internal, INVISIBLE, "", SettingStoreType.Global));
	public static final SettingString quickButtonList = (SettingString) SettingsList.addSetting(new SettingString("quickButtonList",
			SettingCategory.QuickList, INVISIBLE, "5,0,1,3,2", SettingStoreType.Global));
	public static final SettingString GcJoker = (SettingString) SettingsList.addSetting(new SettingString("GcJoker", SettingCategory.Login,
			NORMAL, "", SettingStoreType.Platform));

	// ArrayInt
	public static final SettingIntArray ZoomCross = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("ZoomCross",
			SettingCategory.Map, NORMAL, 16, SettingStoreType.Global, CrossLevel));
	public static final SettingIntArray SoundApproachDistance = (SettingIntArray) SettingsList.addSetting(new SettingIntArray(
			"SoundApproachDistance", SettingCategory.Misc, NORMAL, 50, SettingStoreType.Global, approach));
	public static final SettingIntArray TrackDistance = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("TrackDistance",
			SettingCategory.Misc, NORMAL, 3, SettingStoreType.Global, TrackDistanceArray));
	public static final SettingIntArray OsmMinLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("OsmMinLevel",
			SettingCategory.Map, NORMAL, 7, SettingStoreType.Global, Level));
	public static final SettingIntArray OsmMaxImportLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray(
			"OsmMaxImportLevel", SettingCategory.Map, INVISIBLE, 16, SettingStoreType.Global, Level));
	public static final SettingIntArray OsmMaxLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray("OsmMaxLevel",
			SettingCategory.Map, NORMAL, 19, SettingStoreType.Global, Level));
	public static final SettingIntArray CompassMapMaxZommLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray(
			"CompassMapMaxZommLevel", SettingCategory.Map, NORMAL, 20, SettingStoreType.Global, Level));
	public static final SettingIntArray CompassMapMinZoomLevel = (SettingIntArray) SettingsList.addSetting(new SettingIntArray(
			"CompassMapMinZoomLevel", SettingCategory.Map, NORMAL, 13, SettingStoreType.Global, Level));

	// double
	public static final SettingDouble MapInitLatitude = (SettingDouble) SettingsList.addSetting(new SettingDouble("MapInitLatitude",
			SettingCategory.Positions, EXPERT, -1000, SettingStoreType.Global));
	public static final SettingDouble MapInitLongitude = (SettingDouble) SettingsList.addSetting(new SettingDouble("MapInitLongitude",
			SettingCategory.Positions, EXPERT, -1000, SettingStoreType.Global));
	public static final SettingDouble ParkingLatitude = (SettingDouble) SettingsList.addSetting(new SettingDouble("ParkingLatitude",
			SettingCategory.Positions, EXPERT, 0, SettingStoreType.Global));
	public static final SettingDouble ParkingLongitude = (SettingDouble) SettingsList.addSetting(new SettingDouble("ParkingLongitude",
			SettingCategory.Positions, EXPERT, 0, SettingStoreType.Global));

	// longString
	public static final SettingLongString Filter = (SettingLongString) SettingsList.addSetting(new SettingLongString("Filter",
			SettingCategory.Misc, NEVER, FilterProperties.presets[0].toString(), SettingStoreType.Local));
	public static final SettingLongString FilterNew = (SettingLongString) SettingsList.addSetting(new SettingLongString("FilterNew",
			SettingCategory.Misc, NEVER, "", SettingStoreType.Local));
	public static final SettingLongString UserFilter = (SettingLongString) SettingsList.addSetting(new SettingLongString("UserFilter",
			SettingCategory.Misc, NEVER, "", SettingStoreType.Global));
	public static final SettingLongString UserFilterNew = (SettingLongString) SettingsList.addSetting(new SettingLongString(
			"UserFilterNew", SettingCategory.Misc, NEVER, "", SettingStoreType.Global));

	public static final SettingTime ScreenLock = (SettingTime) SettingsList.addSetting(new SettingTime("ScreenLock", SettingCategory.Misc,
			NEVER, 60000, SettingStoreType.Global));

	public static final SettingEncryptedString GcVotePassword = (SettingEncryptedString) SettingsList
			.addSetting(new SettingEncryptedString("GcVotePassword", SettingCategory.Login, NORMAL, "", SettingStoreType.Platform));

	// AudioSettings

	public static final SettingsAudio Approach = (SettingsAudio) SettingsList.addSetting(new SettingsAudio("Approach",
			SettingCategory.Sounds, NORMAL, new Audio("data/sound/Approach.ogg", false, false, 1.0f), SettingStoreType.Global));

	public static final SettingsAudio GPS_lose = (SettingsAudio) SettingsList.addSetting(new SettingsAudio("GPS_lose",
			SettingCategory.Sounds, NORMAL, new Audio("data/sound/GPS_lose.ogg", false, false, 1.0f), SettingStoreType.Global));

	public static final SettingsAudio GPS_fix = (SettingsAudio) SettingsList.addSetting(new SettingsAudio("GPS_fix",
			SettingCategory.Sounds, NORMAL, new Audio("data/sound/GPS_Fix.ogg", false, false, 1.0f), SettingStoreType.Global));

	public static final SettingsAudio AutoResortSound = (SettingsAudio) SettingsList.addSetting(new SettingsAudio("AutoResortSound",
			SettingCategory.Sounds, NORMAL, new Audio("data/sound/AutoResort.ogg", false, false, 1.0f), SettingStoreType.Global));

}
