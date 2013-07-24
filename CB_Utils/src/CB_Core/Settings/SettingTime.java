package CB_Core.Settings;

public class SettingTime extends SettingBase
{
	protected int value;
	protected int defaultValue;
	protected int lastValue;

	public SettingTime(String name, SettingCategory category, SettingModus modus, int defaultValue, SettingStoreType StoreType)
	{
		super(name, category, modus, StoreType);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public int getValue()
	{
		return value;
	}

	public int getMin()
	{

		int sec = value / 1000;

		return sec / 60;
	}

	public int getSec()
	{
		int sec = value / 1000;
		int min = sec / 60;

		return sec - (min * 60);
	}

	public void setMin(int min)
	{
		setValue(((min * 60) + getSec()) * 1000);
	}

	public void setSec(int sec)
	{
		setValue(((getMin() * 60) + sec) * 1000);
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
