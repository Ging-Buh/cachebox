package CB_UI_Base.GL_UI;

import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import CB_Utils.Util.MoveableList;
import com.badlogic.gdx.graphics.g2d.Batch;

public class CB_View_Base extends GL_View_Base {

    public static final int FIXED = -1;
    public static final boolean TOPDOWN = true;
    public static final boolean BOTTOMUP = false;
    protected boolean isInitial = false;
    // row handling by arbor95: makes live much easier
    // Designing this ( a page, a box, a panel, ...) by adding rows of objects<GL_View_Base>
    // the position and width (stretched equally, weighted, fixed or percentual) of the objects is calculated automatically
    private MoveableList<GL_View_Base> row;
    private boolean topdown = TOPDOWN; // false = bottomup
    private float rowYPos = 0;
    private float xMargin = 0;
    private float yMargin = 0;
    private float topYAdd;
    private float bottomYAdd = -1;
    private boolean isDisposed = false;

    // # Constructors
    public CB_View_Base() {
        super();
    }

    public CB_View_Base(String Name) {
        super(Name);
    }

    public CB_View_Base(float X, float Y, float Width, float Height, String Name) {
        super(X, Y, Width, Height, Name);
    }

    public CB_View_Base(float X, float Y, float Width, float Height, GL_View_Base Parent, String Name) {
        super(X, Y, Width, Height, Parent, Name);
    }

    public CB_View_Base(CB_RectF rec, String Name) {
        super(rec, Name);
    }

    public CB_View_Base(CB_RectF rec, GL_View_Base Parent, String Name) {
        super(rec, Parent, Name);
    }

    public CB_View_Base(SizeF size, String Name) {
        super(size, Name);
    }

    public static void setToNull(CB_View_Base view) {
        if (view.childs.size() != 0) {
            synchronized (view.childs) {
                view.childs.clear();
            }
        }
    }

    @Override
    public boolean isDisposed() {
        return isDisposed;
    }

    public void resetInitial() {
        isInitial = false;
    }

    protected void render(Batch batch) {
        if (!isInitial) {
            isInitial = true;
            Initial();
        }
    }

    protected void Initial() {
    }

    @Override
    public void onResized(CB_RectF rec) {
        thisInvalidate = true;
    }

    @Override
    public void onParentResized(CB_RectF rec) {
        thisInvalidate = true;
    }

    @Override
    public boolean onLongClick(int x, int y, int pointer, int button) {

        return false;
    }

    /**
     * onTouchDown
     */
    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        return false;
    }

    /**
     * onTouchDragged
     */
    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {
        return false;
    }

    /**
     * onTouchUp
     */
    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        return false;
    }

    @Override
    public void dispose() {
        // Remove from RenderViews if registered
        GL.that.removeRenderView(this);

        if (childs == null) {
            // set this to null!
            setToNull(this);
        } else {
            try {
                synchronized (childs) {
                    for (int i = 0; i < childs.size(); i++) {
                        GL_View_Base view;
                        try {
                            view = childs.get(i);
                        } catch (Exception e) {
                            break;
                        }
                        if (view != null && !view.isDisposed())
                            view.dispose();
                    }

                    childs.clear();
                    // set this to null!
                    setToNull(this);
                }
            } catch (Exception e) {
                // NoSuchElementException
                setToNull(this);
            }
        }

        if (row != null) {
            for (int i = 0; i < row.size(); i++) {
                row.get(i).dispose();
            }
            row.clear();
        }

        row = null;

        isDisposed = true;
        super.dispose();
    }

    public int getCildCount() {
        if (childs == null)
            return -1;
        synchronized (childs) {
            return childs.size();
        }
    }

    public GL_View_Base addChildDirekt(final GL_View_Base view) {
        if (childs == null || view == null)
            return null;
        synchronized (childs) {
            if (!childs.contains(view))
                childs.add(view);
        }

        return view;
    }

    public GL_View_Base addChildDirektLast(final GL_View_Base view) {
        if (childs == null || view == null)
            return null;
        synchronized (childs) {
            if (!childs.contains(view))
                childs.add(0, view);
        }

        return view;
    }

    public void removeChildsDirekt() {
        if (childs == null)
            return;
        synchronized (childs) {
            childs.clear();
        }
    }

    public void removeChildsDirekt(GL_View_Base view) {
        if (childs == null || view == null)
            return;
        synchronized (childs) {
            try {
                if (childs.contains(view))
                    childs.remove(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeChildsDirekt(MoveableList<GL_View_Base> childs) {
        if (childs == null)
            return;
        synchronized (childs) {
            try {
                childs.remove(childs);
            } catch (Exception e) {
                e.printStackTrace();
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

    @Override
    public String toString() {
        return getName() + " X,Y/Width,Height = " + getX() + "," + getY() + "/" + getWidth() + "," + getHeight();
    }

    /**
     * * used Height from Bottom with correct topborder
     **/
    public float getHeightFromBottom() {
        return rowYPos - yMargin + topBorder;
    }

    /**
     * * next objects are added at this Y - Position
     **/
    public float getRowYPos() {
        return rowYPos;
    }

    /**
     * * next objects are added at this Y - Position
     **/
    public void setRowYPos(float YPos) {
        rowYPos = YPos;
    }

    public void updateRowY(float newHeight, CB_View_Base controlWithNewHeight) {
        float diffHeight = newHeight - controlWithNewHeight.getHeight();
        float lastPos = controlWithNewHeight.getY();
        // all elements of Last Row must be corrected
        float lastRowHeight = 0;
        for (GL_View_Base c : this.childs) {
            // get controls of the row
            if (c.getY() == lastPos)
                if (c.getHeight() > lastRowHeight)
                    lastRowHeight = c.getHeight();
        }
        if (newHeight > lastRowHeight) {
            if (topdown) {
                rowYPos = rowYPos - diffHeight;
                topYAdd = topYAdd - diffHeight;
                for (GL_View_Base c : this.childs) {
                    // get controls of the row
                    if (c.getY() == lastPos)
                        c.setY(lastPos - diffHeight);
                }
            } else {
                rowYPos = rowYPos + diffHeight;
                bottomYAdd = bottomYAdd + diffHeight;
            }
        }
    }

    /**
     * * setting the margins between the added objects
     **/
    public void setMargins(float xMargin, float yMargin) {
        this.xMargin = xMargin;
        this.yMargin = yMargin;
    }

    /**
     * * start objects at top
     **/
    public void initRow() {
        initRow(TOPDOWN);
    }

    /**
     * * start objects at top (direction true) or bottom (direction false)
     **/
    public void initRow(boolean direction) {
        if (direction) {
            initRow(direction, getHeight() - topBorder);
        } else {
            // starting at 0
            initRow(direction, bottomBorder);
        }
    }

    /**
     * * start objects at this y Position, direction true = topdown
     **/
    public void initRow(boolean direction, float y) {
        if (row == null) {
            row = new MoveableList<GL_View_Base>();
        } else {
            row.clear();
        }
        rowYPos = y;
        if (bottomYAdd < 0) {
            // nur beim ersten Mal, sonst müssen die Werte erhalten bleiben
            if (direction) {
                bottomYAdd = bottomBorder;
                topYAdd = y;
            } else {
                bottomYAdd = y;
                topYAdd = getHeight() - topBorder;
            }
        }
        topdown = direction;
    }

    /**
     * * get innerHeight - Height of all placed objects
     **/
    public float getAvailableHeight() {
        if (row == null)
            initRow();
        return topYAdd - bottomYAdd;
    }

    public void adjustHeight() {
        // nicht sinnvoll wenn von unten und von oben was hinzugefügt wurde
        // und danach auch bitte nichts mehr hinzufügen.
        if (topdown) {
            setHeight(getHeight() - topYAdd);
            // Die Position aller Clients muss bei TopDown neu gesetzt werden.
            for (int i = 0, n = childs.size(); i < n; i++) {
                GL_View_Base view = childs.get(i);
                view.setPos(view.getX(), view.getY() - topYAdd);
            }
            // topYAdd = bottomYAdd; // fertig gebaut
        } else {
            setHeight(bottomYAdd);
            // topYAdd = bottomYAdd; // fertig gebaut
        }
    }

    // Note: Final Position and Size of objects is done on addLast
    // Note: Changing of objects (depending on final Position or Size) must be
    // done after addLast
    // Examples: setting Text of a Button, ....
    // Now, after rework of Label, this is no longer true. Text will be set
    // correctly (independent of call order)

    /**
     * * Add the object at the end of the current row. the current row will be
     * ended after the object is added.
     **/
    public void addLast(GL_View_Base c) {
        c.Weight = 1f;
        addMe(c, true);
    }

    /**
     * Add the object at the end of the current row.
     **/
    public void addNext(GL_View_Base c) {
        c.Weight = 1f;
        addMe(c, false);
    }

    /**
     * * Add the object at the end of the current row. the current row will be
     * ended after the object is added.
     **/
    public void addLast(GL_View_Base c, float Weight) {
        c.Weight = Weight;
        addMe(c, true);
    }

    /**
     * Add the object at the end of the current row.
     **/
    public void addNext(GL_View_Base c, float Weight) {
        c.Weight = Weight;
        addMe(c, false);
    }

    /**
     * All items within the last row processed in the layout!
     */
    public void FinaliseRow() {
        addMe(null, true);
    }

    // ===================================================================
    private void addMe(GL_View_Base c, boolean lastInRow)
    // ===================================================================
    {
        if (row == null)
            initRow();
        if (c != null)
            row.add(c);
        if (lastInRow) {
            // Determine rowMaxHeight
            float rowMaxHeight = 0;
            for (int i = 0, n = row.size(); i < n; i++) {
                GL_View_Base view = row.get(i);
                // view.parent = this;
                if (view.getHeight() > rowMaxHeight)
                    rowMaxHeight = view.getHeight();
            }
            if (topdown) {
                rowYPos = rowYPos - rowMaxHeight;
            }
            // Determine width of objects from number of objects in row
            float rowXPos = leftBorder;
            float weightedAnz = 0;
            float fixedWidthSum = 0;
            float percentWidthSum = 0;
            float widthToFill = getWidth() - leftBorder - rightBorder;
            for (int i = 0, n = row.size(); i < n; i++) {
                GL_View_Base view = row.get(i);
                if (view.Weight > 0) {
                    weightedAnz += view.Weight;
                } else {
                    if (view.Weight == FIXED) {
                        fixedWidthSum += view.getWidth() + xMargin; // xMargin is added to each object
                    } else {
                        // Prozentuale Breite des Objekts bzgl widthToFill
                        percentWidthSum += Math.abs(view.Weight) * widthToFill;
                    }
                }

            }
            float objectWidth = 0;
            if (weightedAnz != 0) {
                objectWidth = (widthToFill - percentWidthSum - fixedWidthSum + xMargin) / weightedAnz - xMargin;
            }
            for (int i = 0, n = row.size(); i < n; i++) {
                GL_View_Base view = row.get(i);
                if (view.Weight > 0) {
                    view.setWidth(objectWidth * view.Weight);
                } else {
                    if (view.Weight > FIXED) {
                        view.setWidth(Math.abs(view.Weight) * widthToFill - xMargin);
                    }
                }
                view.setPos(rowXPos, rowYPos);
                addChildDirekt(view);
                // next object at x
                rowXPos = rowXPos + view.getWidth() + xMargin;
            }
            // next row objects at y
            if (topdown) {
                rowYPos = rowYPos - yMargin;
                topYAdd = rowYPos;
            } else {
                rowYPos = rowYPos + rowMaxHeight + yMargin;
                bottomYAdd = rowYPos;
            }
            row.clear();
        }
    }

    public void measureRec() {
        // Some Controls can change their size

    }
}
