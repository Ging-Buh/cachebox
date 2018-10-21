package CB_UI.GL_UI.Views;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.TrackableListDAO;
import CB_Core.LogTypes;
import CB_Core.Types.TbList;
import CB_Core.Types.Trackable;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.TB_Details;
import CB_UI.GL_UI.Main.TabMainView;
import CB_UI.GlobalCore;
import CB_UI.TemplateFormatter;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Button;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.Dialogs.StringInputBox;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.GL_UI.Menu.MenuID;
import CB_UI_Base.GL_UI.Menu.MenuItem;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Interfaces.ICancelRunnable;
import CB_Utils.Log.Log;

import java.util.Date;

import static CB_Core.Api.GroundspeakAPI.*;
import static CB_UI_Base.GL_UI.Menu.MenuID.*;

public class TrackableListView extends ActivityBase {
    private static final String log = "TrackableListView";
    private static final int MI_SEARCH = 37;
    private static final int MI_REFRESH_TB_LIST = 165;
    private static final int MI_TB_DROPPED = 167;
    private static final int MI_TB_VISIT = 169;
    private static final int MI_TB_NOTE = 171;
    private static final int MI_QUIT = 24;
    public static TrackableListView that;
    private V_ListView listView;
    private CustomAdapter lvAdapter;
    private TbList mTB_List;
    private Button btnAction;
    private CancelWaitDialog wd;
    private final OnClickListener menuItemClickListener = new OnClickListener() {
        @Override
        public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {

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
                case MI_QUIT:
                    finish();
                    break;
            }
            return true;
        }
    };

    public TrackableListView(CB_RectF rec, String Name) {
        super(rec, Name);
        that = this;
        setBackground(Sprites.ListBack);
        lvAdapter = new CustomAdapter();
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
        btnAction = new Button(Translation.Get("TB_Actions"));
        btnAction.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                showMenu();
                return true;
            }
        });

        listView = new V_ListView(new CB_RectF(0, 0, getWidth(), 0), "listView");
        listView.setEmptyMsg(Translation.Get("TB_List_Empty"));
        listView.setBaseAdapter(lvAdapter);

        Layout();
    }

    public void reloadTB_List() {
        mTB_List = TrackableListDAO.ReadTbList("");
        lvAdapter = new CustomAdapter();
        if (listView != null)
            listView.setBaseAdapter(lvAdapter);
    }

    private boolean fetchTB(final String TBCode) {
        if (TBCode.length() > 0) {
            wd = CancelWaitDialog.ShowWait(Translation.Get("search"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {
                @Override
                public void isCanceled() {
                }
            }, new ICancelRunnable() {
                @Override
                public void run() {
                    Trackable tb = fetchTrackable(TBCode);
                    wd.close();
                    if (tb == null) {
                        if (APIError == 404) {
                            GL.that.Toast(Translation.Get("NoTbFound"));
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
                    // TODO handle cancel
                    return false;
                }
            });
        }
        return true;
    }

    private void Layout() {
        this.removeChilds();
        initRow(BOTTOMUP);

        addLast(btnAction);
        listView.setHeight(getAvailableHeight());
        addLast(listView);

        listView.notifyDataSetChanged();
    }

    // Inventar neu laden
    public void RefreshTbList() {
        wd = CancelWaitDialog.ShowWait(Translation.Get("RefreshInventory"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {

            @Override
            public void isCanceled() {

            }
        }, new ICancelRunnable() {

            @Override
            public void run() {
                TbList searchList = downloadUsersTrackables();
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
                // TODO handle cancel
                return false;
            }
        });
    }

    public void LogTBs(String title, final int LogTypeId, final String LogText) {
        wd = CancelWaitDialog.ShowWait(title, DownloadAnimation.GetINSTANCE(), new IcancelListener() {

            @Override
            public void isCanceled() {

            }
        }, new ICancelRunnable() {

            @Override
            public void run() {
                // todo Dialog for local or direct log
                    /*
                        private void LogNow () {
                        if (rbDirectLog.isChecked())
                            logOnline();
                        else
                            createFieldNote();
                        }
                    */
                for (Trackable tb : mTB_List) {
                    if (!GroundspeakAPI.uploadTrackableLog(tb, GlobalCore.getSelectedCache().getGcCode(), LogTypeId, new Date(), LogText)) {
                        GL.that.Toast(LastAPIError);
                    }
                }
                wd.close();
            }

            @Override
            public boolean doCancel() {
                // TODO handle cancel
                return false;
            }
        });
    }

    private void searchTB() {
        StringInputBox.Show(WrapType.SINGLELINE, Translation.Get("InputTB_Code"), Translation.Get("SearchTB"), "", new GL_MsgBox.OnMsgBoxClickListener() {
            @Override
            public boolean onClick(int which, Object data) {
                switch (which) {
                    case 1: // ok
                        fetchTB(StringInputBox.editText.getText());
                        break;
                    case 3: // cancel
                        break;
                }
                return true;
            }
        });
    }

    private void showMenu() {

        final Menu cm = new Menu("TBLogContextMenu");
        cm.addOnClickListener(menuItemClickListener);

        cm.addItem(MI_SEARCH, "SearchTB", Sprites.getSprite(IconName.lupe.name()));
        cm.addItem(MI_REFRESH_TB_LIST, "RefreshInventory");
        cm.addItem(MI_TB_NOTE, "all_note", Sprites.getSprite(IconName.TBNOTE.name()));
        cm.addItem(MI_TB_VISIT, "all_visit", Sprites.getSprite(IconName.TBVISIT.name()));
        cm.addItem(MI_TB_DROPPED, "all_dropped", Sprites.getSprite(IconName.TBDROP.name()));
        cm.addItem(MI_QUIT, "cancel", Sprites.getSprite(IconName.closeIcon.name()));
        cm.Show();
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
            v.setOnClickListener(new OnClickListener() {
                @Override
                public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                    if (TB_Details.that == null)
                        new TB_Details();
                    TB_Details.that.Show(mTB_List.get(position));
                    return true;
                }
            });

            return v;
        }

        @Override
        public float getItemSize(int position) {
            return UiSizes.that.getCacheListItemRec().getHeight();
        }

    }

}
