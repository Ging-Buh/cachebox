package CB_Core.GL_UI.Main;

import CB_Core.GL_UI.Main.Actions.CB_Action;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_ActionButton
{
	public enum GestureDirection
	{
		None, Right, Up, Left, Down
	};

	private CB_Action action;
	private boolean defaultAction;
	private GestureDirection gestureDirection = GestureDirection.None;

	public CB_ActionButton(CB_Action action, boolean defaultAction, GestureDirection gestureDirection)
	{
		this.action = action;
		this.defaultAction = defaultAction;
		this.gestureDirection = gestureDirection;
	}

	public CB_ActionButton(CB_Action action, boolean defaultAction)
	{
		this(action, defaultAction, GestureDirection.None);
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

	public GestureDirection getGestureDirection()
	{
		return gestureDirection;
	}

	public Sprite getIcon()
	{
		return action.getIcon();
	}
}
