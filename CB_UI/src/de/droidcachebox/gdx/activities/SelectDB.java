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

import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.CoreSettingsForward;
import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CacheListDAO;
import de.droidcachebox.database.Categories;
import de.droidcachebox.database.Database;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.dialogs.NewDB_InputBox;
import de.droidcachebox.gdx.controls.dialogs.Toast;
import de.droidcachebox.gdx.controls.list.*;
import de.droidcachebox.gdx.controls.messagebox.MessageBox.OnMsgBoxClickListener;
import de.droidcachebox.gdx.main.Action_ShowQuit;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.map.LayerManager;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.*;
import de.droidcachebox.utils.log.Log;

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author ging-buh
 * @author Longri
 */
public class SelectDB extends ActivityBase {
    private static final String log = "SelectDB";
    private final FileList dbFiles;
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
    private File AktFile = null;
    private boolean MustSelect;
    private IReturnListener returnListener;
    private OnMsgBoxClickListener mDialogListenerNewDB = (which, data) -> {
        String NewDB_Name = NewDB_InputBox.editText.getText();
        // Behandle das Ergebnis
        switch (which) {
            case 1: // ok clicked

                FilterInstances.setLastFilter(new FilterProperties(Config.FilterNew.getValue()));
                String sqlWhere = FilterInstances.getLastFilter().getSqlWhere(Config.GcLogin.getValue());

                // initialize Database

                String database = Config.mWorkPath + "/" + NewDB_Name + ".db3";
                Config.DatabaseName.setValue(NewDB_Name + ".db3");
                Database.Data.sql.close();
                Database.Data.startUp(database);

                // OwnRepository?
                if (data != null && !(Boolean) data) {
                    String folder = "?/" + NewDB_Name + "/";

                    Config.DescriptionImageFolderLocal.setValue(folder + "Images");
                    Config.MapPackFolderLocal.setValue(folder + "Maps");
                    Config.SpoilerFolderLocal.setValue(folder + "Spoilers");
                    Config.TileCacheFolderLocal.setValue(folder + "Cache");
                    Config.AcceptChanges();
                    Log.debug(log,
                            NewDB_Name + " has own Repository:\n" + //
                                    Config.DescriptionImageFolderLocal.getValue() + ", \n" + //
                                    Config.MapPackFolderLocal.getValue() + ", \n" + //
                                    Config.SpoilerFolderLocal.getValue() + ", \n" + //
                                    Config.TileCacheFolderLocal.getValue()//
                    );

                    // Create Folder?
                    boolean creationOK = FileIO.createDirectory(Config.DescriptionImageFolderLocal.getValue());
                    creationOK = creationOK && FileIO.createDirectory(Config.MapPackFolderLocal.getValue());
                    creationOK = creationOK && FileIO.createDirectory(Config.SpoilerFolderLocal.getValue());
                    creationOK = creationOK && FileIO.createDirectory(Config.TileCacheFolderLocal.getValue());
                    if (!creationOK)
                        Log.debug(log,
                                "Problem with creation of one of the Directories:" + //
                                        Config.DescriptionImageFolderLocal.getValue() + ", " + //
                                        Config.MapPackFolderLocal.getValue() + ", " + //
                                        Config.SpoilerFolderLocal.getValue() + ", " + //
                                        Config.TileCacheFolderLocal.getValue()//
                        );
                }

                Config.AcceptChanges();

                CoreSettingsForward.Categories = new Categories();
                Database.Data.GPXFilenameUpdateCacheCount();

                synchronized (Database.Data.cacheList) {
                    CacheListDAO cacheListDAO = new CacheListDAO();
                    cacheListDAO.ReadCacheList(Database.Data.cacheList, sqlWhere, false, Config.ShowAllWaypoints.getValue());
                    GlobalCore.checkSelectedCacheValid();
                }

                if (!FileIO.createDirectory(Config.mWorkPath + "/User"))
                    return true;
                Database.Drafts.startUp(Config.mWorkPath + "/User/FieldNotes.db3");

                Config.AcceptChanges();
                AktFile = FileFactory.createFile(database);
                selectDB();

                break;
            case 2: // cancel clicked
                SelectDB.this.show();
                break;
            case 3:
                SelectDB.this.show();
                break;
        }

        return true;
    };

    public SelectDB(CB_RectF rec, String Name, boolean mustSelect) {
        super(rec, Name);
        MustSelect = mustSelect;

        lvDBSelection = new V_ListView(new CB_RectF(leftBorder, this.getBottomHeight() + UiSizes.getInstance().getButtonHeight() * 2, innerWidth, getHeight() - (UiSizes.getInstance().getButtonHeight() * 2) - this.getTopHeight() - this.getBottomHeight()),
                "DB File ListView");
        dbFiles = new FileList(Config.mWorkPath, "DB3", true);
        dbItemAdapter = new DBItemAdapter(lvDBSelection, dbFiles);

        String dbFile = Config.DatabaseName.getValue();
        for (File file : dbFiles) {
            if (file.getName().equalsIgnoreCase(dbFile)) {
                AktFile = file;
                break;
            }
        }

        this.addChild(lvDBSelection);

        scrollbar = new Scrollbar(lvDBSelection);
        addChild(scrollbar);

        lvDBSelection.addListPosChangedEventHandler(() -> scrollbar.ScrollPositionChanged());

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
        bNew.addClickHandler((v, x, y, pointer, button) -> {
            stopTimer();
            NewDB_InputBox.Show(WrapType.SINGLELINE, Translation.get("NewDB"), Translation.get("InsNewDBName"), "NewDB", mDialogListenerNewDB);
            return true;
        });

        // Select Button
        bSelect.addClickHandler((v, x, y, pointer, button) -> {
            stopTimer();
            if (AktFile == null) {
                GL.that.Toast("Please select Database!", Toast.LENGTH_SHORT);
                return false;
            }
            selectDB();
            return true;
        });

        // Cancel Button
        bCancel.addClickHandler((v, x, y, pointer, button) -> {
            stopTimer();
            if (MustSelect) {
                Action_ShowQuit.getInstance().Execute();
            } else {
                finish();
            }

            return true;
        });

        // AutoStart Button
        bAutostart.addClickHandler((v, x, y, pointer, button) -> {
            stopTimer();
            showSelectionMenu();
            return true;
        });

        bNew.setText(Translation.get("NewDB"));
        bSelect.setText(Translation.get("confirm"));
        bCancel.setText(Translation.get("cancel"));
        autoStartTime = Config.MultiDBAutoStartTime.getValue();
        autoStartCounter = 0;
        if (autoStartTime > 0) {
            autoStartCounter = autoStartTime;
            bAutostart.setText(autoStartCounter + " " + Translation.get("confirm"));
            if ((autoStartTime > 0) && (AktFile != null)) {
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

        int itemSpace = lvDBSelection.getMaxItemCount();

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
                    File file = dbFiles.get(i);

                    try {
                        if (file.getAbsoluteFile().compareTo(AktFile.getAbsoluteFile()) == 0) {
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

                resetInitial();
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
            for (File file : dbFiles) {
                if (file.getAbsoluteFile().compareTo(AktFile.getAbsoluteFile()) == 0) {
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
        if (AktFile == null) {
            GL.that.Toast("no DB selected", 200);
            return;
        }

        Config.MultiDBAutoStartTime.setValue(autoStartTime);
        Config.MultiDBAsk.setValue(autoStartTime >= 0);

        Config.DatabaseName.setValue(AktFile.getName());
        Config.AcceptChanges();

        LayerManager.getInstance().initLayers();

        finish();
        if (returnListener != null)
            returnListener.back();

    }

    @Override
    protected void finish() {
        GL.that.closeActivity(!MustSelect);
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
        AktFile = null;

        returnListener = null;
        super.dispose();
    }

    public interface IReturnListener {
        void back();
    }

    private class DBItemAdapter implements Adapter {

        private final CB_RectF recItem;
        private V_ListView lvDBSelection;
        private FileList dbFiles;

        public DBItemAdapter(V_ListView lvDBSelection, FileList dbFiles) {
            // params are only for demonstration of dependencies. could also be in SelectDB
            this.lvDBSelection = lvDBSelection;
            this.dbFiles = dbFiles;
            recItem = new CB_RectF(0, 0, lvDBSelection.getInnerWidth(), UiSizes.getInstance().getButtonHeight() * 1.2f);
            lvDBSelection.setBaseAdapter(this);
        }

        @Override
        public int getCount() {
            return this.dbFiles.size();
        }

        @Override
        public ListViewItemBase getView(int position) {
            DBItem v = new DBItem(recItem, position, this.dbFiles.get(position));
            v.addClickHandler((v1, x, y, pointer, button) -> {
                stopTimer();
                DBItem selectedItem = (DBItem) v1;
                AktFile = selectedItem.file;
                this.lvDBSelection.setSelection(selectedItem.getIndex());
                return true;
            });
            return v;
        }

        @Override
        public float getItemSize(int position) {
            return recItem.getHeight();
        }

    }

    private class DBItem extends ListViewItemBackground {

        File file;

        DBItem(CB_RectF rec, int index, File file) {
            super(rec, index, file.getName());
            this.file = file;

            float left = 20;
            float mLabelHeight = getHeight() * 0.7f;
            float mLabelYPos = (getHeight() - mLabelHeight) / 2;

            CB_Label lblName = new CB_Label(name + " lblName", left, mLabelYPos, getWidth(), mLabelHeight);
            lblName.setFont(Fonts.getBig());
            lblName.setVAlignment(CB_Label.VAlignment.TOP);
            lblName.setText(file.getName());
            addChild(lblName);

            CB_Label lblInfo = new CB_Label(name + " lblInfo", left, mLabelYPos, getWidth(), mLabelHeight);
            lblInfo.setFont(Fonts.getBubbleNormal());
            lblInfo.setVAlignment(CB_Label.VAlignment.BOTTOM);

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            lblInfo.setText(Database.Data.getCacheCountInDB(file.getAbsolutePath())
                    + " Caches  "
                    + file.length() / (1024 * 1024) + "MB"
                    + "    last use "
                    + sdf.format(file.lastModified()));
            addChild(lblInfo);

            setClickable(true);
        }

    }

}
