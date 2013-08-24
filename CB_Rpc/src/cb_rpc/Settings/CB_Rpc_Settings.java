package cb_rpc.Settings;

import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingString;

public interface CB_Rpc_Settings
{
	// Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
	public static final SettingModus INVISIBLE = SettingModus.Invisible;
	public static final SettingModus NORMAL = SettingModus.Normal;
	public static final SettingModus EXPERT = SettingModus.Expert;
	public static final SettingModus NEVER = SettingModus.Never;
	public static final SettingModus DEVELOP = SettingModus.develop;

	public static final SettingString CBS_IP = new SettingString("CBS_IP", SettingCategory.CBS, DEVELOP, "", SettingStoreType.Global);

}
