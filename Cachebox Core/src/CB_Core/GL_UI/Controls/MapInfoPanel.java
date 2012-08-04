package CB_Core.GL_UI.Controls;

import CB_Core.GlobalCore;
import CB_Core.UnitFormatter;
import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.math.Vector2;

public class MapInfoPanel extends CB_View_Base
{

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
				lblLatitude.setText(GlobalCore.FormatLatitudeDM(Coord.Latitude));
				lblLongitude.setText(GlobalCore.FormatLongitudeDM(Coord.Longitude));
				GL_Listener.glListener.renderOnce(this.getName() + " setCoord");
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
		GL_Listener.glListener.renderOnce(this.getName() + " setDistance");
	}

	private float aktBearing = 0;

	public void setBearing(float Bearing)
	{
		if (aktBearing == Bearing) return;
		aktBearing = Bearing;
		if (arrow != null)
		{
			arrow.setRotate(-Bearing);
			GL_Listener.glListener.renderOnce(this.getName() + " setBearing");
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

		CB_RectF arrowRec = new CB_RectF(0, 0, this.height, this.height);

		arrow = new Image(arrowRec, "Test_Image");
		arrow.setSprite(SpriteCache.Arrows.get(0));
		arrow.setOrigin(arrowRec.getWidth() / 2, arrowRec.getHeight() / 2);
		arrow.setScale(0.65f);
		this.addChild(arrow);

		float margin = GL_UISizes.margin;

		lblSpeed = new Label(this.ScaleCenter(0.4f), "lblSpeed");
		lblSpeed.setFont(Fonts.getSmall());
		lblSpeed.setPos(new Vector2(arrowRec.getWidth() + margin, this.height * 0.1f));
		lblSpeed.setText("---");
		this.addChild(lblSpeed);

		lblDistance = new Label(this.ScaleCenter(0.4f), "lblDistance");
		lblDistance.setFont(Fonts.getBig());
		lblDistance.setPos(new Vector2(arrowRec.getWidth() + margin, arrowRec.getWidth() / 2));
		lblDistance.setText("---");
		this.addChild(lblDistance);

		lblLatitude = new Label(this.ScaleCenter(0.4f), "lblLatitude");
		lblLatitude.setFont(Fonts.getSmall());
		lblLatitude.setPos(new Vector2(this.getWidth() - lblLatitude.getWidth(), arrowRec.getWidth() / 2));
		lblLatitude.setText("---");
		this.addChild(lblLatitude);

		lblLongitude = new Label(this.ScaleCenter(0.4f), "lblLongitude");
		lblLongitude.setFont(Fonts.getSmall());
		lblLongitude.setPos(new Vector2(this.getWidth() - lblLongitude.getWidth(), this.height * 0.1f));
		lblLongitude.setText("---");
		this.addChild(lblLongitude);
	}

	@Override
	protected void SkinIsChanged()
	{
		Initial();
	}

}
