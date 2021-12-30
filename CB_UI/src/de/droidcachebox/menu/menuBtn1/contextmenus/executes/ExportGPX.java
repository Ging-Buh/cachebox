package de.droidcachebox.menu.menuBtn1.contextmenus.executes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.core.GpxSerializer;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.animation.WorkAnimation;
import de.droidcachebox.gdx.controls.dialogs.ProgressDialog;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.RunAndReady;
import de.droidcachebox.utils.log.Log;

public class ExportGPX {
    private static final String sClass = "ExportGPX";
    private final AtomicBoolean isCanceled;
    private int actExportedCount;
    private ProgressDialog progressDialog;

    public ExportGPX() {
        isCanceled = new AtomicBoolean();
    }

    public void exportGPX() {
        AbstractFile defaultFile = FileFactory.createFile(Settings.gpxExportFileName.getValue());
        if (!defaultFile.exists()) FileIO.createFile(Settings.gpxExportFileName.getValue());
        new FileOrFolderPicker(FileIO.getDirectoryName(Settings.gpxExportFileName.getValue()),
                ".gpx",
                Translation.get("enterFileName"), // selectExportFolder
                Translation.get("select"),
                gpxFile -> GL.that.RunOnGL(() -> outputFile(gpxFile))).show();
    }

    private void outputFile(AbstractFile exportFile) {
        actExportedCount = 0;
        isCanceled.set(false);

        Settings.gpxExportFileName.setValue(exportFile.getPath());
        Settings.getInstance().acceptChanges();

        // Delete File if exist
        if (exportFile.exists()) {
            try {
                exportFile.delete();
            } catch (Exception e) {
                Log.err(sClass, "Delete export gpx - file", e);
            }
        }
        // Export all Caches from DB
        final ArrayList<String> allGeocodes = CBDB.getInstance().cacheList.getGcCodes();
        final int numberOfGeoCachesToExport = allGeocodes.size();
        final GpxSerializer gpxSerializer = new GpxSerializer();
        try {
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(exportFile.getFileOutputStream(), StandardCharsets.UTF_8));
            RunAndReady runAndReady = new RunAndReady() {

                @Override
                public void run() {
                    try {
                        gpxSerializer.writeGPX(allGeocodes, writer,
                                (countExported, msg) -> {
                                    actExportedCount = countExported;
                                    progressDialog.setProgress("Export: " + countExported + "/" + numberOfGeoCachesToExport, msg, (countExported * 100) / numberOfGeoCachesToExport);
                                    if (isCanceled.get()) gpxSerializer.cancel();
                                });
                    } catch (IOException ignored) {
                    }
                }

                @Override
                public void ready(boolean canceled) {

                    if (progressDialog != null) {
                        progressDialog.close();
                        progressDialog.dispose();
                        progressDialog = null;
                    }

                    if (canceled) {
                        MsgBox.show(Translation.get("exportedCanceld",
                                String.valueOf(actExportedCount),
                                String.valueOf(numberOfGeoCachesToExport)),
                                Translation.get("export"),
                                MsgBoxIcon.Stop);
                    } else {
                        PlatformUIBase.addToMediaScannerList(exportFile.getAbsolutePath());
                        MsgBox.show(Translation.get("exported",
                                String.valueOf(actExportedCount)),
                                Translation.get("export"),
                                MsgBoxIcon.Information);
                    }

                }

                @Override
                public void setIsCanceled() {
                    isCanceled.set(true);
                }
            };

            progressDialog = new ProgressDialog("export", new WorkAnimation(), runAndReady);

            GL.that.showDialog(progressDialog);

        } catch (IOException ignored) {
        }
    }

}
