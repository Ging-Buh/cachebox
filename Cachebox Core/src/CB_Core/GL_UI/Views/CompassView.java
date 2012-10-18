package CB_Core.GL_UI.Views;

import CB_Core.GlobalCore;
import CB_Core.UnitFormatter;
import CB_Core.Events.PositionChangedEvent;
import CB_Core.Events.PositionChangedEventList;
import CB_Core.Events.SelectedCacheEvent;
import CB_Core.Events.SelectedCacheEventList;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.Controls.Box;
import CB_Core.GL_UI.Controls.CacheInfo;
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Locator.Locator;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class CompassView extends CB_View_Base implements SelectedCacheEvent, PositionChangedEvent
{
	CB_RectF imageRec, WpInfoRec;
	Image frame, scale, arrow, att[];
	CacheInfo info;
	WaypointViewItem wpInfo;
	Box topBox, leftBox, rightBox, rightBoxMask, distanceBack;
	MapView map;

	Label lblDistance;

	Cache aktCache;
	Waypoint aktWaypoint;

	float margin, attHeight;

	public CompassView(CB_RectF rec, String Name)
	{
		super(rec, Name);

		leftBox = new Box(new CB_RectF(0, 0, this.halfWidth, this.halfWidth), "left");
		rightBox = new Box(new CB_RectF(this.halfWidth, 0, this.halfWidth, this.halfWidth), "right");
		rightBoxMask = new Box(new CB_RectF(this.halfWidth, 0, this.halfWidth, this.halfWidth), "rightMask");
		topBox = new Box(new CB_RectF(0, this.halfWidth, this.width, this.height - this.halfWidth), "top");

		rightBox.setBackground(SpriteCache.activityBackground);
		rightBoxMask.setBackground(SpriteCache.activityBorderMask);
		leftBox.setBackground(SpriteCache.activityBackground);
		topBox.setBackground(SpriteCache.activityBackground);

		this.addChild(topBox);
		this.addChild(leftBox);
		this.addChild(rightBox);
		this.addChild(rightBoxMask);

		margin = GL_UISizes.margin;

		WpInfoRec = new CB_RectF(0, 0, this.halfWidth - margin - margin, GL_UISizes.Info.getHeight());

		imageRec = (new CB_RectF(0, 0, width, width)).ScaleCenter(0.6f);
		this.setBackground(SpriteCache.ListBack);

		frame = new Image(imageRec, "frame");
		frame.setDrawable(SpriteCache.Compass.get(0));
		this.addChild(frame);

		scale = new Image(imageRec, "scale");
		scale.setDrawable(SpriteCache.Compass.get(1));
		this.addChild(scale);

		arrow = new Image(imageRec, "arrow");
		arrow.setDrawable(SpriteCache.Compass.get(4));
		this.addChild(arrow);

		info = new CacheInfo(
				new SizeF(width - margin - margin, UiSizes.getCacheInfoHeight() + margin + Fonts.Measure("T").height + margin),
				"Cacheinfo", GlobalCore.SelectedCache());

		info.setViewMode(CacheInfo.VIEW_MODE_COMPAS);

		topBox.addChild(info);

		att = new Image[12];
		attHeight = (this.width / 10) - margin;

		CB_RectF attRec = new CB_RectF(0, 0, attHeight, attHeight);

		for (int i = 0; i < 12; i++)
		{
			att[i] = new Image(attRec, "");
			topBox.addChild(att[i]);
		}

		map = new MapView(rightBox, true, "CompassMap");
		map.setZeroPos();
		rightBox.addChild(map);

		wpInfo = new WaypointViewItem(WpInfoRec, -1, null, null, CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK);
		rightBox.addChild(wpInfo);

		lblDistance = new Label(margin, margin, rightBox.getWidth(), (Fonts.MeasureBig("T").height * 2.5f), "distanceLabel");
		BitmapFont font = Fonts.getCompass();
		lblDistance.setFont(font);
		lblDistance.setHAlignment(HAlignment.CENTER);

		distanceBack = new Box(lblDistance, "DistanceBack");
		distanceBack.setBackground(SpriteCache.InfoBack);
		rightBox.addChild(distanceBack);
		rightBox.addChild(lblDistance);

		setCache(GlobalCore.SelectedCache());
		setWP(GlobalCore.SelectedCache(), GlobalCore.SelectedWaypoint());

		Layout();

		SelectedCacheEventList.Add(this);

	}

	@Override
	public void onShow()
	{
		PositionChangedEventList.Add(this);
		if (map != null) map.onShow();
	}

	@Override
	public void onHide()
	{
		PositionChangedEventList.Remove(this);
		if (map != null) map.onHide();
	}

	@Override
	protected void Initial()
	{

	}

	@Override
	protected void SkinIsChanged()
	{

	}

	private void setWP(Cache c, Waypoint wp)
	{
		if (wp == null)
		{
			if (wpInfo != null) this.removeChild(wpInfo);
			wpInfo = null;
		}
		else
		{
			if (wpInfo != null) this.removeChild(wpInfo);
			wpInfo = new WaypointViewItem(WpInfoRec, -1, c, wp, CacheInfo.VIEW_MODE_WAYPOINTS_WITH_CORRD_LINEBREAK);
			rightBox.addChild(wpInfo);

			wpInfo.setBackground(null);

		}
		aktWaypoint = wp;
		setCache(c);
	}

	private void setCache(Cache c)
	{
		info.setCache(c);
		aktCache = c;
		for (int i = 0; i < 12; i++)
		{
			try
			{
				String ImageName = c.getAttributes().get(i).getImageName() + "Icon";
				ImageName = ImageName.replace("_", "-");
				att[i].setDrawable(new SpriteDrawable(SpriteCache.getThemedSprite(ImageName)));
			}
			catch (Exception e)
			{
				att[i].setDrawable(null);
			}
		}
		Layout();
	}

	private void Layout()
	{

		info.setX(margin);
		info.setY(topBox.getHeight() - info.getHeight() - margin);

		float attY = info.getY() - margin - margin - attHeight;
		float attX = margin;

		for (int i = 0; i < 12; i++)
		{
			att[i].setPos(attX, attY);
			attX += attHeight + margin;

			if (i == 6)
			{
				attX = margin;
				attY -= margin + attHeight;
			}
		}

		// Die größe des Kompasses nach rest Platz berechnen

		float compassHeight = 0;

		compassHeight = this.halfWidth - margin - margin;

		lblDistance.setY(margin);

		SizeF s = new SizeF(compassHeight, compassHeight);

		frame.setSize(s);
		scale.setSize(s);

		// calc center

		float left = margin;

		frame.setX(left);
		scale.setX(left);

		frame.setY(margin);
		scale.setY(margin);

		arrow.setSize(frame);
		arrow.setY(margin);
		arrow.setX(frame.getX());
		arrow.setRec(arrow.ScaleCenter(0.8f));
		arrow.setScale(0.7f);

		scale.setOriginCenter();
		arrow.setOriginCenter();

		if (wpInfo != null)
		{
			wpInfo.setX(margin);
			wpInfo.setY(rightBox.getHeight() - wpInfo.getHeight() - margin);
		}

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		leftBox.setRec(new CB_RectF(0, 0, this.halfWidth, this.halfWidth));
		rightBox.setRec(new CB_RectF(this.halfWidth, 0, this.halfWidth, this.halfWidth));
		rightBoxMask.setRec(new CB_RectF(this.halfWidth, -1, this.halfWidth, this.halfWidth + 1));
		topBox.setRec(new CB_RectF(0, this.halfWidth, this.width, this.height - this.halfWidth));

		Layout();
	}

	@Override
	public void SelectedCacheChanged(Cache cache, Waypoint waypoint)
	{
		setWP(cache, waypoint);
	}

	@Override
	public void PositionChanged(Locator locator)
	{
		if (aktCache == null) return;

		if (GlobalCore.LastValidPosition.Valid)
		{
			Coordinate position = GlobalCore.LastValidPosition;
			double heading = (GlobalCore.Locator != null) ? GlobalCore.Locator.getHeading() : 0;

			Coordinate dest = aktCache.Pos;
			float distance = aktCache.Distance(false);
			if (aktWaypoint != null)
			{
				dest = aktWaypoint.Pos;
				distance = aktWaypoint.Distance();
			}
			double bearing = Coordinate.Bearing(position, dest);
			double relativeBearing = bearing - heading;

			arrow.setRotate((float) -relativeBearing);
			scale.setRotate((float) heading);
			TextBounds bounds = lblDistance.setText(UnitFormatter.DistanceString(distance));

			float labelWidth = bounds.width + (6 * margin);

			distanceBack.setWidth(labelWidth);
			distanceBack.setX(rightBox.getHalfWidth() - distanceBack.getHalfWidth());
			GL.that.renderOnce("Compass-PositionChanged");
		}
	}

	@Override
	public void OrientationChanged(float Heading)
	{
		if (aktCache == null) return;

		if (GlobalCore.LastValidPosition.Valid)
		{
			Coordinate position = GlobalCore.LastValidPosition;
			double heading = (GlobalCore.Locator != null) ? GlobalCore.Locator.getHeading() : 0;

			Coordinate dest = aktCache.Pos;
			float distance = aktCache.Distance(false);
			if (aktWaypoint != null)
			{
				dest = aktWaypoint.Pos;
				distance = aktWaypoint.Distance();
			}
			double bearing = Coordinate.Bearing(position, dest);
			double relativeBearing = bearing - heading;
			arrow.setRotate((float) -relativeBearing);
			scale.setRotate((float) heading);
			GL.that.renderOnce("Compass-OrientationChanged");
		}
	}

	@Override
	public String getReceiverName()
	{
		return "CompassView";
	}
}
