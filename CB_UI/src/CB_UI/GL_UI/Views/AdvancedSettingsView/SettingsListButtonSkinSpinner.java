package CB_UI.GL_UI.Views.AdvancedSettingsView;

import CB_Utils.Settings.SettingBase;
import CB_Utils.Settings.SettingCategory;
import CB_Utils.Settings.SettingModus;
import CB_Utils.Settings.SettingStoreType;

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
		 
		return null;
	}

	@Override
	public boolean fromDBString(String dbString)
	{
		 
		return false;
	}

	@Override
	public SettingBase<T> copy()
	{
		// can't copy this obj
		return null;
	}

}
