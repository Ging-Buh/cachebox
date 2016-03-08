package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.NotesView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.MenuID;

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
		return Sprites.getSprite(IconName.userdata.name());
	}

	@Override
	public CB_View_Base getView() {
		return TabMainView.notesView;
	}
}
