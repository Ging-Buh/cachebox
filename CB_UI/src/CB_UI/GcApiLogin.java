package CB_UI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import CB_Core.Api.CB_Api;
import CB_Core.Api.GroundspeakAPI;
import CB_UI.GL_UI.Activitys.settings.SettingsActivity;
import CB_UI.GL_UI.Controls.Dialogs.PasswortDialog;
import CB_UI.GL_UI.Controls.Dialogs.PasswortDialog.IReturnListener;
import CB_UI_Base.GL_UI.IRunOnGL;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog;
import CB_UI_Base.GL_UI.Controls.Dialogs.CancelWaitDialog.IcancelListener;
import CB_UI_Base.GL_UI.GL_Listener.GL;
import CB_Utils.Interfaces.cancelRunnable;

public class GcApiLogin {

	public static GcApiLogin that;

	public static int ERROR_API_URL_NOT_FOUND = 1;

	public static int STATE_GET_API_URL = 1;
	public static int STATE_GET_OAUTH_PAGE = 1;

	// private int State = 0;
	//
	// private int Error = 0;

	public GcApiLogin() {
		that = this;

	}

	CancelWaitDialog WD;

	private long lastCall = 0;

	public void RunRequest() {

		if (lastCall != 0 && lastCall - System.currentTimeMillis() < 100)
			return;// entprellen!

		lastCall = System.currentTimeMillis();

		WD = CancelWaitDialog.ShowWait("Please Wait", new IcancelListener() {

			@Override
			public void isCanceld() {
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

				if (GC_AuthUrl.equals("")) {
					// Error = ERROR_API_URL_NOT_FOUND;
					return;
				}

				try {
					Call_OAuth_Page(GC_AuthUrl);
				} catch (ClientProtocolException e) {

					e.printStackTrace();
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	Cookie GeoPtCookie = null;

	private boolean isRunning = false;

	private void Call_OAuth_Page(String URL) throws ClientProtocolException, IOException {
		if (isRunning)
			return;
		isRunning = true;

		HttpClient httpclient = new DefaultHttpClient();

		// Create a local instance of cookie store
		CookieStore cookieStore = new BasicCookieStore();

		// Create local HTTP context
		HttpContext localContext = new BasicHttpContext();
		// Bind custom cookie store to the local context
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		HttpGet httpget = new HttpGet(URL);

		System.out.println("executing request " + httpget.getURI());

		// Pass local context as a parameter
		HttpResponse response = httpclient.execute(httpget, localContext);
		HttpEntity entity = response.getEntity();

		List<Cookie> cookies = cookieStore.getCookies();
		for (int i = 0; i < cookies.size(); i++) {
			System.out.println("Local cookie: " + cookies.get(i));
			if (cookies.get(i).getDomain().equalsIgnoreCase("geopt.sytes.net"))
				GeoPtCookie = cookies.get(i);
		}

		if (entity == null)
			return;

		// get ACB OAuth Page
		// EntityUtils.consume(entity);
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		StringBuilder builder = new StringBuilder();
		String line = "";
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line).append("\n");
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		String Post1 = "";
		String Post2 = "";

		String page = builder.toString();

		int pos1 = page.indexOf("id=\"__VIEWSTATE\" value=\"") + 24;
		int pos2 = page.indexOf("\"", pos1);
		Post1 = page.substring(pos1, pos2);

		pos1 = page.indexOf("id=\"__EVENTVALIDATION\" value=\"") + 30;
		pos2 = page.indexOf("\"", pos1);
		Post2 = page.substring(pos1, pos2);

		sendPost(URL, Post1, Post2, cookieStore);

	}

	private void sendPost(String Url, String Post1, String Post2, CookieStore cookieStore) {
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
			nameValuePairs.add(new BasicNameValuePair("__VIEWSTATE", Post1));
			nameValuePairs.add(new BasicNameValuePair("__EVENTVALIDATION", Post2));
			nameValuePairs.add(new BasicNameValuePair("uxAuthorizationButton", "Get+Authorization"));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost, localContext);
			// if (response.getStatusLine().getStatusCode() == 302) { todo handle moved,...

			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			try {
				while ((line = reader.readLine()) != null) {
					builder.append(line).append("\n");
				}
			} catch (Exception e) {

				e.printStackTrace();
			}

			String page = builder.toString();

			if (page.contains("<input name=\"ctl00$ContentBody$uxUserName\""))// Vielleicht haben wir schon die richtige seite
			{

				// response.getEffectiveURI()
				// response.setHeader( "Location", url );
				Url = "https://www.geocaching.com/mobileoauth/SignIn.aspx?&redir=http%3a%2f%2fwww.geocaching.com%2foauth%2fMobileAuthorize.aspx%3flocale%3den-US&pc=Team+CacheBox&pa=CacheBox+for+Android&pg=8edfa2c9-e2d1-474c-9cbc-22fde4debfe8";

				// Url = entity.getContentType().

				AskForUserPW(Url, cookieStore, page);
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

		} catch (ClientProtocolException e) {

		} catch (IOException e) {

		}

	}

	private void nextStep(final String Url, final CookieStore cookieStore) {
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
				closeWaitDialog();
				e.printStackTrace();
			}

		} catch (ClientProtocolException e) {
			closeWaitDialog();
			e.printStackTrace();
		} catch (IOException e) {
			closeWaitDialog();
			e.printStackTrace();
		} catch (IllegalStateException e) {
			closeWaitDialog();
			e.printStackTrace();
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
			System.out.println("Move");
			nextStep(moveUrl, cookieStore);
		} else {
			AskForUserPW(Url, cookieStore, page); // ?
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
			public void isCanceld() {
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
		Config.GcAPI.setEncryptedValue(accessToken);
		Config.AcceptChanges();

		String act = Config.GetAccessToken();
		if (act.length() > 0) {
			int status = GroundspeakAPI.GetMembershipType(null);
			if (status >= 0) {

				Config.GcLogin.setValue(GroundspeakAPI.MemberName);
				Config.AcceptChanges();

			}

		}

		closeWaitDialog();

		SettingsActivity.resortList();

	}
}
