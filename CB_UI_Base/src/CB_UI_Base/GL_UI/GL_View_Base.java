package CB_UI_Base.GL_UI;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Util.MoveableList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public abstract class GL_View_Base extends CB_RectF
{

	// # CONSTANTS

	/**
	 * Pointer ID for Mouse wheel scrolling up
	 */
	public static final int MOUSE_WHEEL_POINTER_UP = -280272;

	/**
	 * Pointer ID for Mouse wheel scrolling down
	 */
	public static final int MOUSE_WHEEL_POINTER_DOWN = -280273;

	public static boolean debug = false;
	public static boolean disableScissor = false;

	public boolean withoutScissor = false;

	// # private Member
	protected String name = "";

	protected Drawable drawableBackground;

	/**
	 * Enthaelt alle GL_Views innerhalb dieser Gl_View
	 */
	protected final MoveableList<GL_View_Base> childs = new MoveableList<GL_View_Base>();

	protected OnClickListener mOnClickListener;
	protected OnClickListener mOnLongClickListener;
	protected OnClickListener mOnDoubleClickListener;

	private Pixmap debugRegPixmap = null;
	private Texture debugRegTexture = null;
	private Sprite DebugSprite = null;

	/**
	 * Don't use this Flag direct, use the method isClickable() </br></br> Maby a child is clickable!!
	 */
	private boolean isClickable = false;
	private boolean isLongClickable = false;
	private boolean isDoubleClickable = false;

	private boolean ChildIsClickable = false;
	private boolean ChildIsLongClickable = false;
	private boolean ChildIsDoubleClickable = false;

	protected boolean onTouchUp = false;
	protected boolean onTouchDown = false;
	public Vector2 lastTouchPos;

	private boolean mVisible = true;

	protected GL_View_Base parent;
	protected static int nDepthCounter = 0;

	private boolean enabled = true;

	protected float Weight = 1f;

	// # Constructors

	public GL_View_Base(String Name)
	{
		super();
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
		name = Name;

	}

	public GL_View_Base(float X, float Y, float Width, float Height, GL_View_Base Parent, String Name)
	{
		super(X, Y, Width, Height);
		parent = Parent;
		name = Name;

	}

	public GL_View_Base(CB_RectF rec, String Name)
	{
		super(rec);
		name = Name;

	}

	public GL_View_Base(CB_RectF rec, GL_View_Base Parent, String Name)
	{
		super(rec);
		parent = Parent;
		name = Name;

	}

	public GL_View_Base(SizeF size, String Name)
	{
		super(0, 0, size.width, size.height);
		name = Name;

	}

	public void setVisible(boolean On)
	{
		if (On)
		{
			setVisible();
		}
		else
		{
			setInvisible();
		}
	}

	public void setVisible()
	{
		if (mVisible) return;
		mVisible = true;
		GL.that.renderOnce(this.getName() + "setVisibility");
	}

	public void setInvisible()
	{
		if (!mVisible) return;
		mVisible = false;
		GL.that.renderOnce(this.getName() + "setVisibility");
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
	private boolean getVisibility()
	{
		if (this.getWidth() <= 0f || this.getHeight() <= 0f) return false;
		return mVisible;
	}

	public boolean isVisible()
	{
		return getVisibility();
	}

	public GL_View_Base addChild(final GL_View_Base view)
	{
		return addChild(view, false);
	}

	public GL_View_Base addChild(final GL_View_Base view, final boolean last)
	{
		GL.that.RunOnGL(new IRunOnGL()
		{
			@Override
			public void run()
			{
				if (last)
				{
					childs.add(0, view);
				}
				else
				{
					childs.add(view);
				}
				chkChildClickable();
			}
		});

		return view;
	}

	public void removeChild(final GL_View_Base view)
	{
		GL.that.RunOnGL(new IRunOnGL()
		{
			@Override
			public void run()
			{
				try
				{
					if (childs != null && childs.size() > 0) childs.remove(view);
				}
				catch (Exception e)
				{
				}
				chkChildClickable();
			}
		});
	}

	public void removeChilds()
	{
		GL.that.RunOnGL(new IRunOnGL()
		{
			@Override
			public void run()
			{
				try
				{
					if (childs != null && childs.size() > 0) childs.clear();
				}
				catch (Exception e)
				{
				}
				chkChildClickable();
			}
		});
	}

	public void removeChilds(final MoveableList<GL_View_Base> Childs)
	{
		GL.that.RunOnGL(new IRunOnGL()
		{
			@Override
			public void run()
			{
				try
				{
					if (childs != null && childs.size() > 0) childs.remove(Childs);
				}
				catch (Exception e)
				{
				}
				chkChildClickable();
			}
		});
	}

	/**
	 * Checks whether any child has the status Clickable. </br>If so, then this view must also Clickable!
	 */
	private void chkChildClickable()
	{
		boolean tmpClickable = false;
		boolean tmpDblClickable = false;
		boolean tmpLongClickable = false;
		if (childs != null)
		{

			try
			{
				for (GL_View_Base tmp : childs)
				{
					if (tmp != null)
					{
						if (tmp.isClickable()) tmpClickable = true;
						if (tmp.isLongClickable()) tmpLongClickable = true;
						if (tmp.isDblClickable()) tmpDblClickable = true;
					}

				}
			}
			catch (Exception e)
			{
			}
		}

		ChildIsClickable = tmpClickable;
		ChildIsDoubleClickable = tmpDblClickable;
		ChildIsLongClickable = tmpLongClickable;
	}

	protected float leftBorder = 0;
	protected float rightBorder = 0;
	protected float topBorder = 0;
	protected float bottomBorder = 0;
	protected float innerWidth = getWidth();
	protected float innerHeight = getHeight();

	/**
	 ** setting the drawableBackground and changes the Borders (do own Borders afterwards)
	 **/
	public void setBackground(Drawable background)
	{
		drawableBackground = background;
		if (background != null)
		{
			leftBorder = background.getLeftWidth();
			rightBorder = background.getRightWidth();
			topBorder = background.getTopHeight();
			bottomBorder = background.getBottomHeight(); // this.BottomHeight;
		}
		else
		{
			leftBorder = 0;
			rightBorder = 0;
			topBorder = 0;
			bottomBorder = 0; // this.BottomHeight;
		}
		innerWidth = getWidth() - leftBorder - rightBorder;
		innerHeight = getHeight() - topBorder - bottomBorder;
	}

	/**
	 ** no borders to use on this (page), if you want
	 **/
	public void setNoBorders()
	{
		leftBorder = 0f;
		rightBorder = 0f;
		innerWidth = getWidth();
	}

	/**
	 ** setting the borders to use on this (page), if you want
	 **/
	public void setBorders(float l, float r)
	{
		leftBorder = l;
		rightBorder = r;
		innerWidth = getWidth() - l - r;
	}

	public Drawable getBackground()
	{
		return drawableBackground;
	}

	public float getLeftWidth()
	{
		return leftBorder;
	}

	public float getRightWidth()
	{
		return rightBorder;
	}

	public float getTopHeight()
	{
		return topBorder;
	}

	public float getBottomHeight()
	{
		return bottomBorder;
	}

	/**
	 ** get available width (not filled with objects)
	 **/
	public float getInnerWidth()
	{
		return innerWidth;
	}

	/**
	 ** get available height (not filled with objects)
	 **/
	public float getInnerHeight()
	{
		return innerHeight;
	}

	/**
	 * Die renderChilds() Methode wird vom GL_Listner bei jedem Render-Vorgang aufgerufen. Hier wird dann zuerst die render() Methode dieser
	 * View aufgerufen. Danach werden alle Childs iteriert und dessen renderChilds() Methode aufgerufen, wenn die View sichtbar ist
	 * (Visibility).
	 * 
	 * @param batch
	 */
	public void renderChilds(final Batch batch, ParentInfo parentInfo)
	{

		if (thisInvalidate)
		{
			myParentInfo = parentInfo;
			CalcMyInfoForChild();
		}

		if (!withoutScissor)
		{
			if (intersectRec.getHeight() + 1 < 0 || intersectRec.getWidth() + 1 < 0) return; // hier gibt es nichts zu rendern
			if (!disableScissor) Gdx.gl.glEnable(GL10.GL_SCISSOR_TEST);
			Gdx.gl.glScissor((int) intersectRec.getX(), (int) intersectRec.getY(), (int) intersectRec.getWidth() + 1,
					(int) intersectRec.getHeight() + 1);
		}

		float A = 0, R = 0, G = 0, B = 0; // Farbwerte der batch um diese wieder einzustellen, wenn ein ColorFilter angewandt wurde!

		boolean ColorFilterSeted = false; // Wir benutzen hier dieses Boolean um am ende dieser Methode zu entscheiden, ob wir die alte
											// Farbe des Batches wieder herstellen müssen. Wir verlassen uns hier nicht darauf, das
											// mColorFilter!= null ist, da dies in der zwichenzeit passiert sein kann.

		// Set Colorfilter ?
		if (mColorFilter != null)
		{
			ColorFilterSeted = true;
			// zuerst alte Farbe abspeichern, um sie Wieder Herstellen zu können
			// hier muss jeder Wert einzeln abgespeichert werden, da bei getColor()
			// nur eine Referenz zurück gegeben wird
			Color c = batch.getColor();
			A = c.a;
			R = c.r;
			G = c.g;
			B = c.b;

			batch.setColor(mColorFilter);
		}

		// first Draw Background?

		if (drawableBackground != null)
		{
			drawableBackground.draw(batch, 0, 0, getWidth(), getHeight());
		}

		// set rotation
		boolean isRotated = false;

		if (mRotate != 0 || mScale != 1)
		{
			isRotated = true;

			Matrix4 matrix = new Matrix4();

			matrix.idt();
			matrix.translate(mOriginX, mOriginY, 0);
			matrix.rotate(0, 0, 1, mRotate);
			matrix.scale(mScale, mScale, 1);
			matrix.translate(-mOriginX, -mOriginY, 0);

			batch.setTransformMatrix(matrix);
		}

		this.render(batch);

		// reverse rotation
		if (isRotated)
		{
			Matrix4 matrix = new Matrix4();

			matrix.idt();
			matrix.rotate(0, 0, 1, 0);
			matrix.scale(1, 1, 1);

			batch.setTransformMatrix(matrix);
		}

		if (childs != null && childs.size() > 0)
		{
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
					if (view != null && view.isVisible())
					{

						if (childsInvalidate) view.invalidate();

						ParentInfo myInfoForChild = myParentInfo.cpy();
						myInfoForChild.setWorldDrawRec(intersectRec);

						myInfoForChild.add(view.getX(), view.getY());

						batch.setProjectionMatrix(myInfoForChild.Matrix());
						nDepthCounter++;

						view.renderChilds(batch, myInfoForChild);
						nDepthCounter--;
						// batch.setProjectionMatrix(myParentInfo.Matrix());
					}

				}
				catch (java.util.NoSuchElementException e)
				{
					break; // da die Liste nicht mehr gültig ist, brechen wir hier den Iterator ab
				}
				catch (java.util.ConcurrentModificationException e)
				{
					break; // da die Liste nicht mehr gültig ist, brechen wir hier den Iterator ab
				}
			}

			// }
			childsInvalidate = false;
		}

		// Draw Debug REC
		if (debug)
		{

			if (DebugSprite != null)
			{
				batch.flush();
				DebugSprite.draw(batch);

			}

		}

		// reset Colorfilter ?
		if (ColorFilterSeted)
		{
			// alte abgespeicherte Farbe des Batches wieder herstellen!
			batch.setColor(R, G, B, A);

		}

	}

	private void writeDebug()
	{
		if (DebugSprite == null)
		{
			int w = getNextHighestPO2((int) getWidth());
			int h = getNextHighestPO2((int) getHeight());
			debugRegPixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
			debugRegPixmap.setColor(1f, 0f, 0f, 1f);
			debugRegPixmap.drawRectangle(1, 1, (int) getWidth() - 1, (int) getHeight() - 1);

			debugRegTexture = new Texture(debugRegPixmap, Pixmap.Format.RGBA8888, false);

			DebugSprite = new Sprite(debugRegTexture, (int) getWidth(), (int) getHeight());

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
	protected boolean childsInvalidate = false;
	private boolean thisInvalidate = true;

	public CB_RectF getWorldRec()
	{
		if (ThisWorldRec == null) return new CB_RectF();
		return ThisWorldRec.copy();
	}

	/**
	 * Berechnet das Scissor Rechteck und die Infos fuer die Childs immer dann wenn sich etwas an Position oder Groesse dieses GL_View_Base
	 * geaendert hat. Wenn sich etwas geaendert hat, wird auch ein Invalidate an die Childs uebergeben, da diese auch neu berechnet werden
	 * muessen. Die detection wann sich etwas geaendert hat, kommt von der ueberschriebenen CB_RectF Methode CalcCrossPos, da diese bei
	 * jeder Aenderung aufgerufen wird.
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

	protected abstract void render(Batch batch);

	// ########################
	// Rotate Property
	// ########################
	protected float mRotate = 0;
	protected float mOriginX;
	protected float mOriginY;
	protected float mScale = 1f;

	public void setRotate(float Rotate)
	{
		mRotate = Rotate;
	}

	public void setOrigin(float originX, float originY)
	{
		mOriginX = originX;
		mOriginY = originY;
	}

	public void setOriginCenter()
	{
		mOriginX = this.getHalfWidth();
		mOriginY = this.getHalfHeight();
	}

	/**
	 * setzt den Scale Factor des dargestellten Images, wobei die Größe nicht verändert wird. Ist das Image größer, wird es abgeschnitten
	 * 
	 * @param value
	 */
	public void setScale(float value)
	{
		mScale = value;
	}

	@Override
	public void resize(float width, float height)
	{
		try
		{
			innerWidth = width - leftBorder - rightBorder;
			innerHeight = height - topBorder - bottomBorder;
			onResized(this);
		}
		catch (Exception e1)
		{
			int i = 0;
			i = i + 1;
		}
		DebugSprite = null;

		// Eine Größenänderung an die Childs Melden
		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
				{
					// alle renderChilds() der in dieser GL_View_Base
					// enthaltenen Childs auf rufen.
					GL_View_Base view = iterator.next();
					if (view != null) view.onParentRezised(this);
				}
			}
			catch (java.util.NoSuchElementException e)
			{
				// do nothing
			}
			catch (ConcurrentModificationException e)
			{
				// do nothing
			}
		}
	}

	public abstract void onResized(CB_RectF rec);

	public abstract void onParentRezised(CB_RectF rec);

	public void onShow()
	{
		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
				{
					// alle renderChilds() der in dieser GL_View_Base
					// enthaltenen Childs auf rufen.
					GL_View_Base view = iterator.next();
					if (view != null) view.onShow();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	public void onHide()
	{
		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
				{
					// alle renderChilds() der in dieser GL_View_Base
					// enthaltenen Childs auf rufen.
					GL_View_Base view = iterator.next();
					if (view != null) view.onHide();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

	}

	public void onStop()
	{
		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
				{
					// alle renderChilds() der in dieser GL_View_Base
					// enthaltenen Childs auf rufen.
					GL_View_Base view = iterator.next();
					view.onStop();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public boolean click(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht ï¿½berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown ï¿½bergeben!!!
		boolean behandelt = false;
		try
		{
			if (childs != null && childs.size() > 0)
			{
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
						behandelt = view.click(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
					}
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
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return behandelt;
	}

	public boolean doubleClick(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht ï¿½berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown ï¿½bergeben!!!
		boolean behandelt = false;
		try
		{
			if (childs != null && childs.size() > 0)
			{
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
						behandelt = view.doubleClick(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
					}
				}
			}
			if (!behandelt)
			{
				// kein Klick in einem untergeordnetem View
				// -> hier behandeln
				if (mOnDoubleClickListener != null)
				{
					behandelt = mOnDoubleClickListener.onClick(this, x, y, pointer, button);
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return behandelt;
	}

	public boolean longClick(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht ï¿½berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown ï¿½bergeben!!!
		boolean behandelt = false;

		try
		{
			if (childs != null && childs.size() > 0)
			{
				for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
				{
					// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
					GL_View_Base view = iterator.next();

					if (view == null || !view.isClickable()) continue;

					if (view.contains(x, y))
					{
						// touch innerhalb des Views
						// -> Klick an das View weitergeben
						behandelt = view.longClick(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
					}
				}
			}
			if (!behandelt)
			{
				// kein Klick in einem untergeordnetem View
				// -> hier behandeln
				if (mOnLongClickListener != null)
				{
					behandelt = mOnLongClickListener.onClick(this, x, y, pointer, button);
				}

			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return behandelt;
	}

	public final GL_View_Base touchDown(int x, int y, int pointer, int button)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht ï¿½berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown ï¿½bergeben!!!
		// touchDown liefert die View zurück, die dieses TochDown Ereignis angenommen hat
		GL_View_Base resultView = null;

		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
				{
					// Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
					GL_View_Base view = iterator.next();

					// Invisible Views can not be clicked!
					if (view == null || !view.isVisible()) continue;
					if (!view.getEnabled()) continue;
					if (view.contains(x, y))
					{
						// touch innerhalb des Views
						// -> Klick an das View weitergeben
						lastTouchPos = new Vector2(x - view.getX(), y - view.getY());
						resultView = view.touchDown(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
					}

					if (resultView != null) break;
				}
			}
			catch (IndexOutOfBoundsException e)
			{
				e.printStackTrace();
				return null;
			}
		}

		if (resultView == null)
		{
			// kein Klick in einem untergeordnetem View
			// -> hier behandeln
			boolean behandelt = onTouchDown(x, y, pointer, button);
			if (behandelt) resultView = this;
		}

		GL.that.renderOnce(this.getName() + " touchDown");
		return resultView;
	}

	public final boolean touchDragged(int x, int y, int pointer, boolean KineticPan)
	{
		// Achtung: dieser touchDown ist nicht virtual und darf nicht ï¿½berschrieben werden!!!
		// das Ereignis wird dann in der richtigen View an onTouchDown ï¿½bergeben!!!
		boolean behandelt = false;

		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
				{
					GL_View_Base view = iterator.next();

					if (view != null && view.contains(x, y))
					{
						behandelt = view.touchDragged(x - (int) view.getX(), y - (int) view.getY(), pointer, KineticPan);
					}
					if (behandelt) break;
				}
			}
			catch (IndexOutOfBoundsException e)
			{
				e.printStackTrace();
				return false;
			}
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

		if (childs != null && childs.size() > 0)
		{
			try
			{
				for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext();)
				{
					GL_View_Base view = iterator.next();
					if (view != null && view.contains(x, y))
					{
						// touch innerhalb des Views
						// -> Klick an das View weitergeben
						behandelt = view.touchUp(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
					}

					if (behandelt) break;
				}
			}
			catch (IndexOutOfBoundsException e)
			{
				e.printStackTrace();
				return false;
			}
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

	public void dispose()
	{
		DebugSprite = null;

		try
		{
			if (debugRegTexture != null) debugRegTexture.dispose();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			if (debugRegPixmap != null) debugRegPixmap.dispose();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		debugRegPixmap = null;
		debugRegTexture = null;
	}

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
		isClickable = l != null;
		mOnClickListener = l;
	}

	public OnClickListener getOnClickListner()
	{
		return mOnClickListener;
	}

	public OnClickListener getOnLongClickListner()
	{
		return mOnLongClickListener;
	}

	public OnClickListener getOnDblClickListner()
	{
		return mOnDoubleClickListener;
	}

	/**
	 * Register a callback to be invoked when this view is long clicked. If this view is not clickable, it becomes clickable.
	 * 
	 * @param l
	 *            The callback that will run
	 * @see #setClickable(boolean)
	 */
	public void setOnLongClickListener(OnClickListener l)
	{
		isLongClickable = l != null;
		mOnLongClickListener = l;
	}

	/**
	 * Register a callback to be invoked when this view is double clicked. If this view is not clickable, it becomes clickable.
	 * 
	 * @param l
	 *            The callback that will run
	 * @see #setClickable(boolean)
	 */
	public void setOnDoubleClickListener(OnClickListener l)
	{
		isDoubleClickable = l != null;
		mOnDoubleClickListener = l;
	}

	public boolean isDblClickable()
	{
		if (!this.isVisible()) return false;
		return isDoubleClickable | ChildIsDoubleClickable;
	}

	public boolean isLongClickable()
	{
		if (!this.isVisible()) return false;
		return isLongClickable | ChildIsLongClickable;
	}

	public boolean isClickable()
	{
		if (!this.isVisible()) return false;
		return isClickable | ChildIsClickable;
	}

	/**
	 * Setzt dieses View Clicable mit der Uebergabe von True. Wenn dieses View nicht Clickable ist, werden auch keine Click-Abfragen an die
	 * Childs weitergegeben.
	 * 
	 * @param value
	 */
	public void setClickable(boolean value)
	{
		isClickable = value;
	}

	public void setLongClickable(boolean value)
	{
		isLongClickable = value;
	}

	public void setDoubleClickable(boolean value)
	{
		isDoubleClickable = value;
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
		GL.that.renderOnce(this.getName() + " setY");
	}

	@Override
	public void setX(float i)
	{
		if (this.getX() == i) return;
		super.setX(i);
		this.invalidate(); // Scissor muss neu berechnet werden
		GL.that.renderOnce(this.getName() + " setX");
	}

	@Override
	public void setPos(Vector2 Pos)
	{
		if (this.getX() == Pos.x && this.getY() == Pos.y) return;
		super.setPos(Pos);
		this.invalidate(); // Scissor muss neu berechnet werden
		GL.that.renderOnce(this.getName() + " setPos(Vector)");
	}

	public void setZeroPos()
	{
		super.setPos(new Vector2(0, 0));
		this.invalidate(); // Scissor muss neu berechnet werden
		GL.that.renderOnce(this.getName() + " setZeroPos");
	}

	@Override
	public void setPos(float x, float y)
	{
		super.setPos(x, y);
		this.invalidate(); // Scissor muss neu berechnet werden
		GL.that.renderOnce(this.getName() + " setPos(float)");
	}

	// Abfrage der clickToleranz, mit der Bestimmt wird ab welcher Bewegung ein onTouchDragged erzeugt wird und beim loslassen kein click
	// dies kann hier für einzelne Views unabhängig bestimmt werden
	public int getClickTolerance()
	{
		// wenn eine View clickable ist dann muß für die Verschiebung (onTouchDragged) ein gewisser Toleranzbereich definiert werden,
		// innerhalb dem erstmal kein onTouchDragged aufgerufen wird
		if (isClickable()) return UI_Size_Base.that.getClickToleranz();
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

	private Color mColorFilter = null;

	public void setColorFilter(Color color)
	{
		mColorFilter = color;
	}

	public void clearColorFilter()
	{
		mColorFilter = null;
	}

	public Color getColorFilter()
	{
		return mColorFilter;
	}

	public void setEnabled(boolean value)
	{
		enabled = value;
	}

	public boolean getEnabled()
	{
		return enabled;
	}

	@Override
	protected void calcCrossCorner()
	{
		super.calcCrossCorner();
		thisInvalidate = true;
	}

	private Object data = null;

	public void setData(Object data)
	{
		this.data = data;
	}

	public Object getData()
	{
		return data;
	}

}
