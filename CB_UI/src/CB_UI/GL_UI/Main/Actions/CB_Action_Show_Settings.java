package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.GL_UI.Activitys.settings.SettingsActivity;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.settings.CB_UI_Base_Settings;

public class CB_Action_Show_Settings extends CB_Action {

	public CB_Action_Show_Settings() {
		super("settings", MenuID.AID_SHOW_SETTINGS);
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return Sprites.getSprite(IconName.settings.name());
	}

	boolean lastNightValue;

	@Override
	public void Execute() {

		SettingsActivity settingsDialog = new SettingsActivity();
		lastNightValue = CB_UI_Base_Settings.nightMode.getValue();

		settingsDialog.show();
	}

}
