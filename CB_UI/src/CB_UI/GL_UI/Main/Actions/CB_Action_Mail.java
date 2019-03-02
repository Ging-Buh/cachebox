package CB_UI.GL_UI.Main.Actions;

import CB_UI.GlobalCore;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.net.URLEncoder;

public class CB_Action_Mail extends CB_Action {

    private static CB_Action_Mail that;

    private CB_Action_Mail() {
        super("MailToOwner", MenuID.AID_MAIL);
    }

    public static CB_Action_Mail getInstance() {
        if (that == null) that = new CB_Action_Mail();
        return that;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite("bigLetterbox");
    }

    @Override
    public void Execute() {
        try {
            PlatformConnector.callUrl("https://www.geocaching.com/email/?u=" + URLEncoder.encode(GlobalCore.getSelectedCache().getPlacedBy(), "UTF-8"));
        }
        catch (Exception ignored) {
        }
    }

}
