package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.WaypointView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;

public class CB_Action_ShowWaypointView extends CB_Action_ShowView {

	public CB_Action_ShowWaypointView() {
		super("Waypoints", MenuID.AID_SHOW_WAYPOINTS);
	}

	@Override
	public void Execute() {
		if ((TabMainView.waypointView == null) && (tabMainView != null) && (tab != null))
			TabMainView.waypointView = new WaypointView(tab.getContentRec(), "WaypointView");

		if ((TabMainView.waypointView != null) && (tab != null))
			tab.ShowView(TabMainView.waypointView);
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return Sprites.getSprite(IconName.waypointListIcon.name());
	}

	@Override
	public CB_View_Base getView() {
		return TabMainView.waypointView;
	}

	@Override
	public boolean hasContextMenu() {
		return true;
	}

	@Override
	public Menu getContextMenu() {

		if (WaypointView.that != null)
			return WaypointView.that.getContextMenu();
		return null;
	}
}
