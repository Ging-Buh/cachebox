package de.droidcachebox.menu.menuBtn5;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn5.executes.CreditsView;

public class ShowCredits extends AbstractShowAction {
    private static ShowCredits instance;
    private CreditsView creditsView;

    private ShowCredits() {
        super("Credits");
    }

    public static ShowCredits getInstance() {
        if (instance == null) instance = new ShowCredits();
        return instance;
    }

    @Override
    public void execute() {
        creditsView = new CreditsView(); // is always null or disposed at this point
        ViewManager.leftTab.showView(creditsView);
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
        return creditsView;
    }

}
