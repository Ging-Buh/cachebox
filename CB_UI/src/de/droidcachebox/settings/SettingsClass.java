package de.droidcachebox.settings;

import de.droidcachebox.CB_UI_Base_Settings;
import de.droidcachebox.CB_UI_Settings;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Database_Core;
import de.droidcachebox.database.SettingsDatabase;
import de.droidcachebox.locator.LocatorSettings;

public class SettingsClass extends SettingsList implements CB_Core_Settings, CB_UI_Settings, CB_UI_Base_Settings, LocatorSettings {

    private static final long serialVersionUID = 7330937438116889415L;

    public SettingsClass() {
        super();
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
