package de.droidcachebox.settings;

import de.droidcachebox.database.Database_Core;

public abstract class SettingsDAO {
    public void writeSetting(Database_Core database, SettingBase<?> setting) {
        String dbString = setting.toDBString();
        if (setting instanceof SettingLongString || setting instanceof SettingStringList) {
            database.writeConfigLongString(setting.name, dbString);
        } else
            database.writeConfigString(setting.name, dbString);
    }

    public void readSettingOrDefault(Database_Core db, SettingBase<?> setting) {
        try {
            String dbString = null;

            if (setting instanceof SettingLongString || setting instanceof SettingStringList) {
                if (setting.name.endsWith("Local")) {
                    try {
                        dbString = db.readConfigString(setting.name.substring(0, setting.name.length() - 5));
                    } catch (Exception ignored) {
                    }
                    if (dbString == null)
                        dbString = db.readConfigLongString(setting.name);
                } else {
                    dbString = db.readConfigLongString(setting.name);
                }
            }

            if (dbString == null) {
                dbString = db.readConfigString(setting.name);
            }

            if (dbString == null) {
                setting.loadDefault();
            } else {
                setting.fromDBString(dbString);
            }

            setting.clearDirty();
        } catch (Exception ex) {
            setting.loadDefault();
        }
    }

    public abstract void writePlatformSetting(SettingBase<?> setting);

    public abstract void readPlatformSetting(SettingBase<?> setting);
}
