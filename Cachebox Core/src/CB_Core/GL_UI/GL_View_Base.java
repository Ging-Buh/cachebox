package CB_Core.GL_UI;

import java.util.Iterator;

import CB_Core.Log.Logger;
import CB_Core.Math.CB_RectF;
import CB_Core.Types.MoveableList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
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

	public static boolean debug = false;

	// # private Member

	private boolean hasBackground = false;
	private Sprite Background;
	protected String name = "";
	private boolean hasNinePatchBackground = false;
	private NinePatch nineBackground;

	protected GL_View_Base Me;

	/**
	 * Enthält alle GL_Views innerhalb dieser Gl_View
	 */
	private MoveableList<GL_View_Base> childs = new MoveableList<GL_View_Base>();

	private OnClickListener mOnClickListener;
	protected boolean isClickable = false;

	protected boolean onTouchUp = false;
	protected boolean onTouchDown = false;
	protected Vector2 lastTouchPos;

	private int mViewState = VISIBLE;

	private GL_View_Base parent;

	// # Constructors

	public GL_View_Base(String Name)
	{
		Me = this;
		name = Name;
	}

	/**
	 * Constructor für ein neues GL_View_Base mit Angabe der linken unteren Ecke und der Höhe und Breite
	 * 
	 * @param X
	 * @param Y
	 * @param Width
	 * @param Height
	 */
	public GL_View_Base(float X, float Y, float Width, float Height, String Name)
	{
		super(X, Y, Width, Height);
		Me = this;
		name = Name;
	}

	public GL_View_Base(float X, float Y, float Width, float Height, GL_View_Base Parent, String Name)
	{
		super(X, Y, Width, Height);
		Me = this;
		parent = Parent;
		name = Name;
	}

	public GL_View_Base(CB_RectF rec, String Name)
	{
		super(rec);
		Me = this;
		name = Name;
	}

	public GL_View_Base(CB_RectF rec, GL_View_Base Parent, String Name)
	{
		super(rec);
		Me = this;
		parent = Parent;
		name = Name;
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
	public void renderChilds(final SpriteBatch batch, ParentInfo parentInfo)
	{
		// first Draw Background?
		if (hasBackground || hasNinePatchBackground)
		{
			batch.begin();
			if (hasNinePatchBackground)
			{
				nineBackground.draw(batch, 0, 0, width, height);
			}
			else
			{
				batch.draw(Background, 0, 0, width, height);
			}

			batch.end();
		}

		boolean mustScissorCalc = false;

		if (parent != null)
		{
			mustScissorCalc = !parent.contains(this);
		}

		Gdx.gl.glEnable(GL10.GL_SCISSOR_TEST);

		if (mustScissorCalc)
		{
			CB_RectF temp = parentInfo.drawRec().copy();
			temp.setPos(new Vector2()); // auf 0,0 setzen
			CB_RectF intersectRec = temp.createIntersection(this);

			intersectRec.setPos(parentInfo.Vector());

			Gdx.gl.glScissor((int) intersectRec.getX(), (int) intersectRec.getY(), (int) intersectRec.getWidth(),
					(int) intersectRec.getHeight());
		}
		else
		{
			Gdx.gl.glScissor((int) parentInfo.x(), (int) parentInfo.y(), (int) width, (int) height);
		}

		batch.begin();
		this.render(batch);
		batch.end();

		Gdx.gl.glDisable(GL10.GL_SCISSOR_TEST);

		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			// alle renderChilds() der in dieser GL_View_Base
			// enthaltenen Childs auf rufen.
			GL_View_Base view = iterator.next();

			// hier nicht view.render(batch) aufrufen, da sonnst die in der
			// view enthaldenen Childs nicht aufgerufen werden.
			if (view.getVisibility() == VISIBLE)
			{
				ParentInfo tmpTranslate = parentInfo.cpy();
				tmpTranslate.add(view.Pos.x, view.Pos.y);

				batch.setProjectionMatrix(tmpTranslate.Matrix());

				view.renderChilds(batch, tmpTranslate);

				batch.setProjectionMatrix(parentInfo.Matrix());
			}
		}

		// Draw Debug REC
		if (debug)
		{

			if (debugRec == null)
			{
				int w = getNextHighestPO2((int) width);
				int h = getNextHighestPO2((int) height);
				Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
				p.setColor(1f, 0f, 0f, 1f);
				p.drawRectangle(1, 1, (int) width - 1, (int) height - 1);

				Texture tex = new Texture(p, Pixmap.Format.RGBA8888, false);

				debugRec = new Sprite(tex, (int) width, (int) height);
				Logger.LogCat("GL_Control ------[ " + name + " ]-----------------------");
				Logger.LogCat("Create Debug Rec " + Pos.x + "/" + Pos.y + "/" + width + "/" + height);

			}

			batch.begin();

			debugRec.draw(batch);

			batch.end();
		}

	}

	private Sprite debugRec = null;

	protected abstract void render(SpriteBatch batch);

	@Override
	public void resize(float width, float height)
	{
		onRezised(this);
		debugRec = null;
	}

	public abstract void onRezised(CB_RectF rec);

	public void onStop()
	{

	}

	public boolean click(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht überschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown übergeben!!!
		boolean behandelt = false;
		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
			GL_View_Base view = iterator.next();

			if (!view.isClickable()) continue;

			if (view.contains(x, y))
			{
				// touch innerhalb des Views
				// -> Klick an das View weitergeben
				behandelt = view.click(x - (int) view.Pos.x, y - (int) view.Pos.y, pointer, button);
			}
		}
		if (!behandelt)
		{
			// kein Klick in einem untergeordnetem View
			// -> hier behandeln
			if (mOnClickListener != null)
			{
				behandelt = mOnClickListener.onClick(this, x, y, pointer, button);
			}

		}
		return behandelt;
	}

	public final boolean longClick(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht überschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown übergeben!!!
		boolean behandelt = false;
		return false;
	}

	public final boolean touchDown(int x, int y, int pointer, int button)
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
				behandelt = view.touchDown(x - (int) view.Pos.x, y - (int) view.Pos.y, pointer, button);
			}

			if (behandelt) break;

		}
		if (!behandelt)
		{
			// kein Klick in einem untergeordnetem View
			// -> hier behandeln
			behandelt = onTouchDown(x, y, pointer, button);
		}
		return behandelt;
	}

	public final boolean touchDragged(int x, int y, int pointer)
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

	public final boolean touchUp(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht überschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown übergeben!!!
		boolean behandelt = false;
		// alle Childs abfragen
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			GL_View_Base view = iterator.next();
			if (view.contains(x, y))
			{
				// touch innerhalb des Views
				// -> Klick an das View weitergeben
				behandelt = view.touchUp(x - (int) view.Pos.x, y - (int) view.Pos.y, pointer, button);
			}

			if (behandelt) break;

		}

		if (!behandelt)
		{
			// kein Klick in einem untergeordnetem View
			// -> hier behandeln
			behandelt = onTouchUp(x, y, pointer, button);
		}
		return behandelt;
	}

	// die untergeordneten Klassen müssen diese Event-Handler überschreiben!!!
	// public abstract boolean onClick(int x, int y, int pointer, int button);

	public abstract boolean onLongClick(int x, int y, int pointer, int button);

	public abstract boolean onTouchDown(int x, int y, int pointer, int button);

	public abstract boolean onTouchDragged(int x, int y, int pointer);

	public abstract boolean onTouchUp(int x, int y, int pointer, int button);

	public abstract void dispose();

	/**
	 * Interface definition for a callback to be invoked when a view is clicked.
	 */
	public interface OnClickListener
	{
		/**
		 * Called when a view has been clicked.
		 * 
		 * @param v
		 *            The view that was clicked.
		 */
		boolean onClick(GL_View_Base v, int x, int y, int pointer, int button);
	}

	/**
	 * Register a callback to be invoked when this view is clicked. If this view is not clickable, it becomes clickable.
	 * 
	 * @param l
	 *            The callback that will run
	 * @see #setClickable(boolean)
	 */
	public void setOnClickListener(OnClickListener l)
	{
		if (!isClickable)
		{
			isClickable = true;
		}
		mOnClickListener = l;
	}

	public boolean isClickable()
	{
		return isClickable;
	}

	/**
	 * Setzt dieses View Clicable mit der übergabe von True. </br> Wenn Dieses View nicht Clickable ist, werde auch keine Click-Abfragen an
	 * die Childs weitergegeben.
	 * 
	 * @param value
	 */
	public void setClickable(boolean value)
	{
		isClickable = value;
	}

	public void setBackground(Sprite background)
	{
		hasBackground = background != null;

		Background = background;
	}

	public void setBackground(NinePatch background)
	{
		hasNinePatchBackground = background != null;

		nineBackground = background;
	}

}
