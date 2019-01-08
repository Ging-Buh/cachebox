package cb_server;

import CB_Utils.Settings.*;

public interface CBS_Settings {

	// Abk�rzende Schreibweisen f�r die �bersichlichkeit bei den add Methoden
	public static final SettingModus DEVELOPER = SettingModus.DEVELOPER;
	public static final SettingModus NORMAL = SettingModus.NORMAL;
	public static final SettingModus EXPERT = SettingModus.EXPERT;
	public static final SettingModus NEVER = SettingModus.NEVER;

	public static final SettingInt Port = new SettingInt("Port", SettingCategory.Login, NEVER, 7765, SettingStoreType.Global, SettingUsage.CBS);
	public static final SettingInt PQImportInterval = new SettingInt("PQImportInterval (hours)", SettingCategory.API, NORMAL, 0, SettingStoreType.Global, SettingUsage.CBS);
	public static final SettingString PQImportNames = new SettingString("PQImportNames", SettingCategory.API, NORMAL, "", SettingStoreType.Local, SettingUsage.CBS);
	public static final SettingFile CBS_Mapsforge_Map = new SettingFile("CBS_Mapsforge_Map", SettingCategory.Map, NORMAL, "", SettingStoreType.Platform, SettingUsage.CBS, "map");

}
