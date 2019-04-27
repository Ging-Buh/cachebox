package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Action_EditFilterSettings extends AbstractAction {

    private static Action_EditFilterSettings that;

    private Action_EditFilterSettings() {
        super("Filter", MenuID.AID_SHOW_FILTER_DIALOG);
    }

    public static Action_EditFilterSettings getInstance() {
        if (that == null) that = new Action_EditFilterSettings();
        return that;
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
