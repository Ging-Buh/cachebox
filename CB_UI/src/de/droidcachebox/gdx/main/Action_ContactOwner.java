package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.utils.http.Webb;

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
