package de.droidcachebox.gdx.main;

import com.badlogic.gdx.graphics.g2d.Sprite;
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.GpxSerializer;
import de.droidcachebox.core.GpxSerializer.ProgressListener;
import de.droidcachebox.database.Database;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.*;
import de.droidcachebox.gdx.controls.dialogs.ProgressDialog;
import de.droidcachebox.gdx.controls.dialogs.ProgressDialog.ICancelListener;
import de.droidcachebox.gdx.controls.dialogs.StringInputBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.File;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.RunnableReadyHandler;
import de.droidcachebox.utils.log.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Abstract_ShowImportMenu extends AbstractShowAction {
    public static final int MI_IMPORT_CBS = 189;
    public static final int MI_IMPORT_GCV = 192;
    private static Abstract_ShowImportMenu that;
    int actExportedCount = 0;
    private ProgressDialog pD;
    private boolean cancel = false;

    private Abstract_ShowImportMenu() {
        super("ImportMenu", MenuID.AID_SHOW_IMPORT_MENU);
    }

    public static Abstract_ShowImportMenu getInstance() {
        if (that == null) that = new Abstract_ShowImportMenu();
        return that;
    }

    @Override
    public void Execute() {
        getContextMenu().show();
    }

    @Override
    public CB_View_Base getView() {
        // don't return a view.
        // show menu direct.
        GL.that.RunOnGL(() -> Execute());

        return null;
    }

    @Override
    public boolean getEnabled() {
        return true;
    }

    @Override
    public Sprite getIcon() {
        return Sprites.getSprite(IconName.cacheListIcon.name());
    }

    @Override
    public boolean hasContextMenu() {
        return true;
    }

    @Override
    public Menu getContextMenu() {
        Menu icm = new Menu("ImportMenuTitle");
        icm.addMenuItem("chkState", null, () -> GL.that.postAsync(() -> {
            // Sprites.getSprite(IconName.dayGcLiveIcon.name())
            // First check API-Key with visual Feedback
            Log.debug("ImportMenuTitle", "chkAPiLogInWithWaitDialog");
            GlobalCore.chkAPiLogInWithWaitDialog(isAccessTokenInvalid -> {
                Log.debug("checkReady", "isAccessTokenInvalid: " + isAccessTokenInvalid);
                if (!isAccessTokenInvalid) {
                    TimerTask tt = new TimerTask() {
                        @Override
                        public void run() {
                            GL.that.postAsync(() -> new Action_chkState().Execute());
                        }
                    };
                    Timer t = new Timer();
                    t.schedule(tt, 100);
                }
            });
        }));
        icm.addMenuItem("moreImport", null, () -> GL.that.postAsync(() -> new Import().show()));
        icm.addMenuItem("importCachesOverPosition", null, () -> new ImportGCPosition().show());
        icm.addMenuItem("API_IMPORT_NAME_OWNER_CODE", null, SearchOverNameOwnerGcCode::ShowInstanz);
        icm.addMenuItem("GCVoteRatings", null, () -> new Import(MI_IMPORT_GCV).show());
        icm.addMenuItem("GSAKMenuImport", null, () -> new Import_GSAK().show());
        icm.addDivider();
        icm.addMenuItem("GPX_EXPORT", null, () -> {
            StringInputBox.Show(WrapType.SINGLELINE,
                    Translation.get("enterFileName"),
                    Translation.get("GPX_EXPORT"),
                    FileIO.getFileName(Config.gpxExportFileName.getValue()),
                    (which, data) -> {
                        if (which == 1) {
                            final String FileName = StringInputBox.editText.getText();
                            GL.that.RunOnGL(() -> ExportgetFolderStep(FileName));
                        }
                        return true;
                    });
        });
        if (Config.CBS_IP.getValue().length() > 0)
            icm.addMenuItem("ToCBServer", null, () -> new Import_CBServer().show());
        return icm;
    }

    private void ExportgetFolderStep(final String FileName) {
        PlatformUIBase.getFolder(FileIO.getDirectoryName(Config.gpxExportFileName.getValue()),
                Translation.get("selectExportFolder"),
                Translation.get("select"),
                Path -> GL.that.RunOnGL(() -> ausgebenDatei(FileName, Path)));
    }

    private void ausgebenDatei(final String FileName, String Path) {
        String exportPath = Path + "/" + FileName;
        PlatformUIBase.addToMediaScannerList(exportPath);
        File exportFile = FileFactory.createFile(exportPath);
        Config.gpxExportFileName.setValue(exportFile.getPath());
        Config.AcceptChanges();

        // Delete File if exist
        if (exportFile.exists())
            try {
                exportFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }

        // Export all Caches from DB
        final ArrayList<String> allGeocodesForExport = Database.Data.cacheList.getGcCodes();

        final int count = allGeocodesForExport.size();
        actExportedCount = 0;
        // Show with Progress

        final GpxSerializer ser = new GpxSerializer();
        try {
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(exportFile.getFileOutputStream(), "UTF-8"));

            pD = ProgressDialog.Show("export", new RunnableReadyHandler() {

                @Override
                public void run() {
                    try {
                        ser.writeGPX(allGeocodesForExport, writer, new ProgressListener() {
                            @Override
                            public void publishProgress(int countExported, String msg) {
                                actExportedCount = countExported;
                                if (pD != null) {
                                    int progress = (countExported * 100) / count;
                                    pD.setProgress("Export: " + countExported + "/" + count, msg, progress);
                                    if (pD.isCanceld())
                                        ser.cancel();
                                }
                            }
                        });
                    } catch (IOException ignored) {
                    }
                }

                @Override
                public boolean doCancel() {
                    return cancel;
                }

                @Override
                public void runnableIsReady(boolean canceld) {
                    System.out.print("Export READY");
                    if (pD != null) {
                        pD.close();
                        pD.dispose();
                        pD = null;
                    }

                    if (canceld) {
                        MessageBox.show(Translation.get("exportedCanceld", String.valueOf(actExportedCount), String.valueOf(count)), Translation.get("export"), MessageBoxIcon.Stop);
                    } else {
                        MessageBox.show(Translation.get("exported", String.valueOf(actExportedCount)), Translation.get("export"), MessageBoxIcon.Information);
                    }

                }
            });

            pD.setCancelListener(new ICancelListener() {

                @Override
                public void isCanceled() {
                    cancel = true;
                    if (pD.isCanceld())
                        ser.cancel();
                }
            });
        } catch (IOException e) {

        }
    }

}
