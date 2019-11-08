package de.droidcachebox.gdx.controls;

import de.droidcachebox.Config;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.H_ListView;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.main.QuickActions;
import de.droidcachebox.gdx.main.QuickButtonItem;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.MoveableList;

import java.util.ConcurrentModificationException;

public class QuickButtonList extends H_ListView {

    public static QuickButtonList that;
    public static MoveableList<QuickButtonItem> quickButtonList;
    private final float btnHeight;
    private float btnYPos;

    public QuickButtonList(CB_RectF rec, String Name) {
        super(rec, Name);
        that = this;
        btnHeight = UiSizes.getInstance().getQuickButtonListHeight() * 0.93f;
        setBackground(Sprites.ButtonBack);

        CB_RectF btnRec = new CB_RectF(0, 0, btnHeight, btnHeight);

        btnYPos = this.getHalfHeight() - btnRec.getHalfHeight();

        this.setBaseAdapter(new CustomAdapter());
        this.setDisposeFlag(false);

        registerSkinChangedEvent();
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        btnYPos = this.getHalfHeight() - btnHeight / 2;

        if (quickButtonList != null && !quickButtonList.isEmpty())
            for (int i = 0; i > quickButtonList.size(); i++) {
                setButtonYPos(i);
            }

    }

    @Override
    public void initialize() {
        super.initialize();
        chkIsDraggable();
    }

    private void chkIsDraggable() {
        if (quickButtonList != null) {
            if (this.getMaxItemCount() < quickButtonList.size()) {
                this.setDraggable();
            } else {
                this.setUnDraggable();
            }
        }
    }

    @Override
    public boolean click(int x, int y, int pointer, int button) {
        // send Event to Buttons
        synchronized (childs) {
            for (int i = 0, n = childs.size(); i < n; i++) {
                GL_View_Base btn = childs.get(i);
                if (btn != null) {
                    btn.onTouchUp(x, y, pointer, button);
                    if (btn.contains(x, y)) {
                        return btn.click(x, y, pointer, button);
                    }
                }
            }
        }
        return super.click(x, y, pointer, button);
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        synchronized (this.childs) {
            for (int i = 0, n = childs.size(); i < n; i++) {
                GL_View_Base btn = childs.get(i);
                if (btn != null)
                    btn.onTouchDown(x, y, pointer, button);
            }
        }
        return super.onTouchDown(x, y, pointer, button);
    }

    @Override
    public boolean onTouchUp(int x, int y, int pointer, int button) {
        synchronized (this.childs) {
            for (int i = 0, n = childs.size(); i < n; i++) {
                GL_View_Base btn = childs.get(i);
                if (btn != null)
                    btn.onTouchUp(x, y, pointer, button);
            }
        }
        return super.onTouchUp(x, y, pointer, button);
    }

    @Override
    public boolean onTouchDragged(int x, int y, int pointer, boolean KineticPan) {

        synchronized (this.childs) {

            try {
                for (int i = 0, n = childs.size(); i < n; i++) {
                    GL_View_Base btn = childs.get(i);
                    if (btn != null)
                        btn.onTouchDragged(x, y, pointer, KineticPan);
                }
            } catch (ConcurrentModificationException e) {
                return false;
            }
        }

        return super.onTouchDragged(x, y, pointer, KineticPan);
    }

    private QuickButtonItem setButtonYPos(int position) {
        QuickButtonItem v = quickButtonList.get(position);
        v.setSize(btnHeight, btnHeight);
        v.setY(btnYPos);// center btn on y direction
        return v;
    }

    private void readQuickButtonItemsList() {
        if (quickButtonList == null) {
            String ConfigActionList = Config.quickButtonList.getValue();
            String[] ConfigList = ConfigActionList.split(",");
            quickButtonList = QuickActions.getListFromConfig(ConfigList, btnHeight);
        }
        chkIsDraggable();
    }

    @Override
    public void notifyDataSetChanged() {
        quickButtonList = null;
        readQuickButtonItemsList();
        super.notifyDataSetChanged();
    }

    @Override
    protected void skinIsChanged() {
        quickButtonList = null;
        readQuickButtonItemsList();
        setBackground(Sprites.ButtonBack);
        reloadItems();
        ListViewItemBackground.ResetBackground();
    }

    public class CustomAdapter implements Adapter {

        public CustomAdapter() {
            readQuickButtonItemsList();

        }

        public ListViewItemBase getView(int position) {

            if (quickButtonList == null)
                return null;

            QuickButtonItem v = setButtonYPos(position);
            return v;
        }

        @Override
        public int getCount() {
            if (quickButtonList == null)
                return 0;
            return quickButtonList.size();
        }

        @Override
        public float getItemSize(int position) {
            return btnHeight;
        }
    }

}
