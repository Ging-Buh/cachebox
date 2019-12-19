package de.droidcachebox.gdx.main;

import com.badlogic.gdx.math.Vector2;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.H_ListView;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.log.Log;

import static de.droidcachebox.gdx.math.GL_UISizes.mainBtnSize;

/**
 * the CB_TabView shows the aktView<br>
 * which is shown by clicking a button of the mButtonList
 */
public class CB_TabView extends CB_View_Base {
    private final static String sKlasse = "CB_TabView";

    private final CB_RectF mContentRec;
    private CB_ButtonBar mButtonList;
    private H_ListView buttonListView;
    private CB_View_Base aktView;

    public CB_TabView(CB_RectF rec, String Name) {
        super(rec, Name);
        mContentRec = new CB_RectF(rec);
        layout();
    }

    public void setButtonList(CB_ButtonBar buttonList) {
        mButtonList = buttonList;
        if (mButtonList == null)
            return;
        buttonListView = new H_ListView(new CB_RectF(0, 0, this.getWidth(), mainBtnSize.getHeight()), "ButtonList von " + this.getName());
        buttonListView.setAdapter(new CustomAdapter());
        buttonListView.setUnDraggable();
        buttonListView.setBackground(Sprites.ButtonBack);
        buttonListView.setDisposeFlag(false);
        this.addChild(buttonListView);
    }

    @Override
    public void onResized(CB_RectF rec) {
        layout();
    }

    private void layout() {
        mContentRec.setHeight(this.getHeight() - mainBtnSize.getHeight());
        mContentRec.setPos(0, mainBtnSize.getHeight());

        if (aktView != null) {
            // set View size and pos
            aktView.setSize(this.getWidth(), this.getHeight() - buttonListView.getHeight());
            aktView.setPos(new Vector2(0, buttonListView.getHeight()));

        }
    }

    @Override
    protected void initialize() {
        // Wenn die Anzahl der Buttons = der Anzahl der M�glichen Buttons ist, diese gleichm��ig verteilen
        if (mButtonList.Buttons.size() <= buttonListView.getMaxItemCount()) {
            float sollDivider = (buttonListView.getWidth() - (mainBtnSize.getHeight() * mButtonList.Buttons.size())) / (mButtonList.Buttons.size() + 1);
            buttonListView.setDividerSize(sollDivider);
        }

        // Das Button Seitenverh�ltniss ist 88x76!
        // H�he der Buttons einstellen und diese Zentrieren!
        float buttonHeight = mainBtnSize.getHeight() * 0.863f;
        for (GestureButton btn : mButtonList.Buttons) {
            btn.setHeight(buttonHeight);
        }

    }

    public void showView(final CB_View_Base view) {

        Thread th = new Thread(() -> {
            try {
                GL.that.clearRenderViews();
                GL.that.closeAllDialogs();

                if (aktView != null && aktView != view) {
                    removeChild(aktView);
                    // aktView.onStop();
                    aktView.onHide();
                    aktView.setInvisible();
                }

                try {
                    // set View size and pos
                    view.setSize(CB_TabView.this.getWidth(), CB_TabView.this.getHeight() - buttonListView.getHeight());
                    view.setPos(new Vector2(0, buttonListView.getHeight()));
                } catch (Exception ex) {
                    Log.err(sKlasse, "set view size", ex);
                    return;
                }

                if (aktView == view)
                    return;

                aktView = view;
                addChild(aktView);

                aktView.setVisible();
                sendOnShow2aktView();

                GL.that.renderOnce();
            }
            catch (Exception ex) {
                Log.err(sKlasse, "", ex);
            }
        });

        th.start();

    }

    /**
     * Beim Wechsel der View, kann es sein, dass noch nicht alle Childs der View geladen sind, da die meisten Childs erst in der initial()
     * erstellt werden. Damit erhalten diese Childs dann kein onShow(). Als Abhilfe werden hier erst 150ms gewartet, bevor ein onShow()
     * ausgef�hrt wird.
     */
    private void sendOnShow2aktView() {
        GL.that.RunOnGL(() -> GL.that.RunOnGL(() -> {
            if (aktView != null && aktView.isVisible())
                aktView.onShow();
            buttonListView.notifyDataSetChanged();
        }));
    }

    public CB_RectF getContentRec() {
        return mContentRec;
    }

    @Override
    public void skinIsChanged() {
        showView(aktView);
    }

    public class CustomAdapter implements Adapter {

        public CustomAdapter() {
        }

        @Override
        public ListViewItemBase getView(int position) {
            if (mButtonList == null || mButtonList.Buttons == null)
                return null;
            GestureButton btn = mButtonList.Buttons.get(position);
            btn.setActView(aktView);
            return new CB_ButtonListItem(position, btn, "Item " + position);
        }

        @Override
        public int getCount() {
            return mButtonList.Buttons.size();
        }

        @Override
        public float getItemSize(int position) {
            return mainBtnSize.getHeight();
        }
    }

}
