package de.droidcachebox.settings;

import de.droidcachebox.CB_UI_Base_Settings;
import de.droidcachebox.CB_UI_Settings;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.Database_Core;
import de.droidcachebox.locator.LocatorSettings;

public class SettingsClass extends SettingsList implements CB_Core_Settings, CB_UI_Settings, CB_UI_Base_Settings, LocatorSettings {

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

    @Override
    protected boolean canUsePlatformSettings() {
        return PlatformUIBase.canUsePlatformSettings();
    }

}
