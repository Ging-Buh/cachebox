package CB_UI.GL_UI.Main.Actions;

import CB_UI.TrackRecorder;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.GL_View_Base.OnClickListener;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_RecTrack extends CB_Action {

    private static CB_Action_RecTrack that;

    private CB_Action_RecTrack() {
        super("RecTrack", MenuID.AID_SHOW_TRACK_MENU);
    }

    public static CB_Action_RecTrack getInstance() {
        if (that == null) that = new CB_Action_RecTrack();
        return that;
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
    }

    private void showMenuTrackRecording() {
        MenuItem mi;
        Menu cm2 = new Menu("TrackRecordContextMenu");
        cm2.addOnClickListener((v, x, y, pointer, button) -> {
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
