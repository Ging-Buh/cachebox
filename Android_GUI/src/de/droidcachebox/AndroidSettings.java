package de.droidcachebox;

import CB_Utils.Settings.*;

public class AndroidSettings {
    // Abkürzende Schreibweisen für die Übersichlichkeit bei den add Methoden
    public static final SettingModus DEVELOPER = SettingModus.DEVELOPER;
    public static final SettingModus NORMAL = SettingModus.Normal;
    public static final SettingModus EXPERT = SettingModus.Expert;
    public static final SettingModus NEVER = SettingModus.Never;

    public static final SettingBool RunOverLockScreen = (SettingBool) SettingsList.addSetting(new SettingBool("RunOverLockScreen", SettingCategory.Misc, NORMAL, true, SettingStoreType.Global, SettingUsage.ACB));

}
