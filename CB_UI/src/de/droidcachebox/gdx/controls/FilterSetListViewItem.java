package de.droidcachebox.gdx.controls;


import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import de.droidcachebox.database.GeoCacheType;
import de.droidcachebox.gdx.*;
import de.droidcachebox.gdx.activities.EditFilterSettings;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;

import java.util.ArrayList;

public class FilterSetListViewItem extends ListViewItemBackground {
    public static final int COLLAPSE_BUTTON_ITEM = 0;
    public static final int CHECK_ITEM = 1;
    public static final int THREE_STATE_ITEM = 2;
    public static final int NUMERIC_ITEM = 3;
    public static final int NUMERIC_INT_ITEM = 4;
    public static final int SELECT_ALL_ITEM = 5;
    private final FilterSetEntry filterSetEntry;
    private final ArrayList<FilterSetListViewItem> mChildList = new ArrayList<>();
    public Vector2 lastItemTouchPos;
    private NinePatch btnBack;
    private NinePatch btnBack_pressed;
    private Sprite minusBtn;
    private Sprite plusBtn;
    private Sprite chkOff;
    private Sprite chkOn;
    private Sprite chkNo;
    private CB_RectF lBounds;
    private CB_RectF rBounds;
    private CB_RectF rChkBounds;
    private BitmapFontCache Minus;
    private BitmapFontCache Plus;
    private BitmapFontCache MinusMinus;
    private BitmapFontCache PlusPlus;
    private BitmapFontCache SelectAll;
    private BitmapFontCache DeselectAll;
    private float MARGIN;
    private float BUTTON_MARGIN;
    // private Member
    private float left;
    private BitmapFontCache entryName;
    private boolean setValueFont = false;
    private BitmapFontCache Value;
    private boolean clicked = false;

    private EditFilterSettings.ISelectAllHandler selectAllHandler;

    public FilterSetListViewItem(CB_RectF rec, int Index, FilterSetEntry fne) {
        super(rec, Index, fne.getName());
        filterSetEntry = fne;
        MARGIN = UiSizes.getInstance().getMargin();
        BUTTON_MARGIN = -(MARGIN / 10);
    }

    public FilterSetEntry getFilterSetEntry() {
        return filterSetEntry;
    }

    public FilterSetListViewItem addChild(FilterSetListViewItem item) {
        mChildList.add(item);
        return item;
    }

    public void setSelectAllHandler(EditFilterSettings.ISelectAllHandler handler) {
        selectAllHandler = handler;
    }

    public void toggleChildViewState() {
        if (mChildList != null && mChildList.size() > 0) {
            boolean newState = !mChildList.get(0).isVisible();

            for (FilterSetListViewItem tmp : mChildList) {
                tmp.setVisible(newState);
            }
        }

    }

    @Override
    protected void render(Batch batch) {
        if (filterSetEntry.getItemType() != COLLAPSE_BUTTON_ITEM) {
            super.render(batch);
        }

        try {
            if (isPressed) {
                GL.that.renderOnce();
                isPressed = GL_Input.that.getIsTouchDown();
            }

            // initial
            left = getLeftWidth();
            //top = getHeight() - getTopHeight();
            float top = (getHeight() + Fonts.getNormal().getLineHeight()) / 2f; //getTopHeight();

            switch (filterSetEntry.getItemType()) {
                case COLLAPSE_BUTTON_ITEM:
                    drawCollapseButtonItem(batch);
                    break;
                case CHECK_ITEM:
                    drawChkItem(batch);
                    break;
                case THREE_STATE_ITEM:
                    drawThreeStateItem(batch);
                    break;
                case NUMERIC_ITEM:
                    top = getHeight() - getTopHeight();
                    drawNumericItem(batch);
                    break;
                case NUMERIC_INT_ITEM:
                    top = getHeight() - getTopHeight();
                    drawNumericIntItem(batch);
                    break;
                case SELECT_ALL_ITEM:
                    // top = getHeight() - getTopHeight();
                    drawSelectItem(batch);
                    return;
            }
            // draw Name
            if (entryName == null) {
                entryName = new BitmapFontCache(Fonts.getNormal());
                entryName.setColor(COLOR.getFontColor());
                if (filterSetEntry.getItemType() == THREE_STATE_ITEM) {
                    float TextWidth = getWidth() - (left + 20) - getRightWidth() - getHeight();
                    entryName.setText(name, left + 20, top, TextWidth, Align.left, true);
                } else {
                    entryName.setText(name, left + 10, top);
                }
            }
            entryName.draw(batch);

            if (filterSetEntry.getItemType() == NUMERIC_ITEM ||
                    filterSetEntry.getItemType() == NUMERIC_INT_ITEM) {
                if (Value == null) {
                    Value = new BitmapFontCache(Fonts.getBig());
                    Value.setColor(COLOR.getFontColor());
                    setValueFont = true;
                }
                if (setValueFont) {
                    float valueOffsetX = 0;
                    String valueString = String.valueOf(getValue());
                    if (filterSetEntry.getItemType() == NUMERIC_INT_ITEM) {
                        int val = (int) getValue();
                        if (val >= 0) {
                            valueString = String.valueOf(val);
                            valueOffsetX = MARGIN * 4;
                        } else {
                            valueString = Translation.get("DoesntMatter");
                            valueOffsetX = getHeight() * 2;
                        }
                    }

                    Value.setText(valueString, (getWidth() / 1.5f) - valueOffsetX, (getHeight() / 1.8f));
                    setValueFont = false;
                }
                Value.draw(batch);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void drawCollapseButtonItem(Batch batch) {

        if (isPressed) {
            if (btnBack_pressed == null) {
                btnBack_pressed = new NinePatch(Sprites.getSprite("btn-pressed"), 16, 16, 16, 16);
            }

            btnBack_pressed.draw(batch, 0, 0, getWidth(), getHeight());

        } else {
            if (btnBack == null) {
                btnBack = new NinePatch(Sprites.getSprite(Sprites.IconName.btnNormal.name()), 16, 16, 16, 16);
            }

            btnBack.draw(batch, 0, 0, getWidth(), getHeight());

        }

    }

    private void drawChkItem(Batch batch) {
        drawIcon(batch);
        drawRightChkBox(batch);
        if (filterSetEntry.getState() == 1) {
            if (chkOn == null) {
                chkOn = Sprites.getSprite("check-on");
                chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
            }
            chkOn.draw(batch);
        }
        if (lastItemTouchPos != null) {
            if (isPressed) {
                // if (rBounds.contains(lastItemTouchPos))
                clicked = true;
            } else if (clicked) {
                clicked = false;
                // if (rBounds.contains(lastItemTouchPos))
                stateClick();
            }
        }
    }

    private void drawThreeStateItem(Batch batch) {
        drawIcon(batch);
        drawRightChkBox(batch);

        if (filterSetEntry.getState() == 1) {
            if (chkOn == null) {
                chkOn = Sprites.getSprite("check-on");

                chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

            }

            chkOn.draw(batch);
        } else if (filterSetEntry.getState() == -1) {
            if (chkNo == null) {
                chkNo = Sprites.getSprite(Sprites.IconName.DELETE.name());

                chkNo.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

            }

            chkNo.draw(batch);
        }

        boolean rClick;
        if (lastItemTouchPos != null) {
            if (isPressed) {
                rClick = rBounds.contains(lastItemTouchPos);

                if (rClick)
                    clicked = true;
            } else {
                if (clicked) {
                    clicked = false;
                    rClick = rBounds.contains(lastItemTouchPos);
                    if (rClick)
                        stateClick();
                }
            }
        }

    }

    private void drawNumericItem(Batch batch) {
        lBounds = new CB_RectF(0, 0, getHeight(), getHeight());
        lBounds = lBounds.scaleCenter(0.95f);

        rBounds = new CB_RectF(getWidth() - getHeight(), 0, getHeight(), getHeight());
        rBounds = rBounds.scaleCenter(0.95f);

        boolean rClick = false;
        boolean lClick = false;
        if (lastItemTouchPos != null) {
            if (isPressed) {
                lClick = lBounds.contains(lastItemTouchPos);
                rClick = rBounds.contains(lastItemTouchPos);

                if (lClick || rClick)
                    clicked = true;
            } else {
                if (clicked) {
                    clicked = false;
                    lClick = lBounds.contains(lastItemTouchPos);
                    rClick = rBounds.contains(lastItemTouchPos);
                    if (rClick)
                        plusClick();
                    if (lClick)
                        minusClick();
                }
            }
        }

        plusBtn = rClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(Sprites.IconName.btnNormal.name());
        minusBtn = lClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(Sprites.IconName.btnNormal.name());

        minusBtn.setBounds(lBounds.getX(), lBounds.getY(), lBounds.getWidth(), lBounds.getHeight());

        plusBtn.setBounds(rBounds.getX(), rBounds.getY(), rBounds.getWidth(), rBounds.getHeight());

        if (Minus == null) {
            Minus = new BitmapFontCache(Fonts.getBig());
            Minus.setColor(COLOR.getFontColor());
            Minus.setText("-", 0, 0);
            Minus.setPosition(lBounds.getCenterPosX() - (Minus.getLayouts().first().width / 2), lBounds.getCenterPosY() + (Minus.getLayouts().first().height / 2));
        }

        if (Plus == null) {
            Plus = new BitmapFontCache(Fonts.getBig());
            Plus.setColor(COLOR.getFontColor());
            Plus.setText("+", 0, 0);
            Plus.setPosition(rBounds.getCenterPosX() - (Plus.getLayouts().first().width / 2), rBounds.getCenterPosY() + (Plus.getLayouts().first().height / 2));
        }

        // draw [-] Button
        minusBtn.draw(batch);

        // draw [+] Button
        plusBtn.draw(batch);

        // draw +/- on Button
        Minus.draw(batch);
        Plus.draw(batch);

        left += minusBtn.getWidth() + minusBtn.getX();

        if (filterSetEntry.getIcon() != null) {
            float iconHeight = getHalfHeight() * 0.8f;
            float iconWidth = iconHeight * 5;
            filterSetEntry.getIcon().setBounds(left, MARGIN, iconWidth, iconHeight);
            filterSetEntry.getIcon().draw(batch);
            // top += UiSizes.getIconSize() / 1.5;
        }

    }

    private void drawNumericIntItem(Batch batch) {
        CB_RectF llBounds = new CB_RectF(0, 0, getHeight(), getHeight());
        llBounds = llBounds.scaleCenter(0.95f);

        lBounds = new CB_RectF(llBounds.getMaxX() + BUTTON_MARGIN, 0, getHeight(), getHeight());
        lBounds = lBounds.scaleCenter(0.95f);

        CB_RectF rrBounds = new CB_RectF(getWidth() - getHeight(), 0, getHeight(), getHeight());
        rrBounds = rrBounds.scaleCenter(0.95f);

        rBounds = new CB_RectF(rrBounds.getX() - (BUTTON_MARGIN + getHeight()), 0, getHeight(), getHeight());
        rBounds = rBounds.scaleCenter(0.95f);

        boolean rClick = false;
        boolean lClick = false;
        boolean rrClick = false;
        boolean llClick = false;
        if (lastItemTouchPos != null) {
            if (isPressed) {
                lClick = lBounds.contains(lastItemTouchPos);
                rClick = rBounds.contains(lastItemTouchPos);
                llClick = llBounds.contains(lastItemTouchPos);
                rrClick = rrBounds.contains(lastItemTouchPos);
                if (lClick || rClick || llClick || rrClick)
                    clicked = true;
            } else {
                if (clicked) {
                    clicked = false;
                    lClick = lBounds.contains(lastItemTouchPos);
                    rClick = rBounds.contains(lastItemTouchPos);
                    llClick = llBounds.contains(lastItemTouchPos);
                    rrClick = rrBounds.contains(lastItemTouchPos);
                    if (rClick)
                        plusPlusClick();
                    if (lClick)
                        minusMinusClick();
                    if (rrClick)
                        plusClick();
                    if (llClick)
                        minusClick();
                }
            }
        }

        plusBtn = rrClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(Sprites.IconName.btnNormal.name());
        minusBtn = llClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(Sprites.IconName.btnNormal.name());
        Sprite plusPlusBtn = rClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(Sprites.IconName.btnNormal.name());
        Sprite minusMinusBtn = lClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(Sprites.IconName.btnNormal.name());

        minusBtn.setBounds(llBounds.getX(), llBounds.getY(), llBounds.getWidth(), llBounds.getHeight());
        plusBtn.setBounds(rrBounds.getX(), rrBounds.getY(), rrBounds.getWidth(), rrBounds.getHeight());
        minusMinusBtn.setBounds(lBounds.getX(), lBounds.getY(), lBounds.getWidth(), lBounds.getHeight());
        plusPlusBtn.setBounds(rBounds.getX(), rBounds.getY(), rBounds.getWidth(), rBounds.getHeight());

        if (Minus == null) {
            Minus = new BitmapFontCache(Fonts.getBig());
            Minus.setColor(COLOR.getFontColor());
            Minus.setText("-", 0, 0);
            Minus.setPosition(llBounds.getCenterPosX() - (Minus.getLayouts().first().width / 2), llBounds.getCenterPosY() + (Minus.getLayouts().first().height / 2));
        }

        if (Plus == null) {
            Plus = new BitmapFontCache(Fonts.getBig());
            Plus.setColor(COLOR.getFontColor());
            Plus.setText("+", 0, 0);
            Plus.setPosition(rrBounds.getCenterPosX() - (Plus.getLayouts().first().width / 2), rrBounds.getCenterPosY() + (Plus.getLayouts().first().height / 2));
        }

        if (MinusMinus == null) {
            MinusMinus = new BitmapFontCache(Fonts.getBig());
            MinusMinus.setColor(COLOR.getFontColor());
            MinusMinus.setText("--", 0, 0);
            MinusMinus.setPosition(lBounds.getCenterPosX() - (MinusMinus.getLayouts().first().width / 2), lBounds.getCenterPosY() + (MinusMinus.getLayouts().first().height / 2));
        }

        if (PlusPlus == null) {
            PlusPlus = new BitmapFontCache(Fonts.getBig());
            PlusPlus.setColor(COLOR.getFontColor());
            PlusPlus.setText("++", 0, 0);
            PlusPlus.setPosition(rBounds.getCenterPosX() - (PlusPlus.getLayouts().first().width / 2), rBounds.getCenterPosY() + (PlusPlus.getLayouts().first().height / 2));
        }

        minusBtn.draw(batch);
        plusBtn.draw(batch);
        Minus.draw(batch);
        Plus.draw(batch);

        minusMinusBtn.draw(batch);
        plusPlusBtn.draw(batch);
        MinusMinus.draw(batch);
        PlusPlus.draw(batch);

        left += minusMinusBtn.getWidth() + minusMinusBtn.getX();

        if (filterSetEntry.getIcon() != null) {
            float iconHeight = getHalfHeight() * 0.8f;
            float iconWidth = getHalfHeight() * 0.8f;
            filterSetEntry.getIcon().setBounds(left, MARGIN, iconWidth, iconHeight);
            filterSetEntry.getIcon().draw(batch);
            // top += UiSizes.getIconSize() / 1.5;
        }

    }

    private void drawSelectItem(Batch batch) {
        float btnWidth = (getWidth() / 2) - 2 * MARGIN;
        lBounds = new CB_RectF(MARGIN, 0, btnWidth, getHeight());
        lBounds = lBounds.scaleCenter(0.95f);

        rBounds = new CB_RectF(getWidth() - (btnWidth + MARGIN), 0, btnWidth, getHeight());
        rBounds = rBounds.scaleCenter(0.95f);

        boolean rClick = false;
        boolean lClick = false;
        if (lastItemTouchPos != null) {
            if (isPressed) {
                lClick = lBounds.contains(lastItemTouchPos);
                rClick = rBounds.contains(lastItemTouchPos);

                if (lClick || rClick)
                    clicked = true;
            } else {
                if (clicked && selectAllHandler != null) {
                    clicked = false;
                    lClick = lBounds.contains(lastItemTouchPos);
                    rClick = rBounds.contains(lastItemTouchPos);
                    if (rClick)
                        selectAllHandler.selectAllCacheTypes();
                    if (lClick)
                        selectAllHandler.selectNoCacheTypes();
                }
            }
        }

        Drawable selectAllBtn = rClick ? Sprites.btnPressed : Sprites.btn;
        Drawable deSelectAllBtn = lClick ? Sprites.btnPressed : Sprites.btn;

        if (DeselectAll == null) {
            DeselectAll = new BitmapFontCache(Fonts.getBig());
            DeselectAll.setColor(COLOR.getFontColor());
            DeselectAll.setText(Translation.get("untickAll"), 0, 0);
            DeselectAll.setPosition(lBounds.getCenterPosX() - (DeselectAll.getLayouts().first().width / 2), lBounds.getCenterPosY() + (DeselectAll.getLayouts().first().height / 2));
        }

        if (SelectAll == null) {
            SelectAll = new BitmapFontCache(Fonts.getBig());
            SelectAll.setColor(COLOR.getFontColor());
            SelectAll.setText(Translation.get("tickAll"), 0, 0);
            SelectAll.setPosition(rBounds.getCenterPosX() - (SelectAll.getLayouts().first().width / 2), rBounds.getCenterPosY() + (SelectAll.getLayouts().first().height / 2));
        }


        deSelectAllBtn.draw(batch, lBounds.getX(), lBounds.getY(), lBounds.getWidth(), lBounds.getHeight());
        selectAllBtn.draw(batch, rBounds.getX(), rBounds.getY(), rBounds.getWidth(), rBounds.getHeight());

        DeselectAll.draw(batch);
        SelectAll.draw(batch);
    }

    private void drawIcon(Batch batch) {
        if (filterSetEntry.getIcon() != null) {
            float iconHeight = getHeight() * 0.8f;
            float iconWidth = getHeight() * 0.8f;
            float y = (getHeight() - iconHeight) / 2f; // MARGIN
            filterSetEntry.getIcon().setBounds(left, y, iconWidth, iconHeight);
            filterSetEntry.getIcon().draw(batch);
            left += iconWidth + MARGIN + getLeftWidth();
        }

    }

    private void drawRightChkBox(Batch batch) {
        if (rBounds == null || rChkBounds == null) {
            rBounds = new CB_RectF(getWidth() - getHeight() - 10, 5, getHeight() - 10, getHeight() - 10);// = right Button bounds

            rChkBounds = rBounds.scaleCenter(0.8f);
        }

        if (chkOff == null) {
            chkOff = Sprites.getSprite("check-off");

            chkOff.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

        }

        chkOff.draw(batch);

    }

    private void plusClick() {
        filterSetEntry.plusClick();
        setValueFont = true;
        isPressed = false;
        lastItemTouchPos = null;
        GL.that.renderOnce();
    }

    private void minusClick() {
        filterSetEntry.minusClick();
        setValueFont = true;
        isPressed = false;
        lastItemTouchPos = null;
        GL.that.renderOnce();
    }

    private void plusPlusClick() {
        filterSetEntry.plusPlusClick();
        setValueFont = true;
        isPressed = false;
        lastItemTouchPos = null;
        GL.that.renderOnce();
    }

    private void minusMinusClick() {
        filterSetEntry.minusMinusClick();
        setValueFont = true;
        isPressed = false;
        lastItemTouchPos = null;
        GL.that.renderOnce();
    }

    private void stateClick() {
        filterSetEntry.stateClick();
        isPressed = false;
        lastItemTouchPos = null;
        GL.that.renderOnce();
    }

    public int getChecked() {
        return filterSetEntry.getState();
    }

    public void unCheck() {
        filterSetEntry.setState(0);
    }

    public void setChecked() {
        filterSetEntry.setState(1);
    }

    public double getValue() {
        return filterSetEntry.getNumState();
    }

    public void setValue(int value) {
        filterSetEntry.setState(value);
    }

    public void setValue(double value) {
        filterSetEntry.setState(value);
    }

    public void setValue(boolean b) {
        filterSetEntry.setState(b ? 1 : 0);
    }

    @Override
    public FilterSetListViewItem getChild(int i) {
        return mChildList.get(i);
    }

    public int getChildLength() {
        return mChildList.size();
    }

    public boolean getBoolean() {
        return filterSetEntry.getState() != 0;
    }

    public static class FilterSetEntry {
        private final String mName;
        private final int mItemType;
        private Sprite mIcon;
        private Sprite[] mIconArray;
        private int mState = 0;
        private double mNumericMax;
        private double mNumericMin;
        private double mNumericStep;
        private double mNumericState;
        private GeoCacheType cacheType;

        public FilterSetEntry(String name, Sprite icon, int itemType) {
            mName = name;
            mIcon = icon;
            mItemType = itemType;
        }

        public FilterSetEntry(String Name, Sprite[] Icons, int itemType, double min, double max, double iniValue, double Step) {
            mName = Name;
            mIconArray = Icons;
            mItemType = itemType;
            mNumericMin = min;
            mNumericMax = max;
            mNumericState = iniValue;
            mNumericStep = Step;
        }

        public FilterSetEntry(String Name, Sprite icon, int itemType, double min, double max, double iniValue, double Step) {
            mName = Name;
            mIcon = icon;
            mItemType = itemType;
            mNumericMin = min;
            mNumericMax = max;
            mNumericState = iniValue;
            mNumericStep = Step;
        }

        public FilterSetEntry(GeoCacheType cacheType, String name, Sprite icon, int itemType) {
            mName = name;
            mIcon = icon;
            mItemType = itemType;
            this.cacheType = cacheType;
        }

        public String getName() {
            return mName;
        }

        public Sprite getIcon() {
            if (mItemType == NUMERIC_ITEM) {
                try {
                    double ArrayMultiplier = (mIconArray.length > 5) ? 2 : 1;
                    return mIconArray[(int) (mNumericState * ArrayMultiplier)];
                } catch (Exception ignored) {
                }
            }
            return mIcon;
        }

        public int getState() {
            return mState;
        }

        public void setState(int State) {
            mState = State;
        }

        public void setState(double State) {
            mNumericState = State;
        }

        int getItemType() {
            return mItemType;
        }

        double getNumState() {
            return mNumericState;
        }

        public GeoCacheType getCacheType() {
            return cacheType;
        }

        void plusClick() {
            mNumericState += mNumericStep;
            if (mNumericState > mNumericMax) {
                if (mItemType == NUMERIC_INT_ITEM) {
                    mNumericState = mNumericMax;
                } else {
                    mNumericState = mNumericMin;
                }
            }
        }

        void minusClick() {
            mNumericState -= mNumericStep;
            if (mNumericState < 0) {
                if (mItemType == NUMERIC_INT_ITEM) {
                    mNumericState = mNumericMin;
                } else {
                    mNumericState = mNumericMax;
                }
            }
        }

        void plusPlusClick() {
            if (mNumericState < 0) mNumericState++;
            mNumericState += (mNumericStep * 10);
            if (mNumericState > mNumericMax) {
                if (mItemType == NUMERIC_INT_ITEM) {
                    mNumericState = mNumericMax;
                } else {
                    mNumericState = mNumericMin;
                }
            }
        }

        void minusMinusClick() {
            mNumericState -= (mNumericStep * 10);
            if (mNumericState == 0) mNumericState = -1;
            if (mNumericState < 0) {
                if (mItemType == NUMERIC_INT_ITEM) {
                    mNumericState = mNumericMin;
                } else {
                    mNumericState = mNumericMax;
                }
            }
        }

        void stateClick() {
            mState += 1;
            if (mItemType == CHECK_ITEM) {
                if (mState > 1)
                    mState = 0;
            } else if (mItemType == THREE_STATE_ITEM) {
                if (mState > 1)
                    mState = -1;
            }
        }

    }

}
