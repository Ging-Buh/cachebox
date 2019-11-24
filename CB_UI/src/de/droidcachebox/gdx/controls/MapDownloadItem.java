package de.droidcachebox.gdx.controls;

import de.droidcachebox.ex_import.UnZip;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.activities.FZKDownload.MapRepositoryInfo;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.utils.Copy;
import de.droidcachebox.utils.File;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.http.Download;
import de.droidcachebox.utils.log.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapDownloadItem extends CB_View_Base {
    private static final String log = "MapDownloadItem";
    private final MapRepositoryInfo mapInfo;
    private final CB_CheckBox checkBoxMap;
    private final float margin;
    private final CB_Label lblName, lblSize;
    private final String workPath;
    private final AtomicBoolean downloadIsRunning = new AtomicBoolean(false);
    private int lastProgress = 0;
    private ProgressBar progressBar;
    private boolean canceld = false;

    public MapDownloadItem(MapRepositoryInfo mapInfo, String workPath, float ItemWidth) {
        super(mapInfo.Name);
        this.mapInfo = mapInfo;
        this.workPath = workPath;
        margin = UiSizes.getInstance().getMargin();

        checkBoxMap = new CB_CheckBox("Image");
        this.setHeight(checkBoxMap.getHeight() + (margin * 2));
        this.setWidth(ItemWidth);
        checkBoxMap.setX(margin);

        checkBoxMap.setY(margin);

        lblName = new CB_Label(this.name + " lblName", checkBoxMap.getMaxX() + margin, checkBoxMap.getY(), innerWidth - margin * 3 - checkBoxMap.getWidth(), checkBoxMap.getHeight());
        lblName.setFont(Fonts.getNormal());

        // Cut "Freizeitkarte"
        String Name = mapInfo.Description.replace("Freizeitkarte ", "");
        lblName.setText(Name);

        lblSize = new CB_Label(this.name + " lblSize", checkBoxMap.getMaxX() + margin, checkBoxMap.getY(), innerWidth - margin * 3 - checkBoxMap.getWidth(), checkBoxMap.getHeight());
        lblSize.setFont(Fonts.getNormal());

        // Format Size
        int s = mapInfo.Size / 1024 / 1024;
        lblSize.setHAlignment(HAlignment.RIGHT);
        lblSize.setText(s + " MB");

        this.addChild(checkBoxMap);
        this.addChild(lblName);
        this.addChild(lblSize);

        chkExists();
    }

    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        try {
                            file.delete();
                        } catch (IOException e) {
                            Log.err(log, e.getLocalizedMessage());
                        }
                    }
                }
            }
        }
        try {
            directory.delete();
        } catch (IOException ignored) {
        }
    }

    private void chkExists() {
        int slashPos = mapInfo.Url.lastIndexOf("/");
        String zipFile = mapInfo.Url.substring(slashPos);

        String FileString = FileIO.getFileNameWithoutExtension(zipFile);

        File file = FileFactory.createFile(workPath + "/" + FileString);
        if (file.exists()) {
            checkBoxMap.setChecked(true);
            checkBoxMap.disable();
            checkBoxMap.setClickHandler((view, x, y, pointer, button) -> {
                if (checkBoxMap.isDisabled()) {
                    checkBoxMap.enable();
                } else {
                    checkBoxMap.setChecked(true);
                    checkBoxMap.disable();
                }

                return true;
            });
        }
    }

    public void beginDownload() {
        canceld = false;

        if (!checkBoxMap.isChecked() || checkBoxMap.isDisabled()) {
            lastProgress = -1;
            return;
        }

        downloadIsRunning.set(true);
        float ProgressHeight = (Sprites.ProgressBack.getBottomHeight() + Sprites.ProgressBack.getTopHeight());
        CB_RectF rec = new CB_RectF(checkBoxMap.getMaxX() + margin, 0, innerWidth - margin * 3 - checkBoxMap.getWidth(), ProgressHeight);

        if (progressBar == null) {
            progressBar = new ProgressBar(rec, "");
            this.addChild(progressBar);
            lblName.setY(progressBar.getHalfHeight() - margin);
            lblSize.setY(progressBar.getHalfHeight() - margin);
        }

        lastProgress = 0;

        new Thread(() -> {
            int slashPos = mapInfo.Url.lastIndexOf("/");
            String zipFile = mapInfo.Url.substring(slashPos + 1);
            String target = workPath + "/" + zipFile;

            progressBar.setProgress(lastProgress, lastProgress + " %");

            if (Download.download(mapInfo.Url, target)) {
                Log.info(log, "Unzip " + target + " start.");
                try {
                    UnZip.extractFolder(target);
                } catch (Exception ex) {
                    Log.err(log, "Unzip error: " + ex.toString());
                }
                Log.info(log, "Unzip " + target + " end.");

                // Copy and Clear ? todo check is this necessary and ok?
                File folder = FileFactory.createFile(workPath + "/" + FileIO.getFileNameWithoutExtension(zipFile));
                File newfolder = FileFactory.createFile(workPath + "/" + FileIO.getFileNameWithoutExtension(folder.getName()));

                if (folder.isDirectory()) {
                    folder.renameTo(newfolder);

                    try {
                        Copy.copyFolder(newfolder, FileFactory.createFile(workPath));
                    } catch (IOException e) {
                        Log.err(log, e.getLocalizedMessage());
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.err(log, e.getLocalizedMessage());
                    }

                    deleteDirectory(newfolder);
                }

            }

            try {
                FileFactory.createFile(target).delete();
                Log.info(log, "Deleted " + target);
            } catch (IOException e) {
                Log.err(log, e.getLocalizedMessage());
            }

            lastProgress = canceld ? 0 : 100;
            progressBar.setProgress(lastProgress, lastProgress + " %");
            downloadIsRunning.set(false);
            Log.info(log, "Download everything ready");
        }).start();

    }

    public void cancelDownload() {
        canceld = true;
    }

    public int getDownloadProgress() {
        return lastProgress;
    }

    public boolean isFinished() {
        return !downloadIsRunning.get();
    }

    public void enable() {
        if (checkBoxMap.isChecked())
            checkBoxMap.disable();
        else
            checkBoxMap.enable();
    }
}
