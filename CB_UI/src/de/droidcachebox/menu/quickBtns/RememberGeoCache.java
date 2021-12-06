package de.droidcachebox.menu.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.Cache;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.settings.Settings;

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
        if (Settings.rememberedGeoCache.getValue().length() > 0) {
            Cache rememberedCache = CBDB.getInstance().cacheList.getCacheByGcCodeFromCacheList(Settings.rememberedGeoCache.getValue());
            GlobalCore.setSelectedCache(rememberedCache);
        } else {
            Settings.rememberedGeoCache.setValue(GlobalCore.getSelectedCache().getGeoCacheCode());
            Settings.getInstance().acceptChanges();
        }
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(Sprites.IconName.lockIcon.name());
    }

    public GL_View_Base.OnClickListener getLongClickListener() {
        return (view, x, y, pointer, button) -> {
            // forget remembered
            Settings.rememberedGeoCache.setValue("");
            Settings.getInstance().acceptChanges();
            return true;
        };
    }
}
