package CB_Core.Settings;

import CB_Core.Config;

public class SettingDecryptString extends SettingLongString
{

	public SettingDecryptString(String name, SettingCategory category, SettingModus modus, String defaultValue, boolean global)
	{
		super(name, category, modus, defaultValue, global);
	}

	public String getValue()
	{
		return Config.encrypt(this.value);
	}

	public String getDefaultValue()
	{
		return Config.encrypt(this.defaultValue);
	}

	public void setValue(String value)
	{
		this.value = Config.decrypt(value);
	}

}
