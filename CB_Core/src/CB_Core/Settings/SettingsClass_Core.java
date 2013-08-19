package CB_Core.Settings;

import CB_Utils.Settings.SettingsList;

public abstract class SettingsClass_Core extends SettingsList implements CB_Core_Settings
{
	private static final long serialVersionUID = 5099780361467147392L;
	public static SettingsClass_Core settings = null;

	public SettingsClass_Core()
	{
		super();
		settings = this;
		addSetting(StagingAPI);
		addSetting(DescriptionImageFolder);
		addSetting(DescriptionImageFolderLocal);

		addSetting(SpoilerFolder);
		addSetting(SpoilerFolderLocal);

		addSetting(GcLogin);
		addSetting(GcAPI);
		addSetting(GcAPIStaging);
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
