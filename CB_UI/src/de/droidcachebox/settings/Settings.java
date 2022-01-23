package de.droidcachebox.settings;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.Platform;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Database_Core;
import de.droidcachebox.database.SettingsDatabase;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.translation.Translation;

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

    public void acceptChanges() {
        if (writeToDatabases()) {
            new ButtonDialog(Translation.get("Desc_SettingChangesNeedRestart"), Translation.get("SettingChangesNeedRestart"), MsgBoxButton.OK, MsgBoxIcon.Information).show();
        }
    }

    @Override
    protected Database_Core getSettingsDB() {
        return SettingsDatabase.getInstance();
    }

    @Override
    protected Database_Core getDataDB() {
        // if used from Splash, DataDB is not possible
        if (Platform.canNotUsePlatformSettings())
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
        return Platform.canNotUsePlatformSettings();
    }

}
