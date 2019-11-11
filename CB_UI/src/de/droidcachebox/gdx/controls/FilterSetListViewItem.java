package de.droidcachebox.gdx.controls;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_Input;
import de.droidcachebox.gdx.*;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.controls.FilterSetListView.FilterSetEntry;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;

import java.util.ArrayList;

interface ISelectAllHandler {
    void selectAll();

    void deselectAll();
}

public class FilterSetListViewItem extends ListViewItemBackground {
    private static NinePatch btnBack;
    private static NinePatch btnBack_pressed;
    private static Sprite minusBtn;
    private static Sprite plusBtn;
    private static Sprite minusMinusBtn;
    private static Sprite plusPlusBtn;
    private static Drawable selectAllBtn;
    private static Drawable deSelectAllBtn;
    private static Sprite chkOff;
    private static Sprite chkOn;
    private static Sprite chkNo;
    private static CB_RectF lBounds;
    private static CB_RectF rBounds;
    private static CB_RectF llBounds;
    private static CB_RectF rrBounds;
    private static CB_RectF rChkBounds;
    private static BitmapFontCache Minus;
    private static BitmapFontCache Plus;
    private static BitmapFontCache MinusMinus;
    private static BitmapFontCache PlusPlus;
    private static BitmapFontCache SelectAll;
    private static BitmapFontCache DeselectAll;
    private static float MARGIN;
    private static float BUTTON_MARGIN;
    private final FilterSetEntry mFilterSetEntry;
    private final ArrayList<FilterSetListViewItem> mChildList = new ArrayList<>();
    public Vector2 lastItemTouchPos;
    // private Member
    float left;
    float top;
    BitmapFontCache EntryName;
    private boolean setValueFont = false;
    private BitmapFontCache Value;
    private boolean Clicked = false;

    private ISelectAllHandler selectAllHandler;

    public FilterSetListViewItem(CB_RectF rec, int Index, FilterSetEntry fne) {
        super(rec, Index, fne.getName());
        this.mFilterSetEntry = fne;
        MARGIN = UiSizes.getInstance().getMargin();
        BUTTON_MARGIN = -(MARGIN / 10);
    }

    public FilterSetEntry getFilterSetEntry() {
        return mFilterSetEntry;
    }

    public FilterSetListViewItem addChild(FilterSetListViewItem item) {
        mChildList.add(item);
        return item;
    }

    void setSelectAllHandler(ISelectAllHandler handler) {
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
        if (this.mFilterSetEntry.getItemType() != de.droidcachebox.gdx.controls.FilterSetListView.COLLAPSE_BUTTON_ITEM) {
            super.render(batch);
        }

        try {
            if (isPressed) {
                GL.that.renderOnce();
                isPressed = GL_Input.that.getIsTouchDown();
            }

            // initial
            left = getLeftWidth();
            //top = this.getHeight() - this.getTopHeight();
            top = (this.getHeight() + Fonts.getNormal().getLineHeight()) / 2f; //this.getTopHeight();

            switch (this.mFilterSetEntry.getItemType()) {
                case de.droidcachebox.gdx.controls.FilterSetListView.COLLAPSE_BUTTON_ITEM:
                    drawCollapseButtonItem(batch);
                    break;
                case de.droidcachebox.gdx.controls.FilterSetListView.CHECK_ITEM:
                    drawChkItem(batch);
                    break;
                case de.droidcachebox.gdx.controls.FilterSetListView.THREE_STATE_ITEM:
                    drawThreeStateItem(batch);
                    break;
                case de.droidcachebox.gdx.controls.FilterSetListView.NUMERIC_ITEM:
                    top = this.getHeight() - this.getTopHeight();
                    drawNumericItem(batch);
                    break;
                case de.droidcachebox.gdx.controls.FilterSetListView.NUMERIC_INT_ITEM:
                    top = this.getHeight() - this.getTopHeight();
                    drawNumericIntItem(batch);
                    break;
                case de.droidcachebox.gdx.controls.FilterSetListView.SELECT_ALL_ITEM:
                    top = this.getHeight() - this.getTopHeight();
                    drawSelectItem(batch);
                    return;
            }
            // draw Name
            if (EntryName == null) {
                EntryName = new BitmapFontCache(Fonts.getNormal());
                EntryName.setColor(COLOR.getFontColor());
                if (this.mFilterSetEntry.getItemType() == de.droidcachebox.gdx.controls.FilterSetListView.THREE_STATE_ITEM) {
                    float TextWidth = getWidth() - (left + 20) - getRightWidth() - getHeight();
                    EntryName.setText(name, left + 20, top, TextWidth, Align.left, true);
                } else {
                    EntryName.setText(name, left + 10, top);
                }
            }
            EntryName.draw(batch);

            if (this.mFilterSetEntry.getItemType() == de.droidcachebox.gdx.controls.FilterSetListView.NUMERIC_ITEM ||
                    this.mFilterSetEntry.getItemType() == de.droidcachebox.gdx.controls.FilterSetListView.NUMERIC_INT_ITEM) {
                if (Value == null) {
                    Value = new BitmapFontCache(Fonts.getBig());
                    Value.setColor(COLOR.getFontColor());
                    setValueFont = true;
                }
                if (setValueFont) {
                    float valueOffsetX = 0;
                    String valueString = String.valueOf(getValue());
                    if (this.mFilterSetEntry.getItemType() == de.droidcachebox.gdx.controls.FilterSetListView.NUMERIC_INT_ITEM) {
                        int val = (int) getValue();
                        if (val >= 0) {
                            valueString = String.valueOf(val);
                            valueOffsetX = MARGIN * 4;
                        } else {
                            valueString = Translation.get("DoesntMatter");
                            valueOffsetX = this.getHeight() * 2;
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

        if (this.isPressed) {
            if (btnBack_pressed == null) {
                btnBack_pressed = new NinePatch(Sprites.getSprite("btn-pressed"), 16, 16, 16, 16);
            }

            btnBack_pressed.draw(batch, 0, 0, getWidth(), getHeight());

        } else {
            if (btnBack == null) {
                btnBack = new NinePatch(Sprites.getSprite(IconName.btnNormal.name()), 16, 16, 16, 16);
            }

            btnBack.draw(batch, 0, 0, getWidth(), getHeight());

        }

    }

    private void drawChkItem(Batch batch) {
        drawIcon(batch);
        drawRightChkBox(batch);
        if (this.mFilterSetEntry.getState() == 1) {
            if (chkOn == null) {
                chkOn = Sprites.getSprite("check-on");
                chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
            }
            chkOn.draw(batch);
        }
        if (this.lastItemTouchPos != null) {
            if (isPressed) {
                // if (rBounds.contains(this.lastItemTouchPos))
                Clicked = true;
            } else if (Clicked) {
                Clicked = false;
                // if (rBounds.contains(this.lastItemTouchPos))
                stateClick();
            }
        }
    }

    private void drawThreeStateItem(Batch batch) {
        drawIcon(batch);
        drawRightChkBox(batch);

        if (this.mFilterSetEntry.getState() == 1) {
            if (chkOn == null) {
                chkOn = Sprites.getSprite("check-on");

                chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

            }

            chkOn.draw(batch);
        } else if (this.mFilterSetEntry.getState() == -1) {
            if (chkNo == null) {
                chkNo = Sprites.getSprite(IconName.DELETE.name());

                chkNo.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

            }

            chkNo.draw(batch);
        }

        boolean rClick = false;
        if (this.lastItemTouchPos != null) {
            if (this.isPressed) {
                rClick = rBounds.contains(this.lastItemTouchPos);

                if (rClick)
                    Clicked = true;
            } else {
                if (Clicked) {
                    Clicked = false;
                    rClick = rBounds.contains(this.lastItemTouchPos);
                    if (rClick)
                        stateClick();
                }
            }
        }

    }

    private void drawNumericItem(Batch batch) {
        lBounds = new CB_RectF(0, 0, getHeight(), getHeight());
        lBounds = lBounds.ScaleCenter(0.95f);

        rBounds = new CB_RectF(getWidth() - getHeight(), 0, getHeight(), getHeight());
        rBounds = rBounds.ScaleCenter(0.95f);

        boolean rClick = false;
        boolean lClick = false;
        if (this.lastItemTouchPos != null) {
            if (this.isPressed) {
                lClick = lBounds.contains(this.lastItemTouchPos);
                rClick = rBounds.contains(this.lastItemTouchPos);

                if (lClick || rClick)
                    Clicked = true;
            } else {
                if (Clicked) {
                    Clicked = false;
                    lClick = lBounds.contains(this.lastItemTouchPos);
                    rClick = rBounds.contains(this.lastItemTouchPos);
                    if (rClick)
                        plusClick();
                    if (lClick)
                        minusClick();
                }
            }
        }

        plusBtn = rClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(IconName.btnNormal.name());
        minusBtn = lClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(IconName.btnNormal.name());

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

        if (mFilterSetEntry.getIcon() != null) {
            float iconHeight = this.getHalfHeight() * 0.8f;
            float iconWidth = iconHeight * 5;
            mFilterSetEntry.getIcon().setBounds(left, MARGIN, iconWidth, iconHeight);
            mFilterSetEntry.getIcon().draw(batch);
            // top += UiSizes.getIconSize() / 1.5;
        }

    }

    private void drawNumericIntItem(Batch batch) {
        llBounds = new CB_RectF(0, 0, getHeight(), getHeight());
        llBounds = llBounds.ScaleCenter(0.95f);

        lBounds = new CB_RectF(llBounds.getMaxX() + BUTTON_MARGIN, 0, getHeight(), getHeight());
        lBounds = lBounds.ScaleCenter(0.95f);

        rrBounds = new CB_RectF(getWidth() - getHeight(), 0, getHeight(), getHeight());
        rrBounds = rrBounds.ScaleCenter(0.95f);

        rBounds = new CB_RectF(rrBounds.getX() - (BUTTON_MARGIN + getHeight()), 0, getHeight(), getHeight());
        rBounds = rBounds.ScaleCenter(0.95f);

        boolean rClick = false;
        boolean lClick = false;
        boolean rrClick = false;
        boolean llClick = false;
        if (this.lastItemTouchPos != null) {
            if (this.isPressed) {
                lClick = lBounds.contains(this.lastItemTouchPos);
                rClick = rBounds.contains(this.lastItemTouchPos);
                llClick = llBounds.contains(this.lastItemTouchPos);
                rrClick = rrBounds.contains(this.lastItemTouchPos);
                if (lClick || rClick || llClick || rrClick)
                    Clicked = true;
            } else {
                if (Clicked) {
                    Clicked = false;
                    lClick = lBounds.contains(this.lastItemTouchPos);
                    rClick = rBounds.contains(this.lastItemTouchPos);
                    llClick = llBounds.contains(this.lastItemTouchPos);
                    rrClick = rrBounds.contains(this.lastItemTouchPos);
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

        plusBtn = rrClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(IconName.btnNormal.name());
        minusBtn = llClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(IconName.btnNormal.name());
        plusPlusBtn = rClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(IconName.btnNormal.name());
        minusMinusBtn = lClick ? Sprites.getSprite("btn-pressed") : Sprites.getSprite(IconName.btnNormal.name());

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

        if (mFilterSetEntry.getIcon() != null) {
            float iconHeight = this.getHalfHeight() * 0.8f;
            float iconWidth = iconHeight;
            mFilterSetEntry.getIcon().setBounds(left, MARGIN, iconWidth, iconHeight);
            mFilterSetEntry.getIcon().draw(batch);
            // top += UiSizes.getIconSize() / 1.5;
        }

    }

    private void drawSelectItem(Batch batch) {
        float btnWidth = (getWidth() / 2) - 2 * MARGIN;
        lBounds = new CB_RectF(MARGIN, 0, btnWidth, getHeight());
        lBounds = lBounds.ScaleCenter(0.95f);

        rBounds = new CB_RectF(getWidth() - (btnWidth + MARGIN), 0, btnWidth, getHeight());
        rBounds = rBounds.ScaleCenter(0.95f);

        boolean rClick = false;
        boolean lClick = false;
        if (this.lastItemTouchPos != null) {
            if (this.isPressed) {
                lClick = lBounds.contains(this.lastItemTouchPos);
                rClick = rBounds.contains(this.lastItemTouchPos);

                if (lClick || rClick)
                    Clicked = true;
            } else {
                if (Clicked && selectAllHandler != null) {
                    Clicked = false;
                    lClick = lBounds.contains(this.lastItemTouchPos);
                    rClick = rBounds.contains(this.lastItemTouchPos);
                    if (rClick)
                        selectAllHandler.selectAll();
                    if (lClick)
                        selectAllHandler.deselectAll();
                }
            }
        }

        selectAllBtn = rClick ? Sprites.btnPressed : Sprites.btn;
        deSelectAllBtn = lClick ? Sprites.btnPressed : Sprites.btn;

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
        if (mFilterSetEntry.getIcon() != null) {
            float iconHeight = this.getHeight() * 0.8f;
            float iconWidth = iconHeight;
            float y = (this.getHeight() - iconHeight) / 2f; // MARGIN
            mFilterSetEntry.getIcon().setBounds(left, y, iconWidth, iconHeight);
            // mFilterSetEntry.getIcon().setBounds(left, MARGIN, iconWidth, iconHeight);
            mFilterSetEntry.getIcon().draw(batch);
            left += iconWidth + MARGIN + getLeftWidth();
        }

    }

    private void drawRightChkBox(Batch batch) {
        if (rBounds == null || rChkBounds == null) {
            rBounds = new CB_RectF(getWidth() - getHeight() - 10, 5, getHeight() - 10, getHeight() - 10);// = right Button bounds

            rChkBounds = rBounds.ScaleCenter(0.8f);
        }

        if (chkOff == null) {
            chkOff = Sprites.getSprite("check-off");

            chkOff.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

        }

        chkOff.draw(batch);

    }

    private void plusClick() {
        this.mFilterSetEntry.plusClick();
        setValueFont = true;
        de.droidcachebox.gdx.controls.FilterSetListView.mustSaveFilter = true;
        this.isPressed = false;
        this.lastItemTouchPos = null;
        GL.that.renderOnce();
    }

    private void minusClick() {
        this.mFilterSetEntry.minusClick();
        setValueFont = true;
        de.droidcachebox.gdx.controls.FilterSetListView.mustSaveFilter = true;
        this.isPressed = false;
        this.lastItemTouchPos = null;
        GL.that.renderOnce();
    }

    private void plusPlusClick() {
        this.mFilterSetEntry.plusPlusClick();
        setValueFont = true;
        de.droidcachebox.gdx.controls.FilterSetListView.mustSaveFilter = true;
        this.isPressed = false;
        this.lastItemTouchPos = null;
        GL.that.renderOnce();
    }

    private void minusMinusClick() {
        this.mFilterSetEntry.minusMinusClick();
        setValueFont = true;
        de.droidcachebox.gdx.controls.FilterSetListView.mustSaveFilter = true;
        this.isPressed = false;
        this.lastItemTouchPos = null;
        GL.that.renderOnce();
    }

    private void stateClick() {
        this.mFilterSetEntry.stateClick();
        FilterSetListView.mustSaveFilter = true;
        this.isPressed = false;
        this.lastItemTouchPos = null;
        GL.that.renderOnce();
    }

    int getChecked() {
        return mFilterSetEntry.getState();
    }

    void unCheck() {
        mFilterSetEntry.setState(0);
    }

    void check() {
        mFilterSetEntry.setState(1);
    }

    public double getValue() {
        return mFilterSetEntry.getNumState();
    }

    public void setValue(int value) {
        this.mFilterSetEntry.setState(value);
    }

    public void setValue(double value) {
        this.mFilterSetEntry.setState(value);
    }

    public void setValue(boolean b) {
        this.mFilterSetEntry.setState(b ? 1 : 0);
    }

    @Override
    public FilterSetListViewItem getChild(int i) {
        return mChildList.get(i);
    }

    public int getChildLength() {
        return mChildList.size();
    }

    public boolean getBoolean() {
        return mFilterSetEntry.getState() != 0;
    }
}
