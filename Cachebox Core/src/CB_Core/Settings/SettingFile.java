package CB_Core.Settings;

public class SettingFile extends SettingLongString
{
	private String ext = "*";

	public SettingFile(String name, SettingCategory category, SettingModus modus, String defaultValue, boolean global)
	{
		super(name, category, modus, defaultValue, global);
	}

	public SettingFile(String name, SettingCategory category, SettingModus modus, String defaultValue, boolean global, String ext)
	{
		super(name, category, modus, defaultValue, global);
		this.ext = ext;
	}

	public String getExt()
	{
		return ext;
	}
}
