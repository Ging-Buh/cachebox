package de.droidcachebox.Views.Forms;

import CB_Core.Api.CB_Api;
import CB_Core.CB_Core_Settings;
import CB_UI.Config;
import CB_Utils.Log.Log;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.*;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.webkit.*;
import android.widget.LinearLayout;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;
import de.droidcachebox.main;

import static CB_Core.Api.GroundspeakAPI.fetchMyUserInfos;
import static CB_Core.Api.GroundspeakAPI.setAuthorization;
import static CB_Core.CB_Core_Settings.GcLogin;


public class GcApiLogin extends Activity {
    private static final String sKlasse = "GcApiLogin";
    private static GcApiLogin gcApiLogin;
    private static ProgressDialog pd;
    private static boolean pdIsShow = false;
    final String javaScript = "javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');";
    private LinearLayout webViewLayout;
    private WebView WebControl;
    private Handler onlineSearchReadyHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1: {
                    pd.dismiss();
                    gcApiLogin.finish();
                }
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        ActivityUtils.onActivityCreateSetTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gcapilogin);

        webViewLayout = findViewById(R.id.gal_Layout);

        gcApiLogin = this;

        new RetreiveFeedTask().execute();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
        pd = null;
    }

    private void ShowWebsite(String GC_AuthUrl) {
        // Initial new VebView Instanz

        WebControl = (WebView) gcApiLogin.findViewById(R.id.gal_WebView);

        webViewLayout.removeAllViews();
        if (WebControl != null) {
            WebControl.destroy();
            WebControl = null;
        }

        // Instanz new WebView
        WebControl = new WebView(main.mainActivity, null, android.R.attr.webViewStyle);
        WebControl.requestFocus(View.FOCUS_DOWN);
        WebControl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
        webViewLayout.addView(WebControl);

        if (!pdIsShow) {
            gcApiLogin.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    pd = ProgressDialog.show(gcApiLogin, "", "Loading....", true);
                    pdIsShow = true;
                }
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

        WebControl.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                gcApiLogin.setTitle("Loading...");

                if (!pdIsShow) {
                    gcApiLogin.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            pd = ProgressDialog.show(GcApiLogin.this, "", "Loading....", true);
                            pdIsShow = true;
                        }
                    });

                }

                super.onPageStarted(view, url, favicon);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                gcApiLogin.setTitle(R.string.app_name);
                if (pd != null)
                    pd.dismiss();
                pdIsShow = false;

                if (url.toLowerCase().contains("oauth_verifier=") && (url.toLowerCase().contains("oauth_token="))) {
                    WebControl.loadUrl(javaScript);
                } else
                    super.onPageFinished(view, url);
            }

        });

        WebSettings settings = WebControl.getSettings();

        // settings.setPluginsEnabled(true);
        settings.setJavaScriptEnabled(true);
        // settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // webView.setWebChromeClient(new WebChromeClient());


        {//delete cookies and cache
            WebControl.clearCache(true);
            WebControl.clearHistory();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                Log.debug("GcApiLogin", "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();
            } else {
                Log.debug("GcApiLogin", "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
                CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(this);
                cookieSyncMngr.startSync();
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();
                cookieManager.removeSessionCookie();
                cookieSyncMngr.stopSync();
                cookieSyncMngr.sync();
            }
        }

        WebControl.getSettings().setJavaScriptEnabled(true);
        WebControl.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

        WebControl.loadUrl(GC_AuthUrl);
    }

    class RetreiveFeedTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                String GC_AuthUrl;

                if (Config.OverrideUrl.getValue().equals("")) {
                    GC_AuthUrl = CB_Api.getGcAuthUrl();
                } else {
                    GC_AuthUrl = Config.OverrideUrl.getValue();
                }

                if (GC_AuthUrl.equals("")) {
                    finish();
                }

                return GC_AuthUrl;
            } catch (Exception e) {
                Log.err(sKlasse, "", e);
                return "";
            }
        }

        @Override
        protected void onPostExecute(String GC_AuthUrl) {
            try {
                Log.info("Forms GCApiLogin", "Show WebSite " + GC_AuthUrl);
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
                    if (CB_Core_Settings.UseTestUrl.getValue()) {
                        CB_Core_Settings.AccessTokenForTest.setEncryptedValue(accessToken);
                    } else {
                        CB_Core_Settings.AccessToken.setEncryptedValue(accessToken);
                    }
                    setAuthorization();
                    String userNameOfAuthorization = fetchMyUserInfos().username;
                    Log.debug(sKlasse, "userNameOfAuthorization: " + userNameOfAuthorization);
                    GcLogin.setValue(userNameOfAuthorization);
                    Config.AcceptChanges();
                    onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
                }
            };
            gcApiLogin.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pd = ProgressDialog.show(gcApiLogin, "", "Download Username", true);
                }
            });

            thread.start();
        }
    }
}
