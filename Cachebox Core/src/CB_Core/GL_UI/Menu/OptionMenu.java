package CB_Core.GL_UI.Menu;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_Core.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_Core.GL_UI.GL_Listener.GL;

public class OptionMenu extends Menu
{

	public OptionMenu(String Name)
	{
		super(Name);
		this.setButtonCaptions(MessageBoxButtons.OK);
		this.mMsgBoxClickListner = new GL_MsgBox.OnMsgBoxClickListener()
		{

			@Override
			public boolean onClick(int which, Object data)
			{
				GL.that.closeDialog(that);
				return true;
			}
		};

		MenuItemClickListner = new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{

				if (v instanceof MenuItem)
				{
					MenuItem tmp = (MenuItem) v;
					if (tmp.isCheckable())
					{
						tmp.toggleCheck();
					}
				}

				if (mOnItemClickListner != null)
				{
					for (OnClickListener tmp : mOnItemClickListner)
					{
						if (tmp.onClick(v, x, y, pointer, button)) break;
					}
				}

				return true;
			}
		};
	}

}
