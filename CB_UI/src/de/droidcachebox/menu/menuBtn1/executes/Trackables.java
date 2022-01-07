package de.droidcachebox.menu.menuBtn1.executes;

import static de.droidcachebox.core.GroundspeakAPI.APIError;
import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.core.GroundspeakAPI.downloadUsersTrackables;
import static de.droidcachebox.core.GroundspeakAPI.fetchTrackable;
import static de.droidcachebox.core.GroundspeakAPI.uploadTrackableLog;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.DraftsDatabase;
import de.droidcachebox.database.TrackableListDAO;
import de.droidcachebox.dataclasses.LogType;
import de.droidcachebox.dataclasses.TBList;
import de.droidcachebox.dataclasses.Trackable;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.TB_Details;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
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
import de.droidcachebox.gdx.views.TrackableListViewItem;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn4.executes.TemplateFormatter;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.log.Log;

public class Trackables extends V_ListView {
    private static final String log = "TrackableListView";
    public static Trackables trackables;
    private final TrackableListViewAdapter trackableListViewAdapter;
    private final TBList trackableList;

    private Trackables() {
        super(ViewManager.leftTab.getContentRec(), "TrackableListView");
        trackables = this;
        setBackground(Sprites.ListBack);
        trackableList = new TBList();
        trackableListViewAdapter = new TrackableListViewAdapter();
    }

    public static Trackables getInstance() {
        if (trackables == null) trackables = new Trackables();
        return trackables;
    }

    @Override
    public void initialize() {
        setAdapter(trackableListViewAdapter);
        setEmptyMsgItem(Translation.get("TB_List_Empty"));
    }

    public void dispose() {
        super.dispose();
        trackables = null;
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
        CoreCursor reader = DraftsDatabase.getInstance().rawQuery("select Id,Archived,GcCode,CacheId,CurrentGoal,CurrentOwnerName,DateCreated,Description,IconUrl,ImageUrl,Name,OwnerName,Url,TypeName,Home,TravelDistance from Trackable", null);
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
            AtomicBoolean isCanceled = new AtomicBoolean(false);
            CancelWaitDialog xx = new CancelWaitDialog(Translation.get("Search"), new DownloadAnimation(),
                    new RunAndReady() {
                        @Override
                        public void ready() {
                            if (GroundspeakAPI.APIError != OK) {
                                if (APIError == 404) {
                                    MsgBox.show(GroundspeakAPI.LastAPIError, Translation.get("NoTbFound"), MsgBoxButton.OK, MsgBoxIcon.Information, null);
                                } else {
                                    MsgBox.show(GroundspeakAPI.LastAPIError, "", MsgBoxButton.OK, MsgBoxIcon.Information, null);
                                }
                            }
                        }

                        @Override
                        public void run() {
                            Trackable tb = new Trackable();
                            tb = fetchTrackable(TBCode, tb);
                            if (tb != null) {
                                new TB_Details().show(tb);
                            }
                        }

                        @Override
                        public void setIsCanceled() {
                            isCanceled.set(true);
                        }

                    });
            xx.show();
        }
        return true;
    }

    // reload inventory
    public void refreshTbList() {
        AtomicBoolean isCanceled = new AtomicBoolean(false);
        new CancelWaitDialog(Translation.get("RefreshInventory"), new DownloadAnimation(),
                new RunAndReady() {
                    @Override
                    public void ready() {

                    }

                    @Override
                    public void run() {
                        TBList searchList = downloadUsersTrackables();
                        Log.info(log, "refreshTbList gotTBs");
                        TrackableListDAO.clearDB();
                        searchList.writeToDB();
                        reloadTB_List();
                    }

                    @Override
                    public void setIsCanceled() {
                        isCanceled.set(true);
                    }

                }).show();
    }

    private void logTBs(String title, final int LogTypeId, final String LogText) {
        AtomicBoolean isCanceled = new AtomicBoolean(false);
        new CancelWaitDialog(title, new DownloadAnimation(), new RunAndReady() {
            @Override
            public void ready() {

            }

            @Override
            public void run() {
                for (Trackable tb : trackableList) {
                    if (uploadTrackableLog(tb, GlobalCore.getSelectedCache().getGeoCacheCode(), LogTypeId, new Date(), LogText) != OK) {
                        MsgBox.show(GroundspeakAPI.LastAPIError, "", MsgBoxButton.OK, MsgBoxIcon.Information, null);
                    }
                }
            }

            @Override
            public void setIsCanceled() {
                isCanceled.set(true);
            }

        }).show();
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
            logTBs(((MenuItem) v).getTitle(), LogType.CB_LogType2GC(LogType.note), TemplateFormatter.replaceTemplate(Settings.AddNoteTemplate.getValue(), new Date()));
            return true;
        });
        cm.addMenuItem("all_visit", "", Sprites.getSprite(IconName.TBVISIT.name()), (v, x, y, pointer, button) -> {
            cm.close();
            logTBs(((MenuItem) v).getTitle(), LogType.CB_LogType2GC(LogType.visited), TemplateFormatter.replaceTemplate(Settings.VisitedTemplate.getValue(), new Date()));
            return true;
        });
        cm.addMenuItem("all_dropped", "", Sprites.getSprite(IconName.TBDROP.name()), (v, x, y, pointer, button) -> {
            cm.close();
            logTBs(((MenuItem) v).getTitle(), LogType.CB_LogType2GC(LogType.dropped_off), TemplateFormatter.replaceTemplate(Settings.DroppedTemplate.getValue(), new Date()));
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
