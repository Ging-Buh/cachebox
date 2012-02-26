package CB_Core.GL_UI.Controls;

import CB_Core.GlobalCore;
import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MapInfoPanel extends GL_View_Base
{
	private CharSequence mLatitude = "";
	private CharSequence mLongitude = "";
	private CharSequence mDistance = "";
	private CharSequence mSpeed = "";
	private float mBearing = 0f;

	public void setCoord(Coordinate Coord)
	{
		if (Coord != null && Coord.Valid)
		{
			mLatitude = GlobalCore.FormatLatitudeDM(Coord.Latitude);
			mLongitude = GlobalCore.FormatLongitudeDM(Coord.Longitude);
			GL_Listener.glListener.renderOnce();
		}
	}

	public MapInfoPanel(CB_RectF rec, CharSequence Name)
	{
		super(rec, Name);
		setBackground(SpriteCache.InfoBack);
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

}
