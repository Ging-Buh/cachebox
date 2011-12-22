package de.cachebox_test.Map;

import java.io.ByteArrayOutputStream;

import CB_Core.GlobalCore;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import android.graphics.Bitmap;
import android.graphics.RectF;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import de.cachebox_test.Components.CacheDraw;

/**
 * Enthält Variablen und Logik für die Anzeige der Cache Info Bubble in der Map.
 * 
 * @author Longri
 */
public class Bubble
{
	/**
	 * Vector from act Bubble
	 */
	public static Vector2 Pos = new Vector2();

	/**
	 * true when a dobble click on showing bubble
	 */
	public static Boolean isSelected = false;

	/**
	 * set true to show Bubble from Cache with BubleCacheId
	 */
	public static boolean isShow = false;

	/**
	 * is true when click on showing bubble
	 */
	public static Boolean isClick;

	/**
	 * CacheID of the Cache showing Bubble
	 */
	public static long CacheId = -1;

	/**
	 * Cache showing Bubble
	 */
	public static Cache cache = null;
	public static Waypoint waypoint = null;

	/**
	 * Rectangle to Draw Bubble or detect click inside
	 */
	public static RectF DrawRec;

	public static void showBubleSelected()
	{

		CacheId = GlobalCore.SelectedCache().Id;
		cache = GlobalCore.SelectedCache();
		isShow = true;
	}

	public static void disposeSprite()
	{
		// Texture und Sprite löschen
		if (pixmap != null) pixmap.dispose();
		pixmap = null;
		if (tex != null) tex.dispose();
		tex = null;
		CachedContentSprite = null;
	}

	public static void render(float xSizeUnder, float ySizeUnder)
	{

		if (!isShow) return;

		float BubbleWidth = 430;
		float BubbleHeight = 140;
		DrawRec = new RectF(Pos.x - (BubbleWidth / 2) + (xSizeUnder / 2), Pos.y,
				Pos.x - (BubbleWidth / 2) + (xSizeUnder / 2) + BubbleWidth, Pos.y + BubbleHeight);

		Sprite sprite = (cache == GlobalCore.SelectedCache()) ? SpriteCache.Bubble.get(1) : SpriteCache.Bubble.get(0);
		sprite.setPosition(Pos.x - (BubbleWidth / 2) + (xSizeUnder / 2), Pos.y);
		sprite.setSize(BubbleWidth, BubbleHeight + 40);
		sprite.draw(MapViewGlListener.batch);

		try
		{
			Sprite contentSprite = GetBubbleContentSprite(BubbleWidth, BubbleHeight);

			contentSprite.setPosition(Pos.x - (BubbleWidth / 2) + (xSizeUnder / 2) + 10, Pos.y + 40);
			contentSprite.setSize(BubbleWidth, BubbleHeight);
			contentSprite.draw(MapViewGlListener.batch);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Sprite overlay = SpriteCache.Bubble.get(2);
		overlay.setPosition(Pos.x - (BubbleWidth / 2) + (xSizeUnder / 2), Pos.y);
		overlay.setSize(BubbleWidth, BubbleHeight + 40);
		overlay.draw(MapViewGlListener.batch);

	}

	static Pixmap pixmap = null;
	static Texture tex = null;
	static Sprite CachedContentSprite = null;

	private static Sprite GetBubbleContentSprite(float BubbleWidth, float BubbleHeight)
	{

		if (CachedContentSprite != null) return CachedContentSprite;

		CacheDraw.DrawInfo(cache, (int) BubbleWidth, (int) BubbleHeight, CacheDraw.DrawStyle.withOwnerAndName);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CacheDraw.CachedBitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);

		byte[] ByteArray = baos.toByteArray();

		int length = ByteArray.length;

		pixmap = new Pixmap(ByteArray, 0, length);

		tex = new Texture(pixmap, Pixmap.Format.RGBA8888, false);

		CachedContentSprite = new Sprite(tex);
		return CachedContentSprite;
	}
}
