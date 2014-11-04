package CB_UI_Base.GL_UI.Main.Actions;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI_Base.Tag;
import CB_UI_Base.Events.platformConector;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.MenuID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowQuit extends CB_Action
{

	String OverrideAppName = null;

	public CB_Action_ShowQuit()
	{
		super("quit", MenuID.AID_SHOW_QUIT);
	}

	public void OverrideAppName(String name)
	{
		OverrideAppName = name;
	}

	static GL_MsgBox msg;

	@Override
	public void Execute()
	{
		// if (askIsShown) return;

		if (msg != null && GL.that.actDialog == msg) return;

		String Msg = Translation.Get("QuitReally");
		String Title = Translation.Get("Quit?");

		if (OverrideAppName != null)
		{
			Msg = Msg.replace("Cachebox", OverrideAppName);
			Title = Title.replace("Cachebox", OverrideAppName);
		}

		try
		{
			msg = GL_MsgBox.Show(Msg, Title, MessageBoxButtons.OKCancel, MessageBoxIcon.Stop, new OnMsgBoxClickListener()
			{

				@Override
				public boolean onClick(int which, Object data)
				{
					if (which == GL_MsgBox.BUTTON_POSITIVE)
					{

						Gdx.app.log(Tag.TAG, "\r\n Quit");
						platformConector.callQuitt();
					}
					return true;
				}
			});
		}
		catch (Exception e)
		{
			platformConector.callQuitt();
		}
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
