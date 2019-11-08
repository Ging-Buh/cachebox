package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.views.CreditsView;

public class Abstract_ShowCreditsAction extends AbstractShowAction {
    private static Abstract_ShowCreditsAction that;

    private Abstract_ShowCreditsAction() {
        super("Credits", MenuID.AID_SHOW_CREDITS);
    }

    public static Abstract_ShowCreditsAction getInstance() {
        if (that == null) that = new Abstract_ShowCreditsAction();
        return that;
    }

    @Override
    public void Execute() {
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
