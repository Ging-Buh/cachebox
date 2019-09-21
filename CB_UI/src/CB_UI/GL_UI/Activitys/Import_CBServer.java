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

import CB_Core.DAO.WaypointDAO;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.Import.BreakawayImportThread;
import CB_Core.Import.ImporterProgress;
import CB_Core.Types.ExportEntry;
import CB_Core.Types.ExportList;
import CB_Core.Types.Waypoint;
import CB_RpcCore.ClientCB.RpcClientCB;
import CB_RpcCore.Functions.RpcAnswer_ExportChangesToServer;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.GL_UI.Activitys.APIs.ExportCBServerListItem;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Activitys.ImportAnimation.AnimationType;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.CB_Button;
import CB_UI_Base.GL_UI.Controls.CB_Label;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.ProgressBar;
import CB_UI_Base.GL_UI.Controls.ScrollBox;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Events.ProgressChangedEvent;
import CB_Utils.Events.ProgresssChangedEventList;
import CB_Utils.Log.Log;
import cb_rpc.Functions.RpcAnswer;
import de.cb.sqlite.CoreCursor;

import java.util.Timer;
import java.util.TimerTask;

public class Import_CBServer extends ActivityBase implements ProgressChangedEvent {
    private static final String log = "Import_CBServer";
    private final ScrollBox scrollBox;
    private V_ListView lvExport;
    private CB_Button bOK;
    private CB_Button refreshExportList;
    private CB_Label lblProgressMsg;
    private ProgressBar pgBar;
    private Timer mAnimationTimer;
    private int animationValue = 0;
    private Boolean importStarted = false;
    private ExportList exportList;
    private CB_RectF itemRecCBServer;
    private float itemHeight = -1;
    private ImportAnimation dis;
    private volatile BreakawayImportThread importThread;

    public Import_CBServer() {
        super(ActivityRec(), "importActivity");
        scrollBox = new ScrollBox(ActivityRec());
        scrollBox.setBackground(this.getBackground());
        createOkCancelBtn();
        createTitleLine();
        createExportCollapseBox();
        initialForm();

        Layout();

        refreshExportList();

        Log.info(log, "Export_CBServer created.");
    }

    @Override
    public void onShow() {
        ProgresssChangedEventList.Add(this);
    }

    @Override
    public void onHide() {
        ProgresssChangedEventList.Remove(this);
    }

    private void createOkCancelBtn() {
        bOK = new CB_Button(0, 0, innerWidth / 2, UI_Size_Base.ui_size_base.getButtonHeight(), "OK Button");
        CB_Button bCancel = new CB_Button(0, 0, innerWidth / 2, UI_Size_Base.ui_size_base.getButtonHeight(), "Cancel Button");

        this.initRow(BOTTOMUP);
        // Translations
        bOK.setText(Translation.get("export"));
        bCancel.setText(Translation.get("cancel"));

        this.addNext(bOK);
        bOK.addClickHandler((v, x, y, pointer, button) -> {
            ImportNow();
            return true;
        });

        this.addLast(bCancel);
        bCancel.addClickHandler((v, x, y, pointer, button) -> {
            if (BreakawayImportThread.isCanceled()) {
                BreakawayImportThread.reset();
                finish();
                return true;
            }

            if (importStarted) {
                MessageBox.show(Translation.get("WantCancelImport"), Translation.get("CancelImport"), MessageBoxButtons.YesNo, MessageBoxIcon.Stop, (which, data) -> {
                    if (which == MessageBox.BUTTON_POSITIVE) {
                        cancelImport();
                    }
                    return true;
                });
            } else
                finish();
            return true;
        });

        refreshExportList = new CB_Button(name);
        refreshExportList.setText(Translation.get("refreshExportList"));
        refreshExportList.addClickHandler((v, x, y, pointer, button) -> {
            refreshExportList();
            return true;
        });
        this.addLast(refreshExportList);

    }

    private void createTitleLine() {
        CB_Label lblTitle = new CB_Label(Translation.get("export"), Fonts.getBig(), null, null);

        float lineHeight = UI_Size_Base.ui_size_base.getButtonHeight() * 0.75f;
        CB_RectF rec = new CB_RectF(0, 0, 0, lineHeight);
        pgBar = new ProgressBar(rec, "ProgressBar");
        pgBar.setProgress(0, "");

        float SmallLineHeight = Fonts.MeasureSmall("Tg").height;
        lblProgressMsg = new CB_Label("", Fonts.getSmall(), null, null);
        lblProgressMsg.setHeight(SmallLineHeight);

        this.initRow(TOPDOWN);
        this.addLast(lblTitle);
        this.addLast(pgBar);
        this.addLast(lblProgressMsg);

    }

    private void createExportCollapseBox() {

        scrollBox.setHeight(this.getAvailableHeight());
        this.addLast(scrollBox);
        scrollBox.initRow(TOPDOWN);
        lvExport = new V_ListView(new CB_RectF(0, 0, scrollBox.getWidth(), scrollBox.getHeight()), "");
        scrollBox.addLast(lvExport);
        lvExport.setEmptyMsg(Translation.get("EmptyExportList"));

    }

    private void Layout() {
        scrollBox.setVirtualHeight(scrollBox.getHeight());//
    }

    private void initialForm() {

    }

    private void refreshExportList() {

        lvExport.setBaseAdapter(null);
        lvExport.notifyDataSetChanged();
        refreshExportList.disable();

        new Thread(() -> {
            exportList = new ExportList();
            exportList.loadExportList();

            lvExport.setBaseAdapter(new CustomAdapterExportCBServer());
            lvExport.notifyDataSetChanged();

            stopTimer();
            lvExport.setEmptyMsg(Translation.get("EmptyExportCBServerList"));

            refreshExportList.enable();
        }).start();

        mAnimationTimer = new Timer();
        long ANIMATION_TICK = 450;
        mAnimationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

            private void TimerMethod() {
                animationValue++;

                if (animationValue > 5)
                    animationValue = 0;

                StringBuilder s = new StringBuilder();
                for (int i = 0; i < animationValue; i++) {
                    s.append(".");
                }

                lvExport.setEmptyMsg(Translation.get("LoadExportCBServerList") + s);

            }

        }, 0, ANIMATION_TICK);

    }

    private void stopTimer() {
        if (mAnimationTimer != null) {
            mAnimationTimer.cancel();
            mAnimationTimer = null;
        }
    }

    private void ImportNow() {
        // disable btn
        bOK.disable();

        // disable UI
        dis = new ImportAnimation(scrollBox);
        dis.setBackground(getBackground());

        this.addChild(dis, false);

        ImportThread();

    }

    private void ImportThread() {
        importThread = new BreakawayImportThread() {
            @Override
            public void run() {
                importStarted = true;

                ImporterProgress progress = new ImporterProgress();

                try {

                    dis.setAnimationType(AnimationType.Work);
                    progress.setJobMax("exportCBServer", 1);
                    progress.ProgressChangeMsg("Export CBServer", "");
                    runExport();
                    progress.ProgressInkrement("exportCBServer", "", true);

                    Thread.sleep(1000);


                } catch (InterruptedException e) {
                    cancelImport();
                    FilterProperties props = FilterInstances.getLastFilter();
                    EditFilterSettings.ApplyFilter(props);
                    progress.ProgressChangeMsg("", "");
                    return;
                }

                if (BreakawayImportThread.isCanceled()) {
                    FilterProperties props = FilterInstances.getLastFilter();
                    EditFilterSettings.ApplyFilter(props);
                    progress.ProgressChangeMsg("", "");
                    return;
                }

                finish();

            }
        };

        importThread.setPriority(Thread.MAX_PRIORITY);
        importThread.start();
    }

    private void cancelImport() {
        if (importThread != null) {
            importThread.cancel();
            importThread = null;
        }

        importStarted = false;

        if (dis != null) {
            this.removeChildsDirekt(dis);
            dis.dispose();
            dis = null;
        }
        bOK.enable();

        if (importThread != null && !importThread.isAlive()) {
            this.finish();
        }
    }

    @Override
    public void ProgressChangedEventCalled(final String Message, final String ProgressMessage, final int Progress) {

        GL.that.RunOnGL(() -> {
            pgBar.setProgress(Progress);
            lblProgressMsg.setText(ProgressMessage);
            if (!Message.equals(""))
                pgBar.setText(Message);
        });

    }

    private void runExport() {
        ExportList toExport = new ExportList();
        // notwendige Informationen sammeln
        for (ExportEntry entry : exportList) {
            switch (entry.changeType) {
                case WaypointChanged:
                case NewWaypoint:
                    // Waypoint Informationen laden
                    entry.waypoint = readWaypoint(entry.wpGcCode);
                    break;
                case NotesText:
                    // Note Text laden
                    entry.note = Database.GetNote(entry.cacheId);
                    break;
                case SolverText:
                    // Solver Text laden
                    entry.solver = Database.GetSolver(entry.cacheId);
                    break;
                default:
                    break;
            }
            if (entry.toExport) {
                // nur die Einträge exportieren die markiert wurden
                toExport.add(entry);
            }
        }
        // Export zm CB_Server auführen
        RpcClientCB client = new RpcClientCB();
        RpcAnswer answer = client.ExportChangesToServer(toExport);
        if ((answer instanceof RpcAnswer_ExportChangesToServer)) {
            // Export ohne Fehler -> Replicationseinträge entfernen
            String sql = "delete from Replication";
            Database.Data.sql.execSQL(sql);
            // Liste neu laden
            exportList.loadExportList();
            lvExport.setBaseAdapter(new CustomAdapterExportCBServer());
            lvExport.notifyDataSetChanged();
        }
    }

    private Waypoint readWaypoint(String wpGcCode) {
        Waypoint result = null;
        CoreCursor reader = Database.Data.sql.rawQuery(WaypointDAO.SQL_WP_FULL + " where GcCode = ?", new String[]{wpGcCode});
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            WaypointDAO waypointDAO = new WaypointDAO();
            result = waypointDAO.getWaypoint(reader, true);
            reader.moveToNext();

        }
        reader.close();
        return result;
    }

    public class CustomAdapterExportCBServer implements Adapter {

        CustomAdapterExportCBServer() {
        }

        @Override
        public int getCount() {
            if (exportList != null) {
                return exportList.size();
            } else {
                return 0;
            }
        }

        @Override
        public ListViewItemBase getView(int position) {
            final ExportEntry it = exportList.get(position);
            if (itemRecCBServer == null) {
                itemHeight = UI_Size_Base.ui_size_base.getChkBoxSize().height + UI_Size_Base.ui_size_base.getChkBoxSize().halfHeight;
                float itemWidth = scrollBox.getInnerWidth();

                itemRecCBServer = new CB_RectF(new SizeF(itemWidth, itemHeight));
            }

            return new ExportCBServerListItem(itemRecCBServer, position, it);
        }

        @Override
        public float getItemSize(int position) {
            if (itemHeight == -1)
                itemHeight = UI_Size_Base.ui_size_base.getChkBoxSize().height + UI_Size_Base.ui_size_base.getChkBoxSize().halfHeight;
            return itemHeight;
        }

    }

}
