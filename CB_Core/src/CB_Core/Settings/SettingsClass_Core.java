package CB_Core.Settings;

import CB_Core.Config_Core;

public abstract class SettingsClass_Core extends SettingsList
{
	private static final long serialVersionUID = 5099780361467147392L;
	public static SettingsClass_Core settings = null;

	// Decrypt
	public SettingString GcLogin;
	public SettingEncryptedString GcAPI;
	public SettingEncryptedString GcAPIStaging;

	// Folder Settings
	public SettingFolder DescriptionImageFolder;
	public SettingFolder DescriptionImageFolderLocal;
	public SettingFolder SpoilerFolder;
	public SettingFolder SpoilerFolderLocal;

	public SettingsClass_Core()
	{
		super();
		settings = this;
		addLogInSettings();
		addFolderSettings();
	}

	private void addFolderSettings()
	{
		SettingCategory cat = SettingCategory.Folder;

		String Work = Config_Core.WorkPath;

		addSetting(DescriptionImageFolder = new SettingFolder("DescriptionImageFolder", cat, SettingModus.Expert, Work
				+ "/repository/images", SettingStoreType.Global));
		addSetting(DescriptionImageFolderLocal = new SettingFolder("DescriptionImageFolder", cat, SettingModus.Never, "",
				SettingStoreType.Local));

		addSetting(SpoilerFolder = new SettingFolder("SpoilerFolder", cat, SettingModus.Expert, Work + "/repository/spoilers",
				SettingStoreType.Global));
		addSetting(SpoilerFolderLocal = new SettingFolder("SpoilerFolder", cat, SettingModus.Never, "", SettingStoreType.Local));
	}

	private void addLogInSettings()
	{
		SettingCategory cat = SettingCategory.Login;

		addSetting(GcLogin = new SettingString("GcLogin", cat, SettingModus.Normal, "", SettingStoreType.Platform));
		addSetting(GcAPI = new SettingEncryptedString("GcAPI", cat, SettingModus.Invisible, "", SettingStoreType.Platform));
		addSetting(GcAPIStaging = new SettingEncryptedString("GcAPIStaging", cat, SettingModus.Invisible, "", SettingStoreType.Platform));
	}
}
