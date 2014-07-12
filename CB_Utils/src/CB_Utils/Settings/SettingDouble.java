package CB_Utils.Settings;

public class SettingDouble extends SettingBase<Double>
{

	public SettingDouble(String name, SettingCategory category, SettingModus modus, double defaultValue, SettingStoreType StoreType)
	{
		super(name, category, modus, StoreType);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
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
	public SettingBase<Double> copy()
	{
		SettingBase<Double> ret = new SettingDouble(this.name, this.category, this.modus, this.defaultValue, this.storeType);

		ret.value = this.value;
		ret.lastValue = this.lastValue;

		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof SettingDouble)) return false;

		SettingDouble inst = (SettingDouble) obj;
		if (!(inst.name.equals(this.name))) return false;
		if (inst.value != this.value) return false;

		return true;
	}
}
