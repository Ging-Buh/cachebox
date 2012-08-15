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

	protected Type type;
	protected Drawable marker;

	public SelectionMarker(float X, float Y, float Width, float Height, Type type)
	{
		super(X, Y, Width, Height, "");
		this.type = type;
	}

	@Override
	protected void Initial()
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
			this.setPos(this.getX() + x - touchDownPos.x, this.getY() + y - touchDownPos.y);
		}
		return true;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{
		if (pointer == 0)
		{
			touchDownPos = null;
		}
		return true;
	}
}
