package CB_Core.GL_UI.Views;

import CB_Core.GlobalCore;
import CB_Core.UnitFormatter;
import CB_Core.Events.PositionChangedEvent;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.CacheInfo;
import CB_Core.GL_UI.Controls.List.ListViewItemBase;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Locator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class CacheListViewItem extends ListViewItemBase implements PositionChangedEvent
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

			batch.begin();
			if (hasBackground || hasNinePatchBackground)
			{

				if (hasNinePatchBackground)
				{
					nineBackground.draw(batch, 0, 0, width, height);
				}
				else
				{
					batch.draw(Background, 0, 0, width, height);
				}

			}

			this.render(batch);
			batch.end();

			Gdx.gl.glDisable(GL10.GL_SCISSOR_TEST);
		}
	}

	private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);

	protected extendedCacheInfo info;
	protected boolean isPressed = false;

	private Sprite arrow = new Sprite(SpriteCache.MapArrows.get(0));
	private BitmapFontCache distance = new BitmapFontCache(Fonts.getSmall());

	private BitmapFontCache debugIndex = new BitmapFontCache(Fonts.getSmall());

	private CB_RectF ArrowRec;

	private Cache mCache;

	public CacheListViewItem(CB_RectF rec, int Index, Cache cache)
	{
		super(rec, Index, cache.Name);
		mCache = cache;
		info = new extendedCacheInfo(UiSizes.getCacheListItemRec().asFloat(), "CacheInfo " + Index + " @" + cache.GcCode, cache);
		info.setZeroPos();
		setBackground();
		this.addChild(info);
		PositionChangedEventList.Add(this);

		float size = this.height / 2.3f;
		ArrowRec = new CB_RectF(this.width - (size * 1.2f), this.height - (size * 1.6f), size, size);
		arrow.setBounds(ArrowRec.getX(), ArrowRec.getY(), size, size);
		arrow.setOrigin(ArrowRec.getHalfWidth(), ArrowRec.getHalfHeight());

		if (GlobalCore.LastValidPosition == null || GlobalCore.Locator == null)
		{
			arrow.setColor(DISABLE_COLOR);
			setDistanceString("---");
		}
		else
		{
			setActLocator();
		}

		debugIndex.setText(String.valueOf(Index), this.width - ArrowRec.getWidth() - ArrowRec.getWidth(), 20);

	}

	private void setDistanceString(String txt)
	{
		TextBounds bounds = distance.setText(txt, ArrowRec.getX(), ArrowRec.getY());
		float x = ArrowRec.getHalfWidth() - (bounds.width / 2f);
		distance.setPosition(x, 0);
	}

	private void setActLocator()
	{
		if (GlobalCore.LastValidPosition.Valid || GlobalCore.Marker.Valid)
		{
			Coordinate position = (GlobalCore.Marker.Valid) ? GlobalCore.Marker : GlobalCore.LastValidPosition;
			double heading = (GlobalCore.Locator != null) ? GlobalCore.Locator.getHeading() : 0;
			double bearing = Coordinate.Bearing(position.Latitude, position.Longitude, mCache.Latitude(), mCache.Longitude());
			double cacheBearing = -(bearing - heading);
			setDistanceString(UnitFormatter.DistanceString(mCache.Distance(false)));

			arrow.setRotation((float) cacheBearing);
			if (arrow.getColor() == DISABLE_COLOR)
			{
				float size = this.height / 2.3f;
				arrow = new Sprite(SpriteCache.MapArrows.get(0));
				arrow.setBounds(ArrowRec.getX(), ArrowRec.getY(), size, size);
				arrow.setOrigin(ArrowRec.getHalfWidth(), ArrowRec.getHalfHeight());
			}
		}
	}

	@Override
	protected void Initial()
	{
		setBackground();
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);

		if (arrow != null) arrow.draw(batch);
		if (distance != null) distance.draw(batch);
		if (debugIndex != null) debugIndex.draw(batch);

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

	private void setBackground()
	{

		Boolean BackGroundChanger = ((this.getIndex() % 2) == 1);

		if (isSelected)
		{
			this.setBackground(new NinePatch(SpriteCache.uiAtlas.findRegion("listrec_selected"), 8, 8, 8, 8));
		}
		else if (BackGroundChanger)
		{
			this.setBackground(new NinePatch(SpriteCache.uiAtlas.findRegion("listrec_first"), 8, 8, 8, 8));
		}
		else
		{
			this.setBackground(new NinePatch(SpriteCache.uiAtlas.findRegion("listrec_secend"), 8, 8, 8, 8));
		}

		GL_Listener.glListener.renderOnce(this);
	}

	@Override
	public void PositionChanged(Locator locator)
	{
		setActLocator();
	}

	@Override
	public void OrientationChanged(float heading)
	{
		// TODO Auto-generated method stub

	}

}
