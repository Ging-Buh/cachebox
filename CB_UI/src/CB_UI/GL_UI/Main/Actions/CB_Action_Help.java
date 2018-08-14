package CB_UI.GL_UI.Main.Actions;

import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_Help extends CB_Action {

    public CB_Action_Help() {
        super("Help Online", MenuID.AID_HELP);
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.helpIcon.name());
    }

    @Override
    public void Execute() {
        PlatformConnector.callUrl("http://www.team-cachebox.de/index.php/de/kurzanleitung");
    }

}
