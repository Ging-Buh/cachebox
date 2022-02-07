package de.droidcachebox.menu.menuBtn1.executes;

import static de.droidcachebox.core.GroundspeakAPI.APIError;
import static de.droidcachebox.core.GroundspeakAPI.OK;
import static de.droidcachebox.core.GroundspeakAPI.downloadUsersTrackables;
import static de.droidcachebox.core.GroundspeakAPI.fetchTrackable;
import static de.droidcachebox.core.GroundspeakAPI.uploadTrackableLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.core.GroundspeakAPI;
import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.DraftsDatabase;
import de.droidcachebox.dataclasses.Trackable;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.WrapType;
import de.droidcachebox.gdx.activities.TB_Details;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.Image;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.controls.dialogs.StringInputBox;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.Action;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.translation.Translation;

public class TrackableListView extends V_ListView {
    private static final String sClass = "TrackableListView";
    private final TrackableListViewAdapter trackableListViewAdapter;
    private final ArrayList<Trackable> trackableList;

    public TrackableListView() {
        super(ViewManager.leftTab.getContentRec(), "TrackableListView");
        setBackground(Sprites.ListBack);
        trackableList = new ArrayList<>();
        trackableListViewAdapter = new TrackableListViewAdapter();
    }

    @Override
    public void renderInit() {
        setAdapter(trackableListViewAdapter);
        setEmptyMsgItem(Translation.get("TB_List_Empty"));
    }

    public void onShow() {
        readFromDatabase();
        notifyDataSetChanged();
    }

    public void onHide() {
        ((AbstractShowAction)Action.ShowTrackableList.action).viewIsHiding();
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
                        downloadUsersTrackables(trackableList);
                        writeToDatabase();
                        notifyDataSetChanged();
                    }

                    @Override
                    public void setIsCanceled() {
                        isCanceled.set(true);
                    }

                }).show();
    }

    public void logTBs(String title, final int LogTypeId, final String LogText) {
        AtomicBoolean isCanceled = new AtomicBoolean(false);
        new CancelWaitDialog(title, new DownloadAnimation(), new RunAndReady() {
            @Override
            public void ready() {

            }

            @Override
            public void run() {
                for (Trackable tb : trackableList) {
                    if (uploadTrackableLog(tb, GlobalCore.getSelectedCache().getGeoCacheCode(), LogTypeId, new Date(), LogText) != OK) {
                        new ButtonDialog(GroundspeakAPI.LastAPIError, "", MsgBoxButton.OK, MsgBoxIcon.Information).show();
                    }
                }
            }

            @Override
            public void setIsCanceled() {
                isCanceled.set(true);
            }

        }).show();
    }

    public void searchTB() {
        StringInputBox stringInputBox = new StringInputBox(Translation.get("InputTB_Code"), Translation.get("SearchTB"), "", WrapType.SINGLELINE);
        stringInputBox.setButtonClickHandler((which, data) -> {
            if (which == ButtonDialog.BTN_LEFT_POSITIVE) {
                String TBCode = StringInputBox.editTextField.getText();
                if (TBCode.length() > 0) {
                    AtomicBoolean isCanceled = new AtomicBoolean(false);
                    new CancelWaitDialog(Translation.get("Search"), new DownloadAnimation(),
                            new RunAndReady() {
                                @Override
                                public void ready() {
                                    if (GroundspeakAPI.APIError != OK) {
                                        if (APIError == 404) {
                                            new ButtonDialog(GroundspeakAPI.LastAPIError, Translation.get("NoTbFound"), MsgBoxButton.OK, MsgBoxIcon.Information).show();
                                        } else {
                                            new ButtonDialog(GroundspeakAPI.LastAPIError, "", MsgBoxButton.OK, MsgBoxIcon.Information).show();
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

                            }).show();
                }
            }
            return true;
        });
        stringInputBox.showAtTop();
    }

    private void clearDB() {
        DraftsDatabase.getInstance().delete("Trackable", "", null);
    }

    private void readFromDatabase() {
        trackableList.clear();
        CoreCursor reader = DraftsDatabase.getInstance().rawQuery("select * from Trackable", null);
        if (reader != null) {
            if (reader.getCount() > 0) {
                reader.moveToFirst();
                while (!reader.isAfterLast()) {
                    trackableList.add(new Trackable(reader));
                    reader.moveToNext();
                }
            }
            reader.close();
        }
    }

    private void writeToDatabase() {
        clearDB();
        for (Trackable trackable : trackableList) {
            trackable.writeToDatabase();
        }
    }

    /*
    private void addToDatabase() {
        for (Trackable trackable : trackableList) {
            try {
                if (getFromDbByTBCode(trackable.getTbCode()) == null) {
                    writeToDatabase(trackable);
                } else {
                    updateDatabase(trackable);
                }
            } catch (Exception ex) {
                Log.err(sClass, "TrackableList writeToDatabase", ex);
            }
        }
    }

    private Trackable getFromDbByTBCode(String tbCode) {
        String where = "GcCode = \"" + tbCode + "\"";
        String query = "select Id ,Archived ,GcCode ,CacheId ,CurrentGoal ,CurrentOwnerName ,DateCreated ,Description ,IconUrl ,ImageUrl ,Name ,OwnerName ,Url,TypeName, Home,TravelDistance   from Trackable WHERE " + where;
        Trackable trackable = null;
        CoreCursor reader = DraftsDatabase.getInstance().rawQuery(query, null);
        if (reader != null) {
            if (reader.getCount() > 0) {
                reader.moveToFirst();
                trackable = new Trackable(reader);
            }
            reader.close();
        }
        return trackable;
    }

    private void updateDatabase(Trackable trackable) {
        try {
            Log.debug(sClass, "Write Trackable createArgs");
            Database_Core.Parameters args = createArgs(trackable);
            Log.debug(sClass, "Write Trackable update");
            DraftsDatabase.getInstance().update("Trackable", args, "GcCode='" + trackable.getTbCode() + "'", null);
        } catch (Exception exc) {
            Log.err(sClass, "Update Trackable error", exc);
        }
    }

     */

    private class TrackableListViewAdapter implements Adapter {
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

        private class TrackableListViewItem extends ListViewItemBackground {

            public TrackableListViewItem(CB_RectF rec, int Index, Trackable trackable) {
                super(rec, Index, trackable.getName());

                float hw = getHeight() - getTopHeight() - getBottomHeight();
                topBorder = getTopHeight();
                bottomBorder = getBottomHeight();
                leftBorder = getLeftWidth();
                rightBorder = getRightWidth();

                Image img = new Image(0, 0, hw, hw, "img", false);
                img.setImageURL(trackable.getIconUrl());
                addNext(img, FIXED);

                CB_Label lblName = new CB_Label("lblName", 0, 0, getWidth() - img.getMaxX() - UiSizes.getInstance().getMargin(), img.getHeight());
                lblName.setWrappedText(trackable.getName());
                addLast(lblName);
                setClickHandler((v1, x, y, pointer, button) -> {
                    new TB_Details().show(trackable);
                    return true;
                });

            }

        }

    }
}
