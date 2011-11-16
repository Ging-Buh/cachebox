package CB_Core.Settings;

import java.util.HashMap;
import java.util.Iterator;

import CB_Core.DB.Database;

public class SettingsList extends HashMap<String, SettingBase>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -969846843815877942L;

	public boolean getBool(String key)
	{
		if (this.containsKey(key))
		{
			SettingBase setting = this.get(key);
			if (setting instanceof SettingBool)
			{
				return ((SettingBool) setting).getValue();
			}
		}
		return false;
	}

	public String getString(String key)
	{
		if (this.containsKey(key))
		{
			SettingBase setting = this.get(key);
			if (setting instanceof SettingString)
			{
				return ((SettingString) setting).getValue();
			}
		}
		return "";

	}

	public int getInt(String key)
	{
		if (this.containsKey(key))
		{
			SettingBase setting = this.get(key);
			if (setting instanceof SettingInt)
			{
				return ((SettingInt) setting).getValue();
			}
		}
		return 0;
	}

	public double getDouble(String key)
	{
		if (this.containsKey(key))
		{
			SettingBase setting = this.get(key);
			if (setting instanceof SettingDouble)
			{
				return ((SettingDouble) setting).getValue();
			}
		}
		return 0;
	}

	public void addSetting(SettingBase setting)
	{
		this.put(setting.getName(), setting);
	}

	public void WriteToDB()
	{
		// Write into DB
		SettingsDAO dao = new SettingsDAO();
		Database.Settings.beginTransaction();
		if (Database.Data != null) Database.Data.beginTransaction();

		try
		{
			for (Iterator<SettingBase> it = this.values().iterator(); it.hasNext();)
			{
				SettingBase setting = it.next();
				if (setting.getGlobal())
				{
					dao.WriteToDatabase(Database.Settings, setting);
				}

				else
				{
					if (Database.Data != null) dao.WriteToDatabase(Database.Data, setting);
				}

			}
			Database.Settings.setTransactionSuccessful();
			Database.Settings.endTransaction();
		}
		finally
		{
			if (Database.Data != null)
			{
				Database.Data.setTransactionSuccessful();
				Database.Data.endTransaction();
			}
		}

	}

	public void ReadFromDB()
	{
		// Read from DB
		SettingsDAO dao = new SettingsDAO();
		for (Iterator<SettingBase> it = this.values().iterator(); it.hasNext();)
		{
			SettingBase setting = it.next();
			if (setting.getGlobal()) 
				dao.ReadFromDatabase(Database.Settings, setting);
			else
				dao.ReadFromDatabase(Database.Data, setting);
		}
	}

	public void LoadFromLastValue()
	{
		for (Iterator<SettingBase> it = this.values().iterator(); it.hasNext();)
		{
			SettingBase setting = it.next();
			setting.loadFromLastValue();
		}
	}

	public void SaveToLastValue()
	{
		for (Iterator<SettingBase> it = this.values().iterator(); it.hasNext();)
		{
			SettingBase setting = it.next();
			setting.saveToLastValue();
		}
	}
}
