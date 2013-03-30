package CB_Core.GL_UI.Controls;

import CB_Core.GlobalCore;
import CB_Core.UnitFormatter;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Locator.Coordinate;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

public class MapInfoPanel extends CB_View_Base
{

	private Image compass_frame;
	private Image compas_scale;
	private Image arrow;
	private Label lblSpeed;
	private Label lblDistance;
	private Label lblLatitude;
	private Label lblLongitude;

	private Coordinate aktCoord;

	public void setCoord(Coordinate Coord)
	{
		if (Coord != null && lblLatitude != null && lblLongitude != null)
		{
			if (aktCoord == null || !aktCoord.equals(Coord))
			{
				aktCoord = Coord;
				lblLatitude.setText(GlobalCore.FormatLatitudeDM(Coord.getLatitude()));
				lblLongitude.setText(GlobalCore.FormatLongitudeDM(Coord.getLongitude()));
				GL.that.renderOnce(this.getName() + " setCoord");
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

	public MapInfoPanel(CB_RectF rec, String Name)
	{
		super(rec, Name);
		registerSkinChangedEvent();
	}

	@Override
	protected void Initial()
	{
		this.removeChilds();

		setBackground(SpriteCache.InfoBack);

		// initial Image

		CB_RectF CompassRec = new CB_RectF(0, 0, this.height, this.height);

		compass_frame = new Image(CompassRec, "Test_Image");
		compass_frame.setDrawable(SpriteCache.Compass.get(2));
		compass_frame.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
		compass_frame.setScale(0.80f);
		this.addChild(compass_frame);

		compas_scale = new Image(CompassRec, "Test_Image");
		compas_scale.setDrawable(SpriteCache.Compass.get(3));
		compas_scale.setOrigin(CompassRec.getWidth() / 2, CompassRec.getHeight() / 2);
		compas_scale.setScale(0.80f);
		this.addChild(compas_scale);

		arrow = new Image(CompassRec, "Test_Image");
		arrow.setDrawable(new SpriteDrawable(SpriteCache.Arrows.get(0)));
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
		lblLatitude.setPos(new Vector2(this.width - lblLatitude.getWidth(), CompassRec.getWidth() / 2));
		lblLatitude.setText("---");
		this.addChild(lblLatitude);

		lblLongitude = new Label(this.ScaleCenter(0.4f), "lblLongitude");
		lblLongitude.setFont(Fonts.getSmall());
		lblLongitude.setPos(new Vector2(this.width - lblLongitude.getWidth(), this.height * 0.1f));
		lblLongitude.setText("---");
		this.addChild(lblLongitude);
	}

	@Override
	protected void SkinIsChanged()
	{
		Initial();
	}

}
