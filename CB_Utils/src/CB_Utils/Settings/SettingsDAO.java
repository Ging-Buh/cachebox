package CB_Utils.Settings;

import de.cb.sqlite.Database_Core;

public abstract class SettingsDAO {
    public void WriteToDatabase(Database_Core database, SettingBase<?> setting) {
        String dbString = setting.toDBString();
        if (setting instanceof SettingLongString || setting instanceof SettingStringList) {
            database.WriteConfigLongString(setting.name, dbString);
        } else
            database.WriteConfigString(setting.name, dbString);
    }

    public SettingBase<?> ReadFromDatabase(Database_Core database, SettingBase<?> setting) {
        try {
            String dbString = null;

            if (setting instanceof SettingLongString || setting instanceof SettingStringList) {
                if (setting.name.endsWith("Local")) {
                    try {
                        dbString = database.ReadConfigString(setting.name.substring(0, setting.name.length() - 5));
                    } catch (Exception ex) {
                        dbString = null;
                    }
                    if (dbString == null)
                        dbString = database.ReadConfigLongString(setting.name);
                } else {
                    dbString = database.ReadConfigLongString(setting.name);
                }
            }

            if (dbString == null) {
                dbString = database.ReadConfigString(setting.name);
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

    public abstract void WriteToPlatformSettings(SettingBase<?> setting);

    public abstract SettingBase<?> ReadFromPlatformSetting(SettingBase<?> setting);
}
