package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.Map.Point;
import CB_Core.Math.UiSizes;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class SelectionMarker extends CB_View_Base
{
	public enum Type
	{
		Center, Left, Right
	}

	// protected EditWrapedTextField textField;
	protected Type type;
	protected Drawable marker;
	// Breite des Markers
	protected float markerWidth;
	// X-Position des Einfügepunktes des Markers relativ zur linke Seite
	protected float markerXPos;

	public SelectionMarker(Type type)
	{

		super(0, 0, 10, 10, "");

		float Height = UiSizes.getButtonHeight() / 2;

		this.setSize(Height, Height);

		this.type = type;
		Initial();
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
				marker = SpriteCache.selection_set;
				break;
			case Left:
				marker = SpriteCache.selection_left;
				break;
			case Right:
				marker = SpriteCache.selection_right;
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
	protected void render(SpriteBatch batch)
	{
		if (marker == null) Initial();
		marker.draw(batch, 0, 0, width, height);
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
			EditWrapedTextField tv = GL.that.getKeyboardFocus();
			if (tv != null)
			{
				x -= tv.ThisWorldRec.getX();
				y -= tv.ThisWorldRec.getY();
			}
			// SelectionMarker verschieben
			// neue gewünschte Koordinaten rel. links unten
			float newX = this.getX() + x - touchDownPos.x;
			float newY = this.getY() + y - touchDownPos.y;
			// System.out.println("getX()=" + this.getX() + " x=" + x + " - newX=" + newX);
			// neue gewünschte Koordinaten am Einfügepunkt des Markers
			newX = newX + markerXPos;
			newY = newY + height;
			Point cursorPos = GL.that.getKeyboardFocus().GetNextCursorPos(new Point((int) newX, (int) newY), type, true);
			if (cursorPos != null)
			{
				// System.out.println("x=" + x + " - aktX=" + this.getX() + " - touchX=" + touchDownPos.x);

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
			newY = newY + height;
			Point cursorPos = GL.that.getKeyboardFocus().GetNextCursorPos(new Point((int) newX, (int) newY), type, true);
			touchDownPos = null;
		}
		return true;
	}

	// move absolute
	public void moveTo(float x, float y)
	{
		EditWrapedTextField tv = GL.that.getKeyboardFocus();
		if (tv != null)
		{
			x += tv.ThisWorldRec.getX();
			y += tv.ThisWorldRec.getY();
		}

		float oldX = this.getX();
		float oldY = this.getY();
		this.setPos(x - markerXPos, y - height);
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
