package CB_Core.GL_UI.Views;

import java.util.Date;

import CB_Core.Config;
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
import CB_Core.GL_UI.Controls.Image;
import CB_Core.GL_UI.Controls.Label;
import CB_Core.GL_UI.Controls.Label.VAlignment;
import CB_Core.GL_UI.Controls.SatBarChart;
import CB_Core.GL_UI.Controls.ScrollBox;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Locator.Locator;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Math.SizeF;
import CB_Core.Types.Cache;
import CB_Core.Types.Coordinate;
import CB_Core.Types.Waypoint;
import CB_Core.Util.Astronomy;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class CompassView extends CB_View_Base implements SelectedCacheEvent, PositionChangedEvent
{
	public static CompassView that;
	private CB_RectF imageRec;
	private Image frame, scale, arrow, att[], Icon, Sun, Moon;

	private Box topContentBox, leftBox, rightBox, rightBoxMask, distanceBack;
	private ScrollBox topBox;
	private MapView map;
	private SatBarChart chart;
	private Label lblDistance, lbl_Name, lblGcCode, lblCoords, lblDesc, lblAlt, lblAccuracy, lblSats, lblOwnCoords;

	private Cache aktCache;
	private Waypoint aktWaypoint;

	private float margin, attHeight, descHeight, lblHeight;
	private double heading;
	private boolean isInitial, showMap, showName, showIcon, showAtt, showGcCode, showCoords, showWpDesc, showSatInfos, showSunMoon,
			showAnyContent;

	public CompassView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		margin = GL_UISizes.margin;
		SelectedCacheEventList.Add(this);
		that = this;
		aktCache = GlobalCore.getSelectedCache();
		aktWaypoint = GlobalCore.getSelectedWaypoint();
		createControls();
		Layout();
	}

	@Override
	public void onShow()
	{
		super.onShow();
		if (chart != null)
		{
			chart.onShow();
			chart.setDrawWithAlpha(false);
		}

		if (chkSettingChanges())
		{
			this.removeChilds();
			createControls();
			Layout();
		}
		PositionChangedEventList.Add(this);
		if (map != null) map.onShow();

		PositionChanged(null); // PositionChanged() use only last valid positions
	}

	@Override
	public void onHide()
	{
		super.onHide();
		if (chart != null) chart.onHide();
		PositionChangedEventList.Remove(this);
		if (map != null) map.onHide();
	}

	@Override
	protected void Initial()
	{
		if (isInitial) return;
		chkSettingChanges();
		createControls();
		Layout();
		isInitial = true;
	}

	private boolean chkSettingChanges()
	{
		boolean ret = false;

		if (showMap != Config.settings.CompassShowMap.getValue())
		{
			showMap = Config.settings.CompassShowMap.getValue();
			ret = true;
		}
		if (showName != Config.settings.CompassShowWP_Name.getValue())
		{
			showName = Config.settings.CompassShowWP_Name.getValue();
			ret = true;
		}
		if (showIcon != Config.settings.CompassShowWP_Icon.getValue())
		{
			showIcon = Config.settings.CompassShowWP_Icon.getValue();
			ret = true;
		}
		if (showAtt != Config.settings.CompassShowAttributes.getValue())
		{
			showAtt = Config.settings.CompassShowAttributes.getValue();
			ret = true;
		}
		if (showGcCode != Config.settings.CompassShowGcCode.getValue())
		{
			showGcCode = Config.settings.CompassShowGcCode.getValue();
			ret = true;
		}
		if (showCoords != Config.settings.CompassShowCoords.getValue())
		{
			showCoords = Config.settings.CompassShowCoords.getValue();
			ret = true;
		}
		if (showWpDesc != Config.settings.CompassShowWpDesc.getValue())
		{
			showWpDesc = Config.settings.CompassShowWpDesc.getValue();
			ret = true;
		}
		if (showSatInfos != Config.settings.CompassShowSatInfos.getValue())
		{
			showSatInfos = Config.settings.CompassShowSatInfos.getValue();
			ret = true;
		}
		if (showSunMoon != Config.settings.CompassShowSunMoon.getValue())
		{
			showSunMoon = Config.settings.CompassShowSunMoon.getValue();
			ret = true;
		}

		showAnyContent = showSatInfos || showWpDesc || showCoords || showGcCode || showAtt || showIcon || showName;

		return ret;
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	private void setWP(Cache c, Waypoint wp)
	{
		boolean resetControls = false; // sett if WP desc changed

		if (wp != null)
		{
			if (wp.Description != null && !wp.Description.equals(""))
			{
				float newDescHeight = Fonts.MeasureWrapped(wp.Description, topContentBox.getWidth()).height + margin;
				if (newDescHeight != descHeight) resetControls = true;
			}
		}

		aktWaypoint = wp;

		if (resetControls)
		{
			createControls();
		}
		else
		{
			setCache(c);
		}

	}

	private void setCache(Cache c)
	{

		aktCache = c;
		if (c == null) return;
		if (showAtt)
		{
			for (int i = 0; i < 19; i++)
			{
				if (i < c.getAttributes().size())
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
				else
				{
					att[i].setDrawable(null);
				}
			}
		}

		if (showIcon && Icon != null)
		{
			if (aktWaypoint == null)
			{
				if (c.CorrectedCoordiantesOrMysterySolved())
				{
					Icon.setDrawable(new SpriteDrawable(SpriteCache.BigIcons.get(21)));
				}
				else
				{
					Icon.setDrawable(new SpriteDrawable(SpriteCache.BigIcons.get(c.Type.ordinal())));
				}
			}
			else
			{
				Icon.setDrawable(new SpriteDrawable(SpriteCache.BigIcons.get(aktWaypoint.Type.ordinal())));
			}
		}

		if (showName && lbl_Name != null)
		{
			if (aktWaypoint == null)
			{
				lbl_Name.setText(c.Name);
			}
			else
			{
				lbl_Name.setText(aktWaypoint.Title);
			}
		}

		if (showGcCode && lblGcCode != null)
		{
			lblGcCode.setText(c.GcCode);
		}

		if (showCoords && lblCoords != null)
		{
			if (aktWaypoint == null)
			{
				lblCoords.setText(c.Pos.FormatCoordinate());
			}
			else
			{
				lblCoords.setText(aktWaypoint.Pos.FormatCoordinate());
			}
		}

		if (showWpDesc && lblDesc != null)
		{
			if (aktWaypoint != null && !aktWaypoint.Description.equals(""))
			{
				lblDesc.setWrappedText(aktWaypoint.Description);
			}
			else
			{
				lblDesc.setText("");
			}

		}

		Layout();
	}

	private void Layout()
	{

		// Die größe des Kompasses nach rest Platz berechnen

		float compassHeight = 0;

		compassHeight = Math.min(leftBox.getHeight(), this.width) - margin - margin;

		if (!showMap)
		{
			lblDistance.setY(margin);
			compassHeight *= 0.95f;
		}

		if (showSunMoon) compassHeight -= Sun.getHeight();

		SizeF s = new SizeF(compassHeight, compassHeight);

		frame.setSize(s);
		scale.setSize(s);

		// calc center

		float left = leftBox.getHalfWidth() - s.halfWidth;

		frame.setX(left);
		scale.setX(left);

		float yPos = showSunMoon ? margin + Sun.getHalfHeight() : margin;

		frame.setY(yPos);
		scale.setY(yPos);
		arrow.setY(yPos);

		arrow.setSize(frame);
		arrow.setX(frame.getX());
		arrow.setRec(arrow.ScaleCenter(0.8f));
		arrow.setScale(0.7f);

		scale.setOriginCenter();
		arrow.setOriginCenter();

		if (showSunMoon) setMoonSunPos();
		if (showSatInfos && showCoords && !showGcCode)
		{
			chart.setHeight((lblHeight + margin) * 2.3f + (lblHeight + margin));
		}

		if (showAnyContent)
		{
			topBox.setVisible();
		}
		else
		{
			topBox.setInvisible();
		}
	}

	private void createControls()
	{
		this.removeChilds();

		if (distanceBack != null)
		{
			distanceBack.removeChilds();
			distanceBack.dispose();
		}

		if (topContentBox != null)
		{
			topContentBox.removeChilds();
			topContentBox.dispose();
		}

		// Calc content height
		float contentHeight = margin + margin;
		if (showName || showIcon) contentHeight += (showIcon ? attHeight : lblHeight) + margin;
		if (showAtt) contentHeight += attHeight + margin + attHeight + margin;// two Att Lines
		if (showGcCode || showCoords) contentHeight += lblHeight + margin;
		if (showWpDesc)
		{
			if (aktWaypoint != null)
			{
				if (aktWaypoint.Description != null && !aktWaypoint.Description.equals(""))
				{
					descHeight = Fonts.MeasureWrapped(aktWaypoint.Description, topContentBox.getWidth()).height + margin;
					contentHeight += descHeight + margin;
				}
			}
		}
		if (showSatInfos) contentHeight += attHeight + attHeight + margin;

		float topH = Math.max((this.width * 0.7f), this.height - contentHeight - SpriteCache.activityBackground.getTopHeight()
				- SpriteCache.activityBackground.getBottomHeight());

		if (showMap)
		{
			topH = this.halfWidth;
		}

		topBox = new ScrollBox(new CB_RectF(0, topH, this.width, this.height - topH), topH, "top");

		topBox.setBackground(SpriteCache.activityBackground);

		topContentBox = new Box(topBox, "topContent");
		topContentBox.setWidth(topBox.getAvailableWidth());

		attHeight = (this.width / 9) - margin;
		CB_RectF attRec = new CB_RectF(0, 0, attHeight, attHeight);

		lblHeight = Fonts.Measure("Tg").height * 1.3f;

		topContentBox.setHeight(contentHeight);
		topContentBox.setZeroPos();

		leftBox = new Box(new CB_RectF(0, 0, showMap ? this.halfWidth : this.width, this.height - topBox.getHeight()), "left");
		leftBox.setBackground(SpriteCache.activityBackground);

		if (showMap)
		{
			rightBox = new Box(new CB_RectF(this.halfWidth, 0, this.halfWidth, this.halfWidth), "right");
			rightBoxMask = new Box(new CB_RectF(this.halfWidth, 0, this.halfWidth, this.halfWidth), "rightMask");
			rightBox.setBackground(SpriteCache.activityBackground);
			rightBoxMask.setBackground(SpriteCache.activityBorderMask);
			this.addChild(rightBox);
			this.addChild(rightBoxMask);

			map = new MapView(rightBox, true, "CompassMap");
			map.setZeroPos();
			rightBox.addChild(map);

			lblDistance = new Label(margin, margin, rightBox.getWidth(), (Fonts.MeasureBig("T").height * 2.5f), "distanceLabel");
			BitmapFont font = Fonts.getCompass();
			lblDistance.setFont(font);
			lblDistance.setHAlignment(HAlignment.CENTER);

			distanceBack = new Box(lblDistance, "DistanceBack");
			distanceBack.setBackground(SpriteCache.InfoBack);
			rightBox.addChild(distanceBack);
			rightBox.addChild(lblDistance);
		}
		else
		{
			float h = Fonts.MeasureBig("T").height * 2.5f;
			lblDistance = new Label(margin, leftBox.getHeight() - margin - h, leftBox.getWidth() - margin - margin, h, "distanceLabel");
			BitmapFont font = Fonts.getCompass();
			lblDistance.setFont(font);
			lblDistance.setHAlignment(HAlignment.LEFT);
			distanceBack = new Box(lblDistance, "DistanceBack");
			distanceBack.setBackground(SpriteCache.InfoBack);
			leftBox.addChild(distanceBack);
			lblDistance.setZeroPos();
			lblDistance.setX(margin);
			lblDistance.setVAlignment(VAlignment.BOTTOM);

			lblAccuracy = new Label(lblDistance, "AccuracyLabel");
			lblAccuracy.setHAlignment(HAlignment.RIGHT);
			lblAccuracy.setZeroPos();
			lblAccuracy.setVAlignment(VAlignment.CENTER);

			distanceBack.addChild(lblDistance);
			distanceBack.addChild(lblAccuracy);
		}

		this.addChild(topBox);
		this.addChild(leftBox);

		margin = GL_UISizes.margin;

		topContentBox.setMargins(margin, margin);
		topContentBox.initRow(true);

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

		if (showSunMoon)
		{

			CB_RectF rec = showMap ? attRec.ScaleCenter(0.7f) : attRec.copy();

			Sun = new Image(rec, "sun");
			Sun.setDrawable(SpriteCache.Compass.get(5));
			Sun.setInvisible();
			this.addChild(Sun);

			Moon = new Image(rec, "moon");
			Moon.setDrawable(SpriteCache.Compass.get(6));
			Moon.setInvisible();
			this.addChild(Moon);
		}

		// add WP Name and Icon Line
		if (showIcon || showName)
		{
			if (showIcon)
			{
				Icon = new Image(attRec, "");
				Icon.setWeight(-1);
				if (showName)
				{
					topContentBox.addNext(Icon);
				}
				else
				{
					topContentBox.addLast(Icon);
				}
			}
			if (showName)
			{
				lbl_Name = new Label("NameLabel");
				lbl_Name.setHeight(lblHeight);
				topContentBox.addLast(lbl_Name);
			}
		}

		if (showWpDesc)
		{
			lblDesc = new Label("DescLabel");
			lblDesc.setHeight(descHeight);
			topContentBox.addLast(lblDesc);
		}

		float mesuredCoorWidth = Fonts.Measure("52° 27.130N / 13° 33.117E").width + margin;

		if (showGcCode || showCoords)
		{
			if (showCoords)
			{
				if (showGcCode)
				{
					lblCoords = new Label("CoordsLabel");
					lblCoords.setHeight(lblHeight);
					lblCoords.setWeight(-1);
					lblCoords.setWidth(mesuredCoorWidth);
					topContentBox.addNext(lblCoords);
				}
				else
				{
					lblCoords = new Label("CoordsLabel");
					lblCoords.setHeight(lblHeight);
					lblCoords.setWeight(-1);
					lblCoords.setWidth(mesuredCoorWidth);
					topContentBox.addLast(lblCoords);
				}
			}
			if (showGcCode)
			{
				lblGcCode = new Label("GcCodeLabel");
				lblGcCode.setHeight(lblHeight);
				lblGcCode.setWeight(1);
				topContentBox.addLast(lblGcCode);
			}
		}

		if (showSatInfos)
		{
			if (showMap)//
			{
				lblAlt = new Label("AltLabel");
				lblAlt.setHeight(lblHeight);
				lblAlt.setWeight(0.9f);
				topContentBox.addNext(lblAlt);

				lblAccuracy = new Label("AccuracyLabel");
				lblAccuracy.setWeight(0.7f);
				lblAccuracy.setHeight(lblHeight);
				topContentBox.addNext(lblAccuracy);

			}
			else
			{
				lblAlt = new Label("AltLabel");
				lblAlt.setHeight(lblHeight);
				topContentBox.addNext(lblAlt);
			}

			chart = new SatBarChart(attRec, "");
			chart.setHeight((lblHeight + margin) * 2.3f);

			float chartWidth = topContentBox.getAvailableWidth() - mesuredCoorWidth - margin;
			chart.setWeight(-1);
			chart.setWidth(chartWidth);
			topContentBox.addLast(chart);

			lblOwnCoords = new Label("OwnCoords");
			lblOwnCoords.setHeight(lblHeight);
			lblOwnCoords.setWidth(chart.getX() - margin);
			lblOwnCoords.setPos(0, lblAlt.getMaxY() + margin);
			topContentBox.addChild(lblOwnCoords);

		}

		// add Attribute
		if (showAtt)
		{
			att = new Image[20];

			int attLineBreak = (int) (topContentBox.getAvailableWidth() / (attHeight + margin)) - 2;
			for (int i = 0; i < 20; i++)
			{
				att[i] = new Image(attRec, "");
				att[i].setWeight(-1);
				if ((i < attLineBreak - 1) || (i > attLineBreak && i < 19))
				{
					topContentBox.addNext(att[i]);
				}
				if (i == attLineBreak || i == 19)
				{
					topContentBox.addLast(att[i]);
				}
			}
		}

		topBox.addChild(topContentBox);
		topBox.setInerHeight(contentHeight);
		topBox.scrollTo(0);
		setCache(GlobalCore.getSelectedCache());
		// setWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		createControls();
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

		if (GlobalCore.Locator != null && GlobalCore.Locator.getLocation() != null)
		{
			Coordinate position = GlobalCore.Locator.getLocation();
			heading = (GlobalCore.Locator != null) ? GlobalCore.Locator.getHeading() : 0;

			if (lblOwnCoords != null) lblOwnCoords.setText(position.FormatCoordinate());

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

			if (showMap)
			{
				distanceBack.setWidth(labelWidth);
				distanceBack.setX(rightBox.getHalfWidth() - distanceBack.getHalfWidth());
			}

			if (lblAccuracy != null)
			{
				lblAccuracy.setText("  +/- " + String.valueOf((int) position.getAccuracy()) + "m  ");
			}

			if (showSatInfos && lblAlt != null && locator != null)
			{
				lblAlt.setText(GlobalCore.Translations.Get("alt") + locator.getAltString());
			}

			if (showSunMoon) setMoonSunPos();

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
			heading = (GlobalCore.Locator != null) ? GlobalCore.Locator.getHeading() : 0;

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

			if (showSunMoon) setMoonSunPos();

			GL.that.renderOnce("Compass-OrientationChanged");
		}
	}

	@Override
	public String getReceiverName()
	{
		return "CompassView";
	}

	private void setMoonSunPos()
	{

		// chk instanzes
		if (Sun == null || Moon == null) return;

		if (GlobalCore.Locator != null && GlobalCore.LastValidPosition != null)
		{
			double julianDate = Astronomy.UtcToJulianDate(new Date());
			float centerX = frame.getCenterPos().x;
			float centerY = frame.getCenterPos().y;
			float radius = frame.getHalfWidth() + Sun.getHalfHeight() + (Sun.getHalfHeight() / 4);
			float iconSize = Sun.getWidth();

			// ##################
			// Set Moon
			// ##################
			Coordinate eclipticMoon = Astronomy.EclipticCoordinatesMoon(julianDate);
			Coordinate equatorialMoon = Astronomy.EclipticToEquatorial(eclipticMoon, julianDate);
			Coordinate azymuthMoon = Astronomy.EquatorialToAzymuth(GlobalCore.LastValidPosition, julianDate, equatorialMoon);

			if (azymuthMoon.getLatitude() >= 0)
			{

				int x = (int) (centerX + (radius - iconSize / 2) * Math.sin((azymuthMoon.getLongitude() - heading) * Math.PI / 180.0));
				int y = (int) (centerY + (radius - iconSize / 2) * Math.cos((azymuthMoon.getLongitude() - heading) * Math.PI / 180.0));
				Moon.setPos(x - iconSize / 2, y - iconSize / 2);
				Moon.setVisible();
			}
			else
			{
				Moon.setInvisible();
			}

			// ##################
			// Set Sun
			// ##################
			Coordinate eclipticSun = Astronomy.EclipticCoordinatesSun(julianDate);
			Coordinate equatorialSun = Astronomy.EclipticToEquatorial(eclipticSun, julianDate);
			Coordinate azymuthSun = Astronomy.EquatorialToAzymuth(GlobalCore.LastValidPosition, julianDate, equatorialSun);

			if (azymuthSun.getLatitude() >= 0)
			{
				int x = (int) (centerX + (radius - iconSize / 2) * Math.sin((azymuthSun.getLongitude() - heading) * Math.PI / 180.0));
				int y = (int) (centerY + (radius - iconSize / 2) * Math.cos((azymuthSun.getLongitude() - heading) * Math.PI / 180.0));
				Sun.setPos(x - iconSize / 2, y - iconSize / 2);
				Sun.setVisible();
			}
			else
			{
				Sun.setInvisible();
			}
		}
	}

}
