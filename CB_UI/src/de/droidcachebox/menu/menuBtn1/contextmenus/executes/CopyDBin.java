package de.droidcachebox.menu.menuBtn1.contextmenus.executes;

import java.io.IOException;
import java.util.Objects;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.FileOrFolderPicker;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.gdx.controls.dialogs.WaitDialog;
import de.droidcachebox.gdx.controls.list.V_ListView;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.Copy;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileList;
import de.droidcachebox.utils.log.Log;

public class CopyDBin {
    private static final String sClass = "CopyDBout";
    private String DBFile;
    private FileList dbFiles;
    private V_ListView lvDBSelection;

    public CopyDBin(String copyDBFile, FileList files, V_ListView selection) {
        DBFile = copyDBFile;
        dbFiles = files;
        lvDBSelection = selection;
    }

    public void copyDBin() {
        new FileOrFolderPicker("",
                ".db3",
                Translation.get("enterAnyFileName"),
                Translation.get("select"),
                fromDbFile -> GL.that.runOnGL(() -> inputFile(fromDbFile))).show();
    }

    private void inputFile(AbstractFile copyIn) {
        new WaitDialog(copyIn.getName()+": "+Translation.get("copyingFile"), new RunAndReady() {
            @Override
            public void ready() {

            }

            @Override
            public void setIsCanceled() {

            }

            @Override
            public void run() {
                try {
                    if (Objects.equals(copyIn.getName(), DBFile)) {
                        GL.that.toast("Selected Database is open!");
                    } else {
                        AbstractFile destinationFile = FileFactory.createFile(GlobalCore.workPath, copyIn.getName());
                        if (!copyIn.getParent().equals(GlobalCore.workPath)) {
                            // Delete File if exist
                            boolean dbExists = destinationFile.exists();
                            if (dbExists) {
                                destinationFile.delete();
                            }
                            Copy.copyFolder(copyIn, destinationFile);
                            // Don' add it if exists before
                            if (!dbExists) {
                                dbFiles.add(destinationFile);
                            }
                            lvDBSelection.notifyDataSetChanged();
                        }
                    }
                } catch(IOException ex){
                    Log.err(sClass, "copyDBfromFolder", ex);
                }
            }
        }).show();
    }

}
