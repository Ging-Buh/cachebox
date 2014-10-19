package CB_UI.GL_UI.Views;

import java.text.ParseException;
import java.util.Date;

import CB_Core.Events.CachListChangedEventList;
import CB_Core.Events.CacheListChangedEventListner;
import CB_Core.Types.Cache;
import CB_Core.Types.Waypoint;
import CB_Locator.Coordinate;
import CB_Locator.CoordinateGPS;
import CB_Locator.Locator;
import CB_Locator.LocatorSettings;
import CB_Locator.Events.PositionChangedEvent;
import CB_Locator.Events.PositionChangedEventList;
import CB_Locator.Map.MapViewBase;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GlobalCore;
import CB_UI.Events.SelectedCacheEvent;
import CB_UI.Events.SelectedCacheEventList;
import CB_UI.GL_UI.Controls.CacheInfo;
import CB_UI.GL_UI.Controls.SatBarChart;
import CB_UI.GL_UI.Views.MapView.MapMode;
import CB_UI.Util.Astronomy;
import CB_UI_Base.Events.invalidateTextureEvent;
import CB_UI_Base.Events.invalidateTextureEventList;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Image;
import CB_UI_Base.GL_UI.Controls.Label;
import CB_UI_Base.GL_UI.Controls.Label.VAlignment;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.GL_UISizes;
import CB_UI_Base.Math.SizeF;
import CB_Utils.MathUtils;
import CB_Utils.MathUtils.CalculationType;
import CB_Utils.Util.UnitFormatter;
import CB_Utils.Util.iChanged;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class CompassView extends CB_View_Base implements SelectedCacheEvent, PositionChangedEvent, invalidateTextureEvent, CacheListChangedEventListner
{
	private CB_RectF imageRec;
	private Image frame, scale, arrow, att[], Icon, Sun, Moon;

	private Box topContentBox, leftBox, rightBox, rightBoxMask, distanceBack;
	private ScrollBox topBox;
	private MapViewBase map;
	private SatBarChart chart;
	private Label lblDistance, lbl_Name, lblGcCode, lblCoords, lblDesc, lblAlt, lblAccuracy, lblOwnCoords, lblBearing;
	private CacheInfo SDT;
	private Cache aktCache;
	private Waypoint aktWaypoint;

	private float margin, attHeight, descHeight, lblHeight;
	private double heading;
	private boolean isInitial, showMap, showName, showIcon, showAtt, showGcCode, showCoords, showWpDesc, showSatInfos, showSunMoon, showAnyContent, showTargetDirection, showSDT, showLastFound;

	public CompassView(CB_RectF rec, String Name)
	{
		super(rec, Name);
		margin = GL_UISizes.margin;

		CachListChangedEventList.Add(this);
		invalidateTextureEventList.Add(this);
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
		SelectedCacheEventList.Add(this);
		PositionChangedEventList.Add(this);
		if (map != null) map.onShow();

		PositionChanged();
	}

	@Override
	public void onHide()
	{
		super.onHide();
		if (chart != null) chart.onHide();
		SelectedCacheEventList.Remove(this);
		PositionChangedEventList.Remove(this);
		if (map != null) map.onHide();
	}

	@Override
	protected void Initial()
	{
		if (isInitial) return;
		readSettings();
		registerSetingsChangedListners();
		createControls();
		Layout();
		isInitial = true;
	}

	private void registerSetingsChangedListners()
	{
		Config.CompassShowMap.addChangedEventListner(SettingChangedListner);
		Config.CompassShowWP_Name.addChangedEventListner(SettingChangedListner);
		Config.CompassShowWP_Icon.addChangedEventListner(SettingChangedListner);
		Config.CompassShowAttributes.addChangedEventListner(SettingChangedListner);
		Config.CompassShowGcCode.addChangedEventListner(SettingChangedListner);
		Config.CompassShowCoords.addChangedEventListner(SettingChangedListner);
		Config.CompassShowWpDesc.addChangedEventListner(SettingChangedListner);
		Config.CompassShowSatInfos.addChangedEventListner(SettingChangedListner);
		Config.CompassShowSunMoon.addChangedEventListner(SettingChangedListner);
		Config.CompassShowTargetDirection.addChangedEventListner(SettingChangedListner);
		Config.CompassShowSDT.addChangedEventListner(SettingChangedListner);
		Config.CompassShowLastFound.addChangedEventListner(SettingChangedListner);
	}

	iChanged SettingChangedListner = new iChanged()
	{
		@Override
		public void isChanged()
		{
			readSettings();
			createControls();
			Layout();
		}
	};

	private void readSettings()
	{
		showMap = Config.CompassShowMap.getValue();
		showName = Config.CompassShowWP_Name.getValue();
		showIcon = Config.CompassShowWP_Icon.getValue();
		showAtt = Config.CompassShowAttributes.getValue();
		showGcCode = Config.CompassShowGcCode.getValue();
		showCoords = Config.CompassShowCoords.getValue();
		showWpDesc = Config.CompassShowWpDesc.getValue();
		showSatInfos = Config.CompassShowSatInfos.getValue();
		showSunMoon = Config.CompassShowSunMoon.getValue();
		showTargetDirection = Config.CompassShowTargetDirection.getValue();
		showSDT = Config.CompassShowSDT.getValue();
		showLastFound = Config.CompassShowLastFound.getValue();

		showAnyContent = showMap || showName || showIcon || showAtt || showGcCode || showCoords || showWpDesc || showSatInfos || showSunMoon || showTargetDirection || showSDT || showLastFound;
	}

	@Override
	protected void SkinIsChanged()
	{

	}

	private void setWP(Cache cache, Waypoint waypoint)
	{
		boolean resetControls = false; // Set if WP desk changed

		Waypoint wp = null;

		wp = waypoint;

		if (wp != null)
		{
			if (wp.getDescription() != null && !wp.getDescription().equals(""))
			{
				float newDescHeight = Fonts.MeasureWrapped(wp.getDescription(), topContentBox.getWidth()).height + margin;
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
			setCache(cache);
		}

	}

	private void setCache(Cache cache)
	{

		try
		{
			aktCache = cache;
			if (aktCache == null) return;
			synchronized (aktCache)
			{

				if (aktCache.isDetailLoaded())
				{
					aktCache.loadDetail();
				}

				if (showAtt)
				{
					try
					{
						int attributesSize = aktCache.getAttributes().size();
						for (int i = 0; i < 19; i++)
						{
							if (i < attributesSize)
							{
								try
								{
									String ImageName = aktCache.getAttributes().get(i).getImageName() + "Icon";
									ImageName = ImageName.replace("_", "-");
									att[i].setDrawable(new SpriteDrawable(SpriteCacheBase.getThemedSprite(ImageName)));
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
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (showIcon && Icon != null)
				{
					if (aktWaypoint == null)
					{
						if (aktCache.CorrectedCoordiantesOrMysterySolved())
						{
							Icon.setDrawable(new SpriteDrawable(SpriteCacheBase.BigIcons.get(21)));
						}
						else
						{
							Icon.setDrawable(new SpriteDrawable(SpriteCacheBase.BigIcons.get(aktCache.Type.ordinal())));
						}
					}
					else
					{
						Icon.setDrawable(new SpriteDrawable(SpriteCacheBase.BigIcons.get(aktWaypoint.Type.ordinal())));
					}
				}

				if (showName && lbl_Name != null)
				{
					if (aktWaypoint == null)
					{
						lbl_Name.setText(aktCache.getName());
					}
					else
					{
						lbl_Name.setText(aktWaypoint.getTitle());
					}
				}

				if (showGcCode && lblGcCode != null)
				{
					lblGcCode.setText(aktCache.getGcCode());
				}

				if (showCoords && lblCoords != null)
				{
					if (aktWaypoint == null)
					{
						lblCoords.setText(aktCache.Pos.FormatCoordinate());
					}
					else
					{
						lblCoords.setText(aktWaypoint.Pos.FormatCoordinate());
					}
				}

				if (showWpDesc && lblDesc != null)
				{
					if (aktWaypoint != null && !aktWaypoint.getDescription().equals(""))
					{
						lblDesc.setWrappedText(aktWaypoint.getDescription());
					}
					else
					{
						lblDesc.setText("");
					}

				}

				if (showSDT & SDT != null)
				{
					SDT.setCache(aktCache);
				}

			}
			Layout();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void Layout()
	{

		// Die gr��e des Kompasses nach rest Platz berechnen

		float compassHeight = 0;

		compassHeight = Math.min(leftBox.getHeight(), this.getWidth()) - margin - margin;

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

		if (showSunMoon) try
		{
			setMoonSunPos();
		}
		catch (ParseException e)
		{
		}
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
				if (aktWaypoint.getDescription() != null && !aktWaypoint.getDescription().equals(""))
				{
					descHeight = Fonts.MeasureWrapped(aktWaypoint.getDescription(), this.getWidth()).height + margin;
					contentHeight += descHeight + margin;
				}
			}
		}
		if (showSatInfos) contentHeight += attHeight + attHeight + margin;
		if (showTargetDirection) contentHeight += lblHeight + margin;
		if (showSDT) contentHeight += Fonts.MeasureSmall("Tg").height * 1.3f;
		if (showLastFound) contentHeight += Fonts.MeasureSmall("Tg").height * 1.3f;

		float topH = Math.max((this.getWidth() * 0.7f), this.getHeight() - contentHeight - SpriteCacheBase.activityBackground.getTopHeight() - SpriteCacheBase.activityBackground.getBottomHeight());

		if (showMap)
		{
			topH = this.getHalfWidth();
		}

		topBox = new ScrollBox(new CB_RectF(0, topH, this.getWidth(), this.getHeight() - topH));
		topBox.setVirtualHeight(topH);

		topBox.setBackground(SpriteCacheBase.activityBackground);

		topContentBox = new Box(topBox, "topContent");
		topContentBox.setWidth(topBox.getInnerWidth());

		attHeight = (this.getWidth() / 9) - margin;
		CB_RectF attRec = new CB_RectF(0, 0, attHeight, attHeight);

		lblHeight = Fonts.Measure("Tg").height * 1.3f;

		topContentBox.setHeight(contentHeight);
		topContentBox.setZeroPos();

		leftBox = new Box(new CB_RectF(0, 0, showMap ? this.getHalfWidth() : this.getWidth(), this.getHeight() - topBox.getHeight()), "left");
		leftBox.setBackground(SpriteCacheBase.activityBackground);

		if (showMap)
		{
			rightBox = new Box(new CB_RectF(this.getHalfWidth(), 0, this.getHalfWidth(), this.getHalfWidth()), "right");
			rightBoxMask = new Box(new CB_RectF(this.getHalfWidth(), 0, this.getHalfWidth(), this.getHalfWidth()), "rightMask");
			rightBox.setBackground(SpriteCacheBase.activityBackground);
			rightBoxMask.setBackground(SpriteCacheBase.activityBorderMask);
			this.addChild(rightBox);
			this.addChild(rightBoxMask);

			if (map == null) map = new MapView(rightBox, MapMode.Compass, "CompassMap");
			map.setZeroPos();
			rightBox.addChild(map);

			lblDistance = new Label(margin, margin, rightBox.getWidth(), (Fonts.MeasureBig("T").height * 2.5f), "distanceLabel");
			BitmapFont font = Fonts.getCompass();
			lblDistance.setFont(font);
			lblDistance.setHAlignment(HAlignment.CENTER);

			distanceBack = new Box(lblDistance, "DistanceBack");
			distanceBack.setBackground(SpriteCacheBase.InfoBack);
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
			distanceBack.setBackground(SpriteCacheBase.InfoBack);
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
		topContentBox.initRow();

		imageRec = (new CB_RectF(0, 0, getWidth(), getWidth())).ScaleCenter(0.6f);
		this.setBackground(SpriteCacheBase.ListBack);

		frame = new Image(imageRec, "frame");
		frame.setDrawable(SpriteCacheBase.Compass.get(0));
		this.addChild(frame);

		scale = new Image(imageRec, "scale");
		scale.setDrawable(SpriteCacheBase.Compass.get(1));
		this.addChild(scale);

		arrow = new Image(imageRec, "arrow");
		setArrowDrawable(true);
		this.addChild(arrow);

		if (showSunMoon)
		{

			CB_RectF rec = showMap ? attRec.ScaleCenter(0.7f) : attRec.copy();

			Sun = new Image(rec, "sun");
			Sun.setDrawable(SpriteCacheBase.Compass.get(5));
			Sun.setInvisible();
			this.addChild(Sun);

			Moon = new Image(rec, "moon");
			Moon.setDrawable(SpriteCacheBase.Compass.get(6));
			Moon.setInvisible();
			this.addChild(Moon);
		}

		// add WP Name and Icon Line
		if (showIcon || showName)
		{
			if (showIcon)
			{
				Icon = new Image(attRec, "Compass-CacheIcon");
				if (showName)
				{
					topContentBox.addNext(Icon, CB_View_Base.FIXED);
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

		// add WP description line
		if (showWpDesc)
		{
			lblDesc = new Label("DescLabel");
			lblDesc.setHeight(descHeight);
			topContentBox.addLast(lblDesc);
		}

		// add GC-Code and Coord line
		float mesuredCoorWidth = Fonts.Measure("52� 27.130N / 13� 33.117E").width + margin;
		if (showGcCode || showCoords)
		{
			if (showCoords)
			{
				if (showGcCode)
				{
					lblCoords = new Label("CoordsLabel");
					lblCoords.setHeight(lblHeight);
					lblCoords.setWidth(mesuredCoorWidth);
					topContentBox.addNext(lblCoords, CB_View_Base.FIXED);
				}
				else
				{
					lblCoords = new Label("CoordsLabel");
					lblCoords.setHeight(lblHeight);
					lblCoords.setWidth(mesuredCoorWidth);
					topContentBox.addLast(lblCoords, CB_View_Base.FIXED);
				}
			}
			if (showGcCode)
			{
				lblGcCode = new Label("GcCodeLabel");
				lblGcCode.setHeight(lblHeight);
				topContentBox.addLast(lblGcCode);
			}
		}

		// add sat infos
		if (showSatInfos)
		{
			if (showMap)//
			{
				lblAlt = new Label("AltLabel");
				lblAlt.setHeight(lblHeight);
				topContentBox.addNext(lblAlt, 0.9f);

				lblAccuracy = new Label("AccuracyLabel");
				lblAccuracy.setHeight(lblHeight);
				topContentBox.addNext(lblAccuracy, 0.7f);

			}
			else
			{
				lblAlt = new Label("AltLabel");
				lblAlt.setHeight(lblHeight);
				topContentBox.addNext(lblAlt);
			}

			chart = new SatBarChart(attRec, "");
			chart.setHeight((lblHeight + margin) * 2.3f);

			float chartWidth = topContentBox.getInnerWidth() - mesuredCoorWidth - margin;
			chart.setWidth(chartWidth);
			topContentBox.addLast(chart, CB_View_Base.FIXED);

			lblOwnCoords = new Label("OwnCoords");
			lblOwnCoords.setHeight(lblHeight);
			lblOwnCoords.setWidth(chart.getX() - margin);
			lblOwnCoords.setPos(0, lblAlt.getMaxY() + margin);
			topContentBox.addChild(lblOwnCoords);

		}

		// add Target direction
		if (showTargetDirection)
		{
			lblBearing = new Label("AltLabel");
			lblBearing.setHeight(lblHeight);

			topContentBox.addLast(lblBearing);
		}

		// add SDT line or LastFound
		if (showSDT || showLastFound)
		{

			int ViewMode = 0;
			if (showSDT) ViewMode += CacheInfo.SHOW_S_D_T;
			if (showLastFound) ViewMode += CacheInfo.SHOW_LAST_FOUND;

			float infoHeight = Fonts.MeasureSmall("Tg").height * 1.3f;
			if (showSDT && showLastFound)
			{
				infoHeight *= 2.5f;
			}

			SDT = new CacheInfo(new SizeF(100, infoHeight), "SDT info", aktCache);
			SDT.setViewMode(ViewMode);
			topContentBox.addLast(SDT);
		}

		// add Attribute
		if (showAtt)
		{
			att = new Image[20];

			int attLineBreak = (int) (topContentBox.getInnerWidth() / (attHeight + margin)) - 2;
			for (int i = 0; i < 20; i++)
			{
				att[i] = new Image(attRec, "");
				if ((i < attLineBreak - 1) || (i > attLineBreak && i < 19))
				{
					topContentBox.addNext(att[i], CB_View_Base.FIXED);
				}
				if (i == attLineBreak || i == 19)
				{
					topContentBox.addLast(att[i], CB_View_Base.FIXED);
				}
			}
		}

		topBox.addChild(topContentBox);
		topBox.setVirtualHeight(contentHeight);
		topBox.scrollTo(0);
		setCache(GlobalCore.getSelectedCache());
		// setWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
	}

	private boolean lastUsedCompass = Locator.UseMagneticCompass();

	private void setArrowDrawable()
	{
		setArrowDrawable(false);
	}

	private void setArrowDrawable(boolean forceSet)
	{
		boolean tmp = Locator.UseMagneticCompass();
		if (!forceSet && tmp == lastUsedCompass) return;// no change required
		lastUsedCompass = tmp;
		boolean Transparency = LocatorSettings.PositionMarkerTransparent.getValue();
		int arrowId = 0;
		if (lastUsedCompass)
		{
			arrowId = Transparency ? 1 : 0;
		}
		else
		{
			arrowId = Transparency ? 3 : 2;
		}
		Sprite arrowSprite = new Sprite(SpriteCacheBase.Arrows.get(arrowId));
		arrowSprite.setRotation(0);// reset rotation
		arrowSprite.setOrigin(0, 0);
		arrow.setDrawable(new SpriteDrawable(arrowSprite));
	}

	@Override
	public void onResized(CB_RectF rec)
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
	public void PositionChanged()
	{
		if (aktCache == null) return;

		CoordinateGPS position = Locator.getCoordinate();

		if (position == null) return;

		heading = Locator.getHeading();

		if (lblOwnCoords != null) lblOwnCoords.setText(position.FormatCoordinate());

		Coordinate dest = aktWaypoint != null ? aktWaypoint.Pos : aktCache.Pos;

		float result[] = new float[4];

		try
		{
			MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, position.getLatitude(), position.getLongitude(), dest.getLatitude(), dest.getLongitude(), result);
		}
		catch (Exception e1)
		{
			return;
		}

		float distance = result[0];
		float bearing = result[1];

		if (lblBearing != null)
		{
			double directionToTarget = 0;
			if (bearing < 0) directionToTarget = 360 + bearing;
			else
				directionToTarget = bearing;

			String sBearing = Translation.Get("directionToTarget") + " : " + String.format("%.0f", directionToTarget) + "�";
			lblBearing.setText(sBearing);
		}

		double relativeBearing = bearing - heading;

		if (arrow != null)
		{
			setArrowDrawable();
			arrow.setRotate((float) -relativeBearing);
		}
		if (scale != null) scale.setRotate((float) heading);
		if (lblDistance != null)
		{
			float labelWidth = lblDistance.setText(UnitFormatter.DistanceString(distance)).getTextWidth() + (6 * margin);
			if (showMap)
			{
				if (distanceBack != null)
				{
					distanceBack.setWidth(labelWidth);
					distanceBack.setX(rightBox.getHalfWidth() - distanceBack.getHalfWidth());
				}
			}

		}

		if (lblAccuracy != null)
		{
			lblAccuracy.setText("  +/- " + UnitFormatter.DistanceString(position.getAccuracy()));
		}

		if (showSatInfos && lblAlt != null)
		{
			lblAlt.setText(Translation.Get("alt") + Locator.getAltString());
		}

		if (showSunMoon)
		{
			if (Moon != null && Sun != null) try
			{
				setMoonSunPos();
			}
			catch (ParseException e)
			{
			}
		}

		GL.that.renderOnce();

	}

	@Override
	public void OrientationChanged()
	{
		if (aktCache == null) return;

		if (Locator.Valid())
		{
			Coordinate position = Locator.getCoordinate();
			heading = Locator.getHeading();

			Coordinate dest = aktWaypoint != null ? aktWaypoint.Pos : aktCache.Pos;

			float result[] = new float[2];

			MathUtils.computeDistanceAndBearing(CalculationType.ACCURATE, position.getLatitude(), position.getLongitude(), dest.getLatitude(), dest.getLongitude(), result);

			float bearing = result[1];

			double relativeBearing = bearing - heading;
			setArrowDrawable();
			arrow.setRotate((float) -relativeBearing);
			scale.setRotate((float) heading);

			if (showSunMoon) try
			{
				setMoonSunPos();
			}
			catch (ParseException e)
			{
			}

			GL.that.renderOnce();
		}
	}

	@Override
	public String getReceiverName()
	{
		return "CompassView";
	}

	private void setMoonSunPos() throws ParseException
	{

		// chk instanzes
		if (Sun == null || Moon == null) return;

		if (Locator.Valid())
		{

			Date now = new Date();
			Date UtcNow = new Date(Astronomy.getUtcTime(now.getTime()));

			double julianDate = Astronomy.UtcToJulianDate(UtcNow);
			float centerX = frame.getCenterPosX();
			float centerY = frame.getCenterPosY();
			float radius = frame.getHalfWidth() + Sun.getHalfHeight() + (Sun.getHalfHeight() / 4);
			float iconSize = Sun.getWidth();

			// ##################
			// Set Moon
			// ##################
			Coordinate eclipticMoon = Astronomy.EclipticCoordinatesMoon(julianDate);
			Coordinate equatorialMoon = Astronomy.EclipticToEquatorial(eclipticMoon, julianDate);
			Coordinate azymuthMoon = Astronomy.EquatorialToAzymuth(Locator.getCoordinate(), julianDate, equatorialMoon);

			if (azymuthMoon.getLatitude() >= 0)
			{

				int x = (int) (centerX + (radius - iconSize / 2) * Math.sin((azymuthMoon.getLongitude() - heading) * MathUtils.DEG_RAD));
				int y = (int) (centerY + (radius - iconSize / 2) * Math.cos((azymuthMoon.getLongitude() - heading) * MathUtils.DEG_RAD));
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
			Coordinate azymuthSun = Astronomy.EquatorialToAzymuth(Locator.getCoordinate(), julianDate, equatorialSun);

			if (azymuthSun.getLatitude() >= 0)
			{
				int x = (int) (centerX + (radius - iconSize / 2) * Math.sin((azymuthSun.getLongitude() - heading) * MathUtils.DEG_RAD));
				int y = (int) (centerY + (radius - iconSize / 2) * Math.cos((azymuthSun.getLongitude() - heading) * MathUtils.DEG_RAD));
				Sun.setPos(x - iconSize / 2, y - iconSize / 2);
				Sun.setVisible();
			}
			else
			{
				Sun.setInvisible();
			}
		}
	}

	@Override
	public Priority getPriority()
	{
		return Priority.High;
	}

	@Override
	public void SpeedChanged()
	{
	}

	@Override
	public void invalidateTexture()
	{
		createControls();
		Layout();
	}

	@Override
	public void CacheListChangedEvent()
	{
		setWP(GlobalCore.getSelectedCache(), GlobalCore.getSelectedWaypoint());
	}

	@Override
	public void dispose()
	{
		// release all Member
		if (imageRec != null) imageRec.dispose();
		imageRec = null;
		if (frame != null) frame.dispose();
		frame = null;
		if (scale != null) scale.dispose();
		scale = null;
		if (arrow != null) arrow.dispose();
		arrow = null;
		if (Icon != null) Icon.dispose();
		Icon = null;
		if (Sun != null) Sun.dispose();
		Sun = null;
		if (Moon != null) Moon.dispose();
		Moon = null;
		if (topContentBox != null) topContentBox.dispose();
		topContentBox = null;
		if (leftBox != null) leftBox.dispose();
		leftBox = null;
		if (rightBox != null) rightBox.dispose();
		rightBox = null;
		if (rightBoxMask != null) rightBoxMask.dispose();
		rightBoxMask = null;
		if (distanceBack != null) distanceBack.dispose();
		distanceBack = null;
		if (topBox != null) topBox.dispose();
		topBox = null;
		if (map != null) map.dispose();
		map = null;
		if (chart != null) chart.dispose();
		chart = null;
		if (lblDistance != null) lblDistance.dispose();
		lblDistance = null;
		if (lbl_Name != null) lbl_Name.dispose();
		lbl_Name = null;
		if (lblGcCode != null) lblGcCode.dispose();
		lblGcCode = null;
		if (lblCoords != null) lblCoords.dispose();
		lblCoords = null;
		if (lblDesc != null) lblDesc.dispose();
		lblDesc = null;
		if (lblAlt != null) lblAlt.dispose();
		lblAlt = null;
		if (lblAccuracy != null) lblAccuracy.dispose();
		lblAccuracy = null;
		if (lblOwnCoords != null) lblOwnCoords.dispose();
		lblOwnCoords = null;
		if (lblBearing != null) lblBearing.dispose();
		lblBearing = null;
		if (SDT != null) SDT.dispose();
		SDT = null;

		aktCache = null;
		aktWaypoint = null;

		if (att != null)
		{
			for (Image img : att)
			{
				if (img != null) img.dispose();
				img = null;
			}
			att = null;
		}

		// release all EventHandler
		Config.CompassShowMap.removeChangedEventListner(SettingChangedListner);
		Config.CompassShowWP_Name.removeChangedEventListner(SettingChangedListner);
		Config.CompassShowWP_Icon.removeChangedEventListner(SettingChangedListner);
		Config.CompassShowAttributes.removeChangedEventListner(SettingChangedListner);
		Config.CompassShowGcCode.removeChangedEventListner(SettingChangedListner);
		Config.CompassShowCoords.removeChangedEventListner(SettingChangedListner);
		Config.CompassShowWpDesc.removeChangedEventListner(SettingChangedListner);
		Config.CompassShowSatInfos.removeChangedEventListner(SettingChangedListner);
		Config.CompassShowSunMoon.removeChangedEventListner(SettingChangedListner);
		Config.CompassShowTargetDirection.removeChangedEventListner(SettingChangedListner);
		Config.CompassShowSDT.removeChangedEventListner(SettingChangedListner);
		Config.CompassShowLastFound.removeChangedEventListner(SettingChangedListner);

		SettingChangedListner = null;
		SelectedCacheEventList.Remove(this);
		CachListChangedEventList.Remove(this);
		PositionChangedEventList.Remove(this);
		invalidateTextureEventList.Remove(this);

		super.dispose();
	}

}
