package CB_UI.GL_UI.Main.Actions;

import CB_UI.GlobalCore;
import CB_UI_Base.Events.PlatformUIBase;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.net.URLEncoder;

public class Action_Mail extends AbstractAction {

    private static Action_Mail that;

    private Action_Mail() {
        super("MailToOwner", MenuID.AID_MAIL);
    }

    public static Action_Mail getInstance() {
        if (that == null) that = new Action_Mail();
        return that;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite("bigLetterbox");
    }

    @Override
    public void Execute() {
        try {
            PlatformUIBase.callUrl("https://www.geocaching.com/email/?u=" + URLEncoder.encode(GlobalCore.getSelectedCache().getPlacedBy(), "UTF-8"));
        }
        catch (Exception ignored) {
        }
    }

}
