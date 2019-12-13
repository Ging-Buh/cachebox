package de.droidcachebox.gdx;

import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.translation.Translation;

public class Quit {
    private static MessageBox messageBox;

    public static void quit() {
        if (messageBox != null && GL.that.getCurrentDialog() == messageBox)
            return;

        try {
            messageBox = MessageBox.show(Translation.get("QuitReally"), Translation.get("Quit?"), MessageBoxButton.OKCancel, MessageBoxIcon.Stop,
                    (which, data) -> {
                        if (which == MessageBox.BTN_LEFT_POSITIVE) {
                            PlatformUIBase.quit();
                        }
                        return true;
                    });
        } catch (Exception e) {
            PlatformUIBase.quit();
        }
    }

}
