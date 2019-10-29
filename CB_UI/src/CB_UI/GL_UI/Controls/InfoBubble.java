package CB_UI.GL_UI.Controls;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.SizeF;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class InfoBubble extends CB_View_Base {
    private static CB_RectF saveButtonRec;
    private final Drawable saveIcon;
    /**
     * CacheID of the Cache showing Bubble
     */
    private long mCacheId = -1;
    /**
     * Cache showing Bubble
     */
    private Cache mCache = null;
    private Waypoint mWaypoint = null;
    private CacheInfo cacheInfo;

    public InfoBubble(SizeF Size, String Name) {
        super(Size, Name);
        saveIcon = new SpriteDrawable(Sprites.getSprite(IconName.save.name()));
        registerSkinChangedEvent();
    }

    public long getCacheId() {
        return mCacheId;
    }

    public Waypoint getWaypoint() {
        return mWaypoint;
    }

    public void setCache(Cache cache, Waypoint waypoint) {
        setCache(cache, waypoint, false);
    }

    public void setCache(Cache cache, Waypoint waypoint, boolean force) {

        if (cache == null) {
            mCache = null;
            mCacheId = -1;
            this.removeChilds();
            cacheInfo = null;
            return;
        }

        if (!force) {
            if ((mCache != null) && (mCache.Id == cache.Id) && (mWaypoint == waypoint))
                return;
        }

        // Log.debug(log, "New Cache @InfoBubble");
        mCache = cache;
        mCacheId = cache.Id;
        mWaypoint = waypoint;
        // SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));
        SizeF size = new SizeF(0.96f * getWidth(), 0.72f * getHeight());

        // if Cache is an event we must load details for DateHidden
        if (mCache.isEvent() && !mCache.isDetailLoaded())
            mCache.loadDetail();

        cacheInfo = new CacheInfo(size, "CacheInfo", cache);
        cacheInfo.setViewMode(mCache.isEvent() ? CacheInfo.VIEW_MODE_BUBBLE_EVENT : CacheInfo.VIEW_MODE_BUBBLE);
        cacheInfo.setY(getHeight() - size.height);
        cacheInfo.setFont(Fonts.getBubbleNormal());
        cacheInfo.setSmallFont(Fonts.getBubbleSmall());
        this.removeChilds();
        this.addChild(cacheInfo);
        requestLayout();
    }

    public void showBubbleSelected() {
        // Log.debug(log, "Show BubbleSelected");
        mCacheId = GlobalCore.getSelectedCache().Id;
        mCache = GlobalCore.getSelectedCache();
        setVisible();
    }

    @Override
    protected void render(Batch batch) {
        boolean selectedCache = false;
        if (GlobalCore.isSetSelectedCache()) {
            selectedCache = mCache.equals(GlobalCore.getSelectedCache());
        }

        Sprite sprite = selectedCache ? Sprites.Bubble.get(1) : Sprites.Bubble.get(0);
        sprite.setPosition(0, 0);
        sprite.setSize(getWidth(), getHeight());
        sprite.draw(batch);

        if (mCache != null && mCache.isLive()) {
            if (saveButtonRec == null) {
                float s = GL_UISizes.halfBubble / 5;
                saveButtonRec = new CB_RectF(GL_UISizes.margin, this.getHalfHeight() - (s / 3), s, s);
            }
            saveIcon.draw(batch, saveButtonRec.getX(), saveButtonRec.getY(), saveButtonRec.getWidth(), saveButtonRec.getHeight());
        }
    }

    @Override
    public void onResized(CB_RectF rec) {
        requestLayout();
    }

    private void requestLayout() {
        // Log.debug(log, "InfoBubble RequestLayout");
        // SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));
        SizeF size = new SizeF(0.96f * getWidth(), 0.72f * getHeight());
        cacheInfo.setSize(size);
        cacheInfo.setY(getHeight() - size.height);
    }

    public Cache getCache() {
        return mCache;
    }

    @Override
    protected void skinIsChanged() {
        if (cacheInfo != null) {
            cacheInfo.dispose();
            cacheInfo = null;
        }

        setCache(mCache, mWaypoint, true);
    }

    public boolean saveButtonClicked(int x, int y) {
        if (mCache == null || !mCache.isLive())
            return false;

        if (saveButtonRec.contains(x, y))
            return true;

        return false;
    }

}
