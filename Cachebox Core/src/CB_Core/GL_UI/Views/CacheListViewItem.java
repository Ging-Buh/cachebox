package CB_Core.GL_UI.Views;

import CB_Core.UnitFormatter;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.CacheInfo;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CacheListViewItem extends ListViewItemBackground implements PositionChangedEvent
{

	/**
	 * mit ausgeschaltener scissor berechnung
	 * 
	 * @author Longri
	 */
	private class extendedCacheInfo extends CacheInfo
	{

		public extendedCacheInfo(CB_RectF rec, String Name, Cache value)
		{
			super(rec, Name, value);
		}

		@Override
		public void renderChilds(final SpriteBatch batch, ParentInfo parentInfo)
		{
			if (!disableScissor) Gdx.gl.glEnable(GL10.GL_SCISSOR_TEST);

			batch.flush();

			this.render(batch);
			batch.flush();

			Gdx.gl.glDisable(GL10.GL_SCISSOR_TEST);
		}
	}

	private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);

	protected extendedCacheInfo info;
	protected boolean isPressed = false;

	private Sprite arrow = new Sprite(SpriteCache.Arrows.get(0));
	private BitmapFontCache distance = new BitmapFontCache(Fonts.getSmall());

	// private BitmapFontCache debugIndex = new BitmapFontCache(Fonts.getSmall());

	private CB_RectF ArrowRec;

	private Cache mCache;

	public CacheListViewItem(CB_RectF rec, int Index, Cache cache)
	{
		super(rec, Index, cache.Name);
		mCache = cache;
		info = new extendedCacheInfo(UiSizes.getCacheListItemRec().asFloat(), "CacheInfo " + Index + " @" + cache.GcCode, cache);
		info.setZeroPos();
		distance.setColor(Fonts.getFontColor());
		this.addChild(info);
		PositionChangedEventList.Add(this);

		float size = this.height / 2.3f;
		ArrowRec = new CB_RectF(this.width - (size * 1.2f), this.height - (size * 1.6f), size, size);
		arrow.setBounds(ArrowRec.getX(), ArrowRec.getY(), size, size);
		arrow.setOrigin(ArrowRec.getHalfWidth(), ArrowRec.getHalfHeight());

		if (!Locator.Valid())
		{
			arrow.setColor(DISABLE_COLOR);
			setDistanceString("---");
		}
		else
		{
			setActLocator();
		}

		// Logger.LogCat("New CacheListItem Index:" + String.valueOf(Index));

	}

	private void setDistanceString(String txt)
	{
		synchronized (distance)
		{
			TextBounds bounds = distance.setText(txt, ArrowRec.getX(), ArrowRec.getY());
			float x = ArrowRec.getHalfWidth() - (bounds.width / 2f);
			distance.setPosition(x, 0);
		}

	}

	double heading = 0;

	private void setActLocator()
	{

		// Logger.LogCat("CacheListItem set ActLocator");

		if (Locator.Valid())
		{
			Coordinate position = Locator.getCoordinate();

			double bearing = Coordinate.Bearing(position.getLatitude(), position.getLongitude(), mCache.Latitude(), mCache.Longitude());
			double cacheBearing = -(bearing - heading);
			setDistanceString(UnitFormatter.DistanceString(mCache.Distance(true)));

			arrow.setRotation((float) cacheBearing);
			if (arrow.getColor() == DISABLE_COLOR)
			{
				float size = this.height / 2.3f;
				arrow = new Sprite(SpriteCache.Arrows.get(0));
				arrow.setBounds(ArrowRec.getX(), ArrowRec.getY(), size, size);
				arrow.setOrigin(ArrowRec.getHalfWidth(), ArrowRec.getHalfHeight());
			}
		}
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);

		if (arrow != null) arrow.draw(batch);
		if (distance != null)
		{
			synchronized (distance)
			{
				distance.draw(batch);
			}

		}

	}

	@Override
	public void dispose()
	{
		PositionChangedEventList.Remove(this);
		if (info != null) info.dispose();
		info = null;

		arrow = null;
		// if (distance != null) distance.dispose();
		distance = null;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		isPressed = true;

		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		isPressed = false;

		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		isPressed = false;

		return false;
	}

	@Override
	public void PositionChanged()
	{
		setActLocator();
	}

	@Override
	public void OrientationChanged()
	{
		this.heading = Locator.getHeading();
		setActLocator();
	}

	@Override
	public String getReceiverName()
	{
		return "Core.CacheListViewItem";
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	@Override
	public Priority getPriority()
	{
		return Priority.Normal;
	}

}
