package de.droidcachebox.gdx.activities;

import com.badlogic.gdx.utils.Array;
import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;
import de.droidcachebox.Config;
import de.droidcachebox.core.CB_Core_Settings;
import de.droidcachebox.ex_import.BreakawayImportThread;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.*;
import de.droidcachebox.gdx.controls.ImportAnimation.AnimationType;
import de.droidcachebox.gdx.controls.messagebox.MessageBox;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.locator.map.LayerManager;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.ProgressChangedEvent;
import de.droidcachebox.utils.ProgresssChangedEventList;
import de.droidcachebox.utils.http.Webb;
import de.droidcachebox.utils.log.Log;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

public class FZKDownload extends ActivityBase implements ProgressChangedEvent {
    private static final String log = "MapDownload";
    private static final String URL_repositoryFREIZEITKARTE = "http://repository.freizeitkarte-osm.de/repository_freizeitkarte_android.xml";
    private static FZKDownload fzkDownload;
    private boolean downloadIsCompleted = false;
    private int allProgress = 0;
    private Array<MapRepositoryInfo> mapInfoList = new Array<>();
    private Array<MapDownloadItem> mapInfoItemList = new Array<>();
    private MapRepositoryInfo actMapRepositoryInfo;
    private CB_Button btnOK, btnCancel;
    private CB_Label lblProgressMsg;
    private ProgressBar progressBar;
    private boolean importStarted = false;
    private ScrollBox scrollBox;
    private ImportAnimation importAnimation;
    private String repository_freizeitkarte_android;
    private boolean canceld = false;
    private boolean isChkRepository = false;
    private boolean doImportByUrl;

    private FZKDownload() {
        super("mapDownloadActivity");
        repository_freizeitkarte_android = "";
        scrollBox = new ScrollBox(this);
        addChild(scrollBox);
        createOkCancelBtn();
        createTitleLine();
        scrollBox.setHeight(lblProgressMsg.getY() - btnOK.getMaxY() - margin - margin);
        scrollBox.setY(btnOK.getMaxY() + margin);
        scrollBox.setBackground(getBackground());
    }

    public static FZKDownload getInstance() {
        // cause Activity gets disposed and a second run will produce an error
        if (fzkDownload == null || fzkDownload.isDisposed()) {
            fzkDownload = new FZKDownload();
        }
        return fzkDownload;
    }

    @Override
    public void onShow() {
        ProgresssChangedEventList.add(this);
        if (!doImportByUrl)
            chkRepository();
    }

    @Override
    public void onHide() {
        ProgresssChangedEventList.remove(this);
    }

    private void createOkCancelBtn() {
        btnOK = new CB_Button(leftBorder, leftBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "OK Button");
        btnCancel = new CB_Button(btnOK.getMaxX(), leftBorder, innerWidth / 2, UiSizes.getInstance().getButtonHeight(), "Cancel Button");

        // Translations
        btnOK.setText(Translation.get("import"));
        btnCancel.setText(Translation.get("cancel"));

        addChild(btnOK);
        btnOK.setClickHandler((v, x, y, pointer, button) -> {
            ImportNow();
            return true;
        });

        addChild(btnCancel);
        btnCancel.setClickHandler((v, x, y, pointer, button) -> {
            if (BreakawayImportThread.isCanceled()) {
                BreakawayImportThread.reset();
                finish();
                return true;
            }

            if (importStarted) {
                MessageBox.show(Translation.get("WantCancelImport"), Translation.get("CancelImport"), MessageBoxButton.YesNo, MessageBoxIcon.Stop,
                        (which, data) -> {
                            if (which == MessageBox.BTN_LEFT_POSITIVE) {
                                finishImport();
                            }
                            return true;
                        });
            } else
                finish();
            return true;
        });

    }

    private void createTitleLine() {
        // Title+Progressbar

        float lineHeight = UiSizes.getInstance().getButtonHeight() * 0.75f;

        CB_Label lblTitle = new CB_Label(name + " lblTitle", leftBorder + margin, getHeight() - getTopHeight() - lineHeight - margin, innerWidth - margin, lineHeight);
        lblTitle.setFont(Fonts.getBig());
        float lblWidth = lblTitle.setText(Translation.get("import")).getTextWidth();
        addChild(lblTitle);

        CB_RectF rec = new CB_RectF(lblTitle.getX() + lblWidth + margin, lblTitle.getY(), innerWidth - margin - margin - lblWidth, lineHeight);

        progressBar = new ProgressBar(rec, "ProgressBar");

        progressBar.setProgress(0, "");

        float SmallLineHeight = Fonts.measureForSmallFont("Tg").height;

        lblProgressMsg = new CB_Label(name + " lblProgressMsg", leftBorder + margin, lblTitle.getY() - margin - SmallLineHeight, innerWidth - margin - margin, SmallLineHeight);

        lblProgressMsg.setFont(Fonts.getSmall());

        addChild(progressBar);
        addChild(lblProgressMsg);

    }

    @Override
    public void progressChanged(final String message, final String progressMessage, final int progress) {
        GL.that.RunOnGL(() -> {
            progressBar.setPogress(progress);
            lblProgressMsg.setText(progressMessage);
            if (message.length() > 0)
                progressBar.setText(message);
        });
    }

    public void importByUrl(String url) {
        mapInfoList.clear();
        MapRepositoryInfo mapRepositoryInfo = new MapRepositoryInfo();
        mapRepositoryInfo.url = url;
        int slashPos = mapRepositoryInfo.url.lastIndexOf("/");
        mapRepositoryInfo.description = mapRepositoryInfo.url.substring(slashPos + 1);
        mapRepositoryInfo.size = -1; // ??? MB
        mapInfoList.add(mapRepositoryInfo);
        fillRepositoryList();
        for (MapDownloadItem item : mapInfoItemList) {
            item.check();
        }
        doImportByUrl = true;
    }

    public void importByUrlFinished() {
        doImportByUrl = false;
    }

    private void ImportNow() {
        if (importStarted)
            return;

        downloadIsCompleted = false;

        // disable btn
        btnOK.disable();
        btnCancel.setText(Translation.get("cancel"));

        // disable UI
        importAnimation = new ImportAnimation(scrollBox);
        importAnimation.setBackground(getBackground());
        importAnimation.setAnimationType(AnimationType.Download);
        addChild(importAnimation, false);

        canceld = false;
        importStarted = true;
        for (int i = 0; i < mapInfoItemList.size; i++) {
            MapDownloadItem item = mapInfoItemList.get(i);
            item.beginDownload();
        }

        Thread dlProgressChecker = new Thread(() -> {

            while (!downloadIsCompleted) {
                if (canceld) {
                    for (int i = 0; i < mapInfoItemList.size; i++) {
                        MapDownloadItem item = mapInfoItemList.get(i);
                        item.cancelDownload();
                    }
                }

                int calcAll = 0;
                int downloadCount = 0;
                for (int i = 0; i < mapInfoItemList.size; i++) {
                    MapDownloadItem item = mapInfoItemList.get(i);
                    int actPro = item.getDownloadProgress();
                    if (actPro > -1) {
                        calcAll += actPro;
                        downloadCount++;
                    }

                }
                int newAllProgress = downloadCount != 0 ? calcAll / downloadCount : 0;

                if (allProgress != newAllProgress) {
                    allProgress = newAllProgress;
                    progressBar.setPogress(allProgress);
                    lblProgressMsg.setText(allProgress + " %");
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }

                // chk download ready
                boolean chk = true;
                for (int i = 0, n = mapInfoItemList.size; i < n; i++) {
                    MapDownloadItem item = mapInfoItemList.get(i);
                    if (!item.isFinished()) {
                        chk = false;
                        break;
                    }
                }

                if (chk) {
                    // all downloads ready
                    downloadIsCompleted = true;
                    finishImport();
                }

            }

        });

        dlProgressChecker.start();
    }

    private void finishImport() {
        canceld = true;
        importStarted = false;
        fillDownloadList();
        if (importAnimation != null) {
            removeChildDirect(importAnimation);
            importAnimation.dispose();
            importAnimation = null;
        }
        progressBar.setPogress(0);

        if (downloadIsCompleted) {
            lblProgressMsg.setText("");
            btnCancel.setText(Translation.get("ok"));
            // to prevent download again. On next start you must check again
            for (int i = 0, n = mapInfoItemList.size; i < n; i++) {
                MapDownloadItem item = mapInfoItemList.get(i);
                item.enable();
            }
        } else
            lblProgressMsg.setText(Translation.get("DownloadCanceld"));

        btnOK.enable();
        LayerManager.getInstance().initLayers();
    }

    private void chkRepository() {
        if (repository_freizeitkarte_android.length() == 0) {
            // Download and Parse
            // disable UI
            importAnimation = new ImportAnimation(scrollBox);
            importAnimation.setBackground(getBackground());
            importAnimation.setAnimationType(AnimationType.Download);
            lblProgressMsg.setText(Translation.get("ChkAvailableMaps"));
            addChild(importAnimation, false);
            btnOK.disable();

            if (!isChkRepository)
                readRepository();

        }
    }

    private void readRepository() {
        isChkRepository = true;
        GL.that.postAsync(() -> {
            // Read XML
            repository_freizeitkarte_android = Webb.create()
                    .get(URL_repositoryFREIZEITKARTE)
                    .connectTimeout(CB_Core_Settings.connection_timeout.getValue())
                    .readTimeout(CB_Core_Settings.socket_timeout.getValue())
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
            isChkRepository = false;
            lblProgressMsg.setText("");

            LayerManager.getInstance().initLayers();
        });
    }

    private void fillDownloadList() {
        scrollBox.removeChilds();

        Map<String, String> values = new HashMap<>();
        System.setProperty("sjxp.namespaces", "false");
        List<IRule<Map<String, String>>> ruleList = createRepositoryRules(new ArrayList<>());

        XMLParser<Map<String, String>> parserCache = new XMLParser<>(ruleList.toArray(new IRule[0]));

        InputStream stream = new ByteArrayInputStream(repository_freizeitkarte_android.getBytes());
        parserCache.parse(stream, values);

        if (ShowMap.getInstance().normalMapView.center.isValid()) {
            MapComparer comparer = new MapComparer(ShowMap.getInstance().normalMapView.center);
            mapInfoList.sort(comparer);
        }

        fillRepositoryList();

    }

    private void fillRepositoryList() {
        // Create possible download List
        float yPos = 0;
        String workPath = getPathForMapFile();
        int n = mapInfoList.size - 1;
        for (int i = n; i > -1; i--) {
            MapRepositoryInfo map = mapInfoList.get(i);
            MapDownloadItem item = new MapDownloadItem(map, workPath, innerWidth);
            item.setY(yPos);
            scrollBox.addChild(item);
            mapInfoItemList.add(item);
            yPos += item.getHeight() + margin;
        }

        scrollBox.setVirtualHeight(yPos);
    }

    public String getPathForMapFile() {

        // get and check the target directory (global value)
        String pathForMapFile = Config.MapPackFolder.getValue();
        boolean isWritable;
        if (pathForMapFile.length() > 0)
            isWritable = FileIO.canWrite(pathForMapFile);
        else
            isWritable = false;
        if (isWritable)
            Log.info(log, "Download to " + pathForMapFile);
        else {
            Log.err(log, "Download to " + pathForMapFile + " is not possible!");
            // don't use Config.MapPackFolder.getDefaultValue()
            // because it doesn't reflect own repository
            // own or global repository is writable by default, but do check again
            pathForMapFile = Config.MapPackFolderLocal.getValue();
            isWritable = FileIO.canWrite(pathForMapFile);
            Log.info(log, "Download to " + pathForMapFile + " is possible? " + isWritable);
        }
        return pathForMapFile;
    }

    private List<IRule<Map<String, String>>> createRepositoryRules(List<IRule<Map<String, String>>> ruleList) {
        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Name") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                actMapRepositoryInfo.name = text;
            }
        });

        String locale = Locale.getDefault().getLanguage();
        if (locale.contains("de")) {
            ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/DescriptionGerman") {
                @Override
                public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                    actMapRepositoryInfo.description = text;
                }
            });
        } else {
            ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/DescriptionEnglish") {
                @Override
                public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                    actMapRepositoryInfo.description = text;
                }
            });
        }

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Url") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                actMapRepositoryInfo.url = text;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Size") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                actMapRepositoryInfo.size = Integer.parseInt(text);
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/Checksum") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                actMapRepositoryInfo.md5 = text;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/MapsforgeBoundingBoxMinLat") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                actMapRepositoryInfo.minLat = Integer.parseInt(text) / 1000000f;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/MapsforgeBoundingBoxMinLon") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                actMapRepositoryInfo.minLon = Integer.parseInt(text) / 1000000f;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/MapsforgeBoundingBoxMaxLat") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                actMapRepositoryInfo.maxLat = Integer.parseInt(text) / 1000000f;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.CHARACTER, "/Freizeitkarte/Map/MapsforgeBoundingBoxMaxLon") {
            @Override
            public void handleParsedCharacters(XMLParser<Map<String, String>> parser, String text, Map<String, String> values) {
                actMapRepositoryInfo.maxLon = Integer.parseInt(text) / 1000000f;
            }
        });

        ruleList.add(new DefaultRule<Map<String, String>>(Type.TAG, "/Freizeitkarte/Map") {
            @Override
            public void handleTag(XMLParser<Map<String, String>> parser, boolean isStartTag, Map<String, String> values) {

                if (isStartTag) {
                    actMapRepositoryInfo = new MapRepositoryInfo();
                } else {
                    mapInfoList.add(actMapRepositoryInfo);
                }

            }
        });
        return ruleList;
    }

    public static class MapRepositoryInfo {
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

    static class MapComparer implements Comparator<MapRepositoryInfo> {
        LatLong centre;

        public MapComparer(LatLong centre) {
            this.centre = centre;
        }

        @Override
        public int compare(MapRepositoryInfo a, MapRepositoryInfo b) {
            if ((a == null) || (b == null)) {
                return 0;
            } else {
                boolean aIsIn = a.getBoundingBox().contains(centre);
                boolean bIsIn = b.getBoundingBox().contains(centre);
                if (aIsIn && bIsIn) {
                    // vereinfachend in Relation zur BoundingBox Diagonalen vergleichen.
                    double ad = a.getBoundingBox().getCenterPoint().distance(centre) / a.getMin().distance(a.getMax());
                    double bd = b.getBoundingBox().getCenterPoint().distance(centre) / b.getMin().distance(b.getMax());
                    if (!a.description.startsWith("*")) a.description = "*" + a.description;
                    if (!b.description.startsWith("*")) b.description = "*" + b.description;
                    return (int) ((ad - bd) * 1000);
                } else {
                    if (aIsIn && !bIsIn) {
                        return -1;
                    } else if (!aIsIn && bIsIn) {
                        return 1;
                    }
                    // don't need this map
                    return 0;
                }
            }
        }
    }

}
