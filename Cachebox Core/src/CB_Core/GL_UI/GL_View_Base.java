package CB_Core.GL_UI;

import java.util.ArrayList;
import java.util.Iterator;

import CB_Core.GL_UI.GL_Listener.GL_Listener;
import CB_Core.Math.CB_RectF;
import CB_Core.Math.SizeF;
import CB_Core.Math.UiSizes;
import CB_Core.Types.MoveableList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public abstract class GL_View_Base extends CB_RectF
{

	// # CONSTANDS

	/**
	 * Pointer ID for Mouse wheel scrolling up
	 */
	public static final int MOUSE_WHEEL_POINTER_UP = -280272;

	/**
	 * Pointer ID for Mouse wheel scrolling down
	 */
	public static final int MOUSE_WHEEL_POINTER_DOWN = -280273;

	/**
	 * This view is visible. Use with {@link #setVisibility}.
	 */
	public final static int VISIBLE = 0x00000000;

	/**
	 * This view is invisible. Use with {@link #setVisibility}.
	 */
	public static final int INVISIBLE = 0x00000002;

	public static boolean debug = false;
	public static boolean disableScissor = false;

	// # private Member
	protected String name = "";

	protected Drawable drawableBackground;

	protected GL_View_Base Me;

	/**
	 * Enthï¿½lt alle GL_Views innerhalb dieser Gl_View
	 */
	protected MoveableList<GL_View_Base> childs = new MoveableList<GL_View_Base>();

	protected OnClickListener mOnClickListener;
	protected OnLongClickListener mOnLongClickListener;
	protected boolean isClickable = false;

	protected boolean onTouchUp = false;
	protected boolean onTouchDown = false;
	public Vector2 lastTouchPos;

	private int mViewState = VISIBLE;

	protected GL_View_Base parent;
	protected static int nDepthCounter = 0;

	private Sprite debugRec = null;

	// # Constructors

	public GL_View_Base(String Name)
	{
		Me = this;
		name = Name;
	}

	/**
	 * Constructor fuer ein neues GL_View_Base mit Angabe der linken unteren Ecke und der Hoehe und Breite
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

	public GL_View_Base(SizeF size, String Name)
	{
		super(0, 0, size.width, size.height);
		Me = this;
		name = Name;

	}

	// # Method
	public void setVisibility(int visibility)
	{
		if (mViewState == visibility) return;
		mViewState = visibility;
		GL_Listener.glListener.renderOnce(this.getName() + "setVisibility");
	}

	public MoveableList<GL_View_Base> getchilds()
	{
		return childs;
	}

	/**
	 * Gibt die Parent View zurück, wenn diese über den Constructor übergeben wurde!
	 * 
	 * @return parent View oder null
	 */
	public GL_View_Base getParent()
	{
		return parent;
	}

	/**
	 * Gibt die Visibility dieser GL_View zurueck.</br> Wenn die Groesse dieser GL_View <=0f ist, so wird INVISIBLE zurueck gegeben.
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

	public GL_View_Base addChild(final GL_View_Base view)
	{
		return addChild(view, false);
	}

	public GL_View_Base addChild(final GL_View_Base view, final boolean last)
	{
		this.RunOnGL(new runOnGL()
		{
			@Override
			public void run()
			{
				// synchronized (childs)
				// {

				if (last)
				{
					childs.add(0, view);
				}
				else
				{
					childs.add(view);
				}
				// }
			}
		});

		return view;
	}

	public void removeChild(final GL_View_Base view)
	{
		this.RunOnGL(new runOnGL()
		{
			@Override
			public void run()
			{
				// synchronized (childs)
				// {
				childs.remove(view);
				// }
			}
		});
	}

	public void removeChilds()
	{
		this.RunOnGL(new runOnGL()
		{
			@Override
			public void run()
			{
				// synchronized (childs)
				// {
				childs.clear();
				// }
			}
		});
	}

	public void removeChilds(final MoveableList<GL_View_Base> Childs)
	{
		this.RunOnGL(new runOnGL()
		{
			@Override
			public void run()
			{
				// synchronized (childs)
				// {
				childs.remove(Childs);
				// }
			}
		});
	}

	private ArrayList<runOnGL> runOnGL_List = new ArrayList<runOnGL>();

	public void RunOnGL(runOnGL run)
	{
		// synchronized (runOnGL_List)
		// {
		runOnGL_List.add(run);
		// }

	}

	public float getLeftWidth()
	{
		if (drawableBackground != null) return drawableBackground.getLeftWidth();
		return 0;
	}

	public float getRightWidth()
	{
		if (drawableBackground != null) return drawableBackground.getRightWidth();
		return 0;
	}

	public float getTopHeight()
	{
		if (drawableBackground != null) return drawableBackground.getTopHeight();
		return 0;
	}

	public float getBottomHeight()
	{
		if (drawableBackground != null) return drawableBackground.getBottomHeight();
		return 0;
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

		if (thisInvalidate)
		{
			myParentInfo = parentInfo.cpy();
			CalcMyInfoForChild();
		}

		if (intersectRec.getHeight() + 1 < 0 || intersectRec.getWidth() + 1 < 0) return; // hier gibt es nichts zu rendern

		if (!disableScissor) Gdx.gl.glEnable(GL10.GL_SCISSOR_TEST);
		Gdx.gl.glScissor((int) intersectRec.getX(), (int) intersectRec.getY(), (int) intersectRec.getWidth() + 1,
				(int) intersectRec.getHeight() + 1);

		// first Draw Background?

		batch.begin();

		if (drawableBackground != null)
		{
			drawableBackground.draw(batch, 0, 0, width, height);
		}

		batch.end();

		// synchronized (runOnGL_List)
		// {

		if (runOnGL_List.size() > 0)
		{
			for (runOnGL run : runOnGL_List)
			{
				if (run != null) run.run();
			}

			runOnGL_List.clear();
		}
		// }

		batch.begin();
		this.render(batch);
		batch.end();

		Gdx.gl.glDisable(GL10.GL_SCISSOR_TEST);

		batch.begin();
		this.renderWithoutScissor(batch);
		batch.end();

		// synchronized (childs)
		// {
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			// alle renderChilds() der in dieser GL_View_Base
			// enthaltenen Childs auf rufen.

			GL_View_Base view;
			try
			{
				view = iterator.next();

				// hier nicht view.render(batch) aufrufen, da sonnst die in der
				// view enthaldenen Childs nicht aufgerufen werden.
				if (view != null && view.getVisibility() == VISIBLE)
				{

					if (childsInvalidate) view.invalidate();

					ParentInfo myInfoForChild = myParentInfo.cpy();
					myInfoForChild.setWorldDrawRec(intersectRec);

					myInfoForChild.add(view.Pos.x, view.Pos.y);

					batch.setProjectionMatrix(myInfoForChild.Matrix());
					nDepthCounter++;

					view.renderChilds(batch, myInfoForChild);
					nDepthCounter--;
					batch.setProjectionMatrix(myParentInfo.Matrix());
				}

			}
			catch (java.util.ConcurrentModificationException e)
			{
				// da die Liste nicht mehr gültig ist, brechen wir hier den Iterator ab
				break;
			}
		}
		// }
		childsInvalidate = false;

		// Draw Debug REC
		if (debug)
		{

			if (debugRec != null)
			{
				batch.begin();
				debugRec.draw(batch);
				batch.end();
			}

		}

	}

	private void writeDebug()
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

			// Logger.LogCat("GL_Control ------[ " + name + " ]------[ Ebene: " + nDepthCounter + " ]----------");
			// Logger.LogCat("Create Debug Rec " + Pos.x + "/" + Pos.y + "/" + width + "/" + height);
			// Logger.LogCat("Parent Draw  Rec " + myParentInfo.drawRec().getPos().x + "/" + myParentInfo.drawRec().getPos().y + "/"
			// + myParentInfo.drawRec().getWidth() + "/" + myParentInfo.drawRec().getHeight());
			// Logger.LogCat("intersectRec  Rec " + intersectRec.getPos().x + "/" + intersectRec.getPos().y + "/" + intersectRec.getWidth()
			// + "/" + intersectRec.getHeight() + "  interscted =" + mustSetScissor);
			// Logger.LogCat("This World Rec    " + ThisWorldRec.getPos().x + "/" + ThisWorldRec.getPos().y + "/" + ThisWorldRec.getWidth()
			// + "/" + ThisWorldRec.getHeight());
			// Logger.LogCat("ParentInfo.Vector= " + myParentInfo.Vector());
		}
	}

	public CB_RectF ThisWorldRec;
	public CB_RectF intersectRec;
	public ParentInfo myParentInfo;
	private boolean mustSetScissor = false;
	private boolean childsInvalidate = false;
	private boolean thisInvalidate = true;

	public CB_RectF getWorldRec()
	{
		return ThisWorldRec.copy();
	}

	/**
	 * Berechnet das Scissor Rechteck und die Infos fï¿½r die Childs immer dann wenn sich etwas an Position oder Grï¿½ï¿½e dieses
	 * GL_View_Base geï¿½ndert hat.</br> Wenn sich etwas geï¿½ndert hat, wird auch ein Invalidate an die Childs ï¿½bergeben, da diese auch
	 * neu berechnet werden mï¿½ssen. </br> Die detection wann sich etwas geï¿½ndert hat, kommt von der ï¿½berschriebenen CB_RectF Methode
	 * CalcCrossPos, da diese bei jeder ï¿½nderung aufgerufen wird.
	 */
	private void CalcMyInfoForChild()
	{
		childsInvalidate = true;
		ThisWorldRec = this.copy().offset(myParentInfo.Vector());
		ThisWorldRec.offset(-this.getX(), -this.getY());
		mustSetScissor = !myParentInfo.drawRec().contains(ThisWorldRec);

		if (mustSetScissor)
		{
			intersectRec = myParentInfo.drawRec().createIntersection(ThisWorldRec);
		}
		else
		{
			intersectRec = ThisWorldRec.copy();
		}

		thisInvalidate = false;

		if (debug) writeDebug();
	}

	public void invalidate()
	{
		thisInvalidate = true;
	}

	@Override
	protected void calcCrossCorner()
	{
		super.calcCrossCorner();
		thisInvalidate = true;
	}

	protected abstract void render(SpriteBatch batch);

	protected abstract void renderWithoutScissor(SpriteBatch batch);

	@Override
	public void resize(float width, float height)
	{
		onRezised(this);
		debugRec = null;

		// Eine Größenänderung an die Childs Melden
		// synchronized (childs)
		// {
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			// alle renderChilds() der in dieser GL_View_Base
			// enthaltenen Childs auf rufen.
			GL_View_Base view = iterator.next();
			if (view != null) view.onParentRezised(this);
		}
		// }
	}

	public abstract void onRezised(CB_RectF rec);

	public abstract void onParentRezised(CB_RectF rec);

	public void onShow()
	{
		// synchronized (childs)
		// {
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			// alle renderChilds() der in dieser GL_View_Base
			// enthaltenen Childs auf rufen.
			GL_View_Base view = iterator.next();
			view.onShow();
		}
		// }
	}

	public void onStop()
	{
		// synchronized (childs)
		// {
		for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		{
			// alle renderChilds() der in dieser GL_View_Base
			// enthaltenen Childs auf rufen.
			GL_View_Base view = iterator.next();
			view.onStop();
		}
		// }
	}

	public boolean click(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht ï¿½berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown ï¿½bergeben!!!
		boolean behandelt = false;
		// alle Childs abfragen
		// synchronized (childs)
		// {
		// for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
		{
			// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
			GL_View_Base view = iterator.next();

			if (view == null || !view.isClickable()) continue;
			// Invisible Views can not be clicked!
			if (!view.isVisible()) continue;

			if (view.contains(x, y))
			{
				// touch innerhalb des Views
				// -> Klick an das View weitergeben
				behandelt = view.click(x - (int) view.Pos.x, y - (int) view.Pos.y, pointer, button);
			}
		}
		// }
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

	public boolean doubleClick(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht ï¿½berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown ï¿½bergeben!!!
		boolean behandelt = false;
		// alle Childs abfragen
		// synchronized (childs)
		// {
		// for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
		{
			// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
			GL_View_Base view = iterator.next();

			if (view == null || !view.isClickable()) continue;
			// Invisible Views can not be clicked!
			if (!view.isVisible()) continue;

			if (view.contains(x, y))
			{
				// touch innerhalb des Views
				// -> Klick an das View weitergeben
				behandelt = view.doubleClick(x - (int) view.Pos.x, y - (int) view.Pos.y, pointer, button);
			}
		}
		// }
		return behandelt;
	}

	public boolean longClick(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht ï¿½berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown ï¿½bergeben!!!
		boolean behandelt = false;

		// synchronized (childs)
		// {
		// for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
		for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
		{
			// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
			GL_View_Base view = iterator.next();

			if (view == null || !view.isClickable()) continue;

			if (view.contains(x, y))
			{
				// touch innerhalb des Views
				// -> Klick an das View weitergeben
				behandelt = view.longClick(x - (int) view.Pos.x, y - (int) view.Pos.y, pointer, button);
			}
		}
		// }
		if (!behandelt)
		{
			// kein Klick in einem untergeordnetem View
			// -> hier behandeln
			if (mOnLongClickListener != null)
			{
				behandelt = mOnLongClickListener.onLongClick(this, x, y, pointer, button);
			}

		}
		return behandelt;
	}

	public final GL_View_Base touchDown(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht ï¿½berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown ï¿½bergeben!!!
		// touchDown liefert die View zurück, die dieses TochDown Ereignis angenommen hat
		GL_View_Base resultView = null;

		try
		{
			for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
			{
				// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
				GL_View_Base view = iterator.next();

				// Invisible Views can not be clicked!
				if (view == null || !view.isVisible()) continue;
				if (view.contains(x, y))
				{
					// touch innerhalb des Views
					// -> Klick an das View weitergeben
					lastTouchPos = new Vector2(x - view.Pos.x, y - view.Pos.y);
					resultView = view.touchDown(x - (int) view.Pos.x, y - (int) view.Pos.y, pointer, button);
				}

				if (resultView != null) break;
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			return null;
		}

		if (resultView == null)
		{
			// kein Klick in einem untergeordnetem View
			// -> hier behandeln
			boolean behandelt = onTouchDown(x, y, pointer, button);
			if (behandelt) resultView = this;
		}

		GL_Listener.glListener.renderOnce(this.getName() + " touchDown");

		return resultView;
	}

	public final boolean touchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht ï¿½berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown ï¿½bergeben!!!
		boolean behandelt = false;

		try
		{
			for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
			{
				GL_View_Base view = iterator.next();

				if (view != null && view.contains(x, y))
				{
					behandelt = view.touchDragged(x - (int) view.Pos.x, y - (int) view.Pos.y, pointer, KineticPan);
				}
				if (behandelt) break;
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			return false;
		}

		if (!behandelt)
		{
			// kein Klick in einem untergeordnetem View
			// -> hier behandeln
			behandelt = onTouchDragged(x, y, pointer, KineticPan);
		}
		return behandelt;
	}

	public final boolean touchUp(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht ï¿½berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown ï¿½bergeben!!!
		boolean behandelt = false;

		try
		{
			for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
			{
				GL_View_Base view = iterator.next();
				if (view != null && view.contains(x, y))
				{
					// touch innerhalb des Views
					// -> Klick an das View weitergeben
					behandelt = view.touchUp(x - (int) view.Pos.x, y - (int) view.Pos.y, pointer, button);
				}

				if (behandelt) break;
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			return false;
		}

		if (!behandelt)
		{
			// kein Klick in einem untergeordnetem View
			// -> hier behandeln
			behandelt = onTouchUp(x, y, pointer, button);
		}

		return behandelt;
	}

	// die untergeordneten Klassen mï¿½ssen diese Event-Handler ï¿½berschreiben!!!
	// public abstract boolean onClick(int x, int y, int pointer, int button);

	public abstract boolean onLongClick(int x, int y, int pointer, int button);

	public abstract boolean onTouchDown(int x, int y, int pointer, int button);

	public abstract boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan);

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
	 * Interface definition for a callback to be invoked when a view is clicked.
	 */
	public interface OnLongClickListener
	{
		/**
		 * Called when a view has been Longclicked.
		 * 
		 * @param v
		 *            The view that was clicked.
		 */
		boolean onLongClick(GL_View_Base v, int x, int y, int pointer, int button);
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

	/**
	 * Register a callback to be invoked when this view is clicked. If this view is not clickable, it becomes clickable.
	 * 
	 * @param l
	 *            The callback that will run
	 * @see #setClickable(boolean)
	 */
	public void setOnLongClickListener(OnLongClickListener l)
	{
		if (!isClickable)
		{
			isClickable = true;
		}
		mOnLongClickListener = l;
	}

	public boolean isClickable()
	{
		return isClickable;
	}

	/**
	 * Setzt dieses View Clicable mit der ï¿½bergabe von True. </br> Wenn Dieses View nicht Clickable ist, werden auch keine Click-Abfragen
	 * an die Childs weitergegeben.
	 * 
	 * @param value
	 */
	public void setClickable(boolean value)
	{
		isClickable = value;
	}

	public void setBackground(Drawable background)
	{
		drawableBackground = background;
	}

	public Drawable getBackground()
	{
		return drawableBackground;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public void setY(float i)
	{
		if (this.getY() == i) return;
		super.setY(i);
		this.invalidate(); // Scissor muss neu berechnet werden
		GL_Listener.glListener.renderOnce(this.getName() + " setY");

	}

	@Override
	public void setX(float i)
	{
		if (this.getX() == i) return;
		super.setX(i);
		this.invalidate(); // Scissor muss neu berechnet werden
		GL_Listener.glListener.renderOnce(this.getName() + " setX");
	}

	@Override
	public void setPos(Vector2 Pos)
	{
		if (this.getPos().x == Pos.x && this.getPos().y == Pos.y) return;
		super.setPos(Pos);
		this.invalidate(); // Scissor muss neu berechnet werden
		GL_Listener.glListener.renderOnce(this.getName() + " setPos(Vector)");
	}

	public void setZeroPos()
	{
		super.setPos(new Vector2(0, 0));
		this.invalidate(); // Scissor muss neu berechnet werden
		GL_Listener.glListener.renderOnce(this.getName() + " setZeroPos");
	}

	public void setPos(float x, float y)
	{
		super.setPos(x, y);
		this.invalidate(); // Scissor muss neu berechnet werden
		GL_Listener.glListener.renderOnce(this.getName() + " setPos(float)");
	}

	// Abfrage der clickToleranz, mit der Bestimmt wird ab welcher Bewegung ein onTouchDragged erzeugt wird und beim loslassen kein click
	// dies kann hier für einzelne Views unabhängig bestimmt werden
	public int getClickTolerance()
	{
		// wenn eine View clickable ist dann muß für die Verschiebung (onTouchDragged) ein gewisser Toleranzbereich definiert werden,
		// innerhalb dem erstmal kein onTouchDragged aufgerufen wird
		if (isClickable) return UiSizes.getClickToleranz();
		else
			// Wenn aber eine View nicht clickable ist dann darf der onTouchDragged sofort aufgerufen werden
			return 1;
	}

	// ############# Skin changed ################

	private interface skinChangedEventListner
	{
		public void SkinChanged();
	}

	private static ArrayList<skinChangedEventListner> skinChangedEventList = new ArrayList<GL_View_Base.skinChangedEventListner>();

	public void registerSkinChangedEvent()
	{
		if (calling) return;
		// synchronized (skinChangedEventList)
		// {
		skinChangedEventList.add(listner);
		// }
	}

	private static boolean calling = false;

	public static void CallSkinChanged()
	{
		// synchronized (skinChangedEventList)
		// {
		calling = true;
		for (skinChangedEventListner listner : skinChangedEventList)
		{
			if (listner != null) listner.SkinChanged();
		}
		calling = false;
		// }
	}

	protected skinChangedEventListner listner = new skinChangedEventListner()
	{

		@Override
		public void SkinChanged()
		{
			SkinIsChanged();
		}
	};

	protected abstract void SkinIsChanged();

	// ############# End Skin changed ############

}
