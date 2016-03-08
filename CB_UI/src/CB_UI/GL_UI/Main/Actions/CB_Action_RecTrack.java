package CB_UI.GL_UI.Main.Actions;

import com.badlogic.gdx.graphics.g2d.Sprite;

import CB_UI.TrackRecorder;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;

public class CB_Action_RecTrack extends CB_Action {

	public CB_Action_RecTrack() {
		super("RecTrack", MenuID.AID_SHOW_TRACK_MENU);
	}

	@Override
	public boolean getEnabled() {
		return true;
	}

	@Override
	public Sprite getIcon() {
		return Sprites.getSprite(IconName.trackListIcon.name());
	}

	@Override
	public void Execute() {
		showMenuTrackRecording();
		// TabMainView.that.switchDayNight();
		// new CB_Action_ShowActivity("DayNight", CB_Action.AID_DAY_NIGHT, ViewConst.DAY_NIGHT, SpriteCache.Icons.get(48)).Execute();
	}

	private void showMenuTrackRecording() {
		MenuItem mi;
		Menu cm2 = new Menu("TrackRecordContextMenu");
		cm2.addOnClickListener(new OnClickListener() {

			@Override
			public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
				switch (((MenuItem) v).getMenuItemId()) {
				case MenuID.MI_START:
					TrackRecorder.StartRecording();
					return true;
				case MenuID.MI_PAUSE:
					TrackRecorder.PauseRecording();
					return true;
				case MenuID.MI_STOP:
					TrackRecorder.StopRecording();
					return true;
				}
				return false;
			}
		});
		mi = cm2.addItem(MenuID.MI_START, "start");
		mi.setEnabled(!TrackRecorder.recording);

		if (TrackRecorder.pauseRecording)
			mi = cm2.addItem(MenuID.MI_PAUSE, "continue");
		else
			mi = cm2.addItem(MenuID.MI_PAUSE, "pause");

		mi.setEnabled(TrackRecorder.recording);

		mi = cm2.addItem(MenuID.MI_STOP, "stop");
		mi.setEnabled(TrackRecorder.recording | TrackRecorder.pauseRecording);

		cm2.Show();
	}
}
