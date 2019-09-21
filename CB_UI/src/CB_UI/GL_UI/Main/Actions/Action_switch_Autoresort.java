package CB_UI.GL_UI.Main.Actions;

import CB_Core.Database;
import CB_Core.Types.CacheWithWP;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.Main.Actions.AbstractAction;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Action_switch_Autoresort extends AbstractAction {

    private static Action_switch_Autoresort that;

    private Action_switch_Autoresort() {
        super("AutoResort", MenuID.AID_AUTO_RESORT);
    }

    public static Action_switch_Autoresort getInstance() {
        if (that == null) that = new Action_switch_Autoresort();
        return that;
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
            synchronized (Database.Data.cacheList) {
                if (GlobalCore.isSetSelectedCache()) {
                    CacheWithWP ret = Database.Data.cacheList.resort(GlobalCore.getSelectedCoord(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));
                    GlobalCore.setSelectedWaypoint(ret.getCache(), ret.getWaypoint(), false);
                    GlobalCore.setNearestCache(ret.getCache());
                    ret.dispose();
                }
            }
        }
    }
}
