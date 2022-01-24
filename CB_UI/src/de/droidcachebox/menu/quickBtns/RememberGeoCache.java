package de.droidcachebox.menu.quickBtns;

import com.badlogic.gdx.graphics.g2d.Sprite;

import de.droidcachebox.AbstractAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.settings.Settings;

public class RememberGeoCache extends AbstractAction {

    public RememberGeoCache() {
        super("rememberGeoCacheTitle");
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

}
