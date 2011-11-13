package CB_Core.Settings;

public class SettingDouble extends SettingBase
{
	protected double value;
	protected double defaultValue;

	public SettingDouble(String name, SettingCategory category, SettingModus modus, double defaultValue, boolean global)
	{
		super(name, category, modus, global);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public double getValue()
	{
		return value;
	}

	public double getDefaultValue()
	{
		return defaultValue;
	}

	public void setValue(double value)
	{
		this.value = value;
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
			value = Double.valueOf(dbString);
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

}
