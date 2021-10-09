package de.droidcachebox.settings;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Database_Core;
import de.droidcachebox.database.SettingsDatabase;

public class Settings extends SettingsList implements CB_Core_Settings, CB_UI_Settings, CB_UI_Base_Settings, LocatorSettings {

    private static final long serialVersionUID = 7330937438116889415L;
    private static Settings settings;
    private final Config_Core config;

    public Settings() {
        super(); // creates the settingsList containing all settingsFields
        config = new Config_Core(GlobalCore.workPath);
    }

    public static Settings getInstance() {
        if (settings == null)
            settings = new Settings();
        return settings;
    }

    @Override
    protected Database_Core getSettingsDB() {
        return SettingsDatabase.getInstance();
    }

    @Override
    protected Database_Core getDataDB() {
        // if used from Splash, DataDB is not possible
        if (PlatformUIBase.canUsePlatformSettings())
            return CBDB.getInstance();
        else
            return null;
    }

    @Override
    protected SettingsDAO createSettingsDAO() {
        // this is necessary to use the platform settings
        return new SettingsDAO_UI();
    }

    @Override
    protected boolean canUsePlatformSettings() {
        return PlatformUIBase.canUsePlatformSettings();
    }

}
