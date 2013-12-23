package CB_Utils.Settings;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
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

		// add Member to list
		Member[] mbrs = this.getClass().getFields();

		for (Member mbr : mbrs)
		{
			if (mbr instanceof Field)
			{
				try
				{
					Object obj = ((Field) mbr).get(this);
					if (obj instanceof SettingBase<?>)
					{
						add((SettingBase<?>) obj);
					}
				}
				catch (IllegalArgumentException e)
				{

					e.printStackTrace();
				}
				catch (IllegalAccessException e)
				{

					e.printStackTrace();
				}
			}

		}

		mbrs = null;
	}

	public boolean isLoaded()
	{
		return isLoaded;
	}

	public static SettingBase<?> addSetting(SettingBase<?> setting)
	{

		if (that == null) try
		{
			throw new InstantiationException("Settings List not initial");
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
			return null;
		}
		that.add(setting);
		if (!that.contains(setting))
		{

		}
		else
		{
			String stop = "";
			Logger.LogCat(stop);
		}
		return setting;
	}

	@Override
	public boolean add(SettingBase<?> setting)
	{
		if (!that.contains(setting))
		{
			return super.add(setting);
		}
		return false;
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
					dao.WriteToDatabase(getSettingsDB(), setting);
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
				setting = dao.ReadFromDatabase(getDataDB(), setting);
			}
			else if (SettingStoreType.Global == setting.getStoreType())
			{
				setting = dao.ReadFromDatabase(getSettingsDB(), setting);
			}
			else if (SettingStoreType.Platform == setting.getStoreType())
			{
				SettingBase<?> cpy = setting.copy();
				cpy = dao.ReadFromDatabase(getSettingsDB(), cpy);
				setting = dao.ReadFromPlatformSetting(setting);

				// chk for Value on User.db3 and cleared Platform Value

				if (setting instanceof SettingString)
				{
					SettingString st = (SettingString) setting;

					if (st.value.length() == 0)
					{
						// Platform Settings are empty use db3 value or default
						setting = dao.ReadFromDatabase(getSettingsDB(), setting);
						dao.WriteToPlatformSettings(setting);
					}
				}
				else if (!cpy.value.equals(setting.value))
				{
					if (setting.value.equals(setting.defaultValue))
					{
						// override Platformsettings with UserDBSettings
						setting.setValueFrom(cpy);
						dao.WriteToPlatformSettings(setting);
						setting.clearDirty();
					}
					else
					{
						// override UserDBSettings with Platformsettings
						cpy.setValueFrom(setting);
						dao.WriteToDatabase(getSettingsDB(), cpy);
						cpy.clearDirty();
					}
				}

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
