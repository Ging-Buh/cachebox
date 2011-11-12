package CB_Core.Settings;

public class SettingBool extends SettingBase
{
	protected boolean value;
	protected boolean defaultValue;

	public SettingBool(String name, SettingCategory category, SettingModus modus, boolean defaultValue)	
	{
		super(name, category, modus);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}
}
