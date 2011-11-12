package CB_Core.Settings;

public class SettingBool extends SettingBase
{
	protected boolean value;
	protected boolean defaultValue;

	public SettingBool(String name, SettingCategory category, SettingModus modus, boolean defaultValue, boolean global)	
	{
		super(name, category, modus, global);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	@Override
	public String toDBString() {
		return String.valueOf(value);
	}
	
	@Override
	public boolean fromDBString(String dbString) {
		try {
			value = Boolean.valueOf(dbString);
			return true;
		} catch (Exception ex) {
			value = defaultValue;
			return false;
		}
	}

	@Override
	public void loadDefault() {
		value = defaultValue;
	}
}
