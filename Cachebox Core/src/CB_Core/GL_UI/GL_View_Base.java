package CB_Core.GL_UI;

import java.util.Iterator;

import CB_Core.Math.CB_RectF;
import CB_Core.Types.MoveableList;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public abstract class GL_View_Base extends CB_RectF
{

	// # CONSTANDS
	/**
	 * This view is visible. Use with {@link #setVisibility}.
	 */
	public final static int VISIBLE = 0x00000000;

	/**
	 * This view is invisible. Use with {@link #setVisibility}.
	 */
	public static final int INVISIBLE = 0x00000002;

	// # private Member

	/**
	 * Enthält alle GL_Views innerhalb dieser Gl_View
	 */
	private MoveableList<GL_View_Base> childs = new MoveableList<GL_View_Base>();

	protected boolean onTouchUp = false;
	protected boolean onTouchDown = false;
	protected Vector2 lastTouchPos;

	private int mViewState = VISIBLE;

	// # Constructors
	/**
	 * Constructor für ein neues GL_View_Base mit Angabe der linken unteren Ecke und der Höhe und Breite
	 * 
	 * @param X
	 * @param Y
	 * @param Width
	 * @param Height
	 */
	public GL_View_Base(float X, float Y, float Width, float Height)
	{
		super(X, Y, Width, Height);

	}

	// # Method

	public void setVisibility(int visibility)
	{
		mViewState = visibility;
	}

	/**
	 * Gibt die Visibility dieser GL_View zurück.</br> Wenn die Größe dieser GL_View <=0f ist, so wird INVISIBLE zurück gegeben.
	 * 
	 * @return
	 */
	public int getVisibility()
	{
		if (this.getWidth() <= 0f || this.getHeight() <= 0f) return INVISIBLE;
		return mViewState;
	}

	public boolean isVisible()
	{
		return (getVisibility() == VISIBLE);
	}

	public void addChild(GL_View_Base view)
	{
		childs.add(view);
	}

	public void removeChild(GL_View_Base view)
	{
		childs.remove(view);
	}

	public void removeChilds()
	{
		childs.clear();
	}

	public void removeChilds(MoveableList<GL_View_Base> childs)
	{
		this.childs.remove(childs);
	}

	/**
	 * Die renderChilds() Methode wird vom GL_Listner bei jedem Render-Vorgang aufgerufen. </br> Hier wird dann zuerst die render() Methode
	 * dieser View aufgerufen. </br> Danach werden alle Childs iteriert und dessen renderChilds() Methode aufgerufen, wenn die View sichtbar
	 * ist (Visibility).
	 * 
	 * @param batch
	 */
	public void renderChilds(final SpriteBatch batch)
	{
		this.render(batch);

		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.getVisibility() == VISIBLE) view.render(batch);
		}
	}

	public abstract void render(SpriteBatch batch);

	@Override
	public void resize(float width, float height)
	{
		onRezised(this);
	}

	public abstract void onRezised(CB_RectF rec);

	// # abstracte Methoden zur Übergabe von Eingaben
	protected boolean hitTest(Vector2 pos)
	{
		if (this.contains(pos.x, pos.y))
		{
			// alle Childs abfragen
			for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
			{
				GL_View_Base view = iterator.next();
				view.hitTest(pos);
			}

			onClicked(pos);
			return true;
		}
		return false;
	}

	protected abstract void onClicked(Vector2 pos);

	protected boolean touchDownTest(Vector2 pos)
	{
		if (this.contains(pos.x, pos.y))
		{

			lastTouchPos = pos;

			// alle Childs abfragen
			for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
			{
				GL_View_Base view = iterator.next();
				view.touchDownTest(pos);
			}

			onTouchDown(pos);
			return true;
		}
		return false;
	}

	public abstract boolean onTouchDown(Vector2 pos);

	protected void TouchRelease()
	{
		// die Abfrage schleife brauch nur laufen, wenn
		// der letzte onTouchDown auch dieses View betraf.
		if (this.contains(lastTouchPos.x, lastTouchPos.y))
		{
			// alle Childs abfragen
			for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
			{
				GL_View_Base view = iterator.next();
				view.TouchRelease();
			}

			onTouchUp = false;
			onTouchDown = false;

			onTouchRelease();
		}
	}

	public abstract void onTouchRelease();

	public boolean pan(int x, int y, int deltaX, int deltaY)
	{

		boolean behandelt = false;

		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.pan(x, y, deltaX, deltaY))
			{
				// schon behandelt
				behandelt = true;
				break;
			}
		}

		onTouchUp = false;
		onTouchDown = false;
		return behandelt;
	}

	public boolean zoom(float originalDistance, float currentDistance)
	{
		boolean behandelt = false;

		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.zoom(originalDistance, currentDistance))
			{
				// schon behandelt
				behandelt = true;
				break;
			}
		}

		onTouchUp = false;
		onTouchDown = false;
		return behandelt;
	}

	public boolean fling(float velocityX, float velocityY)
	{
		boolean behandelt = false;

		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.fling(velocityX, velocityY))
			{
				// schon behandelt
				behandelt = true;
				break;
			}
		}

		onTouchUp = false;
		onTouchDown = false;
		return behandelt;
	}

	public boolean longPress(int x, int y)
	{
		boolean behandelt = false;

		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.longPress(x, y))
			{
				// schon behandelt
				behandelt = true;
				break;
			}
		}

		onTouchUp = false;
		onTouchDown = false;
		return behandelt;
	}

	public boolean tap(int x, int y, int count)
	{
		boolean behandelt = false;

		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.tap(x, y, count))
			{
				// schon behandelt
				behandelt = true;
				break;
			}
		}

		onTouchUp = false;
		onTouchDown = false;
		return behandelt;
	}

	public boolean touchDown(int x, int y, int pointer)
	{
		boolean behandelt = false;

		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.touchDown(x, y, pointer))
			{
				// schon behandelt
				behandelt = true;
				break;
			}
		}

		onTouchUp = false;
		onTouchDown = false;
		return behandelt;
	}

	public void onStop()
	{

	}

	public boolean click(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht überschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown übergeben!!!
		boolean behandelt = false;
		return false;
	}

	public boolean longClick(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht überschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown übergeben!!!
		boolean behandelt = false;
		return false;
	}

	public boolean touchDown(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht überschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown übergeben!!!
		boolean behandelt = false;
		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
			GL_View_Base view = iterator.next();

			if (view.contains(x, y))
			{
				// touch innerhalb des Views
				// -> Klick an das View weitergeben
				behandelt = view.touchDown(x, y, pointer, button);
			}
		}
		if (!behandelt)
		{
			// kein Klick in einem untergeordnetem View
			// -> hier behandeln
			behandelt = onTouchDown(x, y, pointer, button);
		}
		return behandelt;
	}

	public boolean touchDragged(int x, int y, int pointer)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht überschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown übergeben!!!
		boolean behandelt = false;
		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.touchDragged(x, y, pointer))
			{
				behandelt = true;
				break;
			}

		}

		return behandelt;
	}

	public boolean touchUp(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht überschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown übergeben!!!
		boolean behandelt = false;
		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.touchUp(x, y, pointer, button))
			{
				behandelt = true;
				break;
			}

		}

		return behandelt;
	}

	// die untergeordneten Klassen müssen diese Event-Handler überschreiben!!!
	public abstract boolean onClick(int x, int y, int pointer, int button);

	public abstract boolean onLongClick(int x, int y, int pointer, int button);

	public abstract boolean onTouchDown(int x, int y, int pointer, int button);

	public abstract boolean onTouchDragged(int x, int y, int pointer);

	public abstract boolean onTouchUp(int x, int y, int pointer, int button);

}
