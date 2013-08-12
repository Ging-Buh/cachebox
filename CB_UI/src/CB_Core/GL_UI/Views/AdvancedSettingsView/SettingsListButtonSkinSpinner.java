package CB_Core.GL_UI.Views.AdvancedSettingsView;

import CB_Core.Settings.SettingBase;
import CB_Core.Settings.SettingCategory;
import CB_Core.Settings.SettingModus;
import CB_Core.Settings.SettingStoreType;

/**
 * Der Button der sich hinter einer Category verbirgt und in der Settings List als Toggle Button dieser Category angezeigt wird.
 * 
 * @author Longri
 */
public class SettingsListButtonSkinSpinner<T> extends SettingBase<T>
{

	public SettingsListButtonSkinSpinner(String name, SettingCategory category, SettingModus modus, SettingStoreType StoreType)
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
