package de.droidcachebox.activities;

import static de.droidcachebox.core.GroundspeakAPI.fetchMyUserInfos;
import static de.droidcachebox.core.GroundspeakAPI.setAuthorization;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import de.droidcachebox.Global;
import de.droidcachebox.Main;
import de.droidcachebox.R;
import de.droidcachebox.core.CB_Api;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.utils.ActivityUtils;
import de.droidcachebox.utils.log.Log;


public class GcApiLogin extends Activity {
    private static final String sClass = "GcApiLogin";
    private static ProgressDialog progressDialog;
    private static boolean progressDialogIsShown = false;
    private LinearLayout webViewLayout;
    private WebView webView;
    private final Handler onlineSearchReadyHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    progressDialog.dismiss();
                    finish();
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gcapilogin);

        webViewLayout = findViewById(R.id.gal_Layout);

        new RetreiveFeedTask().execute();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

    private void ShowWebsite(String GC_AuthUrl) {
        // Initial new VebView Instanz

        webView = findViewById(R.id.gal_WebView);

        webViewLayout.removeAllViews();
        if (webView != null) {
            webView.destroy();
            webView = null;
        }

        // Instanz new WebView
        webView = new WebView(Main.getInstance(), null, android.R.attr.webViewStyle);
        webView.requestFocus(View.FOCUS_DOWN);
        webView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_UP:
                    if (!v.hasFocus()) {
                        v.requestFocus();
                    }
                    break;
            }
            return false;
        });
        webViewLayout.addView(webView);

        if (!progressDialogIsShown) {
            runOnUiThread(() -> {
                progressDialog = ProgressDialog.show(this, "", "Loading....", true);
                progressDialogIsShown = true;
            });

        }

        View titleView = getWindow().findViewById(android.R.id.title);
        if (titleView != null) {
            ViewParent parent = titleView.getParent();
            if (parent != null && (parent instanceof View)) {
                View parentView = (View) parent;
                parentView.setBackgroundColor(Global.getColor(R.attr.TitleBarBackColor));
            }
        }

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setTitle("Loading...");

                if (!progressDialogIsShown) {
                    runOnUiThread(() -> {
                        progressDialog = ProgressDialog.show(GcApiLogin.this, "", "Loading....", true);
                        progressDialogIsShown = true;
                    });

                }

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http:"))
                    url = url.replace("http:", "https:");
                view.loadUrl(url);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String url = request.getUrl().toString();
                    if (url.startsWith("http:"))
                        url = url.replace("http:", "https:");
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setTitle(R.string.app_name);
                if (progressDialog != null)
                    progressDialog.dismiss();
                progressDialogIsShown = false;

                if (url.toLowerCase().contains("oauth_verifier=") && (url.toLowerCase().contains("oauth_token="))) {
                    webView.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                } else
                    super.onPageFinished(view, url);
            }

        });

        WebSettings settings = webView.getSettings();

        // settings.setPluginsEnabled(true);
        settings.setJavaScriptEnabled(true);
        // settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // webView.setWebChromeClient(new WebChromeClient());


        //delete cookies and cache
        webView.clearCache(true);
        webView.clearHistory();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.debug("GcApiLogin", "Using clearCookies code for API >=" + Build.VERSION_CODES.LOLLIPOP_MR1);
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            Log.debug("GcApiLogin", "Using clearCookies code for API <" + Build.VERSION_CODES.LOLLIPOP_MR1);
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(this);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

        webView.loadUrl(GC_AuthUrl);
    }

    class RetreiveFeedTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                String GC_AuthUrl;

                if (Settings.OverrideUrl.getValue().equals("")) {
                    GC_AuthUrl = CB_Api.getGcAuthUrl();
                } else {
                    GC_AuthUrl = Settings.OverrideUrl.getValue();
                }

                if (GC_AuthUrl.equals("")) {
                    finish();
                }

                return GC_AuthUrl;
            } catch (Exception ex) {
                Log.err(sClass, "", ex);
                return "";
            }
        }

        @Override
        protected void onPostExecute(String GC_AuthUrl) {
            try {
                Log.debug("Forms GCApiLogin", "Show WebSite " + GC_AuthUrl);
                ShowWebsite(GC_AuthUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    class MyJavaScriptInterface {

        @JavascriptInterface
        public void showHTML(String html) {

            String search = "Access token: ";
            int pos = html.indexOf(search);
            if (pos < 0)
                return;
            int pos2 = html.indexOf("</span>", pos);
            if (pos2 < pos)
                return;
            // zwischen pos und pos2 sollte ein gültiges AccessToken sein!!!
            final String accessToken = html.substring(pos + search.length(), pos2);

            Thread thread = new Thread() {
                public void run() {
                    // store the encrypted AccessToken in the Config file
                    // wir bekommen den Key schon verschlüsselt, deshalb muss er
                    // nicht noch einmal verschlüsselt werden!
                    if (Settings.UseTestUrl.getValue()) {
                        Settings.AccessTokenForTest.setEncryptedValue(accessToken);
                    } else {
                        Settings.AccessToken.setEncryptedValue(accessToken);
                    }
                    setAuthorization();
                    String userNameOfAuthorization = fetchMyUserInfos().username;
                    Log.debug(sClass, "userNameOfAuthorization: " + userNameOfAuthorization);
                    Settings.GcLogin.setValue(userNameOfAuthorization);
                    Settings.getInstance().acceptChanges();
                    onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
                }
            };
            runOnUiThread(() -> progressDialog = ProgressDialog.show(GcApiLogin.this, "", "Download Username", true));

            thread.start();
        }
    }
}
