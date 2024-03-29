/*
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.MoveableList;
import de.droidcachebox.utils.log.Log;

public class GL_View_Base extends CB_RectF {
    protected static final int MOUSE_WHEEL_POINTER_UP = -280272;
    protected static final int MOUSE_WHEEL_POINTER_DOWN = -280273;
    private static final String sClass = "GL_View_Base";
    private static final ArrayList<SkinChangedEventListener> skinChangedEventList = new ArrayList<>();
    public static boolean debug = false;
    public static boolean disableScissor = false;
    protected static int nDepthCounter = 0;
    private static boolean calling = false;
    protected final MoveableList<GL_View_Base> childs;
    protected final ParentInfo myInfoForChild;
    private final Matrix4 rotateMatrix;
    private final SkinChangedEventListener mSkinChangedEventListener;
    public boolean withoutScissor;
    public CB_RectF thisWorldRec;
    public ParentInfo myParentInfo;
    protected boolean isRenderInitDone;
    protected Pixmap debugRegPixmap;
    protected Texture debugRegTexture;
    protected Vector2 lastTouchPos;
    protected CB_RectF intersectRec;
    protected String name;
    protected Drawable drawableBackground;
    protected OnClickListener mOnClickListener;
    protected Sprite debugSprite;
    protected boolean onTouchUp;
    protected boolean onTouchDown;
    protected GL_View_Base parent;
    protected float leftBorder;
    protected float rightBorder;
    protected float topBorder;
    protected float bottomBorder;
    protected float innerWidth;
    protected float innerHeight;
    protected boolean childsInvalidate;
    protected boolean thisInvalidate;
    protected float mScale;
    protected Object data;
    protected float weight;
    protected int elementAlignment = 0; // bottom
    protected boolean isDisposed;
    private OnClickListener mOnLongClickListener;
    private OnClickListener mOnDoubleClickListener;
    private float mRotate = 0;
    private float mOriginX;
    private float mOriginY;
    private Color mColorFilter;
    private boolean forceHandleTouchEvents;
    private boolean isClickable;
    private boolean isLongClickable;
    private boolean isDoubleClickable;
    private boolean hasClickableChild;
    private boolean hasLongClickableChild;
    private boolean hasDoubleClickableChild;
    private boolean mVisible;
    private boolean enabled;

    public GL_View_Base(CB_RectF rec, GL_View_Base parent, String name) {
        super(rec);
        this.parent = parent;
        this.name = name;
        innerWidth = getWidth();
        innerHeight = getHeight();
        childs = new MoveableList<>();
        rotateMatrix = new Matrix4();
        myInfoForChild = new ParentInfo();
        withoutScissor = false;
        thisWorldRec = new CB_RectF();
        myParentInfo = new ParentInfo();
        debugRegPixmap = null;
        debugRegTexture = null;
        intersectRec = new CB_RectF();
        debugSprite = null;
        onTouchUp = false;
        onTouchDown = false;
        leftBorder = 0;
        rightBorder = 0;
        topBorder = 0;
        bottomBorder = 0;
        childsInvalidate = false;
        thisInvalidate = true;
        mScale = 1f;
        data = null;
        weight = 1f;
        mSkinChangedEventListener = GL_View_Base.this::skinIsChanged;
        mColorFilter = null;
        forceHandleTouchEvents = false;
        isClickable = false;
        isLongClickable = false;
        isDoubleClickable = false;
        hasClickableChild = false;
        hasLongClickableChild = false;
        hasDoubleClickableChild = false;
        mVisible = true;
        enabled = true;
        isDisposed = false;
        isRenderInitDone = false;
    }

    public GL_View_Base(CB_RectF rec, String name) {
        this(rec, null, name);
    }

    public GL_View_Base(float x, float y, float width, float height, GL_View_Base parent, String name) {
        this(new CB_RectF(x, y, width, height), parent, name);
    }

    public GL_View_Base(String name) {
        this(new CB_RectF(), null, name);
    }

    public GL_View_Base(float x, float y, float width, float height, String name) {
        this(new CB_RectF(x, y, width, height), null, name);
    }

    public GL_View_Base(SizeF size, String name) {
        this(new CB_RectF(0, 0, size.getWidth(), size.getHeight()), null, name);
    }

    protected static void fireSkinChanged() {
        calling = true;
        for (SkinChangedEventListener listener : skinChangedEventList) {
            if (listener != null)
                listener.handleSkinChanged();
        }
        calling = false;
    }

    protected void setForceHandleTouchEvents() {
        forceHandleTouchEvents = true;
    }

    public void setVisible() {
        if (mVisible)
            return;
        mVisible = true;
        GL.that.renderOnce();
    }

    public void setInvisible() {
        if (!mVisible)
            return;
        mVisible = false;
        GL.that.renderOnce();
    }

    public MoveableList<GL_View_Base> getchilds() {
        return childs;
    }

    /**
     * Gibt die Parent View zurück, wenn diese über den Constructor übergeben wurde!
     *
     * @return parent View oder null
     */
    public GL_View_Base getParent() {
        return parent;
    }

    /**
     * Returns TRUE if with and height >0, is not disposed and is not set to invisible
     */
    public boolean isVisible() {
        if (isDisposed)
            return false;
        if (getWidth() <= 0f || getHeight() <= 0f)
            return false;
        return mVisible;
    }

    public void setVisible(boolean On) {
        if (On) {
            setVisible();
        } else {
            setInvisible();
        }
    }

    public GL_View_Base addChild(final GL_View_Base view) {
        return addChild(view, false);
    }

    public GL_View_Base addChild(final GL_View_Base view, final boolean last) {
        if (childs.contains(view))
            return view;
        GL.that.runOnGLWithThreadCheck(() -> {
            if (last) {
                childs.add(0, view);
            } else {
                childs.add(view);
            }
            chkChildClickable();
        });

        return view;
    }

    public void removeChild(final GL_View_Base child) {
        GL.that.runOnGLWithThreadCheck(() -> {
            try {
                if (childs != null && childs.size() > 0)
                    childs.remove(child);
            } catch (Exception ignored) {
            }
            chkChildClickable();
        });
    }

    public void removeChildren() {
        GL.that.runOnGLWithThreadCheck(() -> {
            try {
                if (childs != null && childs.size() > 0)
                    childs.clear();
            } catch (Exception ignored) {
            }
            chkChildClickable();
        });
    }

    public void removeChildren(final MoveableList<GL_View_Base> Childs) {
        GL.that.runOnGLWithThreadCheck(() -> {
            try {
                if (childs != null && childs.size() > 0)
                    childs.remove(Childs);
            } catch (Exception ignored) {
            }
            chkChildClickable();
        });
    }

    public int getChildCount() {
        if (childs == null)
            return -1;
        synchronized (childs) {
            return childs.size();
        }
    }

    public GL_View_Base addChildDirect(final GL_View_Base view) {
        if (childs == null || view == null)
            return null;
        synchronized (childs) {
            if (!childs.contains(view))
                childs.add(view);
        }

        return view;
    }

    public GL_View_Base addChildDirectLast(final GL_View_Base view) {
        if (childs == null || view == null)
            return null;
        synchronized (childs) {
            if (!childs.contains(view))
                childs.add(0, view);
        }
        return view;
    }

    public void removeChildrenDirect() {
        if (childs == null)
            return;
        synchronized (childs) {
            childs.clear();
        }
    }

    public void removeChildDirect(GL_View_Base view) {
        if (childs == null || view == null)
            return;
        synchronized (childs) {
            try {
                if (childs.contains(view))
                    childs.remove(view);
            } catch (Exception ignored) {
            }
        }
    }

    public void removeChildrenDirect(MoveableList<GL_View_Base> children) {
        if (childs == null)
            return;
        synchronized (childs) {
            try {
                childs.remove(children);
            } catch (Exception ignored) {
            }
        }
    }

    public GL_View_Base getChild(int i) {
        if (childs == null)
            return null;
        synchronized (childs) {
            if (childs.size() < i || childs.size() == 0)
                return null;
            return childs.get(i);
        }
    }

    /**
     * Checks whether any child has the status Clickable. </br>If so, then this view is also Clickable!
     */
    protected void chkChildClickable() {
        boolean tmpClickable = false;
        boolean tmpDoubleClickable = false;
        boolean tmpLongClickable = false;
        if (childs != null) {
            try {
                for (GL_View_Base tmp : childs                     ) {
                    if (tmp != null) {
                        if (tmp.isClickable()) {
                            tmpClickable = true;
                        }
                        if (tmp.isLongClickable()) {
                            tmpLongClickable = true;
                        }
                        if (tmp.isDoubleClickable()) {
                            tmpDoubleClickable = true;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        hasClickableChild = tmpClickable;
        hasDoubleClickableChild = tmpDoubleClickable;
        hasLongClickableChild = tmpLongClickable;
    }

    /**
     * * no borders to use on this (page), if you want
     **/
    public void setNoBorders() {
        leftBorder = 0f;
        rightBorder = 0f;
        innerWidth = getWidth();
    }

    /**
     * * setting the borders to use on this (page), if you want
     **/
    public void setBorders(float l, float r) {
        leftBorder = l;
        rightBorder = r;
        innerWidth = getWidth() - l - r;
    }

    public Drawable getBackground() {
        return drawableBackground;
    }

    /**
     * * setting the drawableBackground and changes the Borders (do own Borders afterwards)
     **/
    public void setBackground(Drawable background) {
        if (isDisposed)
            return;
        drawableBackground = background;
        if (background != null) {
            leftBorder = background.getLeftWidth();
            rightBorder = background.getRightWidth();
            topBorder = background.getTopHeight();
            bottomBorder = background.getBottomHeight(); // BottomHeight;
        } else {
            leftBorder = 0;
            rightBorder = 0;
            topBorder = 0;
            bottomBorder = 0; // BottomHeight;
        }
        innerWidth = getWidth() - leftBorder - rightBorder;
        innerHeight = getHeight() - topBorder - bottomBorder;
    }

    public float getLeftWidth() {
        return leftBorder;
    }

    public float getRightWidth() {
        return rightBorder;
    }

    public float getTopHeight() {
        return topBorder;
    }

    public float getBottomHeight() {
        return bottomBorder;
    }

    /**
     * * get available width (not filled with objects)
     **/
    public float getInnerWidth() {
        return innerWidth;
    }

    /**
     * Die renderChilds() Methode wird vom GL_Listener bei jedem Render-Vorgang aufgerufen.
     * Hier wird dann zuerst die render() Methode dieser View aufgerufen.
     * Danach werden alle Childs iteriert und deren renderChilds() Methode aufgerufen, wenn die View sichtbar ist (Visibility).
     */
    public void renderChildren(final Batch batch, ParentInfo parentInfo) {
        if (myParentInfo == null)
            return;

        if (isDisposed)
            return;

        if (thisInvalidate) {
            myParentInfo.setParentInfo(parentInfo);
            calcMyInfoForChild();
        }

        if (!withoutScissor) {
            if (intersectRec == null || intersectRec.getHeight() + 1 < 0 || intersectRec.getWidth() + 1 < 0)
                return; // hier gibt es nichts zu rendern
            if (!disableScissor)
                Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
            Gdx.gl.glScissor((int) intersectRec.getX(), (int) intersectRec.getY(), (int) intersectRec.getWidth() + 1, (int) intersectRec.getHeight() + 1);
        }

        Color savedColor = null;

        // batchHasColor to remember, if must reset the color at end of batch
        // mColorFilter != null may be changed in the meantime
        boolean batchHasColor = false;
        if (mColorFilter != null) {
            savedColor = new Color(batch.getColor());
            batchHasColor = true;
            batch.setColor(mColorFilter);
        }

        // first Draw Background?
        if (drawableBackground != null) {
            drawableBackground.draw(batch, 0, 0, getWidth(), getHeight());
        }

        // set rotation
        boolean isRotated = false;

        if (mRotate != 0 || mScale != 1) {
            isRotated = true;

            rotateMatrix.idt();
            rotateMatrix.translate(mOriginX, mOriginY, 0);
            rotateMatrix.rotate(0, 0, 1, mRotate);
            rotateMatrix.scale(mScale, mScale, 1);
            rotateMatrix.translate(-mOriginX, -mOriginY, 0);

            batch.setTransformMatrix(rotateMatrix);
        }

        try {
            render(batch);
        } catch (IllegalStateException e) {
            Log.err(sClass, "renderChilds", e);
            // reset Colorfilter ?
            if (batchHasColor) {
                // alte abgespeicherte Farbe des Batches wieder herstellen!
                batch.setColor(savedColor);
            }
            return;
        }

        // reverse rotation
        if (isRotated) {
            rotateMatrix.idt();
            batch.setTransformMatrix(rotateMatrix);
        }

        if (childs != null && childs.size() > 0) {
            for (int i = 0, n = childs.size(); i < n; i++) {

                if (i >= childs.size()) {
                    break; // ConcurrentModificationException
                }

                // alle renderChilds() der in dieser GL_View_Base
                // enthaltenen Childs auf rufen.

                try {
                    GL_View_Base view = childs.get(i);
                    // hier nicht view.render(batch) aufrufen, da sonnst die in der
                    // view enthaldenen Childs nicht aufgerufen werden.
                    try {
                        if (view != null && !view.isDisposed && view.isVisible()) {
                            synchronized (childs) {
                                if (childsInvalidate)
                                    view.invalidate();

                                myInfoForChild.setParentInfo(myParentInfo);
                                myInfoForChild.setWorldDrawRec(intersectRec);

                                myInfoForChild.add(view.getX(), view.getY());

                                batch.setProjectionMatrix(myInfoForChild.Matrix());
                                nDepthCounter++;
                                if (!view.isDisposed)
                                    view.renderChildren(batch, myInfoForChild);
                                nDepthCounter--;
                            }
                        } else {
                            if (view != null && view.isDisposed) {
                                // Remove disposedView from child list
                                removeChild(view);
                            }
                        }
                    } catch (java.lang.IllegalStateException e) {
                        if (view.isDisposed) {
                            // Remove disposedView from child list
                            removeChild(view);
                        }
                    }

                } catch (NoSuchElementException | ConcurrentModificationException | IndexOutOfBoundsException e) {
                    break; // on error
                }
            }
            childsInvalidate = false;
        }

        // Draw Debug REC
        if (debug) {
            if (debugSprite != null) {
                batch.flush();
                debugSprite.draw(batch);
            }
        }

        // reset Colorfilter ?
        if (batchHasColor) {
            // alte abgespeicherte Farbe des Batches wieder herstellen!
            batch.setColor(savedColor);
        }

    }

    protected void createDebugSprite() {
        if (debugSprite == null) {
            try {
                GL.that.runOnGLWithThreadCheck(() -> {
                    int w = (int) getWidth();
                    int h = (int) getHeight();
                    debugRegPixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                    debugRegPixmap.setColor(1f, 0f, 0f, 1f);
                    debugRegPixmap.drawRectangle(1, 1, w - 1, h - 1);
                    debugRegTexture = new Texture(debugRegPixmap, Pixmap.Format.RGBA8888, false);
                    debugSprite = new Sprite(debugRegTexture, w, h);
                });
            } catch (Exception ignored) {
            }
        }
    }

    public CB_RectF getWorldRec() {
        if (thisWorldRec == null)
            return new CB_RectF();
        return new CB_RectF(thisWorldRec);
    }

    /**
     * Berechnet das Scissor Rechteck und die Infos fuer die Childs immer dann wenn sich etwas an Position oder Groesse dieses GL_View_Base
     * geaendert hat.<br>
     * Wenn sich etwas geaendert hat, wird auch ein Invalidate an die Childs uebergeben, da diese auch neu berechnet werden
     * muessen.<br>
     * Die detection, wann sich etwas geaendert hat, kommt von der ueberschriebenen CB_RectF Methode CalcCrossPos, da diese bei
     * jeder Aenderung aufgerufen wird.
     */
    private void calcMyInfoForChild() {
        childsInvalidate = true;
        thisWorldRec.setRec(this);
        thisWorldRec.offset(-getX() + myParentInfo.Vector().x, -getY() + myParentInfo.Vector().y);
        boolean mustSetScissor = !myParentInfo.drawRec().contains(thisWorldRec);

        if (mustSetScissor) {
            intersectRec.setRec(myParentInfo.drawRec().createIntersection(thisWorldRec));
        } else {
            intersectRec.setRec(thisWorldRec);
        }

        thisInvalidate = false;

        if (debug)
            createDebugSprite();
    }

    public void invalidate() {
        thisInvalidate = true;
    }

    protected void render(Batch batch) {
        if (!isRenderInitDone) {
            isRenderInitDone = true;
            renderInit();
        }
    }

    public void resetRenderInitDone() {
        // renderInit will be done again on next render
        isRenderInitDone = false;
    }

    protected void renderInit() {
    }

    public void setRotate(float Rotate) {
        mRotate = Rotate;
    }

    public void setOrigin(float originX, float originY) {
        mOriginX = originX;
        mOriginY = originY;
    }

    public void setOriginCenter() {
        mOriginX = getHalfWidth();
        mOriginY = getHalfHeight();
    }

    /**
     * setzt den Scale Factor des dargestellten Images, wobei die Größe nicht verändert wird. Ist das Image größer, wird es abgeschnitten
     */
    public void setScale(float value) {
        mScale = value;
    }

    @Override
    public void resize(float width, float height) {
        super.resize(width, height);
        try {
            innerWidth = width - leftBorder - rightBorder;
            innerHeight = height - topBorder - bottomBorder;
            onResized(this);
        } catch (Exception ex) {
            Log.err(sClass, "resize", ex);
        }
        debugSprite = null;

        // Eine Größenänderung an die Childs melden
        if (childs != null && childs.size() > 0) {
            try {
                for (int i = 0, n = childs.size(); i < n; i++) {
                    // alle renderChilds() der in dieser GL_View_Base enthaltenen Childs auf rufen.
                    GL_View_Base view = childs.get(i);
                    if (view != null)
                        view.onParentResized(this);
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    public void onResized(CB_RectF rec) {
        thisInvalidate = true;
    }

    public void onParentResized(CB_RectF rec) {
        thisInvalidate = true;
    }

    public void onShow() {
    }

    public void onHide() {
    }

    public void onStop() {
        if (childs != null && childs.size() > 0) {
            try {
                for (int i = 0, n = childs.size(); i < n; i++) {
                    // alle renderChilds() der in dieser GL_View_Base
                    // enthaltenen Childs auf rufen.
                    GL_View_Base view = childs.get(i);
                    view.onStop();
                }
            } catch (Exception e) {
                Log.err(sClass, "onStop", e);
            }
        }
    }

    public boolean click(int x, int y, int pointer, int button) {
        boolean handled = false;
        try {
            if (childs.size() > 0) {
                Iterator<GL_View_Base> iterator = childs.reverseIterator();
                // iterate for the view with the touchdown (click)
                while (iterator.hasNext()) {
                    GL_View_Base view = iterator.next();
                    if (view != null && view.isClickable() && view.isVisible() && view.contains(x, y)) {
                        // this view was clicked, so call its click listener
                        handled = view.click(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
                        // if handled, we can break and don't test the rest
                        if (handled) break;
                    }
                }
            }
            if (!handled) {
                if (mOnClickListener != null) {
                    // if not finally handled in a subview (result of handled), call the click listener of this view
                    handled = mOnClickListener.onClick(this, x, y, pointer, button);
                }
            }
        } catch (Exception e) {
            Log.err(sClass, "click", e);
        }
        return handled;
    }

    public boolean doubleClick(int x, int y, int pointer, int button) {
        // Achtung: dieser doubleClick ist nicht virtual und darf nicht überschrieben werden!!!
        // das Ereignis wird dann in der richtigen View an doubleClick übergeben!!!
        // todo Überschreibung in EditTextField, MapView Erklärung (final)
        boolean behandelt = false;
        try {
            if (childs != null && childs.size() > 0) {
                Iterator<GL_View_Base> iterator = childs.reverseIterator();
                while (iterator.hasNext()) {
                    // Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
                    GL_View_Base view = iterator.next();

                    if (view == null || !view.isClickable())
                        continue;
                    // Invisible Views can not be clicked!
                    if (!view.isVisible())
                        continue;

                    if (view.contains(x, y)) {
                        // touch innerhalb des Views
                        // -> Klick an das View weitergeben
                        behandelt = view.doubleClick(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
                        if (behandelt)
                            break;
                    }
                }
            }
            if (!behandelt) {
                // kein Klick in einem untergeordnetem View
                // -> hier behandeln
                if (mOnDoubleClickListener != null) {
                    behandelt = mOnDoubleClickListener.onClick(this, x, y, pointer, button);
                }

            }
        } catch (Exception e) {
            Log.err(sClass, "doubleClick", e);
        }
        return behandelt;
    }

    public boolean longClick(int x, int y, int pointer, int button) {
        // Achtung: dieser longClick ist nicht virtual und darf nicht überschrieben werden!!!
        // das Ereignis wird dann in der richtigen View an longClick übergeben!!!
        // todo Überschreibung in MultiToggleButton Erklärung (final)
        boolean behandelt = false;

        try {
            if (childs != null && childs.size() > 0) {
                Iterator<GL_View_Base> iterator = childs.reverseIterator();
                while (iterator.hasNext()) {
                    // Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
                    GL_View_Base view = iterator.next();

                    if (view == null || !view.isClickable())
                        continue;

                    if (view.contains(x, y)) {
                        // touch innerhalb des Views
                        // -> Klick an das View weitergeben
                        behandelt = view.longClick(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
                    }
                }
            }
            if (!behandelt) {
                // kein Klick in einem untergeordnetem View
                // -> hier behandeln
                if (mOnLongClickListener != null) {
                    behandelt = mOnLongClickListener.onClick(this, x, y, pointer, button);
                }

            }
        } catch (Exception e) {
            Log.err(sClass, "longClick", e);
        }
        return behandelt;
    }

    public GL_View_Base touchDown(int x, int y, int pointer, int button) {
        // Achtung: dieser touchDown ist nicht virtual und darf nicht überschrieben werden!!!
        // das Ereignis wird dann in der richtigen View an onTouchDown übergeben!!!
        // touchDown liefert die View zurück, die dieses TochDown Ereignis angenommen hat
        // todo Überschreibung in EditFieldNotes Erklärung (final)
        GL_View_Base resultView = null;

        if (childs != null && childs.size() > 0) {
            try {
                Iterator<GL_View_Base> iterator = childs.reverseIterator();
                while (iterator.hasNext()) {
                    // Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
                    GL_View_Base view = iterator.next();

                    // Invisible Views can not be clicked!
                    if (view == null || !view.isVisible())
                        continue;
                    if (!view.isEnabled())
                        continue;
                    if (view.contains(x, y)) {
                        // touch innerhalb des Views
                        // -> Klick an das View weitergeben
                        lastTouchPos = new Vector2(x - view.getX(), y - view.getY());
                        resultView = view.touchDown(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
                    }

                    if (resultView != null)
                        break;
                }
            } catch (Exception e) {
                return null;
            }
        }

        if (forceHandleTouchEvents || resultView == null) {

            // kein Klick in einem untergeordnetem View
            // -> hier behandeln
            boolean behandelt = onTouchDown(x, y, pointer, button);
            if (behandelt)
                resultView = this;
        }

        GL.that.renderOnce();
        return resultView;
    }

    final boolean touchDragged(int x, int y, int pointer, boolean KineticPan) {
        // Achtung: dieser touchDragged ist nicht virtual und darf nicht überschrieben werden!!!
        // das Ereignis wird dann in der richtigen View an onTouchDown übergeben!!!
        boolean behandelt = false;

        if (childs != null && childs.size() > 0) {
            try {
                Iterator<GL_View_Base> iterator = childs.reverseIterator();
                while (iterator.hasNext()) {
                    GL_View_Base view = iterator.next();

                    if (view != null && view.contains(x, y)) {
                        behandelt = view.touchDragged(x - (int) view.getX(), y - (int) view.getY(), pointer, KineticPan);
                    }
                    if (behandelt)
                        break;
                }
            } catch (Exception e) {
                return false;
            }
        }

        if (forceHandleTouchEvents || !behandelt) {
            // kein Klick in einem untergeordnetem View -> hier behandeln
            behandelt = onTouchDragged(x, y, pointer, KineticPan);
        }
        return behandelt;
    }

    final boolean touchUp(int x, int y, int pointer, int button) {
        // Achtung: dieser touchDown ist nicht virtual und darf nicht überschrieben werden!!!
        // das Ereignis wird dann in der richtigen View an onTouchDown übergeben!!!
        boolean behandelt = false;

        if (childs != null && childs.size() > 0) {
            try {
                Iterator<GL_View_Base> iterator = childs.reverseIterator();
                while (iterator.hasNext()) {
                    GL_View_Base view = iterator.next();
                    if (view != null && view.contains(x, y)) {
                        // touch innerhalb des Views
                        // -> Klick an das View weitergeben
                        behandelt = view.touchUp(x - (int) view.getX(), y - (int) view.getY(), pointer, button);
                    }

                    if (behandelt)
                        break;
                }
            } catch (Exception e) {
                return false;
            }
        }

        if (forceHandleTouchEvents || !behandelt) {
            // kein Klick in einem untergeordnetem View
            // -> hier behandeln
            behandelt = onTouchUp(x, y, pointer, button);
        }

        return behandelt;
    }

    public void onLongClick(int x, int y, int pointer, int button) {
    }

    public boolean onTouchDown(int x, int y, int pointer, int button) {
        return false;
    }

    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        return false;
    }

    public boolean onTouchUp(int x, int y, int pointer, int button) {
        return false;
    }

    @Override
    public String toString() {
        return getName() + " X,Y/Width,Height = " + getX() + "," + getY() + "/" + getWidth() + "," + getHeight();
    }

    @Override
    public void dispose() {
        GL.that.removeRenderView(this);
        GL.that.runOnGLWithThreadCheck(() -> {
            try {
                synchronized (childs) {
                    for (GL_View_Base view : childs) {
                        if (view != null && !view.isDisposed)
                            view.dispose();
                    }
                    childs.clear();
                }

                if (debugRegTexture != null) {
                    debugRegTexture.dispose();
                }

                if (debugRegPixmap != null) {
                    debugRegPixmap.dispose();
                }

            } catch (Exception ignored) {
            }
            isDisposed = true;
        });
        super.dispose();
    }

    public OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    /**
     * Register a callback to be invoked when this view is clicked. If this view is not clickable, it becomes clickable.
     *
     * @param listener The callback that will run
     * @see #setClickable(boolean)
     */
    public void setClickHandler(OnClickListener listener) {
        // name changed from setOnClickListener to setClickHandler to be compatible with CB3
        isClickable = listener != null;
        mOnClickListener = listener;
    }

    public OnClickListener getOnLongClickListener() {
        return mOnLongClickListener;
    }

    /**
     * Register a callback to be invoked when this view is long clicked. If this view is not clickable, it becomes clickable.
     *
     * @param l The callback that will run
     * @see #setClickable(boolean)
     */
    public void setLongClickHandler(OnClickListener l) {
        isLongClickable = l != null;
        mOnLongClickListener = l;
    }

    /**
     * Register a callback to be invoked when this view is double clicked. If this view is not clickable, it becomes clickable.
     *
     * @param l The callback that will run
     * @see #setClickable(boolean)
     */
    public void setOnDoubleClickListener(OnClickListener l) {
        isDoubleClickable = l != null;
        mOnDoubleClickListener = l;
    }

    boolean isDoubleClickable() {
        if (!isVisible())
            return false;
        return isDoubleClickable || hasDoubleClickableChild;
    }

    protected void setDoubleClickable() {
        isDoubleClickable = true;
    }

    boolean isLongClickable() {
        if (!isVisible())
            return false;
        return isLongClickable || hasLongClickableChild;
    }

    public void setLongClickable(boolean value) {
        isLongClickable = value;
    }

    protected boolean isClickable() {
        if (!isVisible())
            return false;
        return isClickable || hasClickableChild;
    }

    /**
     * if value is true, clicks will be sent else not
     */
    public void setClickable(boolean value) {
        isClickable = value;
    }

    public String getName() {
        return name;
    }

    @Override
    public void setY(float i) {
        if (getY() == i)
            return;
        super.setY(i);
        invalidate(); // Scissor muss neu berechnet werden
        GL.that.renderOnce();
    }

    @Override
    public void setX(float i) {
        if (getX() == i)
            return;
        super.setX(i);
        invalidate(); // Scissor muss neu berechnet werden
        GL.that.renderOnce();
    }

    public void setZeroPos() {
        super.setPos(0, 0);
        invalidate(); // Scissor muss neu berechnet werden
        GL.that.renderOnce();
    }

    @Override
    public void setPos(float x, float y) {
        super.setPos(x, y);
        invalidate(); // Scissor muss neu berechnet werden
        GL.that.renderOnce();
    }

    // Abfrage der clickToleranz, mit der Bestimmt wird ab welcher Bewegung ein onTouchDragged erzeugt wird und beim loslassen kein click
    // dies kann hier für einzelne Views unabhängig bestimmt werden
    int getClickTolerance() {
        // wenn eine View clickable ist dann muß für die Verschiebung (onTouchDragged) ein gewisser Toleranzbereich definiert werden,
        // innerhalb dem erstmal kein onTouchDragged aufgerufen wird
        if (isClickable())
            return UiSizes.getInstance().getClickToleranz();
        else
            // Wenn aber eine View nicht clickable ist dann darf der onTouchDragged sofort aufgerufen werden
            return 1;
    }

    public void registerSkinChangedEvent() {
        if (calling)
            return;
        // synchronized (skinChangedEventList)
        // {
        skinChangedEventList.add(mSkinChangedEventListener);
        // }
    }

    protected void skinIsChanged() {
    }

    public void clearColorFilter() {
        mColorFilter = null;
    }

    public void setColorFilter(Color color) {
        mColorFilter = color;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        enabled = value;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    /**
     * Interface definition for a callback to be invoked when a view is clicked.
     */
    public interface OnClickListener {
        boolean onClick(GL_View_Base view, int x, int y, int pointer, int button);
    }

    private interface SkinChangedEventListener {
        void handleSkinChanged();
    }

}
