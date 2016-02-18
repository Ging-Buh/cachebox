package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.TestViews.TestView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.MenuID;

public class CB_Action_ShowTestView extends CB_Action_ShowView {

	public CB_Action_ShowTestView() {
		super("TestView", MenuID.AID_TEST_VIEW);
	}

	@Override
	public void Execute() {
		if ((TabMainView.testView == null) && (tabMainView != null) && (tab != null))
			TabMainView.testView = new TestView(tab.getContentRec(), "TestView");

		if ((TabMainView.testView != null) && (tab != null))
			tab.ShowView(TabMainView.testView);
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return Sprites.getSprite(IconName.jokerPhone.name());
	}

	@Override
	public CB_View_Base getView() {
		return TabMainView.testView;
	}
}
