package de.CB.TestBase;

import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingInt;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingsList;

public interface Settings extends CB_UI_Base.settings.CB_UI_Base_Settings, CB_Locator.LocatorSettings
{

	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	public static final SettingModus INVISIBLE = SettingModus.Invisible;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;

	public static final SettingBool test = (SettingBool) SettingsList.addSetting(new SettingBool("test", SettingCategory.Internal, NEVER,
			false, SettingStoreType.Global));

	public static final SettingBool MapNorthOriented = new SettingBool("MapNorthOriented", SettingCategory.Map, NORMAL, true,
			SettingStoreType.Global);

	public static final SettingBool ImperialUnits = new SettingBool("ImperialUnits", SettingCategory.Misc, NORMAL, false,
			SettingStoreType.Global);

	// Settings Compass
	public static final SettingInt HardwareCompassLevel = (SettingInt) SettingsList.addSetting(new SettingInt("HardwareCompassLevel",
			SettingCategory.Gps, NORMAL, 5, SettingStoreType.Global));

	public static final SettingBool HardwareCompass = new SettingBool("HardwareCompass", SettingCategory.Gps, NORMAL, true,
			SettingStoreType.Global);

	public static final SettingInt gpsUpdateTime = (SettingInt) SettingsList.addSetting(new SettingInt("gpsUpdateTime",
			SettingCategory.Gps, NORMAL, 500, SettingStoreType.Global));

}
