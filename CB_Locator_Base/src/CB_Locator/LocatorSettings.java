package CB_Locator;

import CB_UI_Base.graphics.GL_RenderType;
import CB_Utils.Config_Core;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingDouble;
import CB_Utils.Settings.SettingEnum;
import CB_Utils.Settings.SettingFile;
import CB_Utils.Settings.SettingFolder;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingIntArray;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingString;
import CB_Utils.Settings.SettingStringList;
import CB_Utils.Settings.SettingUsage;
import CB_Utils.Settings.SettingsList;

public interface LocatorSettings {
	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	public static final SettingModus DEVELOPER = SettingModus.DEVELOPER;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;

	public Integer Level[] = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21 };
	public Integer CrossLevel[] = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21 };

	public static final SettingFolder TileCacheFolder = new SettingFolder("TileCacheFolder", SettingCategory.Folder, NEVER, Config_Core.mWorkPath + "/repository/cache", SettingStoreType.Global, SettingUsage.ALL, true);
	public static final SettingFolder TileCacheFolderLocal = new SettingFolder("TileCacheFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true);
	public static final SettingFolder MapPackFolder = new SettingFolder("MapPackFolder", SettingCategory.Map, EXPERT, Config_Core.mWorkPath + "/repository/maps", SettingStoreType.Global, SettingUsage.ALL, false);
	public static final SettingFolder MapPackFolderLocal = new SettingFolder("MapPackFolderLocal", SettingCategory.Map, NEVER, Config_Core.mWorkPath + "/repository/maps", SettingStoreType.Local, SettingUsage.ALL, false);

	public static final SettingStringList CurrentMapLayer = (SettingStringList) SettingsList.addSetting(new SettingStringList("CurrentMapLayer", SettingCategory.Map, DEVELOPER, new String[] { "Mapnik" }, SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString CurrentMapOverlayLayer = (SettingString) SettingsList.addSetting(new SettingString("CurrentMapOverlayLayer", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));

	public static final SettingDouble MapInitLatitude = new SettingDouble("MapInitLatitude", SettingCategory.Positions, EXPERT, -1000, SettingStoreType.Global, SettingUsage.ALL);

	public static final SettingDouble MapInitLongitude = new SettingDouble("MapInitLongitude", SettingCategory.Positions, EXPERT, -1000, SettingStoreType.Global, SettingUsage.ALL);

	public static final SettingInt lastZoomLevel = (SettingInt) SettingsList.addSetting(new SettingInt("lastZoomLevel", SettingCategory.Map, DEVELOPER, 14, SettingStoreType.Global, SettingUsage.ALL));

	public static final SettingBool MoveMapCenterWithSpeed = new SettingBool("MoveMapCenterWithSpeed", SettingCategory.CarMode, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingInt MoveMapCenterMaxSpeed = (SettingInt) SettingsList.addSetting(new SettingInt("MoveMapCenterMaxSpeed", SettingCategory.CarMode, NORMAL, 60, SettingStoreType.Global, SettingUsage.ACB));

	public static final SettingBool ShowAccuracyCircle = new SettingBool("ShowAccuracyCircle", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
	public static final SettingBool ShowMapCenterCross = new SettingBool("ShowMapCenterCross", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingBool PositionMarkerTransparent = new SettingBool("PositionMarkerTransparent", SettingCategory.Map, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingIntArray OsmMinLevel = new SettingIntArray("OsmMinLevel", SettingCategory.Map, EXPERT, 7, SettingStoreType.Global, SettingUsage.ACB, Level);
	public static final SettingIntArray OsmMaxLevel = new SettingIntArray("OsmMaxLevel", SettingCategory.Map, EXPERT, 19, SettingStoreType.Global, SettingUsage.ACB, Level);
	public static final SettingIntArray CompassMapMinZoomLevel = new SettingIntArray("CompassMapMinZoomLevel", SettingCategory.Map, EXPERT, 13, SettingStoreType.Global, SettingUsage.ACB, LocatorSettings.Level);
	public static final SettingIntArray CompassMapMaxZommLevel = new SettingIntArray("CompassMapMaxZommLevel", SettingCategory.Map, EXPERT, 20, SettingStoreType.Global, SettingUsage.ACB, LocatorSettings.Level);

	public static final SettingFolder RenderThemesFolder = new SettingFolder("RenderThemesFolder", SettingCategory.Map, NORMAL, Config_Core.mWorkPath + "/RenderThemes", SettingStoreType.Global, SettingUsage.ALL, false);
	public static final SettingFile MapsforgeDayTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeDayTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
	public static final SettingFile MapsforgeNightTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeNightTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
	public static final SettingFile MapsforgeCarDayTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeCarDayTheme", SettingCategory.Map, NEVER, "CAR", SettingStoreType.Global, SettingUsage.ACB, "xml"));
	public static final SettingFile MapsforgeCarNightTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeCarNightTheme", SettingCategory.Map, NEVER, "CAR", SettingStoreType.Global, SettingUsage.ACB, "xml"));
	public static final SettingString MapsforgeDayStyle = (SettingString) SettingsList.addSetting(new SettingString("MapsforgeDayStyle", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString MapsforgeNightStyle = (SettingString) SettingsList.addSetting(new SettingString("MapsforgeNightStyle", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString MapsforgeCarDayStyle = (SettingString) SettingsList.addSetting(new SettingString("MapsforgeCarDayStyle", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString MapsforgeCarNightStyle = (SettingString) SettingsList.addSetting(new SettingString("MapsforgeCarNightStyle", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));

	public static final SettingString UserMap1 = (SettingString) SettingsList.addSetting(
			new SettingString("UserMap1", SettingCategory.Map, EXPERT, "{JPG}{name:ESRI World_Imagery}http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", SettingStoreType.Global, SettingUsage.ALL));
	public static final SettingString UserMap2 = (SettingString) SettingsList.addSetting(new SettingString("UserMap2", SettingCategory.Map, EXPERT, "", SettingStoreType.Global, SettingUsage.ALL));
	public static final SettingString PreferredMapLanguage = (SettingString) SettingsList.addSetting(new SettingString("MapLanguage", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ALL));

	public static final SettingEnum<GL_RenderType> MapsforgeRenderType = new SettingEnum<GL_RenderType>("MapsforgeRenderType", SettingCategory.Map, EXPERT, GL_RenderType.Mapsforge, SettingStoreType.Global, SettingUsage.ACB, GL_RenderType.Mapsforge);

	public static final SettingBool DEBUG_MapGrid = new SettingBool("DEBUG_MapGrid", SettingCategory.Debug, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
}
