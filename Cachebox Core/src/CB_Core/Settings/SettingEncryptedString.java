package CB_Core.Settings;

import CB_Core.Config;

public class SettingEncryptedString extends SettingLongString
{
	// im value intern ist die Einstellung verschlüsselt abgespeichert
	// so wie sie dann in die DB geschrieben wird.
	public SettingEncryptedString(String name, SettingCategory category, SettingModus modus, String defaultValue, boolean global)
	{
		super(name, category, modus, defaultValue, global);
	}

	// liefert die Einstellung im Klartext
	public String getValue()
	{
		return Config.decrypt(this.value);
	}
	
	// Liefert die verschlüsselte Einstellung zurück
	public String getEncryptedValue()
	{
		return this.value;
	}

	// liefert den Standardwert im Klartext
	public String getDefaultValue()
	{
		return Config.decrypt(this.defaultValue);
	}

	// liefert den verschlüsselten Standadwert 
	public String getEncryptedDefaultValue()
	{
		return this.defaultValue;
	}

	// hiermit kann die Einstellung im Klartext übergeben werden und wird sofort verschlüsselt
	public void setValue(String value)
	{
		this.value = Config.encrypt(value);
	}
	
	// hier kann die schon verschlüsselte Einstellung übergeben werden.
	public void setEncryptedValue(String value) 
	{
		this.value = value;
	}

}
