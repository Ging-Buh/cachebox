package de.droidcachebox.menu.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.AbstractAction;
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.database.Cache;
import de.droidcachebox.database.Database;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;

public class RememberGeoCache extends AbstractAction {

    private static RememberGeoCache rememberGeoCache;

    RememberGeoCache() {
        super("rememberGeoCacheTitle");
    }

    public static RememberGeoCache getInstance() {
        if (rememberGeoCache == null) rememberGeoCache = new RememberGeoCache();
        return rememberGeoCache;
    }

    @Override
    public void execute() {
        if (CB_Core_Settings.rememberedGeoCache.getValue().length() > 0) {
            Cache rememberedCache = Database.Data.cacheList.getCacheByGcCodeFromCacheList(CB_Core_Settings.rememberedGeoCache.getValue());
            if (rememberedCache != null) GlobalCore.setSelectedCache(rememberedCache);
        }
        else {
            Config.rememberedGeoCache.setValue(GlobalCore.getSelectedCache().getGeoCacheCode());
            Config.AcceptChanges();
        }
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(Sprites.IconName.lockIcon.name());
    }

    public GL_View_Base.OnClickListener getLongClickListener() {
        GL_View_Base.OnClickListener onClickListener = (view, x, y, pointer, button) -> {
            // forget remembered
            Config.rememberedGeoCache.setValue("");
            Config.AcceptChanges();
            return true;
        };
        return onClickListener;
    }
}
