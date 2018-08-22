package CB_Locator;

import CB_UI_Base.graphics.GL_RenderType;
import CB_Utils.Config_Core;
import CB_Utils.Settings.*;

public interface LocatorSettings {
    // Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
    SettingModus DEVELOPER = SettingModus.DEVELOPER;
    SettingModus NORMAL = SettingModus.Normal;
    SettingModus EXPERT = SettingModus.Expert;
    SettingModus NEVER = SettingModus.Never;

    Integer Level[] = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};
    Integer CrossLevel[] = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};

    SettingFolder TileCacheFolder = new SettingFolder("TileCacheFolder", SettingCategory.Folder, NEVER, Config_Core.mWorkPath + "/repository/cache", SettingStoreType.Global, SettingUsage.ALL, true);
    SettingFolder TileCacheFolderLocal = new SettingFolder("TileCacheFolderLocal", SettingCategory.Folder, NEVER, "", SettingStoreType.Local, SettingUsage.ALL, true);
    SettingFolder MapPackFolder = new SettingFolder("MapPackFolder", SettingCategory.Map, EXPERT, Config_Core.mWorkPath + "/repository/maps", SettingStoreType.Global, SettingUsage.ALL, false);
    SettingFolder MapPackFolderLocal = new SettingFolder("MapPackFolderLocal", SettingCategory.Map, NEVER, Config_Core.mWorkPath + "/repository/maps", SettingStoreType.Local, SettingUsage.ALL, false);

    SettingStringList CurrentMapLayer = (SettingStringList) SettingsList.addSetting(new SettingStringList("CurrentMapLayer", SettingCategory.Map, DEVELOPER, new String[]{"Mapnik"}, SettingStoreType.Global, SettingUsage.ACB));
    SettingString CurrentMapOverlayLayer = (SettingString) SettingsList.addSetting(new SettingString("CurrentMapOverlayLayer", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));

    SettingDouble MapInitLatitude = new SettingDouble("MapInitLatitude", SettingCategory.Positions, EXPERT, -1000, SettingStoreType.Global, SettingUsage.ALL);

    SettingDouble MapInitLongitude = new SettingDouble("MapInitLongitude", SettingCategory.Positions, EXPERT, -1000, SettingStoreType.Global, SettingUsage.ALL);

    SettingInt lastZoomLevel = (SettingInt) SettingsList.addSetting(new SettingInt("lastZoomLevel", SettingCategory.Map, DEVELOPER, 14, SettingStoreType.Global, SettingUsage.ALL));

    SettingBool MoveMapCenterWithSpeed = new SettingBool("MoveMapCenterWithSpeed", SettingCategory.CarMode, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);

    SettingInt MoveMapCenterMaxSpeed = (SettingInt) SettingsList.addSetting(new SettingInt("MoveMapCenterMaxSpeed", SettingCategory.CarMode, NORMAL, 60, SettingStoreType.Global, SettingUsage.ACB));

    SettingBool ShowAccuracyCircle = new SettingBool("ShowAccuracyCircle", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);
    SettingBool ShowMapCenterCross = new SettingBool("ShowMapCenterCross", SettingCategory.Map, NEVER, true, SettingStoreType.Global, SettingUsage.ACB);

    SettingBool PositionMarkerTransparent = new SettingBool("PositionMarkerTransparent", SettingCategory.Map, EXPERT, true, SettingStoreType.Global, SettingUsage.ACB);

    SettingIntArray OsmMinLevel = new SettingIntArray("OsmMinLevel", SettingCategory.Map, EXPERT, 7, SettingStoreType.Global, SettingUsage.ACB, Level);
    SettingIntArray OsmMaxLevel = new SettingIntArray("OsmMaxLevel", SettingCategory.Map, EXPERT, 19, SettingStoreType.Global, SettingUsage.ACB, Level);
    SettingIntArray CompassMapMinZoomLevel = new SettingIntArray("CompassMapMinZoomLevel", SettingCategory.Map, EXPERT, 13, SettingStoreType.Global, SettingUsage.ACB, Level);
    SettingIntArray CompassMapMaxZommLevel = new SettingIntArray("CompassMapMaxZommLevel", SettingCategory.Map, EXPERT, 20, SettingStoreType.Global, SettingUsage.ACB, Level);

    SettingFolder RenderThemesFolder = new SettingFolder("RenderThemesFolder", SettingCategory.Map, NORMAL, Config_Core.mWorkPath + "/RenderThemes", SettingStoreType.Global, SettingUsage.ALL, false);
    SettingFile MapsforgeDayTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeDayTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
    SettingFile MapsforgeNightTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeNightTheme", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB, "xml"));
    SettingFile MapsforgeCarDayTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeCarDayTheme", SettingCategory.Map, NEVER, "CAR", SettingStoreType.Global, SettingUsage.ACB, "xml"));
    SettingFile MapsforgeCarNightTheme = (SettingFile) SettingsList.addSetting(new SettingFile("MapsforgeCarNightTheme", SettingCategory.Map, NEVER, "CAR", SettingStoreType.Global, SettingUsage.ACB, "xml"));
    SettingString MapsforgeDayStyle = (SettingString) SettingsList.addSetting(new SettingString("MapsforgeDayStyle", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
    SettingString MapsforgeNightStyle = (SettingString) SettingsList.addSetting(new SettingString("MapsforgeNightStyle", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
    SettingString MapsforgeCarDayStyle = (SettingString) SettingsList.addSetting(new SettingString("MapsforgeCarDayStyle", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));
    SettingString MapsforgeCarNightStyle = (SettingString) SettingsList.addSetting(new SettingString("MapsforgeCarNightStyle", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ACB));

    SettingString UserMap1 = (SettingString) SettingsList.addSetting(
            new SettingString("UserMap1", SettingCategory.Map, EXPERT, "{JPG}{name:ESRI World_Imagery}http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}", SettingStoreType.Global, SettingUsage.ALL));
    SettingString UserMap2 = (SettingString) SettingsList.addSetting(new SettingString("UserMap2", SettingCategory.Map, EXPERT, "", SettingStoreType.Global, SettingUsage.ALL));
    SettingString PreferredMapLanguage = (SettingString) SettingsList.addSetting(new SettingString("MapLanguage", SettingCategory.Map, NEVER, "", SettingStoreType.Global, SettingUsage.ALL));

    SettingEnum<GL_RenderType> MapsforgeRenderType = new SettingEnum<GL_RenderType>("MapsforgeRenderType", SettingCategory.Map, EXPERT, GL_RenderType.Mapsforge, SettingStoreType.Global, SettingUsage.ACB, GL_RenderType.Mapsforge);

    SettingBool DEBUG_MapGrid = new SettingBool("DEBUG_MapGrid", SettingCategory.Debug, NORMAL, false, SettingStoreType.Global, SettingUsage.ACB);
}
