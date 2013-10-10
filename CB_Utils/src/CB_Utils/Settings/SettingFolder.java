package CB_Utils.Settings;

import CB_Utils.Config_Core;

public class SettingFolder extends SettingLongString
{

	public SettingFolder(String name, SettingCategory category, SettingModus modus, String defaultValue, SettingStoreType StoreType)
	{
		super(name, category, modus, defaultValue, StoreType);
	}

	@Override
	public String getValue()
	{
		return replacePathSaperator(value);
	}

	@Override
	public String getDefaultValue()
	{
		return replacePathSaperator(defaultValue);
	}

	private String replacePathSaperator(String rep)
	{
		if (rep.startsWith("?"))
		{
			rep = Config_Core.WorkPath + System.getProperty("file.separator") + "Repositories" + rep.substring(1);
		}
		rep = rep.replace("\\", System.getProperty("file.separator"));
		rep = rep.replace("/", System.getProperty("file.separator"));
		return rep;
	}

}
