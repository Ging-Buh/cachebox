package CB_Core.GL_UI.Controls;

import java.io.ByteArrayOutputStream;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class InfoBubble extends GL_View_Base
{

	public InfoBubble(SizeF Size, CharSequence Name)
	{
		super(Size, Name);
	}

	Pixmap pixmap = null;
	Texture tex = null;
	Sprite CachedContentSprite = null;

	/**
	 * Vector from act Bubble
	 */
	public Vector2 Pos = new Vector2();

	/**
	 * true when a dobble click on showing bubble
	 */
	public Boolean isSelected = false;

	/**
	 * set true to show Bubble from Cache with BubleCacheId
	 */
	public boolean isShow = false;

	/**
	 * is true when click on showing bubble
	 */
	public Boolean isClick;

	/**
	 * CacheID of the Cache showing Bubble
	 */
	public long CacheId = -1;

	/**
	 * Cache showing Bubble
	 */
	public Cache cache = null;
	public Waypoint waypoint = null;

	private CacheInfo cacheInfo;

	public void setCache(Cache value)
	{
		cache = value;
		SizeF size = new SizeF(width - (width * 0.04f), height - (height * 0.28f));

		cacheInfo = new CacheInfo(size, "CacheInfo", value);
		cacheInfo.setY(height - size.height);
		this.removeChilds();
		this.addChild(cacheInfo);
	}

	public void showBubleSelected()
	{

		CacheId = GlobalCore.SelectedCache().Id;
		cache = GlobalCore.SelectedCache();
		isShow = true;
	}

	private Sprite GetBubbleContentSprite(float BubbleWidth, float BubbleHeight)
	{

		if (CachedContentSprite != null) return CachedContentSprite;

		// CacheDraw.DrawInfo(cache, (int) BubbleWidth, (int) BubbleHeight, CacheDraw.DrawStyle.withOwnerAndName);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// CacheDraw.CachedBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);

		byte[] ByteArray = baos.toByteArray();

		int length = ByteArray.length;

		pixmap = new Pixmap(ByteArray, 0, length);

		tex = new Texture(pixmap, Pixmap.Format.RGBA8888, false);

		CachedContentSprite = new Sprite(tex);
		return CachedContentSprite;
	}

	public void disposeSprite()
	{
		// Texture und Sprite löschen
		if (pixmap != null) pixmap.dispose();
		pixmap = null;
		if (tex != null) tex.dispose();
		tex = null;
		CachedContentSprite = null;
	}

	@Override
	protected void render(SpriteBatch batch)
	{

		Sprite sprite = (cache == GlobalCore.SelectedCache()) ? SpriteCache.Bubble.get(1) : SpriteCache.Bubble.get(0);
		sprite.setPosition(0, 0);
		sprite.setSize(width, height);
		sprite.draw(batch);

		try
		{
			Sprite contentSprite = GetBubbleContentSprite(512, 128);

			contentSprite.setPosition(0, 0);
			contentSprite.setSize(width, height);
			contentSprite.draw(batch);
		}
		catch (Exception e)
		{
			Logger.Error("Bubble.render", "contentSprite", e);
		}

	}

	@Override
	public void onRezised(CB_RectF rec)
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

}
