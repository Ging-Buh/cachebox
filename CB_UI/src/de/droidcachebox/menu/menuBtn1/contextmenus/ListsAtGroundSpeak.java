package de.droidcachebox.menu.menuBtn1.contextmenus;

import static de.droidcachebox.core.GroundspeakAPI.OK;

import com.badlogic.gdx.graphics.g2d.Sprite;

import java.util.Map;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.translation.Translation;

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
        menu.addMenuItem("Bookmarklists", null, this::getBookmarkLists);
        menu.show();
    }

    private void getBookmarkLists() {
        Menu menu = new Menu("Bookmarklists");
        GL.that.postAsync(() -> {
            for (Map.Entry<String, String> bookmarkList : GroundspeakAPI.fetchBookmarkLists().entrySet()) {
                menu.addMenuItem("", bookmarkList.getKey(), null, () -> groundspeakList(bookmarkList));
            }
            menu.show();
        });
    }

    @Override
    public Sprite getIcon() {
        return null;
    }

    private void groundspeakList(Map.Entry<String, String> bookmarkList) {
        ButtonDialog mb = new ButtonDialog(Translation.get("BookmarklistMessage", bookmarkList.getKey()), bookmarkList.getKey(), MsgBoxButton.AbortRetryIgnore, MsgBoxIcon.Question);
        mb.setButtonClickHandler((btnNumber, data) -> {
            if (btnNumber == ButtonDialog.BTN_LEFT_POSITIVE)
                addToList(bookmarkList.getValue());
            else if (btnNumber == ButtonDialog.BTN_MIDDLE_NEUTRAL)
                removeFromList(bookmarkList.getValue());
            return true;
        });
        mb.setButtonText("append", "remove", "cancel");
        mb.show();
    }

    private void groundspeakList(String title) {
        ButtonDialog mb = new ButtonDialog(Translation.get(title + "Message"), Translation.get(title), MsgBoxButton.AbortRetryIgnore, MsgBoxIcon.Question);
        mb.setButtonClickHandler((btnNumber, data) -> {
            if (btnNumber == ButtonDialog.BTN_LEFT_POSITIVE)
                addToList(title);
            else if (btnNumber == ButtonDialog.BTN_MIDDLE_NEUTRAL)
                removeFromList(title);
            return true;
        });
        mb.setButtonText("append", "remove", "cancel");
        mb.show();
    }

    private void addToList(String title) {
        if (GlobalCore.isSetSelectedCache()) {
            GL.that.postAsync(() -> {
                String listCode, AddToTitle;
                AddToTitle = "AddTo" + title;
                switch (title) {
                    case "Watchlist":
                        listCode = "watch";
                        break;
                    case "Favoriteslist":
                        listCode = "favorites";
                        break;
                    case "Ignorelist":
                        listCode = "ignore";
                        break;
                    default:
                        listCode = title;
                        AddToTitle = "AddToBookmarklist";
                }
                if (GroundspeakAPI.addToList(listCode, GlobalCore.getSelectedCache().getGeoCacheCode()) == OK) {
                    new ButtonDialog(Translation.get("ok"), Translation.get(AddToTitle), MsgBoxButton.OK, MsgBoxIcon.Information).show();
                } else {
                    new ButtonDialog(GroundspeakAPI.LastAPIError, Translation.get(AddToTitle), MsgBoxButton.OK, MsgBoxIcon.Information).show();
                }
            });
        }
    }

    private void removeFromList(String title) {
        if (GlobalCore.isSetSelectedCache()) {
            GL.that.postAsync(() -> {
                String listCode, RemoveFromTitle;
                RemoveFromTitle = "RemoveFrom" + title;
                switch (title) {
                    case "Watchlist":
                        listCode = "watch";
                        break;
                    case "Favoriteslist":
                        listCode = "favorites";
                        break;
                    case "Ignorelist":
                        listCode = "ignore";
                        break;
                    default:
                        listCode = title;
                        RemoveFromTitle = "RemoveFromBookmarklist";
                }
                if (GroundspeakAPI.removeFromList(listCode, GlobalCore.getSelectedCache().getGeoCacheCode()) == OK) {
                    new ButtonDialog(Translation.get("ok"), Translation.get(RemoveFromTitle), MsgBoxButton.OK, MsgBoxIcon.Information).show();
                } else {
                    new ButtonDialog(GroundspeakAPI.LastAPIError, Translation.get(RemoveFromTitle), MsgBoxButton.OK, MsgBoxIcon.Information).show();
                }
            });
        }
    }

}
