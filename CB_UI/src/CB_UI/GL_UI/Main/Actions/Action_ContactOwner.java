package CB_UI.GL_UI.Main.Actions;

import CB_UI.GlobalCore;
import CB_UI_Base.Events.PlatformUIBase;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_Utils.http.Webb;
import com.badlogic.gdx.graphics.g2d.Sprite;

import java.net.URLEncoder;

public class Action_ContactOwner extends AbstractAction {

    private static Action_ContactOwner that;

    private Action_ContactOwner() {
        super("contactOwner", MenuID.AID_ContactOwner);
    }

    public static Action_ContactOwner getInstance() {
        if (that == null) that = new Action_ContactOwner();
        return that;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite("bigLetterbox");
    }

    @Override
    public void Execute() {
        Menu menu = new Menu("contactOwner");
        menu.addMenuItem("MailToOwner", Sprites.getSprite("bigLetterbox"), () -> {
            try {
                String mOwner = URLEncoder.encode(GlobalCore.getSelectedCache().getOwner(), "UTF-8");
                PlatformUIBase.callUrl("https://www.geocaching.com/email/?u=" + mOwner);
            } catch (Exception ignored) {
            }
        });
        menu.addMenuItem("MessageToOwner", Sprites.getSprite("bigLetterbox"), () -> {
            GL.that.postAsync(() -> {
                try {
                    String mGCCode = GlobalCore.getSelectedCache().getGcCode();
                    try {
                        String page = Webb.create()
                                .get("https://coord.info/" + mGCCode)
                                .ensureSuccess()
                                .asString()
                                .getBody();
                        String toSearch = "recipientId=";
                        int pos = page.indexOf(toSearch);
                        if (pos > -1) {
                            int start = pos + toSearch.length();
                            int stop = page.indexOf("&amp;", start);
                            String guid = page.substring(start, stop);
                            PlatformUIBase.callUrl("https://www.geocaching.com/account/messagecenter?recipientId=" + guid + "&gcCode=" + mGCCode);
                        }
                    } catch (Exception ignored) {
                    }
                } catch (Exception ignored) {
                }
            });
        });
        menu.show();
    }

}
