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
import CB_Utils.Settings.SettingUsage;
import CB_Utils.Settings.SettingsList;

public interface LocatorSettings {
	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	public static final SettingModus INVISIBLE = SettingModus.Invisible;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;

	public Integer Level[] = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21 };
	public Integer CrossLevel[] = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21 };

	public static final SettingFolder TileCacheFolder = new SettingFolder("TileCacheFolder", SettingCategory.Folder, NORMAL, Config_Core.mWorkPath + "/repository/cache", SettingStoreType.Global, SettingUsage.ALL, true);
	public static final SettingFolder TileCacheFolderLocal = new SettingFolder("TileCacheFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true);
	public static final SettingFolder MapPackFolder = new SettingFolder("MapPackFolder", SettingCategory.Folder, NORMAL, Config_Core.mWorkPath + "/repository/maps", SettingStoreType.Global, SettingUsage.ALL, false);
	public static final SettingFolder MapPackFolderLocal = new SettingFolder("MapPackFolderLocal", SettingCategory.Folder, NEVER, Config_Core.mWorkPath + "/repository/maps", SettingStoreType.Local, SettingUsage.ALL, false);

	public static final SettingString CurrentMapLayer = (SettingString) SettingsList.addSetting(new SettingString("CurrentMapLayer", SettingCategory.Map, EXPERT, "Mapnik", SettingStoreType.Global, SettingUsage.ACB));
	public static final SettingString CurrentMapOverlayLayer = (SettingString) SettingsList.addSetting(new SettingString("CurrentMapOverlayLayer", SettingCategory.Map, EXPERT, "", SettingStoreType.Global, SettingUsage.ACB));

	public static final SettingDouble MapInitLatitude = new SettingDouble("MapInitLatitude", SettingCategory.Positions, EXPERT, -1000, SettingStoreType.Global, SettingUsage.ALL);

	public static final SettingDouble MapInitLongitude = new SettingDouble("MapInitLongitude", SettingCategory.Positions, EXPERT, -1000, SettingStoreType.Global, SettingUsage.ALL);

	public static final SettingInt lastZoomLevel = (SettingInt) SettingsList.addSetting(new SettingInt("lastZoomLevel", SettingCategory.Map, INVISIBLE, 14, SettingStoreType.Global, SettingUsage.ALL));

	public static final SettingBool MoveMapCenterWithSpeed = new SettingBool("MoveMapCenterWithSpeed", SettingCategory.CarMode, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingInt MoveMapCenterMaxSpeed = (SettingInt) SettingsList.addSetting(new SettingInt("MoveMapCenterMaxSpeed", SettingCategory.CarMode, NORMAL, 60, SettingStoreType.Global, SettingUsage.ACB));

	public static final SettingBool ShowAccuracyCircle = new SettingBool("ShowAccuracyCircle", SettingCategory.Map, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingBool ShowMapCenterCross = new SettingBool("ShowMapCenterCross", SettingCategory.Map, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingBool PositionMarkerTransparent = new SettingBool("PositionMarkerTransparent", SettingCategory.Map, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);

	public static final SettingIntArray OsmMaxLevel = new SettingIntArray("OsmMaxLevel", SettingCategory.Map, NORMAL, 19, SettingStoreType.Global, SettingUsage.ACB, Level);

	public static final SettingIntArray OsmMinLevel = new SettingIntArray("OsmMinLevel", SettingCategory.Map, NORMAL, 7, SettingStoreType.Global, SettingUsage.ACB, Level);

	public static final SettingFile MapsforgeDayTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeDayTheme", SettingCategory.Skin, NORMAL, "", SettingStoreType.Global, SettingUsage.ALL, "xml"));

	public static final SettingFile MapsforgeNightTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeNightTheme", SettingCategory.Skin, NORMAL, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));

	public static final SettingEnum<GL_RenderType> MapsforgeRenderType = new SettingEnum<GL_RenderType>("MapsforgeRenderType", SettingCategory.Map, EXPERT, GL_RenderType.Mixing, SettingStoreType.Global, SettingUsage.ACB, GL_RenderType.Mixing);

	public static final SettingBool DEBUG_MapGrid = new SettingBool("DEBUG_MapGrid", SettingCategory.Debug, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
}
