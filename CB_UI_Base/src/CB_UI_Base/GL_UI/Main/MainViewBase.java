package CB_UI_Base.GL_UI.Main;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.ViewID;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action_ShowQuit;

public class MainViewBase extends CB_View_Base
{
	public static CB_Action_ShowQuit actionClose = new CB_Action_ShowQuit();

	public MainViewBase(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);

	}

	public void setGLViewID(ViewID id)
	{
	}

	public static MainViewBase mainView = null;

	public void requestLayout()
	{
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		return true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		return true;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		return true;
	}

	@Override
	protected void SkinIsChanged()
	{

	}

}