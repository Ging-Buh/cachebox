package CB_Core.Settings;

import CB_Core.Config;
import CB_Core.FilterProperties;
import CB_Core.GlobalCore;
import CB_Core.Enums.SmoothScrollingTyp;

public class Settings extends SettingsList
{
	/**
	 * 
	 */
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
	
	//int
	public SettingInt LogMaxMonthAge;
	public SettingInt LogMinCount;
	public SettingInt TrackDistance;
	public SettingInt OsmMinLevel;
	public SettingInt OsmMaxImportLevel;
	public SettingInt OsmMaxLevel;
	public SettingInt OsmCoverage;
	public SettingInt FoundOffset;
	public SettingInt MapMaxCachesLabel;
	public SettingInt MapMaxCachesDisplay_config;
	public SettingInt SoundApproachDistance;
	public SettingInt mapMaxCachesDisplayLarge_config;
	public SettingInt ZoomCross;
	public SettingInt GCRequestDelay;
	public SettingInt MultiDBAutoStartTime;
	public SettingInt LockM;
	public SettingInt LockSec;
	public SettingInt MoveMapCenterMaxSpeed;
	public SettingInt lastZoomLevel;
	
	//double
	public SettingDouble MapInitLatitude;
	public SettingDouble MapInitLongitude;
	
	//String
	public SettingString CurrentMapLayer;
	public SettingString AutoUpdate;
	public SettingString NavigationProvider;
	public SettingString FoundTemplate;
	public SettingString DNFTemplate;
	public SettingString NeedsMaintenanceTemplate;
	public SettingString AddNoteTemplate;
	public SettingString SpoilersDescriptionTags;
	public SettingString quickButtonList;
	
	
	//Enums
	public SettingEnum SmoothScrolling;
	
	
	public Settings()
	{
		
		String WorkPath=Config.WorkPath;
		
		
		addSetting(HtcLevel = new SettingInt("HtcLevel", SettingCategory.Gps, SettingModus.Normal, 5, true));
		// Settings Map
		
		// Invisible
		addSetting(Filter = new SettingLongString("Filter", SettingCategory.Internal, SettingModus.Invisible, FilterProperties.presets[0].toString(), false));
		
		// Folder
		addSetting(UserImageFolder = new SettingFolder("UserImageFolder", SettingCategory.Internal, SettingModus.Normal, WorkPath + "/User/Media", true));
		addSetting(LanguagePath = new SettingFolder("LanguagePath", SettingCategory.Internal, SettingModus.Normal, WorkPath + "/data/lang", true));
		addSetting(TileCacheFolder = new SettingFolder("TileCacheFolder", SettingCategory.Internal, SettingModus.Normal, WorkPath + "/cache", true));
		addSetting(PocketQueryFolder = new SettingFolder("PocketQueryFolder", SettingCategory.Internal, SettingModus.Normal, WorkPath + "/PocketQuery", true));
		addSetting(DescriptionImageFolder = new SettingFolder("DescriptionImageFolder", SettingCategory.Internal, SettingModus.Normal, WorkPath + "/repository/images", true));
		addSetting(MapPackFolder = new SettingFolder("MapPackFolder", SettingCategory.Internal, SettingModus.Normal, WorkPath + "/repository/maps", true));
		addSetting(SpoilerFolder = new SettingFolder("SpoilerFolder", SettingCategory.Internal, SettingModus.Normal, WorkPath + "/repository/spoilers", true));
		addSetting(TrackFolder = new SettingFolder("TrackFolder", SettingCategory.Internal, SettingModus.Normal, WorkPath + "/User/Tracks", true));


		// Files
		addSetting(Sel_LanguagePath = new SettingFile("Sel_LanguagePath", SettingCategory.Internal, SettingModus.Normal, WorkPath + "/data/lang/en.lan", true,"lan"));
		addSetting(DatabasePath = new SettingFile("DatabasePath", SettingCategory.Internal, SettingModus.Normal, WorkPath + "/cachebox.db3", true,"db3"));
		addSetting(FieldNotesGarminPath = new SettingFile("FieldNotesGarminPath", SettingCategory.Internal, SettingModus.Normal, WorkPath + "/User/geocache_visits.txt", true));
		
		//Bool
		addSetting(SaveFieldNotesHtml = new SettingBool("SaveFieldNotesHtml", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(OsmDpiAwareRendering = new SettingBool("OsmDpiAwareRendering", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(AllowInternetAccess = new SettingBool("AllowInternetAccess", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(AllowRouteInternet = new SettingBool("AllowRouteInternet", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(ImportGpx = new SettingBool("ImportGpx", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(CacheMapData = new SettingBool("CacheMapData", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(CacheImageData = new SettingBool("CacheImageData", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(SuppressPowerSaving = new SettingBool("SuppressPowerSaving", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(PlaySounds = new SettingBool("PlaySounds", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(PopSkipOutdatedGpx = new SettingBool("PopSkipOutdatedGpx", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(MapHideMyFinds = new SettingBool("MapHideMyFinds", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(MapShowRating = new SettingBool("MapShowRating", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(MapShowDT = new SettingBool("MapShowDT", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(MapShowTitles = new SettingBool("MapShowTitles", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(ShowKeypad = new SettingBool("ShowKeypad", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(ImportLayerOsm = new SettingBool("ImportLayerOsm", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(TrackRecorderStartup = new SettingBool("TrackRecorderStartup", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(MapShowCompass = new SettingBool("MapShowCompass", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(ResortRepaint = new SettingBool("ResortRepaint", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(GCAutoSyncCachesFound = new SettingBool("GCAutoSyncCachesFound", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(GCAdditionalImageDownload = new SettingBool("GCAdditionalImageDownload", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(AutoResort = new SettingBool("AutoResort", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(FieldnotesUploadAll = new SettingBool("FieldnotesUploadAll", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(HtcCompass = new SettingBool("HtcCompass", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(MultiDBAsk = new SettingBool("MultiDBAsk", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(AllowLandscape = new SettingBool("AllowLandscape", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(MoveMapCenterWithSpeed = new SettingBool("MoveMapCenterWithSpeed", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(PremiumMember = new SettingBool("PremiumMember", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(SearchWithoutFounds = new SettingBool("SearchWithoutFounds", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(SearchWithoutOwns = new SettingBool("SearchWithoutOwns", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(SearchOnlyAvible = new SettingBool("SearchOnlyAvible", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(quickButtonShow = new SettingBool("quickButtonShow", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(DebugShowPanel = new SettingBool("DebugShowPanel", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(DebugMemory = new SettingBool("DebugMemory", SettingCategory.Gps, SettingModus.Normal, false, true));
		addSetting(DebugShowMsg = new SettingBool("DebugShowMsg", SettingCategory.Gps, SettingModus.Normal, false, true));
		
		//int
		addSetting(LogMaxMonthAge = new SettingInt("LogMaxMonthAge", SettingCategory.Internal, SettingModus.Normal, 99999, true));
		addSetting(LogMinCount = new SettingInt("LogMinCount", SettingCategory.Internal, SettingModus.Normal, 99999, true));
		addSetting(TrackDistance = new SettingInt("TrackDistance", SettingCategory.Internal, SettingModus.Normal, 3, true));
		addSetting(OsmMinLevel = new SettingInt("OsmMinLevel", SettingCategory.Internal, SettingModus.Normal, 8, true));
		addSetting(OsmMaxImportLevel = new SettingInt("OsmMaxImportLevel", SettingCategory.Internal, SettingModus.Normal, 16, true));
		addSetting(OsmMaxLevel = new SettingInt("OsmMaxLevel", SettingCategory.Internal, SettingModus.Normal, 17, true));
		addSetting(OsmCoverage = new SettingInt("OsmCoverage", SettingCategory.Internal, SettingModus.Normal, 1000, true));
		addSetting(FoundOffset = new SettingInt("FoundOffset", SettingCategory.Internal, SettingModus.Normal, 0, true));
		addSetting(MapMaxCachesLabel = new SettingInt("MapMaxCachesLabel", SettingCategory.Internal, SettingModus.Normal, 12, true));
		addSetting(MapMaxCachesDisplay_config = new SettingInt("MapMaxCachesDisplay_config", SettingCategory.Internal, SettingModus.Normal, 10000, true));
		addSetting(SoundApproachDistance = new SettingInt("SoundApproachDistance", SettingCategory.Internal, SettingModus.Normal, 50, true));
		addSetting(mapMaxCachesDisplayLarge_config = new SettingInt("mapMaxCachesDisplayLarge_config", SettingCategory.Internal, SettingModus.Normal, 75, true));
		addSetting(ZoomCross = new SettingInt("ZoomCross", SettingCategory.Internal, SettingModus.Normal, 16, true));
		addSetting(GCRequestDelay = new SettingInt("GCRequestDelay", SettingCategory.Internal, SettingModus.Normal, 10, true));
		addSetting(MultiDBAutoStartTime = new SettingInt("MultiDBAutoStartTime", SettingCategory.Internal, SettingModus.Normal, 0, true));
		addSetting(LockM = new SettingInt("LockM", SettingCategory.Internal, SettingModus.Normal, 1, true));
		addSetting(LockSec = new SettingInt("LockSec", SettingCategory.Internal, SettingModus.Normal, 0, true));
		addSetting(MoveMapCenterMaxSpeed = new SettingInt("MoveMapCenterMaxSpeed", SettingCategory.Internal, SettingModus.Normal, 20, true));
		addSetting(lastZoomLevel = new SettingInt("lastZoomLevel", SettingCategory.Internal, SettingModus.Normal, 14, true));

		//double
		addSetting(MapInitLatitude = new SettingDouble("LogMinCount", SettingCategory.Gps, SettingModus.Normal, -1000, true));
		addSetting(MapInitLongitude = new SettingDouble("MapInitLongitude", SettingCategory.Gps, SettingModus.Normal, -1000, true));
		
		
		//String
		addSetting(CurrentMapLayer = new SettingString("CurrentMapLayer", SettingCategory.Internal, SettingModus.Invisible, "Mapnik", true));
		addSetting(AutoUpdate = new SettingString("AutoUpdate", SettingCategory.Internal, SettingModus.Invisible, "http://www.getcachebox.net/latest-stable", true));
		addSetting(NavigationProvider = new SettingString("NavigationProvider", SettingCategory.Internal, SettingModus.Invisible, "http://129.206.229.146/openrouteservice/php/OpenLSRS_DetermineRoute.php", true));
		addSetting(FoundTemplate = new SettingLongString("FoundTemplate", SettingCategory.Internal, SettingModus.Invisible, "<br>###finds##, ##time##, Found it with DroidCachebox!", true));
		addSetting(DNFTemplate = new SettingLongString("DNFTemplate", SettingCategory.Internal, SettingModus.Invisible, "<br>##time##. Logged it with DroidCachebox!", true));
		addSetting(NeedsMaintenanceTemplate = new SettingLongString("NeedsMaintenanceTemplate", SettingCategory.Internal, SettingModus.Invisible, "Logged it with DroidCachebox!", true));
		addSetting(AddNoteTemplate = new SettingLongString("AddNoteTemplate", SettingCategory.Internal, SettingModus.Invisible, "Logged it with DroidCachebox!", true));
		addSetting(SpoilersDescriptionTags = new SettingString("SpoilersDescriptionTags", SettingCategory.Internal, SettingModus.Invisible, "", true));
		addSetting(quickButtonList = new SettingString("quickButtonList", SettingCategory.Internal, SettingModus.Invisible, "5,0,1,3,2", true));
				
		//Enums
		addSetting(SmoothScrolling = new SettingEnum("SmoothScrolling", SettingCategory.Internal, SettingModus.Invisible, SmoothScrollingTyp.none, false, GlobalCore.SmoothScrolling));

//		validateSetting("DopMin", "0.2");
//		validateSetting("DopWidth", "1");
		
		
		
	ReadFromDB();
	}

}
