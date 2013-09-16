package CB_Utils.Settings;

public class SettingString extends SettingBase<String>
{
	public static final String STRING_SPLITTER = "¡";

	public SettingString(String name, SettingCategory category, SettingModus modus, String defaultValue, SettingStoreType StoreType)
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
	public SettingBase<String> copy()
	{
		SettingBase<String> ret = new SettingString(this.name, this.category, this.modus, this.defaultValue, this.storeType);
		ret.value = this.value;
		ret.lastValue = this.lastValue;
		return ret;
	}

}
