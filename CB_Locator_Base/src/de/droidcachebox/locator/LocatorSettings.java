package de.droidcachebox.locator;

import de.droidcachebox.settings.*;
import de.droidcachebox.utils.Config_Core;

import static de.droidcachebox.settings.SettingCategory.*;
import static de.droidcachebox.settings.SettingModus.*;
import static de.droidcachebox.settings.SettingStoreType.Global;
import static de.droidcachebox.settings.SettingStoreType.Local;
import static de.droidcachebox.settings.SettingUsage.ACB;
import static de.droidcachebox.settings.SettingUsage.ALL;

public interface LocatorSettings {

    Integer[] Level = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};
    Integer[] CrossLevel = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};

    SettingFolder tileCacheFolder = new SettingFolder("TileCacheFolder", Folder, NEVER, Config_Core.workPath + "/repository/cache", Global, ALL, true);
    SettingFolder tileCacheFolderLocal = new SettingFolder("TileCacheFolderLocal", Folder, NEVER, "", Local, ALL, true);

    SettingIntArray ZoomCross = new SettingIntArray("ZoomCross", Map, EXPERT, 16, Global, ACB, CrossLevel);
    SettingFolder MapPackFolder = new SettingFolder("MapPackFolder", Map, EXPERT, Config_Core.workPath + "/repository/maps", Global, ALL, false);
    SettingFolder RenderThemesFolder = new SettingFolder("RenderThemesFolder", Map, EXPERT, Config_Core.workPath + "/RenderThemes", Global, ALL, false);
    SettingBool PositionMarkerTransparent = new SettingBool("PositionMarkerTransparent", Map, EXPERT, true, Global, ACB);
    SettingIntArray OsmMinLevel = new SettingIntArray("OsmMinLevel", Map, EXPERT, 7, Global, ACB, Level);
    SettingIntArray OsmMaxLevel = new SettingIntArray("OsmMaxLevel", Map, EXPERT, 21, Global, ACB, Level);
    SettingIntArray CompassMapMinZoomLevel = new SettingIntArray("CompassMapMinZoomLevel", Map, EXPERT, 13, Global, ACB, Level);
    SettingIntArray CompassMapMaxZommLevel = new SettingIntArray("CompassMapMaxZommLevel", Map, EXPERT, 21, Global, ACB, Level);
    SettingString UserMap1 = new SettingString("UserMap1", Map, EXPERT, "{JPG}{name:ESRI World_Imagery}http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", Global, ALL);
    SettingString UserMap2 = new SettingString("UserMap2", Map, EXPERT, "", Global, ALL);
    SettingFolder MapPackFolderLocal = new SettingFolder("MapPackFolderLocal", Map, NEVER, Config_Core.workPath + "/repository/maps", Local, ALL, false);
    SettingStringList currentMapLayer = new SettingStringList("CurrentMapLayer", Map, NEVER, new String[]{"Mapnik"}, Global, ACB);
    SettingString CurrentMapOverlayLayerName = new SettingString("CurrentMapOverlayLayer", Map, NEVER, "", Global, ACB);
    SettingInt lastZoomLevel = new SettingInt("lastZoomLevel", Map, NEVER, 14, Global, ALL);
    SettingInt mapsForgeSaveZoomLevel = new SettingInt("mapsForgeSaveZoomLevel", Map, EXPERT, 14, Global, ALL);

    SettingBool showRating = new SettingBool("MapShowRating", Map, NEVER, true, Global, ACB);
    SettingBool showDifficultyTerrain = new SettingBool("MapShowDT", Map, NEVER, true, Global, ACB);
    SettingBool showTitles = new SettingBool("MapShowTitles", Map, NEVER, true, Global, ACB);
    SettingBool showAllWaypoints = new SettingBool("ShowAllWaypoints", Map, NEVER, false, Global, ACB);
    SettingBool showAccuracyCircle = new SettingBool("ShowAccuracyCircle", Map, NEVER, true, Global, ACB);
    SettingBool showMapCenterCross = new SettingBool("ShowMapCenterCross", Map, NEVER, true, Global, ACB);
    SettingBool showAtOriginalPosition = new SettingBool("ShowAtOriginalPosition", Map, NEVER, false, Global, ACB);
    SettingBool showDistanceCircle = new SettingBool("ShowDistanceCircle", Map, NEVER, true, Global, ACB);
    SettingBool showInfo = new SettingBool("", Map, NEVER, true, Global, ACB);
    SettingBool isMapNorthOriented = new SettingBool("MapNorthOriented", Map, NEVER, true, Global, ACB);
    SettingBool showDirectLine = new SettingBool("ShowDirektLine", Map, NEVER, false, Global, ACB);
    SettingBool hideMyFinds = new SettingBool("MapHideMyFinds", Map, NEVER, false, Global, ACB);
    SettingInt lastMapToggleBtnState = new SettingInt("LastMapToggleBtnState", Map, NEVER, 0, Global, ACB);

    SettingFile mapsForgeDayTheme = new SettingFile("MapsforgeDayTheme", Map, NEVER, "", Global, ACB, "xml");
    SettingFile mapsForgeNightTheme = new SettingFile("MapsforgeNightTheme", Map, NEVER, "", Global, ACB, "xml");
    SettingFile mapsForgeCarDayTheme = new SettingFile("MapsforgeCarDayTheme", Map, NEVER, "CAR", Global, ACB, "xml");
    SettingFile mapsForgeCarNightTheme = new SettingFile("MapsforgeCarNightTheme", Map, NEVER, "CAR", Global, ACB, "xml");
    SettingString mapsForgeDayStyle = new SettingString("MapsforgeDayStyle", Map, NEVER, "", Global, ACB);
    SettingString mapsForgeNightStyle = new SettingString("MapsforgeNightStyle", Map, NEVER, "", Global, ACB);
    SettingString mapsForgeCarDayStyle = new SettingString("MapsforgeCarDayStyle", Map, NEVER, "", Global, ACB);
    SettingString mapsForgeCarNightStyle = new SettingString("MapsforgeCarNightStyle", Map, NEVER, "", Global, ACB);
    SettingString preferredMapLanguage = new SettingString("MapLanguage", Map, NEVER, "", Global, ALL);

    SettingBool MoveMapCenterWithSpeed = new SettingBool("MoveMapCenterWithSpeed", CarMode, NORMAL, false, Global, ACB);
    SettingInt MoveMapCenterMaxSpeed = new SettingInt("MoveMapCenterMaxSpeed", CarMode, NORMAL, 60, Global, ACB);

    SettingDouble MapInitLatitude = new SettingDouble("MapInitLatitude", Positions, NEVER, -1000, Global, ALL);
    SettingDouble MapInitLongitude = new SettingDouble("MapInitLongitude", Positions, NEVER, -1000, Global, ALL);

}
