package CB_UI.GL_UI.Controls;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_UI.GlobalCore;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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

	public void setCache(Cache value, Waypoint waypoint)
	{
		setCache(value, waypoint, false);
	}

	public void setCache(Cache value, Waypoint waypoint, boolean force)
	{
		if (value == null)
		{
			mCache = null;
			mCacheId = -1;
			this.removeChilds();
			cacheInfo = null;
			return;
		}

		if (!force)
		{
			if ((mCache != null) && (mCache.Id == value.Id) && (mWaypoint == waypoint)) return;
		}

		// Logger.LogCat("New Cache @InfoBubble");
		mCache = value;
		mCacheId = value.Id;
		mWaypoint = waypoint;
		// SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));
		SizeF size = new SizeF(0.96f * width, 0.72f * height);

		cacheInfo = new CacheInfo(size, "CacheInfo", value);
		cacheInfo.setViewMode(CacheInfo.VIEW_MODE_BUBBLE);
		cacheInfo.setY(height - size.height);
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
	protected void render(SpriteBatch batch)
	{
		Sprite sprite = (mCache == GlobalCore.getSelectedCache()) ? SpriteCacheBase.Bubble.get(1) : SpriteCacheBase.Bubble.get(0);
		sprite.setPosition(0, 0);
		sprite.setSize(width, height);
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
		SizeF size = new SizeF(0.96f * width, 0.72f * height);
		cacheInfo.setSize(size);
		cacheInfo.setY(height - size.height);
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