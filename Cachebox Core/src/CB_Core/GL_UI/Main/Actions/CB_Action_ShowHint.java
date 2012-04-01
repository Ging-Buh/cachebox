package CB_Core.GL_UI.Main.Actions;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.Controls.Dialogs.HintDialog;

public class CB_Action_ShowHint extends CB_Action
{

	public CB_Action_ShowHint(int id)
	{
		super("Hint", id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void Execute()
	{
		HintDialog.show();
	}

	@Override
	public boolean getEnabled()
	{
		// liefert true zurück wenn ein Cache gewählt ist und dieser einen Hint hat
		if (GlobalCore.SelectedCache() == null) return false;
		String hintText = GlobalCore.SelectedCache().hint;
		if ((hintText == null) || (hintText.length() == 0)) return false;
		return true;
	}
}
