package de.droidcachebox.gdx.views;

import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.TemplateFormatter;
import de.droidcachebox.WrapType;
import de.droidcachebox.database.GeoCacheLogType;
import de.droidcachebox.database.TBList;
import de.droidcachebox.database.Trackable;
import de.droidcachebox.database.TrackableListDAO;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.TB_Details;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.ConnectionError;
import de.droidcachebox.gdx.controls.dialogs.StringInputBox;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.MenuItem;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.main.ViewManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.log.Log;

import java.util.Date;

import static de.droidcachebox.core.GroundspeakAPI.*;

public class TrackableListView extends CB_View_Base {
    private static final String log = "TrackableListView";
    public static TrackableListView that;
    private V_ListView listView;
    private CustomAdapter lvAdapter;
    private TBList mTB_List;
    private CancelWaitDialog wd;

    private TrackableListView() {
        super(ViewManager.leftTab.getContentRec(), "TrackableListView");
        that = this;
        setBackground(Sprites.ListBack);
        lvAdapter = new CustomAdapter();
    }

    public static TrackableListView getInstance() {
        if (that == null) that = new TrackableListView();
        return that;
    }

    public void dispose() {
        super.dispose();
        that = null;
    }

    public void onShow() {
        reloadTB_List();
    }

    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        Layout();
    }

    public void onHide() {
    }

    protected void initialize() {

        listView = new V_ListView(new CB_RectF(0, 0, getWidth(), 0), "listView");
        listView.setEmptyMsg(Translation.get("TB_List_Empty"));
        listView.setAdapter(lvAdapter);

        Layout();
    }

    private void reloadTB_List() {
        mTB_List = TrackableListDAO.ReadTbList("");
        lvAdapter = new CustomAdapter();
        if (listView != null)
            listView.setAdapter(lvAdapter);
    }

    private boolean fetchTB(final String TBCode) {
        if (TBCode.length() > 0) {
            wd = CancelWaitDialog.ShowWait(Translation.get("Search"), DownloadAnimation.GetINSTANCE(), () -> {
            }, new ICancelRunnable() {
                @Override
                public void run() {
                    Trackable tb = fetchTrackable(TBCode);
                    wd.close();
                    if (tb == null) {
                        if (APIError == 404) {
                            GL.that.Toast(Translation.get("NoTbFound"));
                        } else {
                            // GL.that.Toast(ConnectionError.INSTANCE);
                            // GL.that.Toast(ApiUnavailable.INSTANCE);
                            GL.that.Toast(LastAPIError);
                        }
                        return;
                    }
                    new TB_Details().Show(tb);
                }

                @Override
                public boolean doCancel() {
                    return false;
                }
            });
        }
        return true;
    }

    private void Layout() {
        this.removeChilds();
        initRow(BOTTOMUP);

        listView.setHeight(getAvailableHeight());
        addLast(listView);

        listView.notifyDataSetChanged();
    }

    // Inventar neu laden
    public void RefreshTbList() {
        wd = CancelWaitDialog.ShowWait(Translation.get("RefreshInventory"), DownloadAnimation.GetINSTANCE(), () -> {

        }, new ICancelRunnable() {

            @Override
            public void run() {
                TBList searchList = downloadUsersTrackables();
                Log.info(log, "RefreshTbList gotTBs");
                if (searchList == null) {
                    GL.that.Toast(ConnectionError.INSTANCE);
                    // GL.that.Toast(ApiUnavailable.INSTANCE);
                } else {
                    Log.info(log, "RefreshTbList clearDB");
                    TrackableListDAO.clearDB();
                    Log.info(log, "RefreshTbList writeToDB");
                    searchList.writeToDB();
                    Log.info(log, "RefreshTbList reloadTB_List");
                    reloadTB_List();
                    Log.info(log, "RefreshTbList reloadTB_List done");
                }

                Log.info(log, "CancelWaitDialog.close");
                wd.close();
            }

            @Override
            public boolean doCancel() {
                return false;
            }
        });
    }

    private void LogTBs(String title, final int LogTypeId, final String LogText) {
        wd = CancelWaitDialog.ShowWait(title, DownloadAnimation.GetINSTANCE(), () -> {

        }, new ICancelRunnable() {

            @Override
            public void run() {
                for (Trackable tb : mTB_List) {
                    if (uploadTrackableLog(tb, GlobalCore.getSelectedCache().getGcCode(), LogTypeId, new Date(), LogText) != OK) {
                        GL.that.Toast(LastAPIError);
                    }
                }
                wd.close();
            }

            @Override
            public boolean doCancel() {
                return false;
            }
        });
    }

    private void searchTB() {
        StringInputBox.Show(WrapType.SINGLELINE, Translation.get("InputTB_Code"), Translation.get("SearchTB"), "", (which, data) -> {
            switch (which) {
                case 1: // ok
                    return fetchTB(StringInputBox.editText.getText());
                case 3: // cancel
                    break;
            }
            return true;
        });
    }

    public Menu getContextMenu() {
        final Menu cm = new Menu("TrackableListViewContextMenuTitle");
        cm.addMenuItem("SearchTB", Sprites.getSprite(IconName.lupe.name()), this::searchTB);
        cm.addMenuItem("RefreshInventory", null, this::RefreshTbList);
        cm.addMenuItem("all_note", "", Sprites.getSprite(IconName.TBNOTE.name()), (v, x, y, pointer, button) -> {
            cm.close();
            LogTBs(((MenuItem) v).getTitle(), GeoCacheLogType.CB_LogType2GC(GeoCacheLogType.note), TemplateFormatter.ReplaceTemplate(Config.AddNoteTemplate.getValue(), new Date()));
            return true;
        });
        cm.addMenuItem("all_visit", "", Sprites.getSprite(IconName.TBVISIT.name()), (v, x, y, pointer, button) -> {
            cm.close();
            LogTBs(((MenuItem) v).getTitle(), GeoCacheLogType.CB_LogType2GC(GeoCacheLogType.visited), TemplateFormatter.ReplaceTemplate(Config.VisitedTemplate.getValue(), new Date()));
            return true;
        });
        cm.addMenuItem("all_dropped", "", Sprites.getSprite(IconName.TBDROP.name()), (v, x, y, pointer, button) -> {
            cm.close();
            LogTBs(((MenuItem) v).getTitle(), GeoCacheLogType.CB_LogType2GC(GeoCacheLogType.dropped_off), TemplateFormatter.ReplaceTemplate(Config.DroppedTemplate.getValue(), new Date()));
            RefreshTbList();
            return true;
        });
        return cm;
    }

    public class CustomAdapter implements Adapter {

        @Override
        public int getCount() {
            if (mTB_List == null)
                return 0;
            return mTB_List.size();
        }

        @Override
        public ListViewItemBase getView(final int position) {
            TrackableListViewItem v = new TrackableListViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), position, mTB_List.get(position));
            v.setClickHandler((v1, x, y, pointer, button) -> {
                if (TB_Details.that == null)
                    new TB_Details();
                TB_Details.that.Show(mTB_List.get(position));
                return true;
            });

            return v;
        }

        @Override
        public float getItemSize(int position) {
            return UiSizes.getInstance().getCacheListItemRec().getHeight();
        }

    }

}
