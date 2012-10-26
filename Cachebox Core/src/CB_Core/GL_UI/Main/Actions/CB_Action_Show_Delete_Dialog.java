package CB_Core.GL_UI.Main.Actions;

import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Dialogs.DeleteDialog;
import CB_Core.GL_UI.Menu.MenuID;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_Show_Delete_Dialog extends CB_ActionCommand
{

	Color TrackColor;

	public CB_Action_Show_Delete_Dialog()
	{
		super("DeleteCaches", MenuID.AID_SHOW_DELETE_DIALOG);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(28);
	}

	@Override
	public void Execute()
	{
		DeleteDialog d = new DeleteDialog();
		d.Show();
	}
}
