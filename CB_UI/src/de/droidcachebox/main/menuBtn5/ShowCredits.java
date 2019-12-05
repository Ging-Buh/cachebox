package de.droidcachebox.main.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.AbstractShowAction;
import de.droidcachebox.gdx.main.MenuID;
import de.droidcachebox.gdx.views.CreditsView;
import de.droidcachebox.main.ViewManager;

public class ShowCredits extends AbstractShowAction {
    private static ShowCredits that;

    private ShowCredits() {
        super("Credits", MenuID.AID_SHOW_CREDITS);
    }

    public static ShowCredits getInstance() {
        if (that == null) that = new ShowCredits();
        return that;
    }

    @Override
    public void execute() {
        ViewManager.leftTab.ShowView(CreditsView.getInstance());
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
