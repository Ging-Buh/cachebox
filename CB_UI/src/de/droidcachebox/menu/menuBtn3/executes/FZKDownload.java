package de.droidcachebox.menu.menuBtn3.executes;

import com.badlogic.gdx.utils.Array;
import com.thebuzzmedia.sjxp.XMLParser;
import com.thebuzzmedia.sjxp.rule.DefaultRule;
import com.thebuzzmedia.sjxp.rule.IRule;
import com.thebuzzmedia.sjxp.rule.IRule.Type;

import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.droidcachebox.ex_import.BreakawayImportThread;
import de.droidcachebox.gdx.ActivityBase;
import de.droidcachebox.gdx.Fonts;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.CB_Button;
import de.droidcachebox.gdx.controls.CB_Label;
import de.droidcachebox.gdx.controls.ImportAnimation;
import de.droidcachebox.gdx.controls.ImportAnimation.AnimationType;
import de.droidcachebox.gdx.controls.MapDownloadItem;
import de.droidcachebox.gdx.controls.ProgressBar;
import de.droidcachebox.gdx.controls.ScrollBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBox;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxButton;
import de.droidcachebox.gdx.controls.messagebox.MsgBoxIcon;
import de.droidcachebox.gdx.math.CB_RectF;
import de.droidcachebox.gdx.math.UiSizes;
import de.droidcachebox.menu.menuBtn3.ShowMap;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.FileIO;
import de.droidcachebox.utils.http.Webb;
import de.droidcachebox.utils.log.Log;

public class FZKDownload extends ActivityBase {
    private static final String sClass = "MapDownload";
    private static final String URL_repositoryFREIZEITKARTE = "http://repository.freizeitkarte-osm.de/repository_freizeitkarte_android.xml";
    private static FZKDownload fzkDownload;
    private final Array<MapRepositoryInfo> mapRepositoryInfos = new Array<>();
    private final Array<MapDownloadItem> mapDownloadItems = new Array<>();
    private final ScrollBox scrollBox;
    private boolean areAllDownloadsCompleted = false;
    private int allProgress = 0;
    private MapRepositoryInfo actMapRepositoryInfo;
    private CB_Button btnOK, btnCancel;
    private CB_Label lblProgressMsg;
    private ProgressBar progressBar;
    private boolean importStarted = false;
    private ImportAnimation importAnimation;
    private String repository_freizeitkarte_android;
    private boolean canceled = false;
    private boolean repositoryXMLisDownloading = false;
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
        doImportByUrl = false;
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
                MsgBox.show(Translation.get("WantCancelImport"), Translation.get("CancelImport"), MsgBoxButton.YesNo, MsgBoxIcon.Stop,
                        (which, data) -> {
                            if (which == MsgBox.BTN_LEFT_POSITIVE) {
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

    public void importByUrl(String url) {
        mapRepositoryInfos.clear();
        MapRepositoryInfo mapRepositoryInfo = new MapRepositoryInfo();
        mapRepositoryInfo.url = url;
        int slashPos = mapRepositoryInfo.url.lastIndexOf("/");
        mapRepositoryInfo.description = mapRepositoryInfo.url.substring(slashPos + 1);
        mapRepositoryInfo.size = -1; // ??? MB
        mapRepositoryInfos.add(mapRepositoryInfo);
        fillRepositoryList();
        for (MapDownloadItem item : mapDownloadItems) {
            item.check();
        }
        doImportByUrl = true;
    }

    private void ImportNow() {
        if (importStarted)
            return;

        areAllDownloadsCompleted = false;

        // disable btn
        btnOK.disable();
        btnCancel.setText(Translation.get("cancel"));

        // disable UI
        importAnimation = new ImportAnimation(scrollBox);
        importAnimation.setBackground(getBackground());
        importAnimation.setAnimationType(AnimationType.Download);
        addChild(importAnimation, false);

        canceled = false;
        importStarted = true;
        for (MapDownloadItem item : mapDownloadItems) {
            item.beginDownload();
        }
        Thread dlProgressChecker = new Thread(() -> {

            while (!areAllDownloadsCompleted) {
                if (canceled) {
                    for (MapDownloadItem item : mapDownloadItems) {
                        item.cancelDownload();
                    }
                }

                int calcAll = 0;
                int downloadCount = 0;
                for (MapDownloadItem item : mapDownloadItems) {
                    int actPro = item.getDownloadProgress();
                    if (actPro > -1) {
                        calcAll += actPro;
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
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
                    areAllDownloadsCompleted = true;
                    finishImport();
                }

            }

        });

        dlProgressChecker.start();
    }

    private void finishImport() {
        canceled = true;
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

        if (areAllDownloadsCompleted) {
            lblProgressMsg.setText("");
            btnCancel.setText(Translation.get("ok"));
            // to prevent download again. On next start you must check again
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
        scrollBox.removeChilds();
        mapRepositoryInfos.clear();

        System.setProperty("sjxp.namespaces", "false");
        XMLParser<Map<String, String>> parser = new XMLParser<>(createRepositoryRules().toArray(new IRule[0]));
        parser.parse(new ByteArrayInputStream(repository_freizeitkarte_android.getBytes()), new HashMap<>());

        if (ShowMap.getInstance().normalMapView.center.isValid()) {
            MapComparator mapComparator = new MapComparator(ShowMap.getInstance().normalMapView.center);
            mapRepositoryInfos.sort(mapComparator);
        }

        fillRepositoryList();

    }

    private void fillRepositoryList() {
        // Create possible download List
        mapDownloadItems.clear();
        float yPos = 0;
        String workPath = getPathForMapFile();
        for (MapRepositoryInfo map : mapRepositoryInfos) {
            MapDownloadItem item = new MapDownloadItem(map, workPath, innerWidth);
            item.setY(yPos);
            scrollBox.addChild(item);
            mapDownloadItems.add(item);
            yPos += item.getHeight() + margin;
        }
        scrollBox.setVirtualHeight(yPos);
    }

    public String getPathForMapFile() {

        // get and check the target directory (global value)
        String pathForMapFile = Settings.MapPackFolder.getValue();
        boolean isWritable;
        if (pathForMapFile.length() > 0)
            isWritable = FileIO.canWrite(pathForMapFile);
        else
            isWritable = false;
        if (isWritable)
            Log.info(sClass, "Download to " + pathForMapFile);
        else {
            Log.err(sClass, "Download to " + pathForMapFile + " is not possible!");
            // don't use SettingsClass.MapPackFolder.getDefaultValue()
            // because it doesn't reflect own repository
            // own or global repository is writable by default, but do check again
            pathForMapFile = Settings.MapPackFolderLocal.getValue();
            isWritable = FileIO.canWrite(pathForMapFile);
            Log.info(sClass, "Download to " + pathForMapFile + " is possible? " + isWritable);
        }
        return pathForMapFile;
    }

    private ArrayList<IRule<Map<String, String>>> createRepositoryRules() {

        ArrayList<IRule<Map<String, String>>> ruleList = new ArrayList<>();

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
                    mapRepositoryInfos.add(actMapRepositoryInfo);
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

    static class MapComparator implements Comparator<MapRepositoryInfo> {
        LatLong centre;

        public MapComparator(LatLong centre) {
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

}
