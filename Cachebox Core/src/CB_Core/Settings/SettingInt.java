package CB_Core.Settings;

public class SettingInt extends SettingBase
{
	protected int value;
	protected int defaultValue;
	
	public SettingInt(String name, SettingCategory category, SettingModus modus, int defaultValue) {
		super(name, category, modus);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		this.value = value;
	}
}
