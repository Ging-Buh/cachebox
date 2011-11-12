package CB_Core.Settings;

public class SettingString extends SettingBase
{
	protected String value;
	protected String defaultValue;
	
	public SettingString(String name, SettingCategory category, SettingModus modus, String defaultValue) {
		super(name, category, modus);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
