package CB_Core.Settings;

public class SettingFile extends SettingLongString
{
	private String ext = "*";

	public SettingFile(String name, SettingCategory category, SettingModus modus, String defaultValue, SettingStoreType StoreType)
	{
		super(name, category, modus, defaultValue, StoreType);
	}

	public SettingFile(String name, SettingCategory category, SettingModus modus, String defaultValue, SettingStoreType StoreType,
			String ext)
	{
		super(name, category, modus, defaultValue, StoreType);
		this.ext = ext;
	}

	public String getExt()
	{
		return ext;
	}
}
