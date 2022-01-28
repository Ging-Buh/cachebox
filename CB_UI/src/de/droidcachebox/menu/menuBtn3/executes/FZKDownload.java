package de.droidcachebox.menu.menuBtn3.executes;

import static de.droidcachebox.gdx.controls.dialogs.ButtonDialog.BTN_LEFT_POSITIVE;
import static de.droidcachebox.menu.Action.ShowMap;

import com.badlogic.gdx.utils.Array;
import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;

import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import de.droidcachebox.ex_import.UnZip;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.CB_View_Base;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.Sprites;
import de.droidcachebox.gdx.controls.Box;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_CheckBox;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.ImportAnimation;
import de.droidcachebox.gdx.controls.ImportAnimation.AnimationType;
import de.droidcachebox.gdx.controls.ProgressBar;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.controls.dialogs.ButtonDialog;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.AbstractFile;
import de.droidcachebox.utils.FileFactory;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.http.Download;
import de.droidcachebox.utils.http.Webb;
import de.droidcachebox.utils.log.Log;

public class FZKDownload extends ActivityBase {
    private static final String URL_repositoryFREIZEITKARTE = "http://repository.freizeitkarte-osm.de/repository_freizeitkarte_android.xml";
    private final Array<MapInfo> mapInfos = new Array<>();
    private final Array<MapDownloadItem> mapDownloadItems = new Array<>();
    private final ScrollBox scrollBox;
    private final AtomicBoolean canceled;
    private boolean allDownloadsComplete;
    private int allProgress = 0;
    private CB_Button btnOK, btnCancel;
    private CB_Label lblProgressMsg;
    private ProgressBar progressBar;
    private boolean importStarted = false;
    private ImportAnimation importAnimation;
    private String repository_freizeitkarte_android;
    private boolean repositoryXMLisDownloading = false;
    private boolean doImportByUrl;
    private MapInfo currentMapInfo;

    public FZKDownload() {
        super("mapDownloadActivity");
        repository_freizeitkarte_android = "";
        scrollBox = new ScrollBox(this);
        addChild(scrollBox);
        createOkCancelBtn();
        createTitleLine();
        scrollBox.setHeight(lblProgressMsg.getY() - btnOK.getMaxY() - margin - margin);
        scrollBox.setY(btnOK.getMaxY() + margin);
        scrollBox.setBackground(getBackground());
        canceled = new AtomicBoolean();
        doImportByUrl = false;
    }

    @Override
    public void onShow() {
        if (!doImportByUrl)
            chkRepository();
    }

    private void createOkCancelBtn() {
        btnOK = new CB_Button(leftBorder, bottomBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "OK Button");
        btnCancel = new CB_Button(btnOK.getMaxX(), bottomBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "Cancel Button");

        // Translations
        btnOK.setText(Translation.get("import"));
        btnCancel.setText(Translation.get("cancel"));

        addChild(btnOK);
        btnOK.setClickHandler((v, x, y, pointer, button) -> {
            importNow();
            return true;
        });

        addChild(btnCancel);
        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (importStarted) {
                ButtonDialog bd = new ButtonDialog(Translation.get("WantCancelImport"), Translation.get("CancelImport"), MsgBoxButton.YesNo, MsgBoxIcon.Stop);
                bd.setButtonClickHandler((which, data) -> {
                    if (which == BTN_LEFT_POSITIVE) {
                        canceled.set(true);
                        // finishImport(); // is done by dlProgressChecker
                    }
                    return true;
                });
                bd.show();
            } else
                finish();
            return true;
        });

    }

    private void createTitleLine() {
        // Title+Progressbar+lblProgressMsg
        float lineHeight = UiSizes.getInstance().getButtonHeight() * 0.75f;
        CB_Label lblTitle = new CB_Label(name + " lblTitle", leftBorder + margin, getHeight() - getTopHeight() - lineHeight - margin, innerWidth - margin, lineHeight);
        lblTitle.setFont(Fonts.getBig());
        float lblWidth = lblTitle.setText(Translation.get("import")).getTextWidth();
        addChild(lblTitle);

        CB_RectF rec = new CB_RectF(lblTitle.getX() + lblWidth + margin, lblTitle.getY(), innerWidth - margin - margin - lblWidth, lineHeight);
        progressBar = new ProgressBar(rec);
        progressBar.setValues(0, "");
        addChild(progressBar);

        float SmallLineHeight = Fonts.measureForSmallFont("Tg").height;
        lblProgressMsg = new CB_Label(name + " lblProgressMsg", leftBorder + margin, lblTitle.getY() - margin - SmallLineHeight, innerWidth - margin - margin, SmallLineHeight);
        lblProgressMsg.setFont(Fonts.getSmall());
        addChild(lblProgressMsg);
    }

    public void showImportByUrl(String url) {
        mapInfos.clear();
        MapInfo MapInfo = new MapInfo();
        MapInfo.url = url;
        int slashPos = MapInfo.url.lastIndexOf("/");
        MapInfo.description = MapInfo.url.substring(slashPos + 1);
        MapInfo.size = -1; // ??? MB
        mapInfos.add(MapInfo);
        fillRepositoryList();
        for (MapDownloadItem item : mapDownloadItems) {
            item.check();
        }
        doImportByUrl = true;
        show();
    }

    private void importNow() {
        if (importStarted)
            return;

        allDownloadsComplete = false;

        // disable btn
        btnOK.disable();
        btnCancel.setText(Translation.get("cancel"));

        // disable UI
        importAnimation = new ImportAnimation(scrollBox);
        importAnimation.setBackground(getBackground());
        importAnimation.setAnimationType(AnimationType.Download);
        addChild(importAnimation, false);

        canceled.set(false);
        importStarted = true;
        for (MapDownloadItem item : mapDownloadItems) {
            item.beginDownload();
        }

        new Thread(() -> {

            while (!allDownloadsComplete) {

                int calcAll = 0;
                int downloadCount = 0;
                for (MapDownloadItem item : mapDownloadItems) {
                    int currentItemDownloadProgress = item.getDownloadProgress();
                    if (currentItemDownloadProgress > -1) {
                        calcAll += currentItemDownloadProgress;
                        downloadCount++;
                    }
                }

                int newAllProgress = downloadCount != 0 ? calcAll / downloadCount : 0;

                if (allProgress != newAllProgress) {
                    allProgress = newAllProgress;
                    progressBar.fillBarAt(allProgress);
                    lblProgressMsg.setText(allProgress + " %");
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }

                // chk download ready
                boolean areAllDownloadsReady = true;
                for (MapDownloadItem item : mapDownloadItems) {
                    if (!item.isFinished()) {
                        areAllDownloadsReady = false;
                        break;
                    }
                }

                if (areAllDownloadsReady) {
                    // all downloads ready
                    allDownloadsComplete = true;
                    finishImport();
                }

            }

        }).start();
    }

    private void finishImport() {
        importStarted = false;

        if (!doImportByUrl) {
            fillDownloadList();

            if (importAnimation != null) {
                removeChildDirect(importAnimation);
                importAnimation.dispose();
                importAnimation = null;
            }
        }

        progressBar.fillBarAt(0);

        if (allDownloadsComplete) {
            lblProgressMsg.setText("");
            btnCancel.setText(Translation.get("ok"));
            // to prevent checked items download again: uncheck (== enable). On next start you must check again
            for (MapDownloadItem item : mapDownloadItems) {
                item.enable();
            }
        } else {
            lblProgressMsg.setText(Translation.get("DownloadCanceld"));
        }

        btnOK.enable();
    }

    private void chkRepository() {
        if (repository_freizeitkarte_android.length() == 0) {
            // Download and Parse, disable UI
            importAnimation = new ImportAnimation(scrollBox);
            importAnimation.setBackground(getBackground());
            importAnimation.setAnimationType(AnimationType.Download);
            lblProgressMsg.setText(Translation.get("ChkAvailableMaps"));
            addChild(importAnimation, false);
            btnOK.disable();

            if (!repositoryXMLisDownloading) {
                repositoryXMLisDownloading = true;
                GL.that.postAsync(() -> {
                    // Read XML
                    repository_freizeitkarte_android = Webb.create()
                            .get(URL_repositoryFREIZEITKARTE)
                            .connectTimeout(Settings.connection_timeout.getValue())
                            .readTimeout(Settings.socket_timeout.getValue())
                            .ensureSuccess()
                            .asString()
                            .getBody();

                    fillDownloadList();

                    if (importAnimation != null) {
                        removeChildDirect(importAnimation);
                        importAnimation.dispose();
                        importAnimation = null;
                    }
                    btnOK.enable();
                    repositoryXMLisDownloading = false;
                    lblProgressMsg.setText("");

                });
            }

        }
    }

    private void fillDownloadList() {
        scrollBox.removeChildren();
        mapInfos.clear();

        System.setProperty("sjxp.namespaces", "false");
        XMLParser<Map<String, String>> parser = new XMLParser<>(createFZKRules().toArray(new IRule[0]));
        parser.parse(new ByteArrayInputStream(repository_freizeitkarte_android.getBytes()), new HashMap<>());

        if (((ShowMap) ShowMap.action).normalMapView.center.isValid()) {
            MapComparator mapComparator = new MapComparator(((ShowMap) ShowMap.action).normalMapView.center);
            mapInfos.sort(mapComparator);
        }

        fillRepositoryList();

    }

    private void fillRepositoryList() {
        // Create possible download List
        mapDownloadItems.clear();
        float yPos = 0;
        String workPath = Settings.getInstance().getPathForMapFile();
        for (MapInfo mapInfo : mapInfos) {
            MapDownloadItem item = new MapDownloadItem(mapInfo, workPath, innerWidth);
            item.setY(yPos);
            scrollBox.addChild(item);
            mapDownloadItems.add(item);
            yPos += item.getHeight() + margin;
        }
        scrollBox.setVirtualHeight(yPos);
    }

    private ArrayList<IRule<Map<String, String>>> createFZKRules() {
        ArrayList<IRule<Map<String, String>>> ruleList = new ArrayList<>();

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                currentMapInfo.name = text;
            }
        });

        String locale = Locale.getDefault().getLanguage();
        if (locale.contains("de")) {
            ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/DescriptionGerman") {
                @Override
                public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                    currentMapInfo.description = text;
                }
            });
        } else {
            ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/DescriptionEnglish") {
                @Override
                public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                    currentMapInfo.description = text;
                }
            });
        }

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Url") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                currentMapInfo.url = text;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Size") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                currentMapInfo.size = Integer.parseInt(text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Checksum") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                currentMapInfo.md5 = text;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/MapsforgeBoundingBoxMinLat") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                currentMapInfo.minLat = Integer.parseInt(text) / 1000000f;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/MapsforgeBoundingBoxMinLon") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                currentMapInfo.minLon = Integer.parseInt(text) / 1000000f;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/MapsforgeBoundingBoxMaxLat") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                currentMapInfo.maxLat = Integer.parseInt(text) / 1000000f;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/MapsforgeBoundingBoxMaxLon") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                currentMapInfo.maxLon = Integer.parseInt(text) / 1000000f;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/Freizeitkarte/Map") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {
                if (isStartTag) {
                    currentMapInfo = new MapInfo();
                } else {
                    mapInfos.add(currentMapInfo);
                }
            }
        });

        return ruleList;
    }

    private static class MapComparator implements Comparator<MapInfo> {
        LatLong centre;

        public MapComparator(LatLong centre) {
            this.centre = centre;
        }

        @Override
        public int compare(MapInfo a, MapInfo b) {
            if ((a == null) || (b == null)) {
                return 0;
            } else {
                boolean aIsIn = a.getBoundingBox().contains(centre);
                boolean bIsIn = b.getBoundingBox().contains(centre);
                if (aIsIn && bIsIn) {
                    // simplifying by comparing the diagonal of the BoundingBox.
                    double ad = a.getBoundingBox().getCenterPoint().distance(centre) / a.getMin().distance(a.getMax());
                    double bd = b.getBoundingBox().getCenterPoint().distance(centre) / b.getMin().distance(b.getMax());
                    if (!a.description.startsWith("*")) a.description = "*" + a.description;
                    if (!b.description.startsWith("*")) b.description = "*" + b.description;
                    return (int) ((bd - ad) * 1000);
                } else {
                    if (aIsIn) {
                        return 1;
                    } else if (bIsIn) {
                        return -1;
                    }
                    // don't need this map
                    return 0;
                }
            }
        }
    }

    private static class MapInfo {
        public String name;
        public String description;
        public String url;
        public int size;
        public double minLat;
        public double minLon;
        public double maxLat;
        public double maxLon;
        String md5;
        BoundingBox bb = null;
        LatLong mi = null;
        LatLong ma = null;

        public BoundingBox getBoundingBox() {
            if (bb == null) bb = new BoundingBox(minLat, minLon, maxLat, maxLon);
            return bb;
        }

        public LatLong getMin() {
            if (mi == null) mi = new LatLong(minLat, minLon);
            return mi;
        }

        public LatLong getMax() {
            if (ma == null) ma = new LatLong(maxLat, maxLon);
            return ma;
        }
    }

    private class MapDownloadItem extends CB_View_Base {
        private static final String sClass = "MapDownloadItem";
        private final CB_CheckBox doDownloadMap;
        private final ProgressBar progressBar;
        private final MapInfo mapInfo;
        private final String pathForMapFile;
        private final AtomicBoolean downloadIsRunning = new AtomicBoolean(false);
        private final Download download;
        private int lastProgress = 0;

        public MapDownloadItem(MapInfo _mapInfo, String _pathForMapFile, float _itemWidth) {
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
            lblSize.setHAlignment(CB_Label.HAlignment.RIGHT);
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
            }, canceled::get); // getting the superior canceled

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

                lastProgress = canceled.get() ? 0 : 100;
                progressBar.setValues(lastProgress, lastProgress + " %");
                downloadIsRunning.set(false);
                Log.info(sClass, "Download " + target + (canceled.get() ? " canceled" : " ready"));
            }).start();

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

}
