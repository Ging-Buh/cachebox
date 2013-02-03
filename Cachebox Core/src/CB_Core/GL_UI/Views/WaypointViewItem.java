package CB_Core.GL_UI.Views;

import CB_Core.GlobalCore;
import CB_Core.UnitFormatter;
import CB_Core.Enums.CacheTypes;
import CB_Core.Events.PositionChangedEvent;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.ParentInfo;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.CacheInfo;
import CB_Core.GL_UI.Controls.List.ListViewItemBackground;
import CB_Core.Locator.Locator;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class WaypointViewItem extends ListViewItemBackground implements PositionChangedEvent
{
	private Cache mCache;
	private Waypoint mWaypoint;

	protected extendedCacheInfo info;
	protected boolean isPressed = false;

	private final Color DISABLE_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);
	private CB_RectF ArrowRec;
	private Sprite arrow = new Sprite(SpriteCache.Arrows.get(0));
	private BitmapFontCache distance = new BitmapFontCache(Fonts.getSmall());
	private Sprite mIconSprite;
	private float mIconSize = 0;
	private float mMargin = 0;

	private BitmapFontCache mNameCache;
	private BitmapFontCache mDescCache;
	private BitmapFontCache mCoordCache;

	private int ViewMode = CacheInfo.VIEW_MODE_WAYPOINTS;

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

	public WaypointViewItem(CB_RectF rec, int Index, Cache cache, Waypoint waypoint)
	{
		super(rec, Index, "");
		ViewMode = CacheInfo.VIEW_MODE_WAYPOINTS;
		initial(Index, cache, waypoint);
	}

	public WaypointViewItem(CB_RectF rec, int Index, Cache cache, Waypoint waypoint, int viewMode)
	{
		super(rec, Index, "");
		ViewMode = viewMode;
		initial(Index, cache, waypoint);
	}

	private void initial(int Index, Cache cache, Waypoint waypoint)
	{
		this.mCache = cache;
		this.mWaypoint = waypoint;

		distance.setColor(Fonts.getFontColor());

		if (waypoint == null) // this Item is the Cache
		{

			if (cache == null)
			{
				arrow = null;
				distance = null;
				return;
			}

			info = new extendedCacheInfo(UiSizes.getCacheListItemRec().asFloat(), "CacheInfo " + Index + " @" + cache.GcCode, cache);
			info.setZeroPos();
			info.setViewMode(ViewMode);

			this.addChild(info);
		}

		if (ViewMode != CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK)// For Compass without own compass
		{
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

		}
		else
		{
			arrow = null;
			distance = null;
		}
	}

	public Waypoint getWaypoint()
	{
		return mWaypoint;
	}

	private void setDistanceString(String txt)
	{
		TextBounds bounds = distance.setText(txt, ArrowRec.getX(), ArrowRec.getY());
		float x = ArrowRec.getHalfWidth() - (bounds.width / 2f);
		distance.setPosition(x, 0);
	}

	private void setActLocator()
	{
		if (GlobalCore.LastValidPosition.Valid)
		{

			double lat = (mWaypoint == null) ? mCache.Latitude() : mWaypoint.Pos.getLatitude();
			double lon = (mWaypoint == null) ? mCache.Longitude() : mWaypoint.Pos.getLongitude();
			float distance = (mWaypoint == null) ? mCache.Distance(true) : mWaypoint.Distance();

			Coordinate position = GlobalCore.LastValidPosition;
			double heading = (GlobalCore.Locator != null) ? GlobalCore.Locator.getHeading() : 0;
			double bearing = Coordinate.Bearing(position.getLatitude(), position.getLongitude(), lat, lon);
			double cacheBearing = -(bearing - heading);
			setDistanceString(UnitFormatter.DistanceString(distance));

			if (ViewMode != CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK)
			{
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
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		if (mIndex != -1) super.render(batch);

		if (arrow != null) arrow.draw(batch);
		if (distance != null) distance.draw(batch);
		if (mIconSprite != null) mIconSprite.draw(batch);
		if (mIconSprite == null && mWaypoint != null) requestLayout();

		if (mNameCache != null) mNameCache.draw(batch);
		if (mDescCache != null) mDescCache.draw(batch);
		if (mCoordCache != null) mCoordCache.draw(batch);
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
	public void PositionChanged(Locator locator)
	{
		setActLocator();
	}

	@Override
	public void OrientationChanged(float heading)
	{
		setActLocator();
	}

	@Override
	public String getReceiverName()
	{
		return "Core.WayPointViewItem";
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	private void requestLayout()
	{
		if (mWaypoint != null)
		{
			float scaleFactor = width / UiSizes.getCacheListItemRec().getWidth();
			float mLeft = 3 * scaleFactor;
			float mTop = 3 * scaleFactor;
			mMargin = mLeft;

			mIconSize = Fonts.Measure("T").height * 3.5f * scaleFactor;

			Vector2 mSpriteCachePos = new Vector2(mLeft + mMargin, height - mTop - mIconSize);

			{ // Icon Sprite erstellen
				// MultiStage Waypoint anders darstellen wenn dieser als Startpunkt definiert ist
				if ((mWaypoint.Type == CacheTypes.MultiStage) && mWaypoint.IsStart) mIconSprite = new Sprite(SpriteCache.BigIcons.get(23));
				else
					mIconSprite = new Sprite(SpriteCache.BigIcons.get(mWaypoint.Type.ordinal()));

				mIconSprite.setSize(mIconSize, mIconSize);
				mIconSprite.setPosition(mSpriteCachePos.x, mSpriteCachePos.y);
			}

			mNameCache = new BitmapFontCache(Fonts.getNormal());
			mDescCache = new BitmapFontCache(Fonts.getBubbleNormal());
			mCoordCache = new BitmapFontCache(Fonts.getBubbleNormal());

			mNameCache.setColor(Fonts.getFontColor());
			mDescCache.setColor(Fonts.getFontColor());
			mCoordCache.setColor(Fonts.getFontColor());

			float textYPos = this.height - mMargin;

			textYPos -= (mNameCache.setMultiLineText(mWaypoint.GcCode + ": " + mWaypoint.Title, mSpriteCachePos.x + mIconSize + mMargin,
					textYPos)).height + mMargin + mMargin;

			if (ViewMode == CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK)
			{
				mDescCache = null;
			}
			else
			{
				if (!mWaypoint.Description.equals(""))
				{
					textYPos -= (mDescCache.setMultiLineText(mWaypoint.Description, mSpriteCachePos.x + mIconSize + mMargin, textYPos)).height
							+ mMargin + mMargin;
				}
			}

			String sCoord = "";

			if (ViewMode == CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK)
			{
				sCoord = mWaypoint.Pos.FormatCoordinateLineBreake();
			}
			else
			{
				sCoord = mWaypoint.Pos.FormatCoordinate();
			}

			textYPos -= (mCoordCache.setMultiLineText(sCoord, mSpriteCachePos.x + mIconSize + mMargin, textYPos)).height + mMargin
					+ mMargin;

		}

	}
}
