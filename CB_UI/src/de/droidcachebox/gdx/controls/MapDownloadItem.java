package de.droidcachebox.gdx.controls;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.ex_import.UnZip;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.CB_Label.HAlignment;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.menuBtn3.executes.FZKDownload.MapRepositoryInfo;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.http.Download;
import de.droidcachebox.utils.log.Log;

public class MapDownloadItem extends CB_View_Base {
    private static final String sClass = "MapDownloadItem";
    private final CB_CheckBox doDownloadMap;
    private final ProgressBar progressBar;
    private final MapRepositoryInfo mapInfo;
    private final String pathForMapFile;
    private int lastProgress = 0;
    private boolean canceled = false;
    private final AtomicBoolean downloadIsRunning = new AtomicBoolean(false);
    private final Download download;

    public MapDownloadItem(MapRepositoryInfo _mapInfo, String _pathForMapFile, float _itemWidth) {
        super(_mapInfo.name);
        mapInfo = _mapInfo;
        pathForMapFile = _pathForMapFile;
        doDownloadMap = new CB_CheckBox();
        float progressHeight;
        if (mapInfo.size > -1) {
            progressHeight = (Sprites.progressBack.getBottomHeight() + Sprites.progressBack.getTopHeight());
        } else {
            progressHeight = UiSizes.getInstance().getButtonHeight();
        }
        setHeight(doDownloadMap.getHeight() + progressHeight + 2 * UiSizes.getInstance().getMargin());
        setWidth(_itemWidth);
        Box rightBox = new Box(getWidth(), getHeight());
        String name = mapInfo.description.replace("Freizeitkarte", "").trim();
        name = name.replace("freizeitkarte_", "");
        CB_Label lblName = new CB_Label(name);
        CB_Label lblSize;
        if (mapInfo.size > 0) {
            int s = mapInfo.size / 1024 / 1024;
            lblSize = new CB_Label(s + " MB");
        } else {
            lblSize = new CB_Label("??? MB");
        }
        lblSize.setHAlignment(HAlignment.RIGHT);
        addNext(doDownloadMap, FIXED);
        addLast(rightBox);
        doDownloadMap.setY((getHeight() - doDownloadMap.getHeight()) / 2);
        rightBox.addNext(lblName);
        rightBox.addLast(lblSize, -0.20f);
        progressBar = new ProgressBar(new CB_RectF(0, 0, 0, progressHeight));
        rightBox.addLast(progressBar);
        adjustHeight();

        chkExists();

        download = new Download((message, progressMessage, progress) -> {
            if (mapInfo.size > -1) {
                progressBar.setValues(((progress) * 100) / (mapInfo.size / 1024), "");
            } else {
                progressBar.setValues(100, progress / 1024 + " MB");
            }
        });

    }

    private void chkExists() {
        int slashPos = mapInfo.url.lastIndexOf("/");
        String zipFile = mapInfo.url.substring(slashPos);

        String FileString = FileIO.getFileNameWithoutExtension(zipFile);

        AbstractFile abstractFile = FileFactory.createFile(pathForMapFile + "/" + FileString);
        if (abstractFile.exists()) {
            doDownloadMap.setChecked(true);
            doDownloadMap.disable();
            doDownloadMap.setClickHandler((view, x, y, pointer, button) -> {
                if (doDownloadMap.isDisabled()) {
                    doDownloadMap.enable();
                } else {
                    doDownloadMap.setChecked(true);
                    doDownloadMap.disable();
                }
                return true;
            });
        }
    }

    public void beginDownload() {
        canceled = false;

        if (!doDownloadMap.isChecked() || doDownloadMap.isDisabled()) {
            lastProgress = -1;
            return;
        }

        downloadIsRunning.set(true);

        lastProgress = 0;

        new Thread(() -> {
            int slashPos = mapInfo.url.lastIndexOf("/");
            String zipFile = mapInfo.url.substring(slashPos + 1);
            String target = pathForMapFile + "/" + zipFile;
            progressBar.setValues(lastProgress, lastProgress + " %");
            if (download.download(mapInfo.url, target)) {
                if (target.endsWith(".zip")) {
                    if (mapInfo.size == -1)
                        progressBar.setValues(100, "Unzip " + FileIO.getFileName(target) + " start.");
                    Log.info(sClass, "Unzip " + target + " start.");
                    try {
                        UnZip.extractHere(target);
                        if (mapInfo.size == -1)
                            progressBar.setValues(100, "Unzip " + FileIO.getFileName(target) + " end.");
                    } catch (Exception ex) {
                        Log.err(sClass, "Unzip error: " + ex.toString());
                        if (mapInfo.size == -1)
                            progressBar.setValues(100, "Unzip " + FileIO.getFileName(target) + " error.");
                    }
                    Log.info(sClass, "Unzip " + target + " end.");
                }
            }
            // delete downloaded file
            if (target.endsWith(".zip")) {
                try {
                    FileFactory.createFile(target).delete();
                    Log.info(sClass, "Deleted " + target);
                } catch (IOException e) {
                    progressBar.setValues(100, FileIO.getFileName(target) + "not deleted.");
                    Log.err(sClass, target, e);
                }
            }

            lastProgress = canceled ? 0 : 100;
            progressBar.setValues(lastProgress, lastProgress + " %");
            downloadIsRunning.set(false);
            Log.info(sClass, "Download everything ready");
        }).start();

    }

    public void cancelDownload() {
        canceled = true;
        download.cancelDownload();
    }

    public int getDownloadProgress() {
        return lastProgress;
    }

    public boolean isFinished() {
        return !downloadIsRunning.get();
    }

    public void enable() {
        if (doDownloadMap.isChecked())
            doDownloadMap.disable();
        else
            doDownloadMap.enable();
    }

    public void check() {
        doDownloadMap.enable();
        doDownloadMap.setChecked(true);
    }
}
