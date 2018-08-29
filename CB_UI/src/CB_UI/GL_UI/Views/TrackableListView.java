package CB_UI.GL_UI.Views;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.DAO.TrackableListDAO;
import CB_Core.Types.TbList;
import CB_Core.Types.Trackable;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Activitys.TB_Details;
import CB_UI_Base.GL_UI.CB_View_Base;
import CB_UI_Base.GL_UI.Controls.Animation.DownloadAnimation;
import CB_UI_Base.GL_UI.Controls.Box;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.Controls.EditTextField;
import CB_UI_Base.GL_UI.Controls.ImageButton;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.Sprites;
import CB_UI_Base.GL_UI.Sprites.IconName;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Log.Log;

import static CB_Core.Api.GroundspeakAPI.downloadUsersTrackables;

public class TrackableListView extends CB_View_Base {
    private static final String log = "TrackableListView";
    public static TrackableListView that;
    private V_ListView listView;
    private CustomAdapter lvAdapter;
    private TbList mTB_List;
    private Box searchBox;
    private EditTextField txtSearch;
    private ImageButton btnSearch;
    private CancelWaitDialog wd;

    public TrackableListView(CB_RectF rec, String Name) {
        super(rec, Name);
        that = this;
        setBackground(Sprites.ListBack);
        lvAdapter = new CustomAdapter();
    }

    @Override
    public void onShow() {
        reloadTB_List();
    }

    public void reloadTB_List() {
        mTB_List = TrackableListDAO.ReadTbList("");
        lvAdapter = new CustomAdapter();
        if (listView != null)
            listView.setBaseAdapter(lvAdapter);
    }

    @Override
    public void onHide() {
    }

    @Override
    protected void Initial() {
        this.removeChilds();
        // ##################################################################################################
        // Search Box
        // ##################################################################################################
        searchBox = new Box(this.getWidth(), 10, "TB_Search Box");
        btnSearch = new ImageButton("Search");
        searchBox.setBackground(Sprites.activityBackground);
        searchBox.setHeight(btnSearch.getHeight() + searchBox.getTopHeight() + searchBox.getBottomHeight());
        this.addChild(searchBox);

        searchBox.initRow();

        txtSearch = new EditTextField(this.name + " txtSearch");
        txtSearch.setMessageText(Translation.Get("SearchTB_Code"));
        searchBox.addNext(txtSearch);

        btnSearch.setImage(Sprites.getSprite(IconName.lupe.name()));
        btnSearch.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                final String TBCode = txtSearch.getText().trim();
                if (TBCode.length() > 0) {
                    wd = CancelWaitDialog.ShowWait(Translation.Get("search"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {
                        @Override
                        public void isCanceled() {
                        }
                    }, new cancelRunnable() {
                        @Override
                        public void run() {

                            Trackable tb;

                            /* removed in API 1 */
                            tb = GroundspeakAPI.downloadTrackableByTrackingNumber(TBCode);
                            if (tb == null) {
                                tb = GroundspeakAPI.downloadTrackableByTBCode(TBCode);
                            }
                            if (tb == null) {
                                GL.that.Toast(ConnectionError.INSTANCE);
                                // GL.that.Toast(ApiUnavailable.INSTANCE);
                                // GL.that.Toast(Translation.Get("NoTbFound"));
                                wd.close();
                                return;
                            }

                            wd.close();
                            new TB_Details().Show(tb);

                        }

                        @Override
                        public boolean cancel() {
                            // TODO handle cancel
                            return false;
                        }
                    });

                }

                return true;
            }
        });
        searchBox.addLast(btnSearch, FIXED);

        listView = new V_ListView(new CB_RectF(0, 0, getWidth(), getHeight() - searchBox.getHeight()), "TB_LIstView");
        listView.setEmptyMsg(Translation.Get("TB_List_Empty"));
        listView.setBaseAdapter(lvAdapter);
        this.addChild(listView);

        Layout();
    }

    private void Layout() {
        searchBox.setY(this.getHeight() - searchBox.getHeight());
        listView.setHeight(this.getHeight() - searchBox.getHeight() - UI_Size_Base.that.getMargin());
        listView.notifyDataSetChanged();
    }

    @Override
    public void onResized(CB_RectF rec) {
        super.onResized(rec);
        Layout();
    }

    public void NotifyDataSetChanged() {
        listView.notifyDataSetChanged();
    }

    public void RefreshTbList() {
        wd = CancelWaitDialog.ShowWait(Translation.Get("RefreshInventory"), DownloadAnimation.GetINSTANCE(), new IcancelListener() {

            @Override
            public void isCanceled() {

            }
        }, new cancelRunnable() {

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
            public boolean cancel() {
                // TODO handle cancel
                return false;
            }
        });
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
