package CB_Core.Settings;

public class SettingBase
{
	protected SettingCategory category;
	protected String name;
	protected SettingModus modus;
	
	public SettingBase(String name, SettingCategory category, SettingModus modus) {
		this.name = name;
		this.category = category;
		this.modus = modus;
	}
	
	public String getName() {
		return name;
	}
	
	public SettingCategory getCategory() {
		return category;
	}
	
	public SettingModus getModus() {
		return modus;
	}
}
