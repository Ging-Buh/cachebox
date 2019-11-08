package de.droidcachebox.gdx.main;

import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;

public class OptionMenu extends Menu {
    public OptionMenu(String Name) {
        super(Name);
        autoClose = false;

        setButtonCaptions(MessageBoxButtons.OK);

        mMsgBoxClickListener = new MessageBox.OnMsgBoxClickListener() {
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
