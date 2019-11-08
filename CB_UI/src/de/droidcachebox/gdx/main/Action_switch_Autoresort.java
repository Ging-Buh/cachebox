package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.database.CacheWithWP;
import de.droidcachebox.database.Database;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;

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
