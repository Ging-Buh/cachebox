package CB_UI.GL_UI.Main.Actions;

import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

import static CB_UI_Base.Events.PlatformUIBase.callUrl;

public class Action_Help extends AbstractAction {

    private static Action_Help that;

    private Action_Help() {
        super("Help Online", MenuID.AID_HELP);
    }

    public static Action_Help getInstance() {
        if (that == null) that = new Action_Help();
        return that;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.helpIcon.name());
    }

    @Override
    public void Execute() {
        callUrl("http://www.team-cachebox.de/index.php/de/kurzanleitung");
    }

}
