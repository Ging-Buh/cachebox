package CB_Core.Settings;

import CB_Core.Events.platformConector;

public class SettingsDAO_UI extends SettingsDAO
{
	@Override
	public void WriteToPlatformSettings(SettingBase setting)
	{
		platformConector.WriteSetting(setting);
	}

	@Override
	public void ReadFromPlatformSetting(SettingBase setting)
	{
		try
		{
			platformConector.ReadSetting(setting);
			setting.clearDirty();
		}
		catch (Exception e)
		{
			setting.loadDefault();
		}
	}
}
