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

	private boolean isLoaded = false;

	public boolean isLoaded()
	{
		return isLoaded;
	}

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
		Database Data = Database.Data;

		try
		{
			if (Data != null) Data.beginTransaction();
		}
		catch (Exception ex)
		{
			// do not change Data now!
			Data = null;
		}

		try
		{
			for (Iterator<SettingBase> it = this.values().iterator(); it.hasNext();)
			{
				SettingBase setting = it.next();
				if (!setting.isDirty()) continue; // is not changed -> do not
													// need to be stored

				if (setting.getGlobal())
				{
					dao.WriteToDatabase(Database.Settings, setting);
				}

				else
				{
					if (Data != null) dao.WriteToDatabase(Data, setting);
				}
				// remember that this setting now is stored
				setting.clearDirty();

			}
			if (Data != null) Data.setTransactionSuccessful();
			Database.Settings.setTransactionSuccessful();
		}
		finally
		{
			Database.Settings.endTransaction();
			if (Data != null) Data.endTransaction();
		}

	}

	public void ReadFromDB()
	{
		// Read from DB
		SettingsDAO dao = new SettingsDAO();
		for (Iterator<SettingBase> it = this.values().iterator(); it.hasNext();)
		{
			SettingBase setting = it.next();
			if (setting.getGlobal()) dao.ReadFromDatabase(Database.Settings, setting);
			else
				dao.ReadFromDatabase(Database.Data, setting);
		}
		isLoaded = true;
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

	public void LoadAllDefaultValues()
	{
		for (Iterator<SettingBase> it = this.values().iterator(); it.hasNext();)
		{
			SettingBase setting = it.next();
			setting.loadDefault();
		}
	}
}
