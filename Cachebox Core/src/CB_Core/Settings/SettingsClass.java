package CB_Core.Settings;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;

public class SettingsClass extends SettingsList
{

	private static final long serialVersionUID = 7330937438116889415L;

	private final String FOUND = "<br>###finds##, ##time##, Found it with Cachebox!";
	private final String DNF = "<br>##time##. Logged it with Cachebox!";
	private final String LOG = "Logged it with Cachebox!";
	private final String DISCOVERD = "<br> ##time##, Discovered it with Cachebox!";
	private final String VISITED = "<br> ##time##, Visited it with Cachebox!";
	private final String DROPPED = "<br> ##time##, Dropped off with Cachebox!";
	private final String PICKED = "<br> ##time##, Picked it with Cachebox!";
	private final String GRABED = "<br> ##time##, Grabed it with Cachebox!";

	// Settings Compass
	public SettingBool HardwareCompass;
	public SettingInt HardwareCompassLevel;

	// Settings Map
	public SettingBool MapHideMyFinds;
	public SettingBool ShowAllWaypoints;

	// Invisible
	public SettingLongString Filter;
	public SettingLongString FilterNew;
	public SettingLongString UserFilter;
	public SettingLongString UserFilterNew;

	// Folder
	public SettingFolder UserImageFolder;
	public SettingFolder LanguagePath;
	public SettingFolder SoundPath;
	public SettingFolder TileCacheFolder;
	public SettingFolder TileCacheFolderLocal;
	public SettingFolder PocketQueryFolder;
	public SettingFolder DescriptionImageFolder;
	public SettingFolder DescriptionImageFolderLocal;
	public SettingFolder MapPackFolder;
	public SettingFolder MapPackFolderLocal;
	public SettingFolder SpoilerFolder;
	public SettingFolder SpoilerFolderLocal;
	public SettingFolder TrackFolder;
	public SettingFolder SkinFolder;

	// Files
	public SettingFile Sel_LanguagePath;
	public SettingFile DatabasePath;
	public SettingFile FieldNotesGarminPath;
	public SettingFile MapsforgeDayTheme;
	public SettingFile MapsforgeNightTheme;

	// Bool
	public SettingBool useMipMap;
	public SettingBool ImportGpx;
	public SettingBool CacheMapData;
	public SettingBool CacheImageData;
	public SettingBool CacheSpoilerData;
	public SettingBool SuppressPowerSaving;
	public SettingBool PlaySounds;
	// public SettingBool PopSkipOutdatedGpx;
	public SettingBool MapShowRating;
	public SettingBool MapShowDT;
	public SettingBool MapShowTitles;
	// public SettingBool ShowKeypad;
	public SettingBool ImportLayerOsm;
	public SettingBool TrackRecorderStartup;
	public SettingBool MapShowCompass;
	public SettingBool CompassNorthOriented;
	public SettingBool MapNorthOriented;

	public SettingBool GCAutoSyncCachesFound;
	public SettingBool GCAdditionalImageDownload;
	public SettingBool StartWithAutoSelect;
	public SettingBool FieldnotesUploadAll;
	public SettingBool MultiDBAsk;
	// public SettingBool AllowLandscape;
	public SettingBool MoveMapCenterWithSpeed;
	// public SettingBool PremiumMember;
	public SettingBool SearchWithoutFounds;
	public SettingBool SearchWithoutOwns;
	public SettingBool SearchOnlyAvible;
	public SettingBool quickButtonShow;
	public SettingBool DebugShowPanel;
	public SettingBool DebugMemory;
	public SettingBool DebugShowMsg;
	public SettingBool DebugMode;
	public SettingBool nightMode;
	public SettingBool DebugShowLog;
	public SettingBool DescriptionNoAttributes;
	public SettingBool quickButtonLastShow;
	public SettingBool newInstall;
	public SettingBool ImperialUnits;
	public SettingBool ShowDirektLine;
	public SettingBool ShowAccuracyCircle;
	public SettingBool ShowMapCenterCross;
	public SettingBool PositionMarkerTransparent;
	public SettingBool StagingAPI;

	public SettingBool DebugShowMarker;
	public SettingBool ImportRatings;
	public SettingBool ImportPQsFromGeocachingCom;
	public SettingBool SettingsShowExpert;
	public SettingBool SettingsShowAll;
	public SettingBool switchViewApproach;
	public SettingBool hasCallPermission;
	public SettingBool vibrateFeedback;
	public SettingBool hasPQ_PlugIn;
	public SettingBool hasFTF_PlugIn;
	public SettingBool dynamicZoom;
	public SettingBool dynamicFilterAtSearch;
	public SettingBool CompassShowMap;
	public SettingBool CompassShowWP_Name;
	public SettingBool CompassShowWP_Icon;
	public SettingBool CompassShowAttributes;
	public SettingBool CompassShowGcCode;
	public SettingBool CompassShowCoords;
	public SettingBool CompassShowWpDesc;
	public SettingBool CompassShowSatInfos;
	public SettingBool CompassShowSunMoon;
	public SettingBool CompassShowTargetDirection;
	public SettingBool CompassShowSDT;
	public SettingBool CompassShowLastFound;
	public SettingBool DeleteLogs;
	public SettingBool CompactDB;

	public SettingBool RememberAsk_API_Coast;
	public SettingBool AskAgain;
	public SettingBool RememberAsk_Get_API_Key;

	public SettingBool FireMapQueueProcessorExceptions;
	public SettingBool DebugSpriteBatchCountBuffer;
	public SettingBool TB_DirectLog;

	// int
	public SettingInt LogMaxMonthAge;
	public SettingInt LogMinCount;
	public SettingInt installRev;
	public SettingInt MapIniWidth;
	public SettingInt MapIniHeight;
	public SettingInt LongClicktime;
	public SettingInt VibrateTime;

	public SettingInt FONT_SIZE_COMPASS_DISTANCE;
	public SettingInt FONT_SIZE_BIG;
	public SettingInt FONT_SIZE_NORMAL;
	public SettingInt FONT_SIZE_NORMAL_BUBBLE;
	public SettingInt FONT_SIZE_SMALL;
	public SettingInt FONT_SIZE_SMALL_BUBBLE;

	// public SettingInt OsmCoverage;
	public SettingInt FoundOffset;
	public SettingInt MapMaxCachesLabel;
	public SettingInt MapMaxCachesDisplay_config;
	public SettingInt mapMaxCachesDisplayLarge_config;
	public SettingInt MultiDBAutoStartTime;
	public SettingInt MoveMapCenterMaxSpeed;
	public SettingInt lastZoomLevel;
	public SettingInt lastSearchRadius;
	public SettingInt LastMapToggleBtnState;
	public SettingInt dynamicZoomLevelMax;
	public SettingInt dynamicZoomLevelMin;
	public SettingInt gpsUpdateTime;
	public SettingInt conection_timeout;
	public SettingInt socket_timeout;

	public SettingTime ScreenLock;

	// double
	public SettingDouble MapInitLatitude;
	public SettingDouble MapInitLongitude;
	public SettingDouble ParkingLatitude;
	public SettingDouble ParkingLongitude;
	public SettingDouble MapViewDPIFaktor;
	public SettingDouble MapViewFontFaktor;

	// String
	public SettingString CurrentMapLayer;
	public SettingString CurrentMapOverlayLayer;
	public SettingString LastSelectedCache;
	public SettingString NavigationProvider;
	public SettingString FoundTemplate;
	public SettingString DNFTemplate;
	public SettingString NeedsMaintenanceTemplate;
	public SettingString AddNoteTemplate;
	public SettingString DiscoverdTemplate;
	public SettingString VisitedTemplate;
	public SettingString DroppedTemplate;
	public SettingString GrabbedTemplate;
	public SettingString PickedTemplate;

	public SettingString SpoilersDescriptionTags;
	public SettingString quickButtonList;
	public SettingString GcLogin;
	public SettingString GcJoker;
	public SettingString OverrideUrl;
	// public SettingString PopHost;

	// Decrypt
	public SettingEncryptedString GcAPI;
	public SettingEncryptedString GcAPIStaging;
	public SettingEncryptedString GcVotePassword;

	// Enums
	// public SettingEnum<SmoothScrollingTyp> SmoothScrolling;

	// ArrayInt
	public SettingIntArray ZoomCross;
	public SettingIntArray SoundApproachDistance;
	public SettingIntArray TrackDistance;
	public SettingIntArray OsmMinLevel;
	public SettingIntArray OsmMaxImportLevel;
	public SettingIntArray OsmMaxLevel;

	public SettingIntArray CompassMapMaxZommLevel;
	public SettingIntArray CompassMapMinZoomLevel;

	public Integer Level[] = new Integer[21];
	public Integer CrossLevel[] = new Integer[8];
	public Integer[] approach = new Integer[]
		{ 0, 2, 10, 25, 50, 100, 200, 500, 1000 };
	public Integer[] TrackDistanceArray = new Integer[]
		{ 1, 3, 5, 10, 20 };

	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	private final SettingModus INVISIBLE = SettingModus.Invisible;
	private final SettingModus NORMAL = SettingModus.Normal;
	private final SettingModus EXPERT = SettingModus.Expert;
	private final SettingModus NEVER = SettingModus.Never;

	public SettingsClass()
	{

		for (int i = 0; i < 22; i++)
		{
			if (i < 21) Level[i] = i;

			if (i > 13) CrossLevel[i - 14] = i;

		}

		addMapSettings();
		addCarModeSettings();
		addLogInSettings();
		addFolderSettings();
		addGpsSettings();
		addCompassSettings();
		addMiscSettings();
		addTemplateSettings();
		addInternalSettings();
		addAPISettings();
		addSkinSettings();
		addQuickbuttonsSettings();
		addDebugSettings();
		addPositionSettings();
		addRememberAsk();
	}

	private void addCarModeSettings()
	{
		SettingCategory cat = SettingCategory.CarMode;

		addSetting(MoveMapCenterWithSpeed = new SettingBool("MoveMapCenterWithSpeed", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(MoveMapCenterMaxSpeed = new SettingInt("MoveMapCenterMaxSpeed", cat, NORMAL, 60, SettingStoreType.Global));

		addSetting(dynamicZoom = new SettingBool("dynamicZoom", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(dynamicZoomLevelMax = new SettingInt("dynamicZoomLevelMax", cat, NORMAL, 17, SettingStoreType.Global));
		addSetting(dynamicZoomLevelMin = new SettingInt("dynamicZoomLevelMin", cat, NORMAL, 15, SettingStoreType.Global));

	}

	private void addMiscSettings()
	{
		SettingCategory cat = SettingCategory.Misc;

		addSetting(Filter = new SettingLongString("Filter", cat, NEVER, FilterProperties.presets[0].toString(), SettingStoreType.Local));
		addSetting(UserFilter = new SettingLongString("UserFilter", cat, NEVER, "", SettingStoreType.Global));
		addSetting(FilterNew = new SettingLongString("FilterNew", cat, NEVER, "", SettingStoreType.Local));
		addSetting(UserFilterNew = new SettingLongString("UserFilterNew", cat, NEVER, "", SettingStoreType.Global));
		addSetting(LastSelectedCache = new SettingString("LastSelectedCache", cat, NEVER, "", SettingStoreType.Local));
		addSetting(FoundOffset = new SettingInt("FoundOffset", cat, NEVER, 0, SettingStoreType.Global));
		addSetting(TrackDistance = new SettingIntArray("TrackDistance", cat, NORMAL, 3, SettingStoreType.Global, TrackDistanceArray));
		addSetting(SoundApproachDistance = new SettingIntArray("SoundApproachDistance", cat, NORMAL, 50, SettingStoreType.Global, approach));
		addSetting(TrackRecorderStartup = new SettingBool("TrackRecorderStartup", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(DescriptionNoAttributes = new SettingBool("DescriptionNoAttributes", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(switchViewApproach = new SettingBool("switchViewApproach", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(PlaySounds = new SettingBool("PlaySounds", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(SuppressPowerSaving = new SettingBool("SuppressPowerSaving", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(ImperialUnits = new SettingBool("ImperialUnits", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(ScreenLock = new SettingTime("ScreenLock", cat, NEVER, 60000, SettingStoreType.Global));

		addSetting(MapViewDPIFaktor = new SettingDouble("MapViewDPIFaktor", SettingCategory.Map, EXPERT, GlobalCore.displayDensity,
				SettingStoreType.Global));
		addSetting(MapViewFontFaktor = new SettingDouble("MapViewFontFaktor", SettingCategory.Map, NEVER, 1.0, SettingStoreType.Global));// TODO
																																			// 0.6
																																			// Set
																																			// to
		// EXPERT
		addSetting(vibrateFeedback = new SettingBool("vibrateFeedback", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(VibrateTime = new SettingInt("VibrateTime", cat, NORMAL, 20, SettingStoreType.Global));
		addSetting(LongClicktime = new SettingInt("LongClicktime", cat, NORMAL, 600, SettingStoreType.Global));
		addSetting(dynamicFilterAtSearch = new SettingBool("dynamicFilterAtSearch", cat, NEVER, true, SettingStoreType.Global));
		addSetting(StartWithAutoSelect = new SettingBool("StartWithAutoSelect", cat, EXPERT, false, SettingStoreType.Global));

	}

	private void addTemplateSettings()
	{
		SettingCategory cat = SettingCategory.Templates;

		addSetting(FoundTemplate = new SettingLongString("FoundTemplate", cat, NORMAL, FOUND, SettingStoreType.Global));
		addSetting(DNFTemplate = new SettingLongString("DNFTemplate", cat, NORMAL, DNF, SettingStoreType.Global));
		addSetting(NeedsMaintenanceTemplate = new SettingLongString("NeedsMaintenanceTemplate", cat, NORMAL, LOG, SettingStoreType.Global));
		addSetting(AddNoteTemplate = new SettingLongString("AddNoteTemplate", cat, NORMAL, LOG, SettingStoreType.Global));
		addSetting(DiscoverdTemplate = new SettingLongString("DiscoverdTemplate", cat, NORMAL, DISCOVERD, SettingStoreType.Global));
		addSetting(VisitedTemplate = new SettingLongString("VisitedTemplate", cat, NORMAL, VISITED, SettingStoreType.Global));
		addSetting(DroppedTemplate = new SettingLongString("DroppedTemplate", cat, NORMAL, DROPPED, SettingStoreType.Global));
		addSetting(GrabbedTemplate = new SettingLongString("GrabbedTemplate", cat, NORMAL, GRABED, SettingStoreType.Global));
		addSetting(PickedTemplate = new SettingLongString("PickedTemplate", cat, NORMAL, PICKED, SettingStoreType.Global));
	}

	private void addGpsSettings()
	{
		SettingCategory cat = SettingCategory.Gps;

		addSetting(HardwareCompassLevel = new SettingInt("HardwareCompassLevel", cat, NORMAL, 5, SettingStoreType.Global));
		addSetting(HardwareCompass = new SettingBool("HardwareCompass", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(gpsUpdateTime = new SettingInt("gpsUpdateTime", cat, NORMAL, 500, SettingStoreType.Global));
	}

	private void addCompassSettings()
	{
		SettingCategory cat = SettingCategory.Compass;

		addSetting(CompassShowMap = new SettingBool("CompassShowMap", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(CompassShowWP_Name = new SettingBool("CompassShowWP_Name", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(CompassShowWP_Icon = new SettingBool("CompassShowWP_Icon", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(CompassShowAttributes = new SettingBool("CompassShowAttributes", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(CompassShowGcCode = new SettingBool("CompassShowGcCode", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(CompassShowCoords = new SettingBool("CompassShowCoords", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(CompassShowWpDesc = new SettingBool("CompassShowWpDesc", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(CompassShowSatInfos = new SettingBool("CompassShowSatInfos", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(CompassShowSunMoon = new SettingBool("CompassShowSunMoon", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(CompassShowSDT = new SettingBool("CompassSDT", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(CompassShowLastFound = new SettingBool("CompassShowLastFound", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(CompassShowTargetDirection = new SettingBool("CompassShowTargetDirection", cat, NORMAL, false, SettingStoreType.Global));
	}

	private void addPositionSettings()
	{
		SettingCategory cat = SettingCategory.Positions;

		addSetting(MapInitLatitude = new SettingDouble("MapInitLatitude", cat, EXPERT, -1000, SettingStoreType.Global));
		addSetting(MapInitLongitude = new SettingDouble("MapInitLongitude", cat, EXPERT, -1000, SettingStoreType.Global));
		addSetting(ParkingLatitude = new SettingDouble("ParkingLatitude", cat, EXPERT, 0, SettingStoreType.Global));
		addSetting(ParkingLongitude = new SettingDouble("ParkingLongitude", cat, EXPERT, 0, SettingStoreType.Global));

	}

	private void addMapSettings()
	{
		SettingCategory cat = SettingCategory.Map;

		addSetting(ZoomCross = new SettingIntArray("ZoomCross", cat, NORMAL, 16, SettingStoreType.Global, CrossLevel));
		addSetting(OsmMaxLevel = new SettingIntArray("OsmMaxLevel", cat, NORMAL, 19, SettingStoreType.Global, Level));
		addSetting(OsmMinLevel = new SettingIntArray("OsmMinLevel", cat, NORMAL, 7, SettingStoreType.Global, Level));

		addSetting(CompassMapMaxZommLevel = new SettingIntArray("CompassMapMaxZommLevel", cat, NORMAL, 20, SettingStoreType.Global, Level));
		addSetting(CompassMapMinZoomLevel = new SettingIntArray("CompassMapMinZoomLevel", cat, NORMAL, 13, SettingStoreType.Global, Level));

		addSetting(ShowMapCenterCross = new SettingBool("ShowMapCenterCross", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(ShowDirektLine = new SettingBool("ShowDirektLine", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(ShowAccuracyCircle = new SettingBool("ShowAccuracyCircle", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(MapHideMyFinds = new SettingBool("MapHideMyFinds", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(ShowAllWaypoints = new SettingBool("ShowAllWaypoints", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(MapShowRating = new SettingBool("MapShowRating", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(MapShowDT = new SettingBool("MapShowDT", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(MapShowTitles = new SettingBool("MapShowTitles", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(PositionMarkerTransparent = new SettingBool("PositionMarkerTransparent", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(MapShowCompass = new SettingBool("MapShowCompass", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(CompassNorthOriented = new SettingBool("CompassNorthOriented", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(MapNorthOriented = new SettingBool("MapNorthOriented", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(LastMapToggleBtnState = new SettingInt("LastMapToggleBtnState", cat, INVISIBLE, 0, SettingStoreType.Global));

		addSetting(CurrentMapLayer = new SettingString("CurrentMapLayer", cat, EXPERT, "Mapnik", SettingStoreType.Global));
		addSetting(CurrentMapOverlayLayer = new SettingString("CurrentMapOverlayLayer", cat, EXPERT, "", SettingStoreType.Global));

		addSetting(MapMaxCachesDisplay_config = new SettingInt("MapMaxCachesDisplay_config", cat, INVISIBLE, 10000, SettingStoreType.Global));
		addSetting(lastZoomLevel = new SettingInt("lastZoomLevel", cat, INVISIBLE, 14, SettingStoreType.Global));
		addSetting(mapMaxCachesDisplayLarge_config = new SettingInt("mapMaxCachesDisplayLarge_config", cat, INVISIBLE, 75,
				SettingStoreType.Global));
		addSetting(MapMaxCachesLabel = new SettingInt("MapMaxCachesLabel", cat, INVISIBLE, 12, SettingStoreType.Global));
		addSetting(OsmMaxImportLevel = new SettingIntArray("OsmMaxImportLevel", cat, INVISIBLE, 16, SettingStoreType.Global, Level));

		addSetting(MapIniWidth = new SettingInt("MapIniWidth", cat, INVISIBLE, 480, SettingStoreType.Global));
		addSetting(MapIniHeight = new SettingInt("MapIniHeight", cat, INVISIBLE, 535, SettingStoreType.Global));

	}

	private void addLogInSettings()
	{
		SettingCategory cat = SettingCategory.Login;

		addSetting(GcAPI = new SettingEncryptedString("GcAPI", cat, INVISIBLE, "", SettingStoreType.Global));
		addSetting(GcAPIStaging = new SettingEncryptedString("GcAPIStaging", cat, INVISIBLE, "", SettingStoreType.Global));

		addSetting(GcVotePassword = new SettingEncryptedString("GcVotePassword", cat, NORMAL, "", SettingStoreType.Global));
		addSetting(GcLogin = new SettingString("GcLogin", cat, NORMAL, "", SettingStoreType.Global));
		addSetting(GcJoker = new SettingString("GcJoker", cat, NORMAL, "", SettingStoreType.Global));
	}

	private void addFolderSettings()
	{
		SettingCategory cat = SettingCategory.Folder;

		String Work = Config.WorkPath;

		addSetting(UserImageFolder = new SettingFolder("UserImageFolder", cat, NORMAL, Work + "/User/Media", SettingStoreType.Global));
		addSetting(LanguagePath = new SettingFolder("LanguagePath", cat, NEVER, Work + "/data/lang", SettingStoreType.Global));
		addSetting(SoundPath = new SettingFolder("SoundPath", cat, INVISIBLE, Work + "/data/sound", SettingStoreType.Global));
		addSetting(PocketQueryFolder = new SettingFolder("PocketQueryFolder", cat, INVISIBLE, Work + "/PocketQuery",
				SettingStoreType.Global));

		addSetting(DescriptionImageFolder = new SettingFolder("DescriptionImageFolder", cat, EXPERT, Work + "/repository/images",
				SettingStoreType.Global));
		addSetting(DescriptionImageFolderLocal = new SettingFolder("DescriptionImageFolder", cat, NEVER, "", SettingStoreType.Local));

		addSetting(SpoilerFolder = new SettingFolder("SpoilerFolder", cat, EXPERT, Work + "/repository/spoilers", SettingStoreType.Global));
		addSetting(SpoilerFolderLocal = new SettingFolder("SpoilerFolder", cat, NEVER, "", SettingStoreType.Local));

		addSetting(TileCacheFolder = new SettingFolder("TileCacheFolder", cat, NORMAL, Work + "/repository/cache", SettingStoreType.Global));
		addSetting(TileCacheFolderLocal = new SettingFolder("TileCacheFolder", cat, NEVER, "", SettingStoreType.Local));

		addSetting(MapPackFolder = new SettingFolder("MapPackFolder", cat, NORMAL, Work + "/repository/maps", SettingStoreType.Global));
		addSetting(MapPackFolderLocal = new SettingFolder("MapPackFolder", cat, NEVER, "", SettingStoreType.Local));

		addSetting(TrackFolder = new SettingFolder("TrackFolder", cat, NORMAL, Work + "/User/Tracks", SettingStoreType.Global));

		addSetting(Sel_LanguagePath = new SettingFile("Sel_LanguagePath", cat, NEVER, Work + "/data/lang/en-GB/strings.ini",
				SettingStoreType.Global, "lan"));
		addSetting(DatabasePath = new SettingFile("DatabasePath", cat, NEVER, Work + "/cachebox.db3", SettingStoreType.Global, "db3"));
		addSetting(FieldNotesGarminPath = new SettingFile("FieldNotesGarminPath", cat, INVISIBLE, Work + "/User/geocache_visits.txt",
				SettingStoreType.Global));

		addSetting(SkinFolder = new SettingFolder("SkinFolder", cat, INVISIBLE, Work + "/skins/default", SettingStoreType.Global));
	}

	private void addQuickbuttonsSettings()
	{
		SettingCategory cat = SettingCategory.QuickList;
		addSetting(quickButtonShow = new SettingBool("quickButtonShow", cat, NORMAL, true, SettingStoreType.Global));
		addSetting(quickButtonLastShow = new SettingBool("quickButtonLastShow", cat, INVISIBLE, false, SettingStoreType.Global));
		addSetting(quickButtonList = new SettingString("quickButtonList", cat, INVISIBLE, "5,0,1,3,2", SettingStoreType.Global));

	}

	private void addInternalSettings()
	{
		SettingCategory cat = SettingCategory.Internal;

		addSetting(SettingsShowExpert = new SettingBool("SettingsShowExpert", cat, NEVER, false, SettingStoreType.Global));
		addSetting(SettingsShowAll = new SettingBool("SettingsShowAll", cat, NEVER, false, SettingStoreType.Global));
		addSetting(nightMode = new SettingBool("nightMode", cat, NEVER, false, SettingStoreType.Global));

		addSetting(ImportGpx = new SettingBool("ImportGpx", cat, INVISIBLE, true, SettingStoreType.Global));
		addSetting(CacheMapData = new SettingBool("CacheMapData", cat, INVISIBLE, false, SettingStoreType.Global));
		addSetting(CacheImageData = new SettingBool("CacheImageData", cat, INVISIBLE, true, SettingStoreType.Global));
		addSetting(CacheSpoilerData = new SettingBool("CacheSpoilerData", cat, INVISIBLE, true, SettingStoreType.Global));

		addSetting(ImportLayerOsm = new SettingBool("ImportLayerOsm", cat, NEVER, true, SettingStoreType.Global));
		addSetting(GCAutoSyncCachesFound = new SettingBool("GCAutoSyncCachesFound", cat, NEVER, true, SettingStoreType.Global));

		addSetting(MultiDBAsk = new SettingBool("MultiDBAsk", cat, NEVER, true, SettingStoreType.Global));

		addSetting(newInstall = new SettingBool("newInstall", cat, NEVER, false, SettingStoreType.Global));

		addSetting(LogMaxMonthAge = new SettingInt("LogMaxMonthAge", cat, INVISIBLE, 6, SettingStoreType.Global));
		addSetting(LogMinCount = new SettingInt("LogMinCount", cat, INVISIBLE, 99999, SettingStoreType.Global));
		addSetting(DeleteLogs = new SettingBool("DeleteLogs", cat, INVISIBLE, false, SettingStoreType.Global));
		addSetting(CompactDB = new SettingBool("CompactDB", cat, INVISIBLE, false, SettingStoreType.Global));

		addSetting(installRev = new SettingInt("installRev", cat, NEVER, 0, SettingStoreType.Global));

		addSetting(MultiDBAutoStartTime = new SettingInt("MultiDBAutoStartTime", cat, NEVER, 0, SettingStoreType.Global));

		addSetting(NavigationProvider = new SettingString("NavigationProvider", cat, INVISIBLE,
				"http://openrouteservice.org/php/OpenLSRS_DetermineRoute.php", SettingStoreType.Global));

		addSetting(SpoilersDescriptionTags = new SettingString("SpoilersDescriptionTags", cat, INVISIBLE, "", SettingStoreType.Global));

		addSetting(hasCallPermission = new SettingBool("hasCallPermission", cat, NEVER, false, SettingStoreType.Global));
		addSetting(hasPQ_PlugIn = new SettingBool("hasPQ_PlugIn", cat, NEVER, false, SettingStoreType.Global));
		addSetting(hasFTF_PlugIn = new SettingBool("hasFTF_PlugIn", cat, NEVER, false, SettingStoreType.Global));
		addSetting(FireMapQueueProcessorExceptions = new SettingBool("FireMapQueueProcessorExceptions", cat, INVISIBLE, false,
				SettingStoreType.Global));
		addSetting(TB_DirectLog = new SettingBool("TB_DirectLog", cat, NEVER, true, SettingStoreType.Platform));

		addSetting(conection_timeout = new SettingInt("conection_timeout", cat, INVISIBLE, 5000, SettingStoreType.Global));
		addSetting(socket_timeout = new SettingInt("socket_timeout", cat, INVISIBLE, 30000, SettingStoreType.Global));

	}

	private void addAPISettings()
	{
		SettingCategory cat = SettingCategory.API;

		addSetting(ImportRatings = new SettingBool("ImportRatings", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(ImportGpx = new SettingBool("ImportGpx", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(GCAdditionalImageDownload = new SettingBool("GCAdditionalImageDownload", cat, EXPERT, false, SettingStoreType.Global));
		// addSetting(GCRequestDelay = new SettingInt("GCRequestDelay", cat, EXPERT, 0, true));
		addSetting(ImportPQsFromGeocachingCom = new SettingBool("ImportPQsFromGeocachingCom", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(FieldnotesUploadAll = new SettingBool("FieldnotesUploadAll", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(SearchWithoutFounds = new SettingBool("SearchWithoutFounds", cat, INVISIBLE, true, SettingStoreType.Global));
		addSetting(SearchWithoutOwns = new SettingBool("SearchWithoutOwns", cat, INVISIBLE, true, SettingStoreType.Global));
		addSetting(SearchOnlyAvible = new SettingBool("SearchOnlyAvible", cat, INVISIBLE, true, SettingStoreType.Global));
		addSetting(lastSearchRadius = new SettingInt("lastSearchRadius", cat, INVISIBLE, 5, SettingStoreType.Global));
	}

	private void addSkinSettings()
	{
		SettingCategory cat = SettingCategory.Skin;

		addSetting(MapsforgeDayTheme = new SettingFile("MapsforgeDayTheme", cat, NORMAL, "", SettingStoreType.Global, "xml"));
		addSetting(MapsforgeNightTheme = new SettingFile("MapsforgeNightTheme", cat, NORMAL, "", SettingStoreType.Global, "xml"));
		addSetting(useMipMap = new SettingBool("useMipMap", cat, NORMAL, false, SettingStoreType.Global));

		addSetting(FONT_SIZE_COMPASS_DISTANCE = new SettingInt("FONT_SIZE_COMPASS_DISTANCE", cat, EXPERT, 27, SettingStoreType.Global));
		addSetting(FONT_SIZE_BIG = new SettingInt("FONT_SIZE_BIG", cat, EXPERT, 18, SettingStoreType.Global));
		addSetting(FONT_SIZE_NORMAL = new SettingInt("FONT_SIZE_NORMAL", cat, EXPERT, 15, SettingStoreType.Global));
		addSetting(FONT_SIZE_NORMAL_BUBBLE = new SettingInt("FONT_SIZE_NORMAL_BUBBLE", cat, EXPERT, 14, SettingStoreType.Global));
		addSetting(FONT_SIZE_SMALL = new SettingInt("FONT_SIZE_SMALL", cat, EXPERT, 13, SettingStoreType.Global));
		addSetting(FONT_SIZE_SMALL_BUBBLE = new SettingInt("FONT_SIZE_SMALL_BUBBLE", cat, EXPERT, 11, SettingStoreType.Global));
	}

	private void addDebugSettings()
	{
		SettingCategory cat = SettingCategory.Debug;

		addSetting(DebugMode = new SettingBool("DebugMode", cat, EXPERT, false, SettingStoreType.Global));
		addSetting(DebugShowPanel = new SettingBool("DebugShowPanel", cat, EXPERT, false, SettingStoreType.Global));
		addSetting(DebugMemory = new SettingBool("DebugMemory", cat, EXPERT, false, SettingStoreType.Global));
		addSetting(DebugShowMsg = new SettingBool("DebugShowMsg", cat, EXPERT, false, SettingStoreType.Global));
		addSetting(DebugShowMarker = new SettingBool("DebugShowMarker", cat, EXPERT, false, SettingStoreType.Global));
		addSetting(DebugShowLog = new SettingBool("DebugShowLog", cat, EXPERT, false, SettingStoreType.Global));
		addSetting(OverrideUrl = new SettingString("OverrideUrl", cat, EXPERT, "", SettingStoreType.Global));
		addSetting(DebugSpriteBatchCountBuffer = new SettingBool("DebugSpriteBatchCountBuffer", cat, EXPERT, false, SettingStoreType.Global));
		addSetting(StagingAPI = new SettingBool("StagingAPI", cat, EXPERT, false, SettingStoreType.Global));

	}

	private void addRememberAsk()
	{
		SettingCategory cat = SettingCategory.RememberAsk;

		addSetting(RememberAsk_API_Coast = new SettingBool("RememberAsk_API_Coast", cat, NORMAL, false, SettingStoreType.Global));
		addSetting(AskAgain = new SettingBool("AskAgain", cat, NORMAL, true, SettingStoreType.Platform));
		addSetting(RememberAsk_Get_API_Key = new SettingBool("RememberAsk_Get_API_Key", cat, NORMAL, false, SettingStoreType.Global));

	}

}
