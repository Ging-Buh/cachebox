package CB_Core.Settings;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.DB.Database;

public abstract class SettingsList extends ArrayList<SettingBase>
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

	public void addSetting(SettingBase setting)
	{
		this.add(setting);
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
			for (Iterator<SettingBase> it = this.iterator(); it.hasNext();)
			{
				SettingBase setting = it.next();
				if (!setting.isDirty()) continue; // is not changed -> do not

				switch (setting.getStoreType().ordinal())
				{
				case 0: // Global
					dao.WriteToDatabase(Database.Settings, setting);
					break;

				case 1:
					if (Data != null) dao.WriteToDatabase(Data, setting);
					break;

				case 2:
					dao.WriteToPlatformSettings(setting);
					break;
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
		for (Iterator<SettingBase> it = this.iterator(); it.hasNext();)
		{
			SettingBase setting = it.next();

			switch (setting.getStoreType().ordinal())
			{
			case 0: // Global
				dao.ReadFromDatabase(Database.Settings, setting);
				break;

			case 1:
				dao.ReadFromDatabase(Database.Data, setting);
				break;

			case 2:
				dao.ReadFromPlatformSetting(setting);
				break;
			}
		}
		isLoaded = true;
	}

	public void LoadFromLastValue()
	{
		for (Iterator<SettingBase> it = this.iterator(); it.hasNext();)
		{
			SettingBase setting = it.next();
			setting.loadFromLastValue();
		}
	}

	public void SaveToLastValue()
	{
		for (Iterator<SettingBase> it = this.iterator(); it.hasNext();)
		{
			SettingBase setting = it.next();
			setting.saveToLastValue();
		}
	}

	public void LoadAllDefaultValues()
	{
		for (Iterator<SettingBase> it = this.iterator(); it.hasNext();)
		{
			SettingBase setting = it.next();
			setting.loadDefault();
		}
	}
}
