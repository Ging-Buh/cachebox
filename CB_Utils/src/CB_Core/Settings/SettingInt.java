package CB_Core.Settings;

public class SettingInt extends SettingBase
{
	protected int value;
	protected int defaultValue;
	protected int lastValue;

	public SettingInt(String name, SettingCategory category, SettingModus modus, int defaultValue, SettingStoreType StoreType)
	{
		super(name, category, modus, StoreType);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public int getValue()
	{
		return value;
	}

	public int getDefaultValue()
	{
		return defaultValue;
	}

	public void setValue(int value)
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
			value = Integer.valueOf(dbString);
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