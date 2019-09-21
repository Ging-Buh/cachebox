package CB_UI.GL_UI.Views;

import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Main.ViewManager;
import CB_UI.GlobalCore;
import CB_UI.RouteOverlay;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import com.badlogic.gdx.math.Vector2;

import java.util.Iterator;

public class TrackListView extends V_ListView {
    private static CB_RectF ItemRec;
    private static TrackListView that;
    private int selectedTrackItem;
    private TrackListViewItem aktRouteItem;

    private TrackListView() {
        super(ViewManager.leftTab.getContentRec(), "TrackListView");

        ItemRec = new CB_RectF(0, 0, this.getWidth(), UI_Size_Base.ui_size_base.getButtonHeight() * 1.1f);

        this.setEmptyMsg(Translation.get("EmptyTrackList"));

        setBackground(Sprites.ListBack);

        this.setBaseAdapter(null);
        this.setBaseAdapter(new CustomAdapter());

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

    public class CustomAdapter implements Adapter {

        public CustomAdapter() {
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
                        RouteOverlay.RoutesChanged();
                    });
                    aktRouteItem.addClickHandler((v, x, y, pointer, button) -> {
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
                RouteOverlay.RoutesChanged();
            });

            v.addClickHandler((v1, x, y, pointer, button) -> {
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
