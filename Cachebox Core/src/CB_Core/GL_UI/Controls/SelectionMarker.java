package CB_Core.GL_UI.Controls;

import CB_Core.GL_UI.CB_View_Base;
import CB_Core.GL_UI.SpriteCache;
import CB_Core.Map.Point;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class SelectionMarker extends CB_View_Base
{
	public enum Type
	{
		Center, Left, Right
	}

	protected EditWrapedTextField textField;
	protected Type type;
	protected Drawable marker;
	// Breite des Markers
	protected float markerWidth;
	// X-Position des Einfügepunktes des Markers relativ zur linke Seite
	protected float markerXPos;

	public SelectionMarker(EditWrapedTextField textField, float X, float Y, float Height, Type type)
	{
		super(X, Y, Height, Height, "");
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
			markerXPos = ((orgWidth - 1) / 2) / Width * orgWidth;
			break;
		case Right:
			markerXPos = 0;
			break;
		case Left:
			markerXPos = (orgWidth - 1) / Width * orgWidth;
			break;
		}
		this.setWidth(Width);
		this.textField = textField;
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
			// SelectionMarker verschieben
			// neue gewünschte Koordinaten rel. links unten
			float newX = this.getX() + x - touchDownPos.x;
			float newY = this.getY() + y - touchDownPos.y;

			// neue gewünschte Koordinaten am Einfügepunkt des Markers
			newX = newX + markerXPos;
			newY = newY + height;
			Point cursorPos = textField.GetNextCursorPos(new Point((int) newX, (int) newY), type, true);
			if (cursorPos != null)
			{
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
			Point cursorPos = textField.GetNextCursorPos(new Point((int) newX, (int) newY), type, true);
			touchDownPos = null;
		}
		return true;
	}

	public void moveTo(float x, float y)
	{
		this.setPos(x - markerXPos, y - height);
	}
}
