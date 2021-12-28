package de;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

import de.droidcachebox.core.CB_Api;
import de.droidcachebox.gdx.controls.animation.DownloadAnimation;
import de.droidcachebox.gdx.controls.dialogs.CancelWaitDialog;
import de.droidcachebox.settings.Settings;
import de.droidcachebox.utils.TestCancelRunnable;
import de.droidcachebox.utils.http.Request;
import de.droidcachebox.utils.http.Response;
import de.droidcachebox.utils.http.Webb;
import de.droidcachebox.utils.log.Log;

public class GcApiLogin {
    private static final String log = "GcApiLogin";

    CancelWaitDialog wd;
    private long lastCall = 0;
    private boolean isRunning = false;

    public GcApiLogin() {
    }

    public void RunRequest() {

        if (lastCall != 0 && lastCall - System.currentTimeMillis() < 100)
            return;// entprellen!

        lastCall = System.currentTimeMillis();

        wd = new CancelWaitDialog("Please Wait", new DownloadAnimation(), () -> closeWaitDialog(), new TestCancelRunnable() {

            @Override
            public void run() {
                runOnWaitDialog();
            }

            @Override
            public boolean checkCanceled() {
                return false;
            }
        });
        wd.show();

    }

    private void closeWaitDialog() {
        wd.close();
    }

    private void runOnWaitDialog() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                // State = 0;
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
                    Call_OAuth_Page(GC_AuthUrl);
                } catch (Exception e) {
                    Log.err(log, e.getLocalizedMessage());
                }
            }
        });
        t.start();
    }

    private void Call_OAuth_Page(String remote) throws IOException {
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
                    String cookies = "";
                    String sep = "";
                    for (java.net.HttpCookie cookie : msCookieManager.getCookieStore().getCookies()) {
                        cookies += sep + cookie.toString();
                    }
                    if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                        request.header("Cookie", cookies);
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
