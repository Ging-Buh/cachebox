package CB_Core.Settings;

import CB_Core.DB.Database;
import CB_Core.Events.platformConector;

public class SettingsDAO
{
	public void WriteToDatabase(Database database, SettingBase setting)
	{
		String dbString = setting.toDBString();
		if (setting instanceof SettingLongString)
		{
			database.WriteConfigLongString(setting.name, dbString);
		}
		else
			database.WriteConfigString(setting.name, dbString);
	}

	public void ReadFromDatabase(Database database, SettingBase setting)
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
	}

	public void WriteToPlatformSettings(SettingBase setting)
	{
		platformConector.WriteSetting(setting);
	}

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
