package CB_UI.GL_UI.Views;

import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.Locator;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Controls.CacheInfo;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.ParentInfo;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Util.UnitFormatter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;

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
		public void renderChilds(final Batch batch, ParentInfo parentInfo)
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

	private Sprite arrow = new Sprite(SpriteCacheBase.Arrows.get(0));
	private BitmapFontCache distance = new BitmapFontCache(Fonts.getSmall());

	// private BitmapFontCache debugIndex = new BitmapFontCache(Fonts.getSmall());

	private CB_RectF ArrowRec;

	private Cache mCache;

	public Cache getCache()
	{
		return mCache;
	}

	public CacheListViewItem(CB_RectF rec, int Index, Cache cache)
	{
		super(rec, Index, cache.Name);
		mCache = cache;
		info = new extendedCacheInfo(UiSizes.that.getCacheListItemRec().asFloat(), "CacheInfo " + Index + " @" + cache.GcCode, cache);
		info.setZeroPos();
		distance.setColor(Fonts.getFontColor());
		this.addChild(info);
		PositionChangedEventList.Add(this);

		float size = this.getHeight() / 2.3f;
		ArrowRec = new CB_RectF(this.getWidth() - (size * 1.2f), this.getHeight() - (size * 1.6f), size, size);
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

			Waypoint FinalWp = mCache.GetFinalWaypoint();

			Coordinate Final = FinalWp != null ? FinalWp.Pos : mCache.Pos;
			CalculationType calcType = CalculationType.FAST;
			Cache c = GlobalCore.getSelectedCache();
			if (c != null)
			{
				calcType = mCache.Id == GlobalCore.getSelectedCache().Id ? CalculationType.ACCURATE : CalculationType.FAST;
			}

			float result[] = new float[4];

			MathUtils.computeDistanceAndBearing(calcType, position.getLatitude(), position.getLongitude(), Final.getLatitude(),
					Final.getLongitude(), result);

			double cacheBearing = -(result[2] - heading);
			mCache.cachedDistance = result[0];
			setDistanceString(UnitFormatter.DistanceString(mCache.cachedDistance));

			arrow.setRotation((float) cacheBearing);
			if (arrow.getColor() == DISABLE_COLOR)
			{
				float size = this.getHeight() / 2.3f;
				arrow = new Sprite(SpriteCacheBase.Arrows.get(0));
				arrow.setBounds(ArrowRec.getX(), ArrowRec.getY(), size, size);
				arrow.setOrigin(ArrowRec.getHalfWidth(), ArrowRec.getHalfHeight());
			}
		}
		else
		{
			if (mCache.cachedDistance >= 0) // (mCache.cachedDistance > 0)|| mCache == GlobalCore.getSelectedCache())
			{
				setDistanceString(UnitFormatter.DistanceString(mCache.cachedDistance));
			}
		}
	}

	@Override
	protected void render(Batch batch)
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

	@Override
	public void SpeedChanged()
	{
	}

}
