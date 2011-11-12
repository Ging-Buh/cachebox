package CB_Core.Settings;

import java.util.HashMap;

public class SettingsList extends HashMap<String, SettingBase>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8281683955382397406L;

	public boolean getBool(String key)
	{
		if (this.containsKey(key))
		{
			SettingBase setting = this.get(key);
			if (setting instanceof SettingBool) {
				return ((SettingBool) setting).getValue();
			}
		}
		return false;
	}

	public String getString(String key)
	{
		if (this.containsKey(key))
		{
			SettingBase setting = this.get(key);
			if (setting instanceof SettingString)
			{
				return ((SettingString) setting).getValue();
			}
		}
		return "";

	}

	public void addSetting(SettingBase setting) {
		this.put(setting.getName(), setting);
	}
}
