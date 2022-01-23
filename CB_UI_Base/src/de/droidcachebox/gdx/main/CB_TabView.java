package de.droidcachebox.gdx.main;

import static de.droidcachebox.gdx.math.GL_UISizes.mainButtonSize;

import java.util.ArrayList;

import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.H_ListView;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.utils.log.Log;

/**
 * the CB_TabView shows the currentView<br>
 * which is shown by clicking a button of the gestureButtons
 */
public class CB_TabView extends CB_View_Base {
    private final static String sClass = "CB_TabView";
    private final CB_RectF mContentRec;
    private final ArrayList<GestureButton> mainButtons;
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
        mainButtonListView.setBackground(Sprites.buttonBack);
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
            currentView.setPos(0, mainButtonListView.getHeight());

        }
    }

    @Override
    protected void renderInit() {
        // if number of buttons equals number of possible buttons: spread them evenly
        if (mainButtons.size() <= mainButtonListView.getMaxNumberOfVisibleItems()) {
            float dividerSize = (mainButtonListView.getWidth() - (mainButtonSize.getHeight() * mainButtons.size())) / (mainButtons.size() + 1);
            mainButtonListView.setDividerSize(dividerSize);
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
                view.setSize(getWidth(), getHeight() - mainButtonListView.getHeight());
                view.setPos(0, mainButtonListView.getHeight());
                if (currentView == view)
                    return;
                currentView = view;
                addChild(currentView);
                currentView.setVisible();
                sendOnShow2CurrentView();
                GL.that.renderOnce();
            } catch (Exception ex) {
                Log.err(sClass, ex);
            }
        }).start();
    }

    private void sendOnShow2CurrentView() {
        // on changing the view the children are perhaps not yet created, cause they are created in initializing method.
        // resulting they would not be shown. therefore doing RunOnGL twice (resulting in wait for 150ms)
        GL.that.runOnGL(
                () -> GL.that.runOnGL(
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
