package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowFilterSettings extends CB_Action {

    public CB_Action_ShowFilterSettings() {
        super("filter", MenuID.AID_SHOW_FILTER_DIALOG);
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.filter.name());
    }

    @Override
    public void Execute() {
        EditFilterSettings edFi = new EditFilterSettings(ActivityBase.ActivityRec(), "Filter");
        edFi.show();

    }
}
