package CB_Core.Settings;

public class PlatformSettings
{
	// ------ setPlatformSetting ------
	public interface iPlatformSettings
	{
		public SettingBase Read(SettingBase setting);

		public void Write(SettingBase setting);
	}

	public static iPlatformSettings platformSettingsListner;

	public static void setPlatformSettings(iPlatformSettings listner)
	{
		platformSettingsListner = listner;
	}

	public static SettingBase ReadSetting(SettingBase setting)
	{
		if (platformSettingsListner != null) setting = platformSettingsListner.Read(setting);
		return setting;
	}

	public static void WriteSetting(SettingBase setting)
	{
		if (platformSettingsListner != null) platformSettingsListner.Write(setting);
	}

	// -----------------------------------------
}
