package CB_UI.GL_UI.Main;

import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.TrackRecorder;
import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.ViewID;
import CB_UI.GL_UI.Main.Actions.CB_Action_ShowCompassView;
import CB_Utils.Log.Logger;

public class MainViewBase extends CB_View_Base implements PositionChangedEvent
{

	private static boolean TrackRecIsRegisted = false;
	public static CB_Action_ShowCompassView actionShowCompassView;

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
	public void PositionChanged()
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

		if (GlobalCore.getSelectedCache() != null)
		{
			float distance = GlobalCore.getSelectedCache().Distance(false);
			if (GlobalCore.getSelectedWaypoint() != null)
			{
				distance = GlobalCore.getSelectedWaypoint().Distance();
			}

			if (Config.settings.switchViewApproach.getValue() && !GlobalCore.switchToCompassCompleted
					&& (distance < Config.settings.SoundApproachDistance.getValue()))
			{
				actionShowCompassView.Execute();
				GlobalCore.switchToCompassCompleted = true;
			}
		}
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

	}

	@Override
	public Priority getPriority()
	{
		return Priority.High;
	}

	@Override
	public void OrientationChanged()
	{
	}

	@Override
	public void SpeedChanged()
	{
	}

}
