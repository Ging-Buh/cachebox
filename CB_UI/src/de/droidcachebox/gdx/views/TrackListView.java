package de.droidcachebox.gdx.views;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.RouteOverlay;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.main.ViewManager;
import de.droidcachebox.translation.Translation;

public class TrackListView extends V_ListView {
    private static CB_RectF itemRec;
    private static TrackListView trackListView;
    private TrackListViewItem aktRouteItem;

    private TrackListView() {
        super(ViewManager.leftTab.getContentRec(), "TrackListView");
        itemRec = new CB_RectF(0, 0, getWidth(), UiSizes.getInstance().getButtonHeight() * 1.1f);
        setBackground(Sprites.ListBack);
        // specific initialize
        setEmptyMsg(Translation.get("EmptyTrackList"));
        setAdapter(new TrackListViewAdapter());
    }

    public static TrackListView getInstance() {
        if (trackListView == null) trackListView = new TrackListView();
        return trackListView;
    }

    @Override
    public void onShow() {
        notifyDataSetChanged();
    }

    public void notifyActTrackChanged() {
        if (aktRouteItem != null) {
            aktRouteItem.notifyTrackChanged(GlobalCore.AktuelleRoute);
            GL.that.renderOnce();
        }
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
            int size = RouteOverlay.getNumberOfTracks();
            if (GlobalCore.AktuelleRoute != null)
                size++;
            return size;
        }

        @Override
        public ListViewItemBase getView(int position) {
            int index = position;
            if (GlobalCore.AktuelleRoute != null) {
                if (position == 0) {
                    aktRouteItem = new TrackListViewItem(itemRec, index, GlobalCore.AktuelleRoute);
                    return aktRouteItem;
                }
                position--;
            }
            return new TrackListViewItem(itemRec, index, RouteOverlay.getTrack(position));
        }

        @Override
        public float getItemSize(int position) {
            if (GlobalCore.AktuelleRoute != null && position == 1) {
                return itemRec.getHeight() + itemRec.getHalfHeight();
            }
            return itemRec.getHeight();
        }

    }

}
