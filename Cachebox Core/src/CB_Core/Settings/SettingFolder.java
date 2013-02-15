package CB_Core.Settings;

import CB_Core.Config;

public class SettingFolder extends SettingLongString
{

	public SettingFolder(String name, SettingCategory category, SettingModus modus, String defaultValue, SettingStoreType StoreType)
	{
		super(name, category, modus, defaultValue, StoreType);
	}

	@Override
	public String getValue()
	{
		return replasePathSaperator(value);
	}

	@Override
	public String getDefaultValue()
	{
		return replasePathSaperator(defaultValue);
	}

	private String replasePathSaperator(String rep)
	{
		if (rep.startsWith("?"))
		{
			rep = Config.WorkPath + "/Repositories" + rep.substring(1);
		}

		if (rep.contains("\\"))
		{
			rep = rep.replace("\\", "/");
		}
		return rep;
	}

}
