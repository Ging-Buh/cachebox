package CB_Locator;

import CB_Utils.Config_Core;
import CB_Utils.Settings.*;

import static CB_Utils.Settings.SettingCategory.*;
import static CB_Utils.Settings.SettingModus.*;
import static CB_Utils.Settings.SettingStoreType.Global;
import static CB_Utils.Settings.SettingStoreType.Local;
import static CB_Utils.Settings.SettingUsage.ACB;
import static CB_Utils.Settings.SettingUsage.ALL;

public interface LocatorSettings {

    Integer[] Level = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};
    Integer[] CrossLevel = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};

    SettingFolder TileCacheFolder = new SettingFolder("TileCacheFolder", Folder, NEVER, Config_Core.mWorkPath + "/repository/cache", Global, ALL, true);
    SettingFolder TileCacheFolderLocal = new SettingFolder("TileCacheFolderLocal", Folder, NEVER, "", Local, ALL, true);

    SettingIntArray ZoomCross = new SettingIntArray("ZoomCross", Map, EXPERT, 16, Global, ACB, CrossLevel);
    SettingFolder MapPackFolder = new SettingFolder("MapPackFolder", Map, EXPERT, Config_Core.mWorkPath + "/repository/maps", Global, ALL, false);
    SettingFolder RenderThemesFolder = new SettingFolder("RenderThemesFolder", Map, EXPERT, Config_Core.mWorkPath + "/RenderThemes", Global, ALL, false);
    SettingBool PositionMarkerTransparent = new SettingBool("PositionMarkerTransparent", Map, EXPERT, true, Global, ACB);
    SettingIntArray OsmMinLevel = new SettingIntArray("OsmMinLevel", Map, EXPERT, 7, Global, ACB, Level);
    SettingIntArray OsmMaxLevel = new SettingIntArray("OsmMaxLevel", Map, EXPERT, 21, Global, ACB, Level);
    SettingIntArray CompassMapMinZoomLevel = new SettingIntArray("CompassMapMinZoomLevel", Map, EXPERT, 13, Global, ACB, Level);
    SettingIntArray CompassMapMaxZommLevel = new SettingIntArray("CompassMapMaxZommLevel", Map, EXPERT, 21, Global, ACB, Level);
    SettingString UserMap1 = new SettingString("UserMap1", Map, EXPERT, "{JPG}{name:ESRI World_Imagery}http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", Global, ALL);
    SettingString UserMap2 = new SettingString("UserMap2", Map, EXPERT, "", Global, ALL);
    SettingFolder MapPackFolderLocal = new SettingFolder("MapPackFolderLocal", Map, NEVER, Config_Core.mWorkPath + "/repository/maps", Local, ALL, false);
    SettingStringList currentMapLayer = new SettingStringList("CurrentMapLayer", Map, NEVER, new String[]{"Mapnik"}, Global, ACB);
    SettingString CurrentMapOverlayLayer = new SettingString("CurrentMapOverlayLayer", Map, NEVER, "", Global, ACB);
    SettingInt lastZoomLevel = new SettingInt("lastZoomLevel", Map, NEVER, 14, Global, ALL);
    SettingBool ShowAccuracyCircle = new SettingBool("ShowAccuracyCircle", Map, NEVER, true, Global, ACB);
    SettingBool ShowMapCenterCross = new SettingBool("ShowMapCenterCross", Map, NEVER, true, Global, ACB);
    SettingFile MapsforgeDayTheme = new SettingFile("MapsforgeDayTheme", Map, NEVER, "", Global, ACB, "xml");
    SettingFile MapsforgeNightTheme = new SettingFile("MapsforgeNightTheme", Map, NEVER, "", Global, ACB, "xml");
    SettingFile MapsforgeCarDayTheme = new SettingFile("MapsforgeCarDayTheme", Map, NEVER, "CAR", Global, ACB, "xml");
    SettingFile MapsforgeCarNightTheme = new SettingFile("MapsforgeCarNightTheme", Map, NEVER, "CAR", Global, ACB, "xml");
    SettingString MapsforgeDayStyle = new SettingString("MapsforgeDayStyle", Map, NEVER, "", Global, ACB);
    SettingString MapsforgeNightStyle = new SettingString("MapsforgeNightStyle", Map, NEVER, "", Global, ACB);
    SettingString MapsforgeCarDayStyle = new SettingString("MapsforgeCarDayStyle", Map, NEVER, "", Global, ACB);
    SettingString MapsforgeCarNightStyle = new SettingString("MapsforgeCarNightStyle", Map, NEVER, "", Global, ACB);
    SettingString PreferredMapLanguage = new SettingString("MapLanguage", Map, NEVER, "", Global, ALL);

    SettingBool MoveMapCenterWithSpeed = new SettingBool("MoveMapCenterWithSpeed", CarMode, NORMAL, false, Global, ACB);
    SettingInt MoveMapCenterMaxSpeed = new SettingInt("MoveMapCenterMaxSpeed", CarMode, NORMAL, 60, Global, ACB);

    SettingDouble MapInitLatitude = new SettingDouble("MapInitLatitude", Positions, NEVER, -1000, Global, ALL);
    SettingDouble MapInitLongitude = new SettingDouble("MapInitLongitude", Positions, NEVER, -1000, Global, ALL);

}
