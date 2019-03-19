package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.TrackableListView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowTrackableListView extends CB_Action_ShowView {
    private static CB_Action_ShowTrackableListView that;

    private CB_Action_ShowTrackableListView() {
        super("TBList", MenuID.AID_SHOW_TRACKABLELIST);
    }

    public static CB_Action_ShowTrackableListView getInstance() {
        if (that == null) that = new CB_Action_ShowTrackableListView();
        return that;
    }

    @Override
    public void Execute() {
        TabMainView.leftTab.ShowView(TrackableListView.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.tbListIcon.name());
    }

    @Override
    public CB_View_Base getView() {
        return TrackableListView.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return TrackableListView.getInstance().getContextMenu();
    }
}