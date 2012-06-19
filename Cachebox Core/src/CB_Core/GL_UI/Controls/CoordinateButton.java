package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.GL_View_Base;
import CB_Core.GL_UI.Controls.Dialogs.EditCoord;
import CB_Core.GL_UI.Controls.Dialogs.EditCoord.ReturnListner;
import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.Coordinate;

import com.badlogic.gdx.graphics.g2d.NinePatch;

public class CoordinateButton extends Button
{
	Coordinate mActCoord;

	public CoordinateButton(CB_RectF rec, String name, Coordinate coord)
	{
		super(rec, name);
		mActCoord = coord;
		setText();
		this.setOnClickListener(click);
		dialogRec = new CB_RectF(0, 0, UiSizes.getSmallestWidth(), UiSizes.getWindowHeight());
		edCo = new EditCoord(dialogRec, "EditCoord", mActCoord, new ReturnListner()
		{

			@Override
			public void returnCoord(Coordinate coord)
			{
				if (coord != null && coord.Valid)
				{
					mActCoord = coord;
					setText();
				}
			}
		});

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

	CB_RectF dialogRec;

	EditCoord edCo;

	OnClickListener click = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{

			GL_Listener.glListener.showDialog(edCo);

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

}
