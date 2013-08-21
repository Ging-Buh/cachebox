package CB_Core.Settings;

import CB_Utils.Config_Core;
import CB_Utils.Settings.SettingBool;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingEncryptedString;
import CB_Utils.Settings.SettingFolder;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;
import CB_Utils.Settings.SettingString;
import CB_Utils.Settings.SettingsList;

public interface CB_Core_Settings
{
	public static final SettingString GcLogin = (SettingString) SettingsList.addSetting(new SettingString("GcLogin", SettingCategory.Login,
			SettingModus.Normal, "", SettingStoreType.Platform));

	public static final SettingEncryptedString GcAPI = (SettingEncryptedString) SettingsList.addSetting(new SettingEncryptedString("GcAPI",
			SettingCategory.Login, SettingModus.Invisible, "", SettingStoreType.Platform));

	public static final SettingEncryptedString GcAPIStaging = (SettingEncryptedString) SettingsList.addSetting(new SettingEncryptedString(
			"GcAPIStaging", SettingCategory.Login, SettingModus.Invisible, "", SettingStoreType.Platform));

	public static final SettingBool StagingAPI = (SettingBool) SettingsList.addSetting(new SettingBool("StagingAPI",
			SettingCategory.Folder, SettingModus.Expert, false, SettingStoreType.Global));

	// Folder Settings
	public static final SettingFolder DescriptionImageFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder(
			"DescriptionImageFolder", SettingCategory.Folder, SettingModus.Expert, Config_Core.WorkPath + "/repository/images",
			SettingStoreType.Global));

	public static final SettingFolder DescriptionImageFolderLocal = (SettingFolder) SettingsList.addSetting(new SettingFolder(
			"DescriptionImageFolderLocal", SettingCategory.Folder, SettingModus.Never, "", SettingStoreType.Local));

	public static final SettingFolder SpoilerFolder = (SettingFolder) SettingsList.addSetting(new SettingFolder("SpoilerFolder",
			SettingCategory.Folder, SettingModus.Expert, Config_Core.WorkPath + "/repository/spoilers", SettingStoreType.Global));

	public static final SettingFolder SpoilerFolderLocal = (SettingFolder) SettingsList.addSetting(new SettingFolder("SpoilerFolder",
			SettingCategory.Folder, SettingModus.Never, "", SettingStoreType.Local));

}
