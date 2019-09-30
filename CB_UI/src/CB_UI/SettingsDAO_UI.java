package CB_UI;

import CB_UI_Base.Events.PlatformConnector;
import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingsDAO;

public class SettingsDAO_UI extends SettingsDAO {
    @Override
    public void WriteToPlatformSettings(SettingBase<?> setting) {
        PlatformConnector.WriteSetting(setting);
    }

    @Override
    public SettingBase<?> ReadFromPlatformSetting(SettingBase<?> setting) {
        try {
            PlatformConnector.ReadSetting(setting);
            setting.clearDirty();
        } catch (Exception e) {
            setting.loadDefault();
        }
        return setting;
    }
}
