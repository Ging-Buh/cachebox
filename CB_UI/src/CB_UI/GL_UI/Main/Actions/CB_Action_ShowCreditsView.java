package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GL_UI.Views.CreditsView;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowView;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowCreditsView extends CB_Action_ShowView {
    public CB_Action_ShowCreditsView() {
        super("Credits", MenuID.AID_SHOW_CREDITS);
    }

    @Override
    public void Execute() {
        if ((TabMainView.creditsView == null) && (tabMainView != null) && (tab != null))
            TabMainView.creditsView = new CreditsView(tab.getContentRec(), "CreditsView");

        if ((TabMainView.creditsView != null) && (tab != null))
            tab.ShowView(TabMainView.creditsView);
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
        return TabMainView.creditsView;
    }

}
