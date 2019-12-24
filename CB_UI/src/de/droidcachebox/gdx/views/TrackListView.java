package de.droidcachebox.gdx.views;

import com.badlogic.gdx.math.Vector2;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.RouteOverlay;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.GL_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.main.ViewManager;
import de.droidcachebox.translation.Translation;

import java.util.Iterator;

public class TrackListView extends V_ListView {
    private static CB_RectF ItemRec;
    private static TrackListView that;
    private int selectedTrackItem;
    private TrackListViewItem aktRouteItem;

    private TrackListView() {
        super(ViewManager.leftTab.getContentRec(), "TrackListView");

        ItemRec = new CB_RectF(0, 0, this.getWidth(), UiSizes.getInstance().getButtonHeight() * 1.1f);

        this.setEmptyMsg(Translation.get("EmptyTrackList"));

        setBackground(Sprites.ListBack);

        setAdapter(new TrackListViewAdapter());

    }

    public static TrackListView getInstance() {
        if (that == null) that = new TrackListView();
        return that;
    }

    @Override
    public void onShow() {
        this.notifyDataSetChanged();
    }

    @Override
    public void onHide() {
        // this.dispose();
        // that = null;
    }

    @Override
    public boolean onTouchDown(int x, int y, int pointer, int button) {
        super.onTouchDown(x, y, pointer, button);

        // for (Iterator<GL_View_Base> iterator = childs.iterator(); iterator.hasNext();)
        for (Iterator<GL_View_Base> iterator = childs.reverseIterator(); iterator.hasNext(); ) {
            // Child View suchen, innerhalb derer Bereich der touchDown statt gefunden hat.
            GL_View_Base view = iterator.next();

            if (view instanceof TrackListViewItem) {
                if (view.contains(x, y)) {
                    ((TrackListViewItem) view).lastItemTouchPos = new Vector2(x - view.getX(), y - view.getY());
                }
            }
        }
        return true;
    }

    public void notifyActTrackChanged() {
        if (aktRouteItem != null)
            aktRouteItem.notifyTrackChanged(GlobalCore.AktuelleRoute);
        GL.that.renderOnce();
    }

    @Override
    public TrackListViewItem getSelectedItem() {
        return (TrackListViewItem) super.getSelectedItem();

    }

    public class TrackListViewAdapter implements Adapter {

        public TrackListViewAdapter() {
        }

        @Override
        public int getCount() {
            int size = RouteOverlay.getRouteCount();
            if (GlobalCore.AktuelleRoute != null)
                size++;
            return size;
        }

        @Override
        public ListViewItemBase getView(int position) {
            int index = position;
            if (GlobalCore.AktuelleRoute != null) {
                if (position == 0) {
                    aktRouteItem = new TrackListViewItem(ItemRec, index, GlobalCore.AktuelleRoute, route -> {
                        // Notify Map to Reload RouteOverlay
                        RouteOverlay.routesChanged();
                    });
                    aktRouteItem.setClickHandler((v, x, y, pointer, button) -> {
                        selectedTrackItem = ((ListViewItemBase) v).getIndex();
                        setSelection(selectedTrackItem);
                        return true;
                    });
                    aktRouteItem.setOnLongClickListener(TrackListView.this.getOnLongClickListener());

                    return aktRouteItem;
                }
                position--;
            }

            TrackListViewItem v = new TrackListViewItem(ItemRec, index, RouteOverlay.getRoute(position), route -> {
                // Notify Map to Reload RouteOverlay
                RouteOverlay.routesChanged();
            });

            v.setClickHandler((v1, x, y, pointer, button) -> {
                selectedTrackItem = ((ListViewItemBase) v1).getIndex();
                setSelection(selectedTrackItem);
                return true;
            });
            v.setOnLongClickListener(TrackListView.this.getOnLongClickListener());
            return v;
        }

        @Override
        public float getItemSize(int position) {
            if (GlobalCore.AktuelleRoute != null && position == 1) {
                return ItemRec.getHeight() + ItemRec.getHalfHeight();
            }
            return ItemRec.getHeight();
        }

    }

}
