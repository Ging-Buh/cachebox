package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;

public class CB_Action_ShowFilterSettings extends CB_Action {

	public CB_Action_ShowFilterSettings() {
		super("filter", MenuID.AID_SHOW_FILTER_DIALOG);
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return SpriteCacheBase.Icons.get(IconName.filter_13.ordinal());
	}

	@Override
	public void Execute() {
		EditFilterSettings edFi = new EditFilterSettings(ActivityBase.ActivityRec(), "Filter");
		edFi.show();

	}
}
