package de.droidcachebox.menu.menuBtn5.executes;

import static de.droidcachebox.gdx.controls.dialogs.ButtonDialog.BTN_LEFT_POSITIVE;

import de.droidcachebox.Platform;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.translation.Translation;

public class Quit {
    public void quit() {
        ButtonDialog bd = new ButtonDialog(Translation.get("QuitReally"), Translation.get("Quit?"), MsgBoxButton.OKCancel, MsgBoxIcon.Stop);
        bd.setButtonClickHandler((which, data) -> {
            if (which == BTN_LEFT_POSITIVE) {
                Platform.quit(); // do all closes there
            }
            return true;
        });
        bd.show();
    }
}
