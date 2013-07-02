package CB_Core.GL_UI.Main.Actions;

import CB_Core.Config;
import CB_Core.GlobalCore;
import CB_Core.Events.platformConector;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.SpriteCache.IconName;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.Menu.MenuID;
import CB_Core.Log.Logger;
import CB_Core.TranslationEngine.Translation;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowQuit extends CB_Action
{

	public CB_Action_ShowQuit()
	{
		super("quit", MenuID.AID_SHOW_QUIT);
	}

	static GL_MsgBox msg;

	@Override
	public void Execute()
	{
		// if (askIsShown) return;

		if (msg != null && GL.that.actDialog == msg) return;

		msg = GL_MsgBox.Show(Translation.Get("QuitReally"), Translation.Get("Quit?"), MessageBoxButtons.OKCancel, MessageBoxIcon.Stop,
				new OnMsgBoxClickListener()
				{

					@Override
					public boolean onClick(int which, Object data)
					{
						if (which == GL_MsgBox.BUTTON_POSITIVE)
						{
							if (GlobalCore.getSelectedCache() != null)
							{
								// speichere selektierten Cache, da nicht alles über die SelectedCacheEventList läuft
								Config.settings.LastSelectedCache.setValue(GlobalCore.getSelectedCache().GcCode);
								Config.AcceptChanges();
								Logger.DEBUG("LastSelectedCache = " + GlobalCore.getSelectedCache().GcCode);
							}
							Logger.DEBUG("\r\n Quit");
							platformConector.callQuitt();
						}
						return true;
					}
				});
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(IconName.close_31.ordinal());
	}
}
