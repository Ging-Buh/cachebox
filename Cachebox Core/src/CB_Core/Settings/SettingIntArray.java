package CB_Core.Settings;

public class SettingIntArray extends SettingInt
{

	private Integer values[];

	public SettingIntArray(String name, SettingCategory category, SettingModus modus, int defaultValue, SettingStoreType StoreType,
			Integer arr[])
	{
		super(name, category, modus, defaultValue, StoreType);
		values = arr;
	}

	public Integer[] getValues()
	{
		return values;
	}

	public int getIndex()
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i] == value) return i;
		}
		return -1;
	}

	public int getValueFromIndex(int index)
	{
		return values[index];
	}
}
