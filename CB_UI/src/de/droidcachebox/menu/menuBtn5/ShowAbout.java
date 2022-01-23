package de.droidcachebox.menu.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn5.executes.AboutView;

public class ShowAbout extends AbstractShowAction {

    private static ShowAbout instance;
    private AboutView aboutView;

    private ShowAbout() {
        super("about");
    }

    public static ShowAbout getInstance() {
        if (instance == null) instance = new ShowAbout();
        return instance;
    }

    @Override
    public void execute() {
        aboutView = new AboutView();
        ViewManager.leftTab.showView(aboutView);
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
        return aboutView;
    }

    @Override
    public Menu getContextMenu() {
        return null;
    }
}
