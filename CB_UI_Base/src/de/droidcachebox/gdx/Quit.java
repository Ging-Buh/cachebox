package de.droidcachebox.gdx;

import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.translation.Translation;

public class Quit {
    private static MessageBox messageBox;

    public static void quit() {
        if (messageBox != null && GL.that.getCurrentDialog() == messageBox)
            return;

        try {
            messageBox = MessageBox.create(Translation.get("QuitReally"), Translation.get("Quit?"), MessageBoxButtons.OKCancel, MessageBoxIcon.Stop,
                    (which, data) -> {
                        if (which == MessageBox.BUTTON_POSITIVE) {
                            PlatformUIBase.quit();
                        }
                        return true;
                    });
            messageBox.show();
        } catch (Exception e) {
            PlatformUIBase.quit();
        }
    }

}
