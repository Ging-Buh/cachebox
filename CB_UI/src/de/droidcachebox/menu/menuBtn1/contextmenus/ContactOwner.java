package de.droidcachebox.menu.menuBtn1.contextmenus;

import com.badlogic.gdx.graphics.g2d.Sprite;

import java.net.URLEncoder;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.locator.Formatter;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.http.Webb;

public class ContactOwner extends AbstractAction {

    private static ContactOwner contactOwner;

    private ContactOwner() {
        super("contactOwner");
    }

    public static ContactOwner getInstance() {
        if (contactOwner == null) contactOwner = new ContactOwner();
        return contactOwner;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite("bigLetterbox");
    }

    @Override
    public void execute() {
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
                    Cache geoCache = GlobalCore.getSelectedCache();
                    String mGCCode = geoCache.getGeoCacheCode();
                    // fill clipboard
                    if (PlatformUIBase.getClipboard() != null) {
                        String text = mGCCode + " - " + geoCache.getGeoCacheName() + ("\n" + "https://coord.info/" + mGCCode);
                        if (geoCache.hasCorrectedCoordinatesOrHasCorrectedFinal()) {
                            text = text + ("\n\n" + "Location (corrected)");
                            if (geoCache.hasCorrectedCoordinates()) {
                                text = text + ("\n" + Formatter.FormatCoordinate(geoCache.getCoordinate(), ""));
                            } else {
                                text = text + ("\n" + Formatter.FormatCoordinate(geoCache.getCorrectedFinal().getCoordinate(), ""));
                            }
                        } else {
                            text = text + ("\n\n" + "Location");
                            text = text + ("\n" + Formatter.FormatCoordinate(geoCache.getCoordinate(), ""));
                        }
                        PlatformUIBase.getClipboard().setContents(text);
                    }
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
                        } else {
                            new ButtonDialog(Translation.get("noRecipient"), Translation.get("Error"), MsgBoxButton.OK, MsgBoxIcon.Error).show();
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
