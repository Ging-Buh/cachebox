package de.droidcachebox.Views.Forms;

import CB_Core.Config;
import CB_Core.Api.CB_Api;
import CB_Core.Api.GroundspeakAPI;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import de.droidcachebox.Global;
import de.droidcachebox.R;
import de.droidcachebox.Ui.ActivityUtils;

public class GcApiLogin extends Activity
{
	private static GcApiLogin gcApiLogin;
	private static ProgressDialog pd;
	private static boolean pdIsShow = false;

	public void onCreate(Bundle savedInstanceState)
	{
		ActivityUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gcapilogin);
		gcApiLogin = this;

		String GC_AuthUrl;

		if (Config.settings.OverrideUrl.getValue().equals(""))
		{
			GC_AuthUrl = CB_Api.getGcAuthUrl();
		}
		else
		{
			GC_AuthUrl = Config.settings.OverrideUrl.getValue();
		}

		if (GC_AuthUrl.equals(""))
		{
			finish();
		}

		if (!pdIsShow)
		{
			pd = ProgressDialog.show(gcApiLogin, "", "Loading....", true);
			pdIsShow = true;
		}

		View titleView = getWindow().findViewById(android.R.id.title);
		if (titleView != null)
		{
			ViewParent parent = titleView.getParent();
			if (parent != null && (parent instanceof View))
			{
				View parentView = (View) parent;
				parentView.setBackgroundColor(Global.getColor(R.attr.TitleBarBackColor));
			}
		}

		final WebView webView = (WebView) this.findViewById(R.id.gal_WebView);

		webView.setWebViewClient(new WebViewClient()
		{

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon)
			{
				gcApiLogin.setTitle("Loading...");

				if (!pdIsShow)
				{
					pd = ProgressDialog.show(gcApiLogin, "", "Loading....", true);
					pdIsShow = true;
				}

				super.onPageStarted(view, url, favicon);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{

				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url)
			{

				gcApiLogin.setTitle(R.string.app_name);
				if (pd != null) pd.dismiss();
				pdIsShow = false;

				if (url.toLowerCase().contains("oauth_verifier=") && (url.toLowerCase().contains("oauth_token=")))
				{
					webView.loadUrl("javascript:window.HTMLOUT.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
				}
				else
					super.onPageFinished(view, url);
			}

		});
		WebSettings settings = webView.getSettings();

		// settings.setPluginsEnabled(true);
		settings.setJavaScriptEnabled(true);
		// settings.setJavaScriptCanOpenWindowsAutomatically(true);

		// webView.setWebChromeClient(new WebChromeClient());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

		webView.loadUrl(GC_AuthUrl);
	}

	class MyJavaScriptInterface
	{

		public void showHTML(String html)
		{

			String search = "Access token: ";
			int pos = html.indexOf(search);
			if (pos < 0) return;
			int pos2 = html.indexOf("</span>", pos);
			if (pos2 < pos) return;
			// zwischen pos und pos2 sollte ein gültiges AccessToken sein!!!
			final String accessToken = html.substring(pos + search.length(), pos2);

			Thread thread = new Thread()
			{
				public void run()
				{
					GroundspeakAPI.CacheStatusValid = false;
					GroundspeakAPI.CacheStatusLiteValid = false;

					// store the encrypted AccessToken in the Config file
					// wir bekommen den Key schon verschlüsselt, deshalb muss er
					// nicht noch einmal verschlüsselt werden!
					Config.settings.GcAPI.setEncryptedValue(accessToken);
					Config.AcceptChanges();

					String act = Config.GetAccessToken();
					if (act.length() > 0)
					{
						int status = GroundspeakAPI.GetMembershipType(act);
						if (status >= 0)
						{

							Config.settings.GcLogin.setValue(GroundspeakAPI.MemberName);
							Config.AcceptChanges();

						}

					}

					onlineSearchReadyHandler.sendMessage(onlineSearchReadyHandler.obtainMessage(1));
				}
			};
			pd = ProgressDialog.show(gcApiLogin, "", "Download Username", true);
			thread.start();
		}
	}

	private Handler onlineSearchReadyHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 1:
			{
				pd.dismiss();
				gcApiLogin.finish();
			}
			}
		}
	};
}
