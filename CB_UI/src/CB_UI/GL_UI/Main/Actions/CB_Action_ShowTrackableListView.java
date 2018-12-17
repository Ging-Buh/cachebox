package CB_UI.GL_UI.Main.Actions;

import CB_UI.GL_UI.Views.TrackableListView;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_ShowTrackableListView extends CB_Action {

    public CB_Action_ShowTrackableListView() {
        super("TBList", MenuID.AID_SHOW_TRACKABLELIST);
    }

    @Override
    public void Execute() {
        if ((TrackableListView.that == null))
            new TrackableListView(ActivityBase.ActivityRec(), "TrackableListView");

        if ((TrackableListView.that != null))
            TrackableListView.that.show();
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.tbListIcon.name());
    }

}
