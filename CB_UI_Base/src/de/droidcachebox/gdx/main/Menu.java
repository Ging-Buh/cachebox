package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import de.droidcachebox.WrapType;
import de.droidcachebox.gdx.*;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.controls.messagebox.ButtonDialog;
import de.droidcachebox.gdx.graphics.ColorDrawable;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.GL_UISizes;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.translation.Translation;

import java.util.ArrayList;

public class Menu extends ButtonDialog {

    private static final int ANIMATION_DURATION = 1200;
    private static float mMoreMenuToggleButtonWidth = -1;
    private static CB_RectF sMenuRec = null;
    private static boolean MENU_REC_IsInitial = false;
    public float ItemHeight = -1f;
    public ArrayList<MenuItem> mItems = new ArrayList<>();
    protected boolean autoClose;
    protected boolean singleSelection; // true: only one option can be selected
    private V_ListView mListView;
    private Menu mMoreMenu = null;
    private boolean mMoreMenuVisible = false;
    private CB_Button mMoreMenuToggleButton;
    private CB_Label mMoreMenuLabel;
    /**
     * -1=not initial<br>
     * 0=left<br>
     * 1=to left<br>
     * 2= to right<br>
     * 3=right
     */
    private int mAnimationState = -1;// -1=not initial 0=left 1=to left 2= to right 3=right
    private int itemsCount = -1;
    private float animateStartTime;
    private boolean isMoreMenu = false;
    private String mMoreMenuTextRight = "";
    private String mMoreMenuTextLeft = "";
    private Menu mParentMenu;
    protected OnClickListener menuItemClickListener = new OnClickListener() {
        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
            GL.that.closeDialog(Menu.this);
            if (isMoreMenu)
                GL.that.closeDialog(mParentMenu);
            return false;
        }
    };
    private boolean mMoreMenuIsInitial = false;
    private int Level = 0;

    public Menu(String titleTranlationId) {
        super(getMenuRec(), titleTranlationId);
        setTitle("<= " + Translation.get(titleTranlationId));
        autoClose = true;
        singleSelection = false;

        if (ItemHeight == -1f)
            ItemHeight = UiSizes.getInstance().getButtonHeight();
        mListView = new V_ListView(this, "MenuList");
        mListView.setSize(this.getContentSize());
        mListView.setZeroPos();
        mListView.setDisposeFlag(false);
        this.addChild(mMoreMenu);
        initialDialog();
        // setClickHandler(...); // auf titel geklickt
    }

    public static CB_RectF getMenuRec() {
        if (!MENU_REC_IsInitial) {
            float sollWidth = GL_UISizes.UI_Left.getWidth();

            sollWidth *= 0.83;
            sMenuRec = new CB_RectF(new SizeF(sollWidth, 50));
            MENU_REC_IsInitial = true;
        }
        return sMenuRec;
    }

    @Override
    public void close() {
        GL.that.closeDialog(this);
        if (isMoreMenu)
            GL.that.closeDialog(mParentMenu);
    }

    public void setAutoClose(boolean value) {
        autoClose = value;
    }

    public void addMoreMenu(Menu menu, String TextLeft, String TextRight) {
        if (menu == null) {
            mMoreMenuTextRight = "";
            mMoreMenuTextLeft = "";
            mMoreMenu = null;
            return;
        }
        mMoreMenuTextRight = TextRight;
        mMoreMenuTextLeft = TextLeft;
        mMoreMenu = menu;
        mMoreMenu.isMoreMenu = true;
        mMoreMenu.setParentMenu(this);
        mMoreMenu.setVisible(false);
        mMoreMenu.Level = this.Level + 1;
        mMoreMenu.setBackground(new ColorDrawable(COLOR.getMenuBackColor()));
    }

    public Menu getMoreMenu() {
        return mMoreMenu;
    }

    public String getTextLeftMoreMenu() {
        return mMoreMenuTextLeft;
    }

    public String getTextRightMoreMenu() {
        return mMoreMenuTextRight;
    }

    private void setParentMenu(Menu menu) {
        mParentMenu = menu;
    }

    private void toggleMoreMenu() {
        mMoreMenuVisible = !mMoreMenuVisible;
        if (mMoreMenuVisible) {
            showMoreMenu();
        } else {
            hideMoreMenu();
        }
        animateStartTime = GL.that.getStateTime();
        mListView.notifyDataSetChanged();
    }

    private void showMoreMenu() {
        mMoreMenu.setVisible(true);
        mAnimationState = 1;
        mMoreMenu.setWidth(0);
        layout();
        int index = GL.that.getDialogLayer().getchilds().indexOf(mMoreMenuToggleButton);
        GL.that.getDialogLayer().getchilds().MoveItemLast(index);
    }

    private void hideMoreMenu() {
        mAnimationState = 2;
        mMoreMenu.setWidth(this.getWidth());
        layout();
        int index = GL.that.getDialogLayer().getchilds().indexOf(mMoreMenuToggleButton);
        GL.that.getDialogLayer().getchilds().MoveItemLast(index);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (mMoreMenuToggleButton != null) {
            if (visible)
                layout();
            mMoreMenuToggleButton.setVisible(visible);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        if (!isMoreMenu) {
            // Menu level 1

            if (mItems.size() != itemsCount) {
                // new Hight calculation
                itemsCount = mItems.size();
                float higherValue = mTitleHeight + mHeaderHeight + mFooterHeight + (margin * 2);
                for (MenuItem item : mItems) {
                    higherValue += item.getHeight() + mListView.getDividerHeight();
                }
                float freiraumObenPlusUnten = 0; // 2 * UiSizes.getInstance().getButtonHeight();
                if (higherValue > UiSizes.getInstance().getWindowHeight() - freiraumObenPlusUnten) {
                    higherValue = UiSizes.getInstance().getWindowHeight() - freiraumObenPlusUnten;
                }
                float MenuWidth = GL_UISizes.UI_Left.getWidth();
                this.setSize(MenuWidth, higherValue);

                // initial more menus
                if (mMoreMenu != null)
                    mMoreMenu.initialize();

            }
        } else {
            this.setSize(mParentMenu.getWidth(), mParentMenu.getHeight());
        }

        if (mMoreMenuToggleButtonWidth == -1) {
            float mesuredLblHeigt = Fonts.MeasureSmall("T").height;
            mMoreMenuToggleButtonWidth = Sprites.btn.getLeftWidth() + Sprites.btn.getRightWidth() + (mesuredLblHeigt * 1.5f);
        }

        mListView.setSize(this.getContentSize());

        this.addChild(mListView);
        mListView.setBaseAdapter(new CustomAdapter());

        if (mMoreMenu != null && !mMoreMenuIsInitial) {
            mMoreMenu.initialize();
            mMoreMenu.setVisible(false);
            mMoreMenu.setZeroPos();
            mMoreMenu.setHeight(this.getHeight());
            mMoreMenu.setWidth(0);
            mMoreMenu.setY(0 - mFooterHeight);

            mMoreMenu.setBackground(new ColorDrawable(COLOR.getMenuBackColor()));

            this.addChild(mMoreMenu);

            mMoreMenuToggleButton = new CB_Button("");
            mMoreMenuToggleButton.setWidth(mMoreMenuToggleButtonWidth);
            mMoreMenuToggleButton.setHeight(this.getContentSize().height);
            float MenuY = mParentMenu != null ? mParentMenu.getY() : this.getY();
            mMoreMenuToggleButton.setY(MenuY + mFooterHeight);
            GL.that.getDialogLayer().addChild(mMoreMenuToggleButton);
            mMoreMenuToggleButton.setClickHandler(new OnClickListener() {

                @Override
                public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                    toggleMoreMenu();
                    return true;
                }
            });

            mMoreMenuLabel = new CB_Label(mMoreMenuTextRight, Fonts.getSmall(), COLOR.getFontColor(), WrapType.SINGLELINE).setHAlignment(HAlignment.CENTER);
            // mMoreMenuLabel.setRec(mMoreMenuToggleButton);
            mMoreMenuLabel.setWidth(mMoreMenuToggleButton.getHeight());
            mMoreMenuLabel.setHeight(mMoreMenuToggleButton.getWidth());
            mMoreMenuLabel.setX(mMoreMenuToggleButton.getWidth());
            mMoreMenuLabel.setY(0);
            mMoreMenuLabel.setOrigin(0, 0);
            mMoreMenuLabel.setRotate(90);
            mMoreMenuLabel.withoutScissor = true;
            mMoreMenuToggleButton.addChild(mMoreMenuLabel);
            mMoreMenuIsInitial = true;
        }

        // set display center pos
        float cx = (UiSizes.getInstance().getWindowWidth() / 2) - this.getHalfWidth();
        float cy = (UiSizes.getInstance().getWindowHeight() / 2) - this.getHalfHeight();
        this.setPos(cx, cy);

        layout();
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);

        if (mMoreMenu == null || !mMoreMenuIsInitial || mMoreMenuToggleButton == null)
            return;

        if (mAnimationState > -1)
            mMoreMenuToggleButton.setY(this.getWorldRec().getY() + mFooterHeight);

        // Animation calculation
        if (mAnimationState == 1 || mAnimationState == 2) {
            float targetValue = this.getWidth() * 1.5f;

            float animateValue = (1 + ((int) ((GL.that.getStateTime() - animateStartTime) * 1000) % ANIMATION_DURATION) / (ANIMATION_DURATION / targetValue));

            if (mAnimationState == 1) {
                if (animateValue >= this.getWidth() - 10) {
                    animateValue = this.getWidth();
                    mAnimationState = 0;
                }
                mMoreMenu.setSize(animateValue, this.getHeight());
            } else {
                if (animateValue >= this.getWidth() - 10) {
                    animateValue = this.getWidth();
                    mMoreMenu.setVisible(false);
                    mAnimationState = 3;
                }
                mMoreMenu.setSize(this.getWidth() - animateValue, this.getHeight());
            }

            layout();
            GL.that.renderOnce();
        } else if (mAnimationState == -1) {
            if (mMoreMenu != null) {
                mMoreMenuToggleButton.setHeight(this.getContentSize().height);
                mAnimationState = 3;
                layout();
            }

        }

    }

    public MenuItem addMenuItem(String titleTranlationId, String addUnTranslatedPart, Object icon, OnClickListener onClickListener) {
        String title = ((titleTranlationId.length() == 0) ? "" : Translation.get(titleTranlationId)) + addUnTranslatedPart;
        if (title.contains("\n")) {
            CB_Label tmp = new CB_Label("");
            tmp.setWidth(mListView.getWidth());
            tmp.setMultiLineText(title);
            ItemHeight = tmp.getTextHeight();
            // ItemHeight = (title.split("\n")).length * UI_Size_Base.that.getButtonHeight();
        }
        MenuItem item = new MenuItem(new SizeF(mListView.getWidth(), ItemHeight), mItems.size(), titleTranlationId);
        item.setTitle(title);
        if (icon != null) {
            if (icon instanceof Sprite) {
                item.setIcon(new SpriteDrawable((Sprite) icon));
            } else
                item.setIcon((Drawable) icon);
        }
        item.setClickHandler(onClickListener);
        mItems.add(item);
        mListView.notifyDataSetChanged();
        return item;
    }

    public void tickCheckBoxes(MenuItem clickedItem) {
        if (clickedItem.mIsCheckable) {
            // for update the presentation (without recreating the menu)
            if (singleSelection) {
                boolean newCheck = !clickedItem.isChecked();
                // only one item can be checked : remove all checks
                for (MenuItem smi : this.mItems) {
                    if (smi instanceof MenuItem) {
                        MenuItem tmi = smi;
                        if (tmi.isChecked()) {
                            tmi.setChecked(false);
                        }
                    }
                }
                clickedItem.setChecked(newCheck); // toggle clicked item
            } else {
                clickedItem.toggleCheck();
            }
        }
    }

    public MenuItem addMenuItem(String titleTranlationId, String addUnTranslatedPart, Sprite icon, Runnable runnable) {
        return addMenuItem(titleTranlationId, addUnTranslatedPart, icon,
                (v, x, y, pointer, button) -> {
                    if (autoClose)
                        close();
                    tickCheckBoxes((MenuItem) v);
                    if (runnable != null) runnable.run();
                    return true;
                });
    }

    public MenuItem addMenuItem(String titleTranlationId, Sprite icon, Runnable runnable) {
        return addMenuItem(titleTranlationId, "", icon, runnable);
    }

    public MenuItem addCheckableMenuItem(String titleTranlationId, boolean checked, Runnable runnable) {
        return addCheckableMenuItem(titleTranlationId, null, checked, runnable);
    }

    public MenuItem addCheckableMenuItem(String titleTranlationId, Sprite icon, boolean checked, Runnable runnable) {
        MenuItem item = addMenuItem(titleTranlationId, icon, runnable);
        item.setCheckable(true);
        item.setChecked(checked);
        return item;
    }

    private void layout() {

        if (mListView == null || mListView.isDisposed())
            return;

        try {
            float WithOffset = isMoreMenu ? mMoreMenuToggleButtonWidth : 0;
            if (isMoreMenu && mMoreMenu != null)
                WithOffset = mMoreMenuToggleButtonWidth;
            if (!isMoreMenu && mMoreMenu != null)
                WithOffset = mMoreMenuToggleButtonWidth / 1.1f;
            if (mListView != null) {
                mListView.setSize(this.getContentSize().width - WithOffset, this.getContentSize().height);
                mListView.setZeroPos();
                if (isMoreMenu && mMoreMenu != null)
                    WithOffset /= 2;
                if (!isMoreMenu && mMoreMenu != null)
                    WithOffset = 0;
                mListView.setX(WithOffset);
            }

            // Alle Items in der Breite anpassen
            float w = mListView.getWidth();
            for (MenuItem item : mItems) {
                item.setWidth(w);
            }

            if (mMoreMenuToggleButton != null) {

                switch (mAnimationState) {
                    case 0:
                        this.setWidth(getLeve0_Width());
                        mMoreMenu.setWidth(getLeve0_Width());
                        mMoreMenu.setX(-this.getLeftWidth() - this.getRightWidth() - 2.5f);
                        // TODO die -2,5f müssen auf meinem S3 sein,
                        // damit die linke Position passt auf dem desktop sind es 0 auf anderen?
                        // ich habe hier den zusammen hang noch nicht finden können
                        mMoreMenuToggleButton.setX(getLevel0_x() - mMoreMenuToggleButton.getHalfWidth() + (margin * 2));

                        mMoreMenuLabel.setText(mMoreMenuTextLeft);
                        break;
                    case 1:
                        mMoreMenu.setX(this.getWidth() - mMoreMenu.getWidth() - this.getLeftWidth());
                        mMoreMenuToggleButton.setX(getLevel0_x() + mMoreMenu.getX() - this.getLeftWidth());
                        break;
                    case 2:
                        mMoreMenu.setX(this.getWidth() - mMoreMenu.getWidth() - this.getLeftWidth());
                        mMoreMenuToggleButton.setX(getLevel0_x() + mMoreMenu.getX() - mMoreMenuToggleButton.getHalfWidth());
                        break;
                    case 3:
                        mMoreMenu.setWidth(0);
                        mMoreMenuToggleButton.setX(getLevel0_maxX() - mMoreMenuToggleButton.getHalfWidth() - (margin * 2));

                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        layout();
        super.show();
    }

    public void setPrompt(String Prompt) {
        // set Title with full width, add many blanks: that is bad
        // this.setTitle(Prompt + "                                                       ");
        this.setTitle(Prompt);
        layout();
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        layout();
    }

    public ArrayList<MenuItem> getItems() {
        return mItems;
    }

    public void addItems(ArrayList<MenuItem> items) {
        addItems(items, false);
    }

    public void addItems(ArrayList<MenuItem> items, boolean setEnabled) {
        for (MenuItem menuItem : items) {
            if (menuItem.getOnClickListener() == null)
                menuItem.setClickHandler(menuItemClickListener);
            if (setEnabled)
                menuItem.setEnabled(true);
            mItems.add(menuItem);
            mListView.notifyDataSetChanged();
        }
    }

    public void addDivider() {
        MenuItemDivider item = new MenuItemDivider(mItems.size(), "Menu Devider");
        item.setHeight(ItemHeight / 5);
        item.setEnabled(false);
        mItems.add(item);
        mListView.notifyDataSetChanged();
    }

    /**
     * Die indexes der Items werden neu erstellt.
     */
    public int reorganizeIndexes() {
        int Index = 0;
        for (MenuItem item : mItems) {
            item.setIndex(Index++);
        }
        return Index;
    }

    private float getLevel0_x() {
        if (mParentMenu == null)
            return this.getX();
        return mParentMenu.getLevel0_x();
    }

    private float getLevel0_maxX() {
        if (mParentMenu == null)
            return this.getMaxX();
        return mParentMenu.getLevel0_maxX();
    }

    private float getLeve0_Width() {
        if (mParentMenu == null)
            return this.getWidth();
        return mParentMenu.getLeve0_Width();
    }

    @Override
    public void dispose() {
        mMoreMenuTextRight = null;
        mMoreMenuTextLeft = null;
        mParentMenu = null;

        if (mMoreMenu != null) {
            mMoreMenu.dispose();
        }
        mMoreMenu = null;

        if (mMoreMenuToggleButton != null) {
            mMoreMenuToggleButton.dispose();
        }
        mMoreMenuToggleButton = null;

        if (mMoreMenuLabel != null) {
            mMoreMenuLabel.dispose();
        }
        mMoreMenuLabel = null;

        if (mItems != null) {
            for (MenuItem it : mItems) {
                it.dispose();
            }
            mItems.clear();
        }
        mItems = null;

        if (mListView != null) {
            mListView.dispose();
        }
        mListView = null;
        super.dispose();
    }

    public class CustomAdapter implements Adapter {

        @Override
        public ListViewItemBase getView(int position) {
            ListViewItemBase v = mItems.get(position);
            return v;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public float getItemSize(int position) {
            if (mItems == null || mItems.size() == 0 || mItems.size() < position)
                return 0;
            return mItems.get(position).getHeight();
        }
    }

}
