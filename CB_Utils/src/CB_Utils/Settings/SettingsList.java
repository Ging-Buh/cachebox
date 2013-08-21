package CB_Utils.Settings;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Utils.DB.Database_Core;
import CB_Utils.Log.Logger;

public abstract class SettingsList extends ArrayList<SettingBase<?>>
{
	private static SettingsList that;

	private static final long serialVersionUID = -969846843815877942L;

	private boolean isLoaded = false;

	public SettingsList()
	{
		that = this;
	}

	public boolean isLoaded()
	{
		return isLoaded;
	}

	public static SettingBase<?> addSetting(SettingBase<?> setting)
	{
		if (that == null) return null;
		if (!that.contains(setting)) that.add(setting);
		return setting;
	}

	protected abstract Database_Core getSettingsDB();

	protected abstract Database_Core getDataDB();

	protected SettingsDAO createSettingsDAO()
	{
		return new SettingsDAO();
	}

	public void WriteToDB()
	{
		// Write into DB
		SettingsDAO dao = createSettingsDAO();
		getSettingsDB().beginTransaction();
		Database_Core Data = getDataDB();

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
			for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext();)
			{
				SettingBase<?> setting = it.next();
				if (!setting.isDirty()) continue; // is not changed -> do not

				if (SettingStoreType.Local == setting.getStoreType())
				{
					if (Data != null) dao.WriteToDatabase(Data, setting);
				}
				else if (SettingStoreType.Global == setting.getStoreType())
				{
					dao.WriteToDatabase(getSettingsDB(), setting);
				}
				else if (SettingStoreType.Platform == setting.getStoreType())
				{
					dao.WriteToPlatformSettings(setting);
				}
				setting.clearDirty();

			}
			if (Data != null) Data.setTransactionSuccessful();
			getSettingsDB().setTransactionSuccessful();
		}
		finally
		{
			getSettingsDB().endTransaction();
			if (Data != null) Data.endTransaction();
		}

	}

	public void ReadFromDB()
	{
		// Read from DB
		try
		{
			Logger.DEBUG("Reading global settings: " + getSettingsDB().getDatabasePath());
			Logger.DEBUG("and local settings: " + getSettingsDB().getDatabasePath());
		}
		catch (Exception e)
		{
			// gibt beim splash - Start: NPE in Translation.readMissingStringsFile
			// Nachfolgende Starts sollten aber protokolliert werden
		}
		SettingsDAO dao = new SettingsDAO();
		for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext();)
		{
			SettingBase<?> setting = it.next();
			if (SettingStoreType.Local == setting.getStoreType())
			{
				dao.ReadFromDatabase(getDataDB(), setting);
			}
			else if (SettingStoreType.Global == setting.getStoreType())
			{
				dao.ReadFromDatabase(getSettingsDB(), setting);
			}
			else if (SettingStoreType.Platform == setting.getStoreType())
			{
				dao.ReadFromPlatformSetting(setting);
			}
		}
		isLoaded = true;
	}

	public void LoadFromLastValue()
	{
		for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext();)
		{
			SettingBase<?> setting = it.next();
			setting.loadFromLastValue();
		}
	}

	public void SaveToLastValue()
	{
		for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext();)
		{
			SettingBase<?> setting = it.next();
			setting.saveToLastValue();
		}
	}

	public void LoadAllDefaultValues()
	{
		for (Iterator<SettingBase<?>> it = this.iterator(); it.hasNext();)
		{
			SettingBase<?> setting = it.next();
			setting.loadDefault();
		}
	}
}
