package CB_UI_Base.GL_UI.Menu;

import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.GL_Listener.GL;

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
