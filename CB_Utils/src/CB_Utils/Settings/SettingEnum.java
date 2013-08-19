package CB_Utils.Settings;

import java.util.ArrayList;

public class SettingEnum<EnumTyp extends Enum<?>> extends SettingString
{

	private ArrayList<String> values;

	private EnumTyp myEnum;

	@SuppressWarnings("rawtypes")
	public SettingEnum(String name, SettingCategory category, SettingModus modus, EnumTyp defaultValue, SettingStoreType StoreType,
			EnumTyp enu)
	{
		super(name, category, modus, defaultValue.name(), StoreType);
		myEnum = enu;

		values = new ArrayList<String>();

		// hier bekommst du die Klasse TestEnum
		Class c = enu.getDeclaringClass();
		// hier kannst du alle Zustände abfragen
		Object[] oo = c.getEnumConstants();
		// hier kannst du dann über alle Zustände iterieren
		for (Object o : oo)
		{
			// und von jedem den Namen abfragen (in unserem Beispiel "wert1",
			// "wert2", "wert3"
			values.add(((Enum) o).name());
		}

	}

	public ArrayList<String> getValues()
	{
		return values;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(String value)
	{
		if (this.value.equals(value)) return;
		this.value = value;
		myEnum = (EnumTyp) EnumTyp.valueOf(myEnum.getDeclaringClass(), value);
		setDirty();
	}

	@SuppressWarnings(
		{ "unchecked" })
	public EnumTyp getEnumValue()
	{
		return (EnumTyp) Enum.valueOf(myEnum.getDeclaringClass(), value);
	}

	@SuppressWarnings(
		{ "unchecked" })
	public EnumTyp getEnumDefaultValue()
	{
		return (EnumTyp) Enum.valueOf(myEnum.getDeclaringClass(), defaultValue);
	}

	public void setEnumValue(EnumTyp value)
	{
		if (this.myEnum == value) return;
		this.value = value.name();
		myEnum = value;
		setDirty();
	}

}
