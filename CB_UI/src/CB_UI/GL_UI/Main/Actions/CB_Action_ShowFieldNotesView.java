package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.FieldNotesView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;

public class CB_Action_ShowFieldNotesView extends CB_Action_ShowView {

	public CB_Action_ShowFieldNotesView() {
		super("Fieldnotes", MenuID.AID_SHOW_FIELDNOTES);
	}

	@Override
	public void Execute() {
		if ((TabMainView.fieldNotesView == null) && (tabMainView != null) && (tab != null))
			TabMainView.fieldNotesView = new FieldNotesView(tab.getContentRec(), "FieldNotesView");

		if ((TabMainView.fieldNotesView != null) && (tab != null))
			tab.ShowView(TabMainView.fieldNotesView);
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return Sprites.getSprite(IconName.fieldnoteListIcon.name());
	}

	@Override
	public CB_View_Base getView() {
		return TabMainView.fieldNotesView;
	}

	@Override
	public boolean hasContextMenu() {
		return true;
	}

	@Override
	public Menu getContextMenu() {
		return FieldNotesView.that.getContextMenu();
	}
}
