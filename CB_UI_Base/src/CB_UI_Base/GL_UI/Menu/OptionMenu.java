package CB_UI_Base.GL_UI.Menu;

import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.GL_Listener.GL;

public class OptionMenu extends Menu {

	public OptionMenu(String Name) {
		super(Name);
		this.setButtonCaptions(MessageBoxButtons.OK);
		this.mMsgBoxClickListener = new GL_MsgBox.OnMsgBoxClickListener() {

			@Override
			public boolean onClick(int which, Object data) {
				GL.that.closeDialog(OptionMenu.this);
				return true;
			}
		};

		menuItemClickListener = new OnClickListener() {

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

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
			}
		};
	}

}
