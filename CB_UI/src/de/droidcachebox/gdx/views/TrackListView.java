package de.droidcachebox.gdx.views;

import com.badlogic.gdx.utils.Array;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.RouteOverlay;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;

public class TrackListView extends V_ListView {
    private static CB_RectF itemRec;
    private static TrackListView trackListView;
    private TrackListViewItem aktRouteItem;
    private Array<TrackListViewItem> trackListViewItems;

    private TrackListView() {
        super(ViewManager.leftTab.getContentRec(), "TrackListView");
        itemRec = new CB_RectF(0, 0, getWidth(), UiSizes.getInstance().getButtonHeight() * 1.1f);
        setBackground(Sprites.ListBack);
        // specific initialize
        trackListViewItems = new Array<>();
        setEmptyMsg(Translation.get("EmptyTrackList"));
        setAdapter(new TrackListViewAdapter());
    }

    public static TrackListView getInstance() {
        if (trackListView == null) trackListView = new TrackListView();
        return trackListView;
    }

    @Override
    public void onShow() {
        Log.info("TrackListView", "onShow");
        // notifyDataSetChanged();
    }

    public void notifyActTrackChanged() {
        if (aktRouteItem != null) {
            aktRouteItem.notifyTrackChanged(GlobalCore.aktuelleRoute);
            GL.that.renderOnce();
        }
    }

    public class TrackListViewAdapter implements Adapter {
        // if tracking is activated, aktuelleRoute gets index 0 and the others get one more
        public TrackListViewAdapter() {
        }

        @Override
        public int getCount() {
            int size = RouteOverlay.getInstance().getNumberOfTracks();
            if (GlobalCore.aktuelleRoute != null)
                size++;
            Log.info("TrackListView", "items: " + size);
            return size;
        }

        @Override
        public ListViewItemBase getView(int position) {
            Log.info("TrackListView", "create item for " + position);
            int index = position;
            if (GlobalCore.aktuelleRoute != null) {
                if (position == 0) {
                    if (aktRouteItem == null) {
                        aktRouteItem = new TrackListViewItem(itemRec, index, GlobalCore.aktuelleRoute);
                        trackListViewItems.add(aktRouteItem);
                    }
                    return aktRouteItem;
                }
                index++; // position + 1, if tracking is activated
            }
            if (trackListViewItems.size > index) {
                return trackListViewItems.get(index);
            }
            TrackListViewItem t = new TrackListViewItem(itemRec, index, RouteOverlay.getInstance().getTrack(position));
            trackListViewItems.add(t);
            return t;
        }

        @Override
        public float getItemSize(int position) {
            if (GlobalCore.aktuelleRoute != null && position == 1) {
                // so there is a distance between aktuelleRoute and the others
                return itemRec.getHeight() + itemRec.getHalfHeight();
            }
            return itemRec.getHeight();
        }

    }

}
