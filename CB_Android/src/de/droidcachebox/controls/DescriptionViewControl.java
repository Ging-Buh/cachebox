package de.droidcachebox.controls;

import static de.droidcachebox.core.GroundspeakAPI.APIError;
import static de.droidcachebox.core.GroundspeakAPI.GeoCacheRelated;
import static de.droidcachebox.core.GroundspeakAPI.LastAPIError;
import static de.droidcachebox.core.GroundspeakAPI.fetchMyCacheLimits;
import static de.droidcachebox.core.GroundspeakAPI.fetchMyUserInfos;
import static de.droidcachebox.core.GroundspeakAPI.isDownloadLimitExceeded;
import static de.droidcachebox.core.GroundspeakAPI.isPremiumMember;
import static de.droidcachebox.core.GroundspeakAPI.updateGeoCache;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import de.droidcachebox.GlobalCore;
import de.droidcachebox.Platform;
import de.droidcachebox.ViewOptionsMenu;
import de.droidcachebox.database.CBDB;
import de.droidcachebox.database.CachesDAO;
import de.droidcachebox.database.ImageDAO;
import de.droidcachebox.database.LogsTableDAO;
import de.droidcachebox.database.WaypointDAO;
import de.droidcachebox.dataclasses.Attribute;
import de.droidcachebox.dataclasses.Cache;
import de.droidcachebox.dataclasses.ImageEntry;
import de.droidcachebox.dataclasses.LogEntry;
import de.droidcachebox.dataclasses.Waypoint;
import de.droidcachebox.ex_import.DescriptionImageGrabber;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxButton;
import de.droidcachebox.gdx.controls.dialogs.MsgBoxIcon;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.http.Download;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.views.forms.MessageBox;

public class DescriptionViewControl extends WebView implements ViewOptionsMenu {
    private final static String sClass = "DescriptionViewControl";
    private static final Handler downloadReadyHandler = new Handler();
    private final static LinkedList<String> NonLocalImages = new LinkedList<>();
    private final static LinkedList<String> NonLocalImagesUrl = new LinkedList<>();
    private static ProgressDialog pd;
    private static Cache aktCache;
    private static String message = "";
    private Activity mainActivity;
    private final CachesDAO cachesDAO;
    // private static int downloadTryCounter = 0;
    private final DialogInterface.OnClickListener downloadCacheDialogResult = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int button) {
            CachesDAO cachesDAO = new CachesDAO();
            if (button == -1) {
                Cache newCache;

                ArrayList<GeoCacheRelated> geoCacheRelateds = updateGeoCache(aktCache);
                if (geoCacheRelateds.size() > 0) {
                    GeoCacheRelated geoCacheRelated = geoCacheRelateds.get(0);
                    newCache = geoCacheRelated.cache;

                    synchronized (CBDB.getInstance().cacheList) {
                        CBDB.getInstance().beginTransaction();

                        CBDB.getInstance().cacheList.remove(aktCache);
                        CBDB.getInstance().cacheList.add(newCache);

                        cachesDAO.updateDatabase(newCache);
                        newCache.setLongDescription("");

                        for (LogEntry apiLog : geoCacheRelated.logs)
                            LogsTableDAO.getInstance().WriteLogEntry(apiLog);

                        WaypointDAO waypointDAO = WaypointDAO.getInstance();
                        for (int i = 0, n = newCache.getWayPoints().size(); i < n; i++) {
                            Waypoint waypoint = newCache.getWayPoints().get(i);

                            boolean update = true;

                            // dont refresh wp if aktCache.wp is user changed
                            for (int j = 0, m = aktCache.getWayPoints().size(); j < m; j++) {
                                Waypoint wp = aktCache.getWayPoints().get(j);
                                if (wp.getWaypointCode().equalsIgnoreCase(waypoint.getWaypointCode())) {
                                    if (wp.isUserWaypoint)
                                        update = false;
                                    break;
                                }
                            }

                            if (update)
                                waypointDAO.writeToDatabase(waypoint, false);
                        }

                        ImageDAO imageDAO = new ImageDAO();
                        for (ImageEntry image : geoCacheRelated.images)
                            imageDAO.writeToDatabase(image, false);

                        CBDB.getInstance().setTransactionSuccessful();
                        CBDB.getInstance().endTransaction();

                        cachesDAO.updateCacheCountForGPXFilenames();
                    }
                    aktCache = newCache;
                    setCache(newCache);
                    if (!isPremiumMember()) {
                        String s = "Download successful!\n";
                        fetchMyCacheLimits();
                        s += "Downloads left for today: " + fetchMyUserInfos().remaining + "\n";
                        s += "If you upgrade to Premium Member you are allowed to download the full cache details of 6000 caches per day and you can search not only for traditional caches (www.geocaching.com).";

                        MessageBox.show(mainActivity, s, Translation.get("GC_title"), MsgBoxButton.OKCancel, MsgBoxIcon.Powerd_by_GC_Live, null);
                    }
                }
            }
            if (dialog != null)
                dialog.dismiss();
        }
    };
    Handler onlineSearchReadyHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    pd.dismiss();
                    break;
                }
                case 2: {
                    pd.dismiss();
                    MessageBox.show(mainActivity, message, Translation.get("GC_title"), MsgBoxButton.OKCancel, MsgBoxIcon.Powerd_by_GC_Live, null);
                    break;
                }
                case 3: {
                    pd.dismiss();
                    MessageBox.show(mainActivity, message, Translation.get("GC_title"), MsgBoxButton.OKCancel, MsgBoxIcon.Powerd_by_GC_Live, downloadCacheDialogResult);
                    break;
                }
                case 4: {
                    pd.dismiss();
                    downloadCacheDialogResult.onClick(null, -1);
                    break;
                }
            }
        }
    };
    private WebViewClient webViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // handles submitting a form
            if (url.contains("fake://fake.de?GetAttInfo")) {
                // the url is missing the name=value on different devices (perhaps dependant from chromium), so we give that appended to the name and the blank
                int pos = url.indexOf("+"); // the Blank is converted to + in url
                // 25 is the length of "fake://fake.de?GetAttInfo"
                if (pos > 0)
                    MessageBox.show(mainActivity, Translation.get(url.substring(25, pos)));
                return true;
            } else if (url.contains("fake://fake.de?download")) {

                Thread thread = new Thread() {
                    @Override
                    public void run() {

                        fetchMyCacheLimits();
                        if (APIError != 0) {
                            GL.that.toast(LastAPIError);
                            onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
                            return;
                        }
                        if (isDownloadLimitExceeded()) {
                            String s;
                            if (isPremiumMember()) {
                                s = "You have left " + fetchMyUserInfos().remaining + " full and " + fetchMyUserInfos().remainingLite + " lite caches.";
                                s += "The time to wait is " + fetchMyUserInfos().remainingTime + "/" + fetchMyUserInfos().remainingLiteTime;
                            } else {
                                s = "Upgrade to Geocaching.com Premium Membership today\n"
                                        + "for as little at $2.50 per month\n"
                                        + "to download the full details for up to 6000 caches per day,\n"
                                        + "view all cache types in your area,\n"
                                        + "and access many more benefits. \n"
                                        + "Visit Geocaching.com to upgrade.";
                            }
                            message = s;

                            onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(2));

                            return;
                        }

                        if (!isPremiumMember()) {
                            String s = "Download Details of this cache?\n";
                            s += "Full Downloads left: " + fetchMyUserInfos().remaining + "\n";
                            message = s;
                            onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(3));
                        } else {
                            // call the download directly
                            onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(4));
                        }
                    }
                };
                pd = ProgressDialog.show(getContext(), "", "Download Description", true);

                thread.start();

                return true;
            } else if (url.startsWith("http")) {
                // Load Url in ext Browser
                Platform.callUrl(url);
                return true;
            }
            // never reached?
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (mainActivity == null) return;
                    mainActivity.runOnUiThread(() -> scrollTo(0, 0));
                }
            };
            timer.schedule(task, 100);
        }
    };
    public DescriptionViewControl(Context context) {
        super(context);
        mainActivity = (Activity) context;
        cachesDAO = new CachesDAO();
        setDrawingCacheEnabled(false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            setAlwaysDrawnWithCacheEnabled(false);
        }

        getSettings().setLoadWithOverviewMode(true);
        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        /*
         getSettings().setDomStorageEnabled(true);
         getSettings().setLoadsImagesAutomatically(true);
         if (Build.VERSION.SDK_INT >= 21) {
         getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
         }
        */
        setWebViewClient(webViewClient);
        setFocusable(false);
    }

    public DescriptionViewControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        mainActivity = (Activity) context;
        cachesDAO = new CachesDAO();

        setDrawingCacheEnabled(false);
        setAlwaysDrawnWithCacheEnabled(false);

        // getSettings().setJavaScriptEnabled(true);
        getSettings().setLightTouchEnabled(false);
        getSettings().setLoadWithOverviewMode(true);
        getSettings().setSupportZoom(true);
        getSettings().setBuiltInZoomControls(true);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        setWebViewClient(webViewClient);
    }

    public void setCache(final Cache cache) {
        if (cache != null) {
            Log.debug(sClass, "set " + cache.getGeoCacheCode() + " for description");
            if (aktCache == cache) {
                // check maybe new cache values
                Log.debug(sClass, "same Cache " + cache.getGeoCacheCode());
                return;
            }
            aktCache = cache;
            NonLocalImages.clear();
            NonLocalImagesUrl.clear();
            String html = cachesDAO.getShortDescription(cache) + cachesDAO.getDescription(cache);
            // cache.getApiStatus() == Cache.IS_FULL
            if (html.length() > 0) {
                html = DescriptionImageGrabber.resolveImages(cache, html, false, NonLocalImages, NonLocalImagesUrl);
                html = getAttributesHtml(cache) + html;
                // add 2 empty lines so that the last line of description can be selected with the markers
                html += "</br></br>";
            } else {
                // a IS_LITE has no description. a NOT_LIVE ?
                // the action part is (no longer) returned in the url ( in WebViewClientCompat shouldOverrideUrlLoading )
                // so using the input attribute name (here "download")
                // what comes as fake://fake.de?download=+Beschreibung+herunterladen+ in shouldOverrideUrlLoading
                String nodesc = Translation.get("GC_NoDescription");
                html = "</br>" + nodesc + "</br></br></br><form action=\"/download.html\"><input name=\"download\" type=\"submit\" value=\" " + Translation.get("GC_DownloadDescription") + " \"></form>";
            }

            final String finalHtml = html;
            mainActivity.runOnUiThread(() -> {
                try {
                    loadDataWithBaseURL("fake://fake.de", finalHtml, "text/html", "utf-8", null);
                } catch (Exception ignored) {
                }
            });
        }

        try {
            mainActivity.runOnUiThread(() -> {
                if (getSettings() != null)
                    getSettings().setLightTouchEnabled(true);
            });
        } catch (Exception e1) {
            // dann kann eben nicht gezoomt werden!
        }

        // Falls nicht geladene Bilder vorliegen und eine Internetverbindung erlaubt ist, diese laden und Bilder erneut auflösen
        if (NonLocalImagesUrl.size() > 0) {
            new Thread() {
                @Override
                public void run() {

                    /*
                    if (downloadTryCounter > 0) {
                        // Thread.sleep(100);
                    }
                    */

                    boolean anyImagesLoaded = false;
                    while (NonLocalImagesUrl != null && NonLocalImagesUrl.size() > 0) {
                        String local, url;

                        try {
                            local = NonLocalImages.poll();
                            url = NonLocalImagesUrl.poll();
                            Download download = new Download(null, null);
                            if (download.download(url, local)) {
                                anyImagesLoaded = true;
                            }
                        } catch (Exception e) {
                            Log.err(sClass, "setCache()", "downloadThread run()", e);
                        }
                    }
                    if (anyImagesLoaded && downloadReadyHandler != null)
                        downloadReadyHandler.post(() -> {
                            // if (downloadTryCounter < 10) // nur 10 Download versuche zu lassen
                            setCache(aktCache);
                        });
                }
            }.start();
        }

        if (cache != null) {
            cache.loadSpoilerRessources();
        }

        if (cache != null)
            Log.debug(sClass, "set " + cache.getGeoCacheCode() + " finished for description (despite fetching images etc...)");
    }

    private String getAttributesHtml(Cache cache) {
        StringBuilder sb = new StringBuilder();
        try {
            Iterator<Attribute> attrs = cache.getAttributes().iterator();

            if (!attrs.hasNext())
                return "";

            // alternate is perhaps something like
            // <button name="action" value="blue"><img src="blue.png" alt="blue"></button>
            sb.append("<form action=\"Attr\">");
            // syntx <form action="URL"> absolute or relative
            // In HTML5, the action attribute is no longer required.
            do {
                Attribute attribute = attrs.next();
                File attributesImageFile = new File(GlobalCore.workPath + "/data/Attributes/" + attribute.getImageName() + ".png");
                if (attributesImageFile.exists()) {
                    // the url is missing the value, so we give that appended in the name and the blank
                    // String toAppend = "<input name=\"GetAttInfo" + attribute.getImageName() + " \" type=\"image\" src=\"file://" + result.getAbsolutePath() + "\" value=\"1\">";
                    String contentString = FileProvider.getUriForFile(mainActivity, "de.droidcachebox.android.fileprovider", attributesImageFile).toString();
                    String toAppend = "<input name=\"GetAttInfo" + attribute.getImageName() + " \" type=\"image\" src=\"" + contentString + "\" value=\"1\">";
                    sb.append(toAppend);
                } else {
                    Log.err(sClass, "missing file:<input name=\"GetAttInfo" + attribute.getImageName() + " \" type=\"image\" src=\"file://" + attributesImageFile.getAbsolutePath() + "\" value=\"1\">");
                }
            } while (attrs.hasNext());
            sb.append("</form>");

            if (sb.length() > 0)
                sb.append("<br>");
            return sb.toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        getParent();
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean itemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void beforeShowMenu(Menu menu) {
    }

    @Override
    public void onShow() {
        mainActivity.runOnUiThread(() -> {
            if (GlobalCore.isSetSelectedCache()) {
                // aktCache = GlobalCore.getSelectedCache();

                // if (downloadTryCounter > 9) {
                setCache(GlobalCore.getSelectedCache());
                // }
                // downloadTryCounter = 0;

                // im Day Mode brauchen wir kein InvertView
                // das sollte mehr Performance geben
                if (Settings.nightMode.getValue()) {
                    InvertViewControl.Me.setVisibility(VISIBLE);
                } else {
                    InvertViewControl.Me.setVisibility(GONE);
                }

                setWillNotDraw(false);
                invalidate();
            }
        });
    }

    @Override
    public void onHide() {
    }

    @Override
    public void onFree() {
        destroy();
    }

    @Override
    public int getMenuId() {
        return 0;
    }

    @Override
    public int getContextMenuId() {
        return 0;
    }

    @Override
    public boolean contextMenuItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        InvertViewControl.Me.invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

}
