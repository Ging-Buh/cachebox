package CB_UI.GL_UI.Activitys.FilterSettings;

import CB_Core.Types.GpxFilename;
import CB_UI.GL_UI.Activitys.FilterSettings.CategorieListView.CategorieEntry;
import CB_UI_Base.GL_UI.COLOR;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBackground;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL_Input;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class CategorieListViewItem extends ListViewItemBackground {
    private static final SimpleDateFormat postFormater = new SimpleDateFormat("dd/MM/yyyy hh:mm ");
    private static Sprite chkOff;
    private static Sprite chkOn;
    private static Sprite chkNo;
    private static Sprite chkBox;
    private static CB_RectF lPinBounds;
    private static CB_RectF rBounds;
    private static CB_RectF rChkBounds;
    private static float halfSize = 0;
    private static NinePatch btnBack;
    private static NinePatch btnBack_pressed;
    private static Sprite sPinOn;
    private static Sprite sPinOff;
    private static float margin = 0;

    // Draw Methods

    // static Member
    private final ArrayList<CategorieListViewItem> mChildList = new ArrayList<CategorieListViewItem>();
    public CategorieEntry categorieEntry;
    public Vector2 lastItemTouchPos;
    // private Member
    float left;
    float top;
    private BitmapFontCache EntryName;
    private BitmapFontCache EntryDate;
    private BitmapFontCache EntryCount;

    public CategorieListViewItem(CB_RectF rec, int Index, CategorieEntry fne) {
        super(rec, Index, "");

        this.categorieEntry = fne;

    }

    public CategorieEntry getCategorieEntry() {
        return categorieEntry;
    }

    public CategorieListViewItem addChild(CategorieListViewItem item) {
        mChildList.add(item);
        return item;
    }

    public void toggleChildeViewState() {
        if (mChildList != null && mChildList.size() > 0) {
            boolean newState = !mChildList.get(0).isVisible();

            for (CategorieListViewItem tmp : mChildList) {
                tmp.setVisible(newState);
            }
        }

    }

    @Override
    protected void render(Batch batch) {
        if (this.categorieEntry.getItemType() != FilterSetListView.COLLAPSE_BUTTON_ITEM)
            super.render(batch);

        if (isPressed) {
            isPressed = GL_Input.that.getIsTouchDown();
        }

        // initial
        left = getLeftWidth();
        top = this.getHeight() - this.getTopHeight();

        if (rBounds == null || rChkBounds == null || lPinBounds == null) {
            rBounds = new CB_RectF(getWidth() - getHeight() - 10, 5, getHeight() - 10, getHeight() - 10);// =
            // right
            // Button
            // bounds
            halfSize = rBounds.getWidth() / 4;

            rChkBounds = rBounds.ScaleCenter(0.8f);
            lPinBounds = new CB_RectF(rChkBounds);
            lPinBounds.offset(-(getWidth() - (halfSize * 2) - rChkBounds.getWidth()), 0);
        }

        // boolean selected = false;
        // if (this.categorieEntry == CategorieListView.aktCategorieEntry) selected = true;

        switch (this.categorieEntry.getItemType()) {
            case FilterSetListView.COLLAPSE_BUTTON_ITEM:
                drawCollapseButtonItem(batch);
                break;
            case FilterSetListView.CHECK_ITEM:
                drawChkItem(batch);
                break;
            case FilterSetListView.THREE_STATE_ITEM:
                drawThreeStateItem(batch);
                break;

        }
        // draw Name
        if (EntryName == null) {

            GpxFilename file = categorieEntry.getFile();

            String Name = "";
            String Date = "";
            String Count = "";

            if (file != null) {
                Name = file.GpxFileName;
                Date = postFormater.format(file.Imported);
                Count = String.valueOf(file.CacheCount);
            } else {
                Name = categorieEntry.getCatName();
                Date = postFormater.format(categorieEntry.getCat().LastImported());
                Count = String.valueOf(categorieEntry.getCat().CacheCount());
            }

            Count += " Caches";

            EntryName = new BitmapFontCache(Fonts.getNormal());
            EntryName.setColor(COLOR.getFontColor());
            EntryName.setText(Name, left + UI_Size_Base.that.getMargin(), top);

            top = margin + margin + Fonts.MeasureSmall(Count).height;

            EntryDate = new BitmapFontCache(Fonts.getSmall());
            EntryDate.setColor(COLOR.getFontColor());
            EntryDate.setText(Date, left + UI_Size_Base.that.getMargin(), top);

            float measure = Fonts.Measure(Count).width;
            EntryCount = new BitmapFontCache(Fonts.getSmall());
            EntryCount.setColor(COLOR.getFontColor());
            EntryCount.setText(Count, rBounds.getX() - margin - measure, top);

        }

        if (EntryName != null)
            EntryName.draw(batch);
        if (EntryCount != null)
            EntryCount.draw(batch);
        if (EntryDate != null)
            EntryDate.draw(batch);

        // draw Count
        // ActivityUtils.drawStaticLayout(batch, layoutEntryCount, left, top);

        // draw Import Date
        top += 52;
        // ActivityUtils.drawStaticLayout(batch, layoutEntryDate, left, top);

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

        drawPin(batch);
        drawChkItem(batch);

    }

    private void drawPin(Batch batch) {
        margin = UI_Size_Base.that.getMargin();
        float iconHeight = this.getHeight() * 0.6f;
        float iconWidth = iconHeight;

        if (this.getCategorieEntry().getCat().pinned) {
            if (sPinOn == null) {
                sPinOn = Sprites.getSprite("pin-icon");
                sPinOn.setBounds(left, UI_Size_Base.that.getMargin(), iconWidth, iconHeight);
            }

            sPinOn.draw(batch);
        } else {
            if (sPinOff == null) {
                sPinOff = Sprites.getSprite("pin-icon-disable");
                sPinOff.setBounds(left, UI_Size_Base.that.getMargin(), iconWidth, iconHeight);
            }
            sPinOff.draw(batch);

        }

        left += iconWidth + UI_Size_Base.that.getMargin();

    }

    private void drawChkItem(Batch batch) {
        if (this.categorieEntry == null)
            return;

        drawIcon(batch);
        drawRightChkBox(batch);
        int ChkState = 0;
        if (this.categorieEntry.getItemType() == FilterSetListView.COLLAPSE_BUTTON_ITEM) {
            ChkState = this.categorieEntry.getCat().getCheck();
        } else {
            ChkState = this.categorieEntry.getState();
        }

        if (ChkState == 1) {
            if (chkOn == null) {
                chkOn = Sprites.getSprite("check-on");

                chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

            }

            chkOn.draw(batch);
        }
        if (ChkState == -1) {
            if (chkOff == null) {
                chkOff = Sprites.getSprite("check-disable");

                chkOff.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

            }
            chkOff.draw(batch);
        }
    }

    private void drawThreeStateItem(Batch batch) {
        drawIcon(batch);
        drawRightChkBox(batch);

        if (this.categorieEntry.getCat().getCheck() == 1) {
            if (chkOn == null) {
                chkOn = Sprites.getSprite("check-on");
                chkOn.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
            }

            chkOn.draw(batch);
        } else if (this.categorieEntry.getCat().getCheck() == 0) {
            if (chkNo == null) {
                chkNo = Sprites.getSprite(IconName.DELETE.name());
                chkNo.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());
            }
            chkNo.draw(batch);
        }
    }

    private void drawIcon(Batch batch) {
        // if (categorieEntry.getIcon() != null) ActivityUtils.PutImageTargetHeight(batch, categorieEntry.getIcon(), left, top,
        // UiSizes.getIconSize());
        // left += UiSizes.getIconAddCorner();

    }

    private void drawRightChkBox(Batch batch) {

        if (rBounds == null || rChkBounds == null) {
            rBounds = new CB_RectF(getWidth() - getHeight() - margin, margin, getHeight() - margin, getHeight() - margin);// = right Button
            // bounds

            rChkBounds = rBounds.ScaleCenter(0.8f);
        }

        if (chkBox == null) {
            chkBox = Sprites.getSprite("check-off");

            chkBox.setBounds(rChkBounds.getX(), rChkBounds.getY(), rChkBounds.getWidth(), rChkBounds.getHeight());

        }

        chkBox.draw(batch);

    }

    public void plusClick() {
        this.categorieEntry.plusClick();
    }

    public void minusClick() {
        this.categorieEntry.minusClick();
    }

    public void stateClick() {
        this.categorieEntry.stateClick();
    }

    public void setValue(int value) {

        this.categorieEntry.setState(value);

    }

    public void setValue(float value) {
        this.categorieEntry.setState(value);

    }

    public int getChecked() {
        return categorieEntry.getState();
    }

    public float getValue() {
        return (float) categorieEntry.getNumState();
    }

    public void setValue(boolean b) {
        this.categorieEntry.setState(b ? 1 : 0);
    }

    @Override
    public CategorieListViewItem getChild(int i) {
        return mChildList.get(i);
    }

    public int getChildLength() {
        return mChildList.size();
    }

    public boolean getBoolean() {
        if (categorieEntry.getState() == 0)
            return false;

        return true;
    }

}
