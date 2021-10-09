package de.droidcachebox.menu.menuBtn1.contextmenus;

import com.badlogic.gdx.graphics.g2d.Sprite;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.AbstractShowAction;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.WrapType;
import de.droidcachebox.core.GpxSerializer;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.Sprites.IconName;
import de.droidcachebox.gdx.activities.Import;
import de.droidcachebox.gdx.activities.ImportGCPosition;
import de.droidcachebox.gdx.activities.Import_GSAK;
import de.droidcachebox.gdx.activities.SearchOverNameOwnerGcCode;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.dialogs.ProgressDialog;
import de.droidcachebox.gdx.controls.dialogs.StringInputBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.gdx.main.Menu;
import de.droidcachebox.menu.ViewManager;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.RunnableReadyHandler;
import de.droidcachebox.utils.log.Log;

public class ShowImportMenu extends AbstractShowAction {
    public static final int MI_IMPORT_CBS = 189;
    public static final int MI_IMPORT_GCV = 192;
    private static ShowImportMenu that;
    private int actExportedCount = 0;
    private ProgressDialog pD;
    private boolean cancel = false;

    private ShowImportMenu() {
        super("ImportMenu");
    }

    public static ShowImportMenu getInstance() {
        if (that == null) that = new ShowImportMenu();
        return that;
    }

    private void checkStateOfGeoCache() {
        GL.that.postAsync(() -> {
            Log.debug("ImportMenuTitle", "chkAPiLogInWithWaitDialog");
            GlobalCore.chkAPiLogInWithWaitDialog(isAccessTokenInvalid -> {
                Log.debug("checkReady", "isAccessTokenInvalid: " + isAccessTokenInvalid);
                if (!isAccessTokenInvalid) {
                    TimerTask tt = new TimerTask() {
                        @Override
                        public void run() {
                            GL.that.postAsync(() -> new UpdateCachesState().execute());
                        }
                    };
                    Timer t = new Timer();
                    t.schedule(tt, 100);
                }
            });
        });
    }

    @Override
    public void execute() {
        getContextMenu().show();
    }

    @Override
    public CB_View_Base getView() {
        // don't return a view.
        // show menu direct.
        GL.that.RunOnGL(this::execute);
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
        icm.addMenuItem("chkState", null, this::checkStateOfGeoCache);
        icm.addMenuItem("moreImport", null, () -> GL.that.postAsync(() -> new Import().show()));
        icm.addMenuItem("importCachesOverPosition", null, () -> new ImportGCPosition().show());
        icm.addMenuItem("API_IMPORT_NAME_OWNER_CODE", null, SearchOverNameOwnerGcCode::showInstance);
        icm.addMenuItem("GCVoteRatings", null, () -> new Import(MI_IMPORT_GCV).show());
        icm.addMenuItem("GSAKMenuImport", null, () -> new Import_GSAK().show());
        icm.addDivider();
        icm.addMenuItem("GPX_EXPORT", null, this::exportGPX);
        return icm;
    }

    private void exportGetFolderStep(final String fileName) {
        new FileOrFolderPicker(FileIO.getDirectoryName(Settings.gpxExportFileName.getValue()),
                Translation.get("selectExportFolder"),
                Translation.get("select"),
                abstractFile -> GL.that.RunOnGL(() -> ShowImportMenu.this.outputFile(fileName, abstractFile))).show();
    }

    private void outputFile(final String fileName, AbstractFile abstractFile) {

        String exportPath = abstractFile.getAbsolutePath() + "/" + fileName;
        PlatformUIBase.addToMediaScannerList(exportPath);

        AbstractFile exportAbstractFile = FileFactory.createFile(exportPath);
        Settings.gpxExportFileName.setValue(exportAbstractFile.getPath());
        ViewManager.that.acceptChanges();

        // Delete File if exist
        if (exportAbstractFile.exists())
            try {
                exportAbstractFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
            }

        // Export all Caches from DB
        final ArrayList<String> allGeocodesForExport = CBDB.getInstance().cacheList.getGcCodes();

        final int count = allGeocodesForExport.size();
        actExportedCount = 0;
        // Show with Progress

        final GpxSerializer ser = new GpxSerializer();
        try {
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(exportAbstractFile.getFileOutputStream(), StandardCharsets.UTF_8));

            pD = ProgressDialog.Show("export", new RunnableReadyHandler() {

                @Override
                public void run() {
                    try {
                        ser.writeGPX(allGeocodesForExport, writer, (countExported, msg) -> {
                            actExportedCount = countExported;
                            if (pD != null) {
                                int progress = (countExported * 100) / count;
                                pD.setProgress("Export: " + countExported + "/" + count, msg, progress);
                                if (pD.isCanceld())
                                    ser.cancel();
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
                public void runnableIsReady(boolean canceled) {
                    System.out.print("Export READY");
                    if (pD != null) {
                        pD.close();
                        pD.dispose();
                        pD = null;
                    }

                    if (canceled) {
                        MsgBox.show(Translation.get("exportedCanceld", String.valueOf(actExportedCount), String.valueOf(count)), Translation.get("export"), MsgBoxIcon.Stop);
                    } else {
                        MsgBox.show(Translation.get("exported", String.valueOf(actExportedCount)), Translation.get("export"), MsgBoxIcon.Information);
                    }

                }
            });

            pD.setCancelListener(() -> {
                cancel = true;
                if (pD.isCanceld())
                    ser.cancel();
            });
        } catch (IOException ignored) {
        }
    }

    private void exportGPX() {
        StringInputBox.show(WrapType.SINGLELINE,
                Translation.get("enterFileName"),
                Translation.get("GPX_EXPORT"),
                FileIO.getFileName(Settings.gpxExportFileName.getValue()),
                (which, data) -> {
                    if (which == 1) {
                        final String FileName = StringInputBox.editText.getText();
                        GL.that.RunOnGL(() -> exportGetFolderStep(FileName));
                    }
                    return true;
                });
    }
}
