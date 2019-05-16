package CB_UI_Base.GL_UI.Menu;

import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.GL_Listener.GL;

public class OptionMenu extends Menu {
    public OptionMenu(String Name) {
        super(Name);
        autoClose = false;

        setButtonCaptions(MessageBoxButtons.OK);
        mMsgBoxClickListener = (which, data) -> {
            GL.that.closeDialog(OptionMenu.this);
            return true;
        };

        // a default handler for the items. will not be neccessary with the new addMenuItem(...runnable)
        menuItemClickListener = (v, x, y, pointer, button) -> {
            if (v instanceof MenuItem) {
                // update the checkbox
                ((MenuItem) v).toggleCheck();
            }
            // execute all registered clickhandlers
            if (mOnItemClickListeners != null) {
                for (OnClickListener tmp : mOnItemClickListeners) {
                    if (tmp.onClick(v, x, y, pointer, button))
                        break;
                }
            }
            return true;
        };

    }

    public void setSingleSelection(){
        singleSelection=true;
    }

}
