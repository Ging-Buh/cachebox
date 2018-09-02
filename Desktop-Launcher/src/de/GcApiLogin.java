package de;

import CB_Core.Api.CB_Api;
import CB_UI.CB_UI_Settings;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_Utils.Interfaces.cancelRunnable;
import CB_Utils.Log.Log;
import CB_Utils.http.Request;
import CB_Utils.http.Response;
import CB_Utils.http.Webb;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

public class GcApiLogin {
    private static final String log = "GcApiLogin";

    CancelWaitDialog WD;
    private long lastCall = 0;
    private boolean isRunning = false;

    public GcApiLogin() {
    }

    public void RunRequest() {

        if (lastCall != 0 && lastCall - System.currentTimeMillis() < 100)
            return;// entprellen!

        lastCall = System.currentTimeMillis();

        WD = CancelWaitDialog.ShowWait("Please Wait", new IcancelListener() {

            @Override
            public void isCanceled() {
                closeWaitDialog();
            }
        }, new cancelRunnable() {

            @Override
            public void run() {
                runOnWaitDialog();
            }

            @Override
            public boolean cancel() {
                // TODO Handle Cancel
                return false;
            }
        });

    }

    private void closeWaitDialog() {
        WD.close();
    }

    private void runOnWaitDialog() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                // State = 0;
                String GC_AuthUrl;

                if (CB_UI_Settings.OverrideUrl.getValue().equals("")) {
                    GC_AuthUrl = CB_Api.getGcAuthUrl();
                } else {
                    GC_AuthUrl = CB_UI_Settings.OverrideUrl.getValue();
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
                            msCookieManager.getCookieStore().add(null,HttpCookie.parse(cookie).get(0));
                        }
                    }
                    String cookies = "";
                    String sep = "";
                    for ( java.net.HttpCookie cookie : msCookieManager.getCookieStore().getCookies() ) {
                        cookies += sep + cookie.toString();
                    }
                    if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                        request.header("Cookie", cookies);
                    }
                    remote = response.getHeaderField("location");
                }
                else retry = false;
            }
            catch (Exception e) {
                Log.err(log,"Call_OAuth_Page");
                return;
            }
        }
        while (retry);

        // Jetzt haben wir die Login Seite.
        /*
        String page;
        int pos1 = page.indexOf("id=\"__VIEWSTATE\" value=\"") + 24;
        int pos2 = page.indexOf("\"", pos1);
        String Post1 = page.substring(pos1, pos2);

        pos1 = page.indexOf("id=\"__EVENTVALIDATION\" value=\"") + 30;
        pos2 = page.indexOf("\"", pos1);
        String Post2 = page.substring(pos1, pos2);
        */

        // sendPost(remote, Post1, Post2, cookieStore);

    }

    /*
    private void sendPost(String remote, String Post1, String Post2, CookieStore cookieStore) {

        try {
            // Add your data
            // Bind custom cookie store to the local context
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", Post1));
            nameValuePairs.add(new BasicNameValuePair("__EVENTVALIDATION", Post2));
            nameValuePairs.add(new BasicNameValuePair("uxAuthorizationButton", "Get+Authorization"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


            Request request = Webb.create().post(remote).ensureSuccess()
                    ;

            Response<String> response = request.asString();
            String page = response.getBody();

            if (page.contains("<input name=\"ctl00$ContentBody$uxUserName\""))
            {
                // Url = "https://www.geocaching.com/mobileoauth/SignIn.aspx?&redir=http%3a%2f%2fwww.geocaching.com%2foauth%2fMobileAuthorize.aspx%3flocale%3den-US&pc=Team+CacheBox&pa=CacheBox+for+Android&pg=8edfa2c9-e2d1-474c-9cbc-22fde4debfe8";

                AskForUserPW(remote, cookieStore, page);
            } else {
                if (page.contains("moved")) {
                    int pos1 = page.indexOf("Object moved to <a href=\"") + 25;
                    int pos2 = page.indexOf("\"", pos1);
                    Url = page.substring(pos1, pos2);

                    nextStep(Url, cookieStore);
                } else {
                    // page empty, possibly moved
                }
            }

        } catch (Exception e) {

        }

    }

    private void nextStep(final String remote, final CookieStore cookieStore) {
        try {

            Request request = Webb.create().post(remote).ensureSuccess()
                    ;

            Response<String> response = request.asString();
            String page = response.getBody();

            AskForUserPW(remote, cookieStore, page); // ?

        } catch (Exception e) {
            Log(log,e.getLocalizedMessage());
            closeWaitDialog();
        }

    }

    private void AskForUserPW(final String Url, final CookieStore cookieStore, String page) {
        // now we have the LogIn Page

        // we neat the __VIEWSTATE
        // final String ViewState = "";

        // id="__VIEWSTATE" value="
        int pos1 = page.indexOf("id=\"__VIEWSTATE\" value=\"") + 24;
        int pos2 = page.indexOf("\"", pos1);
        final String ViewState1 = page.substring(pos1, pos2);

        closeWaitDialog();

        // Ask for User/PW

        final PasswortDialog PWD = new PasswortDialog(new IReturnListener() {

            @Override
            public void returnFromPW_Dialog(String User, String PW) {
                if (User != null && PW != null) {
                    nextStep2(Url, ViewState1, cookieStore, User, PW);
                }
            }
        });

        GL.that.RunOnGL(new IRunOnGL() {

            @Override
            public void run() {
                GL.that.showDialog(PWD, true);
            }
        });
    }

    private void nextStep2(final String Url, final String viewstate, final CookieStore cookieStore, final String User, final String PW) {

        WD = CancelWaitDialog.ShowWait("Please Wait", new IcancelListener() {

            @Override
            public void isCanceled() {
                closeWaitDialog();
            }
        }, new cancelRunnable() {

            @Override
            public void run() {
                runOnWaitDialog(Url, viewstate, cookieStore, User, PW);
            }

            @Override
            public boolean cancel() {
                // TODO Handle Cancel
                return false;
            }
        });

    }

    private void runOnWaitDialog(String Url, String viewstate, CookieStore cookieStore, String User, String PW) {
        System.out.println("URL= " + Url);

        // fill the Inputs and press Sign Button!!!!

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(Url);

        ((AbstractHttpClient) httpclient).setCookieStore(cookieStore);

        // Create local HTTP context
        HttpContext localContext = new BasicHttpContext();
        // Bind custom cookie store to the local context
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        StringBuilder builder = new StringBuilder();
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
            nameValuePairs.add(new BasicNameValuePair("__EVENTTARGET", " "));
            nameValuePairs.add(new BasicNameValuePair("__EVENTARGUMENT", " "));
            nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", viewstate));
            nameValuePairs.add(new BasicNameValuePair("ctl00$ContentBody$uxUserName", User));
            nameValuePairs.add(new BasicNameValuePair("ctl00$ContentBody$uxPassword", PW));
            nameValuePairs.add(new BasicNameValuePair("ctl00$ContentBody$uxLogin", "Sign+In"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost, localContext);
            System.out.println("Send Auth info User/PW");
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
            } catch (Exception e) {

                e.printStackTrace();
            }

        } catch (ClientProtocolException e) {

        } catch (IOException e) {

        }

        String page = builder.toString();

        // Jetzt haben wir die Anfrage abgeschickt und wir müssen die Abhol Seite Aufrufen
        // Wenn wir als Rückgabe eine Move to URl Seite bekommen! Ansonsten ist etwas schief gegeangen!
        // http://www.geocaching.com/oauth/MobileAuthorize.aspx?locale=en-US
        if (page.contains("moved")) {

            int pos1 = page.indexOf("Object moved to <a href=\"") + 25;
            int pos2 = page.indexOf("\"", pos1);
            String moveUrl = page.substring(pos1, pos2);

            if (moveUrl.startsWith("/")) {
                moveUrl = "https://" + httppost.getURI().getHost() + moveUrl;
            }

            moveUrl = moveUrl.replace("amp;", "");
            System.out.println("Call Page allow access");
            callPageAllowAccess(moveUrl, viewstate, cookieStore);
        } else {
            String Token = "";

            // id="__VIEWSTATE" value="
            int pos1 = page.indexOf("id=\"ctl00_ContentPlaceHolder1_OAuthAuthorizationSecToken\" value=\"") + 65;
            int pos2 = page.indexOf("\"", pos1);
            Token = page.substring(pos1, pos2);
            clickAllowAccess(Url, viewstate, Token, cookieStore);
        }
    }

    private void callPageAllowAccess(String Url, String viewstate, CookieStore cookieStore) {
        System.out.println("URL= " + Url);

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(Url);

        ((AbstractHttpClient) httpclient).setCookieStore(cookieStore);

        // Create local HTTP context
        HttpContext localContext = new BasicHttpContext();
        // Bind custom cookie store to the local context
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        StringBuilder builder = new StringBuilder();
        try {

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost, localContext);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
            } catch (Exception e) {

                e.printStackTrace();
            }

        } catch (ClientProtocolException e) {

        } catch (IOException e) {

        }

        String page = builder.toString();

        // we neat the __VIEWSTATE
        String ViewState = "";

        // id="__VIEWSTATE" value="
        int pos1 = page.indexOf("id=\"__VIEWSTATE\" value=\"") + 24;
        int pos2 = page.indexOf("\"", pos1);
        ViewState = page.substring(pos1, pos2);

        // we neat the OAuthAuthorizationSecToken
        String Token = "";

        // id="__VIEWSTATE" value="
        pos1 = page.indexOf("id=\"ctl00_ContentPlaceHolder1_OAuthAuthorizationSecToken\" value=\"") + 65;
        pos2 = page.indexOf("\"", pos1);
        Token = page.substring(pos1, pos2);

        clickAllowAccess(Url, ViewState, Token, cookieStore);
    }

    private void clickAllowAccess(String Url, String viewstate, String Token, CookieStore cookieStore) {
        System.out.println("URL= " + Url);

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(Url);

        ((AbstractHttpClient) httpclient).setCookieStore(cookieStore);

        // Create local HTTP context
        HttpContext localContext = new BasicHttpContext();
        // Bind custom cookie store to the local context
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        StringBuilder builder = new StringBuilder();
        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", viewstate));
            nameValuePairs.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$uxAllowAccessButton", "Allow+Access"));
            nameValuePairs.add(new BasicNameValuePair("ctl00$ContentPlaceHolder1$OAuthAuthorizationSecToken", Token));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost, localContext);
            System.out.println("Click Allow Access");
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
            } catch (Exception e) {

                e.printStackTrace();
            }

        } catch (ClientProtocolException e) {

        } catch (IOException e) {

        }

        String page = builder.toString();

        if (page.contains("moved")) {

            int pos1 = page.indexOf("Object moved to <a href=\"") + 25;
            int pos2 = page.indexOf("\"", pos1);
            String moveUrl = page.substring(pos1, pos2);

            if (moveUrl.startsWith("/")) {
                moveUrl = "https://" + httppost.getURI().getHost() + moveUrl;
            }

            moveUrl = moveUrl.replace("amp;", "");
            System.out.println("Call Page allow access");
            FinalPageOnGeoPt(moveUrl, cookieStore);
        } else {
            // Fehler Aufgetreten!
        }

    }

    private void FinalPageOnGeoPt(String Url, CookieStore cookieStore) {
        System.out.println("URL= " + Url);

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(Url);

        ((AbstractHttpClient) httpclient).setCookieStore(cookieStore);

        // Create local HTTP context
        HttpContext localContext = new BasicHttpContext();
        // Bind custom cookie store to the local context
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        StringBuilder builder = new StringBuilder();
        try {

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httpget, localContext);

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line = "";
            try {
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
            } catch (Exception e) {

                e.printStackTrace();
            }

        } catch (ClientProtocolException e) {

        } catch (IOException e) {

        }

        String html = builder.toString();

        String search = "Access token: ";
        int pos = html.indexOf(search);
        if (pos < 0)
            return;
        int pos2 = html.indexOf("</span>", pos);
        if (pos2 < pos)
            return;
        // zwischen pos und pos2 sollte ein gültiges AccessToken sein!!!
        final String accessToken = html.substring(pos + search.length(), pos2);

        // store the encrypted AccessToken in the Config file
        // wir bekommen den Key schon verschlüsselt, deshalb muss er
        // nicht noch einmal verschlüsselt werden!
        Config.AccessToken.setEncryptedValue(accessToken);
        Config.AcceptChanges();
        Config.GcLogin.setValue(GroundspeakAPI.fetchMemberNameAfterGotNewAccessToken());
        Config.AcceptChanges();

        closeWaitDialog();

        SettingsActivity.resortList();

    }
    */
}
