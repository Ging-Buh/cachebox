package CB_UI.GL_UI.Controls;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class InfoBubble extends CB_View_Base
{

	public InfoBubble(SizeF Size, String Name)
	{
		super(Size, Name);
		registerSkinChangedEvent();
	}

	/**
	 * CacheID of the Cache showing Bubble
	 */
	private long mCacheId = -1;

	public long getCacheId()
	{
		return mCacheId;
	}

	public Waypoint getWaypoint()
	{
		return mWaypoint;
	}

	/**
	 * Cache showing Bubble
	 */
	private Cache mCache = null;
	private Waypoint mWaypoint = null;

	private CacheInfo cacheInfo;

	public void setCache(Cache cache, Waypoint waypoint)
	{
		setCache(cache, waypoint, false);
	}

	public void setCache(Cache Cache, Waypoint waypoint, boolean force)
	{

		if (Cache == null)
		{
			mCache = null;
			mCacheId = -1;
			this.removeChilds();
			cacheInfo = null;
			return;
		}

		Cache cache = Cache;

		if (!force)
		{
			if ((mCache != null) && (mCache.Id == cache.Id) && (mWaypoint == waypoint)) return;
		}

		// Logger.LogCat("New Cache @InfoBubble");
		mCache = cache;
		mCacheId = cache.Id;
		mWaypoint = waypoint;
		// SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));
		SizeF size = new SizeF(0.96f * getWidth(), 0.72f * getHeight());

		cacheInfo = new CacheInfo(size, "CacheInfo", cache);
		cacheInfo.setViewMode(CacheInfo.VIEW_MODE_BUBBLE);
		cacheInfo.setY(getHeight() - size.height);
		cacheInfo.setFont(Fonts.getBubbleNormal());
		cacheInfo.setSmallFont(Fonts.getBubbleSmall());
		this.removeChilds();
		this.addChild(cacheInfo);
		requestLayout();
	}

	public void showBubbleSelected()
	{
		// Logger.LogCat("Show BubbleSelected");
		mCacheId = GlobalCore.getSelectedCache().Id;
		mCache = GlobalCore.getSelectedCache();
		setVisible();
	}

	@Override
	protected void render(Batch batch)
	{
		Sprite sprite = (mCache.Id == GlobalCore.getSelectedCache().Id) ? SpriteCacheBase.Bubble.get(1) : SpriteCacheBase.Bubble.get(0);
		sprite.setPosition(0, 0);
		sprite.setSize(getWidth(), getHeight());
		sprite.draw(batch);
	}

	@Override
	public void onResized(CB_RectF rec)
	{
		requestLayout();
	}

	private void requestLayout()
	{
		// Logger.LogCat("InfoBubble RequestLayout");
		// SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));
		SizeF size = new SizeF(0.96f * getWidth(), 0.72f * getHeight());
		cacheInfo.setSize(size);
		cacheInfo.setY(getHeight() - size.height);
	}

	public Cache getCache()
	{
		return mCache;
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{
		if (cacheInfo != null)
		{
			cacheInfo.dispose();
			cacheInfo = null;
		}

		setCache(mCache, mWaypoint, true);
	}

}
