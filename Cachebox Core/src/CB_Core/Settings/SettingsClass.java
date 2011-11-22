package CB_Core.Settings;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Enums.SmoothScrollingTyp;

public class SettingsClass extends SettingsList
{

	private static final long serialVersionUID = 7330937438116889415L;

	// Settings Compass
	public SettingBool HtcCompass;
	public SettingInt HtcLevel;

	// Settings Map
	public SettingBool MapHideMyFinds;

	// Invisible
	public SettingLongString Filter;

	// Folder
	public SettingFolder UserImageFolder;
	public SettingFolder LanguagePath;
	public SettingFolder TileCacheFolder;
	public SettingFolder PocketQueryFolder;
	public SettingFolder DescriptionImageFolder;
	public SettingFolder MapPackFolder;
	public SettingFolder SpoilerFolder;
	public SettingFolder TrackFolder;

	// Files
	public SettingFile Sel_LanguagePath;
	public SettingFile DatabasePath;
	public SettingFile FieldNotesGarminPath;

	// Bool
	public SettingBool SaveFieldNotesHtml;
	public SettingBool OsmDpiAwareRendering;
	public SettingBool AllowInternetAccess;
	public SettingBool AllowRouteInternet;
	public SettingBool ImportGpx;
	public SettingBool CacheMapData;
	public SettingBool CacheImageData;
	public SettingBool SuppressPowerSaving;
	public SettingBool PlaySounds;
	public SettingBool PopSkipOutdatedGpx;
	public SettingBool MapShowRating;
	public SettingBool MapShowDT;
	public SettingBool MapShowTitles;
	public SettingBool ShowKeypad;
	public SettingBool ImportLayerOsm;
	public SettingBool TrackRecorderStartup;
	public SettingBool MapShowCompass;
	public SettingBool ResortRepaint;
	public SettingBool GCAutoSyncCachesFound;
	public SettingBool GCAdditionalImageDownload;
	public SettingBool AutoResort;
	public SettingBool FieldnotesUploadAll;
	public SettingBool MultiDBAsk;
	public SettingBool AllowLandscape;
	public SettingBool MoveMapCenterWithSpeed;
	public SettingBool PremiumMember;
	public SettingBool SearchWithoutFounds;
	public SettingBool SearchWithoutOwns;
	public SettingBool SearchOnlyAvible;
	public SettingBool quickButtonShow;
	public SettingBool DebugShowPanel;
	public SettingBool DebugMemory;
	public SettingBool DebugShowMsg;
	public SettingBool nightMode;
	public SettingBool DebugShowLog;
	public SettingBool DescriptionNoAttributes;
	public SettingBool quickButtonLastShow;
	public SettingBool newInstall;
	public SettingBool ImperialUnits;
	public SettingBool ShowDirektLine;
	public SettingBool PositionMarkerTransparent;
	public SettingBool PositionAtVertex;
	public SettingBool DebugShowMarker;
	public SettingBool ImportRatings;
	public SettingBool ImportPQsFromGeocachingCom;
	public SettingBool SettingsShowExpert;
	public SettingBool SettingsShowAll;

	// int
	public SettingInt LogMaxMonthAge;
	public SettingInt LogMinCount;
	public SettingInt installRev;
	public SettingInt OsmCoverage;
	public SettingInt FoundOffset;
	public SettingInt MapMaxCachesLabel;
	public SettingInt MapMaxCachesDisplay_config;
	public SettingInt mapMaxCachesDisplayLarge_config;
	public SettingInt MultiDBAutoStartTime;
	public SettingInt LockM;
	public SettingInt LockSec;
	public SettingInt MoveMapCenterMaxSpeed;
	public SettingInt lastZoomLevel;

	// double
	public SettingDouble MapInitLatitude;
	public SettingDouble MapInitLongitude;

	// String
	public SettingString CurrentMapLayer;

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
	public SettingString PopHost;

	// Decrypt
	public SettingEncryptedString GcAPI;
	public SettingEncryptedString GcVotePassword;

	// Enums
	public SettingEnum<SmoothScrollingTyp> SmoothScrolling;

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

	public SettingsClass()
	{

		for (int i = 0; i < 22; i++)
		{
			if (i < 21) Level[i] = i;

			if (i > 13) CrossLevel[i - 14] = i;

		}

		SettingCategory cat = SettingCategory.Internal;

		addSetting(ShowKeypad = new SettingBool("ShowKeypad", cat, NORMAL, true, true));
		addSetting(PlaySounds = new SettingBool("PlaySounds", cat, NORMAL, true, true));

		addSetting(ImportGpx = new SettingBool("ImportGpx", cat, NORMAL, true, true));
		addSetting(CacheMapData = new SettingBool("CacheMapData", cat, NORMAL, false, true));
		addSetting(CacheImageData = new SettingBool("CacheImageData", cat, NORMAL, false, true));
		addSetting(SuppressPowerSaving = new SettingBool("SuppressPowerSaving", cat, NORMAL, true, true));

		addSetting(PopSkipOutdatedGpx = new SettingBool("PopSkipOutdatedGpx", cat, NORMAL, true, true));

		addSetting(ImportLayerOsm = new SettingBool("ImportLayerOsm", cat, NORMAL, true, true));

		addSetting(ResortRepaint = new SettingBool("ResortRepaint", cat, NORMAL, false, true));
		addSetting(GCAutoSyncCachesFound = new SettingBool("GCAutoSyncCachesFound", cat, NORMAL, true, true));

		addSetting(AutoResort = new SettingBool("AutoResort", cat, NORMAL, false, true));

		addSetting(MultiDBAsk = new SettingBool("MultiDBAsk", cat, NORMAL, true, true));
		addSetting(AllowLandscape = new SettingBool("AllowLandscape", cat, NORMAL, false, true));
		addSetting(MoveMapCenterWithSpeed = new SettingBool("MoveMapCenterWithSpeed", cat, NORMAL, false, true));
		addSetting(PremiumMember = new SettingBool("PremiumMember", cat, NORMAL, false, true));

		addSetting(quickButtonLastShow = new SettingBool("quickButtonLastShow", cat, NORMAL, false, true));
		addSetting(newInstall = new SettingBool("newInstall", cat, NORMAL, false, true));
		addSetting(ImperialUnits = new SettingBool("ImperialUnits", cat, NORMAL, false, true));

		// int
		addSetting(LogMaxMonthAge = new SettingInt("LogMaxMonthAge", cat, NORMAL, 99999, true));
		addSetting(LogMinCount = new SettingInt("LogMinCount", cat, NORMAL, 99999, true));
		addSetting(installRev = new SettingInt("installRev", cat, NORMAL, 0, true));
		addSetting(OsmCoverage = new SettingInt("OsmCoverage", cat, NORMAL, 1000, true));

		addSetting(MultiDBAutoStartTime = new SettingInt("MultiDBAutoStartTime", cat, NORMAL, 0, true));
		addSetting(LockM = new SettingInt("LockM", cat, NORMAL, 1, true));
		addSetting(LockSec = new SettingInt("LockSec", cat, NORMAL, 0, true));

		addSetting(NavigationProvider = new SettingString("NavigationProvider", cat, INVISIBLE,
				"http://129.206.229.146/openrouteservice/php/OpenLSRS_DetermineRoute.php", true));

		addSetting(SpoilersDescriptionTags = new SettingString("SpoilersDescriptionTags", cat, INVISIBLE, "", true));
		addSetting(quickButtonList = new SettingString("quickButtonList", cat, INVISIBLE, "5,0,1,3,2", true));

		addSetting(OverrideUrl = new SettingString("OverrideUrl", cat, INVISIBLE, "", true));
		addSetting(PopHost = new SettingString("PopHost", cat, INVISIBLE, "", true));

		// Decrypt String

		// Enums

		// IntArray

		addInternalSattings();
		addMapSettings();
		addLogInSettings();
		addFolderSettings();
		addGpsSettings();
		addMiscSettings();
		addTemplateSettings();
		addInternalSettings();
		addAPISettings();
		addDebugSettings();
	}

	private void addInternalSattings()
	{
		SettingCategory cat = SettingCategory.Internal;

		addSetting(nightMode = new SettingBool("nightMode", cat, INVISIBLE, false, true));
	}

	private void addMiscSettings()
	{
		SettingCategory cat = SettingCategory.Misc;

		addSetting(Filter = new SettingLongString("Filter", cat, INVISIBLE, FilterProperties.presets[0].toString(), false));
		addSetting(SaveFieldNotesHtml = new SettingBool("SaveFieldNotesHtml", cat, NORMAL, true, true));
		addSetting(AllowInternetAccess = new SettingBool("AllowInternetAccess", cat, NORMAL, true, true));
		addSetting(AllowRouteInternet = new SettingBool("AllowRouteInternet", cat, NORMAL, true, true));
		addSetting(FoundOffset = new SettingInt("FoundOffset", cat, NORMAL, 0, true));
		addSetting(TrackDistance = new SettingIntArray("TrackDistance", cat, NORMAL, 3, true, TrackDistanceArray));
		addSetting(SoundApproachDistance = new SettingIntArray("SoundApproachDistance", cat, NORMAL, 50, true, approach));
		addSetting(TrackRecorderStartup = new SettingBool("TrackRecorderStartup", cat, NORMAL, false, true));
		addSetting(DescriptionNoAttributes = new SettingBool("DescriptionNoAttributes", cat, NORMAL, false, true));
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

		addSetting(HtcLevel = new SettingInt("HtcLevel", cat, NORMAL, 5, true));
		addSetting(HtcCompass = new SettingBool("HtcCompass", cat, NORMAL, false, true));
	}

	private void addMapSettings()
	{
		SettingCategory cat = SettingCategory.Map;

		addSetting(SmoothScrolling = new SettingEnum<SmoothScrollingTyp>("SmoothScrolling", cat, INVISIBLE, SmoothScrollingTyp.normal,
				true, GlobalCore.SmoothScrolling));

		addSetting(ZoomCross = new SettingIntArray("ZoomCross", cat, NORMAL, 16, true, CrossLevel));
		addSetting(OsmMaxLevel = new SettingIntArray("OsmMaxLevel", cat, NORMAL, 17, true, Level));
		addSetting(OsmMinLevel = new SettingIntArray("OsmMinLevel", cat, NORMAL, 8, true, Level));
		addSetting(OsmMaxImportLevel = new SettingIntArray("OsmMaxImportLevel", cat, NORMAL, 16, true, Level));
		addSetting(OsmMinLevel = new SettingIntArray("OsmMinLevel", cat, NORMAL, 8, true, Level));
		addSetting(OsmMaxImportLevel = new SettingIntArray("OsmMaxImportLevel", cat, NORMAL, 16, true, Level));
		addSetting(ShowDirektLine = new SettingBool("ShowDirektLine", cat, NORMAL, false, true));
		addSetting(PositionMarkerTransparent = new SettingBool("PositionMarkerTransparent", cat, NORMAL, false, true));
		addSetting(PositionAtVertex = new SettingBool("PositionAtVertex", cat, NORMAL, false, true));
		addSetting(MapInitLatitude = new SettingDouble("LogMinCount", SettingCategory.Gps, EXPERT, -1000, true));
		addSetting(MapInitLongitude = new SettingDouble("MapInitLongitude", SettingCategory.Gps, EXPERT, -1000, true));
		addSetting(MapShowCompass = new SettingBool("MapShowCompass", cat, NORMAL, true, true));
		addSetting(MapMaxCachesLabel = new SettingInt("MapMaxCachesLabel", cat, NORMAL, 12, true));
		addSetting(MapMaxCachesDisplay_config = new SettingInt("MapMaxCachesDisplay_config", cat, NORMAL, 10000, true));

		addSetting(mapMaxCachesDisplayLarge_config = new SettingInt("mapMaxCachesDisplayLarge_config", cat, NORMAL, 75, true));
		addSetting(OsmDpiAwareRendering = new SettingBool("OsmDpiAwareRendering", cat, NORMAL, false, true));
		addSetting(MoveMapCenterMaxSpeed = new SettingInt("MoveMapCenterMaxSpeed", cat, NORMAL, 20, true));
		addSetting(lastZoomLevel = new SettingInt("lastZoomLevel", cat, INVISIBLE, 14, true));

		addSetting(CurrentMapLayer = new SettingString("CurrentMapLayer", cat, EXPERT, "Mapnik", true));

		addSetting(MapHideMyFinds = new SettingBool("MapHideMyFinds", cat, NORMAL, false, true));
		addSetting(MapShowRating = new SettingBool("MapShowRating", cat, NORMAL, true, true));
		addSetting(MapShowDT = new SettingBool("MapShowDT", cat, NORMAL, true, true));
		addSetting(MapShowTitles = new SettingBool("MapShowTitles", cat, NORMAL, true, true));

	}

	private void addLogInSettings()
	{
		SettingCategory cat = SettingCategory.Login;

		addSetting(GcAPI = new SettingEncryptedString("GcAPI", cat, NORMAL, "", true));
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
		addSetting(TileCacheFolder = new SettingFolder("TileCacheFolder", cat, NORMAL, Work + "/cache", true));
		addSetting(PocketQueryFolder = new SettingFolder("PocketQueryFolder", cat, NORMAL, Work + "/PocketQuery", true));
		addSetting(DescriptionImageFolder = new SettingFolder("DescriptionImageFolder", cat, NORMAL, Work + "/repository/images", true));
		addSetting(MapPackFolder = new SettingFolder("MapPackFolder", cat, NORMAL, Work + "/repository/maps", true));
		addSetting(SpoilerFolder = new SettingFolder("SpoilerFolder", cat, NORMAL, Work + "/repository/spoilers", true));
		addSetting(TrackFolder = new SettingFolder("TrackFolder", cat, NORMAL, Work + "/User/Tracks", true));

		addSetting(Sel_LanguagePath = new SettingFile("Sel_LanguagePath", cat, INVISIBLE, Work + "/data/lang/en.lan", true, "lan"));
		addSetting(DatabasePath = new SettingFile("DatabasePath", cat, NORMAL, Work + "/cachebox.db3", true, "db3"));
		addSetting(FieldNotesGarminPath = new SettingFile("FieldNotesGarminPath", cat, NORMAL, Work + "/User/geocache_visits.txt", true));

	}

	private void addInternalSettings()
	{
		SettingCategory cat = SettingCategory.Internal;

		addSetting(SettingsShowExpert = new SettingBool("SettingsShowExpert", cat, INVISIBLE, false, true));
		addSetting(SettingsShowAll = new SettingBool("SettingsShowAll", cat, INVISIBLE, false, true));
	}

	private void addAPISettings()
	{
		SettingCategory cat = SettingCategory.API;

		addSetting(ImportRatings = new SettingBool("ImportRatings", cat, NORMAL, false, true));
		addSetting(ImportGpx = new SettingBool("ImportGpx", cat, NORMAL, false, true));
		addSetting(GCAdditionalImageDownload = new SettingBool("GCAdditionalImageDownload", cat, NORMAL, false, true));
		addSetting(ImportPQsFromGeocachingCom = new SettingBool("ImportPQsFromGeocachingCom", cat, NORMAL, false, true));
		addSetting(FieldnotesUploadAll = new SettingBool("FieldnotesUploadAll", cat, NORMAL, false, true));
		addSetting(SearchWithoutFounds = new SettingBool("SearchWithoutFounds", cat, NORMAL, true, true));
		addSetting(SearchWithoutOwns = new SettingBool("SearchWithoutOwns", cat, NORMAL, true, true));
		addSetting(SearchOnlyAvible = new SettingBool("SearchOnlyAvible", cat, NORMAL, true, true));
	}

	private void addDebugSettings()
	{
		SettingCategory cat = SettingCategory.Debug;

		addSetting(quickButtonShow = new SettingBool("quickButtonShow", cat, NORMAL, true, true));
		addSetting(DebugShowPanel = new SettingBool("DebugShowPanel", cat, NORMAL, false, true));
		addSetting(DebugMemory = new SettingBool("DebugMemory", cat, NORMAL, false, true));
		addSetting(DebugShowMsg = new SettingBool("DebugShowMsg", cat, NORMAL, false, true));
		addSetting(DebugShowMarker = new SettingBool("DebugShowMarker", cat, NORMAL, false, true));
		addSetting(DebugShowLog = new SettingBool("DebugShowLog", cat, NORMAL, false, true));

	}

}
