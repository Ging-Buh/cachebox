package CB_UI;

import CB_Core.CB_Core_Settings;
import CB_Core.Database;
import CB_Locator.LocatorSettings;
import CB_UI_Base.settings.CB_UI_Base_Settings;
import CB_Utils.Settings.SettingsDAO;
import CB_Utils.Settings.SettingsList;
import cb_rpc.Settings.CB_Rpc_Settings;
import de.cb.sqlite.Database_Core;

public class SettingsClass extends SettingsList implements CB_Core_Settings, CB_UI_Settings, CB_UI_Base_Settings, CB_Rpc_Settings, LocatorSettings {

    private static final long serialVersionUID = 7330937438116889415L;

    public SettingsClass() {
        super();
    }

    @Override
    protected Database_Core getSettingsDB() {
        return Database.Settings;
    }

    @Override
    protected Database_Core getDataDB() {
        return Database.Data;
    }

    @Override
    protected SettingsDAO createSettingsDAO() {
        // this is necessary to use the platform settings
        return new SettingsDAO_UI();
    }

}
