package CB_Core.Settings;

public class SettingBool extends SettingBase
{
	protected boolean value;
	protected boolean defaultValue;
	protected boolean lastValue;

	public SettingBool(String name, SettingCategory category, SettingModus modus, boolean defaultValue, boolean global)
	{
		super(name, category, modus, global);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public boolean getValue()
	{
		return value;
	}

	public boolean getDefaultValue()
	{
		return defaultValue;
	}

	public void setValue(boolean value)
	{
		if (this.value == value) return;
		this.value = value;
		setDirty();
	}

	@Override
	public String toDBString()
	{
		return String.valueOf(value);
	}

	@Override
	public boolean fromDBString(String dbString)
	{
		try
		{
			value = Boolean.valueOf(dbString);
			return true;
		}
		catch (Exception ex)
		{
			value = defaultValue;
			return false;
		}
	}

	@Override
	public void loadDefault()
	{
		value = defaultValue;
	}

	@Override
	public void saveToLastValue()
	{
		lastValue = value;
	}

	@Override
	public void loadFromLastValue()
	{
		value = lastValue;
	}

}
