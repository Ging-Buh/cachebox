package CB_UI_Base.GL_UI.Menu;

import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.GL_Listener.GL;

public class OptionMenu extends Menu {
    public OptionMenu(String Name) {
        super(Name);
        setButtonCaptions(MessageBoxButtons.OK);
        mMsgBoxClickListener = (which, data) -> {
            GL.that.closeDialog(OptionMenu.this);
            return true;
        };
        menuItemClickListener = (v, x, y, pointer, button) -> {
            if (v instanceof MenuItem) {
                ((MenuItem) v).toggleCheck();
            }
            if (mOnItemClickListeners != null) {
                for (OnClickListener tmp : mOnItemClickListeners) {
                    if (tmp.onClick(v, x, y, pointer, button))
                        break;
                }
            }
            return true;
        };
    }
}
