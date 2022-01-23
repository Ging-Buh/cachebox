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
package de.droidcachebox.menu.menuBtn1.executes;

import static de.droidcachebox.settings.AllSettings.DatabaseName;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.Platform;
import de.droidcachebox.core.CoreData;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CacheDAO;
import de.droidcachebox.database.CacheListDAO;
import de.droidcachebox.database.DraftsDatabase;
import de.droidcachebox.dataclasses.Categories;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.NewDB_InputBox;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.Scrollbar;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
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
    private static final String sClass = "SelectDB";
    private final FileList dbFiles;
    private final boolean mustSelect;
    private DBItemAdapter dbItemAdapter;
    private Timer updateTimer;
    private int autoStartTime;
    private int autoStartCounter;
    private CB_Button btnNew;
    private CB_Button btnSelect;
    private CB_Button btnCancel;
    private CB_Button btnAutostart;
    private V_ListView lvDBSelection;
    private Scrollbar scrollbar;
    private AbstractFile currentDBFile = null;
    private IReturnListener returnListener;

    public SelectDB(CB_RectF rec, String Name, boolean mustSelect) {
        super(rec, Name);
        this.mustSelect = mustSelect;

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

        btnNew = new CB_Button(new CB_RectF(leftBorder, getBottomHeight(), btWidth, UiSizes.getInstance().getButtonHeight()), "selectDB.bNew");
        btnSelect = new CB_Button(new CB_RectF(btnNew.getMaxX(), getBottomHeight(), btWidth, UiSizes.getInstance().getButtonHeight()), "selectDB.bSelect");
        btnCancel = new CB_Button(new CB_RectF(btnSelect.getMaxX(), getBottomHeight(), btWidth, UiSizes.getInstance().getButtonHeight()), "selectDB.bCancel");
        btnAutostart = new CB_Button(new CB_RectF(leftBorder, btnNew.getMaxY(), innerWidth, UiSizes.getInstance().getButtonHeight()), "selectDB.bAutostart");

        addChild(btnSelect);
        addChild(btnNew);
        addChild(btnCancel);
        addChild(btnAutostart);

        // New Button
        btnNew.setClickHandler((v, x, y, pointer, button) -> {
            stopTimer();
            NewDB_InputBox newDB_inputBox = new NewDB_InputBox(Translation.get("NewDB"), Translation.get("InsNewDBName"));
            newDB_inputBox.setButtonClickHandler((which, data) -> {
                if (which == ButtonDialog.BTN_LEFT_POSITIVE) {
                    String NewDB_Name = NewDB_InputBox.editTextField.getText();
                    if (NewDB_Name.length() > 0) {

                        FilterInstances.setLastFilter(new FilterProperties(Settings.lastFilter.getValue()));
                        String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Settings.GcLogin.getValue());

                        // initialize Database

                        String database = GlobalCore.workPath + "/" + NewDB_Name + ".db3";
                        Settings.DatabaseName.setValue(NewDB_Name + ".db3");
                        Log.debug(sClass, "\r\nnew DB " + DatabaseName.getValue());
                        CBDB.getInstance().close();
                        CBDB.getInstance().startUp(database);

                        if (!((AtomicBoolean) data).get()) {
                            // use an own Repository area for spoiler, images, maps, cache
                            String folder = "?/" + NewDB_Name + "/";

                            Settings.DescriptionImageFolderLocal.setValue(folder + "Images");
                            Settings.MapPackFolderLocal.setValue(folder + "Maps");
                            Settings.SpoilerFolderLocal.setValue(folder + "Spoilers");
                            Settings.tileCacheFolderLocal.setValue(folder + "Cache");
                            Settings.getInstance().acceptChanges();
                            Log.debug(sClass,
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
                                Log.debug(sClass,
                                        "Problem with creation of one of the Directories:" + //
                                                Settings.DescriptionImageFolderLocal.getValue() + ", " + //
                                                Settings.MapPackFolderLocal.getValue() + ", " + //
                                                Settings.SpoilerFolderLocal.getValue() + ", " + //
                                                Settings.tileCacheFolderLocal.getValue()//
                                );
                        }

                        Settings.getInstance().acceptChanges();

                        CoreData.categories = new Categories();
                        new CacheDAO().updateCacheCountForGPXFilenames();

                        synchronized (CBDB.getInstance().cacheList) {
                            CacheListDAO.getInstance().readCacheList(sqlWhere, false, false, Settings.showAllWaypoints.getValue());
                            GlobalCore.checkSelectedCacheValid();
                        }

                        if (!FileIO.createDirectory(GlobalCore.workPath + "/User"))
                            return true;
                        DraftsDatabase.getInstance().startUp(GlobalCore.workPath + "/User/FieldNotes.db3");

                        Settings.getInstance().acceptChanges();
                        currentDBFile = FileFactory.createFile(database);
                        selectDB();
                    }
                }
                return true;
            });
            newDB_inputBox.showAtTop();
            return true;
        });

        // Select Button
        btnSelect.setClickHandler((v, x, y, pointer, button) -> {
            stopTimer();
            if (currentDBFile == null) {
                GL.that.toast("Please select Database!");
                return false;
            }
            selectDB();
            return true;
        });

        // Cancel Button
        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            stopTimer();
            if (this.mustSelect) {
                ShowQuit.getInstance().execute();
            } else {
                finish();
            }

            return true;
        });

        // AutoStart Button
        btnAutostart.setClickHandler((v, x, y, pointer, button) -> {
            stopTimer();
            showSelectionMenu();
            return true;
        });

        btnNew.setText(Translation.get("NewDB"));
        btnSelect.setText(Translation.get("confirm"));
        btnCancel.setText(Translation.get("cancel"));
        autoStartTime = Settings.MultiDBAutoStartTime.getValue();
        autoStartCounter = 0;
        if (autoStartTime > 0) {
            autoStartCounter = autoStartTime;
            btnAutostart.setText(autoStartCounter + " " + Translation.get("confirm"));
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
                                btnAutostart.setText(autoStartCounter + "    " + Translation.get("confirm"));
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
                        Log.err(sClass, "select item", e);
                    }
                }

                GL.that.runOnGL(() -> setSelectedItemVisible());

                resetRenderInitDone();
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
                    GL.that.runOnGL(() -> {
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
            Log.err(sClass, "Set selected DB Visible", e);
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
        Settings.getInstance().acceptChanges();

        finish();

        if (returnListener != null)
            returnListener.dbSelected();

    }

    @Override
    public void finish() {
        GL.that.runOnGL(() -> GL.that.closeActivity(!mustSelect));
    }

    private void setAutoStartText() {
        if (autoStartTime < 0)
            btnAutostart.setText(Translation.get("StartWithoutSelection"));
        else if (autoStartTime == 0)
            btnAutostart.setText(Translation.get("AutoStartDisabled"));
        else
            btnAutostart.setText(Translation.get("AutoStartTime", String.valueOf(autoStartTime)));
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
        return !mustSelect;
    }

    @Override
    public void dispose() {

        if (btnNew != null)
            btnNew.dispose();
        btnNew = null;
        if (btnSelect != null)
            btnSelect.dispose();
        btnSelect = null;
        if (btnCancel != null)
            btnCancel.dispose();
        btnCancel = null;
        if (btnAutostart != null)
            btnAutostart.dispose();
        btnAutostart = null;
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
        void dbSelected();
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

            lblInfo.setText(Platform.getCacheCountInDB(theFileToShow.getAbsolutePath())
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
