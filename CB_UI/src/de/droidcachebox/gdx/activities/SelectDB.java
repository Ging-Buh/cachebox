/*
 * Copyright (C) 2014 team-cachebox.de
 *
 * Licensed under the : GNU General Public License (GPL);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.droidcachebox.gdx.activities;

import static de.droidcachebox.settings.AllSettings.DatabaseName;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.CacheListDAO;
import de.droidcachebox.database.Categories;
import de.droidcachebox.database.DraftsDatabase;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.dialogs.NewDB_InputBox;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.Scrollbar;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.controls.messagebox.MsgBox.OnMsgBoxClickListener;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.menu.menuBtn5.ShowQuit;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.FileList;
import de.droidcachebox.utils.Point;
import de.droidcachebox.utils.log.Log;

/**
 * @author ging-buh
 * @author Longri
 */
public class SelectDB extends ActivityBase {
    private static final String log = "SelectDB";
    private final FileList dbFiles;
    private final boolean MustSelect;
    private DBItemAdapter dbItemAdapter;
    private Timer updateTimer;
    private int autoStartTime;
    private int autoStartCounter;
    private CB_Button bNew;
    private CB_Button bSelect;
    private CB_Button bCancel;
    private CB_Button bAutostart;
    private V_ListView lvDBSelection;
    private Scrollbar scrollbar;
    private AbstractFile currentDBFile = null;
    private IReturnListener returnListener;
    private final OnMsgBoxClickListener mDialogListenerNewDB = (which, data) -> {
        String NewDB_Name = NewDB_InputBox.editText.getText();
        switch (which) {
            case 1: // ok clicked

                FilterInstances.setLastFilter(new FilterProperties(Settings.FilterNew.getValue()));
                String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue());

                // initialize Database

                String database = GlobalCore.workPath + "/" + NewDB_Name + ".db3";
                Settings.DatabaseName.setValue(NewDB_Name + ".db3");
                Log.debug(log, "\r\nnew DB " + DatabaseName.getValue());
                CBDB.getInstance().close();
                CBDB.getInstance().startUp(database);

                // OwnRepository?
                if (data != null && !(Boolean) data) {
                    String folder = "?/" + NewDB_Name + "/";

                    Settings.DescriptionImageFolderLocal.setValue(folder + "Images");
                    Settings.MapPackFolderLocal.setValue(folder + "Maps");
                    Settings.SpoilerFolderLocal.setValue(folder + "Spoilers");
                    Settings.tileCacheFolderLocal.setValue(folder + "Cache");
                    ViewManager.that.acceptChanges();
                    Log.debug(log,
                            NewDB_Name + " has own Repository:\n" + //
                                    Settings.DescriptionImageFolderLocal.getValue() + ", \n" + //
                                    Settings.MapPackFolderLocal.getValue() + ", \n" + //
                                    Settings.SpoilerFolderLocal.getValue() + ", \n" + //
                                    Settings.tileCacheFolderLocal.getValue()//
                    );

                    // Create Folder?
                    boolean creationOK = FileIO.createDirectory(Settings.DescriptionImageFolderLocal.getValue());
                    creationOK = creationOK && FileIO.createDirectory(Settings.MapPackFolderLocal.getValue());
                    creationOK = creationOK && FileIO.createDirectory(Settings.SpoilerFolderLocal.getValue());
                    creationOK = creationOK && FileIO.createDirectory(Settings.tileCacheFolderLocal.getValue());
                    if (!creationOK)
                        Log.debug(log,
                                "Problem with creation of one of the Directories:" + //
                                        Settings.DescriptionImageFolderLocal.getValue() + ", " + //
                                        Settings.MapPackFolderLocal.getValue() + ", " + //
                                        Settings.SpoilerFolderLocal.getValue() + ", " + //
                                        Settings.tileCacheFolderLocal.getValue()//
                        );
                }

                ViewManager.that.acceptChanges();

                CoreData.categories = new Categories();
                CacheDAO.getInstance().updateCacheCountForGPXFilenames();

                synchronized (CBDB.getInstance().cacheList) {
                    CacheListDAO.getInstance().readCacheList(sqlWhere, false, false, Settings.showAllWaypoints.getValue());
                    GlobalCore.checkSelectedCacheValid();
                }

                if (!FileIO.createDirectory(GlobalCore.workPath + "/User"))
                    return true;
                DraftsDatabase.getInstance().startUp(GlobalCore.workPath + "/User/FieldNotes.db3");

                ViewManager.that.acceptChanges();
                currentDBFile = FileFactory.createFile(database);
                selectDB();

                break;
            case 2: // cancel clicked
            case 3:
                activityBase.show();
                break;
        }

        return true;
    };

    public SelectDB(CB_RectF rec, String Name, boolean mustSelect) {
        super(rec, Name);
        MustSelect = mustSelect;

        lvDBSelection = new V_ListView(new CB_RectF(leftBorder, getBottomHeight() + UiSizes.getInstance().getButtonHeight() * 2, innerWidth, getHeight() - (UiSizes.getInstance().getButtonHeight() * 2) - getTopHeight() - getBottomHeight()),
                "DB File ListView");
        dbFiles = new FileList(GlobalCore.workPath, "DB3", true);
        dbItemAdapter = new DBItemAdapter();
        lvDBSelection.setAdapter(dbItemAdapter);

        String currentDBFileName = Settings.DatabaseName.getValue();
        for (AbstractFile dbFile : dbFiles) {
            if (dbFile.getName().equalsIgnoreCase(currentDBFileName)) {
                currentDBFile = dbFile;
                break;
            }
        }

        addChild(lvDBSelection);

        scrollbar = new Scrollbar(lvDBSelection);
        addChild(scrollbar);
        lvDBSelection.addListPosChangedEventHandler(() -> scrollbar.scrollPositionChanged());

        float btWidth = innerWidth / 3;

        bNew = new CB_Button(new CB_RectF(leftBorder, getBottomHeight(), btWidth, UiSizes.getInstance().getButtonHeight()), "selectDB.bNew");
        bSelect = new CB_Button(new CB_RectF(bNew.getMaxX(), getBottomHeight(), btWidth, UiSizes.getInstance().getButtonHeight()), "selectDB.bSelect");
        bCancel = new CB_Button(new CB_RectF(bSelect.getMaxX(), getBottomHeight(), btWidth, UiSizes.getInstance().getButtonHeight()), "selectDB.bCancel");
        bAutostart = new CB_Button(new CB_RectF(leftBorder, bNew.getMaxY(), innerWidth, UiSizes.getInstance().getButtonHeight()), "selectDB.bAutostart");

        addChild(bSelect);
        addChild(bNew);
        addChild(bCancel);
        addChild(bAutostart);

        // New Button
        bNew.setClickHandler((v, x, y, pointer, button) -> {
            stopTimer();
            NewDB_InputBox.Show(WrapType.SINGLELINE, Translation.get("NewDB"), Translation.get("InsNewDBName"), "NewDB", mDialogListenerNewDB);
            return true;
        });

        // Select Button
        bSelect.setClickHandler((v, x, y, pointer, button) -> {
            stopTimer();
            if (currentDBFile == null) {
                GL.that.toast("Please select Database!");
                return false;
            }
            selectDB();
            return true;
        });

        // Cancel Button
        bCancel.setClickHandler((v, x, y, pointer, button) -> {
            stopTimer();
            if (MustSelect) {
                ShowQuit.getInstance().execute();
            } else {
                finish();
            }

            return true;
        });

        // AutoStart Button
        bAutostart.setClickHandler((v, x, y, pointer, button) -> {
            stopTimer();
            showSelectionMenu();
            return true;
        });

        bNew.setText(Translation.get("NewDB"));
        bSelect.setText(Translation.get("confirm"));
        bCancel.setText(Translation.get("cancel"));
        autoStartTime = Settings.MultiDBAutoStartTime.getValue();
        autoStartCounter = 0;
        if (autoStartTime > 0) {
            autoStartCounter = autoStartTime;
            bAutostart.setText(autoStartCounter + " " + Translation.get("confirm"));
            if ((autoStartTime > 0) && (currentDBFile != null)) {
                updateTimer = new Timer();
                updateTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (autoStartCounter == 0) {
                            stopTimer();
                            selectDB();
                        } else {
                            try {
                                autoStartCounter--;
                                bAutostart.setText(autoStartCounter + "    " + Translation.get("confirm"));
                            } catch (Exception e) {
                                autoStartCounter = 0;
                                stopTimer();
                                selectDB();
                            }
                        }
                    }
                }, 1000, 1000);
            } else
                stopTimer();
        }
        setAutoStartText();

        isClickable();
    }

    @Override
    public void onShow() {

        int itemSpace = lvDBSelection.getMaxNumberOfVisibleItems();

        if (itemSpace >= dbItemAdapter.getCount()) {
            lvDBSelection.setUnDraggable();
        } else {
            lvDBSelection.setDraggable();
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                // Set selected item
                for (int i = 0; i < dbItemAdapter.getCount(); i++) {
                    AbstractFile abstractFile = dbFiles.get(i);

                    try {
                        if (abstractFile.getAbsoluteFile().compareTo(currentDBFile.getAbsoluteFile()) == 0) {
                            lvDBSelection.setSelection(i);
                        }

                        Point firstAndLast = lvDBSelection.getFirstAndLastVisibleIndex();

                        if (!(firstAndLast.x < i && firstAndLast.y > i))
                            lvDBSelection.scrollToItem(i);
                    } catch (Exception e) {
                        Log.err(log, "select item", e);
                    }
                }

                GL.that.RunOnGL(() -> setSelectedItemVisible());

                resetIsInitialized();
                lvDBSelection.chkSlideBack();
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 350);

        GL.that.renderOnce();
    }

    private void setSelectedItemVisible() {
        int id = 0;
        Point firstAndLast = lvDBSelection.getFirstAndLastVisibleIndex();

        try {
            for (AbstractFile abstractFile : dbFiles) {
                if (abstractFile.getAbsoluteFile().compareTo(currentDBFile.getAbsoluteFile()) == 0) {
                    lvDBSelection.setSelection(id);
                    if (lvDBSelection.isDraggable()) {
                        if (!(firstAndLast.x <= id && firstAndLast.y >= id)) {
                            lvDBSelection.scrollToItem(id);
                        }
                    }
                    break;
                }
                id++;
            }

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    GL.that.RunOnGL(() -> {
                        if (lvDBSelection != null) {
                            lvDBSelection.chkSlideBack();
                            GL.that.renderOnce();
                        }
                    });
                }
            };

            Timer timer = new Timer();
            timer.schedule(task, 50);

            GL.that.renderOnce();
        } catch (Exception e) {
            Log.err(log, "Set selected DB Visible", e);
        }
    }

    private void selectDB() {
        if (currentDBFile == null) {
            GL.that.toast("no DB selected");
            return;
        }

        Settings.MultiDBAutoStartTime.setValue(autoStartTime);
        Settings.MultiDBAsk.setValue(autoStartTime >= 0);

        Settings.DatabaseName.setValue(currentDBFile.getName());
        ViewManager.that.acceptChanges();

        finish();

        if (returnListener != null)
            returnListener.back();

    }

    @Override
    public void finish() {
        GL.that.RunOnGL(() -> GL.that.closeActivity(!MustSelect));
    }

    private void setAutoStartText() {
        if (autoStartTime < 0)
            bAutostart.setText(Translation.get("StartWithoutSelection"));
        else if (autoStartTime == 0)
            bAutostart.setText(Translation.get("AutoStartDisabled"));
        else
            bAutostart.setText(Translation.get("AutoStartTime", String.valueOf(autoStartTime)));
    }

    private void stopTimer() {
        if (updateTimer != null)
            updateTimer.cancel();
        // bAutostart.setText(Translation.Get("confirm"));
    }

    public void setReturnListener(IReturnListener listener) {
        returnListener = listener;
    }

    private void showSelectionMenu() {
        Menu cm = new Menu("SelectDBContextMenuTitle");
        cm.addMenuItem("StartWithoutSelection", null, () -> {
            autoStartTime = -1;
            setAutoStartText();
        });
        cm.addMenuItem("AutoStartDisabled", null, () -> {
            autoStartTime = 0;
            setAutoStartText();
        });
        cm.addMenuItem("", Translation.get("AutoStartTime", "5"), null, () -> {
            autoStartTime = 5;
            setAutoStartText();
        });
        cm.addMenuItem("", Translation.get("AutoStartTime", "10"), null, () -> {
            autoStartTime = 10;
            setAutoStartText();
        });
        cm.addMenuItem("", Translation.get("AutoStartTime", "25"), null, () -> {
            autoStartTime = 25;
            setAutoStartText();
        });
        cm.addMenuItem("", Translation.get("AutoStartTime", "60"), null, () -> {
            autoStartTime = 60;
            setAutoStartText();
        });
        cm.show();
    }

    @Override
    public boolean canCloseWithBackKey() {
        return !MustSelect;
    }

    @Override
    public void dispose() {

        if (bNew != null)
            bNew.dispose();
        bNew = null;
        if (bSelect != null)
            bSelect.dispose();
        bSelect = null;
        if (bCancel != null)
            bCancel.dispose();
        bCancel = null;
        if (bAutostart != null)
            bAutostart.dispose();
        bAutostart = null;
        if (lvDBSelection != null)
            lvDBSelection.dispose();
        lvDBSelection = null;
        if (scrollbar != null)
            scrollbar.dispose();
        scrollbar = null;

        dbItemAdapter = null;
        currentDBFile = null;

        returnListener = null;
        super.dispose();
    }

    public interface IReturnListener {
        void back();
    }

    private class DBItemAdapter implements Adapter {

        private final CB_RectF recItem;
        private final float itemHeight;

        public DBItemAdapter() {
            itemHeight = UiSizes.getInstance().getButtonHeight() * 1.2f;
            recItem = new CB_RectF(0, 0, lvDBSelection.getInnerWidth(), itemHeight);
        }

        @Override
        public int getCount() {
            return dbFiles.size();
        }

        @Override
        public ListViewItemBase getView(int position) {
            return new DBItem(recItem, position);
        }

        @Override
        public float getItemSize(int position) {
            return itemHeight;
        }

    }

    private class DBItem extends ListViewItemBackground {

        DBItem(CB_RectF rec, int index) {
            super(rec, index, "" + index);
            AbstractFile theFileToShow = dbFiles.get(index);

            float left = 20;
            float mLabelHeight = getHeight() * 0.7f;
            float mLabelYPos = (getHeight() - mLabelHeight) / 2;

            CB_Label lblName = new CB_Label(name + " lblName", left, mLabelYPos, getWidth(), mLabelHeight);
            lblName.setFont(Fonts.getBig());
            lblName.setVAlignment(CB_Label.VAlignment.TOP);
            lblName.setText(theFileToShow.getName());
            addChild(lblName);

            CB_Label lblInfo = new CB_Label(name + " lblInfo", left, mLabelYPos, getWidth(), mLabelHeight);
            lblInfo.setFont(Fonts.getBubbleNormal());
            lblInfo.setVAlignment(CB_Label.VAlignment.BOTTOM);

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);

            lblInfo.setText(PlatformUIBase.getCacheCountInDB(theFileToShow.getAbsolutePath())
                    + " Caches  "
                    + theFileToShow.length() / (1024 * 1024) + "MB"
                    + "    last use "
                    + sdf.format(theFileToShow.lastModified()));


            addChild(lblInfo);

            setClickHandler((v1, x, y, pointer, button) -> {
                stopTimer();
                // int pos = ((DBItem) v1).getIndex();
                currentDBFile = dbFiles.get(mIndex);
                lvDBSelection.setSelection(mIndex);
                return true;
            });

            setClickable(true);
        }

    }

}
