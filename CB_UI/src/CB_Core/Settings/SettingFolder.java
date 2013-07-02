package CB_Core.Settings;

import CB_Core.Config;
import CB_Core.GlobalCore;

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
			rep = Config.WorkPath + GlobalCore.fs + "Repositories" + rep.substring(1);
		}
		rep = rep.replace("\\", GlobalCore.fs);
		rep = rep.replace("/", GlobalCore.fs);
		return rep;
	}

}
