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

import de.droidcachebox.core.FilterInstances;
import de.droidcachebox.core.FilterProperties;
import de.droidcachebox.database.CoreCursor;
import de.droidcachebox.database.Database;
import de.droidcachebox.database.Waypoint;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.ex_import.BreakawayImportThread;
import de.droidcachebox.ex_import.ExportEntry;
import de.droidcachebox.ex_import.ExportList;
import de.droidcachebox.ex_import.ImporterProgress;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.ImportAnimation.AnimationType;
import de.droidcachebox.gdx.controls.list.Adapter;
import de.droidcachebox.gdx.controls.list.ListViewItemBackground;
import de.droidcachebox.gdx.controls.list.ListViewItemBase;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.SizeF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.rpc.RpcAnswer;
import de.droidcachebox.rpc.RpcAnswer_ExportChangesToServer;
import de.droidcachebox.rpc.RpcClientCB;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.ProgressChangedEvent;
import de.droidcachebox.utils.ProgresssChangedEventList;
import de.droidcachebox.utils.log.Log;

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
        super(activityRec(), "importActivity");
        scrollBox = new ScrollBox(activityRec());
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
        bOK = new CB_Button(0, 0, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "OK Button");
        CB_Button bCancel = new CB_Button(0, 0, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "Cancel Button");

        this.initRow(BOTTOMUP);
        // Translations
        bOK.setText(Translation.get("export"));
        bCancel.setText(Translation.get("cancel"));

        this.addNext(bOK);
        bOK.setClickHandler((v, x, y, pointer, button) -> {
            ImportNow();
            return true;
        });

        this.addLast(bCancel);
        bCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (BreakawayImportThread.isCanceled()) {
                BreakawayImportThread.reset();
                finish();
                return true;
            }

            if (importStarted) {
                MessageBox.show(Translation.get("WantCancelImport"), Translation.get("CancelImport"), MessageBoxButtons.YesNo, MessageBoxIcon.Stop,
                        (which, data) -> {
                            if (which == MessageBox.BTN_LEFT_POSITIVE) {
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
        refreshExportList.setClickHandler((v, x, y, pointer, button) -> {
            refreshExportList();
            return true;
        });
        this.addLast(refreshExportList);

    }

    private void createTitleLine() {
        CB_Label lblTitle = new CB_Label(Translation.get("export"), Fonts.getBig(), null, null);

        float lineHeight = UiSizes.getInstance().getButtonHeight() * 0.75f;
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
                    EditFilterSettings.applyFilter(props);
                    progress.ProgressChangeMsg("", "");
                    return;
                }

                if (BreakawayImportThread.isCanceled()) {
                    FilterProperties props = FilterInstances.getLastFilter();
                    EditFilterSettings.applyFilter(props);
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
                    entry.note = Database.getNote(entry.cacheId);
                    break;
                case SolverText:
                    // Solver Text laden
                    entry.solver = Database.getSolver(entry.cacheId);
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

    static class ExportCBServerListItem extends ListViewItemBackground {
        private CB_CheckBox chk;
        private CB_Label lblName, lblInfo;

        public ExportCBServerListItem(CB_RectF rec, int Index, final ExportEntry item) {
            super(rec, Index, "");

            lblName = new CB_Label(this.name + " lblName", getLeftWidth(), this.getHalfHeight(), this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());
            lblInfo = new CB_Label(this.name + " lblInfo", getLeftWidth(), 0, this.getWidth() - getLeftWidth() - getRightWidth(), this.getHalfHeight());

            lblName.setFont(Fonts.getNormal());
            lblInfo.setFont(Fonts.getSmall());

            lblName.setText(item.cacheName);

            // SimpleDateFormat postFormater = new SimpleDateFormat("dd.MM.yy");
            // String dateString = postFormater.format(pq.lastGenerated);
            // DecimalFormat df = new DecimalFormat("###.##");
            // String FileSize = df.format(pq.sizeMB) + " MB";
            String type = String.valueOf(item.changeType);
            lblInfo.setText(type);

            // lblInfo.setText("---");

            chk = new CB_CheckBox("");
            chk.setX(this.getWidth() - getRightWidth() - chk.getWidth() - UiSizes.getInstance().getMargin());
            chk.setY(this.getHalfHeight() - chk.getHalfHeight());
            chk.setChecked(item.toExport);
            chk.setOnCheckChangedListener((view, isChecked) -> item.setExport(isChecked));

            this.addChild(lblName);
            this.addChild(lblInfo);
            this.addChild(chk);
        }

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
                itemHeight = UiSizes.getInstance().getChkBoxSize().height + UiSizes.getInstance().getChkBoxSize().halfHeight;
                float itemWidth = scrollBox.getInnerWidth();

                itemRecCBServer = new CB_RectF(new SizeF(itemWidth, itemHeight));
            }

            return new ExportCBServerListItem(itemRecCBServer, position, it);
        }

        @Override
        public float getItemSize(int position) {
            if (itemHeight == -1)
                itemHeight = UiSizes.getInstance().getChkBoxSize().height + UiSizes.getInstance().getChkBoxSize().halfHeight;
            return itemHeight;
        }

    }

}
