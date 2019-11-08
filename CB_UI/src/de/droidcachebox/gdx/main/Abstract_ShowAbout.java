package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.views.AboutView;

public class Abstract_ShowAbout extends AbstractShowAction {

    private static Abstract_ShowAbout that;

    private Abstract_ShowAbout() {
        super("about", MenuID.AID_SHOW_ABOUT);
    }

    public static Abstract_ShowAbout getInstance() {
        if (that == null) that = new Abstract_ShowAbout();
        return that;
    }


    @Override
    public void Execute() {
        ViewManager.leftTab.ShowView(AboutView.getInstance());
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
