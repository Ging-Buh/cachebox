package CB_Core.Settings;

/***
 * Login("Login"), Map("Map"), Gps("Gps"), Internal("Internal"), Folder("Folder"), Button("Button"), Misc("Misc"), Templates("Templates"),
 * API("API"), Debug("Debug")
 */
public enum SettingCategory
{
	Login("Login"),
	Map("Map"),
	Gps("Gps"),
	Misc("Misc"),
	API("API"),
	Folder("Folder"),
	Templates("Templates"),
	Internal("Internal"),
	Debug("Debug"),
	Button("Button"), ;

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

	private boolean mIsCollapse = false;

	public boolean IsCollapse()
	{
		return mIsCollapse;
	}

	public void Toggle()
	{
		mIsCollapse = !mIsCollapse;
	}

	public void Toggle(boolean value)
	{
		mIsCollapse = value;
	}

}
