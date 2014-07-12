package CB_Utils.Settings;

public class PlatformSettings
{
	// ------ setPlatformSetting ------
	public interface iPlatformSettings
	{
		public SettingBase<?> Read(SettingBase<?> setting);

		public void Write(SettingBase<?> setting);

	}

	public static iPlatformSettings platformSettingsListner;

	public static void setPlatformSettings(iPlatformSettings listner)
	{
		platformSettingsListner = listner;
	}

	public static SettingBase<?> ReadSetting(SettingBase<?> setting)
	{
		if (platformSettingsListner != null) setting = platformSettingsListner.Read(setting);
		return setting;
	}

	public static <T> void WriteSetting(SettingBase<T> setting)
	{
		if (platformSettingsListner != null) platformSettingsListner.Write(setting);
	}

	/**
	 * @return True, if platform settings are set
	 */
	public static boolean canUsePlatformSettings()
	{
		return (platformSettingsListner != null);
	}

}
