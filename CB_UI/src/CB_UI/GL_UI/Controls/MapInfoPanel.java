package CB_UI.GL_UI.Controls;

import CB_Locator.Coordinate;
import CB_UI.GL_UI.CB_View_Base;
import CB_UI.GL_UI.Fonts;
import CB_UI.GL_UI.SpriteCacheBase;
import CB_UI.GL_UI.GL_Listener.GL;
import CB_UI.Math.CB_RectF;
import CB_UI.Math.GL_UISizes;
import CB_Utils.Util.UnitFormatter;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class MapInfoPanel extends CB_View_Base
{

	private Image compass_frame;
	private Image compas_scale;
	private Image arrow;
	private Image CoordSymbol;
	private Label lblSpeed;
	private Label lblDistance;
	private Label lblLatitude;
	private Label lblLongitude;

	private Coordinate aktCoord;

	private CoordType lastCoordType = CoordType.NULL;

	public enum CoordType
	{
		NULL, GPS, Cache, Map
	}

	public void setCoord(Coordinate Coord)
	{
		if (Coord != null && lblLatitude != null && lblLongitude != null)
		{
			if (aktCoord == null || !aktCoord.equals(Coord))
			{
				aktCoord = Coord;
				lblLatitude.setText(UnitFormatter.FormatLatitudeDM(Coord.getLatitude()));
				lblLongitude.setText(UnitFormatter.FormatLongitudeDM(Coord.getLongitude()));
				GL.that.renderOnce(this.getName() + " setCoord");
			}

		}
	}

	public void setCoordType(CoordType type)
	{
		if (CoordSymbol == null)
		{
			// store type in lastCoordType to be initialized later
			lastCoordType = type;
			return;
		}
		if (lastCoordType != type)
		{
			lastCoordType = type;
			switch (type)
			{
			case Cache:
				CoordSymbol.setDrawable(new SpriteDrawable(SpriteCacheBase.getThemedSprite("cache-icon")));
				break;
			case GPS:
				CoordSymbol.setDrawable(new SpriteDrawable(SpriteCacheBase.getThemedSprite("satellite")));
				break;
			case Map:
				CoordSymbol.setDrawable(new SpriteDrawable(SpriteCacheBase.getThemedSprite("map")));
				break;
			case NULL:
				CoordSymbol.setDrawable(null);
				break;
			}
		}
	}

	public void setSpeed(String speed)
	{

		if (lblSpeed == null) return;
		if (lblSpeed.getText().equals(speed)) return;

		lblSpeed.setText(speed);
	}

	private float aktDistance = -1;

	public void setDistance(float distance)
	{
		if (lblDistance == null) return;
		if (aktDistance == distance) return;
		aktDistance = distance;
		if (distance == -1) lblDistance.setText("?");
		else
			lblDistance.setText(UnitFormatter.DistanceString(distance));
		GL.that.renderOnce(this.getName() + " setDistance");
	}

	private float aktHeading = 0;
	private float aktBearing = 0;

	public void setBearing(float Heading, float Bearing)
	{
		if ((aktHeading == Heading) && (aktBearing == Bearing)) return;
		aktHeading = Heading;
		aktBearing = Bearing;
		if (arrow != null && compas_scale != null)
		{
			arrow.setRotate(-Heading);
			compas_scale.setRotate(Bearing);
			GL.that.renderOnce(this.getName() + " setBearing");
		}
	}

	@Override
	protected void render(SpriteBatch batch)
	{
		super.render(batch);
	}

	public MapInfoPanel(CB_RectF rec, String Name)
	{
		super(rec, Name);
		registerSkinChangedEvent();
	}

	@Override
	protected void Initial()
	{
		this.removeChilds();

		setBackground(SpriteCacheBase.InfoBack);

		// initial Image

		CB_RectF CompassRec = new CB_RectF(0, 0, this.height, this.height);

		compass_frame = new Image(CompassRec, "Test_Image");
		compass_frame.setDrawable(SpriteCacheBase.Compass.get(2));
		compass_frame.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
		compass_frame.setScale(0.80f);
		this.addChild(compass_frame);

		compas_scale = new Image(CompassRec, "Test_Image");
		compas_scale.setDrawable(SpriteCacheBase.Compass.get(3));
		compas_scale.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
		compas_scale.setScale(0.80f);
		this.addChild(compas_scale);

		arrow = new Image(CompassRec, "Test_Image");
		arrow.setDrawable(new SpriteDrawable(SpriteCacheBase.Arrows.get(0)));
		arrow.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
		arrow.setScale(0.50f);
		this.addChild(arrow);

		float margin = GL_UISizes.margin;

		lblSpeed = new Label(this.ScaleCenter(0.4f), "lblSpeed");
		lblSpeed.setFont(Fonts.getSmall());
		lblSpeed.setPos(new Vector2(CompassRec.getWidth() + margin, this.height * 0.1f));
		lblSpeed.setText("---");
		this.addChild(lblSpeed);

		lblDistance = new Label(this.ScaleCenter(0.4f), "lblDistance");
		lblDistance.setFont(Fonts.getBig());
		lblDistance.setPos(new Vector2(CompassRec.getWidth() + margin, CompassRec.getWidth() / 2));
		lblDistance.setText("---");
		this.addChild(lblDistance);

		lblLatitude = new Label(this.ScaleCenter(0.4f), "lblLatitude");
		lblLatitude.setFont(Fonts.getSmall());
		lblLatitude.setPos(new Vector2(this.width - lblLatitude.getWidth() - rightBorder, CompassRec.getWidth() / 2));
		lblLatitude.setText("---");
		this.addChild(lblLatitude);

		lblLongitude = new Label(this.ScaleCenter(0.4f), "lblLongitude");
		lblLongitude.setFont(Fonts.getSmall());
		lblLongitude.setPos(new Vector2(this.width - lblLongitude.getWidth() - rightBorder, this.height * 0.1f));
		lblLongitude.setText("---");
		this.addChild(lblLongitude);

		CoordSymbol = new Image((new CB_RectF(0, 0, this.height, this.height)).ScaleCenter(0.62f), "CoordSymbol");
		CoordSymbol.setX(this.width - CoordSymbol.getWidth() - (rightBorder / 3));
		CoordSymbol.setDrawable(new SpriteDrawable(SpriteCacheBase.getThemedSprite("cache-icon")));
		this.addChild(CoordSymbol);
		CoordType tmp = lastCoordType;
		lastCoordType = CoordType.NULL;
		setCoordType(tmp);
	}

	@Override
	protected void SkinIsChanged()
	{
		Initial();
	}

}
