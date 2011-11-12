package CB_Core.Settings;

import CB_Core.DB.Database;

public class SettingsDAO
{
	public void WriteToDatabase(Database database, SettingBase setting)
	{
		if (setting instanceof SettingLongString) 
			database.WriteConfigLongString(setting.name, setting.toDBString());
		else
			database.WriteConfigString(setting.name, setting.toDBString());
	}

	public void ReadFromDatabase(Database database, SettingBase setting)
	{
		try
		{
			String dbString = "";
			if (setting instanceof SettingLongString) 
				dbString = database.ReadConfigLongString(setting.name);
			else
				dbString = database.ReadConfigString(setting.name);
			setting.fromDBString(dbString);
		}
		catch (Exception ex)
		{
			setting.loadDefault();
		}
	}
}
