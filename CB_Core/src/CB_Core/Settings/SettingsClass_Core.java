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
	public SettingBool StagingAPI;

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
		addDebugSettings();
	}

	private void addDebugSettings()
	{
		SettingCategory cat = SettingCategory.Folder;
		addSetting(StagingAPI = new SettingBool("StagingAPI", cat, SettingModus.Expert, false, SettingStoreType.Global));
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

	// Read the encrypted AccessToken from the config and check wheter it is
	// correct for Andorid CB

	/**
	 * Read the encrypted AccessToken from the config and check wheter it is correct for Andorid CB
	 * 
	 * @return
	 */
	public String GetAccessToken()
	{
		return GetAccessToken(false);
	}

	/**
	 * Read the encrypted AccessToken from the config and check wheter it is correct for Andorid CB </br> If Url_Codiert==true so the
	 * API-Key is URL-Codiert </br> Like replase '/' with '%2F'</br></br> This is essential for PQ-List
	 * 
	 * @param boolean Url_Codiert
	 * @return
	 */
	public String GetAccessToken(boolean Url_Codiert)
	{
		String act = "";
		if (StagingAPI.getValue())
		{
			act = GcAPIStaging.getValue();
		}
		else
		{
			act = GcAPI.getValue();
		}

		// Prüfen, ob das AccessToken für ACB ist!!!
		if (!(act.startsWith("A"))) return "";
		String result = act.substring(1, act.length());

		// URL encoder
		if (Url_Codiert)
		{
			result = result.replace("/", "%2F");
			result = result.replace("\\", "%5C");
			result = result.replace("+", "%2B");
			result = result.replace("=", "%3D");
		}

		return result;
	}

}
