package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.NotesView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowNotesView extends CB_Action_ShowView {

	public CB_Action_ShowNotesView() {
		super("Notes", MenuID.AID_SHOW_NOTES);
	}

	@Override
	public void Execute() {
		if ((TabMainView.notesView == null) && (tabMainView != null) && (tab != null))
			TabMainView.notesView = new NotesView(tab.getContentRec(), "NotesView");

		if ((TabMainView.notesView != null) && (tab != null))
			tab.ShowView(TabMainView.notesView);
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return SpriteCacheBase.Icons.get(IconName.userdata_50.ordinal());
	}

	@Override
	public CB_View_Base getView() {
		return TabMainView.notesView;
	}
}
