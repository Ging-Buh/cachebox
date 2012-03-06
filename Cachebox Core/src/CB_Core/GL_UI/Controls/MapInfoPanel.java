package CB_Core.GL_UI.Controls;

import CB_Core.GlobalCore;
import CB_Core.UnitFormatter;
import CB_Core.GL_UI.Fonts;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.GL_UISizes;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class MapInfoPanel extends GL_View_Base
{

	private Image arrow;
	private Label lblSpeed;
	private Label lblDistance;
	private Label lblLatitude;
	private Label lblLongitude;

	public void setCoord(Coordinate Coord)
	{
		if (Coord != null)
		{
			lblLatitude.setText(GlobalCore.FormatLatitudeDM(Coord.Latitude));
			lblLongitude.setText(GlobalCore.FormatLongitudeDM(Coord.Longitude));
			GL_Listener.glListener.renderOnce(this);
		}
	}

	public void setSpeed(String speed)
	{
		lblSpeed.setText(speed);
	}

	public void setDistance(float distance)
	{
		if (distance == -1) lblDistance.setText("?");
		else
			lblDistance.setText(UnitFormatter.DistanceString(distance));
		GL_Listener.glListener.renderOnce(this);
	}

	public void setBearing(float Bearing)
	{
		arrow.setRotate(Bearing);
		GL_Listener.glListener.renderOnce(this);
	}

	public MapInfoPanel(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
		setBackground(new NinePatch(SpriteCache.InfoBack, 16, 16, 16, 16));

		// initial Image

		CB_RectF arrowRec = new CB_RectF(0, 0, this.height, this.height);

		arrow = new Image(arrowRec, "Test_Image");
		arrow.setSprite(SpriteCache.MapArrows.get(0));
		arrow.setOrigin(arrowRec.getWidth() / 2, arrowRec.getHeight() / 2);
		arrow.setScale(0.65f);
		this.addChild(arrow);

		float margin = GL_UISizes.margin;

		lblSpeed = new Label(this.ScaleCenter(0.4f), "lblSpeed");
		lblSpeed.setFont(Fonts.get16());
		lblSpeed.setPos(new Vector2(arrowRec.getWidth() + margin, this.height * 0.1f));
		lblSpeed.setText("---");
		this.addChild(lblSpeed);

		lblDistance = new Label(this.ScaleCenter(0.4f), "lblDistance");
		lblDistance.setFont(Fonts.get20());
		lblDistance.setPos(new Vector2(arrowRec.getWidth() + margin, arrowRec.getWidth() / 2));
		lblDistance.setText("---");
		this.addChild(lblDistance);

		lblLatitude = new Label(this.ScaleCenter(0.4f), "lblLatitude");
		lblLatitude.setFont(Fonts.get16());
		lblLatitude.setPos(new Vector2(this.getWidth() - lblLatitude.getWidth(), arrowRec.getWidth() / 2));
		lblLatitude.setText("---");
		this.addChild(lblLatitude);

		lblLongitude = new Label(this.ScaleCenter(0.4f), "lblLongitude");
		lblLongitude.setFont(Fonts.get16());
		lblLongitude.setPos(new Vector2(this.getWidth() - lblLongitude.getWidth(), this.height * 0.1f));
		lblLongitude.setText("---");
		this.addChild(lblLongitude);
	}

	@Override
	protected void render(SpriteBatch batch)
	{

	}

	@Override
	public void onRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onParentRezised(CB_RectF rec)
	{
		// TODO Auto-generated method stub

	}

}
