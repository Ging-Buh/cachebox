package de.droidcachebox.settings;

import de.droidcachebox.Platform;

public class SettingsDAO_UI extends SettingsDAO {
    @Override
    public void writePlatformSetting(SettingBase<?> setting) {
        Platform.writePlatformSetting(setting);
    }

    @Override
    public SettingBase<?> readPlatformSetting(SettingBase<?> setting) {
        try {
            setting = Platform.readPlatformSetting(setting);
            setting.clearDirty();
        } catch (Exception e) {
            setting.loadDefault();
        }
        return setting;
    }
}
