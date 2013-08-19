package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI.GL_UI.Controls.MessageBox.MessageBoxIcon;

public class CB_ActionCommand extends CB_Action
{

	public CB_ActionCommand(String name, int id)
	{
		super(name, id);
	}

	@Override
	public void Execute()
	{
		GL_MsgBox.Show(name, "Id=" + id, MessageBoxButtons.OK, MessageBoxIcon.Information, null);
	}

}
