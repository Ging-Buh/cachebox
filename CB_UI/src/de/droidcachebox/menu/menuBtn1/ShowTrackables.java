package de.droidcachebox.menu.menuBtn1;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn1.executes.Trackables;

public class ShowTrackables extends AbstractShowAction {
    private static ShowTrackables showTrackables;

    private ShowTrackables() {
        super("TBList");
    }

    public static ShowTrackables getInstance() {
        if (showTrackables == null) showTrackables = new ShowTrackables();
        return showTrackables;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(Trackables.getInstance());
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
        return Trackables.getInstance();
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        return Trackables.getInstance().getContextMenu();
    }
}