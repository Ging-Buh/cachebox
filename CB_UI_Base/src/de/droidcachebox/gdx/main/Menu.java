package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;

import java.util.ArrayList;

import de.droidcachebox.WrapType;
import de.droidcachebox.gdx.COLOR;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
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
import de.droidcachebox.utils.log.Log;

public class Menu extends ButtonDialog {

    private static final int ANIMATION_DURATION = 1200;
    private static float mMoreMenuToggleButtonWidth = -1;
    public float itemHeight = -1f;
    protected boolean autoClose;
    protected boolean singleSelection; // true: only one option can be selected
    private ArrayList<MenuItem> menuItems = new ArrayList<>();
    private V_ListView menuItemsListView;
    private Menu mMoreMenu = null;
    private boolean mMoreMenuVisible = false;
    private CB_Button mMoreMenuToggleButton;
    private CB_Label mMoreMenuLabel;
    private int mAnimationState = -1; // -1=not initialized 0=left 1=to left 2=to right 3=right
    private int itemsCount = -1;
    private float animateStartTime;
    private boolean isMoreMenu = false;
    private String mMoreMenuTextRight = "";
    private String mMoreMenuTextLeft = "";
    private Menu mParentMenu;
    protected OnClickListener closeMenuOnClick = new OnClickListener() {
        @Override
        public boolean onClick(GL_View_Base view, int x, int y, int pointer, int button) {
            GL.that.closeDialog(Menu.this);
            if (isMoreMenu)
                GL.that.closeDialog(mParentMenu);
            return false;
        }
    };
    private boolean mMoreMenuIsInitial = false;
    private int Level = 0;

    public Menu(String titleTranslationId) {
        this(Translation.get(titleTranslationId), "");
    }

    public Menu(String title, String dummy) {
        super(GL_UISizes.menuRectangle, title);
        setTitle("<= " + title);
        createMenu();
    }

    private void createMenu() {
        autoClose = true;
        singleSelection = false;

        if (itemHeight == -1f)
            itemHeight = UiSizes.getInstance().getButtonHeight();
        menuItemsListView = new V_ListView(this, "MenuList");
        menuItemsListView.setSize(getContentSize());
        menuItemsListView.setZeroPos();
        menuItemsListView.setDisposeFlag(false);
        addChild(mMoreMenu);
        initialDialog();
        // setClickHandler(...); // auf titel geklickt
    }

    @Override
    public void close() {
        GL.that.closeDialog(this);
        if (isMoreMenu)
            GL.that.closeDialog(mParentMenu);
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
        mMoreMenu.Level = Level + 1;
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
        menuItemsListView.notifyDataSetChanged();
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
        mMoreMenu.setWidth(getWidth());
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

            if (menuItems.size() != itemsCount) {
                // new Hight calculation
                itemsCount = menuItems.size();
                float higherValue = mTitleHeight + mHeaderHeight + mFooterHeight + (margin * 2);
                for (MenuItem item : menuItems) {
                    higherValue += item.getHeight() + menuItemsListView.getDividerHeight();
                }
                float freiraumObenPlusUnten = 0; // 2 * UiSizes.getInstance().getButtonHeight();
                if (higherValue > UiSizes.getInstance().getWindowHeight() - freiraumObenPlusUnten) {
                    higherValue = UiSizes.getInstance().getWindowHeight() - freiraumObenPlusUnten;
                }
                setSize(GL_UISizes.uiLeft.getWidth(), higherValue);

                // initial more menus
                if (mMoreMenu != null)
                    mMoreMenu.initialize();

            }
        } else {
            setSize(mParentMenu.getWidth(), mParentMenu.getHeight());
        }

        if (mMoreMenuToggleButtonWidth == -1) {
            float mesuredLblHeigt = Fonts.measureForSmallFont("T").height;
            mMoreMenuToggleButtonWidth = Sprites.btn.getLeftWidth() + Sprites.btn.getRightWidth() + (mesuredLblHeigt * 1.5f);
        }

        menuItemsListView.setSize(getContentSize());
        addChild(menuItemsListView);
        menuItemsListView.setAdapter(new MenuItemsListViewAdapter());

        if (mMoreMenu != null && !mMoreMenuIsInitial) {
            mMoreMenu.initialize();
            mMoreMenu.setVisible(false);
            mMoreMenu.setZeroPos();
            mMoreMenu.setHeight(getHeight());
            mMoreMenu.setWidth(0);
            mMoreMenu.setY(0 - mFooterHeight);

            mMoreMenu.setBackground(new ColorDrawable(COLOR.getMenuBackColor()));

            addChild(mMoreMenu);

            mMoreMenuToggleButton = new CB_Button("");
            mMoreMenuToggleButton.setWidth(mMoreMenuToggleButtonWidth);
            mMoreMenuToggleButton.setHeight(getContentSize().getHeight());
            float MenuY = mParentMenu != null ? mParentMenu.getY() : getY();
            mMoreMenuToggleButton.setY(MenuY + mFooterHeight);
            GL.that.getDialogLayer().addChild(mMoreMenuToggleButton);
            mMoreMenuToggleButton.setClickHandler((view, x, y, pointer, button) -> {
                toggleMoreMenu();
                return true;
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
        float cx = (UiSizes.getInstance().getWindowWidth() / 2f) - getHalfWidth();
        float cy = (UiSizes.getInstance().getWindowHeight() / 2f) - getHalfHeight();
        setPos(cx, cy);

        layout();
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);

        if (mMoreMenu == null || !mMoreMenuIsInitial || mMoreMenuToggleButton == null)
            return;

        if (mAnimationState > -1)
            mMoreMenuToggleButton.setY(getWorldRec().getY() + mFooterHeight);

        // Animation calculation
        if (mAnimationState == 1 || mAnimationState == 2) {
            float targetValue = getWidth() * 1.5f;

            float animateValue = (1 + ((int) ((GL.that.getStateTime() - animateStartTime) * 1000) % ANIMATION_DURATION) / (ANIMATION_DURATION / targetValue));

            if (mAnimationState == 1) {
                if (animateValue >= getWidth() - 10) {
                    animateValue = getWidth();
                    mAnimationState = 0;
                }
                mMoreMenu.setSize(animateValue, getHeight());
            } else {
                if (animateValue >= getWidth() - 10) {
                    animateValue = getWidth();
                    mMoreMenu.setVisible(false);
                    mAnimationState = 3;
                }
                mMoreMenu.setSize(getWidth() - animateValue, getHeight());
            }

            layout();
            GL.that.renderOnce();
        } else if (mAnimationState == -1) {
            if (mMoreMenu != null) {
                mMoreMenuToggleButton.setHeight(getContentSize().getHeight());
                mAnimationState = 3;
                layout();
            }

        }

    }

    public MenuItem addMenuItem(String titleTranslationId, String addUnTranslatedPart, Object icon, OnClickListener onClickListener) {
        String title = ((titleTranslationId.length() == 0) ? "" : Translation.get(titleTranslationId)) + addUnTranslatedPart;
        if (title.contains("\n")) {
            CB_Label tmp = new CB_Label("");
            tmp.setWidth(menuItemsListView.getWidth());
            tmp.setMultiLineText(title);
            itemHeight = tmp.getTextHeight();
            // ItemHeight = (title.split("\n")).length * UI_Size_Base.that.getButtonHeight();
        }
        MenuItem item = new MenuItem(new SizeF(menuItemsListView.getWidth(), itemHeight), menuItems.size(), titleTranslationId);
        item.setTitle(title);
        if (icon != null) {
            if (icon instanceof Sprite) {
                item.setIcon(new SpriteDrawable((Sprite) icon));
            } else
                item.setIcon((Drawable) icon);
        }
        item.setClickHandler(onClickListener);
        menuItems.add(item);
        menuItemsListView.notifyDataSetChanged();
        return item;
    }

    public void tickCheckBoxes(MenuItem clickedItem) {
        if (clickedItem.mIsCheckable) {
            // for update the presentation (without recreating the menu)
            if (singleSelection) {
                boolean newCheck = !clickedItem.isChecked();
                // only one item can be checked : remove all checks
                for (MenuItem smi : menuItems) {
                    if (smi != null) {
                        if (smi.isChecked()) {
                            smi.setChecked(false);
                        }
                    }
                }
                clickedItem.setChecked(newCheck); // toggle clicked item
            } else {
                clickedItem.toggleCheck();
            }
        }
    }

    public MenuItem addMenuItem(String titleTranslationId, String addUnTranslatedPart, Sprite icon, Runnable runnable) {
        return addMenuItem(titleTranslationId, addUnTranslatedPart, icon,
                (v, x, y, pointer, button) -> {
                    if (autoClose)
                        close();
                    tickCheckBoxes((MenuItem) v);
                    if (runnable != null) runnable.run();
                    return true;
                });
    }

    public MenuItem addMenuItem(String titleTranslationId, Sprite icon, Runnable runnable) {
        return addMenuItem(titleTranslationId, "", icon, runnable);
    }

    public void addCheckableMenuItem(String titleTranslationId, boolean checked, Runnable runnable) {
        addCheckableMenuItem(titleTranslationId, null, checked, runnable);
    }

    public void addCheckableMenuItem(String titleTranslationId, Sprite icon, boolean checked, Runnable runnable) {
        MenuItem item = addMenuItem(titleTranslationId, icon, runnable);
        item.setCheckable(true);
        item.setChecked(checked);
    }

    private void layout() {

        if (menuItemsListView == null || menuItemsListView.isDisposed())
            return;

        try {
            float WithOffset = isMoreMenu ? mMoreMenuToggleButtonWidth : 0;
            /*
            if (isMoreMenu && mMoreMenu != null)
                WithOffset = mMoreMenuToggleButtonWidth;
             */
            if (!isMoreMenu && mMoreMenu != null)
                WithOffset = mMoreMenuToggleButtonWidth / 1.1f;
            if (menuItemsListView != null) {
                menuItemsListView.setSize(getContentSize().getWidth() - WithOffset, getContentSize().getHeight());
                menuItemsListView.setZeroPos();
                if (isMoreMenu && mMoreMenu != null)
                    WithOffset /= 2;
                if (!isMoreMenu && mMoreMenu != null)
                    WithOffset = 0;
                menuItemsListView.setX(WithOffset);
            }

            // Alle Items in der Breite anpassen
            if (menuItemsListView != null) {
                float w = menuItemsListView.getWidth();
                for (MenuItem item : menuItems) {
                    item.setWidth(w);
                }
            }

            if (mMoreMenu != null && mMoreMenuToggleButton != null)
                switch (mAnimationState) {
                    case 0:
                        setWidth(getLeve0_Width());
                        mMoreMenu.setWidth(getLeve0_Width());
                        mMoreMenu.setX(-getLeftWidth() - getRightWidth() - 2.5f);
                        // TODO die -2,5f müssen auf meinem S3 sein,
                        // damit die linke Position passt auf dem desktop sind es 0 auf anderen?
                        // ich habe hier den zusammen hang noch nicht finden können
                        mMoreMenuToggleButton.setX(getLevel0_x() - mMoreMenuToggleButton.getHalfWidth() + (margin * 2));
                        mMoreMenuLabel.setText(mMoreMenuTextLeft);
                        break;
                    case 1:
                        mMoreMenu.setX(getWidth() - mMoreMenu.getWidth() - getLeftWidth());
                        mMoreMenuToggleButton.setX(getLevel0_x() + mMoreMenu.getX() - getLeftWidth());
                        break;
                    case 2:
                        mMoreMenu.setX(getWidth() - mMoreMenu.getWidth() - getLeftWidth());
                        mMoreMenuToggleButton.setX(getLevel0_x() + mMoreMenu.getX() - mMoreMenuToggleButton.getHalfWidth());
                        break;
                    case 3:
                        mMoreMenu.setWidth(0);
                        mMoreMenuToggleButton.setX(getLevel0_maxX() - mMoreMenuToggleButton.getHalfWidth() - (margin * 2));
                        break;
                }
        } catch (Exception ex) {
            Log.err("Menu layout", ex);
        }
    }

    @Override
    public void show() {
        layout();
        super.show();
    }

    public void setPrompt(String Prompt) {
        // set Title with full width, add many blanks: that is bad
        // setTitle(Prompt + "                                                       ");
        setTitle(Prompt);
        layout();
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        layout();
    }

    public ArrayList<MenuItem> getItems() {
        return menuItems;
    }

    public void addItems(ArrayList<MenuItem> items) {
        addItems(items, false);
    }

    public void addItems(ArrayList<MenuItem> items, boolean setEnabled) {
        for (MenuItem menuItem : items) {
            if (menuItem.getOnClickListener() == null)
                menuItem.setClickHandler(closeMenuOnClick);
            if (setEnabled)
                menuItem.setEnabled(true);
            menuItems.add(menuItem);
            menuItemsListView.notifyDataSetChanged();
        }
    }

    public void addDivider() {
        MenuItemDivider item = new MenuItemDivider(menuItems.size(), "Menu Divider");
        item.setHeight(itemHeight / 5);
        item.setEnabled(false);
        menuItems.add(item);
        menuItemsListView.notifyDataSetChanged();
    }

    /**
     * Die indexes der Items werden neu erstellt.
     */
    public int reorganizeIndexes() {
        int index = 0;
        for (MenuItem item : menuItems) {
            item.setIndex(index++);
        }
        return index;
    }

    private float getLevel0_x() {
        if (mParentMenu == null)
            return getX();
        return mParentMenu.getLevel0_x();
    }

    private float getLevel0_maxX() {
        if (mParentMenu == null)
            return getMaxX();
        return mParentMenu.getLevel0_maxX();
    }

    private float getLeve0_Width() {
        if (mParentMenu == null)
            return getWidth();
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

        if (menuItems != null) {
            for (MenuItem it : menuItems) {
                it.dispose();
            }
            menuItems.clear();
        }
        menuItems = null;

        if (menuItemsListView != null) {
            menuItemsListView.dispose();
        }
        menuItemsListView = null;
        super.dispose();
    }

    private class MenuItemsListViewAdapter implements Adapter {

        @Override
        public int getCount() {
            return menuItems.size();
        }

        @Override
        public ListViewItemBase getView(int position) {
            return menuItems.get(position);
        }

        @Override
        public float getItemSize(int position) {
            if (menuItems == null || menuItems.size() == 0 || menuItems.size() < position)
                return 0;
            return menuItems.get(position).getHeight();
        }
    }

}
