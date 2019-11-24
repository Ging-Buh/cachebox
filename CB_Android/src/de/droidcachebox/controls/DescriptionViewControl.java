package de.droidcachebox.controls;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import de.droidcachebox.Config;
import de.droidcachebox.GlobalCore;
import de.droidcachebox.PlatformUIBase;
import de.droidcachebox.ViewOptionsMenu;
import de.droidcachebox.database.*;
import de.droidcachebox.ex_import.DescriptionImageGrabber;
import de.droidcachebox.gdx.GL;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxButtons;
import de.droidcachebox.gdx.controls.messagebox.MessageBoxIcon;
import de.droidcachebox.translation.Translation;
import de.droidcachebox.utils.http.Download;
import de.droidcachebox.utils.log.Log;
import de.droidcachebox.views.forms.MessageBox;

import java.io.File;
import java.util.*;

import static de.droidcachebox.core.GroundspeakAPI.*;

public class DescriptionViewControl extends WebView implements ViewOptionsMenu {
    private final static String log = "DescriptionViewControl";
    private static final Handler downloadReadyHandler = new Handler();
    private final static LinkedList<String> NonLocalImages = new LinkedList<>();
    private final static LinkedList<String> NonLocalImagesUrl = new LinkedList<>();
    private static ProgressDialog pd;
    private static DescriptionViewControl that;
    private static Cache aktCache;
    // private static int downloadTryCounter = 0;
    private static final DialogInterface.OnClickListener downloadCacheDialogResult = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int button) {
            switch (button) {
                case -1:
                    Cache newCache;

                    ArrayList<GeoCacheRelated> geoCacheRelateds = updateGeoCache(aktCache);
                    if (geoCacheRelateds.size() > 0) {
                        GeoCacheRelated geoCacheRelated = geoCacheRelateds.get(0);
                        newCache = geoCacheRelated.cache;

                        synchronized (Database.Data.cacheList) {
                            Database.Data.sql.beginTransaction();

                            Database.Data.cacheList.remove(aktCache);
                            Database.Data.cacheList.add(newCache);

                            new CacheDAO().UpdateDatabase(newCache);
                            newCache.setLongDescription("");

                            LogDAO logDAO = new LogDAO();
                            for (LogEntry apiLog : geoCacheRelated.logs) logDAO.WriteToDatabase(apiLog);

                            WaypointDAO waypointDAO = new WaypointDAO();
                            for (int i = 0, n = newCache.waypoints.size(); i < n; i++) {
                                Waypoint waypoint = newCache.waypoints.get(i);

                                boolean update = true;

                                // dont refresh wp if aktCache.wp is user changed
                                for (int j = 0, m = aktCache.waypoints.size(); j < m; j++) {
                                    Waypoint wp = aktCache.waypoints.get(j);
                                    if (wp.getGcCode().equalsIgnoreCase(waypoint.getGcCode())) {
                                        if (wp.IsUserWaypoint)
                                            update = false;
                                        break;
                                    }
                                }

                                if (update)
                                    waypointDAO.WriteToDatabase(waypoint, false);
                            }

                            ImageDAO imageDAO = new ImageDAO();
                            for (ImageEntry image : geoCacheRelated.images) imageDAO.WriteToDatabase(image, false);

                            Database.Data.sql.setTransactionSuccessful();
                            Database.Data.sql.endTransaction();

                            Database.Data.updateCacheCountForGPXFilenames();
                        }
                        aktCache = newCache;
                        setCache(newCache);
                        if (!isPremiumMember()) {
                            String s = "Download successful!\n";
                            fetchMyCacheLimits();
                            s += "Downloads left for today: " + fetchMyUserInfos().remaining + "\n";
                            s += "If you upgrade to Premium Member you are allowed to download the full cache details of 6000 caches per day and you can search not only for traditional caches (www.geocaching.com).";

                            MessageBox.show(staticMainActivity, s, Translation.get("GC_title"), MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live, null);
                        }
                    }

                    break;
            }
            if (dialog != null)
                dialog.dismiss();
        }
    };
    private static String message = "";
    private final static Handler onlineSearchReadyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    pd.dismiss();
                    break;
                }
                case 2: {
                    pd.dismiss();
                    MessageBox.show(staticMainActivity, message, Translation.get("GC_title"), MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live, null);
                    break;
                }
                case 3: {
                    pd.dismiss();
                    MessageBox.show(staticMainActivity, message, Translation.get("GC_title"), MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live, downloadCacheDialogResult);
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
                    MessageBox.show(staticMainActivity, Translation.get(url.substring(25, pos)));
                return true;
            } else if (url.contains("fake://fake.de/download")) {

                Thread thread = new Thread() {
                    @Override
                    public void run() {

                        fetchMyCacheLimits();
                        if (APIError != 0) {
                            GL.that.Toast(LastAPIError);
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
                            return;
                        } else {
                            // call the download directly
                            onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(4));
                            return;
                        }
                    }
                };
                pd = ProgressDialog.show(getContext(), "", "Download Description", true);

                thread.start();

                return true;
            } else if (url.startsWith("http://")) {
                // Load Url in ext Browser
                PlatformUIBase.callUrl(url);
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
                    if (staticMainActivity == null) return;
                    staticMainActivity.runOnUiThread(() -> scrollTo(0, 0));
                }
            };
            timer.schedule(task, 100);
        }
    };

    private Activity mainActivity;
    private static Activity staticMainActivity;

    public DescriptionViewControl(Context context) {
        super(context);
        mainActivity = (Activity) context;
        staticMainActivity = mainActivity;
        this.setDrawingCacheEnabled(false);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            this.setAlwaysDrawnWithCacheEnabled(false);
        }

        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setSupportZoom(true);
        this.getSettings().setBuiltInZoomControls(true);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        this.setWebViewClient(webViewClient);
        that = this;
        this.setFocusable(false);
    }

    public DescriptionViewControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        mainActivity = (Activity) context;
        staticMainActivity = mainActivity;

        this.setDrawingCacheEnabled(false);
        this.setAlwaysDrawnWithCacheEnabled(false);

        // this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setLightTouchEnabled(false);
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setSupportZoom(true);
        this.getSettings().setBuiltInZoomControls(true);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        this.setWebViewClient(webViewClient);
        that = this;
    }

    public static void setCache(final Cache cache) {
        if (cache != null) {
            Log.debug(log, "set " + cache.getGcCode() + " for description");
            if (aktCache == cache) {
                // todo check maybe new cache values
                Log.debug(log, "same Cche " + cache.getGcCode());
                return;
            }
            aktCache = cache;
            NonLocalImages.clear();
            NonLocalImagesUrl.clear();
            String html = Database.getShortDescription(cache) + Database.getDescription(cache);
            // cache.getApiStatus() == Cache.IS_FULL
            if (html.length() > 0) {
                html = DescriptionImageGrabber.resolveImages(cache, html, false, NonLocalImages, NonLocalImagesUrl);
                html = getAttributesHtml(cache) + html;
                // add 2 empty lines so that the last line of description can be selected with the markers
                html += "</br></br>";
            } else {
                // a IS_LITE has no description. a NOT_LIVE ?
                String nodesc = Translation.get("GC_NoDescription");
                html = "</br>" + nodesc + "</br></br></br><form action=\"download\"><input type=\"submit\" value=\" " + Translation.get("GC_DownloadDescription") + " \"></form>";
            }

            final String FinalHtml = html;
            staticMainActivity.runOnUiThread(() -> {
                try {
                    DescriptionViewControl.that.loadDataWithBaseURL("fake://fake.de", FinalHtml, "text/html", "utf-8", null);
                } catch (Exception ignored) {
                }
            });
        }

        try {
            staticMainActivity.runOnUiThread(() -> {
                if (DescriptionViewControl.that.getSettings() != null)
                    DescriptionViewControl.that.getSettings().setLightTouchEnabled(true);
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


                            if (Download.download(url, local)) {
                                anyImagesLoaded = true;
                            }
                        } catch (Exception e) {
                            Log.err(log, "setCache()", "downloadThread run()", e);
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
            Log.debug(log, "set " + cache.getGcCode() + " finished for description (despite fetching images etc...)");
    }

    private static String getAttributesHtml(Cache cache) {
        StringBuilder sb = new StringBuilder();
        try {
            Iterator<Attributes> attrs = cache.getAttributes().iterator();

            if (attrs == null || !attrs.hasNext())
                return "";

            // alternate is perhaps something like
            // <button name="action" value="blue"><img src="blue.png" alt="blue"></button>
            sb.append("<form action=\"Attr\">");
            // syntx <form action="URL"> absolute or relative
            // In HTML5, the action attribute is no longer required.
            do {
                Attributes attribute = attrs.next();
                File result = new File(Config.mWorkPath + "/data/Attributes/" + attribute.getImageName() + ".png");
                // the url is missing the value, so we give that appended in the name and the blank
                sb.append("<input name=\"GetAttInfo" + attribute.getImageName() + " \" type=\"image\" src=\"file://" + result.getAbsolutePath() + "\" value=\"1\">");
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
        this.getParent();
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean ItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void BeforeShowMenu(Menu menu) {
    }

    @Override
    public void OnShow() {
        mainActivity.runOnUiThread(() -> {
            if (GlobalCore.isSetSelectedCache()) {
                // aktCache = GlobalCore.getSelectedCache();

                // if (downloadTryCounter > 9) {
                setCache(GlobalCore.getSelectedCache());
                // }
                // downloadTryCounter = 0;

                // im Day Mode brauchen wir kein InvertView
                // das sollte mehr Performance geben
                if (Config.nightMode.getValue()) {
                    InvertViewControl.Me.setVisibility(VISIBLE);
                } else {
                    InvertViewControl.Me.setVisibility(GONE);
                }

                that.setWillNotDraw(false);
                that.invalidate();
            }
        });
    }

    @Override
    public void OnHide() {
    }

    @Override
    public void OnFree() {
        this.destroy();
    }

    @Override
    public int GetMenuId() {
        return 0;
    }

    @Override
    public int GetContextMenuId() {
        return 0;
    }

    @Override
    public boolean ContextMenuItemSelected(MenuItem item) {
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
