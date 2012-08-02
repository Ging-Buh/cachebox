package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Activitys.ActivityBase;
import CB_Core.GL_UI.Activitys.EditCoord;
import CB_Core.GL_UI.Activitys.EditCoord.ReturnListner;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.graphics.g2d.NinePatch;

public class CoordinateButton extends Button
{
	Coordinate mActCoord;

	public interface CoordinateChangeListner
	{
		public void coordinateChanged(Coordinate coord);
	}

	private CoordinateChangeListner mCoordinateChangedListner;

	public CoordinateButton(CB_RectF rec, String name, Coordinate coord)
	{
		super(rec, name);
		mActCoord = coord;
		setText();
		this.setOnClickListener(click);

	}

	public void setCoordinateChangedListner(CoordinateChangeListner listner)
	{
		mCoordinateChangedListner = listner;
	}

	private void setText()
	{
		this.setText(mActCoord.FormatCoordinate());
	}

	@Override
	protected void Initial()
	{
		super.Initial();
		// switch ninePatchImages
		NinePatch tmp = mNinePatch;
		mNinePatch = mNinePatchPressed;
		mNinePatchPressed = tmp;
	}

	EditCoord edCo;

	private void initialEdCo()
	{

		edCo = new EditCoord(ActivityBase.ActivityRec(), "EditCoord", mActCoord, new ReturnListner()
		{

			@Override
			public void returnCoord(Coordinate coord)
			{
				if (coord != null && coord.Valid)
				{
					mActCoord = coord;
					if (mCoordinateChangedListner != null) mCoordinateChangedListner.coordinateChanged(coord);
					setText();
				}
				edCo = null;
			}
		});
	}

	OnClickListener click = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			if (edCo == null) initialEdCo();
			GL_Listener.glListener.showActivity(edCo);

			return true;
		}
	};

	public void setCoordinate(Coordinate coord)
	{
		mActCoord = coord;
		setText();
	}

	public Coordinate getCoordinate()
	{
		return mActCoord;
	}

	public void performClick()
	{
		super.performClick();
	}

}
