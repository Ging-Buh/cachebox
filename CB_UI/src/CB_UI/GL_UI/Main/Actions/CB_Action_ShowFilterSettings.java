package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.SpriteCache;
import CB_UI.GL_UI.Activitys.ActivityBase;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Menu.MenuID;
import CB_UI.GL_UI.SpriteCache.IconName;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowFilterSettings extends CB_ActionCommand
{

	public CB_Action_ShowFilterSettings()
	{
		super("filter", MenuID.AID_SHOW_FILTER_DIALOG);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(IconName.filter_13.ordinal());
	}

	EditFilterSettings edFi;

	@Override
	public void Execute()
	{
		if (edFi == null) edFi = new EditFilterSettings(ActivityBase.ActivityRec(), "Filter");
		edFi.show();

	}
}
