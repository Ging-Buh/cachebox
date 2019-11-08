package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.views.TrackableListView;

public class Abstract_ShowTrackableListAction extends AbstractShowAction {
    private static Abstract_ShowTrackableListAction that;

    private Abstract_ShowTrackableListAction() {
        super("TBList", MenuID.AID_SHOW_TRACKABLELIST);
    }

    public static Abstract_ShowTrackableListAction getInstance() {
        if (that == null) that = new Abstract_ShowTrackableListAction();
        return that;
    }

    @Override
    public void Execute() {
        ViewManager.leftTab.ShowView(TrackableListView.getInstance());
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