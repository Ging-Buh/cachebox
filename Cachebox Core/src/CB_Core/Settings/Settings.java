package CB_Core.Settings;

public class Settings extends SettingsList
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7330937438116889415L;

	public static Settings settings = new Settings();
	
	// Settings Compass
	public SettingBool HtcCompass;
	public SettingInt HtcLevel;
	// Settings Map
	public SettingBool MapHideMyFinds;
	// Invisible
	public SettingLongString Filter;

	public Settings() {
		// Settings Compass
		addSetting(HtcCompass = new SettingBool("HtcCompass", SettingCategory.Gps, SettingModus.Normal, true, true));
		addSetting(HtcLevel = new SettingInt("HtcLevel", SettingCategory.Gps, SettingModus.Normal, 5, true));
		// Settings Map
		addSetting(MapHideMyFinds = new SettingBool("MapHideMyFinds", SettingCategory.Map, SettingModus.Normal, false, true));
		// Invisible
		addSetting(Filter = new SettingLongString("Filter", SettingCategory.Internal, SettingModus.Invisible, "", false));
	}

	
}
