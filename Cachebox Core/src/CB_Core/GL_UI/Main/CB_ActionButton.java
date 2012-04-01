package CB_Core.GL_UI.Main;

import CB_Core.GL_UI.Main.Actions.CB_Action;

public class CB_ActionButton
{
	private CB_Action action;
	private boolean defaultAction;

	public CB_ActionButton(CB_Action action, boolean defaultAction)
	{
		this.action = action;
		this.defaultAction = defaultAction;
	}

	public CB_Action getAction()
	{
		return action;
	}

	public boolean isDefaultAction()
	{
		return defaultAction;
	}

	public boolean getEnabled()
	{
		if (action == null) return false;
		return action.getEnabled();
	}

}
