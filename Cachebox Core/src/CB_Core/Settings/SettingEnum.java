package CB_Core.Settings;

import java.util.ArrayList;

public class SettingEnum extends SettingString
{

	private ArrayList<String> values;

	private Enum myDefaultEnum;
	private Enum myEnum;
	
	
	public SettingEnum(String name, SettingCategory category, SettingModus modus, Enum defaultValue, boolean global, Enum enu)
	{
		super(name, category, modus, defaultValue.name(), global);
		myEnum=enu;
		myDefaultEnum=defaultValue;
		
		values= new ArrayList<String>();
		
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

	public  ArrayList<String> getValues()
	{
		return values;
	}
	
	@Override
	public void setValue(String value)
	{
		this.value = value;
		myEnum = Enum.valueOf(myEnum.getDeclaringClass(), value);
	}
	
	public Enum getEnumValue()
	{
		return Enum.valueOf(myEnum.getDeclaringClass(), value);
	}

	public Enum getEnumDefaultValue()
	{
		return Enum.valueOf(myEnum.getDeclaringClass(), defaultValue);
	}

	public void setEnumValue(Enum value)
	{
		this.value = value.name();
		myEnum = value;
	}

}
