package de.droidcachebox;

import CB_Utils.Settings.*;

public class AndroidSettings {
    // Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
    public static final SettingModus DEVELOPER = SettingModus.DEVELOPER;
    public static final SettingModus NORMAL = SettingModus.NORMAL;
    public static final SettingModus EXPERT = SettingModus.EXPERT;
    public static final SettingModus NEVER = SettingModus.NEVER;

    public static final SettingBool RunOverLockScreen = (SettingBool) SettingsList.addSetting(new SettingBool("RunOverLockScreen", SettingCategory.Misc, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));

}
