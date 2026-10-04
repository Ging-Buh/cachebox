package de.droidcachebox.menu.menuBtn1.contextmenus.executes;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.Platform;
import de.droidcachebox.core.GpxSerializer;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.controls.dialogs.ProgressDialog;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.controls.dialogs.WaitDialog;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.Copy;
import de.droidcachebox.utils.CopyJobDefinition;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.log.Log;

public class CopyDBout {
    private static final String sClass = "CopyDBout";
    private AbstractFile DBFile = null;

    public CopyDBout(AbstractFile copyDBFile) {
        DBFile = copyDBFile;
    }

    public void copyDBout() {
        new FileOrFolderPicker("",
                Translation.get("selectExportFolder"),
                Translation.get("select"),
                intoDbFile -> GL.that.runOnGL(() -> outputFile(intoDbFile))).show();
    }

    private void outputFile(AbstractFile exportDir) {
        new WaitDialog(DBFile.getName()+": "+Translation.get("copyingFile"), new RunAndReady() {
            @Override
            public void ready() {

            }

            @Override
            public void setIsCanceled() {

            }

            @Override
            public void run() {
                try {
                    AbstractFile destinationFile = FileFactory.createFile(exportDir, DBFile.getName());
                    if (!destinationFile.getParent().equals(GlobalCore.workPath)) {
                        // Delete File if exist
                        if (destinationFile.exists()) {
                            destinationFile.delete();
                        }
                        Copy.copyFolder(DBFile, destinationFile);
                    }
                } catch (IOException ex) {
                    Log.err(sClass, "copyDBintoFolder", ex);
                }
            }
        }).show();
    }

}
