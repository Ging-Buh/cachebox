package CB_UI.GL_UI.Main.Actions;

import CB_Core.Database;
import CB_Core.Types.CacheWithWP;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.Main.Actions.CB_Action;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class CB_Action_switch_Autoresort extends CB_Action {

    public CB_Action_switch_Autoresort() {
        super("AutoResort", MenuID.AID_AUTO_RESORT);
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.autoSortOffIcon.name());
    }

    @Override
    public void Execute() {
        GlobalCore.setAutoResort(!(GlobalCore.getAutoResort()));
        if (GlobalCore.getAutoResort()) {
            synchronized (Database.Data.Query) {
                if (GlobalCore.isSetSelectedCache()) {
                    CacheWithWP ret = Database.Data.Query.Resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));
                    GlobalCore.setSelectedWaypoint(ret.getCache(), ret.getWaypoint(), false);
                    GlobalCore.setNearestCache(ret.getCache());
                    ret.dispose();
                }
            }
        }
    }
}
