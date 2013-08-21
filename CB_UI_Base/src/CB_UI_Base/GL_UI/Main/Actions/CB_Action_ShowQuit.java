package CB_UI_Base.GL_UI.Main.Actions;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_Utils.Log.Logger;

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
		return SpriteCacheBase.Icons.get(IconName.close_31.ordinal());
	}
}
