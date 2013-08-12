package CB_Core.Settings;

import CB_Core.DB.Database_Core;

public class SettingsDAO
{
	public void WriteToDatabase(Database_Core database, SettingBase<?> setting)
	{
		String dbString = setting.toDBString();
		if (setting instanceof SettingLongString)
		{
			database.WriteConfigLongString(setting.name, dbString);
		}
		else
			database.WriteConfigString(setting.name, dbString);
	}

	public SettingBase<?> ReadFromDatabase(Database_Core database, SettingBase<?> setting)
	{
		try
		{
			String dbString = null;

			if (setting instanceof SettingLongString)
			{
				dbString = database.ReadConfigLongString(setting.name);
			}

			if (dbString == null)
			{
				dbString = database.ReadConfigString(setting.name);
			}

			if (dbString == null)
			{
				setting.loadDefault();
			}
			else
			{
				setting.fromDBString(dbString);
			}

			setting.clearDirty();
		}
		catch (Exception ex)
		{
			setting.loadDefault();
		}

		return setting;
	}

	public void WriteToPlatformSettings(SettingBase<?> setting)
	{
		// to use the PlatformSettings -> a new class must be generated on base of this
		PlatformSettings.WriteSetting(setting);
	}

	public SettingBase<?> ReadFromPlatformSetting(SettingBase<?> setting)
	{
		setting = PlatformSettings.ReadSetting(setting);
		return setting;
	}
}
