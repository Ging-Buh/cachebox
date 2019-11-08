package de.droidcachebox.settings;

import de.droidcachebox.PlatformUIBase;

public class SettingsDAO_UI extends SettingsDAO {
    @Override
    public void writePlatformSetting(SettingBase<?> setting) {
        PlatformUIBase.writePlatformSetting(setting);
    }

    @Override
    public SettingBase<?> readPlatformSetting(SettingBase<?> setting) {
        try {
            setting = PlatformUIBase.readPlatformSetting(setting);
            setting.clearDirty();
        } catch (Exception e) {
            setting.loadDefault();
        }
        return setting;
    }
}
