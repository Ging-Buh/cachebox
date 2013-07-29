package CB_Core.Settings;

public class SettingsDAO_UI extends SettingsDAO
{
	@Override
	public void WriteToPlatformSettings(SettingBase setting)
	{
		PlatformSettings.WriteSetting(setting);
	}

	@Override
	public SettingBase ReadFromPlatformSetting(SettingBase setting)
	{
		try
		{
			PlatformSettings.ReadSetting(setting);
			setting.clearDirty();
		}
		catch (Exception e)
		{
			setting.loadDefault();
		}
		return setting;
	}
}
