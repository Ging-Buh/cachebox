package CB_UI;

import CB_UI_Base.Events.PlatformUIBase;
import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingsDAO;

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
