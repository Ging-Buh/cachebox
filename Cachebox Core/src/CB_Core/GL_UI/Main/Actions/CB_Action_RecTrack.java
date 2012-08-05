package CB_Core.GL_UI.Main.Actions;

import CB_Core.TrackRecorder;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.GL_View_Base.OnClickListener;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Menu.Menu;
import CB_Core.GL_UI.Menu.MenuItem;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_RecTrack extends CB_ActionCommand
{

	public CB_Action_RecTrack()
	{
		super("DayNight", AID_DAY_NIGHT);
	}

	@Override
	public boolean getEnabled()
	{
		return true;
	}

	@Override
	public Sprite getIcon()
	{
		return SpriteCache.Icons.get(48);
	}

	@Override
	public void Execute()
	{
		showMenuTrackRecording();
		// TabMainView.that.switchDayNight();
		// new CB_Action_ShowActivity("DayNight", CB_Action.AID_DAY_NIGHT, ViewConst.DAY_NIGHT, SpriteCache.Icons.get(48)).Execute();
	}

	private static final int START = 1;
	private static final int PAUSE = 2;
	private static final int STOP = 3;

	private void showMenuTrackRecording()
	{
		MenuItem mi;
		Menu cm2 = new Menu("TrackRecordContextMenu");
		cm2.setItemClickListner(new OnClickListener()
		{

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
			{
				switch (((MenuItem) v).getMenuItemId())
				{
				case START:
					TrackRecorder.StartRecording();
					return true;
				case PAUSE:
					TrackRecorder.PauseRecording();
					return true;
				case STOP:
					TrackRecorder.StopRecording();
					return true;
				}
				return false;
			}
		});
		mi = cm2.addItem(START, "start");
		mi.setEnabled(!TrackRecorder.recording);

		if (TrackRecorder.pauseRecording) mi = cm2.addItem(PAUSE, "continue");
		else
			mi = cm2.addItem(PAUSE, "pause");

		mi.setEnabled(TrackRecorder.recording);

		mi = cm2.addItem(STOP, "stop");
		mi.setEnabled(TrackRecorder.recording | TrackRecorder.pauseRecording);

		cm2.show();
	}
}
