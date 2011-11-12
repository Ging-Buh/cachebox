package CB_Core.Settings;

public enum SettingCategory {
	Login("Login"), Map("Map"), Gps("Gps");

	private String langString;
	
	SettingCategory(String langString) {
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


