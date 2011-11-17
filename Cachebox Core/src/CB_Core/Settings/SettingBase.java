package CB_Core.Settings;

public class SettingBase
{
	protected SettingCategory category;
	protected String name;
	protected SettingModus modus;
	protected boolean global; // true, if this setting should be stored in
								// global setting databsae, otherwise in local
								// database file
	protected boolean dirty;	// saves whethter this setting is changed and needs to be saved

	public SettingBase(String name, SettingCategory category, SettingModus modus, boolean global)
	{
		this.name = name;
		this.category = category;
		this.modus = modus;
		this.global = global;
		dirty = false;
	}

	public boolean isDirty() {
		return dirty;
	}
	
	public void setDirty() {
		dirty = true;
	}
	
	public void clearDirty() {
		dirty = false;
	}
	
	public String getName()
	{
		return name;
	}

	public SettingCategory getCategory()
	{
		return category;
	}

	public boolean getGlobal()
	{
		return global;
	}

	public SettingModus getModus()
	{
		return modus;
	}

	public String toDBString()
	{
		return "";
	}

	public boolean fromDBString(String dbString)
	{
		return false;
	}

	public void loadDefault()
	{

	}
	
	public void saveToLastValue()
	{
		
	}
	
	public void loadFromLastValue()
	{

	}	
}
