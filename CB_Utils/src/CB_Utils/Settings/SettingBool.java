package CB_Utils.Settings;

public class SettingBool extends SettingBase<Boolean>
{

	public SettingBool(String name, SettingCategory category, SettingModus modus, boolean defaultValue, SettingStoreType StoreType)
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
	public SettingBase<Boolean> copy()
	{
		SettingBase<Boolean> ret = new SettingBool(this.name, this.category, this.modus, this.defaultValue, this.storeType);

		ret.value = this.value;
		ret.lastValue = this.lastValue;

		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof SettingBool)) return false;

		SettingBool inst = (SettingBool) obj;
		if (!(inst.name.equals(this.name))) return false;
		if (inst.value != this.value) return false;

		return true;
	}
}
