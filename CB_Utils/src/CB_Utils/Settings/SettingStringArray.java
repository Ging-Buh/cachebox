package CB_Utils.Settings;

public class SettingStringArray extends SettingString
{

	private String possibleValues[];

	public SettingStringArray(String name, SettingCategory category, SettingModus modus, String defaultValue, SettingStoreType StoreType,
			String possibleValues[])
	{
		super(name, category, modus, defaultValue, StoreType);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
		this.possibleValues = possibleValues;
	}

	public String[] possibleValues()
	{
		return possibleValues;
	}

	public int getIndexOfValue()
	{
		for (int i = 0; i < possibleValues.length; i++)
		{
			if (possibleValues[i].equals(value)) return i;
		}
		return -1;
	}

	public String getValueFromIndex(int index)
	{
		return possibleValues[index];
	}
}
