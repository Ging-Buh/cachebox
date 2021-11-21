package de.droidcachebox.settings;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Database_Core;
import de.droidcachebox.database.SettingsDatabase;

public class Settings extends SettingsList implements AllSettings {

    private static final long serialVersionUID = 7330937438116889415L;
    private static Settings settings;

    private Settings() {
        // creates the settingsList containing all settingsFields
    }

    public static Settings getInstance() {
        if (settings == null) {
            new Config_Core(GlobalCore.workPath); // must execute before creation of SettingsList (cause set workPath in Config_Core)
            settings = new Settings();
        }
        return settings;
    }

    @Override
    protected Database_Core getSettingsDB() {
        return SettingsDatabase.getInstance();
    }

    @Override
    protected Database_Core getDataDB() {
        // if used from Splash, DataDB is not possible
        if (PlatformUIBase.canNotUsePlatformSettings())
            return null;
        else
            return CBDB.getInstance();
    }

    @Override
    protected SettingsDAO createSettingsDAO() {
        // this is necessary to use the platform settings
        return new SettingsDAO_UI();
    }

    @Override
    protected boolean canNotUsePlatformSettings() {
        return PlatformUIBase.canNotUsePlatformSettings();
    }

}
