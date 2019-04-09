package CB_UI.GL_UI.Views;

import CB_Core.DAO.TrackableListDAO;
import CB_Core.LogTypes;
import CB_Core.Types.TBList;
import CB_Core.Types.Trackable;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.TB_Details;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GlobalCore;
import CB_UI.TemplateFormatter;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.StringInputBox;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Log.Log;

import java.util.Date;

import static CB_Core.Api.GroundspeakAPI.*;

public class TrackableListView extends CB_View_Base {
    private static final String log = "TrackableListView";
    private static final int MI_SEARCH = 37;
    private static final int MI_REFRESH_TB_LIST = 165;
    private static final int MI_TB_DROPPED = 167;
    private static final int MI_TB_VISIT = 169;
    private static final int MI_TB_NOTE = 171;
    public static TrackableListView that;
    private V_ListView listView;
    private CustomAdapter lvAdapter;
    private TBList mTB_List;
    private CancelWaitDialog wd;

    private TrackableListView() {
        super(TabMainView.leftTab.getContentRec(), "TrackableListView");
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

    protected void Initial() {

        listView = new V_ListView(new CB_RectF(0, 0, getWidth(), 0), "listView");
        listView.setEmptyMsg(Translation.get("TB_List_Empty"));
        listView.setBaseAdapter(lvAdapter);

        Layout();
    }

    private void reloadTB_List() {
        mTB_List = TrackableListDAO.ReadTbList("");
        lvAdapter = new CustomAdapter();
        if (listView != null)
            listView.setBaseAdapter(lvAdapter);
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
        final Menu cm = new Menu("TBLogContextMenu");
        cm.addOnItemClickListener((v, x, y, pointer, button) -> {
            switch (((MenuItem) v).getMenuItemId()) {
                case MI_SEARCH:
                    searchTB();
                    break;
                case MI_REFRESH_TB_LIST:
                    RefreshTbList();
                    break;
                case MI_TB_VISIT:
                    LogTBs(((MenuItem) v).getTitle(), LogTypes.CB_LogType2GC(LogTypes.visited), TemplateFormatter.ReplaceTemplate(Config.VisitedTemplate.getValue(), new Date()));
                    break;
                case MI_TB_DROPPED:
                    LogTBs(((MenuItem) v).getTitle(), LogTypes.CB_LogType2GC(LogTypes.dropped_off), TemplateFormatter.ReplaceTemplate(Config.DroppedTemplate.getValue(), new Date()));
                    RefreshTbList();
                    break;
                case MI_TB_NOTE:
                    LogTBs(((MenuItem) v).getTitle(), LogTypes.CB_LogType2GC(LogTypes.note), TemplateFormatter.ReplaceTemplate(Config.AddNoteTemplate.getValue(), new Date()));
                    break;
                default:
                    return false;
            }
            return true;
        });
        cm.addItem(MI_SEARCH, "SearchTB", Sprites.getSprite(IconName.lupe.name()));
        cm.addItem(MI_REFRESH_TB_LIST, "RefreshInventory");
        cm.addItem(MI_TB_NOTE, "all_note", Sprites.getSprite(IconName.TBNOTE.name()));
        cm.addItem(MI_TB_VISIT, "all_visit", Sprites.getSprite(IconName.TBVISIT.name()));
        cm.addItem(MI_TB_DROPPED, "all_dropped", Sprites.getSprite(IconName.TBDROP.name()));
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
            TrackableListViewItem v = new TrackableListViewItem(UiSizes.that.getCacheListItemRec().asFloat(), position, mTB_List.get(position));
            v.setOnClickListener((v1, x, y, pointer, button) -> {
                if (TB_Details.that == null)
                    new TB_Details();
                TB_Details.that.Show(mTB_List.get(position));
                return true;
            });

            return v;
        }

        @Override
        public float getItemSize(int position) {
            return UiSizes.that.getCacheListItemRec().getHeight();
        }

    }

}
