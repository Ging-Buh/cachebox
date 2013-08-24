package CB_UI.Settings;

import CB_Core.DB.Database;
import CB_Core.Settings.CB_Core_Settings;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.DB.Database_Core;
import CB_Utils.Settings.SettingsDAO;
import CB_Utils.Settings.SettingsList;
import cb_rpc.Settings.CB_Rpc_Settings;

public class SettingsClass extends SettingsList implements CB_Core_Settings, CB_UI_Settings, CB_UI_Base_Settings, CB_Rpc_Settings
{

	private static final long serialVersionUID = 7330937438116889415L;

	public SettingsClass()
	{
		super();

	}

	@Override
	protected Database_Core getSettingsDB()
	{
		return Database.Settings;
	}

	@Override
	protected Database_Core getDataDB()
	{
		return Database.Data;
	}

	@Override
	protected SettingsDAO createSettingsDAO()
	{
		// this is necessary to use the platform settings
		return new SettingsDAO_UI();
	}

}
