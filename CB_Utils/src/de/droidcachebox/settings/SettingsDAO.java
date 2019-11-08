package de.droidcachebox.settings;

import de.droidcachebox.database.Database_Core;

public abstract class SettingsDAO {
    public void WriteToDatabase(Database_Core database, SettingBase<?> setting) {
        String dbString = setting.toDBString();
        if (setting instanceof SettingLongString || setting instanceof SettingStringList) {
            database.writeConfigLongString(setting.name, dbString);
        } else
            database.writeConfigString(setting.name, dbString);
    }

    public SettingBase<?> ReadFromDatabase(Database_Core database, SettingBase<?> setting) {
        try {
            String dbString = null;

            if (setting instanceof SettingLongString || setting instanceof SettingStringList) {
                if (setting.name.endsWith("Local")) {
                    try {
                        dbString = database.readConfigString(setting.name.substring(0, setting.name.length() - 5));
                    } catch (Exception ex) {
                        dbString = null;
                    }
                    if (dbString == null)
                        dbString = database.readConfigLongString(setting.name);
                } else {
                    dbString = database.readConfigLongString(setting.name);
                }
            }

            if (dbString == null) {
                dbString = database.readConfigString(setting.name);
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

        return setting;
    }

    public abstract void writePlatformSetting(SettingBase<?> setting);

    public abstract SettingBase<?> readPlatformSetting(SettingBase<?> setting);
}
