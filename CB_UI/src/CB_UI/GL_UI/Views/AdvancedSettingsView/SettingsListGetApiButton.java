package CB_UI.GL_UI.Views.AdvancedSettingsView;

import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;

/**
 * Der Button der sich hinter einer Category verbirgt und in der Settings List als Toggle Button dieser Category angezeigt wird.
 * 
 * @author Longri
 * @param <T>
 */
public class SettingsListGetApiButton<T> extends SettingBase<T>
{

	public SettingsListGetApiButton(String name, SettingCategory category, SettingModus modus, SettingStoreType StoreType)
	{
		super(name, category, modus, StoreType);

	}

	@Override
	public String toDBString()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean fromDBString(String dbString)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
