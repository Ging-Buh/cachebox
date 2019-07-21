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
package CB_UI.GL_UI.Activitys;

import CB_Core.CoreSettingsForward;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.Types.CacheListDAO;
import CB_Core.Types.Categories;
import CB_Locator.Map.ManagerBase;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Controls.Dialogs.NewDB_InputBox;
import CB_UI.GlobalCore;
import CB_UI_Base.Enums.WrapType;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.CB_Button;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.Dialogs.Toast;
import CB_UI_Base.GL_UI.Controls.List.*;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.Main.Actions.Action_ShowQuit;
import CB_UI_Base.GL_UI.Menu.Menu;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_UI_Base.Math.UiSizes;
import CB_Utils.Log.Log;
import CB_Utils.Math.Point;
import CB_Utils.Util.FileIO;
import CB_Utils.Util.FileList;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author ging-buh
 * @author Longri
 */
public class SelectDB extends ActivityBase {
    private static final String log = "SelectDB";
    private Timer updateTimer;
    private int autoStartTime;
    private int autoStartCounter = 0;
    private CB_Button bNew;
    private CB_Button bSelect;
    private CB_Button bCancel;
    private CB_Button bAutostart;
    private V_ListView lvDBFile;
    private Scrollbar scrollbar;
    private CustomAdapter lvAdapter;
    private File AktFile = null;
    private String[] fileInfos;
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
                Database.Data.StartUp(database);

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
                Database.Drafts.StartUp(Config.mWorkPath + "/User/FieldNotes.db3");

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
        String DBFile = Config.DatabaseName.getValue();
		/*
		if (DBFile.length() == 0)
			FileIO.GetDirectoryName(Config.DatabasePath.getValue());
		*/
        final FileList files = new FileList(Config.mWorkPath, "DB3", true);
        fileInfos = new String[files.size()];
        int index = 0;
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(DBFile))
                AktFile = file;
            fileInfos[index] = "";
            index++;
        }

        lvDBFile = new V_ListView(new CB_RectF(leftBorder, this.getBottomHeight() + UI_Size_Base.that.getButtonHeight() * 2, innerWidth, getHeight() - (UI_Size_Base.that.getButtonHeight() * 2) - this.getTopHeight() - this.getBottomHeight()),
                "DB File ListView");
        lvAdapter = new CustomAdapter(files);
        lvDBFile.setBaseAdapter(lvAdapter);
        this.addChild(lvDBFile);

        this.scrollbar = new Scrollbar(lvDBFile);
        this.addChild(this.scrollbar);

        this.lvDBFile.addListPosChangedEventHandler(() -> scrollbar.ScrollPositionChanged());

        float btWidth = innerWidth / 3;

        bNew = new CB_Button(new CB_RectF(leftBorder, this.getBottomHeight(), btWidth, UI_Size_Base.that.getButtonHeight()), "selectDB.bNew");
        bSelect = new CB_Button(new CB_RectF(bNew.getMaxX(), this.getBottomHeight(), btWidth, UI_Size_Base.that.getButtonHeight()), "selectDB.bSelect");
        bCancel = new CB_Button(new CB_RectF(bSelect.getMaxX(), this.getBottomHeight(), btWidth, UI_Size_Base.that.getButtonHeight()), "selectDB.bCancel");
        bAutostart = new CB_Button(new CB_RectF(leftBorder, bNew.getMaxY(), innerWidth, UI_Size_Base.that.getButtonHeight()), "selectDB.bAutostart");

        this.addChild(bSelect);
        this.addChild(bNew);
        this.addChild(bCancel);
        this.addChild(bAutostart);

        // New Button
        bNew.setOnClickListener((v, x, y, pointer, button) -> {
            stopTimer();
            NewDB_InputBox.Show(WrapType.SINGLELINE, Translation.get("NewDB"), Translation.get("InsNewDBName"), "NewDB", mDialogListenerNewDB);
            return true;
        });

        // Select Button
        bSelect.setOnClickListener((v, x, y, pointer, button) -> {
            stopTimer();
            if (AktFile == null) {
                GL.that.Toast("Please select Database!", Toast.LENGTH_SHORT);
                return false;
            }
            selectDB();
            return true;
        });

        // Cancel Button
        bCancel.setOnClickListener((v, x, y, pointer, button) -> {
            stopTimer();
            if (MustSelect) {
                Action_ShowQuit.getInstance().Execute();
            } else {
                finish();
            }

            return true;
        });

        // AutoStart Button
        bAutostart.setOnClickListener((v, x, y, pointer, button) -> {
            stopTimer();
            showSelectionMenu();
            return true;
        });

        // Translations
        bNew.setText(Translation.get("NewDB"));
        bSelect.setText(Translation.get("confirm"));
        bCancel.setText(Translation.get("cancel"));

        autoStartTime = Config.MultiDBAutoStartTime.getValue();
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

        this.isClickable();

        readCountatThread();
    }

    private void readCountatThread() {
        new Thread(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            int index = 0;
            if (lvAdapter == null) return;// maybe is disposed
            for (File file : lvAdapter.files) {
                if (fileInfos == null) return; // maybe is disposed
                String lastModified = sdf.format(file.lastModified());
                String fileSize = file.length() / (1024 * 1024) + "MB";
                fileInfos[index] = Database.Data.getCacheCountInDB(file.getAbsolutePath()) + " Caches  " + fileSize + "    last use " + lastModified;
                index++;
            }
            if (lvDBFile != null) lvDBFile.setBaseAdapter(lvAdapter);
        }).start();
    }

    @Override
    public void onShow() {

        int itemSpace = lvDBFile.getMaxItemCount();

        if (itemSpace >= lvAdapter.getCount()) {
            lvDBFile.setUnDraggable();
        } else {
            lvDBFile.setDraggable();
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {

                // Set selected item
                for (int i = 0; i < lvAdapter.getCount(); i++) {
                    File file = lvAdapter.getItem(i);

                    try {
                        if (file.getAbsoluteFile().compareTo(AktFile.getAbsoluteFile()) == 0) {
                            lvDBFile.setSelection(i);
                        }

                        Point firstAndLast = lvDBFile.getFirstAndLastVisibleIndex();

                        if (!(firstAndLast.x < i && firstAndLast.y > i))
                            lvDBFile.scrollToItem(i);
                    } catch (Exception e) {
                        Log.err(log, "select item", e);
                    }
                }

                GL.that.RunOnGL(() -> setSelectedItemVisible());

                resetInitial();
                lvDBFile.chkSlideBack();
            }
        };

        Timer timer = new Timer();
        timer.schedule(task, 350);

        GL.that.renderOnce();
    }

    private void setSelectedItemVisible() {
        int id = 0;
        Point firstAndLast = lvDBFile.getFirstAndLastVisibleIndex();

        try {
            for (File file : lvAdapter.getFileList()) {
                if (file.getAbsoluteFile().compareTo(AktFile.getAbsoluteFile()) == 0) {
                    lvDBFile.setSelection(id);
                    if (lvDBFile.isDraggable()) {
                        if (!(firstAndLast.x <= id && firstAndLast.y >= id)) {
                            lvDBFile.scrollToItem(id);
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
                        if (lvDBFile != null) {
                            lvDBFile.chkSlideBack();
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

        ManagerBase.Manager.initMapPacks();

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
        if (lvDBFile != null)
            lvDBFile.dispose();
        lvDBFile = null;
        if (scrollbar != null)
            scrollbar.dispose();
        scrollbar = null;

        lvAdapter = null;
        AktFile = null;
        fileInfos = null;

        returnListener = null;
        super.dispose();
    }

    public interface IReturnListener {
        void back();
    }

    private class CustomAdapter implements Adapter {

        private final CB_RectF recItem;
        private FileList files;

        public CustomAdapter(FileList files) {
            this.files = files;
            recItem = UiSizes.that.getCacheListItemRec().asFloat();
            recItem.setHeight(recItem.getHeight() * 0.8f);
            recItem.setWidth(getWidth() - getLeftWidth() - getRightWidth() - (margin * 1.5f));
        }

        public void setFiles(FileList files) {
            this.files = files;
        }

        FileList getFileList() {
            return files;
        }

        @Override
        public int getCount() {
            return files.size();
        }

        File getItem(int position) {
            return files.get(position);
        }

        @Override
        public ListViewItemBase getView(int position) {
            SelectDBItem v = new SelectDBItem(recItem, position, files.get(position), fileInfos[position]);
            v.setOnClickListener((v1, x, y, pointer, button) -> {
                stopTimer();
                for (int i = 0; i < files.size(); i++) {
                    if (v1.getName().equals(files.get(i).getName())) {
                        AktFile = files.get(i);
                        lvDBFile.setSelection(i);
                        break;
                    }
                }
                return true;
            });
            return v;
        }

        @Override
        public float getItemSize(int position) {
            return recItem.getHeight();
        }

    }

    private class SelectDBItem extends ListViewItemBackground {

        float left = 20;
        float mLabelHeight = -1;
        float mLabelYPos = -1;
        float mLabelWidth = -1;
        CB_Label nameLabel;
        CB_Label countLabel;

        public SelectDBItem(CB_RectF rec, int Index, File file, String count) {
            super(rec, Index, file.getName());

            if (mLabelHeight == -1) {
                mLabelHeight = getHeight() * 0.7f;
                mLabelYPos = (getHeight() - mLabelHeight) / 2;
                mLabelWidth = getWidth() - (left * 2);
            }

            nameLabel = new CB_Label(this.name + " nameLabel", left, mLabelYPos, getWidth(), mLabelHeight);
            nameLabel.setFont(Fonts.getBig());
            nameLabel.setVAlignment(CB_Label.VAlignment.TOP);
            nameLabel.setText(file.getName());
            this.addChild(nameLabel);

            countLabel = new CB_Label(this.name + " countLabel", left, mLabelYPos, getWidth(), mLabelHeight);
            countLabel.setFont(Fonts.getBubbleNormal());
            countLabel.setVAlignment(CB_Label.VAlignment.BOTTOM);
            countLabel.setText(count);
            this.addChild(countLabel);

            this.setClickable(true);
        }

    }

}
