package CB_Core.Settings;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;

public class SettingsClass extends SettingsList
{

	private static final long serialVersionUID = 7330937438116889415L;

	// Settings Compass
	public SettingBool HardwareCompass;
	public SettingInt HardwareCompassLevel;

	// Settings Map
	public SettingBool MapHideMyFinds;

	// Invisible
	public SettingLongString Filter;
	public SettingLongString UserFilter;

	// Folder
	public SettingFolder UserImageFolder;
	public SettingFolder LanguagePath;
	public SettingFolder SoundPath;
	public SettingFolder TileCacheFolder;
	public SettingFolder PocketQueryFolder;
	public SettingFolder DescriptionImageFolder;
	public SettingFolder MapPackFolder;
	public SettingFolder SpoilerFolder;
	public SettingFolder TrackFolder;
	public SettingFolder SkinFolder;

	// Files
	public SettingFile Sel_LanguagePath;
	public SettingFile DatabasePath;
	public SettingFile FieldNotesGarminPath;
	public SettingFile MapsforgeDayTheme;
	public SettingFile MapsforgeNightTheme;

	// Bool

	public SettingBool ImportGpx;
	public SettingBool CacheMapData;
	public SettingBool CacheImageData;
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
	// public SettingBool ResortRepaint;
	public SettingBool GCAutoSyncCachesFound;
	public SettingBool GCAdditionalImageDownload;
	public SettingBool AutoResort;
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
	public SettingBool PositionMarkerTransparent;

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

	// int
	public SettingInt LogMaxMonthAge;
	public SettingInt LogMinCount;
	public SettingInt installRev;
	public SettingInt MapIniWidth;
	public SettingInt MapIniHeight;
	public SettingInt LongClicktime;
	public SettingInt VibrateTime;

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
	public SettingString LastSelectedCache;
	public SettingString NavigationProvider;
	public SettingString FoundTemplate;
	public SettingString DNFTemplate;
	public SettingString NeedsMaintenanceTemplate;
	public SettingString AddNoteTemplate;
	public SettingString SpoilersDescriptionTags;
	public SettingString quickButtonList;
	public SettingString GcLogin;
	public SettingString GcJoker;
	public SettingString OverrideUrl;
	// public SettingString PopHost;

	// Decrypt
	public SettingEncryptedString GcAPI;
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
		addMiscSettings();
		addTemplateSettings();
		addInternalSettings();
		addAPISettings();
		addSkinSettings();
		addQuickbuttonsSettings();
		addDebugSettings();
		addPositionSettings();
	}

	private void addCarModeSettings()
	{
		SettingCategory cat = SettingCategory.CarMode;

		addSetting(MoveMapCenterWithSpeed = new SettingBool("MoveMapCenterWithSpeed", cat, NORMAL, false, true));
		addSetting(MoveMapCenterMaxSpeed = new SettingInt("MoveMapCenterMaxSpeed", cat, NORMAL, 20, true));

		addSetting(dynamicZoom = new SettingBool("dynamicZoom", cat, NORMAL, true, true));
		addSetting(dynamicZoomLevelMax = new SettingInt("dynamicZoomLevelMax", cat, NORMAL, 16, true));
		addSetting(dynamicZoomLevelMin = new SettingInt("dynamicZoomLevelMin", cat, NORMAL, 14, true));

	}

	private void addMiscSettings()
	{
		SettingCategory cat = SettingCategory.Misc;

		addSetting(Filter = new SettingLongString("Filter", cat, NEVER, FilterProperties.presets[0].toString(), false));
		addSetting(UserFilter = new SettingLongString("UserFilter", cat, NEVER, "", true));
		addSetting(LastSelectedCache = new SettingString("LastSelectedCache", cat, NORMAL, "", false));
		addSetting(FoundOffset = new SettingInt("FoundOffset", cat, NORMAL, 0, true));
		addSetting(TrackDistance = new SettingIntArray("TrackDistance", cat, NORMAL, 3, true, TrackDistanceArray));
		addSetting(SoundApproachDistance = new SettingIntArray("SoundApproachDistance", cat, NORMAL, 50, true, approach));
		addSetting(TrackRecorderStartup = new SettingBool("TrackRecorderStartup", cat, NORMAL, false, true));
		addSetting(DescriptionNoAttributes = new SettingBool("DescriptionNoAttributes", cat, NORMAL, false, true));
		addSetting(switchViewApproach = new SettingBool("switchViewApproach", cat, NORMAL, false, true));
		addSetting(PlaySounds = new SettingBool("PlaySounds", cat, NORMAL, true, true));
		addSetting(SuppressPowerSaving = new SettingBool("SuppressPowerSaving", cat, NORMAL, true, true));
		addSetting(ImperialUnits = new SettingBool("ImperialUnits", cat, NORMAL, false, true));
		addSetting(ScreenLock = new SettingTime("ScreenLock", cat, NORMAL, 60000, true));

		addSetting(MapViewDPIFaktor = new SettingDouble("MapViewDPIFaktor", SettingCategory.Map, EXPERT, GlobalCore.displayDensity, true));
		addSetting(MapViewFontFaktor = new SettingDouble("MapViewFontFaktor", SettingCategory.Map, EXPERT, 1.0, true));
		addSetting(vibrateFeedback = new SettingBool("vibrateFeedback", cat, NORMAL, true, true));
		addSetting(VibrateTime = new SettingInt("VibrateTime", cat, NORMAL, 20, true));
		addSetting(LongClicktime = new SettingInt("LongClicktime", cat, NORMAL, 600, true));
		addSetting(dynamicFilterAtSearch = new SettingBool("dynamicFilterAtSearch", cat, NORMAL, true, true));

	}

	private void addTemplateSettings()
	{
		SettingCategory cat = SettingCategory.Templates;

		addSetting(FoundTemplate = new SettingLongString("FoundTemplate", cat, NORMAL,
				"<br>###finds##, ##time##, Found it with DroidCachebox!", true));
		addSetting(DNFTemplate = new SettingLongString("DNFTemplate", cat, NORMAL, "<br>##time##. Logged it with DroidCachebox!", true));
		addSetting(NeedsMaintenanceTemplate = new SettingLongString("NeedsMaintenanceTemplate", cat, NORMAL,
				"Logged it with DroidCachebox!", true));
		addSetting(AddNoteTemplate = new SettingLongString("AddNoteTemplate", cat, NORMAL, "Logged it with DroidCachebox!", true));
	}

	private void addGpsSettings()
	{
		SettingCategory cat = SettingCategory.Gps;

		addSetting(HardwareCompassLevel = new SettingInt("HardwareCompassLevel", cat, NORMAL, 5, true));
		addSetting(HardwareCompass = new SettingBool("HardwareCompass", cat, NORMAL, true, true));
		addSetting(gpsUpdateTime = new SettingInt("gpsUpdateTime", cat, NORMAL, 150, true));
	}

	private void addPositionSettings()
	{
		SettingCategory cat = SettingCategory.Positions;

		addSetting(MapInitLatitude = new SettingDouble("MapInitLatitude", cat, EXPERT, -1000, true));
		addSetting(MapInitLongitude = new SettingDouble("MapInitLongitude", cat, EXPERT, -1000, true));
		addSetting(ParkingLatitude = new SettingDouble("ParkingLatitude", cat, EXPERT, 0, true));
		addSetting(ParkingLongitude = new SettingDouble("ParkingLongitude", cat, EXPERT, 0, true));

	}

	private void addMapSettings()
	{
		SettingCategory cat = SettingCategory.Map;

		addSetting(ZoomCross = new SettingIntArray("ZoomCross", cat, NORMAL, 16, true, CrossLevel));
		addSetting(OsmMaxLevel = new SettingIntArray("OsmMaxLevel", cat, NORMAL, 17, true, Level));
		addSetting(OsmMinLevel = new SettingIntArray("OsmMinLevel", cat, NORMAL, 8, true, Level));

		addSetting(ShowDirektLine = new SettingBool("ShowDirektLine", cat, NORMAL, false, true));
		addSetting(MapHideMyFinds = new SettingBool("MapHideMyFinds", cat, NORMAL, false, true));
		addSetting(MapShowRating = new SettingBool("MapShowRating", cat, NORMAL, true, true));
		addSetting(MapShowDT = new SettingBool("MapShowDT", cat, NORMAL, true, true));
		addSetting(MapShowTitles = new SettingBool("MapShowTitles", cat, NORMAL, true, true));
		addSetting(PositionMarkerTransparent = new SettingBool("PositionMarkerTransparent", cat, NORMAL, false, true));
		addSetting(MapShowCompass = new SettingBool("MapShowCompass", cat, NORMAL, true, true));
		addSetting(CompassNorthOriented = new SettingBool("CompassNorthOriented", cat, NORMAL, true, true));
		addSetting(MapNorthOriented = new SettingBool("MapNorthOriented", cat, NORMAL, true, true));
		addSetting(LastMapToggleBtnState = new SettingInt("LastMapToggleBtnState", cat, INVISIBLE, 0, true));

		addSetting(CurrentMapLayer = new SettingString("CurrentMapLayer", cat, EXPERT, "Mapnik", true));

		addSetting(MapMaxCachesDisplay_config = new SettingInt("MapMaxCachesDisplay_config", cat, INVISIBLE, 10000, true));
		addSetting(lastZoomLevel = new SettingInt("lastZoomLevel", cat, INVISIBLE, 14, true));
		addSetting(mapMaxCachesDisplayLarge_config = new SettingInt("mapMaxCachesDisplayLarge_config", cat, INVISIBLE, 75, true));
		addSetting(MapMaxCachesLabel = new SettingInt("MapMaxCachesLabel", cat, INVISIBLE, 12, true));
		addSetting(OsmMaxImportLevel = new SettingIntArray("OsmMaxImportLevel", cat, INVISIBLE, 16, true, Level));

		addSetting(MapIniWidth = new SettingInt("MapIniWidth", cat, INVISIBLE, 480, true));
		addSetting(MapIniHeight = new SettingInt("MapIniHeight", cat, INVISIBLE, 535, true));

	}

	private void addLogInSettings()
	{
		SettingCategory cat = SettingCategory.Login;

		addSetting(GcAPI = new SettingEncryptedString("GcAPI", cat, INVISIBLE, "", true));
		addSetting(GcVotePassword = new SettingEncryptedString("GcVotePassword", cat, NORMAL, "", true));
		addSetting(GcLogin = new SettingString("GcLogin", cat, NORMAL, "", true));
		addSetting(GcJoker = new SettingString("GcJoker", cat, NORMAL, "", true));
	}

	private void addFolderSettings()
	{
		SettingCategory cat = SettingCategory.Folder;

		String Work = Config.WorkPath;

		addSetting(UserImageFolder = new SettingFolder("UserImageFolder", cat, NORMAL, Work + "/User/Media", true));
		addSetting(LanguagePath = new SettingFolder("LanguagePath", cat, NORMAL, Work + "/data/lang", true));
		addSetting(SoundPath = new SettingFolder("SoundPath", cat, NORMAL, Work + "/data/sound", true));
		addSetting(TileCacheFolder = new SettingFolder("TileCacheFolder", cat, NORMAL, Work + "/cache", true));
		addSetting(PocketQueryFolder = new SettingFolder("PocketQueryFolder", cat, NORMAL, Work + "/PocketQuery", true));

		addSetting(DescriptionImageFolder = new SettingFolder("DescriptionImageFolder", cat, NORMAL, Work + "/repository/images", true));
		addSetting(MapPackFolder = new SettingFolder("MapPackFolder", cat, NORMAL, Work + "/repository/maps", true));
		addSetting(SpoilerFolder = new SettingFolder("SpoilerFolder", cat, NORMAL, Work + "/repository/spoilers", true));

		addSetting(TrackFolder = new SettingFolder("TrackFolder", cat, NORMAL, Work + "/User/Tracks", true));

		addSetting(Sel_LanguagePath = new SettingFile("Sel_LanguagePath", cat, INVISIBLE, Work + "/data/lang/en-GB/strings.ini", true,
				"lan"));
		addSetting(DatabasePath = new SettingFile("DatabasePath", cat, NORMAL, Work + "/cachebox.db3", true, "db3"));
		addSetting(FieldNotesGarminPath = new SettingFile("FieldNotesGarminPath", cat, INVISIBLE, Work + "/User/geocache_visits.txt", true));

		addSetting(SkinFolder = new SettingFolder("SkinFolder", cat, INVISIBLE, Work + "/skins/default", true)); // NEVER vorerst!

	}

	private void addQuickbuttonsSettings()
	{
		SettingCategory cat = SettingCategory.QuickList;
		addSetting(quickButtonShow = new SettingBool("quickButtonShow", cat, NORMAL, true, true));
		addSetting(quickButtonLastShow = new SettingBool("quickButtonLastShow", cat, INVISIBLE, false, true));
		addSetting(quickButtonList = new SettingString("quickButtonList", cat, INVISIBLE, "5,0,1,3,2", true));

	}

	private void addInternalSettings()
	{
		SettingCategory cat = SettingCategory.Internal;

		addSetting(SettingsShowExpert = new SettingBool("SettingsShowExpert", cat, NEVER, false, true));
		addSetting(SettingsShowAll = new SettingBool("SettingsShowAll", cat, NEVER, false, true));
		addSetting(nightMode = new SettingBool("nightMode", cat, NEVER, false, true));

		addSetting(ImportGpx = new SettingBool("ImportGpx", cat, INVISIBLE, true, true));
		addSetting(CacheMapData = new SettingBool("CacheMapData", cat, INVISIBLE, false, true));
		addSetting(CacheImageData = new SettingBool("CacheImageData", cat, INVISIBLE, true, true));
		addSetting(AutoResort = new SettingBool("AutoResort", cat, INVISIBLE, false, true));

		addSetting(ImportLayerOsm = new SettingBool("ImportLayerOsm", cat, INVISIBLE, true, true));
		addSetting(GCAutoSyncCachesFound = new SettingBool("GCAutoSyncCachesFound", cat, INVISIBLE, true, true));

		addSetting(MultiDBAsk = new SettingBool("MultiDBAsk", cat, NEVER, true, true));

		addSetting(newInstall = new SettingBool("newInstall", cat, NEVER, false, true));

		// int
		addSetting(LogMaxMonthAge = new SettingInt("LogMaxMonthAge", cat, INVISIBLE, 99999, true));
		addSetting(LogMinCount = new SettingInt("LogMinCount", cat, INVISIBLE, 99999, true));
		addSetting(installRev = new SettingInt("installRev", cat, NEVER, 0, true));

		addSetting(MultiDBAutoStartTime = new SettingInt("MultiDBAutoStartTime", cat, NEVER, 0, true));

		addSetting(NavigationProvider = new SettingString("NavigationProvider", cat, INVISIBLE,
				"http://openrouteservice.org/php/OpenLSRS_DetermineRoute.php", true));

		addSetting(SpoilersDescriptionTags = new SettingString("SpoilersDescriptionTags", cat, INVISIBLE, "", true));

		addSetting(hasCallPermission = new SettingBool("hasCallPermission", cat, NEVER, false, true));
		addSetting(hasPQ_PlugIn = new SettingBool("hasPQ_PlugIn", cat, NEVER, false, true));
		addSetting(hasFTF_PlugIn = new SettingBool("hasFTF_PlugIn", cat, NEVER, false, true));
	}

	private void addAPISettings()
	{
		SettingCategory cat = SettingCategory.API;

		addSetting(ImportRatings = new SettingBool("ImportRatings", cat, NORMAL, false, true));
		addSetting(ImportGpx = new SettingBool("ImportGpx", cat, NORMAL, false, true));
		addSetting(GCAdditionalImageDownload = new SettingBool("GCAdditionalImageDownload", cat, EXPERT, false, true));
		// addSetting(GCRequestDelay = new SettingInt("GCRequestDelay", cat, EXPERT, 0, true));
		addSetting(ImportPQsFromGeocachingCom = new SettingBool("ImportPQsFromGeocachingCom", cat, NORMAL, false, true));
		addSetting(FieldnotesUploadAll = new SettingBool("FieldnotesUploadAll", cat, NORMAL, false, true));
		addSetting(SearchWithoutFounds = new SettingBool("SearchWithoutFounds", cat, INVISIBLE, true, true));
		addSetting(SearchWithoutOwns = new SettingBool("SearchWithoutOwns", cat, INVISIBLE, true, true));
		addSetting(SearchOnlyAvible = new SettingBool("SearchOnlyAvible", cat, INVISIBLE, true, true));
		addSetting(lastSearchRadius = new SettingInt("lastSearchRadius", cat, INVISIBLE, 5, true));
	}

	private void addSkinSettings()
	{
		SettingCategory cat = SettingCategory.Skin;
		addSetting(MapsforgeDayTheme = new SettingFile("MapsforgeDayTheme", cat, NORMAL, "", true, "xml"));
		addSetting(MapsforgeNightTheme = new SettingFile("MapsforgeNightTheme", cat, NORMAL, "", true, "xml"));
	}

	private void addDebugSettings()
	{
		SettingCategory cat = SettingCategory.Debug;

		addSetting(DebugMode = new SettingBool("DebugMode", cat, EXPERT, false, true));
		addSetting(DebugShowPanel = new SettingBool("DebugShowPanel", cat, EXPERT, false, true));
		addSetting(DebugMemory = new SettingBool("DebugMemory", cat, EXPERT, false, true));
		addSetting(DebugShowMsg = new SettingBool("DebugShowMsg", cat, EXPERT, false, true));
		addSetting(DebugShowMarker = new SettingBool("DebugShowMarker", cat, EXPERT, false, true));
		addSetting(DebugShowLog = new SettingBool("DebugShowLog", cat, EXPERT, false, true));
		addSetting(OverrideUrl = new SettingString("OverrideUrl", cat, EXPERT, "", true));

	}

}
