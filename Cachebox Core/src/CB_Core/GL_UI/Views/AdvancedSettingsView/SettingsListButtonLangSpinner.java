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
public class SettingsListButtonLangSpinner extends SettingBase
{

	public SettingsListButtonLangSpinner(String name, SettingCategory category, SettingModus modus, SettingStoreType StoreType)
	{
		super(name, category, modus, StoreType);

	}

}
