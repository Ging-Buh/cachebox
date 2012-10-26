package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Activitys.settings.SettingsActivity;
import CB_Core.GL_UI.Controls.Dialogs.WaitDialog;
import CB_Core.GL_UI.Menu.MenuID;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_Show_Settings extends CB_ActionCommand
{

	public CB_Action_Show_Settings()
	{
		super("settings", MenuID.AID_SHOW_SETTINGS);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(26);
	}

	SettingsActivity settingsDialog;

	@Override
	public void Execute()
	{
		SettingsActivity set = new SettingsActivity();
		set.show();

	}

	WaitDialog wd;

}
