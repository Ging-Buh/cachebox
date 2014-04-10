package CB_UI.GL_UI.Controls;

import CB_Locator.Coordinate;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GlobalCore;
import CB_UI.GL_UI.Activitys.EditCoord;
import CB_UI.GL_UI.Activitys.EditCoord.ReturnListner;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.PopUps.CopiePastePopUp;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.interfaces.ICopyPaste;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;

import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Clipboard;

public class CoordinateButton extends Button implements ICopyPaste
{
	protected Coordinate mActCoord;
	protected CopiePastePopUp popUp;
	protected Clipboard clipboard;

	public interface CoordinateChangeListner
	{
		public void coordinateChanged(Coordinate coord);
	}

	private CoordinateChangeListner mCoordinateChangedListner;

	public CoordinateButton(CB_RectF rec, String name, Coordinate coord)
	{
		super(rec, name);
		if (coord == null) coord = new Coordinate();
		mActCoord = coord;
		setText();
		this.setOnClickListener(click);
		this.setOnLongClickListener(longCLick);
		clipboard = GlobalCore.getDefaultClipboard();
	}

	public CoordinateButton(String name)
	{
		super(name);
		mActCoord = new Coordinate();
		this.setOnClickListener(click);
		this.setOnLongClickListener(longCLick);
		clipboard = GlobalCore.getDefaultClipboard();
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
		Drawable tmp = drawableNormal;
		drawableNormal = drawablePressed;
		drawablePressed = tmp;
	}

	EditCoord edCo;

	private void initialEdCo()
	{

		edCo = new EditCoord(ActivityBase.ActivityRec(), "EditCoord", mActCoord, new ReturnListner()
		{

			@Override
			public void returnCoord(Coordinate coord)
			{
				if (coord != null && coord.isValid())
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
			GL.that.showActivity(edCo);
			return true;
		}
	};

	OnClickListener longCLick = new OnClickListener()
	{

		@Override
		public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button)
		{
			showPopUp(x, y);
			return true;
		}
	};

	public void setCoordinate(Coordinate coord)
	{
		mActCoord = coord;
		if (mActCoord == null) mActCoord = new Coordinate();
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

	protected void showPopUp(int x, int y)
	{

		popUp = new CopiePastePopUp("CopiePastePopUp=>" + getName(), this);

		float noseOffset = popUp.getHalfWidth() / 2;

		CB_RectF world = getWorldRec();

		// not enough place on Top?
		float windowH = UI_Size_Base.that.getWindowHeight();
		float windowW = UI_Size_Base.that.getWindowWidth();
		float worldY = world.getY();

		if (popUp.getHeight() + worldY > windowH * 0.8f)
		{
			popUp.flipX();
			worldY -= popUp.getHeight() + (popUp.getHeight() * 0.2f);
		}

		x += world.getX() - noseOffset;

		if (x < 0) x = 0;
		if (x + popUp.getWidth() > windowW) x = (int) (windowW - popUp.getWidth());

		y += worldY + (popUp.getHeight() * 0.2f);
		popUp.show(x, y);
	}

	@Override
	public String pasteFromClipboard()
	{
		if (clipboard == null) return null;
		String content = clipboard.getContents();
		Coordinate cor = null;
		if (content != null)
		{
			try
			{
				cor = new Coordinate(content);
			}
			catch (Exception e)
			{
			}

			if (cor != null)
			{
				this.setCoordinate(cor);
				return content;
			}
			else
			{
				return Translation.Get("cantPaste") + GlobalCore.br + content;
			}
		}
		else
			return null;
	}

	@Override
	public String copyToClipboard()
	{
		if (clipboard == null) return null;
		String content = this.getText();
		clipboard.setContents(content);
		return content;
	}

	@Override
	public String cutToClipboard()
	{
		if (clipboard == null) return null;
		String content = this.getText();
		clipboard.setContents(content);
		Coordinate cor = new Coordinate("N 0° 0.00 / E 0° 0.00");
		cor.setValid(false);
		this.setCoordinate(cor);
		return content;
	}
}
