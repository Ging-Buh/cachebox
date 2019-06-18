package CB_UI_Base.GL_UI.Main;

import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.H_ListView;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_Utils.Log.Log;
import com.badlogic.gdx.math.Vector2;

import static CB_UI_Base.Math.GL_UISizes.MainBtnSize;

/**
 * the CB_TabView shows the aktView<br>
 * which is shown by clicking a button of the mButtonList
 */
public class CB_TabView extends CB_View_Base {
    private final static String sKlasse = "CB_TabView";

    private final CB_RectF mContentRec;
    private CB_ButtonList mButtonList;
    private H_ListView buttonListView;
    private CB_View_Base aktView;

    public CB_TabView(CB_RectF rec, String Name) {
        super(rec, Name);
        mContentRec = rec.copy();
        layout();
    }

    public void setButtonList(CB_ButtonList buttonList) {
        mButtonList = buttonList;
        if (mButtonList == null)
            return;
        buttonListView = new H_ListView(new CB_RectF(0, 0, this.getWidth(), MainBtnSize.getHeight()), "ButtonList von " + this.getName());
        buttonListView.setBaseAdapter(new CustomAdapter());
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
        mContentRec.setHeight(this.getHeight() - MainBtnSize.getHeight());
        mContentRec.setPos(0, MainBtnSize.getHeight());

        if (aktView != null) {
            // set View size and pos
            aktView.setSize(this.getWidth(), this.getHeight() - buttonListView.getHeight());
            aktView.setPos(new Vector2(0, buttonListView.getHeight()));

        }
    }

    @Override
    protected void Initial() {
        // Wenn die Anzahl der Buttons = der Anzahl der M�glichen Buttons ist, diese gleichm��ig verteilen
        if (mButtonList.Buttons.size() <= buttonListView.getMaxItemCount()) {
            float sollDivider = (buttonListView.getWidth() - (MainBtnSize.getHeight() * mButtonList.Buttons.size())) / (mButtonList.Buttons.size() + 1);
            buttonListView.setDividerSize(sollDivider);
        }

        // Das Button Seitenverh�ltniss ist 88x76!
        // H�he der Buttons einstellen und diese Zentrieren!
        float buttonHeight = MainBtnSize.getHeight() * 0.863f;
        for (CB_CB_Button btn : mButtonList.Buttons) {
            btn.setHeight(buttonHeight);
        }

    }

    public void ShowView(final CB_View_Base view) {

        Thread th = new Thread(() -> {
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
            } catch (Exception e) {
                Log.err(sKlasse, "set view size", e);
                return;
            }

            if (aktView == view)
                return;

            aktView = view;
            addChild(aktView);

            aktView.setVisible();
            sendOnShow2aktView();

            GL.that.renderOnce();
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
    public void SkinIsChanged() {
        ShowView(aktView);
    }

    public class CustomAdapter implements Adapter {

        public CustomAdapter() {
        }

        @Override
        public ListViewItemBase getView(int position) {

            if (mButtonList == null || mButtonList.Buttons == null)
                return null;

            CB_CB_Button btn = mButtonList.Buttons.get(position);

            btn.setActView(aktView);

            CB_ButtonListItem v = new CB_ButtonListItem(position, btn, "Item " + position);
            return v;
        }

        @Override
        public int getCount() {
            return mButtonList.Buttons.size();
        }

        @Override
        public float getItemSize(int position) {
            return MainBtnSize.getHeight();
        }
    }

}
