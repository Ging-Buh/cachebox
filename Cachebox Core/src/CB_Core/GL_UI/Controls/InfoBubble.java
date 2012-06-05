package CB_Core.GL_UI.Controls;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class InfoBubble extends CB_View_Base
{

	public InfoBubble(SizeF Size, String Name)
	{
		super(Size, Name);
		registerSkinChangedEvent();
	}

	Pixmap pixmap = null;
	Texture tex = null;
	// Sprite CachedContentSprite = null;

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
		SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));

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
		mCacheId = GlobalCore.SelectedCache().Id;
		mCache = GlobalCore.SelectedCache();
		setVisibility(VISIBLE);
	}

	@Override
	protected void render(SpriteBatch batch)
	{

		Sprite sprite = (mCache == GlobalCore.SelectedCache()) ? SpriteCache.Bubble.get(1) : SpriteCache.Bubble.get(0);
		sprite.setPosition(0, 0);
		sprite.setSize(width, height);
		sprite.draw(batch);

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		requestLayout();
	}

	private void requestLayout()
	{
		// Logger.LogCat("InfoBubble RequestLayout");
		SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));
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
		// TODO Auto-generated method stub

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
