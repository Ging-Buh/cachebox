package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.GL_UI.Controls.Dialogs.DeleteDialog;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;

public class CB_Action_Show_Delete_Dialog extends CB_Action {

	Color TrackColor;

	public CB_Action_Show_Delete_Dialog() {
		super("DeleteCaches", MenuID.AID_SHOW_DELETE_DIALOG);
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return SpriteCacheBase.Icons.get(IconName.delete_28.ordinal());
	}

	@Override
	public void Execute() {
		DeleteDialog d = new DeleteDialog();
		d.Show();
	}
}
