package CB_Core.GL_UI.Main;

import CB_Core.TrackRecorder;
import CB_Core.Events.PositionChangedEvent;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.ViewID;
import CB_Core.Log.Logger;
import CB_Core.Types.Locator;

public abstract class MainViewBase extends CB_View_Base implements PositionChangedEvent
{

	public MainViewBase(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
		PositionChangedEventList.Add(this);
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
		// TODO Auto-generated method stub

	}

	@Override
	public String getReceiverName()
	{
		return "Core.MainViewBase";
	}

}
