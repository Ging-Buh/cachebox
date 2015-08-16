package CB_UI_Base.GL_UI.Controls;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.SpriteCacheBase;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Math.Point;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class SelectionMarker extends CB_View_Base
{
	public enum Type
	{
		Center, Left, Right
	}

	// protected EditTextField textField;
	protected Type type;
	protected Drawable marker;
	// Breite des Markers
	protected float markerWidth;
	// X-Position des Einfügepunktes des Markers relativ zur linke Seite
	protected float markerXPos;

	public SelectionMarker(Type type)
	{

		super(0, 0, 10, 10, "");

		float Height = UI_Size_Base.that.getButtonHeight();

		this.setSize(Height, Height);

		this.type = type;
		Initial();

		if (marker == null) return;

		// Orginalgröße des Marker-Sprites
		float orgWidth = marker.getMinWidth();
		float orgHeight = marker.getMinHeight();

		float Width = Height / orgHeight * orgWidth;
		// markerXPos ist der Einfügepunkt rel. der linken Seite
		switch (type)
		{
		case Center:
			markerXPos = ((orgWidth - 1) / 2) / orgWidth * Width;
			break;
		case Right:
			markerXPos = 0;
			break;
		case Left:
			markerXPos = (orgWidth - 1) / orgWidth * Width;
			break;
		}
		this.setWidth(Width);

	}

	@Override
	protected void Initial()
	{
		if (marker == null)
		{
			switch (type)
			{
			case Center:
				marker = SpriteCacheBase.selection_set;
				break;
			case Left:
				marker = SpriteCacheBase.selection_left;
				break;
			case Right:
				marker = SpriteCacheBase.selection_right;
				break;
			}
		}
	}

	@Override
	protected void SkinIsChanged()
	{
		Initial();
	}

	@Override
	protected void render(Batch batch)
	{
		if (marker == null) Initial();
		marker.draw(batch, 0, 0, getWidth(), getHeight());
	}

	private Point touchDownPos = null;

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{
		// Position merken, an der der TouchDown war
		if (pointer == 0)
		{
			touchDownPos = new Point(x, y);
		}
		return true;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		if ((pointer == 0) && (touchDownPos != null) && (!KineticPan))
		{
			// Korrektur der Position
			EditTextField tv = GL.that.getKeyboardFocus();
			if (tv != null)
			{
				x -= tv.ThisWorldRec.getX();
				y -= tv.ThisWorldRec.getY();
			}
			// SelectionMarker verschieben
			// neue gewünschte Koordinaten rel. links unten
			float newX = this.getX() + x - touchDownPos.x;
			float newY = this.getY() + y - touchDownPos.y;
			// System.out.println("getX()=" + this.Pos.x + " x=" + x + " - newX=" + newX);
			// neue gewünschte Koordinaten am Einfügepunkt des Markers
			newX = newX + markerXPos;
			newY = newY + getHeight();
			Point cursorPos = GL.that.getKeyboardFocus().GetNextCursorPos(new Point((int) newX, (int) newY), type, true);
			if (cursorPos != null)
			{
				// System.out.println("x=" + x + " - aktX=" + this.Pos.x + " - touchX=" + touchDownPos.x);

				// SelectionMarker verschieben
				moveTo(cursorPos.x, cursorPos.y);
			}
			// this.setPos(newX, newY);
		}
		return true;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		if ((pointer == 0) && (touchDownPos != null))
		{
			// SelectionMarker verschieben
			// neue gewünschte Koordinaten rel. links unten
			float newX = this.getX() + x - touchDownPos.x;
			float newY = this.getY() + y - touchDownPos.y;

			// neue gewünschte Koordinaten am Einfügepunkt des Markers
			newX = newX + markerXPos;
			newY = newY + getHeight();
			// Point cursorPos = GL.that.getKeyboardFocus().GetNextCursorPos(new Point((int) newX, (int) newY), type, true);
			touchDownPos = null;
		}
		return true;
	}

	// move absolute
	public void moveTo(float x, float y)
	{
		final EditTextField tv = GL.that.getKeyboardFocus();
		if (tv != null)
		{

			x += tv.ThisWorldRec.getX();
			y += tv.ThisWorldRec.getY();
		}

		// float oldX = this.Pos.x;
		// float oldY = this.Pos.y;
		this.setPos(x - markerXPos, y - getHeight());
		// if (this.ThisWorldRec != null)
		// {
		// this.ThisWorldRec.offset(x - markerXPos - oldX, y - height - oldY);
		// }
	}

	// move relative
	public void moveBy(float dx, float dy)
	{
		if ((Math.abs(dx) < 0.5) && (Math.abs(dy) < 0.5)) return;
		this.setPos(this.getX() + dx, this.getY() + dy);
	}
}
