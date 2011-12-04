package CB_Core.Settings;

public class SettingBase implements Comparable<SettingBase>
{
	protected SettingCategory category;
	protected String name;
	protected SettingModus modus;
	/**
	 * true, if this setting should be stored in global setting databsae, otherwise in local database file
	 */
	protected boolean global;

	/**
	 * saves whethter this setting is changed and needs to be saved
	 */
	protected boolean dirty;

	private static int indexCount = 0;
	private int index = -1;

	public SettingBase(String name, SettingCategory category, SettingModus modus, boolean global)
	{
		this.name = name;
		this.category = category;
		this.modus = modus;
		this.global = global;
		dirty = false;

		index = indexCount++;
	}

	public boolean isDirty()
	{
		return dirty;
	}

	public void setDirty()
	{
		dirty = true;
	}

	public void clearDirty()
	{
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

	@Override
	public int compareTo(SettingBase o)
	{
		return (this.index < o.index ? -1 : (this.index == o.index ? 0 : 1));
	}
}
