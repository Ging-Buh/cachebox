package CB_Core.Settings;

/***
 * Login("Login"), Map("Map"), Gps("Gps"), Internal("Internal")
 */
public enum SettingCategory
{
	Login("Login"), Map("Map"), Gps("Gps"), Internal("Internal");

	private String langString;

	SettingCategory(String langString)
	{
		this.setLangString(langString);
	}

	public String getLangString()
	{
		return langString;
	}

	public void setLangString(String langString)
	{
		this.langString = langString;
	}
}
