package de.droidcachebox.main.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.database.CacheWithWP;
import de.droidcachebox.database.Database;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.main.AbstractAction;
import de.droidcachebox.gdx.main.MenuID;

public class SwitchAutoresort extends AbstractAction {

    private static SwitchAutoresort that;

    private SwitchAutoresort() {
        super("AutoResort", MenuID.AID_AUTO_RESORT);
    }

    public static SwitchAutoresort getInstance() {
        if (that == null) that = new SwitchAutoresort();
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
    public void execute() {
        GlobalCore.setAutoResort(!(GlobalCore.getAutoResort()));
        if (GlobalCore.getAutoResort()) {
            synchronized (Database.Data.cacheList) {
                if (GlobalCore.isSetSelectedCache()) {
                    CacheWithWP ret = Database.Data.cacheList.resort(GlobalCore.getSelectedCoordinate(), new CacheWithWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint()));
                    GlobalCore.setSelectedWaypoint(ret.getCache(), ret.getWaypoint(), false);
                    GlobalCore.setNearestCache(ret.getCache());
                    ret.dispose();
                }
            }
        }
    }
}
