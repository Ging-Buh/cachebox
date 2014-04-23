package CB_UI.GL_UI.Main.Actions;

import CB_UI.GlobalCore;
import CB_UI.GL_UI.Controls.Dialogs.HintDialog;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowHint extends CB_Action
{

	public CB_Action_ShowHint()
	{
		super("hint", MenuID.AID_SHOW_HINT);
	}

	@Override
	public void Execute()
	{
		if (getEnabled()) HintDialog.show();
	}

	@Override
	public boolean getEnabled()
	{
		// liefert true zurück wenn ein Cache gewählt ist und dieser einen Hint hat
		if (GlobalCore.getSelectedCache() == null) return false;
		return GlobalCore.getSelectedCache().hasHint();
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCacheBase.Icons.get(IconName.hint_19.ordinal());
	}
}
