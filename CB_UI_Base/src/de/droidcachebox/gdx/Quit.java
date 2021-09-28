package de.droidcachebox.gdx;

import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.translation.Translation;

public class Quit {
    private static MsgBox msgBox;

    public static void quit() {
        if (msgBox != null && GL.that.getCurrentDialog() == msgBox)
            return;

        try {
            msgBox = MsgBox.show(Translation.get("QuitReally"), Translation.get("Quit?"), MsgBoxButton.OKCancel, MsgBoxIcon.Stop,
                    (which, data) -> {
                        if (which == MsgBox.BTN_LEFT_POSITIVE) {
                            PlatformUIBase.quit();
                        }
                        return true;
                    });
        } catch (Exception e) {
            PlatformUIBase.quit();
        }
    }

}
