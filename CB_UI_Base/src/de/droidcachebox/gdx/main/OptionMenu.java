package de.droidcachebox.gdx.main;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;

public class OptionMenu extends Menu {
    public OptionMenu(String Name) {
        super(Name);
        autoClose = false;

        setButtonCaptions(MsgBoxButton.OK);

        buttonClickHandler = new ButtonClickHandler() {
            @Override
            public boolean onClick(int which, Object data) {
                GL.that.closeDialog(OptionMenu.this);
                return true;
            }
        };
    }

    public void setSingleSelection() {
        singleSelection = true;
    }

}
