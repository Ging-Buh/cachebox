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

import CB_Core.Api.GroundspeakAPI;
import CB_Core.CacheListChangedEventList;
import CB_Core.DAO.WaypointDAO;
import CB_Core.Database;
import CB_Core.FilterInstances;
import CB_Core.FilterProperties;
import CB_Core.Import.BreakawayImportThread;
import CB_Core.Import.ImportCBServer;
import CB_Core.Import.ImporterProgress;
import CB_Core.Types.ExportEntry;
import CB_Core.Types.ExportList;
import CB_Core.Types.Waypoint;
import CB_RpcCore.ClientCB.RpcClientCB;
import CB_RpcCore.Functions.RpcAnswer_ExportChangesToServer;
import CB_RpcCore.Functions.RpcAnswer_GetExportList;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Activitys.APIs.ExportCBServerListItem;
import CB_UI.GL_UI.Activitys.APIs.ImportAPIListItem;
import CB_UI.GL_UI.Activitys.FilterSettings.EditFilterSettings;
import CB_UI.GL_UI.Activitys.ImportAnimation.AnimationType;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI_Base.GL_UI.Activitys.ActivityBase;
import CB_UI_Base.GL_UI.Controls.*;
import CB_UI_Base.GL_UI.Controls.ChkBox.OnCheckChangedListener;
import CB_UI_Base.GL_UI.Controls.CollapseBox.IAnimatedHeightChangedListener;
import CB_UI_Base.GL_UI.Controls.Dialogs.NumericInputBox;
import CB_UI_Base.GL_UI.Controls.List.Adapter;
import CB_UI_Base.GL_UI.Controls.List.ListViewItemBase;
import CB_UI_Base.GL_UI.Controls.List.V_ListView;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox;
import CB_UI_Base.GL_UI.Controls.MessageBox.GL_MsgBox.OnMsgBoxClickListener;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.Controls.Spinner.ISelectionChangedListener;
import CB_UI_Base.GL_UI.Fonts;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_UI_Base.GL_UI.GL_View_Base;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.Math.CB_RectF;
import CB_UI_Base.Math.SizeF;
import CB_UI_Base.Math.UI_Size_Base;
import CB_Utils.Events.ProgressChangedEvent;
import CB_Utils.Events.ProgresssChangedEventList;
import CB_Utils.Log.Log;
import CB_Utils.StringH;
import CB_Utils.fileProvider.File;
import CB_Utils.fileProvider.FileFactory;
import cb_rpc.Functions.RpcAnswer;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.cb.sqlite.CoreCursor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Import_CBServer extends ActivityBase implements ProgressChangedEvent {
    private static final String log = "Import_CBServer";
    private final ScrollBox scrollBox;
    private V_ListView lvExport;
    private Button bOK;
    private Button refreshExportList;
    private Label lblProgressMsg;
    private ProgressBar pgBar;
    private Timer mAnimationTimer;
    private int animationValue = 0;
    private Boolean importStarted = false;
    private ExportList exportList;
    private CB_RectF itemRecCBServer;
    private float itemHeight = -1;
    private ImportAnimation dis;
    private volatile BreakawayImportThread importThread;
    private float CollapseBoxHeight;

    public Import_CBServer() {
        super(ActivityRec(), "importActivity");
        CollapseBoxHeight = UI_Size_Base.that.getButtonHeight() * 6;
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
        bOK = new Button(0, 0, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "OK Button");
        Button bCancel = new Button(0, 0, innerWidth / 2, UI_Size_Base.that.getButtonHeight(), "Cancel Button");

        this.initRow(BOTTOMUP);
        // Translations
        bOK.setText(Translation.Get("export"));
        bCancel.setText(Translation.Get("cancel"));

        this.addNext(bOK);
        bOK.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                ImportNow();
                return true;
            }
        });

        this.addLast(bCancel);
        bCancel.setOnClickListener(new OnClickListener() {
            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                if (BreakawayImportThread.isCanceled()) {
                    BreakawayImportThread.reset();
                    finish();
                    return true;
                }

                if (importStarted) {
                    GL_MsgBox.Show(Translation.Get("WantCancelImport"), Translation.Get("CancelImport"), MessageBoxButtons.YesNo, MessageBoxIcon.Stop, new OnMsgBoxClickListener() {

                        @Override
                        public boolean onClick(int which, Object data) {
                            if (which == GL_MsgBox.BUTTON_POSITIVE) {
                                cancelImport();
                            }
                            return true;
                        }
                    });
                } else
                    finish();
                return true;
            }
        });

        refreshExportList = new Button(name);
        refreshExportList.setText(Translation.Get("refreshExportList"));
        refreshExportList.setOnClickListener(new OnClickListener() {

            @Override
            public boolean onClick(GL_View_Base v, int x, int y, int pointer, int button) {
                refreshExportList();
                return true;
            }
        });
        this.addLast(refreshExportList);

    }

    private void createTitleLine() {
        Label lblTitle = new Label(Translation.Get("export"),Fonts.getBig(),null,null);

        float lineHeight = UI_Size_Base.that.getButtonHeight() * 0.75f;
        CB_RectF rec = new CB_RectF(0, 0, 0, lineHeight);
        pgBar = new ProgressBar(rec, "ProgressBar");
        pgBar.setProgress(0, "");

        float SmallLineHeight = Fonts.MeasureSmall("Tg").height;
        lblProgressMsg = new Label("", Fonts.getSmall(), null, null);
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
        lvExport.setEmptyMsg(Translation.Get("EmptyExportList"));

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

        Thread thread = new Thread() {
            @Override
            public void run() {
                exportList = new ExportList();
                exportList.loadExportList();

                lvExport.setBaseAdapter(new CustomAdapterExportCBServer());
                lvExport.notifyDataSetChanged();

                stopTimer();
                lvExport.setEmptyMsg(Translation.Get("EmptyExportCBServerList"));

                refreshExportList.enable();
            }

        };

        thread.start();

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

                String s = "";
                for (int i = 0; i < animationValue; i++) {
                    s += ".";
                }

                lvExport.setEmptyMsg(Translation.Get("LoadExportCBServerList") + s);

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

    public void ImportThread() {
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

        GL.that.RunOnGL(new IRunOnGL() {

            @Override
            public void run() {
                pgBar.setProgress(Progress);
                lblProgressMsg.setText(ProgressMessage);
                if (!Message.equals(""))
                    pgBar.setText(Message);
            }
        });

    }

    protected void runExport() {
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
        if ((answer != null) && (answer instanceof RpcAnswer_ExportChangesToServer)) {
            // Export ohne Fehler -> Replicationseinträge entfernen
            String sql = "delete from Replication";
            Database.Data.execSQL(sql);
            // Liste neu laden
            exportList.loadExportList();
            lvExport.setBaseAdapter(new CustomAdapterExportCBServer());
            lvExport.notifyDataSetChanged();
        } else {
            // Fehler beim Export
            // TODO
        }
    }

    private Waypoint readWaypoint(String wpGcCode) {
        Waypoint result = null;
        CoreCursor reader = Database.Data.rawQuery(WaypointDAO.SQL_WP_FULL + " where GcCode = ?", new String[]{wpGcCode});
        reader.moveToFirst();
        while (!reader.isAfterLast()) {
            WaypointDAO waypointDAO = new WaypointDAO();
            Waypoint wp = waypointDAO.getWaypoint(reader, true);
            result = wp;
            reader.moveToNext();

        }
        reader.close();
        return result;
    }

    public class CustomAdapterExportCBServer implements Adapter {

        public CustomAdapterExportCBServer() {
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
                itemHeight = UI_Size_Base.that.getChkBoxSize().height + UI_Size_Base.that.getChkBoxSize().halfHeight;
                float itemWidth = scrollBox.getInnerWidth();

                itemRecCBServer = new CB_RectF(new SizeF(itemWidth, itemHeight));
            }

            return new ExportCBServerListItem(itemRecCBServer, position, it);
        }

        @Override
        public float getItemSize(int position) {
            if (itemHeight == -1)
                itemHeight = UI_Size_Base.that.getChkBoxSize().height + UI_Size_Base.that.getChkBoxSize().halfHeight;
            return itemHeight;
        }

    }

}
