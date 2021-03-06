package de.droidcachebox.menu.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.views.CreditsView;
import de.droidcachebox.menu.ViewManager;

public class ShowCredits extends AbstractShowAction {
    private static ShowCredits that;

    private ShowCredits() {
        super("Credits");
    }

    public static ShowCredits getInstance() {
        if (that == null) that = new ShowCredits();
        return that;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.showView(CreditsView.getInstance());
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
        return CreditsView.getInstance();
    }

}
