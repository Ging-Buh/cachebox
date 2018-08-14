package de.droidcachebox.Custom_Controls;

import CB_Core.Api.GroundspeakAPI;
import CB_Core.Attributes;
import CB_Core.Database;
import CB_Core.Import.DescriptionImageGrabber;
import CB_Core.Types.Cache;
import CB_Translation_Base.TranslationEngine.Translation;
import CB_UI.Config;
import CB_UI.GL_UI.Controls.PopUps.ApiUnavailable;
import CB_UI.GlobalCore;
import CB_UI.SearchForGeocaches;
import CB_UI_Base.Events.PlatformConnector;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxButtons;
import CB_UI_Base.GL_UI.Controls.MessageBox.MessageBoxIcon;
import CB_UI_Base.GL_UI.Controls.PopUps.ConnectionError;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import de.droidcachebox.Events.ViewOptionsMenu;
import de.droidcachebox.Views.Forms.MessageBox;
import de.droidcachebox.main;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("deprecation")
public class DescriptionViewControl extends WebView implements ViewOptionsMenu {
    final static org.slf4j.Logger log = LoggerFactory.getLogger(DescriptionViewControl.class);
    public static boolean isDrawn = false;
    private static ProgressDialog pd;
    private static DescriptionViewControl that;
    final Handler downloadReadyHandler = new Handler();
    private final LinkedList<String> NonLocalImages = new LinkedList<String>();
    private final LinkedList<String> NonLocalImagesUrl = new LinkedList<String>();
    Thread downloadThread;
    private Cache aktCache;
    private String message = "";
    WebViewClient clint = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("fake://fake.de/Attr")) {
                int pos = url.indexOf("+");
                if (pos < 0)
                    return true;

                final String attr = url.substring(pos + 1, url.length() - 1);

                MessageBox.Show(Translation.Get(attr));
                return true;
            } else if (url.contains("fake://fake.de?Button")) {
                int pos = url.indexOf("+");
                if (pos < 0)
                    return true;

                final String attr = url.substring(pos + 1, url.length() - 1);

                MessageBox.Show(Translation.Get(attr));
                return true;
            } else if (url.contains("fake://fake.de/download")) {

                Thread thread = new Thread() {
                    @Override
                    public void run() {

                        if (!GroundspeakAPI.CacheStatusValid) {
                            int result = GroundspeakAPI.GetCacheLimits(null);
                            if (result != 0) {
                                onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
                                return;
                            }

                            if (result == GroundspeakAPI.CONNECTION_TIMEOUT) {
                                GL.that.Toast(ConnectionError.INSTANCE);
                                return;
                            }
                            if (result == GroundspeakAPI.API_IS_UNAVAILABLE) {
                                GL.that.Toast(ApiUnavailable.INSTANCE);
                                return;
                            }
                        }
                        if (GroundspeakAPI.CachesLeft <= 0) {
                            String s = "Download limit is reached!\n";
                            s += "You have downloaded the full cache details of " + GroundspeakAPI.MaxCacheCount + " caches in the last 24 hours.\n";
                            if (GroundspeakAPI.MaxCacheCount < 10)
                                s += "If you want to download the full cache details of 6000 caches per day you can upgrade to Premium Member at \nwww.geocaching.com!";

                            message = s;

                            onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(2));

                            return;
                        }

                        if (!GroundspeakAPI.IsPremiumMember()) {
                            String s = "Download Details of this cache?\n";
                            s += "Full Downloads left: " + GroundspeakAPI.CachesLeft + "\n";
                            s += "Actual Downloads: " + GroundspeakAPI.CurrentCacheCount + "\n";
                            s += "Max. Downloads in 24h: " + GroundspeakAPI.MaxCacheCount;
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
                PlatformConnector.callUrl(url);
                return true;
            }
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
                    if (main.mainActivity == null) return;
                    main.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scrollTo(0, 0);
                        }
                    });
                }
            };
            timer.schedule(task, 100);

        }

    };
    private int downloadTryCounter = 0;
    private final DialogInterface.OnClickListener DownloadCacheDialogResult = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int button) {
            switch (button) {
                case -1:
                    Cache newCache = SearchForGeocaches.getInstance().LoadApiDetails(aktCache, null);
                    if (newCache != null) {
                        aktCache = newCache;
                        setCache(newCache);

                        if (!GroundspeakAPI.IsPremiumMember()) {
                            String s = "Download successful!\n";
                            s += "Downloads left for today: " + GroundspeakAPI.CachesLeft + "\n";
                            s += "If you upgrade to Premium Member you are allowed to download the full cache details of 6000 caches per day and you can search not only for traditional caches (www.geocaching.com).";

                            MessageBox.Show(s, Translation.Get("GC_title"), MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live, null);
                        }
                    }
                    break;
            }
            if (dialog != null)
                dialog.dismiss();
        }
    };
    private final Handler onlineSearchReadyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    pd.dismiss();
                    break;
                }
                case 2: {
                    pd.dismiss();
                    MessageBox.Show(message, Translation.Get("GC_title"), MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live, null);
                    break;
                }
                case 3: {
                    pd.dismiss();
                    MessageBox.Show(message, Translation.Get("GC_title"), MessageBoxButtons.OKCancel, MessageBoxIcon.Powerd_by_GC_Live, DownloadCacheDialogResult);
                    break;
                }
                case 4: {
                    pd.dismiss();
                    DownloadCacheDialogResult.onClick(null, -1);
                    break;
                }
            }
        }
    };
    final Runnable downloadComplete = new Runnable() {
        @Override
        public void run() {
            if (downloadTryCounter < 10) // nur 10 Download versuche zu lassen
                setCache(aktCache);
        }
    };

    public DescriptionViewControl(Context context) {
        super(context);

        this.setDrawingCacheEnabled(false);
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            this.setAlwaysDrawnWithCacheEnabled(false);
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            this.getSettings().setLightTouchEnabled(false);
        }
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setSupportZoom(true);
        this.getSettings().setBuiltInZoomControls(true);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        this.setWebViewClient(clint);
        that = this;
        this.setFocusable(false);
    }

    public DescriptionViewControl(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setDrawingCacheEnabled(false);
        this.setAlwaysDrawnWithCacheEnabled(false);

        // this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setLightTouchEnabled(false);
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setSupportZoom(true);
        this.getSettings().setBuiltInZoomControls(true);
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        this.setWebViewClient(clint);
        that = this;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        this.getParent();
        return super.dispatchTouchEvent(event);
    }

    public void setCache(final Cache cache) {
        final String mimeType = "text/html";
        final String encoding = "utf-8";
        if (cache != null) {
            if (aktCache == cache) {
                // todo check maybe new cache values
                return;
            }
            NonLocalImages.clear();
            NonLocalImagesUrl.clear();
            String cachehtml = Database.GetShortDescription(cache) + Database.GetDescription(cache);
            String html = "";
            if (cache.getApiStatus() == 1)// GC.com API lite
            { // Load Standard HTML
                String nodesc = Translation.Get("GC_NoDescription");
                html = "</br>" + nodesc + "</br></br></br><form action=\"download\"><input type=\"submit\" value=\" " + Translation.Get("GC_DownloadDescription") + " \"></form>";
            } else {
                html = DescriptionImageGrabber.ResolveImages(cache, cachehtml, false, NonLocalImages, NonLocalImagesUrl);

                if (!Config.DescriptionNoAttributes.getValue())
                    html = getAttributesHtml(cache) + html;

                // add 2 empty lines so that the last line of description can be selected with the markers
                html += "</br></br>";
            }

            final String FinalHtml = html;

            main.mainActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        DescriptionViewControl.this.loadDataWithBaseURL("fake://fake.de", FinalHtml, mimeType, encoding, null);
                    } catch (Exception e) {
                        return; // if an exception here, then this is not initializes
                    }
                }
            });

        }

        try {
            main.mainActivity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (DescriptionViewControl.this.getSettings() != null)
                        DescriptionViewControl.this.getSettings().setLightTouchEnabled(true);
                }
            });

        } catch (Exception e1) {
            // dann kann eben nicht gezoomt werden!
        }

        // Falls nicht geladene Bilder vorliegen und eine Internetverbindung erlaubt ist, diese laden und Bilder erneut auflösen
        if (NonLocalImagesUrl.size() > 0) {
            downloadThread = new Thread() {
                @Override
                public void run() {

                    if (downloadTryCounter > 0) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            log.error("DescriptionViewControl.setCache()", "Thread.sleep fehler", e);
                            e.printStackTrace();
                        }
                    }

                    boolean anyImagesLoaded = false;
                    while (NonLocalImagesUrl != null && NonLocalImagesUrl.size() > 0) {
                        String local, url;

                        try {
                            local = NonLocalImages.poll();
                            url = NonLocalImagesUrl.poll();


                            if (DescriptionImageGrabber.Download(url, local)) {
                                anyImagesLoaded = true;
                            }
                        } catch (Exception e) {
                            log.error("DescriptionViewControl.setCache()", "downloadThread run()", e);
                        }
                    }
                    if (anyImagesLoaded && downloadReadyHandler != null)
                        downloadReadyHandler.post(downloadComplete);
                }
            };
            downloadThread.start();
        }

        if (cache != null) {
            cache.loadSpoilerRessources();
        }
    }

    private String getAttributesHtml(Cache cache) {
        StringBuilder sb = new StringBuilder();
        try {
            Iterator<Attributes> attrs = cache.getAttributes().iterator();

            if (attrs == null || !attrs.hasNext())
                return "";

            do {
                Attributes attribute = attrs.next();
                File result = new File(Config.mWorkPath + "/data/Attributes/" + attribute.getImageName() + ".png");

                sb.append("<form action=\"Attr\">");
                sb.append("<input name=\"Button\" type=\"image\" src=\"file://" + result.getAbsolutePath() + "\" value=\" " + attribute.getImageName() + " \">");
            } while (attrs.hasNext());

            sb.append("</form>");

            if (sb.length() > 0)
                sb.append("<br>");
            return sb.toString();
        } catch (Exception ex) {
            // TODO Handle Exception
            return "";
        }
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
        main.mainActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (GlobalCore.isSetSelectedCache()) {
                    aktCache = GlobalCore.getSelectedCache();

                    if (downloadTryCounter > 9) {
                        setCache(aktCache);
                    }
                    downloadTryCounter = 0;

                    // im Day Mode brauchen wir kein InvertView
                    // das sollte mehr Performance geben
                    if (Config.nightMode.getValue()) {
                        invertViewControl.Me.setVisibility(VISIBLE);
                    } else {
                        invertViewControl.Me.setVisibility(GONE);
                    }

                    that.setWillNotDraw(false);
                    that.invalidate();
                }
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
    public void ActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public int GetContextMenuId() {
        return 0;
    }

    @Override
    public void BeforeShowContextMenu(Menu menu) {
    }

    @Override
    public boolean ContextMenuItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        isDrawn = true;
        invertViewControl.Me.invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

}
