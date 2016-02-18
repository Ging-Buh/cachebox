package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.GL_UI.Controls.PopUps.SearchDialog;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;

public class CB_Action_Show_Search extends CB_Action {

	public CB_Action_Show_Search() {
		super("search", MenuID.AID_SEARCH);
	}

	@Override
	public void Execute() {

		if (TabMainView.cacheListView == null || !TabMainView.cacheListView.isVisible()) {
			TabMainView.actionShowCacheList.Execute();
		}

		if (SearchDialog.that == null) {
			new SearchDialog();
		}

		SearchDialog.that.showNotCloseAutomaticly();

	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return Sprites.getSprite(IconName.lupe.name());
	}
}
