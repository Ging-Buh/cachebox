package CB_Utils.Settings;

/**
 * The audio settings contain several settings!<br>
 * <br>
 * Path to the sound file<br>
 * Absolute or ClassPath<br>
 * Mute<br>
 * Volume<br>
 * <br>
 * These are written with the separator '#' in a string.<br>
 * 
 * @author Longri
 */
public class SettingsAudio extends SettingBase<Audio>
{

	public SettingsAudio(String name, SettingCategory category, SettingModus modus, Audio defaultValue, SettingStoreType StoreType)
	{
		super(name, category, modus, StoreType);
		this.defaultValue = defaultValue;
		this.value = new Audio(defaultValue);
	}

	@Override
	public int compareTo(SettingBase<Audio> arg0)
	{
		// no sort
		return 0;
	}

	@Override
	public String toDBString()
	{
		String ret = "";
		ret += "#" + value.Path;
		ret += "#" + String.valueOf(value.Volume);
		ret += "#" + String.valueOf(value.Mute);
		ret += "#" + String.valueOf(value.Class_Absolute);
		return ret;
	}

	@Override
	public boolean fromDBString(String dbString)
	{
		String[] values = dbString.split("#");
		value.Path = values[1];
		value.Volume = Float.parseFloat(values[2]);
		value.Mute = Boolean.parseBoolean(values[3]);
		value.Class_Absolute = Boolean.parseBoolean(values[4]);
		return false;
	}

	@Override
	public SettingBase<Audio> copy()
	{
		SettingBase<Audio> ret = new SettingsAudio(this.name, this.category, this.modus, this.defaultValue, this.storeType);
		ret.value = this.value;
		ret.lastValue = this.lastValue;
		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof SettingsAudio)) return false;

		SettingsAudio inst = (SettingsAudio) obj;
		if (!(inst.name.equals(this.name))) return false;
		if (inst.value.Mute != this.value.Mute) return false;
		if (inst.value.Volume != this.value.Volume) return false;
		if (inst.value.Class_Absolute != this.value.Class_Absolute) return false;
		if (!inst.value.Path.equals(this.value.Path)) return false;

		return true;
	}
}
