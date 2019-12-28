package de.droidcachebox.menu.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.ViewManager;

public class ShowAbout extends AbstractShowAction {

    private static ShowAbout that;

    private ShowAbout() {
        super("about");
    }

    public static ShowAbout getInstance() {
        if (that == null) that = new ShowAbout();
        return that;
    }


    @Override
    public void execute() {
        ViewManager.leftTab.showView(AboutView.getInstance());
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.cb.name());
    }

    @Override
    public CB_View_Base getView() {
        return AboutView.getInstance();
    }

    @Override
    public Menu getContextMenu() {
        return null;
    }
}
