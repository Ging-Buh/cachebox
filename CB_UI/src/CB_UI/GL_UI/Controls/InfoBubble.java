package CB_UI.GL_UI.Controls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.SpriteCacheBase.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.SizeF;

public class InfoBubble extends CB_View_Base {

    public InfoBubble(SizeF Size, String Name) {
	super(Size, Name);
	registerSkinChangedEvent();
    }

    /**
     * CacheID of the Cache showing Bubble
     */
    private long mCacheId = -1;
    private static CB_RectF saveButtonRec;

    public long getCacheId() {
	return mCacheId;
    }

    public Waypoint getWaypoint() {
	return mWaypoint;
    }

    /**
     * Cache showing Bubble
     */
    private Cache mCache = null;
    private Waypoint mWaypoint = null;
    private CacheInfo cacheInfo;
    private Drawable saveIcon = new SpriteDrawable(SpriteCacheBase.Icons.get(IconName.save_66.ordinal()));

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

	// log.debug("New Cache @InfoBubble");
	mCache = cache;
	mCacheId = cache.Id;
	mWaypoint = waypoint;
	// SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));
	SizeF size = new SizeF(0.96f * getWidth(), 0.72f * getHeight());

	// if Cache a event we must load details for needed DateHidden
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
	// log.debug("Show BubbleSelected");
	mCacheId = GlobalCore.getSelectedCache().Id;
	mCache = GlobalCore.getSelectedCache();
	setVisible();
    }

    @Override
    protected void render(Batch batch) {
	boolean selectedCache = false;
	if (GlobalCore.isSetSelectedCache()) {
	    selectedCache = mCache.Id == GlobalCore.getSelectedCache().Id;
	}

	Sprite sprite = selectedCache ? SpriteCacheBase.Bubble.get(1) : SpriteCacheBase.Bubble.get(0);
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
	// log.debug("InfoBubble RequestLayout");
	// SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));
	SizeF size = new SizeF(0.96f * getWidth(), 0.72f * getHeight());
	cacheInfo.setSize(size);
	cacheInfo.setY(getHeight() - size.height);
    }

    public Cache getCache() {
	return mCache;
    }

    @Override
    protected void Initial() {

    }

    @Override
    protected void SkinIsChanged() {
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
