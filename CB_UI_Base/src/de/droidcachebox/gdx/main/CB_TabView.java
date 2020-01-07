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

import java.util.ArrayList;

import static de.droidcachebox.gdx.math.GL_UISizes.mainButtonSize;

/**
 * the CB_TabView shows the currentView<br>
 * which is shown by clicking a button of the gestureButtons
 */
public class CB_TabView extends CB_View_Base {
    private final static String sKlasse = "CB_TabView";
    private final CB_RectF mContentRec;
    private ArrayList<GestureButton> mainButtons;
    private H_ListView mainButtonListView;
    private CB_View_Base currentView;

    public CB_TabView(CB_RectF rec, String Name) {
        super(rec, Name);
        mainButtons = new ArrayList<>();
        mContentRec = new CB_RectF(rec);
        layout();
    }

    public void setButtonList() {
        mainButtonListView = new H_ListView(new CB_RectF(0, 0, getWidth(), mainButtonSize.getHeight()), "ButtonList von " + getName());
        mainButtonListView.setAdapter(new MainButtonListViewAdapter());
        mainButtonListView.setUnDraggable();
        mainButtonListView.setBackground(Sprites.ButtonBack);
        mainButtonListView.setDisposeFlag(false);
        addChild(mainButtonListView);
    }

    public void addMainButton(GestureButton button) {
        mainButtons.add(button);
    }

    @Override
    public void onResized(CB_RectF rec) {
        layout();
    }

    private void layout() {
        mContentRec.setHeight(getHeight() - mainButtonSize.getHeight());
        mContentRec.setPos(0, mainButtonSize.getHeight());

        if (currentView != null) {
            // set View size and pos
            currentView.setSize(getWidth(), getHeight() - mainButtonListView.getHeight());
            currentView.setPos(new Vector2(0, mainButtonListView.getHeight()));

        }
    }

    @Override
    protected void initialize() {
        // Wenn die Anzahl der Buttons = der Anzahl der M�glichen Buttons ist, diese gleichm��ig verteilen
        if (mainButtons.size() <= mainButtonListView.getMaxItemCount()) {
            float sollDivider = (mainButtonListView.getWidth() - (mainButtonSize.getHeight() * mainButtons.size())) / (mainButtons.size() + 1);
            mainButtonListView.setDividerSize(sollDivider);
        }

        // the main button is 88x76!
        float buttonHeight = mainButtonSize.getHeight() * 0.863f;
        for (GestureButton mainButton : mainButtons) {
            mainButton.setHeight(buttonHeight);
        }

    }

    public void showView(final CB_View_Base view) {
        new Thread(() -> {
            try {
                GL.that.clearRenderViews();
                GL.that.closeAllDialogs();
                if (currentView != null && currentView != view) {
                    removeChild(currentView);
                    currentView.onHide();
                    currentView.setInvisible();
                }
                try {
                    view.setSize(getWidth(), getHeight() - mainButtonListView.getHeight());
                    view.setPos(new Vector2(0, mainButtonListView.getHeight()));
                } catch (Exception ex) {
                    Log.err(sKlasse, "set view size", ex);
                    return;
                }
                if (currentView == view)
                    return;
                currentView = view;
                addChild(currentView);
                currentView.setVisible();
                sendOnShow2CurrentView();
                GL.that.renderOnce();
            } catch (Exception ex) {
                Log.err(sKlasse, "", ex);
            }
        }).start();
    }

    private void sendOnShow2CurrentView() {
        // on changing the view the children are perhaps not yet created, cause they are created in initializing method.
        // resulting they would not be shown. therefore doing RunOnGL twice (resulting in waittime 150ms)
        GL.that.RunOnGL(
                () -> GL.that.RunOnGL(
                        () -> {
                            if (currentView != null && currentView.isVisible())
                                currentView.onShow();
                            mainButtonListView.notifyDataSetChanged();
                        }
                )
        );
    }

    public CB_RectF getContentRec() {
        return mContentRec;
    }

    @Override
    public void skinIsChanged() {
        showView(currentView);
    }

    private class MainButtonListViewAdapter implements Adapter {
        public MainButtonListViewAdapter() {
        }

        @Override
        public int getCount() {
            return mainButtons.size();
        }

        @Override
        public ListViewItemBase getView(int position) {
            if (mainButtons == null)
                return null;
            GestureButton gestureButton = mainButtons.get(position);
            gestureButton.setCurrentView(currentView);
            return new CB_ButtonListItem(position, gestureButton, "Item " + position);
        }

        @Override
        public float getItemSize(int position) {
            return mainButtonSize.getHeight();
        }
    }

}
