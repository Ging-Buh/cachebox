package CB_Core.GL_UI;

import java.util.ConcurrentModificationException;
import java.util.Timer;
import java.util.TimerTask;

import CB_Core.GL_UI.GL_Listener.GL;
import CB_Core.GL_UI.interfaces.ViewOptionsMenu;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Types.MoveableList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class CB_View_Base extends GL_View_Base implements ViewOptionsMenu
{

	public float Left;
	public float Right;
	public float Top;
	public float Bottom;

	// # Constructors

	public CB_View_Base(String Name)
	{
		super(Name);
	}

	/**
	 * Constructor fuer ein neues GL_View_Base mit Angabe der linken unteren Ecke und der Hoehe und Breite
	 * 
	 * @param X
	 * @param Y
	 * @param Width
	 * @param Height
	 */
	public CB_View_Base(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height, Name);
	}

	public CB_View_Base(float X, float Y, float Width, float Height, GL_View_Base Parent, String Name)
	{
		super(X, Y, Width, Height, Parent, Name);
	}

	public CB_View_Base(CB_RectF rec, String Name)
	{
		super(rec, Name);
	}

	public CB_View_Base(CB_RectF rec, GL_View_Base Parent, String Name)
	{
		super(rec, Parent, Name);
	}

	public CB_View_Base(SizeF size, String Name)
	{
		super(size, Name);
	}

	@Override
	public void onFree()
	{

	}

	protected boolean isInitial = false;

	public void resetInitial()
	{
		isInitial = false;
		Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{

			@Override
			public void run()
			{
				GL.that.renderOnce("ResetInitial");
			}
		}, 50);
	}

	@Override
	protected void render(SpriteBatch batch)
	{

		if (!isInitial)
		{
			isInitial = true;
			Initial();
		}
	}

	@Override
	protected void renderWithoutScissor(SpriteBatch batch)
	{

	}

	/**
	 * Da die meisten Sprite Initialisierungen von Sprites im Render Thread durchgeführt werden müssen, wird diese Methode, zu
	 * Initialisierung im ersten Render Durchgang ausgeführt.
	 */
	protected abstract void Initial();

	@Override
	public void onRezised(CB_RectF rec)
	{

	}

	@Override
	public void onParentRezised(CB_RectF rec)
	{

	}

	@Override
	public boolean onLongClick(int x, int y, int pointer, int button)
	{

		return false;
	}

	@Override
	public boolean onTouchDown(int x, int y, int pointer, int button)
	{

		return false;
	}

	@Override
	public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan)
	{

		return false;
	}

	@Override
	public boolean onTouchUp(int x, int y, int pointer, int button)
	{

		return false;
	}

	@Override
	public void dispose()
	{
		// Remove from RenderViews if registered
		GL.that.removeRenderView(this);

		if (childs == null)
		{
			// set this to null!
			setToNull(this);
		}
		else
		{

			try
			{
				synchronized (childs)
				{
					for (GL_View_Base v : childs)
					{
						removeChild(v);
						v.dispose();
					}

					childs.clear();
					// set this to null!
					setToNull(this);
				}
			}
			catch (ConcurrentModificationException e)
			{
				setToNull(this);
			}
		}

	}

	public static void setToNull(CB_View_Base view)
	{
		if (view.childs == null)
		{
			view = null;
		}
		else
		{
			synchronized (view.childs)
			{
				view.childs = null;
				view = null;
			}
		}
	}

	public int getCildCount()
	{
		if (childs == null) return -1;
		synchronized (childs)
		{
			return childs.size();
		}
	}

	public GL_View_Base addChildDirekt(final GL_View_Base view)
	{
		if (childs == null) return null;
		synchronized (childs)
		{
			childs.add(view);
		}

		return view;
	}

	public GL_View_Base addChildDirektLast(final GL_View_Base view)
	{
		if (childs == null) return null;
		synchronized (childs)
		{
			childs.add(0, view);
		}

		return view;
	}

	public void removeChildsDirekt()
	{
		if (childs == null) return;
		synchronized (childs)
		{
			childs.clear();
		}
	}

	public void removeChildsDirekt(GL_View_Base view)
	{
		if (childs == null) return;
		synchronized (childs)
		{
			childs.remove(view);
		}
	}

	public void removeChildsDirekt(MoveableList<GL_View_Base> childs)
	{
		if (childs == null) return;
		synchronized (childs)
		{
			childs.remove(childs);
		}
	}

	public GL_View_Base getChild(int i)
	{
		if (childs == null) return null;
		synchronized (childs)
		{
			if (childs.size() < i || childs.size() == 0) return null;
			return childs.get(i);
		}
	}

	@Override
	public String toString()
	{
		return getName() + " X,Y/Width,Height = " + this.getX() + "," + this.getY() + "/" + this.width + "," + this.height;
	}

}
