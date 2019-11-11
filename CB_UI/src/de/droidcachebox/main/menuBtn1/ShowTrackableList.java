package de.droidcachebox.main.menuBtn1;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.AbstractShowAction;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.MenuID;
import de.droidcachebox.gdx.views.TrackableListView;
import de.droidcachebox.main.ViewManager;

public class ShowTrackableList extends AbstractShowAction {
    private static ShowTrackableList that;

    private ShowTrackableList() {
        super("TBList", MenuID.AID_SHOW_TRACKABLELIST);
    }

    public static ShowTrackableList getInstance() {
        if (that == null) that = new ShowTrackableList();
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