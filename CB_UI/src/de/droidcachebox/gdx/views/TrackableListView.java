package de.droidcachebox.gdx.views;

import static de.droidcachebox.core.GroundspeakAPI.APIError;
import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.core.GroundspeakAPI.downloadUsersTrackables;
import static de.droidcachebox.core.GroundspeakAPI.fetchTrackable;
import static de.droidcachebox.core.GroundspeakAPI.uploadTrackableLog;

import java.util.Date;

import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.TemplateFormatter;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.LogType;
import de.droidcachebox.database.TBList;
import de.droidcachebox.database.Trackable;
import de.droidcachebox.database.TrackableListDAO;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.TB_Details;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.StringInputBox;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.main.MenuItem;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ICancelRunnable;
import de.droidcachebox.utils.log.Log;

public class TrackableListView extends V_ListView {
    private static final String log = "TrackableListView";
    public static TrackableListView that;
    private TrackableListViewAdapter trackableListViewAdapter;
    private TBList trackableList;
    private CancelWaitDialog wd;

    private TrackableListView() {
        super(ViewManager.leftTab.getContentRec(), "TrackableListView");
        that = this;
        setBackground(Sprites.ListBack);
        trackableList = new TBList();
        trackableListViewAdapter = new TrackableListViewAdapter();
    }

    public static TrackableListView getInstance() {
        if (that == null) that = new TrackableListView();
        return that;
    }

    @Override
    public void initialize() {
        setAdapter(trackableListViewAdapter);
        setEmptyMsgItem(Translation.get("TB_List_Empty"));
    }

    public void dispose() {
        super.dispose();
        that = null;
    }

    public void onShow() {
        reloadTB_List();
        // resetIsInitialized(); is not needed
    }

    private void reloadTB_List() {
        readTbList();
        notifyDataSetChanged();
    }

    private void readTbList() {
        trackableList.clear();
        CoreCursor reader = Database.Drafts.sql.rawQuery("select Id,Archived,GcCode,CacheId,CurrentGoal,CurrentOwnerName,DateCreated,Description,IconUrl,ImageUrl,Name,OwnerName,Url,TypeName,Home,TravelDistance from Trackable", null);
        if (reader != null) {
            reader.moveToFirst();
            while (!reader.isAfterLast()) {
                trackableList.add(new Trackable(reader));
                reader.moveToNext();
            }
            reader.close();
        }
    }

    private boolean fetchTB(final String TBCode) {
        if (TBCode.length() > 0) {
            wd = CancelWaitDialog.ShowWait(Translation.get("Search"), DownloadAnimation.GetINSTANCE(),
                    () -> {
                        // IcancelListener
                    },
                    new ICancelRunnable() {
                        @Override
                        public void run() {
                            Trackable tb = fetchTrackable(TBCode);
                            wd.close();
                            if (tb == null) {
                                if (APIError == 404) {
                                    MsgBox.show(GroundspeakAPI.LastAPIError, Translation.get("NoTbFound"), MsgBoxButton.OK, MsgBoxIcon.Information, null);
                                } else {
                                    MsgBox.show(GroundspeakAPI.LastAPIError, "", MsgBoxButton.OK, MsgBoxIcon.Information, null);
                                }
                                return;
                            }
                            new TB_Details().show(tb);
                        }

                        @Override
                        public boolean doCancel() {
                            return false;
                        }
                    });
        }
        return true;
    }

    // Inventar neu laden
    public void refreshTbList() {
        wd = CancelWaitDialog.ShowWait(Translation.get("RefreshInventory"), DownloadAnimation.GetINSTANCE(),
                () -> {
                    // IcancelListener
                },
                new ICancelRunnable() {
                    @Override
                    public void run() {
                        TBList searchList = downloadUsersTrackables();
                        Log.info(log, "refreshTbList gotTBs");
                        TrackableListDAO.clearDB();
                        searchList.writeToDB();
                        reloadTB_List();
                        wd.close();
                    }

                    @Override
                    public boolean doCancel() {
                        return false;
                    }
                });
    }

    private void logTBs(String title, final int LogTypeId, final String LogText) {
        wd = CancelWaitDialog.ShowWait(title, DownloadAnimation.GetINSTANCE(), () -> {

        }, new ICancelRunnable() {

            @Override
            public void run() {
                for (Trackable tb : trackableList) {
                    if (uploadTrackableLog(tb, GlobalCore.getSelectedCache().getGeoCacheCode(), LogTypeId, new Date(), LogText) != OK) {
                        MsgBox.show(GroundspeakAPI.LastAPIError, "", MsgBoxButton.OK, MsgBoxIcon.Information, null);
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
        StringInputBox.show(WrapType.SINGLELINE, Translation.get("InputTB_Code"), Translation.get("SearchTB"), "", (which, data) -> {
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
        cm.addMenuItem("RefreshInventory", null, this::refreshTbList);
        cm.addMenuItem("all_note", "", Sprites.getSprite(IconName.TBNOTE.name()), (v, x, y, pointer, button) -> {
            cm.close();
            logTBs(((MenuItem) v).getTitle(), LogType.CB_LogType2GC(LogType.note), TemplateFormatter.ReplaceTemplate(Config.AddNoteTemplate.getValue(), new Date()));
            return true;
        });
        cm.addMenuItem("all_visit", "", Sprites.getSprite(IconName.TBVISIT.name()), (v, x, y, pointer, button) -> {
            cm.close();
            logTBs(((MenuItem) v).getTitle(), LogType.CB_LogType2GC(LogType.visited), TemplateFormatter.ReplaceTemplate(Config.VisitedTemplate.getValue(), new Date()));
            return true;
        });
        cm.addMenuItem("all_dropped", "", Sprites.getSprite(IconName.TBDROP.name()), (v, x, y, pointer, button) -> {
            cm.close();
            logTBs(((MenuItem) v).getTitle(), LogType.CB_LogType2GC(LogType.dropped_off), TemplateFormatter.ReplaceTemplate(Config.DroppedTemplate.getValue(), new Date()));
            refreshTbList();
            return true;
        });
        return cm;
    }

    public class TrackableListViewAdapter implements Adapter {
        final float fixedItemHeight = UiSizes.getInstance().getCacheListItemRec().getHeight();

        @Override
        public int getCount() {
            if (trackableList == null)
                return 0;
            return trackableList.size();
        }

        @Override
        public ListViewItemBase getView(final int position) {
            return new TrackableListViewItem(UiSizes.getInstance().getCacheListItemRec().asFloat(), position, trackableList.get(position));
        }

        @Override
        public float getItemSize(int position) {
            return fixedItemHeight;
        }
    }

}
