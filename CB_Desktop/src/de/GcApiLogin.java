package de;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

import de.droidcachebox.core.CB_Api;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.gdx.controls.dialogs.RunAndReady;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.utils.http.Request;
import de.droidcachebox.utils.http.Response;
import de.droidcachebox.utils.http.Webb;
import de.droidcachebox.utils.log.Log;

public class GcApiLogin {
    private static final String log = "GcApiLogin";

    private long lastCall = 0;
    private boolean isRunning = false;

    public GcApiLogin() {
    }

    public void runRequest() {
        if (lastCall != 0 && lastCall - System.currentTimeMillis() < 100)
            return;// avoid double call by click
        lastCall = System.currentTimeMillis();
        new CancelWaitDialog("Please Wait", new DownloadAnimation(), new RunAndReady() {
            @Override
            public void ready() {

            }

            @Override
            public void setIsCanceled() {

            }

            @Override
            public void run() {
                String GC_AuthUrl;

                if (Settings.OverrideUrl.getValue().equals("")) {
                    GC_AuthUrl = CB_Api.getGcAuthUrl();
                } else {
                    GC_AuthUrl = Settings.OverrideUrl.getValue();
                }
                GC_AuthUrl = GC_AuthUrl.trim();

                if (GC_AuthUrl.length() == 0) {
                    Log.err(log, "APIUrlNotFound");
                    return;
                }

                try {
                    Log.info("CB_UI GCApiLogin", "Show WebSite " + GC_AuthUrl);
                    callOAuthPage(GC_AuthUrl);
                } catch (Exception e) {
                    Log.err(log, e.getLocalizedMessage());
                }
            }
        }).show();
    }

    private void callOAuthPage(String remote) {
        if (isRunning)
            return;
        isRunning = true;

        Webb httpclient = Webb.create();
        boolean retry = true;
        do {
            try {
                Request request = httpclient.get(remote);
                Response<String> response = request.asString();
                if (response.getStatusCode() == 302) {
                    java.net.CookieManager msCookieManager = new java.net.CookieManager();
                    Map<String, List<String>> headerFields = response.getConnection().getHeaderFields();
                    List<String> cookiesHeader = headerFields.get("Set-Cookie");
                    if (cookiesHeader != null) {
                        for (String cookie : cookiesHeader) {
                            msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                        }
                    }
                    StringBuilder cookies = new StringBuilder();
                    String sep = "";
                    for (java.net.HttpCookie cookie : msCookieManager.getCookieStore().getCookies()) {
                        cookies.append(sep).append(cookie.toString());
                    }
                    if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                        request.header("Cookie", cookies.toString());
                    }
                    remote = response.getHeaderField("location");
                } else retry = false;
            } catch (Exception e) {
                Log.err(log, "Call_OAuth_Page");
                return;
            }
        }
        while (retry);
    }
}
