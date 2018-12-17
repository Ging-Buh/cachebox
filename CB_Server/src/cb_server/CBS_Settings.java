package cb_server;

import CB_Utils.Settings.*;

public interface CBS_Settings {

	// Abk�rzende Schreibweisen f�r die �bersichlichkeit bei den add Methoden
	public static final SettingModus DEVELOPER = SettingModus.DEVELOPER;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;

	public static final SettingInt Port = new SettingInt("Port", SettingCategory.Login, SettingModus.Normal, 7765, SettingStoreType.Global, SettingUsage.CBS);
	public static final SettingInt PQImportInterval = new SettingInt("PQImportInterval (hours)", SettingCategory.API, SettingModus.Normal, 0, SettingStoreType.Global, SettingUsage.CBS);
	public static final SettingString PQImportNames = new SettingString("PQImportNames", SettingCategory.API, SettingModus.Normal, "", SettingStoreType.Local, SettingUsage.CBS);
	public static final SettingFile CBS_Mapsforge_Map = new SettingFile("CBS_Mapsforge_Map", SettingCategory.Map, NORMAL, "", SettingStoreType.Platform, SettingUsage.CBS, "map");

}
