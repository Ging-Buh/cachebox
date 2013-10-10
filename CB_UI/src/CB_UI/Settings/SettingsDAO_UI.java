package CB_UI.Settings;

import CB_Utils.Settings.PlatformSettings;
import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingsDAO;

public class SettingsDAO_UI extends SettingsDAO
{
	@Override
	public void WriteToPlatformSettings(SettingBase<?> setting)
	{
		PlatformSettings.WriteSetting(setting);
	}

	@Override
	public SettingBase<?> ReadFromPlatformSetting(SettingBase<?> setting)
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
