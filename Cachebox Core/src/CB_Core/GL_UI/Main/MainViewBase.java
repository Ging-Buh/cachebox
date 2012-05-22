package CB_Core.GL_UI.Main;

import CB_Core.TrackRecorder;
import CB_Core.Events.PositionChangedEvent;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.ViewID;
import CB_Core.Log.Logger;
import CB_Core.Types.Locator;

public class MainViewBase extends CB_View_Base implements PositionChangedEvent
{

	private static boolean TrackRecIsRegisted = false;

	public MainViewBase(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
		if (!TrackRecIsRegisted) PositionChangedEventList.Add(this);
		TrackRecIsRegisted = true;
	}

	public void setGLViewID(ViewID id)
	{
	}

	public static MainViewBase mainView = null;

	public void requestLayout()
	{
	}

	@Override
	public void PositionChanged(Locator locator)
	{
		try
		{
			TrackRecorder.recordPosition();
		}
		catch (Exception e)
		{
			Logger.Error("Core.MainViewBase.PositionChanged()", "TrackRecorder.recordPosition()", e);
			e.printStackTrace();
		}
	}

	@Override
	public void OrientationChanged(float heading)
	{

	}

	@Override
	public String getReceiverName()
	{
		return "Core.MainViewBase";
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
		// TODO Auto-generated method stub

	}

}
