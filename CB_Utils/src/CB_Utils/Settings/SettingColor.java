package CB_Utils.Settings;

import CB_Utils.Util.HSV_Color;

import com.badlogic.gdx.graphics.Color;

public class SettingColor extends SettingBase<Color>
{

	public SettingColor(String name, SettingCategory category, SettingModus modus, Color defaultValue, SettingStoreType StoreType)
	{
		super(name, category, modus, StoreType);
		this.defaultValue = defaultValue;
	}

	@Override
	public String toDBString()
	{
		return value.toString();
	}

	@Override
	public boolean fromDBString(String dbString)
	{
		try
		{
			value = new HSV_Color(dbString);
			return true;
		}
		catch (Exception e)
		{
			value = defaultValue;
			return false;
		}
	}

	@Override
	public SettingBase<Color> copy()
	{
		SettingBase<Color> ret = new SettingColor(this.name, this.category, this.modus, this.defaultValue, this.storeType);
		ret.value = this.value;
		ret.lastValue = this.lastValue;
		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof SettingColor)) return false;

		SettingColor inst = (SettingColor) obj;
		if (!(inst.name.equals(this.name))) return false;
		if (!inst.value.equals(this.value)) return false;

		return true;
	}

}
