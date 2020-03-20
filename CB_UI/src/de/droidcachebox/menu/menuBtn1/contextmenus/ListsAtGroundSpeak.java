package de.droidcachebox.menu.menuBtn1.contextmenus;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.translation.Translation;

import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.gdx.controls.messagebox.MessageBox.BTN_LEFT_POSITIVE;

public class ListsAtGroundSpeak extends AbstractAction {
    private static ListsAtGroundSpeak listsAtGroundSpeak;

    private ListsAtGroundSpeak() {
        super("GroundSpeakLists");
    }

    public static ListsAtGroundSpeak getInstance() {
        if (listsAtGroundSpeak == null) listsAtGroundSpeak = new ListsAtGroundSpeak();
        return listsAtGroundSpeak;
    }

    @Override
    public void execute() {
        Menu menu = new Menu("GroundSpeakLists");
        menu.addMenuItem("Watchlist", null, () -> groundspeakList("Watchlist"));
        menu.addMenuItem("Favoriteslist", null, () -> groundspeakList("Favoriteslist"));
        menu.addMenuItem("Ignorelist", null, () -> groundspeakList("Ignorelist"));
        menu.show();
    }

    @Override
    public Sprite getIcon() {
        return null;
    }

    private static void groundspeakList(String title) {
        MessageBox mb = MessageBox.show(Translation.get(title + "Message"), Translation.get(title), MessageBoxButton.AbortRetryIgnore, MessageBoxIcon.Question,
                (btnNumber, data) -> {
                    if (btnNumber == BTN_LEFT_POSITIVE)
                        addToList(title);
                    else if (btnNumber == MessageBox.BTN_MIDDLE_NEUTRAL)
                        removeFromList(title);
                    return true;
                });
        mb.setButtonText("append", "remove", "cancel");
    }

    private static void addToList(String title) {
        if (GlobalCore.isSetSelectedCache()) {
            GL.that.postAsync(() -> {
                String listCode;
                switch (title) {
                    case "Watchlist":
                        listCode = "watch";
                        break;
                    case "Favoriteslist":
                        listCode = "favorites";
                        break;
                    default:
                        listCode = "ignore";
                }
                if (GroundspeakAPI.addToList(listCode, GlobalCore.getSelectedCache().getGeoCacheCode()) == OK) {
                    MessageBox.show(Translation.get("ok"), Translation.get("AddTo" + title), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                } else {
                    MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("AddTo" + title), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                }
            });
        }
    }

    private static void removeFromList(String title) {
        if (GlobalCore.isSetSelectedCache()) {
            GL.that.postAsync(() -> {
                String listCode;
                switch (title) {
                    case "Watchlist":
                        listCode = "watch";
                        break;
                    case "Favoriteslist":
                        listCode = "favorites";
                        break;
                    default:
                        listCode = "ignore";
                }
                if (GroundspeakAPI.removeFromList(listCode, GlobalCore.getSelectedCache().getGeoCacheCode()) == OK) {
                    MessageBox.show(Translation.get("ok"), Translation.get("RemoveFrom" + title), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                } else {
                    MessageBox.show(GroundspeakAPI.LastAPIError, Translation.get("RemoveFrom" + title), MessageBoxButton.OK, MessageBoxIcon.Information, null);
                }
            });
        }
    }

}
