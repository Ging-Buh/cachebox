package CB_Core.Settings;

public class SettingString extends SettingBase
{
	protected String value;
	protected String defaultValue;
	protected String lastValue;

	public SettingString(String name, SettingCategory category, SettingModus modus, String defaultValue, boolean global)
	{
		super(name, category, modus, global);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public String getValue()
	{
		return value;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setValue(String value)
	{

		if (value == null)
		{
			int i = 0;
			i++;
		}

		if (this.value.equals(value)) return;
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
			value = dbString;
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
		if (lastValue == null) throw new IllegalArgumentException("You have never saved the last value! Call SaveToLastValue()");
		value = lastValue;
	}

}
