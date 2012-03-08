package CB_Core.GL_UI.Controls;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class InfoBubble extends GL_View_Base
{

	public InfoBubble(SizeF Size, CharSequence Name)
	{
		super(Size, Name);
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

	/**
	 * Cache showing Bubble
	 */
	private Cache mCache = null;
	private Waypoint mWaypoint = null;

	private CacheInfo cacheInfo;

	public void setCache(Cache value)
	{
		if (value == null)
		{
			mCache = null;
			mCacheId = -1;
			this.removeChilds();
			cacheInfo = null;
			return;
		}

		mCache = value;
		mCacheId = value.Id;
		SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));

		cacheInfo = new CacheInfo(size, "CacheInfo", value);
		cacheInfo.setViewMode(CacheInfo.VIEW_MODE_BUBBLE);
		cacheInfo.setY(height - size.height);
		cacheInfo.setFont(Fonts.get11());
		this.removeChilds();
		this.addChild(cacheInfo);
		requestLayout();
	}

	public void showBubleSelected()
	{

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
		SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));
		cacheInfo.setSize(size);
		cacheInfo.setY(height - size.height);
	}

	@Override
	public void onParentRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}

	public Cache getCache()
	{
		return mCache;
	}

}
